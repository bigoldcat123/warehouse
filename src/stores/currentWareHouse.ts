import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export type WareHouseStore = {
  waerhouseId?: string,
  wareHouseNO?: string,
  wareHouseName?: string
}
export const useCurrentWareHouse = defineStore('currentWareHouse', () => {
  const currentWarehouse = ref<WareHouseStore>({
    waerhouseId: '',
    wareHouseNO: '',
    wareHouseName: ''
  })
  const isInited = ref(false)
  function init(wareHouse: WareHouseStore) {
    if (!isInited.value){
      isInited.value = true
      setWareHouse(wareHouse)
    }
  }
  function setWareHouse(wareHouse: WareHouseStore) {
    if (!isInited.value)
      isInited.value = true
    if (wareHouse.waerhouseId)
      currentWarehouse.value.waerhouseId = wareHouse.waerhouseId
    if (wareHouse.wareHouseNO)
      currentWarehouse.value.wareHouseNO = wareHouse.wareHouseNO
    if (wareHouse.wareHouseName)
      currentWarehouse.value.wareHouseName = wareHouse.wareHouseName
  }

  function getWareHouse() {
    return currentWarehouse.value
  }
  function clear() {
    isInited.value = false
  }

  return { setWareHouse, getWareHouse,init ,clear}
})
