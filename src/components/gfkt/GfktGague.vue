<template>
<div class="flex items-center justify-center flex-col">
    <el-progress :format="(n:number)=>`${props.value} ${props.unit}`"  :stroke-width="10" type="dashboard" :percentage="(props.value / props.max) * 100" :color="cast_color(props.value)" />
    <p :style="{'color':cast_color(props.value)}">{{props.title}}</p>
</div>
</template>
<script setup lang="ts">
const props = defineProps<{
  title:string,
  value:number,
  unit:string,
  max:number
}>()


function cast_color(temperature: number): string {
  const clamp = (v: number, min: number, max: number) => Math.min(Math.max(v, min), max);
  const t = clamp((temperature + 10) / 50, 0, 1); // 映射到 0~1
  const r = Math.floor(255 * t);
  const g = Math.floor(255 * (1 - Math.abs(t - 0.5) * 2));
  const b = Math.floor(255 * (1 - t));
  return `rgb(${r}, ${g}, ${b})`;
}

</script>
