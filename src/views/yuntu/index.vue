<template>
    <div class="w-full h-screen min-w-[1000px] min-h-[800px] bg-[#2c3e50] overflow-auto">
        <div class="absolute z-50 top-[17px] text-[rgb(6,142,206)]"
            :style="{ left: 'max(500px, 50%)', transform: 'translateX(-50%)' }">
            {{ currentWarehouse.getWareHouse().wareHouseName }}
        </div>
        <div class="absolute z-50 top-[50px] text-[rgb(6,142,206)]"
            :style="{ left: 'max(500px, 50%)', transform: 'translateX(-50%)' }">
            {{ "云图播放" }}
        </div>
        <div @click="$router.push('/house')" class="absolute z-50 top-[25px] text-[rgb(6,142,206)] size-[20px] cursor-pointer "
            :style="{ left: 'max(968px, 97.3%)', transform: 'translateX(-50%)' }">
        </div>
        <!-- Size badge -->
        <div class="fixed top-2 left-2 z-50 text-sm text-white rounded px-3 py-2"
            :class="belowMin ? 'bg-red-800/80' : 'bg-black/80'">
            <div class="font-semibold">窗口尺寸</div>
            <div>{{ width }} × {{ height }}</div>
            <div class="opacity-80">最小：1000 × 800</div>
        </div>

        <div class="relative w-full h-full min-w-[1000px] min-h-[800px]">
            <!-- Corners -->
            <div class="absolute top-0 left-0 w-[500px] h-[118px] z-20 overflow-hidden">
                <img src="/show_pic/left_top.png" alt="左上角图片" class="w-full h-full object-cover" />
            </div>

            <div class="absolute top-0 right-0 w-[500px] h-[118px] z-20 overflow-hidden">
                <img src="/show_pic/right_top.png" alt="右上角图片" class="w-full h-full object-cover" />
            </div>

            <div class="absolute bottom-0 left-0 w-[500px] h-[62px] z-20 overflow-hidden">
                <img src="/show_pic/left_bot.png" alt="左下角图片" class="w-full h-full object-cover" />
            </div>

            <div class="absolute bottom-0 right-0 w-[500px] h-[62px] z-20 overflow-hidden">
                <img src="/show_pic/right_bot.png" alt="右下角图片" class="w-full h-full object-cover" />
            </div>

            <!-- Top / Bottom panels (repeat-x backgrounds) -->
            <div class="absolute top-0 left-[500px] right-[500px] h-[118px] z-10 border-0" :style="{
                backgroundImage: `url('/show_pic/center_top_bg.png')`,
                backgroundRepeat: 'repeat-x'
            }" />

            <div class="absolute bottom-0 left-[500px] right-[500px] h-[62px] z-10 border-0" :style="{
                backgroundImage: `url('/show_pic/center_bot_bg.png')`,
                backgroundRepeat: 'repeat-x'
            }" />

            <!-- Side panels (repeat-y backgrounds) -->
            <div class="absolute top-[118px] bottom-[62px] left-0 w-[167px] z-10" :style="{
                backgroundImage: `url('/show_pic/left_center.png')`,
                backgroundRepeat: 'repeat-y'
            }" />

            <div class="absolute top-[118px] bottom-[62px] right-0 w-[167px] z-10" :style="{
                backgroundImage: `url('/show_pic/right_center.png')`,
                backgroundRepeat: 'repeat-y'
            }" />

            <div
                class="absolute top-[118px] bottom-[62px] left-[167px] right-[167px] z-30 overflow-y-auto p-5 flex flex-col items-center bg-black/40 backdrop-blur">
                <div class="w-full  max-w-[800px] space-y-5">
                    <!-- <el-carousel height="auto" autoplay>
                        <el-carousel-item :style="{height:h}" :key="url" v-for="url in urls">
                            <img :src="url" alt="内容图片" class="w-full h-auto shadow-xl border border-white/20" />
                        </el-carousel-item>
                    </el-carousel> -->
                    <Carousel :images="urls"></Carousel>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
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

<!--
  说明：
  - 样式均以 Tailwind 实现，极少数使用内联 style 来设置平铺背景图片（repeat-x / repeat-y）。
  - 维持与原版相同的布局尺寸与行为（四角、上下横幅、左右竖带与居中可滚动内容）。
  - 将原生 JS 改为 Vue3 响应式状态，实时显示当前窗口尺寸并提醒是否低于最小尺寸。
-->
<!-- 
<template>
    <Binner />

    <div>
        {{ urls }}
    </div>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import house from '@/api/house';
import Binner from '@/components/common/Binner.vue';
import { useCurrentWareHouse } from '@/stores/currentWareHouse';
import { api_PreFix } from '@/api';
const currentWarehouse = useCurrentWareHouse()
const urls = ref<Array<string>>([])
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
}
fetch_yuntu()
</script>
<style scoped>
</style> -->