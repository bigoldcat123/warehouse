<template>
  <PageFrame>
    <template #title>{{ houseName || houseNo || '仓房可视化' }}</template>
    <template #subtitle>{{ metricLabel }} · {{ viewLabel }}</template>
    <template #actions>
      <button class="return-button" title="返回仓库全景图" @click="router.push('/warehousePanorama')">↩</button>
    </template>

    <div class="visualization-shell">
      <Granary3d v-if="viewMode === '3d'" :house-no="houseNo" :metric="metric" />
      <CubeHeatmap
        v-else-if="viewMode === 'heatmap'"
        :house-no="houseNo"
        :metric="heatmapMetric"
      />
      <LineChartPage v-else :metric="lineMetric" embedded />

      <aside class="view-navigation">
        <div class="view-navigation__title">图表类型</div>
        <button
          v-for="item in viewOptions"
          :key="item.value"
          :class="{ active: viewMode === item.value }"
          @click="selectView(item.value)"
        >
          <span>{{ item.icon }}</span>
          {{ item.label }}
        </button>
      </aside>
    </div>
  </PageFrame>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PageFrame from '@/components/common/PageFrame.vue'
import Granary3d from '@/components/Granary3d.vue'
import CubeHeatmap from '@/components/CubeHeatmap.vue'
import LineChartPage from '@/components/LineChartPage.vue'

type Metric = 'temperature' | 'humidity' | 'ph3'
type ViewMode = '3d' | 'heatmap' | 'line'

const route = useRoute()
const router = useRouter()
const houseNo = computed(() => (route.query.houseNo as string) || '')
const houseName = computed(() => (route.query.houseName as string) || '')
const metric = computed<Metric>(() => {
  const value = route.query.metric
  return value === 'humidity' || value === 'ph3' ? value : 'temperature'
})
const viewMode = computed<ViewMode>(() => {
  const value = route.query.view
  return value === 'heatmap' || value === 'line' ? value : '3d'
})

const metricLabel = computed(() => ({
  temperature: '温度',
  humidity: '湿度',
  ph3: 'PH3',
})[metric.value])

const viewLabel = computed(() => ({
  '3d': '3D图',
  heatmap: '热力图',
  line: '曲线图',
})[viewMode.value])

const heatmapMetric = computed<'temperature' | 'humidity' | 'gas'>(() =>
  metric.value === 'ph3' ? 'gas' : metric.value,
)
const lineMetric = computed<'temp' | 'humidity' | 'gas'>(() => {
  if (metric.value === 'temperature') return 'temp'
  return metric.value === 'ph3' ? 'gas' : 'humidity'
})

const viewOptions: Array<{ value: ViewMode; label: string; icon: string }> = [
  { value: '3d', label: '3D图', icon: '◈' },
  { value: 'heatmap', label: '热力图', icon: '▦' },
  { value: 'line', label: '曲线图', icon: '⌁' },
]

function selectView(view: ViewMode) {
  router.replace({
    path: '/warehouseVisualization',
    query: { ...route.query, view },
  })
}
</script>

<style scoped>
.visualization-shell {
  position: relative;
  width: 100%;
  height: 100%;
}

.view-navigation {
  position: absolute;
  top: 50%;
  left: -150px;
  z-index: 40;
  display: grid;
  width: 126px;
  gap: 8px;
  padding: 12px;
  color: #d6e4f0;
  background: rgba(16, 36, 58, 0.93);
  border: 1px solid #2a4a6a;
  border-radius: 10px;
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.32);
  transform: translateY(-50%);
  backdrop-filter: blur(8px);
}

.view-navigation__title {
  padding-bottom: 8px;
  color: #8cb7d2;
  font-size: 12px;
  text-align: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.view-navigation button {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 10px;
  color: #b8cede;
  font-size: 13px;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 6px;
  transition: 160ms ease;
}

.view-navigation button:hover,
.view-navigation button.active {
  color: #ffffff;
  background: #1e88e5;
  border-color: #64b5f6;
}

.view-navigation button span {
  width: 18px;
  color: #9dd5ff;
  font-size: 17px;
  text-align: center;
}

.return-button {
  display: grid;
  width: 24px;
  height: 24px;
  place-items: center;
  color: #7ec3ff;
  font-size: 20px;
  cursor: pointer;
  background: transparent;
  border: 0;
}
</style>
