<template>
    <el-dialog v-model="visiable" width="1132" :before-close="handleClose" :show-close="false" header-class="h"
        body-class="h" footer-class="h">
        <div class="min-h-[632px] bg-[url('/image_show_bg.png')] bg-contain relative">
            <div class=" absolute w-[350px] left-[388px] top-[56px]  text-center text-[rgb(6,142,206)] ">{{ p.urlInfo && p.urlInfo.length > 0 ? p.urlInfo[current_idx].name : ""}}</div>
            <div class=" absolute left-[128px] top-[80px]  text-red-50">
                <button   class=" ml-2 p-[5px] bg-blue-400" v-for="i,k in p.urlInfo" @click="current_idx = k">
                    {{ i.name }} 
                </button>
            </div>
            <button @click="emit('close')" class=" absolute right-[20px] top-[20px] h-[30px] w-[30px]"></button>
            <div
                class=" absolute left-[218px] top-[150px] max-h-[400px] max-w-[700px] min-h-[400px] min-w-[700px]  overflow-auto">
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