import type { CubeSlice } from './cubeSlice'

/**
 * 二维热力图平滑色场渲染工具（零依赖，纯 canvas 手绘，不引入 vue/echarts/three）。
 *
 * 算法（小方格 + 高斯模糊卷积扩散）：
 * 1. 栅格化：把原始二维数组按格画成实心小方格（低分辨率工作位图，
 *    每格 cellPx×cellPx 像素），有效格写入归一化值 t、权重 1，无效格权重 0；
 * 2. 高斯模糊：对「值通道」和「权重通道」分别做可分离高斯卷积
 *    （横向一趟 + 纵向一趟），sigma 与格边长成正比 → 值由格心向四周扩散；
 * 3. 归一化着色：t = blur(值) / blur(权重)，逐像素查 5 段色板；
 *    覆盖率（模糊后的权重）决定透明度——无数据区域由邻近值扩散填充，
 *    远离任何测点的像素保持暗底透明。
 *
 * PALETTE 同时用于色场着色与 CubeHeatmap 手绘色标，保证图面与 legend 对齐。
 */

 /** 色板端点（色场与色标共用） */
const PALETTE: ReadonlyArray<readonly [number, number, number]> = [
  [43, 108, 255], // #2b6cff
  [34, 195, 214], // #22c3d6
  [62, 207, 90], // #3ecf5a
  [242, 197, 49], // #f2c531
  [240, 67, 58], // #f0433a
]

/** 暗底背景色（无数据区域） */
const BG = '#0b1b2b'

/** 工作位图最长边像素（卷积成本控制；最终画布经平滑放大到目标尺寸） */
const WORK_MAX_SIDE = 384
/** 每格最小/最大工作像素边长 */
const CELL_PX_MIN = 6
const CELL_PX_MAX = 24
/** 模糊强度：sigma = SIGMA_PER_CELL × 格边长（工作像素） */
const SIGMA_PER_CELL = 0.6

/**
 * t∈[0,1] → 5 段线性插值色板，返回 [r, g, b]（0-255）。
 * 越界 t 收敛到端点。
 */
export function paletteAt(t: number): [number, number, number] {
  const u = (t < 0 ? 0 : t > 1 ? 1 : t) * (PALETTE.length - 1)
  const i = Math.min(Math.floor(u), PALETTE.length - 2)
  const f = u - i
  const a = PALETTE[i]
  const b = PALETTE[i + 1]
  return [
    Math.round(a[0] + (b[0] - a[0]) * f),
    Math.round(a[1] + (b[1] - a[1]) * f),
    Math.round(a[2] + (b[2] - a[2]) * f),
  ]
}

/** 生成归一化一维高斯核，半径 = ceil(2.5 × sigma)（覆盖约 99% 能量） */
function gaussianKernel(sigma: number): Float32Array {
  const radius = Math.max(1, Math.ceil(2.5 * sigma))
  const k = new Float32Array(radius + 1)
  const inv = 1 / (2 * sigma * sigma)
  let sum = k[0] = 1
  for (let i = 1; i <= radius; i++) {
    k[i] = Math.exp(-i * i * inv)
    sum += 2 * k[i]
  }
  for (let i = 0; i <= radius; i++) k[i] /= sum
  return k
}

/**
 * 可分离高斯模糊：先横向卷积（src→tmp），再纵向卷积（tmp→src）。
 * 边缘按复制边界（clamp）处理。kernel 为 gaussianKernel 的输出。
 */
function gaussianBlurSeparable(src: Float32Array, w: number, h: number, kernel: Float32Array): void {
  const radius = kernel.length - 1
  const tmp = new Float32Array(src.length)

  // 横向
  for (let y = 0; y < h; y++) {
    const row = y * w
    for (let x = 0; x < w; x++) {
      let acc = src[row + x] * kernel[0]
      for (let i = 1; i <= radius; i++) {
        const xl = x - i < 0 ? 0 : x - i
        const xr = x + i >= w ? w - 1 : x + i
        acc += (src[row + xl] + src[row + xr]) * kernel[i]
      }
      tmp[row + x] = acc
    }
  }

  // 纵向
  for (let y = 0; y < h; y++) {
    const yl0 = y * w
    for (let x = 0; x < w; x++) {
      let acc = tmp[yl0 + x] * kernel[0]
      for (let i = 1; i <= radius; i++) {
        const yt = (y - i < 0 ? 0 : y - i) * w
        const yb = (y + i >= h ? h - 1 : y + i) * w
        acc += (tmp[yt + x] + tmp[yb + x]) * kernel[i]
      }
      src[yl0 + x] = acc
    }
  }
}

/**
 * 把原始二维数组栅格化为小方格，经高斯模糊卷积扩散后绘制成平滑色场，
 * 返回 width×height 的 canvas。slice 为空或 width/height ≤ 0 → null。
 */
export function renderCubeHeatCanvas(
  slice: CubeSlice,
  cfg: { min: number; max: number },
  width: number,
  height: number,
): HTMLCanvasElement | null {
  const cols = slice.cols.length
  const rows = slice.rows.length
  if (cols === 0 || rows === 0 || width <= 0 || height <= 0) return null

  // 工作分辨率：格数多时压低 cellPx 控制卷积规模
  const cellPx = Math.min(
    CELL_PX_MAX,
    Math.max(CELL_PX_MIN, Math.floor(WORK_MAX_SIDE / Math.max(cols, rows))),
  )
  const w = cols * cellPx
  const h = rows * cellPx

  // 1. 栅格化小方格：值通道（归一化 t）+ 权重通道（有效格 1，无效格 0）
  const val = new Float32Array(w * h)
  const wt = new Float32Array(w * h)
  const range = cfg.max - cfg.min
  for (let r = 0; r < rows; r++) {
    for (let c = 0; c < cols; c++) {
      const v = slice.grid[r]?.[c]
      if (v === null || v === undefined) continue
      const t = range > 0 ? (v - cfg.min) / range : 0.5
      const y0 = r * cellPx
      const x0 = c * cellPx
      for (let y = y0; y < y0 + cellPx; y++) {
        const base = y * w
        for (let x = x0; x < x0 + cellPx; x++) {
          val[base + x] = t
          wt[base + x] = 1
        }
      }
    }
  }

  // 2. 高斯模糊卷积（可分离，两趟）——值由小方格向四周扩散
  const kernel = gaussianKernel(SIGMA_PER_CELL * cellPx)
  gaussianBlurSeparable(val, w, h, kernel)
  gaussianBlurSeparable(wt, w, h, kernel)

  // 3. 归一化着色：t = 值/权重；覆盖率 → 透明度（远离测点处透明 → 暗底）
  const work = document.createElement('canvas')
  work.width = w
  work.height = h
  const wctx = work.getContext('2d')
  if (!wctx) return null
  const img = wctx.createImageData(w, h)
  const d = img.data
  for (let i = 0, p = 0; i < wt.length; i++, p += 4) {
    const coverage = wt[i]
    if (coverage < 0.02) continue // alpha 保持 0
    const [R, G, B] = paletteAt(val[i] / coverage)
    d[p] = R
    d[p + 1] = G
    d[p + 2] = B
    // 覆盖率 0.05→0 全透明，0.5→255 不透明，之间线性过渡
    const a = (coverage - 0.05) / 0.45
    d[p + 3] = a <= 0 ? 0 : a >= 1 ? 255 : Math.round(a * 255)
  }
  wctx.putImageData(img, 0, 0)

  // 4. 平滑放大到目标尺寸，铺暗底（无数据区域为暗底）
  const out = document.createElement('canvas')
  out.width = width
  out.height = height
  const octx = out.getContext('2d')
  if (!octx) return null
  octx.fillStyle = BG
  octx.fillRect(0, 0, width, height)
  octx.imageSmoothingEnabled = true
  octx.imageSmoothingQuality = 'high'
  octx.drawImage(work, 0, 0, width, height)
  return out
}
