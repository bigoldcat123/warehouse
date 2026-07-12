<template>
<div class=" flex justify-evenly">
    <Gauge unit="kWh" name="空调每小时用电量" :value="data?.airHourElec ?? 0" :max="50"></Gauge>
    <Gauge unit="kWh" name="小时耗电量" :value="data?.meterElec ?? 0" :max="100"></Gauge>
</div>
<div class="flex justify-center">
    <Gauge unit="kWh" name="光伏每小时发电量" :value="data?.pvElec ?? 0" :max="500"></Gauge>
</div>
<ElButton @click="$router.back()" class=" absolute z-50 top-10 left-10">返回</ElButton>
</template>
<script setup lang="ts">
import Gauge from '@/components/charts/Gauge.vue'
import { useCurrentUserStore } from '@/stores/currentUser'
import { useCurrentWareHouse } from '@/stores/currentWareHouse'
import type { ElButton } from 'element-plus/lib'
import { onMounted ,onUnmounted} from 'vue'
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import gf_kt, { type GfktItem } from '@/api/gf_kt'

const ware_house = useCurrentWareHouse()
const ware_house_no = ware_house.getWareHouse().wareHouseNO
const house_no = useRoute().query.houseno as string
const store = useCurrentUserStore()
const data = ref<GfktItem | null>(null)
// const s_data_air_elec = ref(0)
// const s_data_elec = ref(0)
// const s_data_elec_gen = ref(0)

const fetch_data = async () => {
  const res = (await gf_kt.getOneByHouseNo(house_no)).data.value
  data.value = res
}
const interval = ref(0)
onMounted(() => {
  interval.value = setInterval(() => {
    fetch_data()
  },1000)

})
onUnmounted(() => {
  clearInterval(interval.value)
})
// console.log([store.getToken() ?? "123",ware_house_no ?? "123",house_no ??"123"])
//   const ws = new WebSocket(`ws://127.0.0.1:8080/ws?t=${store.getToken()}&w=${ware_house_no}&h=${house_no}`)

//   ws.onerror = e => {
//     console.log(e)
//   }
//   ws.onopen = (e) => {
//     console.log(e)
//   }
//   ws.onmessage = (e) => {
//     console.log(e.data)
//     const res = JSON.parse(e.data) as MqttDeviceInfoResponse
//     const d = parse_data(res)
//     s_data_air_elec.value = d.air_hour_elec ?? 0
//     s_data_elec.value = d.meter_elec ?? 0
//     s_data_elec_gen.value = d.pv_elec ?? 0
//   }
//   onUnmounted(() => {
//     ws.close()
//   })
//   function parse_data(raw:MqttDeviceInfoResponse) {
//     let air_hour_elec = undefined
//     let meter_elec = undefined
//     let pv_elec = undefined
//     let event_time = undefined
//     if (raw.devices.length > 0) {
//       const data = raw.devices[0].data
//       event_time = data.eventTime
//       pv_elec = data.PV_elec
//       if (data.air_data.length > 0) {
//         air_hour_elec = data.air_data[0].air_hour_elec
//       }
//       if (data.meter_data.length > 0) {
//         meter_elec = data.meter_data[0].meter_elec
//       }
//     }
//     return {
//       air_hour_elec,meter_elec,pv_elec,event_time
//     }

//   }
</script>
