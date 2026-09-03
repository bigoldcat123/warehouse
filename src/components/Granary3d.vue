<template>
  <div class="w-full h-full bg-[#0b1b2b] relative overflow-hidden select-none">
    <!-- three.js 画布 -->
    <div ref="containerRef" class="w-full h-full"></div>

    <!-- 控制面板 -->
    <div class="absolute top-4 left-4 w-[260px] rounded-lg bg-[#10243a]/90 border border-[#2a4a6a] text-[#d6e4f0] p-4 shadow-lg backdrop-blur">
      <div class="text-base font-bold mb-3 text-[#7ec3ff]">{{ cfg.title }}</div>

      <div class="space-y-2 text-sm">
        <div class="flex items-center justify-between">
          <span>粮仓编号</span>
          <span class="text-[#7ec3ff]">{{ houseNo || '--' }}</span>
        </div>
        <div class="flex items-center justify-between">
          <span>采集时间</span>
          <el-select v-model="currentTime" size="small" class="w-[150px]" placeholder="选择时间" @wheel.prevent="onTimeWheel" title="滚轮切换">
            <el-option v-for="t in store.times" :key="t" :label="formatTime(t)" :value="t" />
          </el-select>
        </div>
        <div class="flex items-center justify-between">
          <span>显示层</span>
          <el-select v-model="visibleLayer" size="small" class="w-[120px]" @wheel.prevent="onLayerWheel" title="滚轮切换">
            <el-option label="全部" value="all" />
            <el-option v-for="l in store.dimZ" :key="l" :label="`第 ${l} 层`" :value="l - 1" />
          </el-select>
        </div>
      </div>

      <div class="mt-3 pt-3 border-t border-[#2a4a6a] text-xs space-y-1 text-[#9fb8cc]">
        <div class="flex justify-between"><span>维度 X/Y/Z</span><span>{{ store.dimX }} × {{ store.dimY }} × {{ store.dimZ }}</span></div>
        <div class="flex justify-between"><span>测点总数</span><span>{{ stats.count }}</span></div>
        <div class="flex justify-between"><span>平均{{ cfg.label }}</span><span>{{ stats.avg }} {{ cfg.unit }}</span></div>
        <div class="flex justify-between"><span>最高{{ cfg.label }}</span><span class="text-[#ff7a59]">{{ stats.max }} {{ cfg.unit }}</span></div>
        <div class="flex justify-between"><span>最低{{ cfg.label }}</span><span class="text-[#59b7ff]">{{ stats.min }} {{ cfg.unit }}</span></div>
      </div>
    </div>

    <!-- 色标 -->
    <div class="absolute bottom-6 right-6 rounded-lg bg-[#10243a]/90 border border-[#2a4a6a] px-4 py-3 text-xs text-[#d6e4f0]">
      <div class="mb-1">{{ cfg.label }}({{ cfg.unit }})</div>
      <div class="flex items-center gap-2">
        <span>{{ cfg.min }}</span>
        <div class="w-[160px] h-[10px] rounded" :style="{ background: legendGradient }"></div>
        <span>{{ cfg.max }}</span>
      </div>
    </div>

    <!-- 悬浮提示 -->
    <div
      v-show="tooltip.visible"
      class="absolute pointer-events-none z-10 rounded bg-[#0a1626]/95 border border-[#3a6a9a] text-[#e8f2fb] text-xs px-3 py-2 shadow"
      :style="{ left: tooltip.x + 'px', top: tooltip.y + 'px' }"
    >
      <div>坐标：X{{ tooltip.px }} · Y{{ tooltip.py }} · Z{{ tooltip.pz }}</div>
      <div>{{ cfg.label }}：<span class="font-bold" :style="{ color: tooltip.color }">{{ tooltip.value }} {{ cfg.unit }}</span></div>
    </div>

    <!-- 加载/空状态提示 -->
    <div v-if="store.loading" class="absolute inset-0 z-10 flex items-center justify-center text-sm text-[#7ec3ff] bg-[#0b1b2b]/70">
      温度数据加载中...
    </div>
    <div v-else-if="!houseNo" class="absolute inset-0 z-10 flex items-center justify-center text-sm text-[#9fb8cc] bg-[#0b1b2b]/70">
      缺少仓房编号，无法加载温度数据
    </div>
    <div v-else-if="store.error" class="absolute inset-0 z-10 flex items-center justify-center text-sm text-[#ff7a59] bg-[#0b1b2b]/70">
      {{ store.error }}
    </div>
    <div v-else-if="!store.times.length" class="absolute inset-0 z-10 flex items-center justify-center text-sm text-[#9fb8cc] bg-[#0b1b2b]/70">
      暂无温度数据
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import { useCubeStore, CUBE_METRICS, type CubeKind, type SensorPoint } from '@/stores/cube'

const props = defineProps<{ houseNo: string; metric?: CubeKind }>()

const store = useCubeStore(props.metric ?? 'temperature')

const cfg = computed(() => CUBE_METRICS[props.metric ?? 'temperature'])
const ROOM_W = 16 // 仓房宽(x方向, 固定)
const ROOM_D = 16 // 仓房深(y方向, 固定)
const ROOM_H = 9 // 仓房高(z方向, 固定)
const BALL_R = 0.35 // 测点圆球半径(固定)
const TILE_OPACITY = 0.5 // 常态透明度
const cellGeo = new THREE.SphereGeometry(BALL_R, 24, 16)

const containerRef = ref<HTMLDivElement>()
const visibleLayer = ref<number | 'all'>('all')

const points = computed(() => store.points)

const currentTime = computed<string | undefined>({
  get: () => store.currentTime ?? undefined,
  set: (v) => { if (v) store.selectTime(v) },
})

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
  return valueColor((value - cfg.value.min) / (cfg.value.max - cfg.value.min))
}

/** 测点位置: 房间均分为 dimX×dimY×dimZ 个格子, 取格子中心, 点数变化时间距自适应 */
function pointPosition(p: SensorPoint): THREE.Vector3 {
  return new THREE.Vector3(
    -ROOM_W / 2 + ((p.x + 0.5) / Math.max(store.dimX, 1)) * ROOM_W,
    ((p.z + 0.5) / Math.max(store.dimZ, 1)) * ROOM_H,
    -ROOM_D / 2 + ((p.y + 0.5) / Math.max(store.dimY, 1)) * ROOM_D,
  )
}

/** 重建测点实例(按显示层过滤) */
function rebuildInstances() {
  if (!warehouseGroup) return
  // 层数变化后, 若已选层越界则回到全部
  if (typeof visibleLayer.value === 'number' && visibleLayer.value >= store.dimZ) {
    visibleLayer.value = 'all'
  }
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

  const w = ROOM_W
  const d = ROOM_D
  const h = ROOM_H
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
  controls.target.set(0, ROOM_H / 2, 0)

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
  rebuildWarehouse()

  animate()
}

function animate() {
  animationId = requestAnimationFrame(animate)
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

function formatTime(t: string) {
  return t.replace('T', ' ')
}
function onTimeWheel(e: WheelEvent) {
  const times = store.times
  if (!times.length) return
  const idx = times.indexOf(store.currentTime ?? '')
  const next = (idx + (e.deltaY > 0 ? 1 : -1) + times.length) % times.length
  store.selectTime(times[next])
}

function onLayerWheel(e: WheelEvent) {
  const opts: Array<number | 'all'> = ['all', ...Array.from({ length: store.dimZ }, (_, i) => i)]
  const idx = opts.indexOf(visibleLayer.value)
  const next = (idx + (e.deltaY > 0 ? 1 : -1) + opts.length) % opts.length
  visibleLayer.value = opts[next]
}

watch(points, rebuildInstances)
watch(visibleLayer, rebuildInstances)
watch(() => props.houseNo, (no) => store.fetchCube(no), { immediate: true })

onMounted(() => {
  initScene()
  rebuildInstances()
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
