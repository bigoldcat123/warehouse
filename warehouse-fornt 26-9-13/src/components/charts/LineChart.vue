<template>
<div class=" w-[45vw]">
    <div class="w-[45vw] h-[45vh]" ref="chart"></div>
    <div class=" w-full text-center">{{p.title}}</div>
</div>
</template>
<script setup lang="ts">
import { onMounted } from 'vue';
import * as echarts from 'echarts';
import { ref } from 'vue'
import { onUpdated } from 'vue';
import { watch } from 'vue';
import { watchEffect } from 'vue';

const p = defineProps<{
  x_data:any[],
  s_data:any[],
  title?:string
}>()

const chart = ref<HTMLDivElement | null>(null)
let t = 0;
let myChart:echarts.ECharts | undefined
let option = {
  xAxis: {
    type: 'category',
    data: p.x_data,
    min: 0,
    max: 10, // 初始窗口大小
  },
  yAxis: {
    type: 'value'
  },
  series: [
    {
      data: p.s_data,
      type: 'line',
      showSymbol: false,
      animationEasing: 'linear'
    }
  ]
};
onMounted(() => {
  let chartDom = chart.value
  myChart = echarts.init(chartDom);
  option && myChart!.setOption(option);
})
watchEffect(()=> {
  console.log(p.s_data.length,p.x_data.length)

  if (myChart) {
    option.xAxis.min = t - 9;
    option.xAxis.max = t;
    option && myChart!.setOption(option);
    t += 1;
  }
})



</script>
