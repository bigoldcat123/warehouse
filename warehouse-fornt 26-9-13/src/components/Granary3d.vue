<template>
  <div class="sensor-scene">
    <div ref="containerRef" class="sensor-scene__canvas"></div>

    <section class="control-panel">
      <h2>{{ cfg.title }}</h2>

      <div class="control-row">
        <span>仓房编号</span>
        <strong>{{ houseNo || '--' }}</strong>
      </div>
      <div class="control-row">
        <span>测试时间</span>
        <el-select
          v-model="selectedTime"
          size="small"
          placeholder="选择时间"
          class="time-select"
          @wheel.prevent="switchTime"
        >
          <el-option v-for="time in testDates" :key="time" :label="time" :value="time" />
        </el-select>
      </div>
      <div class="control-row">
        <span>显示层</span>
        <el-select v-model="visibleLayer" size="small" class="layer-select">
          <el-option label="全部" value="all" />
          <el-option
            v-for="layer in layerCount"
            :key="layer"
            :label="`第 ${layer} 层`"
            :value="layer - 1"
          />
        </el-select>
      </div>
      <div class="control-row">
        <span>自动旋转</span>
        <el-switch v-model="autoRotate" size="small" />
      </div>

      <div class="statistics">
        <div><span>排列</span><strong>{{ stringCount }} 串 × {{ layerCount }} 层</strong></div>
        <div><span>测点总数</span><strong>{{ stats.count }}</strong></div>
        <div><span>平均{{ cfg.label }}</span><strong>{{ stats.avg }} {{ cfg.unit }}</strong></div>
        <div><span>最大{{ cfg.label }}</span><strong class="value-high">{{ stats.max }} {{ cfg.unit }}</strong></div>
        <div><span>最小{{ cfg.label }}</span><strong class="value-low">{{ stats.min }} {{ cfg.unit }}</strong></div>
      </div>
    </section>
<!--
    <section class="layout-guide">
      <span>传感器布局</span>
      <strong>{{ stringCount || '--' }} 串</strong>
      <small>1–5 为固定位置，6–7 为七串仓房附加位置</small>
    </section>
-->
    <section class="legend">
      <span>{{ cfg.label }}（{{ cfg.unit }}）</span>
      <div class="legend__scale">
        <small>{{ colorRange.min }}</small>
        <i :style="{ background: legendGradient }"></i>
        <small>{{ colorRange.max }}</small>
      </div>
    </section>

    <div
      v-show="tooltip.visible"
      class="sensor-tooltip"
      :style="{ left: `${tooltip.x}px`, top: `${tooltip.y}px` }"
    >
      <span>第 {{ tooltip.stringNo }} 串 · 第 {{ tooltip.layerNo }} 层</span>
      <strong :style="{ color: tooltip.color }">
        {{ tooltip.value }} {{ cfg.unit }}
      </strong>
    </div>

    <div v-if="loading" class="scene-state">{{ cfg.label }}数据加载中...</div>
    <div v-else-if="!houseNo" class="scene-state">缺少仓房编号</div>
    <div v-else-if="error" class="scene-state scene-state--error">{{ error }}</div>
    <div v-else-if="!testDates.length" class="scene-state">暂无测试时间</div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import receiverDataApi, { type SensorMatrix } from '@/api/receiverData'
import { SENSOR_METRICS, type SensorMetric } from '@/features/receiverData3d/config'

interface SensorPoint {
  stringIndex: number
  layerIndex: number
  value: number
}

const props = withDefaults(defineProps<{
  houseNo: string
  metric?: SensorMetric
}>(), {
  metric: 'temperature',
})

const cfg = computed(() => SENSOR_METRICS[props.metric])
const containerRef = ref<HTMLDivElement>()
const testDates = ref<string[]>([])
const selectedTime = ref('')
const matrix = ref<SensorMatrix>([])
const loadingDates = ref(false)
const loadingMatrix = ref(false)
const error = ref('')
const visibleLayer = ref<number | 'all'>('all')
const autoRotate = ref(false)

const loading = computed(() => loadingDates.value || loadingMatrix.value)
const stringCount = computed(() => matrix.value.length)
const layerCount = computed(() => matrix.value[0]?.length ?? 0)
const points = computed<SensorPoint[]>(() => matrix.value.flatMap((layers, stringIndex) =>
  layers.map((value, layerIndex) => ({ stringIndex, layerIndex, value })),
))

const stats = computed(() => {
  const values = points.value.map((point) => point.value).filter(Number.isFinite)
  if (!values.length) return { count: 0, avg: '--', max: '--', min: '--' }
  const sum = values.reduce((total, value) => total + value, 0)
  return {
    count: values.length,
    avg: (sum / values.length).toFixed(1),
    max: Math.max(...values).toFixed(1),
    min: Math.min(...values).toFixed(1),
  }
})

const colorRange = computed(() => {
  const values = points.value.map((point) => point.value).filter(Number.isFinite)
  if (!values.length) return { min: 0, max: 1 }
  const min = Math.min(...values)
  const max = Math.max(...values)
  return min === max ? { min: min - 1, max: max + 1 } : { min, max }
})

const legendGradient = computed(() => {
  const stops = Array.from({ length: 5 }, (_, index) => {
    const ratio = index / 4
    return `#${paletteColor(ratio).getHexString()} ${ratio * 100}%`
  })
  return `linear-gradient(to right, ${stops.join(', ')})`
})

const tooltip = reactive({
  visible: false,
  x: 0,
  y: 0,
  stringNo: 0,
  layerNo: 0,
  value: '0.0',
  color: '#fff',
})

const STRING_POSITIONS = [
  { x: -6, z: -4.5 },
  { x: 6, z: -4.5 },
  { x: 6, z: 4.5 },
  { x: -6, z: 4.5 },
  { x: 0, z: 0 },
  { x: 0, z: 4.7 },
  { x: -4, z: 0.2 },
]
const ROOM_WIDTH = 18
const ROOM_DEPTH = 14
const ROOM_HEIGHT = 10
const SENSOR_TOP = 8.4
const SENSOR_BOTTOM = 1.25

let renderer: THREE.WebGLRenderer | undefined
let scene: THREE.Scene | undefined
let camera: THREE.PerspectiveCamera | undefined
let controls: OrbitControls | undefined
let sensorGroup: THREE.Group | undefined
let sensorMesh: THREE.InstancedMesh | undefined
let highlightMesh: THREE.Mesh | undefined
let animationFrame = 0
let matrixRequestId = 0
const raycaster = new THREE.Raycaster()
const pointer = new THREE.Vector2()
const dummy = new THREE.Object3D()

function paletteColor(ratio: number) {
  const value = Math.min(1, Math.max(0, ratio))
  const stops: Array<[number, THREE.Color]> = [
    [0, new THREE.Color('#2864dc')],
    [0.25, new THREE.Color('#19b9cf')],
    [0.5, new THREE.Color('#43ca66')],
    [0.75, new THREE.Color('#f0c83c')],
    [1, new THREE.Color('#ed4b3e')],
  ]
  for (let index = 0; index < stops.length - 1; index++) {
    const [start, startColor] = stops[index]
    const [end, endColor] = stops[index + 1]
    if (value <= end) {
      return startColor.clone().lerp(endColor, (value - start) / (end - start))
    }
  }
  return stops[stops.length - 1][1].clone()
}

function colorOf(value: number) {
  const { min, max } = colorRange.value
  return paletteColor((value - min) / (max - min))
}

function pointPosition(point: Pick<SensorPoint, 'stringIndex' | 'layerIndex'>) {
  const position = STRING_POSITIONS[point.stringIndex] ?? { x: 0, z: 0 }
  const layers = Math.max(layerCount.value, 1)
  const y = layers === 1
    ? (SENSOR_TOP + SENSOR_BOTTOM) / 2
    : SENSOR_TOP - (point.layerIndex / (layers - 1)) * (SENSOR_TOP - SENSOR_BOTTOM)
  return new THREE.Vector3(position.x, y, position.z)
}

function createStringLabel(stringNo: number) {
  const canvas = document.createElement('canvas')
  canvas.width = 180
  canvas.height = 72
  const context = canvas.getContext('2d')!
  context.fillStyle = 'rgba(11, 27, 43, 0.9)'
  context.strokeStyle = '#7ec3ff'
  context.lineWidth = 3
  context.beginPath()
  context.roundRect(3, 3, 174, 66, 18)
  context.fill()
  context.stroke()
  context.fillStyle = '#edf7ff'
  context.font = '600 30px sans-serif'
  context.textAlign = 'center'
  context.textBaseline = 'middle'
  context.fillText(`${stringNo}号串`, 90, 37)

  const texture = new THREE.CanvasTexture(canvas)
  texture.colorSpace = THREE.SRGBColorSpace
  const label = new THREE.Sprite(new THREE.SpriteMaterial({
    map: texture,
    transparent: true,
    depthTest: false,
  }))
  label.scale.set(2.7, 1.08, 1)
  label.renderOrder = 10
  return label
}

function disposeObject(object: THREE.Object3D) {
  object.traverse((child) => {
    if (child instanceof THREE.Sprite) {
      child.material.map?.dispose()
      child.material.dispose()
    }
    if (child instanceof THREE.Mesh || child instanceof THREE.LineSegments || child instanceof THREE.Line) {
      child.geometry.dispose()
      const materials = Array.isArray(child.material) ? child.material : [child.material]
      materials.forEach((material) => material.dispose())
    }
  })
}

function rebuildSensors() {
  if (!scene) return
  if (sensorGroup) {
    scene.remove(sensorGroup)
    disposeObject(sensorGroup)
  }
  sensorGroup = new THREE.Group()
  scene.add(sensorGroup)
  sensorMesh = undefined
  tooltip.visible = false
  if (highlightMesh) highlightMesh.visible = false

  if (![5, 7].includes(stringCount.value) || !layerCount.value) return

  const cableMaterial = new THREE.LineBasicMaterial({ color: '#7894a8', transparent: true, opacity: 0.72 })
  for (let stringIndex = 0; stringIndex < stringCount.value; stringIndex++) {
    const position = STRING_POSITIONS[stringIndex]
    const cableGeometry = new THREE.BufferGeometry().setFromPoints([
      new THREE.Vector3(position.x, SENSOR_BOTTOM - 0.35, position.z),
      new THREE.Vector3(position.x, SENSOR_TOP + 0.45, position.z),
    ])
    sensorGroup.add(new THREE.Line(cableGeometry, cableMaterial.clone()))
    const label = createStringLabel(stringIndex + 1)
    label.position.set(position.x, SENSOR_TOP + 1.15, position.z)
    sensorGroup.add(label)
  }

  const visiblePoints = points.value.filter((point) =>
    Number.isFinite(point.value)
    && (visibleLayer.value === 'all' || point.layerIndex === visibleLayer.value),
  )
  if (!visiblePoints.length) return

  const geometry = new THREE.SphereGeometry(0.42, 24, 18)
  const material = new THREE.MeshStandardMaterial({ roughness: 0.28, metalness: 0.08 })
  sensorMesh = new THREE.InstancedMesh(geometry, material, visiblePoints.length)
  visiblePoints.forEach((point, index) => {
    dummy.position.copy(pointPosition(point))
    dummy.updateMatrix()
    sensorMesh!.setMatrixAt(index, dummy.matrix)
    sensorMesh!.setColorAt(index, colorOf(point.value))
  })
  sensorMesh.instanceMatrix.needsUpdate = true
  sensorMesh.instanceColor!.needsUpdate = true
  sensorMesh.userData.points = visiblePoints
  sensorGroup.add(sensorMesh)
}

function createWarehouse() {
  if (!scene) return
  const warehouse = new THREE.Group()
  const floor = new THREE.Mesh(
    new THREE.BoxGeometry(ROOM_WIDTH + 1.5, 0.3, ROOM_DEPTH + 1.5),
    new THREE.MeshStandardMaterial({ color: '#1a3448', roughness: 0.88 }),
  )
  floor.position.y = -0.15
  warehouse.add(floor)

  const shellGeometry = new THREE.BoxGeometry(ROOM_WIDTH, ROOM_HEIGHT, ROOM_DEPTH)
  const shell = new THREE.Mesh(shellGeometry, new THREE.MeshPhongMaterial({
    color: '#7aa4bd',
    transparent: true,
    opacity: 0.055,
    side: THREE.BackSide,
    depthWrite: false,
  }))
  shell.position.y = ROOM_HEIGHT / 2
  warehouse.add(shell)

  const outline = new THREE.LineSegments(
    new THREE.EdgesGeometry(shellGeometry),
    new THREE.LineBasicMaterial({ color: '#4f7895', transparent: true, opacity: 0.8 }),
  )
  outline.position.y = ROOM_HEIGHT / 2
  warehouse.add(outline)
  scene.add(warehouse)
}

function initScene() {
  const container = containerRef.value
  if (!container) return
  renderer = new THREE.WebGLRenderer({ antialias: true })
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.setSize(container.clientWidth, container.clientHeight)
  renderer.outputColorSpace = THREE.SRGBColorSpace
  container.appendChild(renderer.domElement)

  scene = new THREE.Scene()
  scene.background = new THREE.Color('#0b1b2b')
  scene.fog = new THREE.Fog('#0b1b2b', 48, 100)

  camera = new THREE.PerspectiveCamera(48, container.clientWidth / container.clientHeight, 0.1, 200)
  camera.position.set(19, 16, 21)

  controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true
  controls.dampingFactor = 0.08
  controls.minDistance = 12
  controls.maxDistance = 55
  controls.target.set(0, ROOM_HEIGHT / 2, 0)

  scene.add(new THREE.HemisphereLight(0xc9e5ff, 0x132536, 1.1))
  const light = new THREE.DirectionalLight(0xffffff, 1.25)
  light.position.set(12, 22, 8)
  scene.add(light)

  const grid = new THREE.GridHelper(48, 24, 0x315875, 0x18354c)
  grid.position.y = -0.01
  scene.add(grid)
  createWarehouse()

  highlightMesh = new THREE.Mesh(
    new THREE.SphereGeometry(0.48, 24, 18),
    new THREE.MeshStandardMaterial({ roughness: 0.2, emissiveIntensity: 0.42 }),
  )
  highlightMesh.visible = false
  scene.add(highlightMesh)
  rebuildSensors()
  animate()
}

function animate() {
  animationFrame = requestAnimationFrame(animate)
  if (!renderer || !scene || !camera || !controls) return
  controls.update()
  renderer.render(scene, camera)
}

function handleResize() {
  const container = containerRef.value
  if (!container || !renderer || !camera) return
  camera.aspect = container.clientWidth / container.clientHeight
  camera.updateProjectionMatrix()
  renderer.setSize(container.clientWidth, container.clientHeight)
}

function handlePointerMove(event: PointerEvent) {
  const container = containerRef.value
  if (!container || !sensorMesh || !camera) return
  const rect = container.getBoundingClientRect()
  pointer.x = ((event.clientX - rect.left) / rect.width) * 2 - 1
  pointer.y = -((event.clientY - rect.top) / rect.height) * 2 + 1
  raycaster.setFromCamera(pointer, camera)
  const hit = raycaster.intersectObject(sensorMesh)[0]
  if (hit?.instanceId !== undefined) {
    const point = (sensorMesh.userData.points as SensorPoint[])[hit.instanceId]
    if (point) {
      const color = colorOf(point.value)
      tooltip.visible = true
      tooltip.x = event.clientX - rect.left + 14
      tooltip.y = event.clientY - rect.top + 14
      tooltip.stringNo = point.stringIndex + 1
      tooltip.layerNo = point.layerIndex + 1
      tooltip.value = point.value.toFixed(1)
      tooltip.color = `#${color.getHexString()}`
      if (highlightMesh) {
        highlightMesh.position.copy(pointPosition(point))
        const material = highlightMesh.material as THREE.MeshStandardMaterial
        material.color.copy(color)
        material.emissive.copy(color)
        highlightMesh.visible = true
      }
      return
    }
  }
  tooltip.visible = false
  if (highlightMesh) highlightMesh.visible = false
}

function handlePointerLeave() {
  tooltip.visible = false
  if (highlightMesh) highlightMesh.visible = false
}

function switchTime(event: WheelEvent) {
  if (!testDates.value.length) return
  const currentIndex = testDates.value.indexOf(selectedTime.value)
  const direction = event.deltaY > 0 ? 1 : -1
  const nextIndex = (currentIndex + direction + testDates.value.length) % testDates.value.length
  selectedTime.value = testDates.value[nextIndex]
}

async function fetchTestDates() {
  matrixRequestId++
  loadingMatrix.value = false
  matrix.value = []
  testDates.value = []
  selectedTime.value = ''
  error.value = ''
  if (!props.houseNo) return
  loadingDates.value = true
  try {
    const response = await receiverDataApi.testDates(props.houseNo)
    testDates.value = response.data.value ?? []
    selectedTime.value = testDates.value[testDates.value.length - 1] ?? ''
  } catch (reason) {
    error.value = String(reason || '测试时间加载失败')
  } finally {
    loadingDates.value = false
  }
}

async function fetchMatrix() {
  const houseNo = props.houseNo
  const testDate = selectedTime.value
  if (!houseNo || !testDate) {
    matrix.value = []
    return
  }
  const requestId = ++matrixRequestId
  loadingMatrix.value = true
  error.value = ''
  try {
    const response = await receiverDataApi[props.metric](houseNo, testDate)
    if (requestId !== matrixRequestId) return
    const nextMatrix = response.data.value ?? []
    if (nextMatrix.length && ![5, 7].includes(nextMatrix.length)) {
      throw new Error(`传感器串数应为 5 或 7，当前为 ${nextMatrix.length}`)
    }
    matrix.value = nextMatrix
    visibleLayer.value = 'all'
  } catch (reason) {
    if (requestId !== matrixRequestId) return
    matrix.value = []
    error.value = reason instanceof Error ? reason.message : String(reason || `${cfg.value.label}数据加载失败`)
  } finally {
    if (requestId === matrixRequestId) loadingMatrix.value = false
  }
}

watch(() => props.houseNo, fetchTestDates, { immediate: true })
watch([selectedTime, () => props.metric], fetchMatrix)
watch([points, visibleLayer], rebuildSensors)
watch(autoRotate, (enabled) => {
  if (controls) controls.autoRotate = enabled
})

onMounted(() => {
  initScene()
  window.addEventListener('resize', handleResize)
  renderer?.domElement.addEventListener('pointermove', handlePointerMove)
  renderer?.domElement.addEventListener('pointerleave', handlePointerLeave)
})

onBeforeUnmount(() => {
  cancelAnimationFrame(animationFrame)
  window.removeEventListener('resize', handleResize)
  renderer?.domElement.removeEventListener('pointermove', handlePointerMove)
  renderer?.domElement.removeEventListener('pointerleave', handlePointerLeave)
  controls?.dispose()
  if (scene) disposeObject(scene)
  renderer?.dispose()
  renderer?.domElement.remove()
})
</script>

<style scoped>
.sensor-scene {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
  color: #d6e4f0;
  background: #0b1b2b;
  user-select: none;
}

.sensor-scene__canvas {
  width: 100%;
  height: 100%;
}

.sensor-scene__canvas :deep(canvas) {
  cursor: grab;
}

.sensor-scene__canvas :deep(canvas:active) {
  cursor: grabbing;
}

.control-panel,
.layout-guide,
.legend {
  position: absolute;
  border: 1px solid #2a4a6a;
  border-radius: 10px;
  background: rgb(16 36 58 / 92%);
  box-shadow: 0 14px 36px rgb(2 12 22 / 30%);
  backdrop-filter: blur(10px);
}

.control-panel {
  top: 16px;
  left: 16px;
  width: 282px;
  padding: 16px;
}

.control-panel h2 {
  margin: 0 0 14px;
  color: #7ec3ff;
  font-size: 16px;
}

.control-row {
  display: flex;
  min-height: 34px;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  font-size: 13px;
}

.control-row strong {
  color: #7ec3ff;
}

.time-select {
  width: 170px;
}

.layer-select {
  width: 112px;
}

.statistics {
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px solid #2a4a6a;
}

.statistics div {
  display: flex;
  justify-content: space-between;
  margin-top: 5px;
  color: #9fb8cc;
  font-size: 12px;
}

.statistics strong {
  color: #e3edf5;
}

.statistics .value-high {
  color: #ff7a59;
}

.statistics .value-low {
  color: #59b7ff;
}

.layout-guide {
  right: 20px;
  top: 18px;
  display: grid;
  width: 200px;
  padding: 12px 14px;
  gap: 3px;
}

.layout-guide span,
.layout-guide small {
  color: #8ea9bc;
  font-size: 11px;
}

.layout-guide strong {
  color: #7ec3ff;
  font-size: 18px;
}

.legend {
  right: 20px;
  bottom: 20px;
  padding: 11px 14px;
  font-size: 12px;
}

.legend__scale {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 6px;
}

.legend__scale i {
  width: 160px;
  height: 10px;
  border-radius: 999px;
}

.sensor-tooltip {
  position: absolute;
  z-index: 20;
  display: grid;
  padding: 8px 10px;
  pointer-events: none;
  border: 1px solid #3a6a9a;
  border-radius: 6px;
  background: rgb(10 22 38 / 96%);
  box-shadow: 0 8px 22px rgb(0 0 0 / 24%);
  font-size: 12px;
}

.sensor-tooltip span {
  color: #a8bdcc;
}

.scene-state {
  position: absolute;
  z-index: 15;
  inset: 0;
  display: grid;
  place-items: center;
  color: #9fc8e8;
  background: rgb(11 27 43 / 72%);
  font-size: 14px;
}

.scene-state--error {
  color: #ff8a70;
}

@media (max-width: 760px) {
  .control-panel {
    width: min(282px, calc(100vw - 32px));
  }

  .layout-guide {
    display: none;
  }
}
</style>
