import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import type { AxiosResponse } from 'axios'
import data, { type type_TemperatureCube } from '@/api/data'

/** 单个测点：行/列/层(0-based) + 数值 */
export interface SensorPoint {
  x: number // 行(0-based)
  y: number // 列(0-based)
  z: number // 层(0-based)
  value: number
}

import { INVALID_TEMP } from '@/utils/cubeSlice'

/** 三维数组指标：温度 / 湿度 / 气体浓度 */
export type CubeKind = 'temperature' | 'humidity' | 'gas'

type CubeApi = (houseNo: string) => Promise<AxiosResponse<ResponseData<type_TemperatureCube>>>

const CUBE_API: Record<CubeKind, CubeApi> = {
  temperature: (houseNo) => data.temperatureCube(houseNo),
  humidity: (houseNo) => data.humidityCube(houseNo),
  gas: (houseNo) => data.gasCube(houseNo),
}

/** 指标展示配置（数值范围/单位/文案），数据逻辑三者一致 */
export interface CubeMetricConfig {
  kind: CubeKind
  min: number
  max: number
  unit: string
  label: string
  title: string
  subtitle: string
}

export const CUBE_METRICS: Record<CubeKind, CubeMetricConfig> = {
  temperature: { kind: 'temperature', min: 10, max: 40, unit: '℃', label: '温度', title: '3D粮仓温度展示', subtitle: '3D温度云图' },
  humidity: { kind: 'humidity', min: 20, max: 90, unit: '%', label: '湿度', title: '3D粮仓湿度展示', subtitle: '3D湿度云图' },
  gas: { kind: 'gas', min: 300, max: 3000, unit: 'ppm', label: '气体浓度', title: '3D粮仓气体浓度展示', subtitle: '3D浓度云图' },
}

/**
 * 粮仓三维数值数组 store（接口 `/data/temperatureCube|humidityCube|gasCube`）
 * value 为 Map：key 是采集时间(ISO-8601, 升序), value 是 [层 z][行 x][列 y] 的字符串数值(1 位小数)
 */
function createCubeStore(id: string, kind: CubeKind) {
  return defineStore(id, () => {
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

    /** 根据粮房编号获取每个采集时间点的三维数组 */
    async function fetchCube(no: string) {
      if (!no) return
      houseNo.value = no
      loading.value = true
      error.value = ''
      try {
        const res = await CUBE_API[kind](no)
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
}

export const useTemperatureCubeStore = createCubeStore('temperatureCube', 'temperature')
export const useHumidityCubeStore = createCubeStore('humidityCube', 'humidity')
export const useGasCubeStore = createCubeStore('gasCube', 'gas')

/** 按指标取对应 store（三个 store 结构一致） */
export function useCubeStore(kind: CubeKind) {
  switch (kind) {
    case 'humidity':
      return useHumidityCubeStore()
    case 'gas':
      return useGasCubeStore()
    default:
      return useTemperatureCubeStore()
  }
}
