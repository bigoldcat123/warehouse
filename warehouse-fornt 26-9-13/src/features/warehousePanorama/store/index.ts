import { ref } from 'vue'
import { defineStore } from 'pinia'
import receiverDataApi from '@/api/receiverData'
import xunzhengApi from '@/api/xunzheng'

export type WarehousePanoramaHouseInfo = {
  modelIndex: number
  houseNo: string
  fumigationStatus: string
  oxygenConcentration: string
  cq2Concentration: string
  ph3Concentration: string
  collectedAt: string
  loading: boolean
  loadFailed: boolean
}

const configuredHouseNumbers = (import.meta.env.ENV_PANORAMA_HOUSE_NUMBERS || '')
  .split(',')
  .map((value) => value.trim())
  .filter(Boolean)

export const useWarehousePanoramaStore = defineStore('warehousePanorama', () => {
  const selectedHouse = ref<WarehousePanoramaHouseInfo>()

  function getHouseNo(modelIndex: number) {
    return configuredHouseNumbers[modelIndex - 1] || ''
  }

  function getConfiguredHouses() {
    return configuredHouseNumbers.map((houseNo, index) => ({
      modelIndex: index + 1,
      houseNo,
    }))
  }

  async function selectHouse(modelIndex: number) {
    const houseNo = getHouseNo(modelIndex)
    selectedHouse.value = {
      modelIndex,
      houseNo,
      fumigationStatus: '加载中...',
      oxygenConcentration: '--',
      cq2Concentration: '--',
      ph3Concentration: '--',
      collectedAt: '--',
      loading: true,
      loadFailed: false,
    }
    if (!houseNo) {
      selectedHouse.value.loading = false
      selectedHouse.value.loadFailed = true
      return
    }

    const [fumigationResult, environmentResult] = await Promise.allSettled([
      xunzhengApi.isFumigating(houseNo),
      receiverDataApi.latest(houseNo),
    ])
    if (selectedHouse.value?.modelIndex !== modelIndex) return

    let failed = false
    if (fumigationResult.status === 'fulfilled' && fumigationResult.value.data.code === '200') {
      selectedHouse.value.fumigationStatus = fumigationResult.value.data.value ? '正在熏蒸' : '未熏蒸'
    } else {
      selectedHouse.value.fumigationStatus = '--'
      failed = true
    }

    if (environmentResult.status === 'fulfilled' && environmentResult.value.data.code === '200') {
      const latest = environmentResult.value.data.value
      if (latest) {
        selectedHouse.value.oxygenConcentration = formatValue(latest.oAir, '%')
        selectedHouse.value.cq2Concentration = formatValue(latest.co2Air, ' ppm')
        selectedHouse.value.ph3Concentration = formatPh3(latest.ph3)
        selectedHouse.value.collectedAt = latest.testDate || '--'
      }
    } else {
      failed = true
    }
    selectedHouse.value.loading = false
    selectedHouse.value.loadFailed = failed
  }

  function formatValue(value: number | null, unit: string) {
    return value === null || value === undefined ? '--' : `${value}${unit}`
  }

  function formatPh3(value: string | null) {
    if (value === null || value === undefined || value.trim() === '') return '--'
    const numericValue = Number(value)
    return `${Number.isFinite(numericValue) ? numericValue.toFixed(2) : value} ppm`
  }

  function clearSelection() {
    selectedHouse.value = undefined
  }

  return {
    selectedHouse,
    getHouseNo,
    getConfiguredHouses,
    selectHouse,
    clearSelection,
  }
})
