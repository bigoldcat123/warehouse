<template>
    <el-dialog v-model="visiable" :width="width" :before-close="handleClose" :show-close="false" class="image-dialog"
        :modal-class="'image-dialog__overlay'" top="1%">
        <div :style="{height:height}" class="relative image-viewer">
            <!-- 标题栏 -->
            <div class="image-viewer__header">
                <svg class="w-5 h-5 text-[#64b5f6] flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
                <span class="text-white text-base font-semibold">
                    {{ p.urlInfo && p.urlInfo.length > 0 ? p.urlInfo[current_idx].name : "" }}
                </span>
                <button @click="emit('close')" class="image-viewer__close" aria-label="关闭">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                    </svg>
                </button>
            </div>

            <!-- 图片列表 -->
            <div class="image-viewer__body">
                <img v-for="url in p.urlInfo && p.urlInfo.length > 0 ? p.urlInfo![current_idx].urls : []" class="image-viewer__img" :src="url" alt="">
                <div v-if="!(p.urlInfo) || p.urlInfo.length == 0 || p.urlInfo[current_idx].urls.length == 0" class="image-viewer__empty">
                    <svg class="w-12 h-12 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                    <span>没有图片</span>
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

<style scoped>
.image-viewer {
    background: #1a3a5c;
    border-radius: 10px;
    overflow: hidden;
    display: flex;
    flex-direction: column;
}
.image-viewer__header {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 12px 20px;
    background: linear-gradient(135deg, #2968a8 0%, #1a3a5c 100%);
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    flex-shrink: 0;
}
.image-viewer__close {
    margin-left: auto;
    background: transparent;
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 4px;
    color: #b0d0f0;
    padding: 4px;
    cursor: pointer;
    transition: all 0.2s ease;
    display: flex;
    align-items: center;
    justify-content: center;
}
.image-viewer__close:hover {
    background: rgba(255, 255, 255, 0.1);
    color: white;
    border-color: rgba(255, 255, 255, 0.4);
}
.image-viewer__body {
    flex: 1;
    overflow: auto;
    padding: 12px;
    background: #1a3a5c;
}
.image-viewer__img {
    width: 100%;
    border-radius: 6px;
    margin-bottom: 8px;
}
.image-viewer__empty {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    color: rgba(255, 255, 255, 0.4);
    font-size: 14px;
}
</style>
<style>
.image-dialog.el-dialog {
    background: #1a3a5c !important;
    border-radius: 10px !important;
    border: 1px solid rgba(255, 255, 255, 0.1);
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.6) !important;
    overflow: hidden;
    padding: 0 !important;
    margin: 1vh auto 0 !important;
}
.image-dialog .el-dialog__body {
    padding: 0 !important;
    background: #1a3a5c !important;
    color: white;
}
.image-dialog .el-dialog__header {
    display: none;
}
.image-dialog__overlay.el-overlay {
    background: rgba(0, 0, 0, 0.8) !important;
    backdrop-filter: blur(4px);
}
</style>
