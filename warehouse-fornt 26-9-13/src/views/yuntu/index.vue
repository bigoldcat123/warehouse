<template>
    <PageFrame>
        <template #title>{{ currentWarehouse.getWareHouse().wareHouseName }}</template>
        <template #subtitle>云图播放</template>
        <template #actions>
            <div @click="$router.push('/house')" class="size-[20px] cursor-pointer"></div>
        </template>

        <div class="h-full overflow-y-auto p-5 flex flex-col items-center justify-center bg-[#011437] backdrop-blur">
            <div class="w-full max-w-[1000px] space-y-5">
                <!-- <el-carousel height="auto" autoplay>
                    <el-carousel-item :style="{height:h}" :key="url" v-for="url in urls">
                        <img :src="url" alt="内容图片" class="w-full h-auto shadow-xl border border-white/20" />
                    </el-carousel-item>
                </el-carousel> -->
                <Carousel :images="urls"></Carousel>
            </div>
        </div>
    </PageFrame>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import PageFrame from '@/components/common/PageFrame.vue';
import Carousel from '@/components/common/Carousel.vue';
import house from '@/api/house';
import { useCurrentWareHouse } from '@/stores/currentWareHouse';
import { api_PreFix } from '@/api';
const width = ref<number>(typeof window !== 'undefined' ? window.innerWidth : 0)
const height = ref<number>(typeof window !== 'undefined' ? window.innerHeight : 0)
const belowMin = computed(() => width.value < 1000 || height.value < 800)
const currentWarehouse = useCurrentWareHouse()
const urls = ref<Array<string>>([])
const h = ref('10px');
const map_fn = (x: string) => {
    if (x.startsWith('/') || x.startsWith('\\')) {
        return '/' + api_PreFix + '/static/' + x.substring(1).replace('\\', '/')
    } else {
        return '/' + api_PreFix + '/static/' + x.replace('\\', '/')
    }
};

async function fetch_yuntu() {
    const r = await house.listYuntu(currentWarehouse.getWareHouse().waerhouseId ?? '0')

    urls.value = r.data.value.map(map_fn)
    console.log(urls.value);
    h.value = 'auto'

}
function cal(url: string) {

}
fetch_yuntu()
function updateWindowSize() {
    width.value = window.innerWidth
    height.value = window.innerHeight
}

onMounted(() => {
    updateWindowSize()
    window.addEventListener('resize', updateWindowSize)
})

onBeforeUnmount(() => {
    window.removeEventListener('resize', updateWindowSize)
})
</script>
