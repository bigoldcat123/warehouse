<template>
    <div class="detail-page">
        <!-- 标题栏 -->
        <div class="detail-header">
            <svg class="w-6 h-6 text-[#64b5f6] flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
            <span class="text-white text-xl font-bold">
                {{ detail?.wareHouseName }}-{{ detail?.houseNo }}({{ detail?.houseName }})
            </span>
            <button class="back-btn" @click="$router.go(-1)">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
                </svg>
                返回
            </button>
        </div>

        <!-- 信息标签区 -->
        <div class="info-tags">
            <div class="info-tag">
                <span class="info-tag__label">品种</span>
                <span class="info-tag__value">{{ detail?.breed || '--' }}</span>
            </div>
            <div class="info-tag">
                <span class="info-tag__label">水分</span>
                <span class="info-tag__value">{{ detail?.water || '--' }}</span>
            </div>
            <div class="info-tag">
                <span class="info-tag__label">入库时间</span>
                <span class="info-tag__value">{{ detail?.entryTime || '--' }}</span>
            </div>
            <div class="info-tag">
                <span class="info-tag__label">保管</span>
                <span class="info-tag__value">{{ detail?.keeper || '--' }}</span>
            </div>
            <div class="info-tag info-tag--temp">
                <span class="info-tag__label">仓温</span>
                <span class="info-tag__value">{{ detail?.inTemperature || '--' }}°C</span>
            </div>
            <div class="info-tag info-tag--temp">
                <span class="info-tag__label">外温</span>
                <span class="info-tag__value">{{ detail?.outTemperature || '--' }}°C</span>
            </div>
            <div class="info-tag info-tag--humidity">
                <span class="info-tag__label">仓湿</span>
                <span class="info-tag__value">{{ detail?.inHumidity || '--' }}%</span>
            </div>
            <div class="info-tag info-tag--humidity">
                <span class="info-tag__label">外湿</span>
                <span class="info-tag__value">{{ detail?.outHumidity || '--' }}%</span>
            </div>
            <div class="info-tag info-tag--hot">
                <span class="info-tag__label">高温</span>
                <span class="info-tag__value">{{ $route.query.maxTemperature || '--' }}°C</span>
            </div>
            <div class="info-tag info-tag--cold">
                <span class="info-tag__label">低温</span>
                <span class="info-tag__value">{{ $route.query.minTemperature || '--' }}°C</span>
            </div>
            <div class="info-tag info-tag--avg">
                <span class="info-tag__label">均温</span>
                <span class="info-tag__value">{{ $route.query.avgTemperature || '--' }}°C</span>
            </div>
            <div class="info-tag" v-for="(item, key) in ($route.query.layerAvg as unknown as string || '').split('|')" :key="key">
                <span class="info-tag__label">{{ (key + 1) + ' 层' }}</span>
                <span class="info-tag__value">{{ item }}</span>
            </div>
            <div class="info-tag info-tag--time">
                <span class="info-tag__label">检测时间</span>
                <span class="info-tag__value">{{ detail?.testTime || '--' }}</span>
            </div>
        </div>

        <!-- 数据矩阵 -->
        <div class="matrix-container">
            <div class="matrix-item" v-for="(item, key) in detail?.list" :key="key">
                <!-- 表头 -->
                <div class="matrix-row matrix-row--header">
                    <span class="matrix-cell matrix-cell--label">
                        {{ route.query.house_type == '平房仓' ? (key + 1) + ' 层' : '点位' }}
                    </span>
                    <span class="matrix-cell matrix-cell--index" v-for="(i, kk) in item[0]" :key="kk">
                        {{ kk + 1 }}
                    </span>
                </div>
                <!-- 数据行 -->
                <div class="matrix-row" v-for="(x, keyx) in item" :key="keyx">
                    <span class="matrix-cell matrix-cell--index">{{ keyx + 1 }}</span>
                    <span class="matrix-cell matrix-cell--data" v-for="(y, yi) in x" :key="yi"
                        :class="getTempClass(y)">
                        {{ cal_temperature(y) }}
                    </span>
                </div>
            </div>
        </div>
    </div>
</template>
<script setup lang="ts">
import data, { type type_Data_Detail } from '@/api/data';
import { ref } from 'vue'
import { useRoute } from 'vue-router';
const route = useRoute()

const detail = ref<type_Data_Detail>()
data.getDetil(route.query.id as unknown as number).then(res => {
    detail.value = res.data.value
})

function cal_temperature(num: string) {
    const n = Number(num)
    if (n && n >= -40 && n <= 80) {
        return num
    } else {
        return "-"
    }
}

function is_ok(num: string) {
    const n = Number(num)
    if (n && n >= -40 && n <= 80) {
        return true
    } else {
        return false
    }
}

function getTempClass(num: string) {
    const n = Number(num)
    if (!n || n < -40 || n > 80) return 'temp--invalid'
    if (n >= 35) return 'temp--hot'
    if (n <= 10) return 'temp--cold'
    return 'temp--normal'
}
</script>
<style scoped>
.detail-page {
    min-height: 100%;
    padding-bottom: 20px;
}

/* 标题栏 */
.detail-header {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 14px 20px;
    background: linear-gradient(135deg, #2968a8 0%, #1a3a5c 100%);
    border-radius: 8px;
    margin-bottom: 16px;
    border: 1px solid rgba(255, 255, 255, 0.1);
}
.back-btn {
    margin-left: auto;
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 6px 14px;
    background: rgba(255, 255, 255, 0.1);
    color: #b0d0f0;
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 6px;
    font-size: 13px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s ease;
}
.back-btn:hover {
    background: rgba(255, 255, 255, 0.2);
    color: white;
    border-color: rgba(255, 255, 255, 0.4);
}

/* 信息标签区 */
.info-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    margin-bottom: 16px;
}
.info-tag {
    display: flex;
    flex-direction: column;
    gap: 2px;
    padding: 8px 14px;
    background: rgba(255, 255, 255, 0.06);
    border: 1px solid rgba(255, 255, 255, 0.12);
    border-radius: 8px;
    min-width: 100px;
}
.info-tag__label {
    font-size: 11px;
    color: #b0d0f0;
    font-weight: 500;
}
.info-tag__value {
    font-size: 14px;
    color: white;
    font-weight: 600;
}
.info-tag--temp {
    background: rgba(251, 140, 0, 0.15);
    border-color: rgba(251, 140, 0, 0.3);
}
.info-tag--humidity {
    background: rgba(0, 188, 212, 0.15);
    border-color: rgba(0, 188, 212, 0.3);
}
.info-tag--hot {
    background: rgba(229, 57, 53, 0.15);
    border-color: rgba(229, 57, 53, 0.3);
}
.info-tag--hot .info-tag__value { color: #EF9A9A; }
.info-tag--cold {
    background: rgba(30, 136, 229, 0.15);
    border-color: rgba(30, 136, 229, 0.3);
}
.info-tag--cold .info-tag__value { color: #90CAF9; }
.info-tag--avg {
    background: rgba(67, 160, 71, 0.15);
    border-color: rgba(67, 160, 71, 0.3);
}
.info-tag--avg .info-tag__value { color: #A5D6A7; }
.info-tag--time {
    background: rgba(156, 39, 176, 0.15);
    border-color: rgba(156, 39, 176, 0.3);
}

/* 数据矩阵 */
.matrix-container {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
    justify-content: center;
}
.matrix-item {
    background: rgba(0, 0, 0, 0.15);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 8px;
    overflow: hidden;
}
.matrix-row {
    display: flex;
}
.matrix-row--header {
    background: linear-gradient(135deg, #2968a8 0%, #1e5f8a 100%);
}
.matrix-cell {
    padding: 8px 12px;
    font-size: 13px;
    text-align: center;
    min-width: 64px;
    border: 1px solid rgba(255, 255, 255, 0.06);
}
.matrix-cell--label {
    background: rgba(30, 136, 229, 0.3);
    color: white;
    font-weight: 600;
    min-width: 80px;
    text-align: left;
}
.matrix-cell--index {
    background: rgba(255, 255, 255, 0.06);
    color: #b0d0f0;
    font-weight: 500;
}
.matrix-row--header .matrix-cell--index {
    background: transparent;
    color: white;
    font-weight: 600;
}
.matrix-cell--data {
    background: #1e4a6e;
    color: white;
    font-weight: 500;
}
.matrix-cell--data:hover {
    background: rgba(30, 136, 229, 0.3);
}

/* 温度值颜色 */
.temp--normal { color: #A5D6A7; }
.temp--hot { color: #EF9A9A; background: rgba(229, 57, 53, 0.2) !important; }
.temp--cold { color: #90CAF9; background: rgba(30, 136, 229, 0.2) !important; }
.temp--invalid { color: rgba(255, 255, 255, 0.3); }
</style>
