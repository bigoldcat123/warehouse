<template>
    <div class="flex justify-evenly items-center h-[180px] my-2">
        <div class="w-30 text-white">{{ props.nhl.houseName }}</div>
        <img :src="props.nhl.run ? '/nhl/yunxing.png' : '/nhl/tingzhi.png'" class="h-16 w-16 ml-4" alt="" />
         <NhlGague2 unit="%" :value="props.nhl.inH ?? 0" title="仓湿"></NhlGague2>

         <NhlGague2 unit="℃" :value="props.nhl.inT ?? 0" title="仓温"></NhlGague2>
        <NhlGague2 unit="℃" :value="props.nhl.gt ?? 0" title="粮温"></NhlGague2>
        <NhlGague2 unit="℃" :value="props.nhl.hiTemp ?? 0" title="开机最高温度"></NhlGague2>
        <NhlGague2 unit="℃" :value="props.nhl.loTemp ?? 0" title="开机最低温"></NhlGague2>
    </div>
</template>
<script setup lang="ts">
import type { Nhlstt } from "@/api/nhlstt";
import NhlGague2 from "./NhlGague2.vue";

const props = defineProps<{
    nhl: Nhlstt;
}>();
</script>
