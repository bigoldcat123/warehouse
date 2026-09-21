<template>
  <div ref="rootRef" class="heatmap-root">
    <canvas ref="canvasRef" class="heatmap-canvas" @mousemove="onHover" @mouseleave="tooltip = null" />

    <div v-if="tooltip" class="heatmap-tooltip" :style="{ left: `${tooltip.x}px`, top: `${tooltip.y}px` }">
      <div>第 {{ tooltip.stringNo }} 串 · 第 {{ selectedLayer + 1 }} 层</div>
      <strong>{{ tooltip.value.toFixed(1) }} {{ cfg.unit }}</strong>
    </div>

    <aside v-if="panelOpen" class="control-panel">
      <div class="panel-header">
        <strong>{{ cfg.title }}</strong>
        <button title="收起" @click="panelOpen = false">‹</button>
      </div>

      <div class="control-row">
        <span>仓房编号</span>
        <b>{{ houseNo || '--' }}</b>
      </div>
      <div class="control-row">
        <span>采集时间</span>
        <el-select v-model="selectedTime" size="small" class="control-select" @wheel.prevent="changeTimeByWheel">
          <el-option v-for="time in testDates" :key="time" :label="time" :value="time" />
        </el-select>
      </div>
      <div class="control-row">
        <span>查看层数</span>
        <el-select v-model="selectedLayer" size="small" class="control-select" @wheel.prevent="changeLayerByWheel">
          <el-option v-for="layer in layerCount" :key="layer" :label="`第 ${layer} 层`" :value="layer - 1" />
        </el-select>
      </div>

      <div class="summary">
        <div><span>串数</span><b>{{ stringCount }}</b></div>
        <div><span>当前层平均</span><b>{{ stats.avg }} {{ cfg.unit }}</b></div>
        <div><span>当前层最高</span><b>{{ stats.max }} {{ cfg.unit }}</b></div>
        <div><span>当前层最低</span><b>{{ stats.min }} {{ cfg.unit }}</b></div>
      </div>
    </aside>

    <button v-else class="panel-open" title="展开控制面板" @click="panelOpen = true">›</button>

    <div v-if="loading" class="state-mask">{{ cfg.label }}数据加载中...</div>
    <div v-else-if="!houseNo" class="state-mask">缺少仓房编号，无法加载数据</div>
    <div v-else-if="error" class="state-mask state-mask--error">{{ error }}</div>
    <div v-else-if="!testDates.length" class="state-mask">暂无{{ cfg.label }}数据</div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import receiverDataApi, { type SensorMatrix } from '@/api/receiverData'

type HeatMetric = 'temperature' | 'humidity' | 'gas'

const props = withDefaults(defineProps<{ houseNo: string; metric?: HeatMetric }>(), {
  metric: 'temperature',
})

const METRICS = {
  temperature: { title: '温度俯视热力图', label: '温度', unit: '℃', min: 10, max: 40 },
  humidity: { title: '湿度俯视热力图', label: '湿度', unit: '%', min: 20, max: 90 },
  gas: { title: 'PH3 俯视热力图', label: 'PH3', unit: 'ppm', min: 0, max: 1000 },
} as const

const MATRIX_APIS = {
  temperature: receiverDataApi.temperature.bind(receiverDataApi),
  humidity: receiverDataApi.humidity.bind(receiverDataApi),
  gas: receiverDataApi.ph3.bind(receiverDataApi),
}

const cfg = computed(() => METRICS[props.metric])
const rootRef = ref<HTMLDivElement>()
const canvasRef = ref<HTMLCanvasElement>()
const testDates = ref<string[]>([])
const selectedTime = ref('')
const selectedLayer = ref(0)
const matrix = ref<SensorMatrix>([])
const loadingDates = ref(false)
const loadingMatrix = ref(false)
const error = ref('')
const panelOpen = ref(true)
const loading = computed(() => loadingDates.value || loadingMatrix.value)
const stringCount = computed(() => matrix.value.length)
const layerCount = computed(() => matrix.value[0]?.length ?? 0)

const STRING_POSITIONS = [
  { x: 0.17, y: 0.18 },
  { x: 0.83, y: 0.18 },
  { x: 0.83, y: 0.78 },
  { x: 0.17, y: 0.78 },
  { x: 0.50, y: 0.48 },
  { x: 0.50, y: 0.80 },
  { x: 0.27, y: 0.49 },
]

const layerPoints = computed(() => matrix.value.flatMap((layers, stringIndex) => {
  const value = layers[selectedLayer.value]
  const position = STRING_POSITIONS[stringIndex]
  return Number.isFinite(value) && position ? [{ stringIndex, value, ...position }] : []
}))

const stats = computed(() => {
  const values = layerPoints.value.map(point => point.value)
  if (!values.length) return { avg: '--', min: '--', max: '--' }
  const sum = values.reduce((total, value) => total + value, 0)
  return {
    avg: (sum / values.length).toFixed(1),
    min: Math.min(...values).toFixed(1),
    max: Math.max(...values).toFixed(1),
  }
})

let datesRequestId = 0
let matrixRequestId = 0

async function loadDates() {
  const requestId = ++datesRequestId
  matrix.value = []
  testDates.value = []
  selectedTime.value = ''
  error.value = ''
  if (!props.houseNo) return
  loadingDates.value = true
  try {
    const response = await receiverDataApi.testDates(props.houseNo)
    if (requestId !== datesRequestId) return
    testDates.value = response.data?.value ?? []
    selectedTime.value = testDates.value[testDates.value.length - 1] ?? ''
  } catch (cause) {
    if (requestId === datesRequestId) error.value = (cause as Error)?.message || '采集时间加载失败'
  } finally {
    if (requestId === datesRequestId) loadingDates.value = false
  }
}

async function loadMatrix() {
  const requestId = ++matrixRequestId
  matrix.value = []
  error.value = ''
  if (!props.houseNo || !selectedTime.value) return
  loadingMatrix.value = true
  try {
    const response = await MATRIX_APIS[props.metric](props.houseNo, selectedTime.value)
    if (requestId !== matrixRequestId) return
    matrix.value = response.data?.value ?? []
    selectedLayer.value = Math.min(selectedLayer.value, Math.max(0, layerCount.value - 1))
    if (matrix.value.length && ![5, 7].includes(matrix.value.length)) {
      error.value = `传感器串数应为 5 或 7，当前为 ${matrix.value.length}`
    }
  } catch (cause) {
    if (requestId === matrixRequestId) error.value = (cause as Error)?.message || '热力图数据加载失败'
  } finally {
    if (requestId === matrixRequestId) loadingMatrix.value = false
  }
}

function changeTimeByWheel(event: WheelEvent) {
  if (!testDates.value.length) return
  const current = testDates.value.indexOf(selectedTime.value)
  selectedTime.value = testDates.value[(current + (event.deltaY > 0 ? 1 : -1) + testDates.value.length) % testDates.value.length]
}

function changeLayerByWheel(event: WheelEvent) {
  if (!layerCount.value) return
  selectedLayer.value = (selectedLayer.value + (event.deltaY > 0 ? 1 : -1) + layerCount.value) % layerCount.value
}

function palette(ratio: number): [number, number, number] {
  const stops: Array<[number, [number, number, number]]> = [
    [0, [40, 100, 220]],
    [0.25, [25, 185, 207]],
    [0.5, [67, 202, 102]],
    [0.75, [240, 200, 60]],
    [1, [237, 75, 62]],
  ]
  const value = Math.max(0, Math.min(1, ratio))
  for (let index = 0; index < stops.length - 1; index++) {
    const [start, startColor] = stops[index]
    const [end, endColor] = stops[index + 1]
    if (value <= end) {
      const progress = (value - start) / (end - start)
      return startColor.map((channel, channelIndex) => Math.round(channel + (endColor[channelIndex] - channel) * progress)) as [number, number, number]
    }
  }
  return stops[stops.length - 1][1]
}

const MARGIN = { top: 34, right: 88, bottom: 48, left: 52 }
let plot = { x: 0, y: 0, width: 0, height: 0 }

function colorRatio(value: number) {
  return (value - cfg.value.min) / Math.max(1, cfg.value.max - cfg.value.min)
}

function draw() {
  const root = rootRef.value
  const canvas = canvasRef.value
  if (!root || !canvas) return
  const width = root.clientWidth
  const height = root.clientHeight
  if (!width || !height) return
  const dpr = Math.min(window.devicePixelRatio || 1, 2)
  canvas.width = Math.round(width * dpr)
  canvas.height = Math.round(height * dpr)
  const context = canvas.getContext('2d')
  if (!context) return
  context.setTransform(dpr, 0, 0, dpr, 0, 0)
  context.fillStyle = '#0b1b2b'
  context.fillRect(0, 0, width, height)

  plot = {
    x: MARGIN.left,
    y: MARGIN.top,
    width: Math.max(10, width - MARGIN.left - MARGIN.right),
    height: Math.max(10, height - MARGIN.top - MARGIN.bottom),
  }
  if (!layerPoints.value.length) return

  const fieldWidth = Math.min(240, Math.max(80, Math.round(plot.width / 3)))
  const fieldHeight = Math.min(180, Math.max(60, Math.round(plot.height / 3)))
  const field = document.createElement('canvas')
  field.width = fieldWidth
  field.height = fieldHeight
  const fieldContext = field.getContext('2d')!
  const image = fieldContext.createImageData(fieldWidth, fieldHeight)
  for (let y = 0; y < fieldHeight; y++) {
    for (let x = 0; x < fieldWidth; x++) {
      const nx = x / Math.max(1, fieldWidth - 1)
      const ny = y / Math.max(1, fieldHeight - 1)
      let weightedValue = 0
      let totalWeight = 0
      layerPoints.value.forEach(point => {
        const distanceSquared = (nx - point.x) ** 2 + (ny - point.y) ** 2
        const weight = 1 / (distanceSquared + 0.008) ** 1.5
        weightedValue += point.value * weight
        totalWeight += weight
      })
      const [red, green, blue] = palette(colorRatio(weightedValue / totalWeight))
      const offset = (y * fieldWidth + x) * 4
      image.data[offset] = red
      image.data[offset + 1] = green
      image.data[offset + 2] = blue
      image.data[offset + 3] = 235
    }
  }
  fieldContext.putImageData(image, 0, 0)
  context.imageSmoothingEnabled = true
  context.drawImage(field, plot.x, plot.y, plot.width, plot.height)

  context.strokeStyle = 'rgba(220, 240, 255, 0.85)'
  context.lineWidth = 2
  context.strokeRect(plot.x, plot.y, plot.width, plot.height)
  context.fillStyle = '#d6e4f0'
  context.font = '13px sans-serif'
  context.textAlign = 'center'
  context.fillText(`第 ${selectedLayer.value + 1} 层俯视图`, plot.x + plot.width / 2, 21)

  layerPoints.value.forEach(point => {
    const px = plot.x + point.x * plot.width
    const py = plot.y + point.y * plot.height
    const [red, green, blue] = palette(colorRatio(point.value))
    context.beginPath()
    context.arc(px, py, 16, 0, Math.PI * 2)
    context.fillStyle = `rgb(${red}, ${green}, ${blue})`
    context.fill()
    context.strokeStyle = '#ffffff'
    context.lineWidth = 2
    context.stroke()
    context.fillStyle = '#ffffff'
    context.font = 'bold 12px sans-serif'
    context.fillText(String(point.stringIndex + 1), px, py + 4)
  })

  drawLegend(context)
}

function drawLegend(context: CanvasRenderingContext2D) {
  const x = plot.x + plot.width + 24
  const y = plot.y
  const width = 14
  const gradient = context.createLinearGradient(0, y + plot.height, 0, y)
  for (let step = 0; step <= 8; step++) {
    const ratio = step / 8
    const [red, green, blue] = palette(ratio)
    gradient.addColorStop(ratio, `rgb(${red}, ${green}, ${blue})`)
  }
  context.fillStyle = gradient
  context.fillRect(x, y, width, plot.height)
  context.fillStyle = '#9fb8cc'
  context.font = '11px sans-serif'
  context.textAlign = 'left'
  context.fillText(String(cfg.value.max), x + 20, y + 5)
  context.fillText(String((cfg.value.min + cfg.value.max) / 2), x + 20, y + plot.height / 2 + 4)
  context.fillText(String(cfg.value.min), x + 20, y + plot.height)
  context.fillText(cfg.value.unit, x - 2, y - 10)
}

const tooltip = ref<{ x: number; y: number; stringNo: number; value: number } | null>(null)

function onHover(event: MouseEvent) {
  const root = rootRef.value
  if (!root) return
  const bounds = root.getBoundingClientRect()
  const mouseX = event.clientX - bounds.left
  const mouseY = event.clientY - bounds.top
  const point = layerPoints.value.find(item => {
    const x = plot.x + item.x * plot.width
    const y = plot.y + item.y * plot.height
    return Math.hypot(mouseX - x, mouseY - y) <= 22
  })
  tooltip.value = point ? {
    x: mouseX + 150 > root.clientWidth ? mouseX - 140 : mouseX + 12,
    y: mouseY + 65 > root.clientHeight ? mouseY - 58 : mouseY + 12,
    stringNo: point.stringIndex + 1,
    value: point.value,
  } : null
}

watch(() => props.houseNo, loadDates, { immediate: true })
watch(selectedTime, loadMatrix)
watch([matrix, selectedLayer, cfg], () => nextTick(draw), { deep: true })

let resizeObserver: ResizeObserver | undefined
onMounted(() => {
  resizeObserver = new ResizeObserver(draw)
  if (rootRef.value) resizeObserver.observe(rootRef.value)
  nextTick(draw)
})

onBeforeUnmount(() => resizeObserver?.disconnect())
</script>

<style scoped>
.heatmap-root {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
  background: #0b1b2b;
  user-select: none;
}

.heatmap-canvas {
  display: block;
  width: 100%;
  height: 100%;
}

.control-panel {
  position: absolute;
  top: 16px;
  left: 16px;
  width: 278px;
  padding: 16px;
  color: #d6e4f0;
  background: rgba(16, 36, 58, 0.93);
  border: 1px solid #2a4a6a;
  border-radius: 8px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(8px);
}

.panel-header,
.control-row,
.summary > div {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.panel-header {
  margin-bottom: 14px;
  color: #7ec3ff;
}

.panel-header button,
.panel-open {
  color: #9fb8cc;
  cursor: pointer;
  background: transparent;
  border: 0;
  font-size: 22px;
}

.control-row {
  min-height: 38px;
  font-size: 13px;
}

.control-row b {
  color: #7ec3ff;
}

.control-select {
  width: 165px;
}

.summary {
  display: grid;
  gap: 6px;
  padding-top: 12px;
  margin-top: 12px;
  color: #9fb8cc;
  font-size: 12px;
  border-top: 1px solid #2a4a6a;
}

.summary b {
  color: #d6e4f0;
}

.panel-open {
  position: absolute;
  top: 16px;
  left: 16px;
  width: 38px;
  height: 38px;
  background: rgba(16, 36, 58, 0.93);
  border: 1px solid #2a4a6a;
  border-radius: 8px;
}

.heatmap-tooltip {
  position: absolute;
  z-index: 20;
  padding: 7px 10px;
  color: #d6e4f0;
  font-size: 12px;
  pointer-events: none;
  white-space: nowrap;
  background: rgba(16, 36, 58, 0.96);
  border: 1px solid #2a4a6a;
  border-radius: 5px;
}

.heatmap-tooltip strong {
  color: #7ec3ff;
}

.state-mask {
  position: absolute;
  inset: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #9fb8cc;
  font-size: 14px;
  background: rgba(11, 27, 43, 0.72);
}

.state-mask--error {
  color: #ff7a59;
}
</style>
