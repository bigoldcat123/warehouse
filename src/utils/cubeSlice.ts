/** 无效点位标识（接口文档：源数据未覆盖的点位返回 "-999"，前端识别为无效数值） */
export const INVALID_TEMP = -999

/** 切片方向：xy=水平(固定层z) / xz=垂直(固定列y) / yz=垂直(固定行x) */
export type CubeSliceOrientation = 'xy' | 'xz' | 'yz'

export interface CubeSlice {
  rows: string[]          // yAxis 标签（1 起始）
  cols: string[]          // xAxis 标签（1 起始）
  grid: (number | null)[][] // [row][col]，null = 无效点位
}

/** 解析单个格子字符串；NaN 或无效标记 → null，非字符串/越界 → null */
function parseCell(v: unknown): number | null {
  if (typeof v !== 'string') return null
  const n = Number.parseFloat(v)
  if (Number.isNaN(n) || n === INVALID_TEMP) return null
  return n
}

function labelRange(n: number): string[] {
  return Array.from({ length: n }, (_, i) => String(i + 1))
}

/**
 * 从三维数组 [层 z][行 x][列 y] 中切出一个面。
 * - 'xy'：index 为层 z，rows=行 x、cols=列 y，grid[x][y]
 * - 'xz'：index 为列 y，rows=行 x、cols=层 z，grid[x][z]
 * - 'yz'：index 为行 x，rows=列 y、cols=层 z，grid[y][z]
 */
export function sliceCube(cube: string[][][] | null, orientation: CubeSliceOrientation, index: number): CubeSlice {
  if (!cube || cube.length === 0) return { rows: [], cols: [], grid: [] }

  const dimZ = cube.length
  const dimX = cube[0]?.length ?? 0
  const dimY = cube[0]?.[0]?.length ?? 0

  const cell = (z: number, x: number, y: number): number | null => {
    const layer = cube[z]
    const row = layer?.[x]
    return row ? parseCell(row[y]) : null
  }

  if (orientation === 'xy') {
    const z = index
    const rows = labelRange(dimX)
    const cols = labelRange(dimY)
    const grid: (number | null)[][] = []
    for (let x = 0; x < dimX; x++) {
      const g: (number | null)[] = []
      for (let y = 0; y < dimY; y++) g.push(cell(z, x, y))
      grid.push(g)
    }
    return { rows, cols, grid }
  }

  if (orientation === 'xz') {
    const y = index
    const rows = labelRange(dimX)
    const cols = labelRange(dimZ)
    const grid: (number | null)[][] = []
    for (let x = 0; x < dimX; x++) {
      const g: (number | null)[] = []
      for (let z = 0; z < dimZ; z++) g.push(cell(z, x, y))
      grid.push(g)
    }
    return { rows, cols, grid }
  }

  // orientation === 'yz'：固定行 x
  const x = index
  const rows = labelRange(dimY)
  const cols = labelRange(dimZ)
  const grid: (number | null)[][] = []
  for (let y = 0; y < dimY; y++) {
    const g: (number | null)[] = []
    for (let z = 0; z < dimZ; z++) g.push(cell(z, x, y))
    grid.push(g)
  }
  return { rows, cols, grid }
}
