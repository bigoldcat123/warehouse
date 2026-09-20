<template>
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
          @click="store.clearSelection"
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
        <dd class="status-safe">{{ selectedHouse.fumigationStatus }}</dd>
      </div>
      <div>
        <dt>仓间氧浓度</dt>
        <dd>{{ selectedHouse.oxygenConcentration }}</dd>
      </div>
      <div>
        <dt>仓间 CQ2 浓度</dt>
        <dd>{{ selectedHouse.cq2Concentration }}</dd>
      </div>
      <div>
        <dt>仓间 PH3 浓度</dt>
        <dd>{{ selectedHouse.ph3Concentration }}</dd>
      </div>
      <div class="house-details__wide">
        <dt>采集时间</dt>
        <dd>{{ selectedHouse.collectedAt }}</dd>
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
</template>

<script setup lang="ts">
import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router'
import { useWarehousePanoramaStore } from '../../store'

const router = useRouter()
const store = useWarehousePanoramaStore()
const { selectedHouse } = storeToRefs(store)

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
</script>

<style scoped>
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
  .house-panel {
    top: auto;
    right: 16px;
    bottom: 16px;
  }
}
</style>
