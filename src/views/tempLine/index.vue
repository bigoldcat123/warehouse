<template>
    <div class="temp-page">
        <!-- 顶部：仓房信息 + 模式切换 -->
        <header class="temp-header">
            <div class="temp-header__title">
                <svg class="w-5 h-5 text-[#64b5f6]" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M3 13h2v-2H3v2zm0 4h2v-2H3v2zm0-8h2V7H3v2zm4 4h14v-2H7v2zm0 4h14v-2H7v2zM7 7v2h14V7H7z" />
                </svg>
                <span class="text-white text-lg font-bold">
                    {{ houseName || houseNo || '温度折线图' }}
                </span>
                <span v-if="houseNo" class="text-[#b0d0f0] text-sm ml-1">温度折线图</span>
            </div>
            <div v-if="houseNo" class="temp-header__modes">
                <button
                    v-for="m in modes"
                    :key="m.key"
                    :class="['mode-btn', { 'mode-btn--active': mode === m.key }]"
                    @click="mode = m.key"
                >
                    {{ m.label }}
                </button>
            </div>
        </header>

        <!-- 缺少 houseNo 的兜底提示 -->
        <div v-if="!houseNo" class="temp-missing">
            缺少仓房信息，请从仓房卡片上的「温度折线图」按钮进入
        </div>

        <div v-else class="temp-body">
            <!-- 左侧控制面板 -->
            <aside class="temp-controls">
                <!-- 模式1：按点 -->
                <template v-if="mode === 'point'">
                    <div class="control-title">选择测温点</div>
                    <div class="control-desc">层/行/列坐标均为从 1 开始的整数</div>

                    <div class="field">
                        <label>层 (z, 1~{{ dimZ }})</label>
                        <el-input-number v-model="pointInput.z" :min="1" :max="dimZ" controls-position="right" />
                    </div>
                    <div class="field">
                        <label>行 (x, 1~{{ dimX }})</label>
                        <el-input-number v-model="pointInput.x" :min="1" :max="dimX" controls-position="right" />
                    </div>
                    <div class="field">
                        <label>列 (y, 1~{{ dimY }})</label>
                        <el-input-number v-model="pointInput.y" :min="1" :max="dimY" controls-position="right" />
                    </div>

                    <button class="add-btn" :disabled="adding" @click="addPoint">
                        {{ adding ? '加载中...' : '+ 添加该点' }}
                    </button>

                    <div v-if="points.length" class="tag-section">
                        <div class="tag-title">已添加 ({{ points.length }})</div>
                        <div class="tag-list">
                            <span v-for="p in points" :key="p.key" class="tag">
                                <span class="tag__dot" :style="{ background: colorOf(p.key) }"></span>
                                层{{ p.z }}-行{{ p.x }}-列{{ p.y }}
                                <button class="tag__close" @click="removePoint(p.key)">×</button>
                            </span>
                        </div>
                        <button class="clear-btn" @click="clearPoints">清空</button>
                    </div>
                </template>

                <!-- 模式2：按层 -->
                <template v-else-if="mode === 'layer'">
                    <div class="control-title">选择层</div>
                    <div class="control-desc">每添加一层，显示该层所有测温点的平均温度折线</div>

                    <div class="field">
                        <label>层 (1~{{ dimZ }})</label>
                        <el-input-number v-model="layerInput" :min="1" :max="dimZ" controls-position="right" />
                    </div>

                    <button class="add-btn" :disabled="adding" @click="addLayer">
                        {{ adding ? '加载中...' : '+ 添加该层' }}
                    </button>

                    <div v-if="layers.length" class="tag-section">
                        <div class="tag-title">已添加 ({{ layers.length }})</div>
                        <div class="tag-list">
                            <span v-for="l in layers" :key="l.key" class="tag">
                                <span class="tag__dot" :style="{ background: colorOf(l.key) }"></span>
                                第 {{ l.z }} 层平均
                                <button class="tag__close" @click="removeLayer(l.key)">×</button>
                            </span>
                        </div>
                        <button class="clear-btn" @click="clearLayers">清空</button>
                    </div>
                </template>

                <!-- 模式3：全仓平均 -->
                <template v-else>
                    <div class="control-title">全仓平均温度</div>
                    <div class="control-desc">
                        显示当前仓房所有测温点在每个时间点的平均温度。
                    </div>
                    <button class="add-btn" :disabled="adding" @click="fetchAvg">
                        {{ adding ? '加载中...' : '查询' }}
                    </button>
                    <div v-if="avgLoaded && !avgData.length" class="empty-tip">暂无数据</div>
                </template>
            </aside>

            <!-- 图表区：图表 div 始终渲染，空态以蒙层形式覆盖 -->
            <section class="temp-chart-wrap">
                <div ref="chartRef" class="temp-chart"></div>
                <div v-if="chartEmpty" class="temp-chart-empty">
                    {{ emptyTip }}
                </div>
            </section>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import dataApi from '@/api/data'
import type { type_TempRecord } from '@/api/data'

// ========= 路由参数 =========
const route = useRoute()
const houseNo = computed(() => (route.query.houseNo as string) || '')
const houseName = computed(() => (route.query.houseName as string) || '')
const dimX = computed(() => Math.max(1, Number(route.query.x) || 1)) // 行数上限
const dimY = computed(() => Math.max(1, Number(route.query.y) || 1)) // 列数上限
const dimZ = computed(() => Math.max(1, Number(route.query.z) || 1)) // 层数上限

// ========= 模式 =========
type Mode = 'point' | 'layer' | 'avg'
const modes: Array<{ key: Mode; label: string }> = [
    { key: 'point', label: '按点' },
    { key: 'layer', label: '按层' },
    { key: 'avg', label: '全仓平均' },
]
const mode = ref<Mode>('point')

// ========= 模式1：按点 =========
const pointInput = ref({ x: 1, y: 1, z: 1 })
type PointItem = { key: string; x: number; y: number; z: number; data: type_TempRecord[] }
const points = ref<PointItem[]>([])

const pointKey = (x: number, y: number, z: number) => `${z}-${x}-${y}`

async function addPoint() {
    if (!houseNo.value) return
    const { x, y, z } = pointInput.value
    const key = pointKey(x, y, z)
    if (points.value.some(p => p.key === key)) {
        ElMessage.warning('该点已添加')
        return
    }
    adding.value = true
    try {
        const res = await dataApi.tempRecords(houseNo.value, z, x, y)
        const data: type_TempRecord[] = res.data?.value ?? []
        points.value.push({ key, x, y, z, data })
        if (!data.length) {
            ElMessage.info(`层${z}-行${x}-列${y} 暂无数据`)
        }
    } catch (e: any) {
        ElMessage.error(e?.message || '查询失败')
    } finally {
        adding.value = false
    }
}
function removePoint(key: string) {
    points.value = points.value.filter(p => p.key !== key)
}
function clearPoints() {
    points.value = []
}

// ========= 模式2：按层 =========
const layerInput = ref(1)
type LayerItem = { key: string; z: number; data: type_TempRecord[] }
const layers = ref<LayerItem[]>([])

const layerKey = (z: number) => `${z}`

async function addLayer() {
    if (!houseNo.value) return
    const z = layerInput.value
    const key = layerKey(z)
    if (layers.value.some(l => l.key === key)) {
        ElMessage.warning('该层已添加')
        return
    }
    adding.value = true
    try {
        const res = await dataApi.layerAvgTemp(houseNo.value, z)
        const data: type_TempRecord[] = res.data?.value ?? []
        layers.value.push({ key, z, data })
        if (!data.length) {
            ElMessage.info(`第 ${z} 层暂无数据`)
        }
    } catch (e: any) {
        ElMessage.error(e?.message || '查询失败')
    } finally {
        adding.value = false
    }
}
function removeLayer(key: string) {
    layers.value = layers.value.filter(l => l.key !== key)
}
function clearLayers() {
    layers.value = []
}

// ========= 模式3：全仓平均 =========
const avgData = ref<type_TempRecord[]>([])
const avgLoaded = ref(false)

async function fetchAvg() {
    if (!houseNo.value) return
    adding.value = true
    try {
        const res = await dataApi.allAvgTemp(houseNo.value)
        avgData.value = res.data?.value ?? []
        avgLoaded.value = true
        if (!avgData.value.length) {
            ElMessage.info('暂无数据')
        }
    } catch (e: any) {
        ElMessage.error(e?.message || '查询失败')
    } finally {
        adding.value = false
    }
}

// ========= 通用状态 =========
const adding = ref(false)

// ========= 颜色 =========
// 固定调色板，给折线 & tag 小圆点统一配色
const PALETTE = ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc']
const colorMap = new Map<string, string>()
let colorIdx = 0
function colorOf(key: string) {
    if (!colorMap.has(key)) {
        colorMap.set(key, PALETTE[colorIdx % PALETTE.length])
        colorIdx++
    }
    return colorMap.get(key)!
}

// ========= 图表 =========
const chartRef = ref<HTMLDivElement | null>(null)
let chart: echarts.ECharts | undefined

const chartEmpty = computed(() => {
    if (mode.value === 'point') return points.value.length === 0
    if (mode.value === 'layer') return layers.value.length === 0
    return !avgLoaded.value || avgData.value.length === 0
})

const emptyTip = computed(() => {
    if (mode.value === 'point') return '在左侧添加测温点，查看温度变化趋势'
    if (mode.value === 'layer') return '在左侧添加层，查看各层平均温度'
    return '点击「查询」获取全仓平均温度'
})

function buildSeries(): any[] {
    if (mode.value === 'point') {
        return points.value.map(p => ({
            name: `层${p.z}-行${p.x}-列${p.y}`,
            type: 'line',
            smooth: true,
            showSymbol: true,
            itemStyle: { color: colorOf(p.key) },
            data: p.data.map(d => [d.testDate, d.temp]),
        }))
    }
    if (mode.value === 'layer') {
        return layers.value.map(l => ({
            name: `第${l.z}层平均`,
            type: 'line',
            smooth: true,
            showSymbol: true,
            itemStyle: { color: colorOf(l.key) },
            data: l.data.map(d => [d.testDate, d.temp]),
        }))
    }
    return [
        {
            name: '全仓平均',
            type: 'line',
            smooth: true,
            showSymbol: true,
            itemStyle: { color: colorOf('avg') },
            data: avgData.value.map(d => [d.testDate, d.temp]),
        },
    ]
}

function renderChart() {
    if (!chart) return
    // 容器尺寸为 0 时不渲染（避免 ECharts 内部报错）
    const dom = chart.getDom()
    if (!dom || dom.clientWidth === 0 || dom.clientHeight === 0) return

    const series = buildSeries()
    chart.setOption(
        {
            backgroundColor: 'transparent',
            tooltip: {
                trigger: 'axis',
                backgroundColor: 'rgba(15, 36, 64, 0.92)',
                borderColor: 'rgba(255,255,255,0.15)',
                textStyle: { color: '#ffffff' },
            },
            legend: {
                data: series.map((s: any) => s.name),
                textStyle: { color: '#b0d0f0' },
                top: 8,
            },
            grid: { left: 56, right: 24, top: 56, bottom: 40 },
            xAxis: {
                type: 'time',
                axisLabel: { color: '#b0d0f0' },
                axisLine: { lineStyle: { color: 'rgba(255,255,255,0.15)' } },
                splitLine: { show: false },
            },
            yAxis: {
                type: 'value',
                name: '温度 (℃)',
                nameTextStyle: { color: '#b0d0f0', padding: [0, 40, 0, 0] },
                axisLabel: { color: '#b0d0f0' },
                axisLine: { lineStyle: { color: 'rgba(255,255,255,0.15)' } },
                splitLine: { lineStyle: { color: 'rgba(255,255,255,0.06)' } },
            },
            series,
        },
        true, // notMerge：完整替换，避免系列数量变化时残留旧数据
    )
}

function ensureChart() {
    if (chart) return
    if (!chartRef.value) return
    chart = echarts.init(chartRef.value)
    renderChart()
}

// ========= 响应式触发 =========
// 数据变化 → 重绘
watch(
    () => [mode.value, points.value.length, layers.value.length, avgData.value.length, avgLoaded.value],
    () => nextTick(renderChart),
)

// 图表容器从隐藏到显示（例如切换模式后数据变化使 chartEmpty 翻转），需要 resize
watch(chartEmpty, (isEmpty) => {
    if (!isEmpty) {
        nextTick(() => {
            chart?.resize()
            renderChart()
        })
    }
})

// ========= 生命周期 =========
function handleResize() {
    chart?.resize()
}

onMounted(() => {
    // 仅在有 houseNo 时才初始化图表
    if (!houseNo.value) return
    // 延迟到下一个 tick，确保 DOM 布局完成、容器有真实尺寸
    nextTick(() => {
        ensureChart()
    })
    window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
    window.removeEventListener('resize', handleResize)
    chart?.dispose()
    chart = undefined
})
</script>

<style scoped>
.temp-page {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.temp-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 14px 20px;
    background: linear-gradient(135deg, rgba(30, 136, 229, 0.25) 0%, rgba(21, 101, 192, 0.15) 100%);
    border: 1px solid rgba(255, 255, 255, 0.08);
    border-radius: 8px;
}
.temp-header__title {
    display: flex;
    align-items: center;
    gap: 8px;
}
.temp-header__modes {
    display: flex;
    gap: 6px;
    background: rgba(0, 0, 0, 0.2);
    padding: 4px;
    border-radius: 6px;
}
.mode-btn {
    padding: 6px 18px;
    border-radius: 4px;
    color: #b0d0f0;
    font-size: 13px;
    font-weight: 600;
    background: transparent;
    border: none;
    cursor: pointer;
    transition: all 0.2s ease;
}
.mode-btn:hover {
    color: #ffffff;
}
.mode-btn--active {
    background: #1e88e5;
    color: #ffffff;
    box-shadow: 0 2px 6px rgba(30, 136, 229, 0.4);
}

.temp-missing {
    padding: 40px;
    text-align: center;
    color: #b0d0f0;
    font-size: 14px;
    background: rgba(255, 255, 255, 0.04);
    border-radius: 8px;
}

.temp-body {
    display: flex;
    gap: 16px;
}

/* 控制面板 */
.temp-controls {
    width: 260px;
    flex-shrink: 0;
    background: linear-gradient(145deg, #2968a8 0%, #1e5f8a 100%);
    border-radius: 8px;
    padding: 18px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}
.control-title {
    color: #ffffff;
    font-size: 15px;
    font-weight: 700;
    margin-bottom: 4px;
}
.control-desc {
    color: #b0d0f0;
    font-size: 12px;
    margin-bottom: 14px;
    line-height: 1.5;
}

.field {
    margin-bottom: 12px;
}
.field label {
    display: block;
    color: #b0d0f0;
    font-size: 12px;
    margin-bottom: 4px;
}

/* Element Plus 暗色覆盖 */
.temp-controls :deep(.el-input-number) {
    width: 100%;
}
.temp-controls :deep(.el-input__wrapper) {
    background: rgba(0, 0, 0, 0.25) !important;
    box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.12) inset !important;
}
.temp-controls :deep(.el-input__inner) {
    color: #ffffff !important;
}
.temp-controls :deep(.el-input-number__decrease),
.temp-controls :deep(.el-input-number__increase) {
    background: rgba(255, 255, 255, 0.06) !important;
    border-color: rgba(255, 255, 255, 0.12) !important;
    color: #b0d0f0 !important;
}

.add-btn {
    width: 100%;
    padding: 8px 12px;
    background: #1e88e5;
    color: #ffffff;
    border: none;
    border-radius: 4px;
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s ease;
    margin-top: 4px;
}
.add-btn:hover:not(:disabled) {
    background: #1976d2;
}
.add-btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

.tag-section {
    margin-top: 18px;
    padding-top: 14px;
    border-top: 1px solid rgba(255, 255, 255, 0.1);
}
.tag-title {
    color: #b0d0f0;
    font-size: 12px;
    margin-bottom: 8px;
}
.tag-list {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
}
.tag {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 4px 8px;
    background: rgba(255, 255, 255, 0.08);
    border: 1px solid rgba(255, 255, 255, 0.12);
    border-radius: 12px;
    color: #ffffff;
    font-size: 12px;
}
.tag__dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
}
.tag__close {
    background: none;
    border: none;
    color: #b0d0f0;
    cursor: pointer;
    font-size: 14px;
    padding: 0 2px;
    line-height: 1;
}
.tag__close:hover {
    color: #ff6b6b;
}
.clear-btn {
    margin-top: 10px;
    background: transparent;
    border: 1px solid rgba(255, 255, 255, 0.15);
    color: #b0d0f0;
    padding: 4px 12px;
    border-radius: 4px;
    font-size: 12px;
    cursor: pointer;
}
.clear-btn:hover {
    color: #ff6b6b;
    border-color: #ff6b6b;
}

.empty-tip {
    margin-top: 12px;
    color: #b0d0f0;
    font-size: 12px;
    text-align: center;
}

/* 图表区 */
.temp-chart-wrap {
    flex: 1;
    background: linear-gradient(145deg, #2968a8 0%, #1e5f8a 100%);
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
    position: relative;
    height: calc(100vh - 220px);
    min-height: 500px;
}
.temp-chart {
    width: 100%;
    height: 100%;
}
.temp-chart-empty {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #b0d0f0;
    font-size: 14px;
    background: rgba(0, 0, 0, 0.15);
    pointer-events: none;
}
</style>
