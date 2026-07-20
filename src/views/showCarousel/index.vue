<template>
    <div class="w-full h-screen min-w-[1000px] min-h-[800px] bg-[#2c3e50] overflow-auto">
        <div class="absolute z-50 top-[17px] text-[rgb(6,142,206)]"
            :style="{ left: 'max(500px, 50%)', transform: 'translateX(-50%)' }">
            {{ urlInfo.houseName }}
        </div>
        <div class="absolute z-50 top-[50px] text-[rgb(6,142,206)]"
            :style="{ left: 'max(500px, 50%)', transform: 'translateX(-50%)' }">
            {{ urlInfo.name }}
        </div>
        <div @click="$router.back()" class="absolute z-50 top-[25px] text-[rgb(6,142,206)] size-[20px] cursor-pointer "
            :style="{ left: 'max(968px, 97.3%)', transform: 'translateX(-50%)' }">
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

            <!-- Center content area: Carousel -->
            <div
                class="absolute top-[118px] bottom-[62px] left-[167px] right-[167px] z-30 overflow-auto flex flex-col bg-[#011437] backdrop-blur">
                <!-- Carousel canvas -->
                <div class="w-full max-w-[1200px] mx-auto my-4 px-4 flex-1 min-h-0 flex items-center justify-center">
                    <div class="carousel-container relative w-full rounded-[20px] overflow-hidden bg-black/40 shadow-2xl"
                        style="aspect-ratio: 16/9; max-height: min(65vh, calc(100vw * 9 / 16));"
                        @mouseenter="pauseOnHover && stopAutoPlay()"
                        @mouseleave="pauseOnHover && isPlaying && startAutoPlay()">
                        <!-- All images absolutely positioned, fade transition -->
                        <img v-for="(url, idx) in processedUrls" :key="idx" :src="url" alt="内容图片"
                            class="carousel-slide" :class="{
                                active: idx === currentIndex,
                                prev: idx === prevIndex
                            }" />

                        <!-- Empty state -->
                        <div v-if="processedUrls.length === 0"
                            class="absolute inset-0 flex items-center justify-center text-white/50 text-lg">
                            暂无图片
                        </div>
                    </div>
                </div>

                <!-- Controls: always visible, never shrink -->
                <div class="flex-shrink-0 w-full max-w-[1200px] mx-auto px-4 pb-4 flex items-center justify-between gap-4 flex-wrap">
                    <!-- Indicators -->
                    <div class="flex gap-2.5 items-center flex-wrap">
                        <button v-for="(_, idx) in processedUrls" :key="idx" class="carousel-dot" :class="{ active: idx === currentIndex }"
                            :aria-label="`转到第 ${idx + 1} 张`" @click="goToSlide(idx); resetAutoPlay()" />
                    </div>

                    <!-- Play/Pause + Counter -->
                    <div class="flex gap-2.5 items-center">
                        <button class="action-btn" @click="togglePlay" :aria-label="isPlaying ? '暂停' : '播放'">
                            <svg v-if="isPlaying" viewBox="0 0 24 24" class="w-[18px] h-[18px] stroke-current stroke-[2.2] fill-none">
                                <rect x="5" y="3" width="4" height="18" />
                                <rect x="15" y="3" width="4" height="18" />
                            </svg>
                            <svg v-else viewBox="0 0 24 24" class="w-[18px] h-[18px] stroke-current stroke-[2.2] fill-none">
                                <polygon points="5,3 19,12 5,21" />
                            </svg>
                            <span>{{ isPlaying ? '暂停' : '播放' }}</span>
                        </button>
                        <span class="badge">{{ currentIndex + 1 }} / {{ processedUrls.length }}</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import type { UrlInfo } from '@/components/HouseInfo.vue'
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const urlInfo = route.query as unknown as UrlInfo

// Prepend /api to every URL per requirement
const processedUrls = computed(() => {
    const urls = urlInfo.urls ?? []
    return urls.map(url => `/api${url}`)
})

// Carousel state
const currentIndex = ref(0)
const prevIndex = ref<number | null>(null)
const isPlaying = ref(true)
const pauseOnHover = ref(true)
let intervalId: ReturnType<typeof setInterval> | null = null

const total = computed(() => processedUrls.value.length)
const AUTO_PLAY_INTERVAL = 2500 // ms, a bit slower than demo's 2200 for smoother feel

function goToSlide(index: number) {
    if (index === currentIndex.value) return
    if (index < 0 || index >= total.value) return
    prevIndex.value = currentIndex.value
    currentIndex.value = index
}

function nextSlide() {
    const next = (currentIndex.value + 1) % total.value
    goToSlide(next)
}

function startAutoPlay() {
    if (intervalId) clearInterval(intervalId)
    if (total.value <= 1) return
    intervalId = setInterval(nextSlide, AUTO_PLAY_INTERVAL)
    isPlaying.value = true
}

function stopAutoPlay() {
    if (intervalId) {
        clearInterval(intervalId)
        intervalId = null
    }
    isPlaying.value = false
}

function togglePlay() {
    if (isPlaying.value) {
        stopAutoPlay()
    } else {
        startAutoPlay()
    }
}

function resetAutoPlay() {
    if (isPlaying.value) {
        if (intervalId) {
            clearInterval(intervalId)
            intervalId = setInterval(nextSlide, AUTO_PLAY_INTERVAL)
        } else {
            startAutoPlay()
        }
    }
}

// Pause auto-play when the page is hidden (saves resources)
function handleVisibility() {
    if (document.hidden) {
        if (isPlaying.value && intervalId) {
            clearInterval(intervalId)
            intervalId = null
        }
    } else {
        if (isPlaying.value && !intervalId && total.value > 1) {
            intervalId = setInterval(nextSlide, AUTO_PLAY_INTERVAL)
        }
    }
}

// Reset index if urls change
watch(() => processedUrls.value, () => {
    currentIndex.value = 0
    prevIndex.value = null
    if (isPlaying.value) startAutoPlay()
})

onMounted(() => {
    startAutoPlay()
    document.addEventListener('visibilitychange', handleVisibility)
})

onBeforeUnmount(() => {
    if (intervalId) clearInterval(intervalId)
    document.removeEventListener('visibilitychange', handleVisibility)
})
</script>

<style scoped>
.carousel-container {
    background: radial-gradient(ellipse at center, #0a1e4a 0%, #011437 100%);
}

.carousel-slide {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 100%;
    object-fit: contain;
    padding: 8px;
    opacity: 0;
    transition: opacity 1.2s cubic-bezier(0.25, 0.46, 0.45, 0.94);
    will-change: opacity;
    pointer-events: none;
    backface-visibility: hidden;
    -webkit-backface-visibility: hidden;
}

.carousel-slide.active {
    opacity: 1;
    z-index: 10;
}

.carousel-slide.prev {
    z-index: 5;
    opacity: 0;
}

.carousel-dot {
    width: 12px;
    height: 12px;
    border-radius: 40px;
    background: #cbd5e1;
    box-shadow: inset 0 1px 2px rgba(0, 0, 0, 0.05);
    transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
    cursor: pointer;
    border: none;
    padding: 0;
    flex-shrink: 0;
}

.carousel-dot.active {
    background: #068ece;
    width: 32px;
    box-shadow: 0 4px 10px rgba(6, 142, 206, 0.5);
}

.carousel-dot:hover:not(.active) {
    background: #94a3b8;
    transform: scale(1.2);
}

.action-btn {
    background: rgba(255, 255, 255, 0.1);
    backdrop-filter: blur(4px);
    border: 1px solid rgba(255, 255, 255, 0.2);
    padding: 8px 18px;
    border-radius: 60px;
    font-weight: 500;
    font-size: 0.9rem;
    color: rgb(6, 142, 206);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    display: inline-flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    transition: all 0.2s ease;
    letter-spacing: 0.3px;
}

.action-btn:hover {
    background: rgba(255, 255, 255, 0.18);
    box-shadow: 0 8px 20px rgba(0, 20, 40, 0.3);
    transform: translateY(-2px);
    border-color: rgba(6, 142, 206, 0.5);
}

.action-btn:active {
    transform: scale(0.96);
}

.badge {
    font-size: 0.85rem;
    background: rgba(6, 142, 206, 0.15);
    padding: 6px 14px;
    border-radius: 40px;
    color: rgb(6, 142, 206);
    letter-spacing: 0.3px;
    border: 1px solid rgba(6, 142, 206, 0.3);
    font-variant-numeric: tabular-nums;
}
</style>
