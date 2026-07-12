<template>
    <Binner />

    <!-- 状态标签筛选栏 -->
    <!-- <div class="flex items-center gap-3 mb-5 flex-wrap">
        <button
            class="status-tag status-tag--active"
            :class="{ 'ring-2 ring-white/40': activeFilter === 'all' }"
            @click="activeFilter = 'all'"
        >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zm10 0a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zm10 0a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z" />
            </svg>
            全部
        </button>
        <button
            class="status-tag status-tag--normal"
            :class="{ 'ring-2 ring-white/40': activeFilter === 'normal' }"
            @click="activeFilter = 'normal'"
        >
            <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M3 6a3 3 0 013-3h10a1 1 0 01.8 1.6L14.25 8l2.55 3.4A1 1 0 0116 13H6a1 1 0 00-1 1v3a1 1 0 11-2 0V6z" clip-rule="evenodd" />
            </svg>
            正常: {{ normalCount }}个
        </button>
        <button
            class="status-tag status-tag--danger"
            :class="{ 'ring-2 ring-white/40': activeFilter === 'overheat' }"
            @click="activeFilter = 'overheat'"
        >
            <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M12.395 2.553a1 1 0 00-1.45-.385c-.345.23-.614.558-.822.88-.214.33-.403.713-.57 1.116-.334.804-.614 1.768-.84 2.734a31.365 31.365 0 00-.613 3.58 2.64 2.64 0 01-.945-1.067c-.328-.68-.398-1.534-.398-2.654A1 1 0 005.05 6.05 6.981 6.981 0 003 11a7 7 0 1011.95-4.95c-.592-.591-.98-.985-1.348-1.467-.363-.476-.724-1.063-1.207-2.03zM12.12 15.12A3 3 0 017 13s.879.5 2.5.5c0-1 .5-4 1.25-4.5.5 1 .786 1.293 1.371 1.879A2.99 2.99 0 0113 13a2.99 2.99 0 01-.879 2.121z" clip-rule="evenodd" />
            </svg>
            整体粮温过高: {{ overheatCount }}个
        </button>
        <button
            class="status-tag status-tag--warning"
            :class="{ 'ring-2 ring-white/40': activeFilter === 'abnormal' }"
            @click="activeFilter = 'abnormal'"
        >
            <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd" />
            </svg>
            存在异常点: {{ abnormalCount }}个
        </button>
        <button
            class="status-tag status-tag--fault"
            :class="{ 'ring-2 ring-white/40': activeFilter === 'fault' }"
            @click="activeFilter = 'fault'"
        >
            <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M11.3 1.046A1 1 0 0112 2v5h4a1 1 0 01.82 1.573l-7 10A1 1 0 018 18v-5H4a1 1 0 01-.82-1.573l7-10a1 1 0 011.12-.38z" clip-rule="evenodd" />
            </svg>
            故障点过多: {{ faultCount }}个
        </button>
    </div> -->

    <!-- 搜索工具栏 -->
    <div class="flex items-center gap-4 mb-5">
        <!-- 新增按钮 -->
        <button class="btn-primary" @click="addDialog = true">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
            </svg>
            新增
        </button>

        <div class="flex items-center gap-3 ml-auto">
            <!-- 仓库选择 -->
            <div class="relative">
                <select v-model="warehouseName" class="custom-select">
                    <option v-for="item in kv" :key="item.value" :value="item.key">
                        {{ item.value }}
                    </option>
                </select>
            </div>

            <!-- 仓房编号输入 -->
            <input
                v-model="houseNo"
                type="text"
                placeholder="请输入仓房编号"
                class="custom-input"
            />

            <!-- 搜索按钮 -->
            <button class="btn-success" @click="searchList">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                </svg>
                搜索
            </button>
        </div>
    </div>

    <!-- 仓房卡片网格 -->
    <div class="flex flex-wrap gap-5">
        <HouseInfo
            :key="house.id"
            v-for="house in filteredList"
            :house="house"
            @delete="handle_delete"
            @update="(house: type_House) => { current = house; updateDialog = true; }"
            @show_image="handle_show_iamge"
        />
    </div>

    <!-- 对话框 -->
    <AddDialog :kv="kv" :dialog-visible="addDialog" @refresh="fetchData" @close="addDialog = false"></AddDialog>
    <UpdateDialog :kv="kv" :house="current!" :dialog-visible="updateDialog" @refresh="fetchData"
        @close="updateDialog = false"></UpdateDialog>
    <ImageDialog :dialog-visible="imageDialog" @close="imageDialog = false" @refresh="fetchData" :urlInfo="urlInfo"></ImageDialog>
</template>
<script setup lang="ts">
import { ref, computed } from 'vue'
import house, { type type_House } from '@/api/house';
import HouseInfo, { type UrlInfo } from '@/components/HouseInfo.vue';
import AddDialog from './AddDialog.vue';
import UpdateDialog from './UpdateDialog.vue';
import warehouse from '@/api/warehouse';
import { useCurrentUserStore } from '@/stores/currentUser';
import Binner from '@/components/common/Binner.vue';
import { useCurrentWareHouse } from '@/stores/currentWareHouse';
import ImageDialog from './ImageDialog.vue';
const currentWareHouse = useCurrentWareHouse()
const currentUser = useCurrentUserStore()
const list = ref<Page<type_House>>()
let sapre_list: type_House[] = []
const addDialog = ref(false)
const updateDialog = ref(false)
const imageDialog = ref(false)
const current = ref<type_House>()
const urlInfo = ref<Array<UrlInfo> | undefined>()
const kv = ref<any[]>([])
const warehouseName = ref('')
const houseNo = ref('')
const activeFilter = ref('all')

// 状态统计（基于实际数据，这里做简单模拟）
const normalCount = computed(() => {
    if (!list.value?.records) return 0
    return list.value.records.filter(h => h.z != null && Number(h.z) < 30).length
})
const overheatCount = computed(() => {
    if (!list.value?.records) return 0
    return list.value.records.filter(h => h.z != null && Number(h.z) >= 30).length
})
const abnormalCount = computed(() => 0)
const faultCount = computed(() => 0)

// 根据筛选条件过滤仓房列表
const filteredList = computed(() => {
    if (!list.value?.records) return []
    const records = list.value.records
    switch (activeFilter.value) {
        case 'normal':
            return records.filter(h => h.z != null && Number(h.z) < 30)
        case 'overheat':
            return records.filter(h => h.z != null && Number(h.z) >= 30)
        case 'abnormal':
            return []
        case 'fault':
            return []
        default:
            return records
    }
})

warehouse.belongKv().then(res => {
    kv.value = res.data.value
    let item = kv.value[0]
    currentWareHouse.init({
        wareHouseNO: item.no,
        wareHouseName: item.value,
        waerhouseId: item.key
    })
    warehouseName.value = currentWareHouse.getWareHouse().waerhouseId as string
    fetchData()
})

const currentpage = ref(1)
const size = ref(1000)
function fetchData() {
    house.list(currentpage.value, size.value, warehouseName.value, houseNo.value).then(res => {
        list.value = res.data.value
        sapre_list = (res.data.value.records)!;
        current.value = list.value.records![0]
    })
}
function pagechange(page: number) {
    currentpage.value = page
    fetchData()
}
function searchList() {
    const item = kv.value.filter(y => y.key == warehouseName.value)[0]
    currentWareHouse.setWareHouse({
        wareHouseNO: item.no,
        wareHouseName: item.value,
        waerhouseId: item.key
    })
    fetchData()
}
const handle_delete = (id: number) => {
    console.log("id ->" + id);
    house.deleteById(id).then(() => fetchData())
}
const handle_show_iamge = (data: Array<UrlInfo>) => {
    console.log(data);
    imageDialog.value = true
    urlInfo.value = data
}
</script>
<style scoped>
/* 状态标签样式 */
.status-tag {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 18px;
    border-radius: 6px;
    font-size: 14px;
    font-weight: 600;
    color: white;
    cursor: pointer;
    transition: all 0.2s ease;
    border: none;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
}
.status-tag:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3);
}
.status-tag--active {
    background: linear-gradient(135deg, #1E88E5 0%, #1565C0 100%);
}
.status-tag--normal {
    background: linear-gradient(135deg, #43A047 0%, #2E7D32 100%);
}
.status-tag--danger {
    background: linear-gradient(135deg, #E53935 0%, #C62828 100%);
}
.status-tag--warning {
    background: linear-gradient(135deg, #FDD835 0%, #F9A825 100%);
    color: #333;
}
.status-tag--fault {
    background: linear-gradient(135deg, #FB8C00 0%, #E65100 100%);
}

/* 按钮样式 */
.btn-primary {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 18px;
    background: linear-gradient(135deg, #1E88E5 0%, #1565C0 100%);
    color: white;
    border: none;
    border-radius: 6px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s ease;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
}
.btn-primary:hover {
    background: linear-gradient(135deg, #1976D2 0%, #0D47A1 100%);
    transform: translateY(-1px);
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3);
}
.btn-success {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 18px;
    background: linear-gradient(135deg, #43A047 0%, #2E7D32 100%);
    color: white;
    border: none;
    border-radius: 6px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s ease;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
}
.btn-success:hover {
    background: linear-gradient(135deg, #388E3C 0%, #1B5E20 100%);
    transform: translateY(-1px);
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3);
}

/* 自定义 select */
.custom-select {
    appearance: none;
    background: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    color: white;
    padding: 8px 36px 8px 14px;
    border-radius: 6px;
    font-size: 14px;
    cursor: pointer;
    outline: none;
    min-width: 160px;
    transition: all 0.2s ease;
}
.custom-select:hover {
    border-color: rgba(255, 255, 255, 0.4);
    background: rgba(255, 255, 255, 0.15);
}
.custom-select:focus {
    border-color: #1E88E5;
    box-shadow: 0 0 0 2px rgba(30, 136, 229, 0.3);
}
.custom-select option {
    background: #1a3a5c;
    color: white;
}

/* 自定义 input */
.custom-input {
    background: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    color: white;
    padding: 8px 14px;
    border-radius: 6px;
    font-size: 14px;
    outline: none;
    width: 180px;
    transition: all 0.2s ease;
}
.custom-input::placeholder {
    color: rgba(255, 255, 255, 0.4);
}
.custom-input:hover {
    border-color: rgba(255, 255, 255, 0.4);
    background: rgba(255, 255, 255, 0.15);
}
.custom-input:focus {
    border-color: #1E88E5;
    box-shadow: 0 0 0 2px rgba(30, 136, 229, 0.3);
}
</style>
