<template>
    <div class="temp-line-page">
        <div class="temp-line-card">
            <div class="temp-line-card__header">
                <svg class="w-5 h-5 text-[#64b5f6]" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M3 13h2v-2H3v2zm0 4h2v-2H3v2zm0-8h2V7H3v2zm4 4h14v-2H7v2zm0 4h14v-2H7v2zM7 7v2h14V7H7z" />
                </svg>
                <span class="text-white text-base font-bold">温度折线图</span>
            </div>
            <div class="temp-line-card__body">
                <div class="coord-row">
                    <span class="coord-label">X</span>
                    <span class="coord-value">{{ x ?? '--' }}</span>
                </div>
                <div class="coord-row">
                    <span class="coord-label">Y</span>
                    <span class="coord-value">{{ y ?? '--' }}</span>
                </div>
                <div class="coord-row">
                    <span class="coord-label">Z</span>
                    <span class="coord-value">{{ z ?? '--' }}</span>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

// 从 query 中读取 x y z，保持数字类型（路由 query 默认是字符串）
const toNumber = (v: string | (string | null)[] | null | undefined) => {
    if (v == null || Array.isArray(v)) return undefined
    const n = Number(v)
    return Number.isNaN(n) ? undefined : n
}

const x = computed(() => toNumber(route.query.x))
const y = computed(() => toNumber(route.query.y))
const z = computed(() => toNumber(route.query.z))
</script>

<style scoped>
.temp-line-page {
    min-height: calc(100vh - 120px);
    padding: 24px;
    display: flex;
    align-items: flex-start;
    justify-content: center;
}

.temp-line-card {
    width: 360px;
    background: linear-gradient(145deg, #2968a8 0%, #1e5f8a 100%);
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.temp-line-card__header {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 16px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.temp-line-card__body {
    padding: 20px 16px;
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.coord-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 14px;
    background: rgba(255, 255, 255, 0.06);
    border-radius: 6px;
    border: 1px solid rgba(255, 255, 255, 0.08);
}

.coord-label {
    color: #64b5f6;
    font-size: 14px;
    font-weight: 600;
    letter-spacing: 1px;
}

.coord-value {
    color: #ffffff;
    font-size: 18px;
    font-weight: 700;
}
</style>
