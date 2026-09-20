import { ref } from 'vue'
import { defineStore } from 'pinia'

export type WarehousePanoramaHouseInfo = {
  modelIndex: number
  houseNo: string
  fumigationStatus: string
  oxygenConcentration: string
  cq2Concentration: string
  ph3Concentration: string
  collectedAt: string
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

  function selectHouse(modelIndex: number) {
    selectedHouse.value = {
      modelIndex,
      houseNo: getHouseNo(modelIndex),
      fumigationStatus: '未熏蒸',
      oxygenConcentration: '20.8%',
      cq2Concentration: '420 ppm',
      ph3Concentration: '0.00 ppm',
      collectedAt: '2026-09-20 10:00:00',
    }
  }

  function clearSelection() {
    selectedHouse.value = undefined
  }

  return {
    selectedHouse,
    getHouseNo,
    selectHouse,
    clearSelection,
  }
})
