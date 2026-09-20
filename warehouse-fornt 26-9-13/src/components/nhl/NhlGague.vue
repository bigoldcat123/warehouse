<template>
<div ref="gague" class=" flex-1 h-full"></div>
</template>
<script setup lang="ts">
import { onMounted, ref } from 'vue';
import * as echarts from 'echarts';
import { watchEffect } from 'vue';
const props = defineProps<{
  title:string,
  value:number,
  unit:string
}>()
const gague = ref<HTMLDivElement | null>(null);
const myChart = ref<echarts.ECharts | null>(null);
let option = {
  series: [
    {

      type: 'gauge',
      center: ['50%', '60%'],
      startAngle: 200,
      endAngle: -20,
      min: 0,
      max: 100,
      animation:false,
      splitNumber: 10,
      itemStyle: {
        color: 'lightblue'
      },
      progress: {
        show: true,
        width: 5
      },
      pointer: {
        show: false
      },
      axisLine: {
        lineStyle: {
          width: 5
        }
      },
      axisTick: {
        distance: 0,
        splitNumber: 1,
        lineStyle: {
          width: 1,
          color: 'yellow'
        }
      },
      splitLine: {
        distance: -5,
        length: 6,
        lineStyle: {
          width: 2,
          color: 'yellow'
        }
      },
      axisLabel: {
        distance: 14,
        color: 'green',
        fontSize: 8
      },
      anchor: {
        show: false
      },
      title: {
        show: false
      },
      detail: {
        valueAnimation: true,
        width: '60%',
        lineHeight: 10,
        borderRadius: 8,
        offsetCenter: [0, '-15%'],
        fontSize: 10,
        fontWeight: 'bolder',
        formatter: '{value} ' + props.unit + '\n' + props.title,
        color: 'inherit'
      },
      data: [
        {
          value: props.value
        }
      ]
    }
  ]
};
function cast_color(temperature:number): string {
  if (temperature <= 0) return "#00BFFF"; // 冰蓝色：极冷
  if (temperature <= 10) return "#1E90FF"; // 冷蓝
  if (temperature <= 20) return "#00FA9A"; // 绿青色：凉爽
  if (temperature <= 30) return "#FFD700"; // 金黄：温暖
  if (temperature <= 40) return "#FF8C00"; // 橙色：炎热
  return "#FF4500"; // 火红：极热
}
watchEffect(() => {
  option.series[0].itemStyle.color = cast_color(props.value)
  option.series[0].data[0].value = props.value;
  myChart.value?.setOption(option);
})
onMounted(() => {
  myChart.value = echarts.init(gague.value!);

  // myChart.value.setOption(option);
})
</script>
