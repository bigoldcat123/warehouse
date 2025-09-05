<template>
    <el-dialog v-model="visiable" :width="width" :before-close="handleClose" :show-close="false" header-class="h"  top="1%"
        body-class="h" footer-class="h">
        <div :style="{height:height}" class=" bg-[url('/image_show_bg_2.jpg')] bg-contain relative">
            <div class=" absolute  w-[36%] left-[32%] top-[6%]  text-center text-[rgb(6,142,206)] ">{{ p.urlInfo && p.urlInfo.length > 0 ? p.urlInfo[current_idx].name : ""}} </div>
            <!-- <div class=" absolute left-[1%] top-[44%]  text-red-50 flex  flex-col gap-y-3">
                <button   class=" ml-2 p-[5px] bg-blue-400 text-[15px] w-[100px]" v-for="i,k in p.urlInfo" @click="current_idx = k">
                    {{ i.name }} 
                </button>
            </div> -->
            <button @click="emit('close')" class="  absolute right-[2%] top-[2%] h-[3%] w-[3%]"></button>
            <div
                class=" absolute left-[20%] top-[13%] h-[82%] w-[60%]   overflow-auto">
                <img v-for="url in p.urlInfo && p.urlInfo.length > 0 ? p.urlInfo![current_idx].urls : []" class=" w-full" :src="url" alt="">
                <div v-if="!(p.urlInfo) || p.urlInfo.length == 0 || p.urlInfo[current_idx].urls.length == 0">
                    没有图片
                </div>
            </div>
        </div>
    </el-dialog>

</template>
<script setup lang="ts">
import type { UrlInfo } from '@/components/HouseInfo.vue';
import { onMounted, onUpdated, ref } from 'vue'
const p = defineProps<{
    dialogVisible: boolean,
    urlInfo?:Array<UrlInfo>
}>()
const height = ref("850px");
const width = ref('1000px');
const current_idx = ref(0)
const emit = defineEmits<{
    (event: 'close'): void,
    (event: 'refresh'): void
}>()
const visiable =ref(false)
onUpdated(() => {
    console.log("???" + p.dialogVisible);
    current_idx.value = 0
    visiable.value = p.dialogVisible})

const handleClose = (done: () => void) => {
    emit('close')
}
import { onBeforeUnmount } from 'vue'

const windowSize = ref({ width: window.innerWidth, height: window.innerHeight })

const updateWindowSize = () => {
    windowSize.value.width = window.innerWidth
    windowSize.value.height = window.innerHeight

    // 保证比例为 2000:1700
    const ratio = 2000 / 1700
    let w = window.innerWidth * 0.7
    let h = window.innerHeight * 0.95

    if (w / h > ratio) {
        w = h * ratio
    } else {
        h = w / ratio
    }

    width.value = `${w}px`
    height.value = `${h}px`
}

onMounted(() => {
    window.addEventListener('resize', updateWindowSize)
})

onBeforeUnmount(() => {
    window.removeEventListener('resize', updateWindowSize)
})
</script>

<style >
/* Remove all padding from el-dialog body */
.h {
    padding: 0;
    margin: 0;
    height: 0;
}

.el-dialog {
        --el-dialog-width: 50%;
        --el-dialog-margin-top: 15vh;
        --el-dialog-bg-color: var(--el-bg-color);
        --el-dialog-box-shadow: var(--el-box-shadow);
        --el-dialog-title-font-size: var(--el-font-size-large);
        --el-dialog-content-font-size: 14px;
        --el-dialog-font-line-height: var(--el-font-line-height-primary);
        --el-dialog-padding-primary: 16px;
        --el-dialog-border-radius: var(--el-border-radius-base);
        background: var(--el-dialog-bg-color);
        border-radius: var(--el-dialog-border-radius);
        box-shadow: var(--el-dialog-box-shadow);
        box-sizing: border-box;
        margin: var(--el-dialog-margin-top, 15vh) auto 50px;
        overflow-wrap: break-word;
        padding: 0px;
        position: relative;
        width: var(--el-dialog-width, 50%);
}
</style>