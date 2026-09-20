<template>
  <main class="panorama-page">
    <div ref="sceneContainer" class="scene-container"></div>
    <header class="page-heading">
      <p>WAREHOUSE PANORAMA</p>
      <h1>粮库全景图</h1>
      <span>第四区域 · 凹形建筑与 10 个小圆柱仓</span>
    </header>

    <aside v-if="selectedHouse" class="house-panel">
      <div class="house-panel__header">
        <div>
          <p>SELECTED WAREHOUSE</p>
          <h2>仓房信息</h2>
        </div>
        <div class="house-panel__tools">
          <span class="status-dot">UI 示例数据</span>
          <button
            type="button"
            class="house-panel__close"
            aria-label="关闭仓房信息"
            title="关闭"
            @click="selectedHouse = undefined"
          >×</button>
        </div>
      </div>

      <dl class="house-details">
        <div class="house-details__wide">
          <dt>仓房编号</dt>
          <dd>{{ selectedHouse.houseNo }}</dd>
        </div>
        <div>
          <dt>熏蒸状态</dt>
          <dd class="status-safe">未熏蒸</dd>
        </div>
        <div>
          <dt>仓间氧浓度</dt>
          <dd>20.8%</dd>
        </div>
        <div>
          <dt>仓间 CQ2 浓度</dt>
          <dd>420 ppm</dd>
        </div>
        <div>
          <dt>仓间 PH3 浓度</dt>
          <dd>0.00 ppm</dd>
        </div>
        <div class="house-details__wide">
          <dt>采集时间</dt>
          <dd>2026-09-20 10:00:00</dd>
        </div>
      </dl>

      <div class="house-actions">
        <button type="button" @click="openHouseView('/granary3d')">查看温度图</button>
        <button type="button" @click="openHouseView('/humidity3d')">查看湿度图</button>
        <button type="button" @click="openHouseView('/gas3d')">查看气体浓度图</button>
        <button type="button" @click="openHouseView('/data')">查看数据</button>
      </div>
    </aside>

    <div v-else class="select-hint">点击仓房查看信息</div>
  </main>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'

const sceneContainer = ref<HTMLDivElement>()
const router = useRouter()
const configuredHouseNumbers = (import.meta.env.ENV_PANORAMA_HOUSE_NUMBERS || '')
  .split(',')
  .map((value) => value.trim())
  .filter(Boolean)
const selectedHouse = ref<{ modelIndex: number; houseNo: string }>()
const raycaster = new THREE.Raycaster()
const pointer = new THREE.Vector2()
let pointerStart = { x: 0, y: 0 }

let renderer: THREE.WebGLRenderer | undefined
let scene: THREE.Scene | undefined
let camera: THREE.PerspectiveCamera | undefined
let controls: OrbitControls | undefined
let animationFrame = 0

function selectHouse(modelIndex: number) {
  selectedHouse.value = {
    modelIndex,
    houseNo: configuredHouseNumbers[modelIndex - 1] || '',
  }
}

function openHouseView(path: string) {
  if (!selectedHouse.value) return
  router.push({
    path,
    query: {
      houseNo: selectedHouse.value.houseNo,
      houseName: `仓房 ${selectedHouse.value.houseNo}`,
    },
  })
}

function createHouseLabel(modelIndex: number) {
  const houseNo = configuredHouseNumbers[modelIndex - 1]
  if (!houseNo) return
  const canvas = document.createElement('canvas')
  canvas.width = 256
  canvas.height = 96
  const context = canvas.getContext('2d')!

  context.fillStyle = 'rgba(20, 42, 50, 0.9)'
  context.strokeStyle = 'rgba(205, 229, 232, 0.72)'
  context.lineWidth = 3
  context.beginPath()
  context.roundRect(3, 3, 250, 90, 16)
  context.fill()
  context.stroke()
  context.fillStyle = '#f2f7f6'
  context.font = '600 38px sans-serif'
  context.textAlign = 'center'
  context.textBaseline = 'middle'
  context.fillText(houseNo, 128, 49)

  const texture = new THREE.CanvasTexture(canvas)
  texture.colorSpace = THREE.SRGBColorSpace
  const material = new THREE.SpriteMaterial({
    map: texture,
    transparent: true,
    depthTest: false,
  })
  const label = new THREE.Sprite(material)
  label.scale.set(4.4, 1.65, 1)
  label.renderOrder = 20
  label.userData.houseNumber = modelIndex
  return label
}

function addHouseLabel(
  parent: THREE.Object3D,
  modelIndex: number,
  x: number,
  y: number,
  z = 0,
) {
  const label = createHouseLabel(modelIndex)
  if (!label) return
  label.position.set(x, y, z)
  parent.add(label)
}

const SITE_WIDTH = 116
const SITE_DEPTH = 64
const HOUSE_WIDTH = 7
const HOUSE_DEPTH = 7.5
const HOUSE_HEIGHT = 3.8
const SILO_DIAMETER = 5.4
const SILO_HEIGHT = 7
const SMALL_SILO_DIAMETER = 2.1
const GROUPS = [4, 2, 2, 2, 2]
const HOUSE_GAP = 1.15
const COMPACT_HOUSE_GAP = 0.7
const GROUP_GAP = 3.8
const THIRD_AREA_Z = 8
const U_HOUSE_DEPTH = 14
const FOURTH_AREA_Z = 24
const FOURTH_TOWER_Z = FOURTH_AREA_Z - U_HOUSE_DEPTH / 4
const ROOF_GRAY = '#858f92'
const ROOF_WHITE = '#e3e7e4'
const ROOF_BLUE = '#3977ad'

function squareRoofColor(houseNumber: number) {
  const isWhite = (houseNumber >= 9 && houseNumber <= 12)
    || (houseNumber >= 21 && houseNumber <= 24)
  if (isWhite) return ROOF_WHITE
  if (houseNumber === 19 || houseNumber === 20 || houseNumber === 39 || houseNumber === 40) {
    return ROOF_BLUE
  }
  return ROOF_GRAY
}

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

function createHouse(index: number, roofColor = ROOF_GRAY) {
  const house = new THREE.Group()
  const wallMaterial = new THREE.MeshStandardMaterial({
    color: index < 4 ? '#d8e2e8' : '#cbd9df',
    roughness: 0.72,
  })
  const trimMaterial = new THREE.MeshStandardMaterial({ color: '#78909c', roughness: 0.6 })
  const roofMaterial = new THREE.MeshStandardMaterial({ color: roofColor, roughness: 0.78 })

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

function createCombinedHouse(count: number, startNumber: number, roofColor = ROOF_GRAY) {
  const house = new THREE.Group()
  const totalWidth = count * HOUSE_WIDTH + (count - 1) * HOUSE_GAP
  const bayWidth = totalWidth / count
  const wallMaterial = new THREE.MeshStandardMaterial({ color: '#cbd9df', roughness: 0.72 })
  const trimMaterial = new THREE.MeshStandardMaterial({ color: '#6f858f', roughness: 0.6 })
  const roofMaterial = new THREE.MeshStandardMaterial({ color: roofColor, roughness: 0.78 })
  const hitMaterial = new THREE.MeshBasicMaterial({
    transparent: true,
    opacity: 0,
    depthWrite: false,
  })

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

    const hitArea = new THREE.Mesh(
      new THREE.BoxGeometry(bayWidth - 0.12, HOUSE_HEIGHT + 0.6, HOUSE_DEPTH + 0.5),
      hitMaterial,
    )
    hitArea.position.set(centerX, HOUSE_HEIGHT / 2, 0)
    hitArea.userData.houseNumber = startNumber + bay
    house.add(hitArea)
    addHouseLabel(house, startNumber + bay, centerX, HOUSE_HEIGHT + 1.25)

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
  const radius = SMALL_SILO_DIAMETER / 2
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

function createRectangularBuilding(
  width: number,
  depth: number,
  height: number,
) {
  const building = new THREE.Group()
  const wallMaterial = new THREE.MeshStandardMaterial({ color: '#aebdc0', roughness: 0.68 })
  const roofMaterial = new THREE.MeshStandardMaterial({ color: ROOF_GRAY, roughness: 0.72 })
  const windowMaterial = new THREE.MeshStandardMaterial({
    color: '#51717e',
    emissive: '#18343f',
    emissiveIntensity: 0.35,
    roughness: 0.3,
  })

  const body = new THREE.Mesh(new THREE.BoxGeometry(width, height, depth), wallMaterial)
  body.position.y = height / 2
  body.castShadow = true
  body.receiveShadow = true
  building.add(body)

  const roof = new THREE.Mesh(new THREE.BoxGeometry(width + 0.25, 0.22, depth + 0.25), roofMaterial)
  roof.position.y = height + 0.11
  roof.castShadow = true
  building.add(roof)

  const levels = height > 7 ? [2.1, 4.6, 6.8] : [1.7, 3.7]
  levels.forEach((y) => {
    const frontWindow = new THREE.Mesh(new THREE.BoxGeometry(width * 0.5, 0.7, 0.12), windowMaterial)
    frontWindow.position.set(0, y, depth / 2 + 0.07)
    building.add(frontWindow)
  })

  return building
}

function createUShapedHouse(outerWidth: number, recessWidth: number, recessCenterX: number) {
  const building = new THREE.Group()
  const outerDepth = U_HOUSE_DEPTH
  const recessDepth = outerDepth / 2
  const backDepth = outerDepth - recessDepth
  const recessLeft = recessCenterX - recessWidth / 2
  const recessRight = recessCenterX + recessWidth / 2
  const leftWidth = recessLeft + outerWidth / 2
  const rightWidth = outerWidth / 2 - recessRight
  const height = 2.2
  const wallMaterial = new THREE.MeshStandardMaterial({ color: '#c8d3d0', roughness: 0.76 })
  const roofMaterial = new THREE.MeshStandardMaterial({ color: ROOF_BLUE, roughness: 0.74 })

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

  // 后部横梁连接两侧实体；中央凹槽可独立调宽、偏移，深度为建筑纵深的一半。
  addSection(outerWidth, backDepth, 0, outerDepth / 2 - backDepth / 2)
  addSection(leftWidth, recessDepth, -outerWidth / 2 + leftWidth / 2, -outerDepth / 2 + recessDepth / 2)
  addSection(rightWidth, recessDepth, outerWidth / 2 - rightWidth / 2, -outerDepth / 2 + recessDepth / 2)

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
      const combinedHouse = createCombinedHouse(count, houseNumber, squareRoofColor(houseNumber))
      combinedHouse.position.set(cursor + combinedWidth / 2, 0.18, z)
      houses.add(combinedHouse)
      cursor += combinedWidth
      houseNumber += count
      if (groupIndex < GROUPS.length - 1) cursor += GROUP_GAP
      return
    }

    const internalGap = compactGroups.includes(groupIndex) ? COMPACT_HOUSE_GAP : HOUSE_GAP
    for (let i = 0; i < count; i += 1) {
      const house = createHouse(houseNumber - startNumber, squareRoofColor(houseNumber))
      house.position.set(cursor + HOUSE_WIDTH / 2, 0.18, z)
      house.userData.houseNumber = houseNumber
      addHouseLabel(house, houseNumber, 0, HOUSE_HEIGHT + 1.25)
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
  const linkedBuildingX = addThirdArea()
  addFourthArea(linkedBuildingX)
  addSkybridge(linkedBuildingX)
}

function addThirdArea() {
  if (!scene) return 0

  const area = new THREE.Group()
  const firstGroupWidth = HOUSE_WIDTH * 4 + HOUSE_GAP * 3
  const cylinderPairWidth = SILO_DIAMETER * 2 + HOUSE_GAP
  const squarePairWidth = HOUSE_WIDTH * 2 + HOUSE_GAP
  const bridgeGap = firstGroupWidth - cylinderPairWidth - squarePairWidth
  const towerWidth = 4.5
  const towerGap = 0.9
  const eightSilosWidth = SILO_DIAMETER * 8 + HOUSE_GAP * 6 + towerGap * 2 + towerWidth
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
      addHouseLabel(silo, houseNumber, 0, SILO_HEIGHT + 1.15)
      area.add(silo)
      houseNumber += 1
    }
  }

  cursor += cylinderPairWidth + bridgeGap
  for (let i = 0; i < 2; i += 1) {
    const house = createHouse(4 + i, squareRoofColor(houseNumber))
    house.position.set(cursor + HOUSE_WIDTH / 2, 0.18, THIRD_AREA_Z)
    house.userData.houseNumber = houseNumber
    addHouseLabel(house, houseNumber, 0, HOUSE_HEIGHT + 1.25)
    area.add(house)
    cursor += HOUSE_WIDTH + (i === 0 ? HOUSE_GAP : 0)
    houseNumber += 1
  }

  // 第二段：四个圆柱仓、高楼、四个圆柱仓单排。
  cursor += GROUP_GAP
  for (let i = 0; i < 4; i += 1) {
    const silo = createSilo()
    silo.position.set(cursor + SILO_DIAMETER / 2, 0.18, THIRD_AREA_Z)
    silo.userData.houseNumber = houseNumber
    addHouseLabel(silo, houseNumber, 0, SILO_HEIGHT + 1.15)
    area.add(silo)
    cursor += SILO_DIAMETER + (i < 3 ? HOUSE_GAP : 0)
    houseNumber += 1
  }

  cursor += towerGap
  const linkedBuildingX = cursor + towerWidth / 2
  const thirdTower = createRectangularBuilding(towerWidth, 4.4, 10.2)
  thirdTower.position.set(linkedBuildingX, 0.18, THIRD_AREA_Z)
  area.add(thirdTower)
  cursor += towerWidth + towerGap

  for (let i = 0; i < 4; i += 1) {
    const silo = createSilo()
    silo.position.set(cursor + SILO_DIAMETER / 2, 0.18, THIRD_AREA_Z)
    silo.userData.houseNumber = houseNumber
    addHouseLabel(silo, houseNumber, 0, SILO_HEIGHT + 1.15)
    area.add(silo)
    cursor += SILO_DIAMETER + (i < 3 ? HOUSE_GAP : 0)
    houseNumber += 1
  }

  // 第三段：两个方仓。
  cursor += GROUP_GAP
  const combinedHouse = createCombinedHouse(2, houseNumber, squareRoofColor(houseNumber))
  combinedHouse.position.set(cursor + squarePairWidth / 2, 0.18, THIRD_AREA_Z)
  area.add(combinedHouse)

  scene.add(area)
  return linkedBuildingX
}

function addFourthArea(linkedBuildingX: number) {
  if (!scene) return

  const area = new THREE.Group()
  const standardRowWidth = GROUPS.reduce((total, count) => total + count * HOUSE_WIDTH, 0)
    + (GROUPS.reduce((total, count) => total + count, 0) - GROUPS.length) * HOUSE_GAP
    + (GROUPS.length - 1) * GROUP_GAP
  const emptyThreeHousesWidth = HOUSE_WIDTH * 3 + HOUSE_GAP * 2
  const leftSideReduction = 5
  const fourthRowRightEdge = standardRowWidth / 2
  const buildingWidth = standardRowWidth - emptyThreeHousesWidth - leftSideReduction
  const buildingCenterX = fourthRowRightEdge - buildingWidth / 2
  const towerWidth = 3.2
  const columnsPerSide = 5
  const siloGapX = 0.7
  const rowWidth = columnsPerSide * 2 * SMALL_SILO_DIAMETER
    + towerWidth
    + columnsPerSide * 2 * siloGapX
  const recessWidth = rowWidth + 0.8
  const recessCenterX = linkedBuildingX - buildingCenterX
  const buildingCenterZ = FOURTH_AREA_Z

  const uHouse = createUShapedHouse(buildingWidth, recessWidth, recessCenterX)
  uHouse.position.set(buildingCenterX, 0.18, buildingCenterZ)
  area.add(uHouse)

  let cursor = linkedBuildingX - rowWidth / 2
  let houseNumber = 41

  for (let column = 0; column < columnsPerSide; column += 1) {
    const silo = createSmallSilo()
    silo.position.set(
      cursor + SMALL_SILO_DIAMETER / 2,
      0.2,
      FOURTH_TOWER_Z,
    )
    silo.userData.houseNumber = houseNumber
    addHouseLabel(silo, houseNumber, 0, 4.25)
    area.add(silo)
    cursor += SMALL_SILO_DIAMETER + (column < columnsPerSide - 1 ? siloGapX : 0)
    houseNumber += 1
  }

  cursor += siloGapX
  const fourthTower = createRectangularBuilding(towerWidth, 4.2, 5.2)
  fourthTower.position.set(linkedBuildingX, 0.18, FOURTH_TOWER_Z)
  area.add(fourthTower)
  cursor += towerWidth + siloGapX

  for (let column = 0; column < columnsPerSide; column += 1) {
    const silo = createSmallSilo()
    silo.position.set(cursor + SMALL_SILO_DIAMETER / 2, 0.2, FOURTH_TOWER_Z)
    silo.userData.houseNumber = houseNumber
    addHouseLabel(silo, houseNumber, 0, 4.25)
    area.add(silo)
    cursor += SMALL_SILO_DIAMETER + (column < columnsPerSide - 1 ? siloGapX : 0)
    houseNumber += 1
  }

  scene.add(area)
}

function addSkybridge(x: number) {
  if (!scene) return

  const thirdTowerEdge = THIRD_AREA_Z + 4.4 / 2
  const fourthTowerEdge = FOURTH_TOWER_Z - 4.2 / 2
  const bridgeDepth = fourthTowerEdge - thirdTowerEdge
  const bridge = new THREE.Group()
  const glassMaterial = new THREE.MeshStandardMaterial({
    color: '#91aeb8',
    transparent: true,
    opacity: 0.82,
    roughness: 0.25,
  })
  const roofMaterial = new THREE.MeshStandardMaterial({ color: ROOF_BLUE, roughness: 0.58 })

  const body = new THREE.Mesh(new THREE.BoxGeometry(2, 1.1, bridgeDepth), glassMaterial)
  body.position.set(x, 4.15, thirdTowerEdge + bridgeDepth / 2)
  body.castShadow = true
  scene.add(body)

  const roof = new THREE.Mesh(new THREE.BoxGeometry(2.25, 0.18, bridgeDepth + 0.2), roofMaterial)
  roof.position.set(x, 4.79, thirdTowerEdge + bridgeDepth / 2)
  roof.castShadow = true
  bridge.add(roof)
  scene.add(bridge)
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

function handleCanvasPointerDown(event: PointerEvent) {
  pointerStart = { x: event.clientX, y: event.clientY }
}

function handleCanvasPointerUp(event: PointerEvent) {
  if (!renderer || !scene || !camera) return
  const moved = Math.hypot(event.clientX - pointerStart.x, event.clientY - pointerStart.y)
  if (moved > 6) return

  const rect = renderer.domElement.getBoundingClientRect()
  pointer.x = ((event.clientX - rect.left) / rect.width) * 2 - 1
  pointer.y = -((event.clientY - rect.top) / rect.height) * 2 + 1
  raycaster.setFromCamera(pointer, camera)

  const intersections = raycaster.intersectObjects(scene.children, true)
  for (const intersection of intersections) {
    let object: THREE.Object3D | null = intersection.object
    while (object && object !== scene) {
      const houseNumber = object.userData.houseNumber
      if (typeof houseNumber === 'number') {
        selectHouse(houseNumber)
        return
      }
      object = object.parent
    }
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
  renderer.domElement.addEventListener('pointerdown', handleCanvasPointerDown)
  renderer.domElement.addEventListener('pointerup', handleCanvasPointerUp)

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
  renderer?.domElement.removeEventListener('pointerdown', handleCanvasPointerDown)
  renderer?.domElement.removeEventListener('pointerup', handleCanvasPointerUp)
  controls?.dispose()
  scene?.traverse((object) => {
    if (object instanceof THREE.Sprite) {
      object.material.map?.dispose()
      object.material.dispose()
      return
    }
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

.scene-container :deep(canvas) {
  cursor: grab;
}

.scene-container :deep(canvas:active) {
  cursor: grabbing;
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

.house-panel {
  position: absolute;
  top: 28px;
  right: 30px;
  width: min(380px, calc(100vw - 32px));
  padding: 20px;
  color: #eef5f5;
  background: linear-gradient(145deg, rgb(23 43 50 / 94%), rgb(31 57 65 / 90%));
  border: 1px solid rgb(184 211 214 / 28%);
  border-radius: 14px;
  box-shadow: 0 18px 50px rgb(12 27 32 / 28%);
  backdrop-filter: blur(12px);
}

.house-panel__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 14px;
  border-bottom: 1px solid rgb(207 225 224 / 16%);
}

.house-panel__header p,
.house-panel__header h2 {
  margin: 0;
}

.house-panel__header p {
  color: #78b5d7;
  font-size: 9px;
  letter-spacing: 0.16em;
}

.house-panel__header h2 {
  margin-top: 4px;
  font-size: 20px;
  font-weight: 600;
}

.house-panel__tools {
  display: flex;
  align-items: center;
  gap: 8px;
}

.house-panel__close {
  display: grid;
  width: 28px;
  height: 28px;
  padding: 0;
  place-items: center;
  color: #c9dadd;
  background: rgb(113 145 153 / 12%);
  border: 1px solid rgb(190 216 219 / 20%);
  border-radius: 50%;
  font-size: 19px;
  line-height: 1;
  cursor: pointer;
  transition: color 160ms ease, background 160ms ease, border-color 160ms ease;
}

.house-panel__close:hover {
  color: #fff;
  background: rgb(181 91 82 / 42%);
  border-color: rgb(230 147 137 / 54%);
}

.status-dot {
  padding: 4px 8px;
  color: #a8c7d5;
  background: rgb(76 124 143 / 20%);
  border: 1px solid rgb(118 170 191 / 24%);
  border-radius: 999px;
  font-size: 10px;
  white-space: nowrap;
}

.house-details {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 1px;
  margin: 14px 0;
  overflow: hidden;
  background: rgb(195 218 220 / 12%);
  border: 1px solid rgb(195 218 220 / 12%);
  border-radius: 9px;
}

.house-details > div {
  padding: 11px 12px;
  background: rgb(29 52 59 / 96%);
}

.house-details__wide {
  grid-column: 1 / -1;
}

.house-details dt {
  margin-bottom: 4px;
  color: #91a9ae;
  font-size: 11px;
}

.house-details dd {
  margin: 0;
  color: #f2f6f5;
  font-size: 14px;
  font-weight: 600;
}

.house-details dd.status-safe {
  color: #84d3a3;
}

.house-actions {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

.house-actions button {
  min-height: 38px;
  padding: 8px 10px;
  color: #dcebef;
  background: rgb(55 112 143 / 28%);
  border: 1px solid rgb(91 157 191 / 38%);
  border-radius: 7px;
  font-size: 12px;
  cursor: pointer;
  transition: background 160ms ease, border-color 160ms ease, transform 160ms ease;
}

.house-actions button:hover {
  background: rgb(55 126 164 / 48%);
  border-color: rgb(121 188 222 / 64%);
  transform: translateY(-1px);
}

.select-hint {
  position: absolute;
  right: 30px;
  bottom: 26px;
  padding: 9px 13px;
  color: #e5eeee;
  background: rgb(25 47 54 / 76%);
  border: 1px solid rgb(207 225 224 / 18%);
  border-radius: 999px;
  font-size: 12px;
  pointer-events: none;
  backdrop-filter: blur(8px);
}

@media (max-width: 720px) {
  .page-heading {
    top: 16px;
    left: 16px;
  }

  .house-panel {
    top: auto;
    right: 16px;
    bottom: 16px;
  }
}
</style>
