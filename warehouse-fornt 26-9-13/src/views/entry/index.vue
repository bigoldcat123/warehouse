<template>
    <Binner />

    <!-- 工具栏 -->
    <div class="flex items-center justify-between mb-5">
        <button v-if="!currentUser.isGuest()" class="btn-primary" @click="addDialog = true">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
            </svg>
            入库
        </button>
    </div>

    <!-- 搜索筛选栏 -->
    <div class="filter-bar">
        <div class="filter-item">
            <label class="filter-label">仓库</label>
            <select v-model="waerhouseID" @change="warehouseChange(waerhouseID)" class="custom-select">
                <option value="" disabled>选择仓库</option>
                <option v-for="item in waerhouseKv" :key="item.key" :value="item.key">{{ item.value }}</option>
            </select>
        </div>
        <div class="filter-item">
            <label class="filter-label">仓房</label>
            <select v-model="houseID" class="custom-select">
                <option value="" disabled>选择仓房</option>
                <option v-for="item in houseKv" :key="item.key" :value="item.key">{{ item.value }}</option>
            </select>
        </div>
        <div class="filter-item">
            <label class="filter-label">起始</label>
            <el-date-picker
                v-model="from"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="选择起始时间"
                class="dark-date"
            />
        </div>
        <div class="filter-item">
            <label class="filter-label">结束</label>
            <el-date-picker
                v-model="to"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="选择结束时间"
                class="dark-date"
            />
        </div>
        <div class="flex items-center gap-2 ml-auto">
            <button class="btn-success" @click="query">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                </svg>
                查询
            </button>
            <button class="btn-warn" @click="reset">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                </svg>
                重置
            </button>
        </div>
    </div>

    <!-- 数据表格 -->
    <div class="table-wrapper">
        <table class="data-table">
            <thead>
                <tr>
                    <th class="text-left">仓房</th>
                    <th class="text-left">品种</th>
                    <th class="text-left">入库人</th>
                    <th class="text-left">管理人</th>
                    <th class="text-left">入库时间</th>
                    <th class="text-center">含水量</th>
                    <th class="text-center">操作</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="(row, idx) in list?.records" :key="idx">
                    <td class="text-white font-medium">
                        {{ kv.filter(x => x.key == row.houseID)[0]?.value || '--' }}
                    </td>
                    <td>
                        <span class="breed-tag">{{ row.breed || '--' }}</span>
                    </td>
                    <td class="text-[#b0d0f0]">
                        {{ userKv.filter(x => x.key == row.entryUserId)[0]?.value || '--' }}
                    </td>
                    <td class="text-[#b0d0f0]">
                        {{ userKv.filter(x => x.key == row.stockman)[0]?.value || '--' }}
                    </td>
                    <td class="text-[#b0d0f0]">{{ row.entryTime || '--' }}</td>
                    <td class="text-center">
                        <span class="water-badge">{{ row.water != null ? row.water + '%' : '--' }}</span>
                    </td>
                    <td class="text-center">
                        <div v-if="!currentUser.isGuest()" class="flex items-center justify-center gap-2">
                            <button class="action-btn action-btn--primary"
                                @click="() => { current = row; updateDialog = true; }">
                                <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                                </svg>
                                编辑
                            </button>
                            <el-popconfirm title="确认删除?" @confirm="ondel(row.id!)">
                                <template #reference>
                                    <button class="action-btn action-btn--danger">
                                        <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6M1 7h22M9 7V4a1 1 0 011-1h4a1 1 0 011 1v3" />
                                        </svg>
                                        删除
                                    </button>
                                </template>
                            </el-popconfirm>
                        </div>
                    </td>
                </tr>
                <tr v-if="!list?.records || list.records.length === 0">
                    <td colspan="7" class="text-center text-[#b0d0f0] py-10">
                        <svg class="w-10 h-10 mx-auto mb-2 opacity-40" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
                        </svg>
                        暂无入库记录
                    </td>
                </tr>
            </tbody>
        </table>
    </div>

    <!-- 分页 -->
    <div v-if="list?.pages && list.pages > 1" class="flex items-center justify-center gap-2 mt-5">
        <button
            class="page-btn"
            :class="{ 'page-btn--disabled': currentpage <= 1 }"
            :disabled="currentpage <= 1"
            @click="pagechange(currentpage - 1)"
        >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
            </svg>
            上一页
        </button>

        <template v-for="page in pageList" :key="page">
            <button
                v-if="page !== '...'"
                class="page-btn"
                :class="{ 'page-btn--active': page === currentpage }"
                @click="pagechange(Number(page))"
            >
                {{ page }}
            </button>
            <span v-else class="text-[#b0d0f0] px-2">…</span>
        </template>

        <button
            class="page-btn"
            :class="{ 'page-btn--disabled': currentpage >= (list?.pages || 1) }"
            :disabled="currentpage >= (list?.pages || 1)"
            @click="pagechange(currentpage + 1)"
        >
            下一页
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
            </svg>
        </button>
    </div>

    <AddDialog :dialog-visible="addDialog" @refresh="fetchData" @close="addDialog = false"></AddDialog>
    <UpdateDialog :entry="current!" :dialog-visible="updateDialog" @refresh="fetchData" @close="updateDialog = false">
    </UpdateDialog>
</template>
<script setup lang="ts">
import { ref, computed } from 'vue'
import entry, { type type_Entry } from '@/api/entry';
import house from '@/api/house';
import AddDialog from './AddDialog.vue';
import UpdateDialog from './UpdateDialog.vue';
import user from '@/api/user';
import warehouse from '@/api/warehouse';
import Binner from '@/components/common/Binner.vue';
import { useCurrentUserStore } from '@/stores/currentUser';
import { useCurrentWareHouse } from '@/stores/currentWareHouse';
const currentWareHouse = useCurrentWareHouse()
const currentUser = useCurrentUserStore()
const list = ref<Page<type_Entry>>()
const addDialog = ref(false)
const updateDialog = ref(false)
const current = ref<type_Entry>()
const currentpage = ref(1)
const size = ref(import.meta.env.ENV_PAGESIZE)
const waerhouseKv = ref<any[]>([])
const houseKv = ref<any[]>([])
const waerhouseID = ref<string | undefined>(undefined)
const houseID = ref<number | undefined>(undefined)
const from = ref('')
const to = ref('')
function ondel(id:number) {
    entry.deleteById(id).then(() => fetchData())
}

function reset () {
    waerhouseID.value = undefined
    houseID.value = undefined
    from.value = ''
    to.value = ''
    fetchData()
}
warehouse.belongKv().then(res => {
    waerhouseKv.value = res.data.value

    let item = waerhouseKv.value[0]

    currentWareHouse.init({
        wareHouseNO: item.no,
        wareHouseName: item.value,
        waerhouseId: item.key
    })
    waerhouseID.value = currentWareHouse.getWareHouse().waerhouseId as string
    warehouseChange(waerhouseID.value)
    fetchData()
})
function warehouseChange(value: any) {
    house.findByWarehouseId(value).then(res => {
        houseKv.value = res.data.value
    })
}
const query = () => {
    const item = waerhouseKv.value.filter(x => x.key == waerhouseID.value)[0]
    currentWareHouse.setWareHouse({
        wareHouseNO: item.no,
        wareHouseName: item.value,
        waerhouseId: item.key
    })
    fetchData()
}
function fetchData() {
    entry.list({ warehouseId: waerhouseID.value, houseId:houseID.value,from:from.value,to:to.value},currentpage.value, size.value).then(res => {
        list.value = res.data.value
    })
}
function pagechange(page: number) {
    currentpage.value = page
    fetchData()
}

// 分页页码列表
const pageList = computed(() => {
    const total = list.value?.pages || 1
    const cur = currentpage.value
    const pages: (number | string)[] = []
    if (total <= 7) {
        for (let i = 1; i <= total; i++) pages.push(i)
        return pages
    }
    pages.push(1)
    if (cur > 3) pages.push('...')
    const start = Math.max(2, cur - 1)
    const end = Math.min(total - 1, cur + 1)
    for (let i = start; i <= end; i++) pages.push(i)
    if (cur < total - 2) pages.push('...')
    pages.push(total)
    return pages
})

const kv = ref<any[]>([])
house.kv().then(res => {
    kv.value = res.data.value
})
const userKv = ref<any[]>([])
user.kv().then(res => {
    userKv.value = res.data.value
})

</script>
<style scoped>
/* 主按钮 */
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
    padding: 7px 16px;
    background: linear-gradient(135deg, #43A047 0%, #2E7D32 100%);
    color: white;
    border: none;
    border-radius: 6px;
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s ease;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
}
.btn-success:hover {
    background: linear-gradient(135deg, #388E3C 0%, #1B5E20 100%);
    transform: translateY(-1px);
}
.btn-warn {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 7px 16px;
    background: rgba(255, 255, 255, 0.08);
    color: #b0d0f0;
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 6px;
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s ease;
}
.btn-warn:hover {
    background: rgba(255, 255, 255, 0.15);
    color: white;
    border-color: rgba(255, 255, 255, 0.4);
}

/* 筛选栏 */
.filter-bar {
    display: flex;
    align-items: flex-end;
    gap: 16px;
    flex-wrap: wrap;
    padding: 14px 18px;
    background: rgba(0, 0, 0, 0.15);
    border: 1px solid rgba(255, 255, 255, 0.08);
    border-radius: 8px;
    margin-bottom: 16px;
}
.filter-item {
    display: flex;
    flex-direction: column;
    gap: 4px;
}
.filter-label {
    font-size: 12px;
    color: #b0d0f0;
    font-weight: 500;
}

/* select / input */
.custom-select {
    appearance: none;
    background: rgba(255, 255, 255, 0.08);
    border: 1px solid rgba(255, 255, 255, 0.2);
    color: white;
    padding: 7px 32px 7px 12px;
    border-radius: 6px;
    font-size: 13px;
    cursor: pointer;
    outline: none;
    min-width: 150px;
    transition: all 0.2s ease;
    background-image: url("data:image/svg+xml;charset=UTF-8,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%23b0d0f0' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3e%3cpolyline points='6 9 12 15 18 9'%3e%3c/polyline%3e%3c/svg%3e");
    background-repeat: no-repeat;
    background-position: right 8px center;
    background-size: 16px;
}
.custom-select:hover {
    border-color: rgba(255, 255, 255, 0.4);
    background-color: rgba(255, 255, 255, 0.12);
}
.custom-select:focus {
    border-color: #1E88E5;
    box-shadow: 0 0 0 2px rgba(30, 136, 229, 0.3);
}
.custom-select option {
    background: #1a3a5c;
    color: white;
}
.custom-input {
    background: rgba(255, 255, 255, 0.08);
    border: 1px solid rgba(255, 255, 255, 0.2);
    color: white;
    padding: 7px 12px;
    border-radius: 6px;
    font-size: 13px;
    outline: none;
    transition: all 0.2s ease;
    color-scheme: dark;
}
.custom-input:hover {
    border-color: rgba(255, 255, 255, 0.4);
    background: rgba(255, 255, 255, 0.12);
}
.custom-input:focus {
    border-color: #1E88E5;
    box-shadow: 0 0 0 2px rgba(30, 136, 229, 0.3);
}

/* 表格 */
.table-wrapper {
    background: rgba(0, 0, 0, 0.15);
    border-radius: 8px;
    overflow: hidden;
    border: 1px solid rgba(255, 255, 255, 0.08);
}
.data-table {
    width: 100%;
    border-collapse: collapse;
}
.data-table thead {
    background: linear-gradient(135deg, #2968a8 0%, #1e5f8a 100%);
}
.data-table thead th {
    padding: 12px 16px;
    color: white;
    font-weight: 600;
    font-size: 14px;
    border-bottom: 2px solid rgba(255, 255, 255, 0.1);
    white-space: nowrap;
}
.data-table tbody tr {
    background: #1e4a6e;
    transition: background 0.15s ease;
}
.data-table tbody tr:nth-child(even) {
    background: #1a3f60;
}
.data-table tbody tr:hover {
    background: rgba(30, 136, 229, 0.25);
}
.data-table tbody td {
    padding: 12px 16px;
    color: white;
    font-size: 14px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

/* 品种标签 */
.breed-tag {
    display: inline-block;
    padding: 2px 10px;
    background: rgba(100, 181, 246, 0.2);
    color: #90CAF9;
    border: 1px solid rgba(100, 181, 246, 0.4);
    border-radius: 10px;
    font-size: 12px;
    font-weight: 500;
}

/* 含水量徽章 */
.water-badge {
    display: inline-block;
    min-width: 48px;
    padding: 3px 10px;
    background: rgba(76, 175, 80, 0.2);
    color: #A5D6A7;
    border: 1px solid rgba(76, 175, 80, 0.4);
    border-radius: 12px;
    font-size: 13px;
    font-weight: 600;
    text-align: center;
}

/* 操作按钮 */
.action-btn {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 4px 10px;
    border-radius: 4px;
    font-size: 12px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s ease;
    border: 1px solid transparent;
}
.action-btn--primary {
    background: #1E88E5;
    color: white;
    border-color: rgba(255, 255, 255, 0.15);
}
.action-btn--primary:hover {
    background: #1976D2;
    border-color: rgba(255, 255, 255, 0.3);
    transform: translateY(-1px);
}
.action-btn--danger {
    background: rgba(229, 57, 53, 0.8);
    color: white;
    border-color: rgba(229, 57, 53, 0.6);
}
.action-btn--danger:hover {
    background: #C62828;
    border-color: #E53935;
    transform: translateY(-1px);
}

/* 分页 */
.page-btn {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 6px 12px;
    background: rgba(255, 255, 255, 0.08);
    color: #b0d0f0;
    border: 1px solid rgba(255, 255, 255, 0.15);
    border-radius: 4px;
    font-size: 13px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s ease;
}
.page-btn:hover:not(:disabled) {
    background: rgba(255, 255, 255, 0.15);
    color: white;
    border-color: rgba(255, 255, 255, 0.3);
}
.page-btn--active {
    background: linear-gradient(135deg, #1E88E5 0%, #1565C0 100%);
    color: white;
    border-color: #1E88E5;
    font-weight: 700;
}
.page-btn--disabled,
.page-btn:disabled {
    opacity: 0.35;
    cursor: not-allowed;
}

/* 日期选择器 */
.dark-date .el-input__wrapper {
    background: rgba(255, 255, 255, 0.08) !important;
    border: 1px solid rgba(255, 255, 255, 0.2) !important;
    box-shadow: none !important;
    border-radius: 6px !important;
}
.dark-date .el-input__wrapper:hover,
.dark-date .el-input__wrapper.is-focus {
    background: rgba(255, 255, 255, 0.12) !important;
    border-color: #43A047 !important;
}
.dark-date .el-input__inner {
    color: white !important;
}
.dark-date .el-input__inner::placeholder {
    color: rgba(255, 255, 255, 0.35) !important;
}
.dark-date .el-input__prefix,
.dark-date .el-input__suffix {
    color: #b0d0f0 !important;
}
</style>
