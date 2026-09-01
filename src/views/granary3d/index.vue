<template>
  <div class="w-full h-screen bg-[#0b1b2b] relative overflow-hidden select-none">
    <!-- three.js 画布 -->
    <div ref="containerRef" class="w-full h-full"></div>

    <!-- 控制面板 -->
    <div class="absolute top-4 left-4 w-[260px] rounded-lg bg-[#10243a]/90 border border-[#2a4a6a] text-[#d6e4f0] p-4 shadow-lg backdrop-blur">
      <div class="text-base font-bold mb-3 text-[#7ec3ff]">3D粮仓温度展示</div>

      <div class="space-y-2 text-sm">
        <div class="flex items-center justify-between">
          <span>X 行数</span>
          <el-input-number v-model="dimX" :min="1" :max="20" size="small" />
        </div>
        <div class="flex items-center justify-between">
          <span>Y 列数</span>
          <el-input-number v-model="dimY" :min="1" :max="20" size="small" />
        </div>
        <div class="flex items-center justify-between">
          <span>Z 层数</span>
          <el-input-number v-model="dimZ" :min="1" :max="10" size="small" />
        </div>
        <div class="flex items-center justify-between">
          <span>自动旋转</span>
          <el-switch v-model="autoRotate" />
        </div>
        <div class="flex items-center justify-between">
          <span>显示层</span>
          <el-select v-model="visibleLayer" size="small" class="w-[120px]">
            <el-option label="全部" value="all" />
            <el-option v-for="l in dimZ" :key="l" :label="`第 ${l} 层`" :value="l - 1" />
          </el-select>
        </div>
      </div>

      <el-button type="primary" class="w-full mt-3" @click="regenerate">生成模拟数据</el-button>

      <div class="mt-3 pt-3 border-t border-[#2a4a6a] text-xs space-y-1 text-[#9fb8cc]">
        <div class="flex justify-between"><span>测点总数</span><span>{{ stats.count }}</span></div>
        <div class="flex justify-between"><span>平均温度</span><span>{{ stats.avg }} ℃</span></div>
        <div class="flex justify-between"><span>最高温度</span><span class="text-[#ff7a59]">{{ stats.max }} ℃</span></div>
        <div class="flex justify-between"><span>最低温度</span><span class="text-[#59b7ff]">{{ stats.min }} ℃</span></div>
      </div>
    </div>

    <!-- 色标 -->
    <div class="absolute bottom-6 right-6 rounded-lg bg-[#10243a]/90 border border-[#2a4a6a] px-4 py-3 text-xs text-[#d6e4f0]">
      <div class="mb-1">温度(℃)</div>
      <div class="flex items-center gap-2">
        <span>{{ TEMP_MIN }}</span>
        <div class="w-[160px] h-[10px] rounded" :style="{ background: legendGradient }"></div>
        <span>{{ TEMP_MAX }}</span>
      </div>
    </div>

    <!-- 悬浮提示 -->
    <div
      v-show="tooltip.visible"
      class="absolute pointer-events-none z-10 rounded bg-[#0a1626]/95 border border-[#3a6a9a] text-[#e8f2fb] text-xs px-3 py-2 shadow"
      :style="{ left: tooltip.x + 'px', top: tooltip.y + 'px' }"
    >
      <div>坐标：X{{ tooltip.px }} · Y{{ tooltip.py }} · Z{{ tooltip.pz }}</div>
      <div>温度：<span class="font-bold" :style="{ color: tooltip.color }">{{ tooltip.value }} ℃</span></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'

/** 单个测点 */
interface SensorPoint {
  x: number // 行(0-based)
  y: number // 列(0-based)
  z: number // 层(0-based)
  value: number
}

const TEMP_MIN = 10
const TEMP_MAX = 40
const GAP = 2 // 测点间距
const LAYER_GAP = 0.8 // 层与层之间的间隔
const BALL_R = GAP * 0.24 // 测点圆球半径
const TILE_OPACITY = 0.5 // 常态透明度
const cellGeo = new THREE.SphereGeometry(BALL_R, 24, 16)

const containerRef = ref<HTMLDivElement>()
const dimX = ref(6)
const dimY = ref(6)
const dimZ = ref(4)
const autoRotate = ref(true)
const visibleLayer = ref<number | 'all'>('all')

const points = ref<SensorPoint[]>([])
const tooltip = reactive({
  visible: false,
  x: 0,
  y: 0,
  px: 0,
  py: 0,
  pz: 0,
  value: '0',
  color: '#fff',
})

const stats = computed(() => {
  if (!points.value.length) return { count: 0, avg: '0', max: '0', min: '0' }
  let sum = 0
  let max = -Infinity
  let min = Infinity
  for (const p of points.value) {
    sum += p.value
    if (p.value > max) max = p.value
    if (p.value < min) min = p.value
  }
  return {
    count: points.value.length,
    avg: (sum / points.value.length).toFixed(1),
    max: max.toFixed(1),
    min: min.toFixed(1),
  }
})

const legendGradient = computed(() => {
  const stops = [0, 0.25, 0.5, 0.75, 1].map((t) => `#${valueColor(t).getHexString()} ${t * 100}%`)
  return `linear-gradient(to right, ${stops.join(', ')})`
})

// ---------- three.js ----------
let renderer: THREE.WebGLRenderer
let scene: THREE.Scene
let camera: THREE.PerspectiveCamera
let controls: OrbitControls
let instancedMesh: THREE.InstancedMesh | null = null
let highlightMesh: THREE.Mesh | null = null
let warehouseGroup: THREE.Group | null = null
let animationId = 0
const raycaster = new THREE.Raycaster()
const pointer = new THREE.Vector2()
const dummy = new THREE.Object3D()

/** 温度值 -> 颜色, t∈[0,1]: 蓝->青->绿->黄->红 */
function valueColor(t: number): THREE.Color {
  const clamped = Math.min(1, Math.max(0, t))
  const stops: Array<[number, THREE.Color]> = [
    [0, new THREE.Color('#2b6cff')],
    [0.25, new THREE.Color('#22c3d6')],
    [0.5, new THREE.Color('#3ecf5a')],
    [0.75, new THREE.Color('#f2c531')],
    [1, new THREE.Color('#f0433a')],
  ]
  for (let i = 0; i < stops.length - 1; i++) {
    const [t0, c0] = stops[i]
    const [t1, c1] = stops[i + 1]
    if (clamped <= t1) {
      const k = (clamped - t0) / (t1 - t0)
      return c0.clone().lerp(c1, k)
    }
  }
  return stops[stops.length - 1][1].clone()
}

function colorOf(value: number): THREE.Color {
  return valueColor((value - TEMP_MIN) / (TEMP_MAX - TEMP_MIN))
}

/** 生成 mock 数据: 基础温度 + 随机热点 */
function generateMockData(): SensorPoint[] {
  const list: SensorPoint[] = []
  const hotspots: Array<[number, number, number]> = []
  const hotCount = Math.max(1, Math.floor((dimX.value * dimY.value * dimZ.value) / 60))
  for (let i = 0; i < hotCount; i++) {
    hotspots.push([
      Math.floor(Math.random() * dimX.value),
      Math.floor(Math.random() * dimY.value),
      Math.floor(Math.random() * dimZ.value),
    ])
  }
  for (let x = 0; x < dimX.value; x++) {
    for (let y = 0; y < dimY.value; y++) {
      for (let z = 0; z < dimZ.value; z++) {
        // 上层温度略高, 叠加随机波动
        let v = 18 + z * 1.5 + Math.random() * 6
        // 靠近热点升温
        for (const [hx, hy, hz] of hotspots) {
          const d = Math.hypot(x - hx, y - hy, z - hz)
          if (d < 2.5) v += (2.5 - d) * 6
        }
        list.push({ x, y, z, value: Math.round(v * 10) / 10 })
      }
    }
  }
  return list
}

function pointPosition(p: SensorPoint): THREE.Vector3 {
  return new THREE.Vector3(
    (p.x - (dimX.value - 1) / 2) * GAP,
    layerY(p.z) + BALL_R + 0.02,
    (p.y - (dimY.value - 1) / 2) * GAP,
  )
}
/** 第 z 层的底部高度 */
function layerY(z: number): number {
  return z * (GAP + LAYER_GAP)
}

/** 粮堆总高(含层间隔) */
function warehouseHeight(): number {
  return layerY(dimZ.value - 1) + BALL_R * 2 + 0.04
}

/** 重建测点实例(按显示层过滤) */
function rebuildInstances() {
  if (!warehouseGroup) return
  if (instancedMesh) {
    warehouseGroup.remove(instancedMesh)
    ;(instancedMesh.material as THREE.Material).dispose()
  }

  const visible = points.value.filter((p) => visibleLayer.value === 'all' || p.z === visibleLayer.value)
  const mesh = new THREE.InstancedMesh(
    cellGeo,
    new THREE.MeshStandardMaterial({
      transparent: true,
      opacity: TILE_OPACITY,
      roughness: 0.3,
      metalness: 0.05,
      depthWrite: false,
    }),
    Math.max(visible.length, 1),
  )
  if (highlightMesh) highlightMesh.visible = false
  tooltip.visible = false
  visible.forEach((p, i) => {
    dummy.position.copy(pointPosition(p))
    dummy.updateMatrix()
    mesh.setMatrixAt(i, dummy.matrix)
    mesh.setColorAt(i, colorOf(p.value))
  })
  mesh.instanceMatrix.needsUpdate = true
  if (mesh.instanceColor) mesh.instanceColor.needsUpdate = true
  mesh.userData.points = visible
  warehouseGroup.add(mesh)
  instancedMesh = mesh
}

/** 重建仓体轮廓/粮堆, 并刷新测点 */
function rebuildWarehouse() {
  if (!scene) return
  if (warehouseGroup) {
    scene.remove(warehouseGroup)
    warehouseGroup.traverse((obj) => {
      const o = obj as THREE.Mesh
      if (o.geometry) o.geometry.dispose()
      const m = o.material as THREE.Material | THREE.Material[] | undefined
      if (m) (Array.isArray(m) ? m : [m]).forEach((mm) => mm.dispose())
    })
  }
  warehouseGroup = new THREE.Group()
  scene.add(warehouseGroup)

  const w = dimX.value * GAP
  const d = dimY.value * GAP
  const h = warehouseHeight()
  const lineMat = new THREE.LineBasicMaterial({ color: 0x5a8ab8 })

  // 地坪
  const floor = new THREE.Mesh(
    new THREE.BoxGeometry(w + 2.5, 0.3, d + 2.5),
    new THREE.MeshStandardMaterial({ color: 0x1c3247, roughness: 0.9 }),
  )
  floor.position.y = -0.15
  warehouseGroup.add(floor)

  // 仓房线框
  const boxGeo = new THREE.BoxGeometry(w + 1, h + 1, d + 1)
  const edges = new THREE.LineSegments(new THREE.EdgesGeometry(boxGeo), lineMat)
  edges.position.y = h / 2
  warehouseGroup.add(edges)
  boxGeo.dispose()

  // 人字形屋顶
  const roofH = Math.min(2.5, Math.max(1.2, d * 0.25))
  const hw = (w + 1) / 2
  const hd = (d + 1) / 2
  const y0 = h + 0.5
  const ridge = y0 + roofH
  const roofPts = new Float32Array([
    // 屋脊
    -hw, ridge, 0, hw, ridge, 0,
    // 四条坡檐
    -hw, y0, -hd, -hw, ridge, 0,
    -hw, y0, hd, -hw, ridge, 0,
    hw, y0, -hd, hw, ridge, 0,
    hw, y0, hd, hw, ridge, 0,
  ])
  const roofGeo = new THREE.BufferGeometry()
  roofGeo.setAttribute('position', new THREE.BufferAttribute(roofPts, 3))
  warehouseGroup.add(new THREE.LineSegments(roofGeo, lineMat))

  rebuildInstances()
}

function regenerate() {
  points.value = generateMockData()
  if (visibleLayer.value !== 'all' && (visibleLayer.value as number) >= dimZ.value) {
    visibleLayer.value = 'all'
  }
  rebuildWarehouse()
  controls?.target.set(0, (warehouseHeight()) / 2, 0)
}

function initScene() {
  const el = containerRef.value!
  renderer = new THREE.WebGLRenderer({ antialias: true })
  renderer.setPixelRatio(window.devicePixelRatio)
  renderer.setSize(el.clientWidth, el.clientHeight)
  el.appendChild(renderer.domElement)

  scene = new THREE.Scene()
  scene.background = new THREE.Color('#0b1b2b')
  scene.fog = new THREE.Fog('#0b1b2b', 60, 140)

  camera = new THREE.PerspectiveCamera(50, el.clientWidth / el.clientHeight, 0.1, 500)
  camera.position.set(16, 14, 18)

  controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true
  controls.dampingFactor = 0.08
  controls.maxDistance = 120
  controls.target.set(0, 3, 0)

  scene.add(new THREE.AmbientLight(0xffffff, 0.45))
  const dir = new THREE.DirectionalLight(0xfff2dd, 1.1)
  const hemi = new THREE.HemisphereLight(0xbcd8ff, 0x16283a, 0.6)
  scene.add(hemi)
  dir.position.set(20, 30, 15)
  scene.add(dir)

  const grid = new THREE.GridHelper(60, 30, 0x2a4a6a, 0x1a3048)
  grid.position.y = -0.01
  scene.add(grid)
  // 悬停高亮块(不透明, 盖在半透明测点上)
  highlightMesh = new THREE.Mesh(
    cellGeo,
    new THREE.MeshStandardMaterial({ roughness: 0.25, metalness: 0.05 }),
  )
  highlightMesh.scale.setScalar(1.06)
  highlightMesh.visible = false
  scene.add(highlightMesh)

  animate()
}

function animate() {
  animationId = requestAnimationFrame(animate)
  controls.autoRotate = autoRotate.value
  controls.autoRotateSpeed = 1.2
  controls.update()
  renderer.render(scene, camera)
}

function onResize() {
  const el = containerRef.value
  if (!el || !renderer || !camera) return
  camera.aspect = el.clientWidth / el.clientHeight
  camera.updateProjectionMatrix()
  renderer.setSize(el.clientWidth, el.clientHeight)
}

function onPointerMove(e: PointerEvent) {
  const el = containerRef.value
  if (!el || !instancedMesh) return
  const rect = el.getBoundingClientRect()
  pointer.x = ((e.clientX - rect.left) / rect.width) * 2 - 1
  pointer.y = -((e.clientY - rect.top) / rect.height) * 2 + 1
  raycaster.setFromCamera(pointer, camera)
  const hit = raycaster.intersectObject(instancedMesh)[0]
  if (hit && hit.instanceId !== undefined) {
    const p = (instancedMesh.userData.points as SensorPoint[])[hit.instanceId]
    if (p) {
      tooltip.visible = true
      tooltip.x = e.clientX - rect.left + 14
      tooltip.y = e.clientY - rect.top + 14
      tooltip.px = p.x + 1
      tooltip.py = p.y + 1
      tooltip.pz = p.z + 1
      tooltip.value = p.value.toFixed(1)
      tooltip.color = `#${colorOf(p.value).getHexString()}`
      if (highlightMesh) {
        const c = colorOf(p.value)
        const m = highlightMesh.material as THREE.MeshStandardMaterial
        m.color.copy(c)
        m.emissive.copy(c)
        m.emissiveIntensity = 0.4
        highlightMesh.position.copy(pointPosition(p))
        highlightMesh.visible = true
      }
      return
    }
  }
  tooltip.visible = false
  if (highlightMesh) highlightMesh.visible = false
}

watch([dimX, dimY, dimZ], regenerate)
watch(visibleLayer, rebuildInstances)

onMounted(() => {
  initScene()
  regenerate()
  window.addEventListener('resize', onResize)
  containerRef.value!.addEventListener('pointermove', onPointerMove)
  containerRef.value!.addEventListener('pointerleave', () => (tooltip.visible = false))
})

onBeforeUnmount(() => {
  cancelAnimationFrame(animationId)
  window.removeEventListener('resize', onResize)
  controls?.dispose()
  renderer?.dispose()
  renderer?.domElement.remove()
})
</script>
