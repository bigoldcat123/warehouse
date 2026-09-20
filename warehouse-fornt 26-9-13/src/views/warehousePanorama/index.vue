<template>
  <main class="panorama-page">
    <div ref="sceneContainer" class="scene-container"></div>
    <header class="page-heading">
      <p>WAREHOUSE PANORAMA</p>
      <h1>粮库全景图</h1>
      <span>第四区域 · 凹形建筑与 10 个小圆柱仓</span>
    </header>
  </main>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'

const sceneContainer = ref<HTMLDivElement>()

let renderer: THREE.WebGLRenderer | undefined
let scene: THREE.Scene | undefined
let camera: THREE.PerspectiveCamera | undefined
let controls: OrbitControls | undefined
let animationFrame = 0

const SITE_WIDTH = 116
const SITE_DEPTH = 64
const HOUSE_WIDTH = 7
const HOUSE_DEPTH = 7.5
const HOUSE_HEIGHT = 3.8
const SILO_DIAMETER = 5.4
const SILO_HEIGHT = 7
const GROUPS = [4, 2, 2, 2, 2]
const HOUSE_GAP = 1.15
const COMPACT_HOUSE_GAP = 0.7
const GROUP_GAP = 3.8

function createRoundedRoof(
  width: number,
  depth: number,
  rise: number,
  material: THREE.Material,
) {
  const radius = depth / 2 + 0.3
  const geometry = new THREE.CylinderGeometry(
    radius,
    radius,
    width + 0.6,
    32,
    1,
    false,
    0,
    Math.PI,
  )
  geometry.rotateZ(Math.PI / 2)
  const roof = new THREE.Mesh(geometry, material)
  roof.scale.y = rise / radius
  roof.castShadow = true
  return roof
}

function createHouse(index: number) {
  const house = new THREE.Group()
  const wallMaterial = new THREE.MeshStandardMaterial({
    color: index < 4 ? '#d8e2e8' : '#cbd9df',
    roughness: 0.72,
  })
  const trimMaterial = new THREE.MeshStandardMaterial({ color: '#78909c', roughness: 0.6 })
  const roofMaterial = new THREE.MeshStandardMaterial({ color: '#436578', roughness: 0.78 })

  const body = new THREE.Mesh(
    new THREE.BoxGeometry(HOUSE_WIDTH, HOUSE_HEIGHT, HOUSE_DEPTH),
    wallMaterial,
  )
  body.position.y = HOUSE_HEIGHT / 2
  body.castShadow = true
  body.receiveShadow = true
  house.add(body)

  const roof = createRoundedRoof(HOUSE_WIDTH, HOUSE_DEPTH, 0.38, roofMaterial)
  roof.position.y = HOUSE_HEIGHT
  house.add(roof)

  const door = new THREE.Mesh(new THREE.BoxGeometry(1.65, 2.3, 0.16), trimMaterial)
  door.position.set(0, 1.15, HOUSE_DEPTH / 2 + 0.09)
  house.add(door)

  const lintel = new THREE.Mesh(new THREE.BoxGeometry(2.2, 0.26, 0.2), trimMaterial)
  lintel.position.set(0, 2.45, HOUSE_DEPTH / 2 + 0.11)
  house.add(lintel)

  return house
}

function createCombinedHouse(count: number, startNumber: number) {
  const house = new THREE.Group()
  const totalWidth = count * HOUSE_WIDTH + (count - 1) * HOUSE_GAP
  const bayWidth = totalWidth / count
  const wallMaterial = new THREE.MeshStandardMaterial({ color: '#cbd9df', roughness: 0.72 })
  const trimMaterial = new THREE.MeshStandardMaterial({ color: '#6f858f', roughness: 0.6 })
  const roofMaterial = new THREE.MeshStandardMaterial({ color: '#436578', roughness: 0.78 })

  const body = new THREE.Mesh(
    new THREE.BoxGeometry(totalWidth, HOUSE_HEIGHT, HOUSE_DEPTH),
    wallMaterial,
  )
  body.position.y = HOUSE_HEIGHT / 2
  body.castShadow = true
  body.receiveShadow = true
  house.add(body)

  const roof = createRoundedRoof(totalWidth, HOUSE_DEPTH, 0.38, roofMaterial)
  roof.position.y = HOUSE_HEIGHT
  house.add(roof)

  for (let bay = 0; bay < count; bay += 1) {
    const centerX = -totalWidth / 2 + bayWidth * (bay + 0.5)
    const door = new THREE.Mesh(new THREE.BoxGeometry(1.65, 2.3, 0.16), trimMaterial)
    door.position.set(centerX, 1.15, HOUSE_DEPTH / 2 + 0.09)
    door.userData.houseNumber = startNumber + bay
    house.add(door)

    const lintel = new THREE.Mesh(new THREE.BoxGeometry(2.2, 0.26, 0.2), trimMaterial)
    lintel.position.set(centerX, 2.45, HOUSE_DEPTH / 2 + 0.11)
    house.add(lintel)

    if (bay < count - 1) {
      const divider = new THREE.Mesh(
        new THREE.BoxGeometry(0.12, HOUSE_HEIGHT - 0.2, 0.12),
        trimMaterial,
      )
      divider.position.set(-totalWidth / 2 + bayWidth * (bay + 1), HOUSE_HEIGHT / 2, HOUSE_DEPTH / 2 + 0.08)
      house.add(divider)
    }
  }

  house.userData.houseNumbers = Array.from({ length: count }, (_, index) => startNumber + index)
  return house
}

function createSilo() {
  const silo = new THREE.Group()
  const wallMaterial = new THREE.MeshStandardMaterial({ color: '#bdcdd0', roughness: 0.66 })
  const roofMaterial = new THREE.MeshStandardMaterial({ color: '#55727d', roughness: 0.74 })
  const doorMaterial = new THREE.MeshStandardMaterial({ color: '#71868d', roughness: 0.62 })
  const radius = SILO_DIAMETER / 2

  const body = new THREE.Mesh(
    new THREE.CylinderGeometry(radius, radius, SILO_HEIGHT, 32),
    wallMaterial,
  )
  body.position.y = SILO_HEIGHT / 2
  body.castShadow = true
  body.receiveShadow = true
  silo.add(body)

  const roof = new THREE.Mesh(new THREE.CylinderGeometry(radius + 0.18, radius + 0.18, 0.28, 32), roofMaterial)
  roof.position.y = SILO_HEIGHT + 0.14
  roof.castShadow = true
  silo.add(roof)

  const door = new THREE.Mesh(new THREE.BoxGeometry(1.25, 2.2, 0.14), doorMaterial)
  door.position.set(0, 1.1, radius + 0.06)
  silo.add(door)

  return silo
}

function createSmallSilo() {
  const silo = new THREE.Group()
  const radius = 1.15
  const height = 3.3
  const wallMaterial = new THREE.MeshStandardMaterial({ color: '#d5ddd7', roughness: 0.68 })
  const roofMaterial = new THREE.MeshStandardMaterial({ color: '#6c8182', roughness: 0.76 })

  const body = new THREE.Mesh(new THREE.CylinderGeometry(radius, radius, height, 24), wallMaterial)
  body.position.y = height / 2
  body.castShadow = true
  body.receiveShadow = true
  silo.add(body)

  const roof = new THREE.Mesh(new THREE.CylinderGeometry(radius + 0.1, radius + 0.1, 0.18, 24), roofMaterial)
  roof.position.y = height + 0.09
  roof.castShadow = true
  silo.add(roof)

  return silo
}

function createUShapedHouse(outerWidth: number) {
  const building = new THREE.Group()
  const outerDepth = 14
  const sectionWidth = outerWidth / 3
  const recessDepth = outerDepth / 2
  const backDepth = outerDepth - recessDepth
  const height = 2.2
  const wallMaterial = new THREE.MeshStandardMaterial({ color: '#c8d3d0', roughness: 0.76 })
  const roofMaterial = new THREE.MeshStandardMaterial({ color: '#465f68', roughness: 0.74 })

  const addSection = (width: number, depth: number, x: number, z: number) => {
    const walls = new THREE.Mesh(new THREE.BoxGeometry(width, height, depth), wallMaterial)
    walls.position.set(x, height / 2, z)
    walls.castShadow = true
    walls.receiveShadow = true
    building.add(walls)

    const roof = createRoundedRoof(width, depth, 0.16, roofMaterial)
    roof.position.set(x, height, z)
    building.add(roof)
  }

  // 左侧实体、中央凹槽、右侧实体宽度为 1:1:1，凹槽深度为建筑纵深的一半。
  addSection(outerWidth, backDepth, 0, outerDepth / 2 - backDepth / 2)
  addSection(sectionWidth, recessDepth, -outerWidth / 2 + sectionWidth / 2, -outerDepth / 2 + recessDepth / 2)
  addSection(sectionWidth, recessDepth, outerWidth / 2 - sectionWidth / 2, -outerDepth / 2 + recessDepth / 2)

  return building
}

function addWarehouseRow(
  z: number,
  startNumber: number,
  combineFromGroup = 3,
  compactGroups: number[] = [],
) {
  if (!scene) return

  const houses = new THREE.Group()
  const totalWidth = GROUPS.reduce((total, count, groupIndex) => {
    const internalGap = compactGroups.includes(groupIndex) ? COMPACT_HOUSE_GAP : HOUSE_GAP
    return total + count * HOUSE_WIDTH + (count - 1) * internalGap
  }, 0)
    + (GROUPS.length - 1) * GROUP_GAP
  let cursor = -totalWidth / 2
  let houseNumber = startNumber

  GROUPS.forEach((count, groupIndex) => {
    if (groupIndex >= combineFromGroup) {
      const combinedWidth = count * HOUSE_WIDTH + (count - 1) * HOUSE_GAP
      const combinedHouse = createCombinedHouse(count, houseNumber)
      combinedHouse.position.set(cursor + combinedWidth / 2, 0.18, z)
      houses.add(combinedHouse)
      cursor += combinedWidth
      houseNumber += count
      if (groupIndex < GROUPS.length - 1) cursor += GROUP_GAP
      return
    }

    const internalGap = compactGroups.includes(groupIndex) ? COMPACT_HOUSE_GAP : HOUSE_GAP
    for (let i = 0; i < count; i += 1) {
      const house = createHouse(houseNumber - startNumber)
      house.position.set(cursor + HOUSE_WIDTH / 2, 0.18, z)
      house.userData.houseNumber = houseNumber
      houses.add(house)
      cursor += HOUSE_WIDTH
      houseNumber += 1
      if (i < count - 1) cursor += internalGap
    }
    if (groupIndex < GROUPS.length - 1) cursor += GROUP_GAP
  })

  scene.add(houses)
}

function addWarehouseAreas() {
  addWarehouseRow(-22, 1, 3, [1, 2])
  addWarehouseRow(-6, 13, 2, [1])
  addThirdArea()
  addFourthArea()
}

function addThirdArea() {
  if (!scene) return

  const area = new THREE.Group()
  const firstGroupWidth = HOUSE_WIDTH * 4 + HOUSE_GAP * 3
  const cylinderPairWidth = SILO_DIAMETER * 2 + HOUSE_GAP
  const squarePairWidth = HOUSE_WIDTH * 2 + HOUSE_GAP
  const bridgeGap = firstGroupWidth - cylinderPairWidth - squarePairWidth
  const eightSilosWidth = SILO_DIAMETER * 8 + HOUSE_GAP * 7
  const totalWidth = firstGroupWidth + GROUP_GAP + eightSilosWidth + GROUP_GAP + squarePairWidth
  let cursor = -totalWidth / 2
  let houseNumber = 25

  // 第三行第一段：四个圆柱仓排成 2×2，并与两个方仓共同对齐上方前四仓的宽度。
  for (let column = 0; column < 2; column += 1) {
    for (let row = 0; row < 2; row += 1) {
      const silo = createSilo()
      silo.position.set(
        cursor + SILO_DIAMETER / 2 + column * (SILO_DIAMETER + HOUSE_GAP),
        0.18,
        row === 0 ? 4.5 : 11.5,
      )
      silo.userData.houseNumber = houseNumber
      area.add(silo)
      houseNumber += 1
    }
  }

  cursor += cylinderPairWidth + bridgeGap
  for (let i = 0; i < 2; i += 1) {
    const house = createHouse(4 + i)
    house.position.set(cursor + HOUSE_WIDTH / 2, 0.18, 8)
    house.userData.houseNumber = houseNumber
    area.add(house)
    cursor += HOUSE_WIDTH + (i === 0 ? HOUSE_GAP : 0)
    houseNumber += 1
  }

  // 第二段：八个圆柱仓单排。
  cursor += GROUP_GAP
  for (let i = 0; i < 8; i += 1) {
    const silo = createSilo()
    silo.position.set(cursor + SILO_DIAMETER / 2, 0.18, 8)
    silo.userData.houseNumber = houseNumber
    area.add(silo)
    cursor += SILO_DIAMETER + (i < 7 ? HOUSE_GAP : 0)
    houseNumber += 1
  }

  // 第三段：两个方仓。
  cursor += GROUP_GAP
  const combinedHouse = createCombinedHouse(2, houseNumber)
  combinedHouse.position.set(cursor + squarePairWidth / 2, 0.18, 8)
  area.add(combinedHouse)

  scene.add(area)
}

function addFourthArea() {
  if (!scene) return

  const area = new THREE.Group()
  const standardRowWidth = GROUPS.reduce((total, count) => total + count * HOUSE_WIDTH, 0)
    + (GROUPS.reduce((total, count) => total + count, 0) - GROUPS.length) * HOUSE_GAP
    + (GROUPS.length - 1) * GROUP_GAP
  const emptyThreeHousesWidth = HOUSE_WIDTH * 3 + HOUSE_GAP * 2
  const buildingWidth = standardRowWidth - emptyThreeHousesWidth
  const buildingCenterX = -standardRowWidth / 2 + emptyThreeHousesWidth + buildingWidth / 2
  const buildingCenterZ = 24

  const uHouse = createUShapedHouse(buildingWidth)
  uHouse.position.set(buildingCenterX, 0.18, buildingCenterZ)
  area.add(uHouse)

  const siloDiameter = 2.3
  const columns = 10
  const recessWidth = buildingWidth / 3
  const rowWidth = recessWidth
  const siloGapX = (rowWidth - columns * siloDiameter) / (columns - 1)
  const startX = buildingCenterX - rowWidth / 2 + siloDiameter / 2

  for (let column = 0; column < columns; column += 1) {
    const silo = createSmallSilo()
    silo.position.set(
      startX + column * (siloDiameter + siloGapX),
      0.2,
      buildingCenterZ - 3.5,
    )
    silo.userData.houseNumber = 41 + column
    area.add(silo)
  }

  scene.add(area)
}

function addSite() {
  if (!scene) return

  const ground = new THREE.Mesh(
    new THREE.BoxGeometry(SITE_WIDTH, 0.35, SITE_DEPTH),
    new THREE.MeshStandardMaterial({ color: '#70806f', roughness: 0.96 }),
  )
  ground.position.y = -0.18
  ground.receiveShadow = true
  scene.add(ground)

  const border = new THREE.LineSegments(
    new THREE.EdgesGeometry(new THREE.BoxGeometry(SITE_WIDTH, 0.38, SITE_DEPTH)),
    new THREE.LineBasicMaterial({ color: '#b8c4b3' }),
  )
  border.position.y = -0.16
  scene.add(border)

  const dividerMaterial = new THREE.MeshBasicMaterial({ color: '#aab7a5' })
  for (let area = 1; area < 4; area += 1) {
    const divider = new THREE.Mesh(new THREE.PlaneGeometry(SITE_WIDTH - 3, 0.18), dividerMaterial)
    divider.rotation.x = -Math.PI / 2
    divider.position.set(0, 0.015, -SITE_DEPTH / 2 + (SITE_DEPTH / 4) * area)
    scene.add(divider)
  }
}

function initScene() {
  const container = sceneContainer.value
  if (!container) return

  scene = new THREE.Scene()
  scene.background = new THREE.Color('#9fb4be')
  scene.fog = new THREE.Fog('#9fb4be', 105, 185)

  camera = new THREE.PerspectiveCamera(42, container.clientWidth / container.clientHeight, 0.1, 300)
  camera.position.set(68, 58, 70)

  renderer = new THREE.WebGLRenderer({ antialias: true })
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.setSize(container.clientWidth, container.clientHeight)
  renderer.shadowMap.enabled = true
  renderer.shadowMap.type = THREE.PCFSoftShadowMap
  renderer.outputColorSpace = THREE.SRGBColorSpace
  container.appendChild(renderer.domElement)

  controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true
  controls.target.set(0, 0, -4)
  controls.minDistance = 45
  controls.maxDistance = 155
  controls.maxPolarAngle = Math.PI / 2.05

  scene.add(new THREE.HemisphereLight('#e7f2f5', '#465647', 2.4))
  const sunlight = new THREE.DirectionalLight('#fff4dc', 3.2)
  sunlight.position.set(-35, 55, 35)
  sunlight.castShadow = true
  sunlight.shadow.mapSize.set(2048, 2048)
  sunlight.shadow.camera.left = -70
  sunlight.shadow.camera.right = 70
  sunlight.shadow.camera.top = 55
  sunlight.shadow.camera.bottom = -55
  scene.add(sunlight)

  addSite()
  addWarehouseAreas()

  const animate = () => {
    animationFrame = requestAnimationFrame(animate)
    controls?.update()
    if (renderer && scene && camera) renderer.render(scene, camera)
  }
  animate()
}

function handleResize() {
  const container = sceneContainer.value
  if (!container || !renderer || !camera) return
  camera.aspect = container.clientWidth / container.clientHeight
  camera.updateProjectionMatrix()
  renderer.setSize(container.clientWidth, container.clientHeight)
}

onMounted(() => {
  initScene()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  cancelAnimationFrame(animationFrame)
  window.removeEventListener('resize', handleResize)
  controls?.dispose()
  scene?.traverse((object) => {
    if (!(object instanceof THREE.Mesh)) return
    object.geometry.dispose()
    const materials = Array.isArray(object.material) ? object.material : [object.material]
    materials.forEach((material) => material.dispose())
  })
  renderer?.dispose()
  renderer?.domElement.remove()
})
</script>

<style scoped>
.panorama-page {
  position: relative;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background: #9fb4be;
}

.scene-container {
  width: 100%;
  height: 100%;
}

.page-heading {
  position: absolute;
  top: 28px;
  left: 34px;
  padding: 14px 18px;
  color: #f5f8f6;
  background: rgb(30 50 56 / 76%);
  border-left: 3px solid #d9b66f;
  pointer-events: none;
  backdrop-filter: blur(8px);
}

.page-heading p,
.page-heading h1 {
  margin: 0;
}

.page-heading p {
  color: #d9b66f;
  font-size: 10px;
  letter-spacing: 0.2em;
}

.page-heading h1 {
  margin-top: 4px;
  font-size: 24px;
  font-weight: 600;
  letter-spacing: 0.08em;
}

.page-heading span {
  display: block;
  margin-top: 4px;
  color: #cfdbd7;
  font-size: 12px;
}
</style>
