<template>
<div class="flex justify-evenly items-center h-[180px] my-2">
    <div class="w-30 text-white">{{ props.info.houseName }}</div>
    <GfktGague :max="50" unit="kWh" :value="props.info.airHourElec ?? 0" title="空调每小时用电量"></GfktGague>
    <GfktGague :max="100" unit="kWh" :value="props.info.meterElec ?? 0" title="小时耗电量"></GfktGague>
    <GfktGague :max="500" unit="kWh" :value="props.info.pvElec ?? 0" title="光伏每小时发电量"></GfktGague>
</div>
</template>

<script setup lang="ts">
import type { GfktItem } from '@/api/gf_kt';
import GfktGague from './GfktGague.vue'
const props = defineProps<{
  info:GfktItem
}>()

</script>
