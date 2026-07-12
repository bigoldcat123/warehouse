<template>
<div class="bg-[#03153d] h-screen overflow-auto relative ">
    <div class=" h-12 bg-[#03153d] sticky top-0 flex items-center justify-center z-50">
        <ElButton class=" absolute left-2" @click="$router.back()">返回</ElButton>
        <img  src="/nhl/wk_title.png" />
    </div>
    <div class=" w-[80%] mx-auto">
         <NhlInfo v-for="item in data" :key="item.id" :nhl="item" />
    </div>
</div>
</template>
<script setup lang="ts">
import NhlInfo from '@/components/nhl/NhlInfo.vue';
import nhlstt,{type Nhlstt} from '@/api/nhlstt';
import { onMounted, ref ,onUnmounted} from  'vue';
import type { ElButton } from 'element-plus/lib';

const data = ref<Nhlstt[]>([]);

async function fetchData() {
    data.value = (await nhlstt.getall()).data.value;
}
fetchData();

// const intervalId = ref<number | null>(null);
// onMounted(() => {
//   intervalId.value = setInterval(() =>{
//     fetchData();
//   },1000)
// });
// onUnmounted(() => {
//   if (intervalId.value) {
//     clearInterval(intervalId.value);
//   }
// });


</script>
