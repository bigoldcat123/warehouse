<template>
  <div ref="rootRef" class="w-full h-full bg-[#0b1b2b] relative overflow-hidden select-none">
    <!-- 纯 canvas 手绘：平滑色场 + 坐标轴 + 色标 -->
    <canvas
      ref="canvasRef"
      class="block w-full h-full"
      @mousemove="onHover"
      @mouseleave="onLeave"
    />

    <!-- 悬浮提示（HTML 覆盖层，随鼠标移动） -->
    <div
      v-if="tooltip"
      class="absolute z-20 pointer-events-none rounded border border-[#2a4a6a] bg-[#10243a]/95 px-2 py-1 text-xs text-[#d6e4f0] shadow-lg whitespace-nowrap"
      :style="{ left: `${tooltip.x}px`, top: `${tooltip.y}px` }"
    >
      <div class="text-[#9fb8cc]">{{ tooltip.head }}</div>
      <div v-if="tooltip.value !== null">{{ cfg.label }}：<b class="text-[#7ec3ff]">{{ tooltip.value }}</b></div>
      <div v-else class="text-[#9fb8cc]">无数据</div>
    </div>

    <!-- 控制面板 -->
    <div v-if="panelOpen" class="absolute top-4 left-4 w-[260px] rounded-lg bg-[#10243a]/90 border border-[#2a4a6a] text-[#d6e4f0] p-4 shadow-lg backdrop-blur">
      <div class="flex items-center justify-between mb-3">
        <div class="text-base font-bold text-[#7ec3ff]">{{ cfg.title }}</div>
        <button
          @click="panelOpen = false"
          class="flex items-center justify-center w-6 h-6 rounded hover:bg-[#2a4a6a] cursor-pointer transition-colors"
          title="收起"
        >
          <svg viewBox="0 0 24 24" class="w-4 h-4 fill-none stroke-current stroke-2 text-[#9fb8cc]"><polyline points="15 18 9 12 15 6" /></svg>
        </button>
      </div>

      <div class="space-y-2 text-sm">
        <div class="flex items-center justify-between">
          <span>粮仓编号</span>
          <span class="text-[#7ec3ff]">{{ houseNo || '--' }}</span>
        </div>
        <div class="flex items-center justify-between">
          <span>采集时间</span>
          <el-select v-model="currentTime" size="small" class="w-[150px]" placeholder="选择时间" @wheel.prevent="onTimeWheel" title="滚轮切换">
            <el-option v-for="t in store.times" :key="t" :label="formatTime(t)" :value="t" />
          </el-select>
        </div>
        <div class="flex items-center justify-between">
          <span>切片方向</span>
          <el-select v-model="orientation" size="small" class="w-[120px]" @wheel.prevent="onOrientationWheel" title="滚轮切换">
            <el-option label="水平·XY层" value="xy" />
            <el-option label="垂直·XZ列" value="xz" />
            <el-option label="垂直·YZ行" value="yz" />
          </el-select>
        </div>
        <div class="flex items-center justify-between">
          <span>切片位置</span>
          <el-select v-model="sliceIndex" size="small" class="w-[120px]" @wheel.prevent="onSliceWheel" title="滚轮切换">
            <el-option v-for="n in sliceCount" :key="n" :label="`第 ${n} ${sliceUnit}`" :value="n - 1" />
          </el-select>
        </div>
      </div>

      <div class="mt-3 pt-3 border-t border-[#2a4a6a] text-xs space-y-1 text-[#9fb8cc]">
        <div class="flex justify-between"><span>维度 X/Y/Z</span><span>{{ store.dimX }} × {{ store.dimY }} × {{ store.dimZ }}</span></div>
      </div>
    </div>

    <!-- 收起后的展开按钮 -->
    <button
      v-else
      @click="panelOpen = true"
      class="absolute top-4 left-4 w-9 h-9 rounded-lg bg-[#10243a]/90 border border-[#2a4a6a] text-[#9fb8cc] flex items-center justify-center shadow-lg backdrop-blur cursor-pointer hover:bg-[#1a3a5a] transition-colors"
      title="展开控制面板"
    >
      <svg viewBox="0 0 24 24" class="w-5 h-5 fill-none stroke-current stroke-2"><polyline points="9 18 15 12 9 6" /></svg>
    </button>

    <!-- 加载/空状态提示 -->
    <div v-if="store.loading" class="absolute inset-0 z-10 flex items-center justify-center text-sm text-[#7ec3ff] bg-[#0b1b2b]/70">
      {{ cfg.label }}数据加载中...
    </div>
    <div v-else-if="!houseNo" class="absolute inset-0 z-10 flex items-center justify-center text-sm text-[#9fb8cc] bg-[#0b1b2b]/70">
      缺少仓房编号，无法加载{{ cfg.label }}数据
    </div>
    <div v-else-if="store.error" class="absolute inset-0 z-10 flex items-center justify-center text-sm text-[#ff7a59] bg-[#0b1b2b]/70">
      {{ store.error }}
    </div>
    <div v-else-if="!store.times.length" class="absolute inset-0 z-10 flex items-center justify-center text-sm text-[#9fb8cc] bg-[#0b1b2b]/70">
      暂无{{ cfg.label }}数据
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted, onBeforeUnmount } from 'vue'
import { useCubeStore, CUBE_METRICS, type CubeKind } from '@/stores/cube'
import { sliceCube, type CubeSliceOrientation } from '@/utils/cubeSlice'
import { renderCubeHeatCanvas, paletteAt } from '@/utils/cubeHeatCanvas'

const props = defineProps<{ houseNo: string; metric?: CubeKind }>()

const store = useCubeStore(props.metric ?? 'temperature')

const cfg = computed(() => CUBE_METRICS[props.metric ?? 'temperature'])

const rootRef = ref<HTMLDivElement | null>(null)
const canvasRef = ref<HTMLCanvasElement | null>(null)

const currentTime = computed<string | undefined>({
  get: () => store.currentTime ?? undefined,
  set: (v) => { if (v) store.selectTime(v) },
})

const orientation = ref<CubeSliceOrientation>('xy')
const sliceIndex = ref(0)
const panelOpen = ref(true)

const sliceCount = computed(() =>
  orientation.value === 'xy' ? store.dimZ : orientation.value === 'xz' ? store.dimY : store.dimX,
)
const sliceUnit = computed(() =>
  orientation.value === 'xy' ? '层' : orientation.value === 'xz' ? '列' : '行',
)

const slice = computed(() => sliceCube(store.currentData, orientation.value, sliceIndex.value))

/** 轴名：xName/yName */
const axisNames = computed(() => {
  if (orientation.value === 'xy') return { x: '列', y: '行' }
  if (orientation.value === 'xz') return { x: '层', y: '行' }
  return { x: '层', y: '列' }
})

// 方向切换或数据维度变化时收敛越界
watch(
  () => [orientation.value, store.currentTime, store.dimZ, store.dimX, store.dimY] as const,
  () => {
    sliceIndex.value = Math.min(sliceIndex.value, Math.max(0, sliceCount.value - 1))
  },
)

function formatTime(t: string) {
  return t.replace('T', ' ')
}
function onTimeWheel(e: WheelEvent) {
  const times = store.times
  if (!times.length) return
  const idx = times.indexOf(store.currentTime ?? '')
  const next = (idx + (e.deltaY > 0 ? 1 : -1) + times.length) % times.length
  store.selectTime(times[next])
}

function onOrientationWheel(e: WheelEvent) {
  const opts: CubeSliceOrientation[] = ['xy', 'xz', 'yz']
  const idx = opts.indexOf(orientation.value)
  const next = (idx + (e.deltaY > 0 ? 1 : -1) + opts.length) % opts.length
  orientation.value = opts[next]
}

function onSliceWheel(e: WheelEvent) {
  const opts = Array.from({ length: sliceCount.value }, (_, i) => i)
  const idx = opts.indexOf(sliceIndex.value)
  const next = (idx + (e.deltaY > 0 ? 1 : -1) + opts.length) % opts.length
  sliceIndex.value = opts[next]
}

/* ---------------- 纯 canvas 绘制 ---------------- */

/** 图面边距（CSS px）：右侧留给色标 */
const MARGIN = { top: 24, right: 78, bottom: 40, left: 46 }
/** 数据绘图区（CSS px），由 draw() 更新，hover 计算复用 */
let plot = { x: 0, y: 0, w: 0, h: 0 }

interface HoverCell { r: number; c: number }
const hover = ref<HoverCell | null>(null)
interface Tip { head: string; value: string | null; x: number; y: number }
const tooltip = ref<Tip | null>(null)

function fmtTick(v: number) {
  return Number.isInteger(v) ? String(v) : v.toFixed(1)
}

function draw() {
  const canvas = canvasRef.value
  const root = rootRef.value
  if (!canvas || !root) return
  const W = root.clientWidth
  const H = root.clientHeight
  if (W === 0 || H === 0) return
  const dpr = Math.min(window.devicePixelRatio || 1, 2)
  canvas.width = Math.round(W * dpr)
  canvas.height = Math.round(H * dpr)
  const ctx = canvas.getContext('2d')
  if (!ctx) return
  ctx.setTransform(dpr, 0, 0, dpr, 0, 0)

  // 背景
  ctx.fillStyle = '#0b1b2b'
  ctx.fillRect(0, 0, W, H)

  const s = slice.value
  const cols = s.cols.length
  const rows = s.rows.length
  if (cols === 0 || rows === 0) return

  plot = {
    x: MARGIN.left,
    y: MARGIN.top,
    w: Math.max(10, W - MARGIN.left - MARGIN.right),
    h: Math.max(10, H - MARGIN.top - MARGIN.bottom),
  }

  // 热力色场：二维数组 → 小方格 → 高斯模糊卷积扩散
  const field = renderCubeHeatCanvas(
    s,
    { min: cfg.value.min, max: cfg.value.max },
    Math.round(plot.w),
    Math.round(plot.h),
  )
  if (field) {
    ctx.imageSmoothingEnabled = true
    ctx.drawImage(field, plot.x, plot.y, plot.w, plot.h)
  }

  const cellW = plot.w / cols
  const cellH = plot.h / rows

  // 坐标轴线（左 + 下）
  ctx.strokeStyle = 'rgba(255,255,255,0.15)'
  ctx.lineWidth = 1
  ctx.beginPath()
  ctx.moveTo(plot.x, plot.y)
  ctx.lineTo(plot.x, plot.y + plot.h)
  ctx.lineTo(plot.x + plot.w, plot.y + plot.h)
  ctx.stroke()

  // 刻度标签：格心对齐，过密时按间隔抽稀
  ctx.fillStyle = '#9fb8cc'
  ctx.font = '11px sans-serif'
  const xStride = Math.max(1, Math.ceil(cols / Math.max(1, Math.floor(plot.w / 30))))
  ctx.textAlign = 'center'
  ctx.textBaseline = 'top'
  for (let c = 0; c < cols; c += xStride) {
    ctx.fillText(s.cols[c], plot.x + (c + 0.5) * cellW, plot.y + plot.h + 6)
  }
  const yStride = Math.max(1, Math.ceil(rows / Math.max(1, Math.floor(plot.h / 20))))
  ctx.textAlign = 'right'
  ctx.textBaseline = 'middle'
  for (let r = 0; r < rows; r += yStride) {
    ctx.fillText(s.rows[r], plot.x - 8, plot.y + (r + 0.5) * cellH)
  }

  // 轴名
  const names = axisNames.value
  ctx.fillStyle = '#9fb8cc'
  ctx.textAlign = 'center'
  ctx.textBaseline = 'bottom'
  ctx.fillText(names.x, plot.x + plot.w / 2, H - 4)
  ctx.save()
  ctx.translate(12, plot.y + plot.h / 2)
  ctx.rotate(-Math.PI / 2)
  ctx.textBaseline = 'top'
  ctx.fillText(names.y, 0, 0)
  ctx.restore()

  // 色标：垂直渐变条（下 min → 上 max）+ min/mid/max 刻度 + 单位
  const lgX = plot.x + plot.w + 18
  const lgW = 12
  const lgY = plot.y
  const lgH = plot.h
  const grad = ctx.createLinearGradient(0, lgY + lgH, 0, lgY)
  for (let i = 0; i <= 8; i++) {
    const t = i / 8
    const [R, G, B] = paletteAt(t)
    grad.addColorStop(t, `rgb(${R},${G},${B})`)
  }
  ctx.fillStyle = grad
  ctx.fillRect(lgX, lgY, lgW, lgH)
  ctx.strokeStyle = 'rgba(255,255,255,0.15)'
  ctx.lineWidth = 1
  ctx.strokeRect(lgX + 0.5, lgY + 0.5, lgW - 1, lgH - 1)
  ctx.fillStyle = '#9fb8cc'
  ctx.textAlign = 'left'
  ctx.textBaseline = 'middle'
  const lx = lgX + lgW + 5
  ctx.fillText(fmtTick(cfg.value.max), lx, lgY)
  ctx.fillText(fmtTick((cfg.value.min + cfg.value.max) / 2), lx, lgY + lgH / 2)
  ctx.fillText(fmtTick(cfg.value.min), lx, lgY + lgH)
  ctx.textBaseline = 'bottom'
  ctx.fillText(cfg.value.unit, lgX, lgY - 6)

  // 悬浮格高亮
  const hv = hover.value
  if (hv && hv.r >= 0 && hv.r < rows && hv.c >= 0 && hv.c < cols) {
    ctx.strokeStyle = 'rgba(255,255,255,0.9)'
    ctx.lineWidth = 1.5
    ctx.strokeRect(plot.x + hv.c * cellW + 0.5, plot.y + hv.r * cellH + 0.5, cellW - 1, cellH - 1)
  }
}

function onHover(e: MouseEvent) {
  const root = rootRef.value
  const s = slice.value
  const cols = s.cols.length
  const rows = s.rows.length
  if (!root || cols === 0 || rows === 0) return
  const rect = root.getBoundingClientRect()
  const mx = e.clientX - rect.left
  const my = e.clientY - rect.top
  const c = Math.floor((mx - plot.x) / (plot.w / cols))
  const r = Math.floor((my - plot.y) / (plot.h / rows))
  if (r < 0 || r >= rows || c < 0 || c >= cols) {
    if (hover.value) {
      hover.value = null
      tooltip.value = null
      draw()
    }
    return
  }
  hover.value = { r, c }
  const names = axisNames.value
  const v = s.grid[r]?.[c]
  tooltip.value = {
    head: `${names.y}${s.rows[r]} · ${names.x}${s.cols[c]}`,
    value: v === null || v === undefined ? null : `${v.toFixed(1)} ${cfg.value.unit}`,
    x: mx + 150 > root.clientWidth ? mx - 138 : mx + 12,
    y: my + 70 > root.clientHeight ? my - 60 : my + 12,
  }
  draw()
}

function onLeave() {
  hover.value = null
  tooltip.value = null
  draw()
}

watch(
  () => [slice.value, cfg.value] as const,
  () => nextTick(draw),
)

watch(() => props.houseNo, (no) => store.fetchCube(no), { immediate: true })

let resizeObserver: ResizeObserver | undefined

onMounted(() => {
  nextTick(draw)
  if (rootRef.value) {
    resizeObserver = new ResizeObserver(() => draw())
    resizeObserver.observe(rootRef.value)
  }
})

onBeforeUnmount(() => {
  resizeObserver?.disconnect()
  resizeObserver = undefined
})
</script>
