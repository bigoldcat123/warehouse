import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import data, { type type_TemperatureCube } from '@/api/data'

/** 单个测点：行/列/层(0-based) + 温度值 */
export interface SensorPoint {
  x: number // 行(0-based)
  y: number // 列(0-based)
  z: number // 层(0-based)
  value: number
}

/** 无效点位标识（接口文档：源数据未覆盖的点位返回 "-999"，前端识别为无效温度） */
export const INVALID_TEMP = -999

/**
 * 粮仓三维温度数据（接口 `/data/temperatureCube`）
 * value 为 Map：key 是采集时间(ISO-8601, 升序), value 是 [层 z][行 x][列 y] 的字符串温度(1 位小数)
 */
export const useTemperatureCubeStore = defineStore('temperatureCube', () => {
  const houseNo = ref('')
  const cube = ref<type_TemperatureCube>({})
  const loading = ref(false)
  const error = ref('')
  const currentTime = ref<string | null>(null)

  /** 采集时间列表（key 为 ISO-8601，接口升序返回） */
  const times = computed<string[]>(() => Object.keys(cube.value))

  /** 当前时间点的三维数组 [层 z][行 x][列 y] */
  const currentData = computed<string[][][] | null>(() => {
    const t = currentTime.value
    return t && cube.value[t] ? cube.value[t] : null
  })

  /** 维度：z=层数, x=行数, y=列数（来自仓房配置，由数据形状推导） */
  const dimZ = computed(() => currentData.value?.length ?? 0)
  const dimX = computed(() => currentData.value?.[0]?.length ?? 0)
  const dimY = computed(() => currentData.value?.[0]?.[0]?.length ?? 0)

  /** 当前时间点的有效测点（过滤 -999 无效点位，其余解析失败的点位也跳过） */
  const points = computed<SensorPoint[]>(() => {
    const arr = currentData.value
    if (!arr) return []
    const list: SensorPoint[] = []
    for (let z = 0; z < arr.length; z++) {
      const layer = arr[z] ?? []
      for (let x = 0; x < layer.length; x++) {
        const row = layer[x] ?? []
        for (let y = 0; y < row.length; y++) {
          const v = Number.parseFloat(row[y])
          if (Number.isNaN(v) || v === INVALID_TEMP) continue
          list.push({ x, y, z, value: v })
        }
      }
    }
    return list
  })

  /** 根据粮房编号获取每个采集时间点的三维温度数组 */
  async function fetchCube(no: string) {
    if (!no) return
    houseNo.value = no
    loading.value = true
    error.value = ''
    try {
      const res = await data.temperatureCube(no)
      cube.value = res.data?.value ?? {}
      const keys = Object.keys(cube.value)
      currentTime.value = keys.length ? keys[0] : null
    } catch (e) {
      cube.value = {}
      currentTime.value = null
      error.value = (e as Error)?.message || String(e) || '加载失败'
    } finally {
      loading.value = false
    }
  }

  function selectTime(t: string) {
    if (cube.value[t]) currentTime.value = t
  }

  function clear() {
    houseNo.value = ''
    cube.value = {}
    currentTime.value = null
    loading.value = false
    error.value = ''
  }

  return { houseNo, cube, loading, error, currentTime, times, currentData, dimX, dimY, dimZ, points, fetchCube, selectTime, clear }
})
