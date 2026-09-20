<template>
<div class="grid grid-cols-2">
    <LineChart :s_data="s_data_air_elec" :x_data="x_data" title="空调每小时用电量(kWh)"></LineChart>
    <LineChart :s_data="s_data_elec" :x_data="x_data" title="小时耗电量(kWh)"></LineChart>
    <LineChart :s_data="s_data_elec_gen" :x_data="x_data" title="光伏每小时发电量"></LineChart>
</div>


</template>
<script setup lang="ts">
import LineChart from '@/components/charts/LineChart.vue'
import { useCurrentUserStore } from '@/stores/currentUser'
import { useCurrentWareHouse } from '@/stores/currentWareHouse'
import { onMounted } from 'vue'
import { ref } from 'vue'
import { useRoute } from 'vue-router'

const ware_house = useCurrentWareHouse()
const ware_house_no = ware_house.getWareHouse().wareHouseNO
const house_no = useRoute().query.houseno as string
const store = useCurrentUserStore()

const s_data_air_elec = ref<any[]>([])
const s_data_elec = ref<any[]>([])
const s_data_elec_gen = ref<any[]>([])


const x_data = ref<any[]>([])
onMounted(() => {
  setInterval(() => {
    s_data_air_elec.value.push(Math.random() * 20)
    s_data_elec.value.push(Math.random() * 70)
    s_data_elec_gen.value.push(Math.random() * 300)
    x_data.value.push(1)
  },1000)

})


  // const ws = new WebSocket('ws://127.0.0.1:8080/ws',[store.getToken() ?? "",ware_house_no ?? "",house_no ??""])
  // ws.onopen = (e) => {
  //   console.log(e)
  // }
  // ws.onmessage = (e) => {
  //   console.log(e)
  // }
  // onUnmounted(() => {
  //   ws.close()
  // })
  function parse_data(raw:MqttDeviceInfoResponse) {
    let air_hour_elec = undefined
    let meter_elec = undefined
    let pv_elec = undefined
    let event_time = undefined
    if (raw.devices.length > 0) {
      const data = raw.devices[0].data
      event_time = data.eventTime
      pv_elec = data.PV_elec
      if (data.air_data.length > 0) {
        air_hour_elec = data.air_data[0].air_hour_elec
      }
      if (data.meter_data.length > 0) {
        meter_elec = data.meter_data[0].meter_elec
      }
    }
    return {
      air_hour_elec,meter_elec,pv_elec,event_time
    }

  }
</script>
