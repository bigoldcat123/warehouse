<template>
  <div class="w-full h-full bg-[#0b1b2b] relative overflow-hidden select-none">
    <!-- echarts 画布 -->
    <div ref="chartRef" class="w-full h-full"></div>

    <!-- 控制面板 -->
    <div v-if="panelOpen" class="absolute top-4 left-4 w-[260px] rounded-lg bg-[#10243a]/90 border border-[#2a4a6a] text-[#d6e4f0] p-4 shadow-lg backdrop-blur">
      <div class="flex items-center justify-between mb-3">
        <div class="text-base font-bold text-[#7ec3ff]">{{ cfg.title }}</div>
        <button
          @click="panelOpen = false"
          class="flex items-center justify-center w-6 h-6 rounded hover:bg-[#2a4a6a] cursor-pointer transition-colors"
          title="收起"
        >
          <svg viewBox="0 0 24 24" class="w-4 h-4 fill-none stroke-current stroke-2 text-[#9fb8cc]"><polyline points="15 18 9 12 15 6" /></svg>
        </button>
      </div>

      <div class="space-y-2 text-sm">
        <div class="flex items-center justify-between">
          <span>粮仓编号</span>
          <span class="text-[#7ec3ff]">{{ houseNo || '--' }}</span>
        </div>
        <div class="flex items-center justify-between">
          <span>采集时间</span>
          <el-select v-model="currentTime" size="small" class="w-[150px]" placeholder="选择时间">
            <el-option v-for="t in store.times" :key="t" :label="formatTime(t)" :value="t" />
          </el-select>
        </div>
        <div class="flex items-center justify-between">
          <span>切片方向</span>
          <el-select v-model="orientation" size="small" class="w-[120px]">
            <el-option label="水平·XY层" value="xy" />
            <el-option label="垂直·XZ列" value="xz" />
            <el-option label="垂直·YZ行" value="yz" />
          </el-select>
        </div>
        <div class="flex items-center justify-between">
          <span>切片位置</span>
          <el-select v-model="sliceIndex" size="small" class="w-[120px]">
            <el-option v-for="n in sliceCount" :key="n" :label="`第 ${n} ${sliceUnit}`" :value="n - 1" />
          </el-select>
        </div>
      </div>

      <div class="mt-3 pt-3 border-t border-[#2a4a6a] text-xs space-y-1 text-[#9fb8cc]">
        <div class="flex justify-between"><span>维度 X/Y/Z</span><span>{{ store.dimX }} × {{ store.dimY }} × {{ store.dimZ }}</span></div>
      </div>
    </div>

    <!-- 收起后的展开按钮 -->
    <button
      v-else
      @click="panelOpen = true"
      class="absolute top-4 left-4 w-9 h-9 rounded-lg bg-[#10243a]/90 border border-[#2a4a6a] text-[#9fb8cc] flex items-center justify-center shadow-lg backdrop-blur cursor-pointer hover:bg-[#1a3a5a] transition-colors"
      title="展开控制面板"
    >
      <svg viewBox="0 0 24 24" class="w-5 h-5 fill-none stroke-current stroke-2"><polyline points="9 18 15 12 9 6" /></svg>
    </button>

    <!-- 加载/空状态提示 -->
    <div v-if="store.loading" class="absolute inset-0 z-10 flex items-center justify-center text-sm text-[#7ec3ff] bg-[#0b1b2b]/70">
      {{ cfg.label }}数据加载中...
    </div>
    <div v-else-if="!houseNo" class="absolute inset-0 z-10 flex items-center justify-center text-sm text-[#9fb8cc] bg-[#0b1b2b]/70">
      缺少仓房编号，无法加载{{ cfg.label }}数据
    </div>
    <div v-else-if="store.error" class="absolute inset-0 z-10 flex items-center justify-center text-sm text-[#ff7a59] bg-[#0b1b2b]/70">
      {{ store.error }}
    </div>
    <div v-else-if="!store.times.length" class="absolute inset-0 z-10 flex items-center justify-center text-sm text-[#9fb8cc] bg-[#0b1b2b]/70">
      暂无{{ cfg.label }}数据
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import { useCubeStore, CUBE_METRICS, type CubeKind } from '@/stores/cube'
import { sliceCube, type CubeSliceOrientation } from '@/utils/cubeSlice'

const props = defineProps<{ houseNo: string; metric?: CubeKind }>()

const store = useCubeStore(props.metric ?? 'temperature')

const cfg = computed(() => CUBE_METRICS[props.metric ?? 'temperature'])

const chartRef = ref<HTMLDivElement | null>(null)
let chart: echarts.ECharts | undefined

const currentTime = computed<string | undefined>({
  get: () => store.currentTime ?? undefined,
  set: (v) => { if (v) store.selectTime(v) },
})

const orientation = ref<CubeSliceOrientation>('xy')
const sliceIndex = ref(0)
const panelOpen = ref(true)

const sliceCount = computed(() =>
  orientation.value === 'xy' ? store.dimZ : orientation.value === 'xz' ? store.dimY : store.dimX,
)
const sliceUnit = computed(() =>
  orientation.value === 'xy' ? '层' : orientation.value === 'xz' ? '列' : '行',
)

const slice = computed(() => sliceCube(store.currentData, orientation.value, sliceIndex.value))

/** 轴名：xName/yName */
const axisNames = computed(() => {
  if (orientation.value === 'xy') return { x: '列', y: '行' }
  if (orientation.value === 'xz') return { x: '层', y: '行' }
  return { x: '层', y: '列' }
})

// 方向切换或数据维度变化时收敛越界
watch(
  () => [orientation.value, store.currentTime, store.dimZ, store.dimX, store.dimY] as const,
  () => {
    sliceIndex.value = Math.min(sliceIndex.value, Math.max(0, sliceCount.value - 1))
  },
)

function formatTime(t: string) {
  return t.replace('T', ' ')
}

function buildOption() {
  const s = slice.value
  const names = axisNames.value
  const data: [number, number, number | string][] = []
  for (let r = 0; r < s.grid.length; r++) {
    for (let c = 0; c < s.grid[r].length; c++) {
      data.push([c, r, s.grid[r][c] ?? '-'])
    }
  }
  return {
    backgroundColor: 'transparent',
    grid: { left: 70, right: 90, top: 40, bottom: 40 },
    tooltip: {
      formatter: (params: any) => {
        const c = params.value[0] as number
        const r = params.value[1] as number
        const head = `${names.y}${s.rows[r] ?? '-'} · ${names.x}${s.cols[c] ?? '-'}`
        const v = s.grid[r]?.[c]
        if (v === null || v === undefined) return `${head}<br/>无数据`
        return `${head}<br/>${cfg.value.label}：<b>${v.toFixed(1)} ${cfg.value.unit}</b>`
      },
    },
    xAxis: {
      type: 'category',
      data: s.cols,
      name: names.x,
      nameTextStyle: { color: '#9fb8cc' },
      axisLabel: { color: '#9fb8cc' },
      axisLine: { lineStyle: { color: 'rgba(255,255,255,0.15)' } },
      splitArea: { show: true, areaStyle: { color: ['rgba(16,36,58,0.4)', 'rgba(16,36,58,0.9)'] } },
    },
    yAxis: {
      type: 'category',
      data: s.rows,
      name: names.y,
      nameTextStyle: { color: '#9fb8cc' },
      axisLabel: { color: '#9fb8cc' },
      axisLine: { lineStyle: { color: 'rgba(255,255,255,0.15)' } },
      splitArea: { show: true, areaStyle: { color: ['rgba(16,36,58,0.4)', 'rgba(16,36,58,0.9)'] } },
    },
    visualMap: {
      min: cfg.value.min,
      max: cfg.value.max,
      calculable: true,
      orient: 'vertical',
      right: 0,
      top: 'center',
      textStyle: { color: '#9fb8cc' },
      inRange: { color: ['#2b6cff', '#22c3d6', '#3ecf5a', '#f2c531', '#f0433a'] },
    },
    series: [
      {
        type: 'heatmap',
        data,
        label: { show: true, color: '#e8f2fb', fontSize: 11 },
        itemStyle: { borderColor: '#0b1b2b', borderWidth: 1 },
      },
    ],
  }
}

function render() {
  if (!chart) return
  chart.setOption(buildOption(), true)
}

function ensureChart() {
  if (chart) return
  if (!chartRef.value) return
  chart = echarts.init(chartRef.value)
  render()
}

function onResize() {
  chart?.resize()
}

watch(
  () => [slice.value, cfg.value] as const,
  () => nextTick(render),
)

watch(() => props.houseNo, (no) => store.fetchCube(no), { immediate: true })

onMounted(() => {
  ensureChart()
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  chart?.dispose()
  chart = undefined
})
</script>
