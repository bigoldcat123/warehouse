<template>
    <div class="detail-page">
        <div class="detail-header">
            <svg class="w-6 h-6 text-[#64b5f6] flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
            <span class="detail-title">
                {{ detail?.wareHouseName || '--' }}-{{ detail?.houseNo || '--' }}({{ detail?.houseName || '--' }})
            </span>
            <button class="back-btn" @click="$router.go(-1)">返回</button>
        </div>

        <div v-if="detail" class="info-tags">
            <div v-for="item in infoItems" :key="item.label" class="info-tag" :class="item.className">
                <span class="info-tag__label">{{ item.label }}</span>
                <span class="info-tag__value">{{ item.value }}</span>
            </div>
        </div>

        <div v-if="detail" class="matrix-container">
            <section v-for="table in matrixTables" :key="table.title" class="matrix-card">
                <h2 class="matrix-title">{{ table.title }}</h2>
                <div v-if="table.rows.length" class="table-scroll">
                    <table class="matrix-table">
                        <thead>
                            <tr>
                                <th>串号 / 层数</th>
                                <th v-for="layer in table.layerCount" :key="layer">第 {{ layer }} 层</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="(row, stringIndex) in table.rows" :key="stringIndex">
                                <th>第 {{ stringIndex + 1 }} 串</th>
                                <td v-for="layer in table.layerCount" :key="layer">
                                    {{ formatSensorValue(row[layer - 1], table.unit) }}
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <div v-else class="empty-data">暂无数据</div>
            </section>
        </div>
    </div>
</template>

<script setup lang="ts">
import data, { type type_Data_Detail } from '@/api/data'
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const detail = ref<type_Data_Detail>()

data.getDetil(Number(route.query.id)).then(res => {
    detail.value = res.data.value
})

const displayValue = (value: unknown, unit = '') => {
    if (value === null || value === undefined || value === '') return '--'
    return `${value}${unit}`
}

const temperatureStats = computed(() => {
    const matrix = detail.value?.temperatureMatrix ?? []
    const allValues = matrix.flat().filter(Number.isFinite)
    const layerCount = Math.max(0, ...matrix.map(row => row.length))
    const layerAverages = Array.from({ length: layerCount }, (_, layerIndex) => {
        const values = matrix
            .map(row => row[layerIndex])
            .filter((value): value is number => Number.isFinite(value))
        return average(values)
    })

    return {
        maximum: allValues.length ? Math.max(...allValues) : null,
        minimum: allValues.length ? Math.min(...allValues) : null,
        average: average(allValues),
        layerAverages
    }
})

const average = (values: number[]) => {
    if (!values.length) return null
    return values.reduce((total, value) => total + value, 0) / values.length
}

const formatTemperature = (value: number | null) => {
    return value === null ? '--' : `${value.toFixed(1)}℃`
}

const infoItems = computed(() => {
    const stats = temperatureStats.value
    return [
        { label: '品种', value: displayValue(detail.value?.grainName) },
        { label: '水分', value: displayValue(detail.value?.grainWater, '%') },
        { label: '入库时间', value: displayValue(detail.value?.dateOfIn) },
        { label: '保管', value: displayValue(detail.value?.keeperName) },
        { label: 'PH3', value: displayValue(detail.value?.housePh3, ' ppm'), className: 'info-tag--gas' },
        { label: 'O₂', value: displayValue(detail.value?.oAir, '%'), className: 'info-tag--gas' },
        { label: 'CO₂', value: displayValue(detail.value?.co2Air, ' ppm'), className: 'info-tag--gas' },
        { label: '高温', value: formatTemperature(stats.maximum), className: 'info-tag--hot' },
        { label: '低温', value: formatTemperature(stats.minimum), className: 'info-tag--cold' },
        { label: '均温', value: formatTemperature(stats.average), className: 'info-tag--average' },
        ...stats.layerAverages.map((value, index) => ({
            label: `第 ${index + 1} 层均温`,
            value: formatTemperature(value),
            className: 'info-tag--layer'
        })),
        { label: '检测时间', value: displayValue(detail.value?.testTime), className: 'info-tag--time' }
    ]
})

const matrixTables = computed(() => [
    {
        title: 'PH3 浓度表',
        rows: detail.value?.ph3Matrix ?? [],
        layerCount: Math.max(0, ...(detail.value?.ph3Matrix ?? []).map(row => row.length)),
        unit: ' ppm'
    },
    {
        title: '温度表',
        rows: detail.value?.temperatureMatrix ?? [],
        layerCount: Math.max(0, ...(detail.value?.temperatureMatrix ?? []).map(row => row.length)),
        unit: '℃'
    }
])

const formatSensorValue = (value: number | undefined, unit: string) => {
    return value === undefined || value === null || Number.isNaN(value) ? '--' : `${value}${unit}`
}
</script>

<style scoped>
.detail-page {
    min-height: 100%;
    padding-bottom: 20px;
}

.detail-header {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 14px 20px;
    margin-bottom: 16px;
    background: linear-gradient(135deg, #2968a8 0%, #1a3a5c 100%);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 8px;
}

.detail-title {
    color: white;
    font-size: 20px;
    font-weight: 700;
}

.back-btn {
    margin-left: auto;
    padding: 6px 14px;
    color: #b0d0f0;
    background: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 6px;
    cursor: pointer;
}

.info-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    margin-bottom: 18px;
}

.info-tag {
    display: flex;
    min-width: 120px;
    flex-direction: column;
    gap: 2px;
    padding: 8px 14px;
    background: rgba(255, 255, 255, 0.06);
    border: 1px solid rgba(255, 255, 255, 0.12);
    border-radius: 8px;
}

.info-tag--gas {
    background: rgba(0, 188, 212, 0.12);
    border-color: rgba(0, 188, 212, 0.3);
}

.info-tag--hot {
    background: rgba(229, 57, 53, 0.15);
    border-color: rgba(229, 57, 53, 0.3);
}

.info-tag--cold {
    background: rgba(30, 136, 229, 0.15);
    border-color: rgba(30, 136, 229, 0.3);
}

.info-tag--average,
.info-tag--layer {
    background: rgba(67, 160, 71, 0.15);
    border-color: rgba(67, 160, 71, 0.3);
}

.info-tag--time {
    background: rgba(156, 39, 176, 0.15);
    border-color: rgba(156, 39, 176, 0.3);
}

.info-tag__label {
    color: #b0d0f0;
    font-size: 11px;
    font-weight: 500;
}

.info-tag__value {
    color: white;
    font-size: 14px;
    font-weight: 600;
}

.matrix-container {
    display: grid;
    gap: 18px;
}

.matrix-card {
    overflow: hidden;
    background: rgba(0, 0, 0, 0.15);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 8px;
}

.matrix-title {
    padding: 12px 16px;
    color: white;
    font-size: 16px;
    font-weight: 600;
    background: linear-gradient(135deg, #2968a8 0%, #1e5f8a 100%);
}

.table-scroll {
    overflow-x: auto;
}

.matrix-table {
    width: 100%;
    border-collapse: collapse;
}

.matrix-table th,
.matrix-table td {
    min-width: 100px;
    padding: 10px 12px;
    color: white;
    text-align: center;
    border: 1px solid rgba(255, 255, 255, 0.08);
}

.matrix-table thead th,
.matrix-table tbody th {
    color: #d4ecff;
    background: rgba(30, 136, 229, 0.2);
}

.matrix-table td {
    background: #1e4a6e;
}

.empty-data {
    padding: 28px;
    color: #b0d0f0;
    text-align: center;
}
</style>
