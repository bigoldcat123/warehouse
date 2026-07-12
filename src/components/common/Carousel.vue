<template>
    <div class="relative w-full mx-auto overflow-hidden">
        <!-- 容器：高度随图片自动调整 -->
        <div class="relative transition-[height] duration-500 ease-in-out" >
            <!-- 图片列表 -->
            <div class="flex transition-transform duration-500 ease-in-out"
                :style="{ transform: `translateX(-${currentIndex * 100}%)` }">
                <div v-for="(img, index) in images" :key="index" class="flex-shrink-0 w-full flex justify-center">
                    <img :src="img" class="object-contain" :ref="el => (imageRefs[index] = el as HTMLImageElement)"
                        @load="updateHeight" />
                </div>
            </div>
        </div>

        <!-- 左右按钮 -->
        <button class="absolute left-2 top-1/2 -translate-y-1/2 bg-black/50 text-white px-2 py-1 rounded" @click="prev">
            ◀
        </button>
        <button class="absolute right-2 top-1/2 -translate-y-1/2 bg-black/50 text-white px-2 py-1 rounded"
            @click="next">
            ▶
        </button>
    </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick, onMounted, onUnmounted } from 'vue'

interface Props {
    images: string[] // 传入的图片数组
    width?: number   // 可选：固定宽度，默认 800
}

const props = defineProps<Props>()

const currentIndex = ref(0)
const containerHeight = ref(0)

const imageRefs = ref<HTMLImageElement[]>([])

function updateHeight() {
    nextTick(() => {
        const img = imageRefs.value[currentIndex.value]
        if (img) {
            // 计算缩放后的高度（保持宽度比例）
            const width = props.width ?? 800
            containerHeight.value = img.naturalHeight * (width / img.naturalWidth)
        }
    })
}

function prev() {
    currentIndex.value =
        (currentIndex.value - 1 + props.images.length) % props.images.length
}

function next() {
    currentIndex.value = (currentIndex.value + 1) % props.images.length
}
let interval_id :number | undefined
onMounted(() => {
    updateHeight()
    interval_id =setInterval(() => {
        next()
    },3000)

})
onUnmounted(() => {
    clearInterval(interval_id)
})

watch(currentIndex, () => {
    updateHeight()
})
</script>