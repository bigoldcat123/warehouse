<template>
  <Binner />

  <div class="toolbar">
    <button class="btn-primary" @click="openAdd">＋ 新增</button>
    <span class="warehouse-label">当前仓库：{{ currentWarehouseName || '--' }}</span>
    <el-select v-model="houseNo" clearable filterable placeholder="全部仓房" class="house-filter">
      <el-option v-for="item in houses" :key="item.houseNo" :label="item.houseNo" :value="item.houseNo" />
    </el-select>
    <button class="btn-success" @click="search">搜索</button>
  </div>

  <div class="table-wrapper">
    <table class="data-table">
      <thead>
        <tr>
          <th>ID</th>
          <th>仓房编号</th>
          <th>开始时间</th>
          <th>结束时间</th>
          <th>最后修改时间</th>
          <th class="text-center">操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="row in list?.records" :key="row.id">
          <td>{{ row.id }}</td>
          <td>{{ row.houseNo }}</td>
          <td>{{ row.startDt }}</td>
          <td>{{ row.stopDt || '进行中' }}</td>
          <td>{{ row.setDatetime || '--' }}</td>
          <td class="text-center">
            <div class="actions">
              <button class="action-btn action-btn--primary" @click="openEdit(row)">编辑</button>
              <el-popconfirm title="确认删除这条熏蒸记录？" @confirm="remove(row.id!)">
                <template #reference>
                  <button class="action-btn action-btn--danger">删除</button>
                </template>
              </el-popconfirm>
            </div>
          </td>
        </tr>
        <tr v-if="!list?.records?.length">
          <td colspan="6" class="empty">暂无熏蒸记录</td>
        </tr>
      </tbody>
    </table>
  </div>

  <div v-if="(list?.pages || 0) > 1" class="pagination">
    <button :disabled="currentPage <= 1" @click="changePage(currentPage - 1)">上一页</button>
    <span>{{ currentPage }} / {{ list?.pages }}</span>
    <button :disabled="currentPage >= (list?.pages || 1)" @click="changePage(currentPage + 1)">下一页</button>
  </div>

  <RecordDialog
    :dialog-visible="dialogVisible"
    :record="current"
    :houses="houses"
    @close="dialogVisible = false"
    @refresh="fetchData"
  />
</template>

<script setup lang="ts">
import { ref } from 'vue'
import Binner from '@/components/common/Binner.vue'
import houseApi, { type type_House } from '@/api/house'
import warehouseApi from '@/api/warehouse'
import xunzheng, { type type_Xunzheng } from '@/api/xunzheng'
import { useCurrentWareHouse } from '@/stores/currentWareHouse'
import RecordDialog from './RecordDialog.vue'

const warehouseStore = useCurrentWareHouse()
const list = ref<Page<type_Xunzheng>>()
const houses = ref<type_House[]>([])
const houseNo = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const current = ref<type_Xunzheng>()
const currentWarehouseId = ref('')
const currentWarehouseName = ref('')

async function initialize() {
  const warehouseResponse = await warehouseApi.belongKv()
  const warehouses = (warehouseResponse.data.value || []) as Array<{ key: string; value: string; no: string }>
  const storedId = warehouseStore.getWareHouse().waerhouseId
  const selected = warehouses.find(item => String(item.key) === String(storedId)) || warehouses[0]
  if (!selected) return
  currentWarehouseId.value = String(selected.key)
  currentWarehouseName.value = selected.value
  warehouseStore.setWareHouse({
    waerhouseId: currentWarehouseId.value,
    wareHouseNO: selected.no,
    wareHouseName: selected.value
  })
  const houseResponse = await houseApi.list(1, 1000, currentWarehouseId.value, '')
  houses.value = houseResponse.data.value.records || []
  await fetchData()
}

async function fetchData() {
  if (!currentWarehouseId.value) return
  const response = await xunzheng.list(
    currentPage.value,
    pageSize.value,
    currentWarehouseId.value,
    houseNo.value || undefined
  )
  list.value = response.data.value
}

function search() {
  currentPage.value = 1
  fetchData()
}

function openAdd() {
  current.value = undefined
  dialogVisible.value = true
}

function openEdit(record: type_Xunzheng) {
  current.value = record
  dialogVisible.value = true
}

async function remove(id: number) {
  const response = await xunzheng.deleteById(id)
  if (response.data.code === '200') await fetchData()
}

function changePage(page: number) {
  currentPage.value = page
  fetchData()
}

initialize()
</script>

<style scoped>
.toolbar { display: flex; align-items: center; gap: 12px; margin-bottom: 20px; }
.warehouse-label { margin-left: auto; color: #b0d0f0; font-size: 14px; }
.house-filter { width: 180px; }
.btn-primary, .btn-success { padding: 8px 18px; border: 0; border-radius: 6px; color: white; font-weight: 600; cursor: pointer; }
.btn-primary { background: linear-gradient(135deg, #1e88e5, #1565c0); }
.btn-success { background: linear-gradient(135deg, #43a047, #2e7d32); }
.table-wrapper { overflow: hidden; border: 1px solid rgba(255,255,255,.08); border-radius: 8px; background: rgba(0,0,0,.15); }
.data-table { width: 100%; border-collapse: collapse; }
.data-table thead { background: linear-gradient(135deg, #2968a8, #1e5f8a); }
.data-table th, .data-table td { padding: 12px 16px; color: white; text-align: left; border-bottom: 1px solid rgba(255,255,255,.06); }
.text-center { text-align: center !important; }
.actions { display: flex; justify-content: center; gap: 8px; }
.action-btn { padding: 5px 12px; border: 0; border-radius: 4px; color: white; cursor: pointer; }
.action-btn--primary { background: #1e88e5; }
.action-btn--danger { background: #e53935; }
.empty { padding: 40px !important; text-align: center !important; color: #b0d0f0 !important; }
.pagination { display: flex; justify-content: center; align-items: center; gap: 14px; margin-top: 18px; color: #b0d0f0; }
.pagination button { padding: 6px 14px; border: 1px solid rgba(255,255,255,.16); border-radius: 4px; background: rgba(255,255,255,.08); color: white; cursor: pointer; }
.pagination button:disabled { opacity: .4; cursor: not-allowed; }
</style>
