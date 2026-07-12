<template>
<div class="w-[320px] relative">
    <div ref="chart" class=" w-full h-[300px]"  ></div>
    <div class=" absolute w-full text-center bottom-7 text-[1rem]">{{p.name}}</div>
</div>
</template>
<script setup lang="ts">
import { ref } from 'vue';
import * as echarts from 'echarts';
import { onMounted } from 'vue';
import { watchEffect } from 'vue';
const p = defineProps<{
  name:string,
  value:number,
  unit:string,
  max:number
}>()
const chart = ref<HTMLDivElement | null>(null)
let myChart:any
let option = {
  tooltip: {
    formatter: '{a} <br/>{c}' + p.unit
  },
  series: [
    {
      name: p.name,
      type: 'gauge',
      axisLine: {
        lineStyle: {
          width: 10,
          color: [
            [0.3, '#67e0e3'],
            [0.7, '#37a2da'],
            [1, '#fd666d']
          ]
        }
      },
      pointer: {
        itemStyle: {
          color: 'auto'
        }
      },

      max:p.max,    // 最大值
      detail: {
        formatter: '{value}' + p.unit,
        fontSize:15
      },
      data: [
        {
          value: p.value,
        }
      ]
    }
  ]
};
onMounted(() => {
  myChart = echarts.init(chart.value);
  option && myChart.setOption(option);
})
watchEffect(() => {
  console.log(p.value)
  if (myChart) {
    option.series[0].data[0].value = p.value
    option && myChart.setOption(option);
  }
  // console.log(option)
})


</script>
