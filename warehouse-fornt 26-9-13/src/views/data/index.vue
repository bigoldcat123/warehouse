<template>
    <Binner />

    <!-- 筛选栏 -->
    <div class="filter-bar">
        <div class="filter-item">
            <label class="filter-label">起始时间</label>
            <input type="datetime-local" v-model="from" class="custom-input" />
        </div>
        <div class="filter-item">
            <label class="filter-label">结束时间</label>
            <input type="datetime-local" v-model="to" class="custom-input" />
        </div>
        <div class="filter-item">
            <label class="filter-label">仓房名</label>
            <input type="text" v-model="houseName" placeholder="请输入仓房名" class="custom-input" />
        </div>
        <div class="filter-item">
            <label class="filter-label">仓库</label>
            <select v-model="warehouseName" class="custom-select">
                <option v-for="item in kv" :key="item.value" :value="item.value">{{ item.value }}</option>
            </select>
        </div>
        <div class="flex items-end ml-auto">
            <button class="btn-success" @click="query">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                </svg>
                搜索
            </button>
        </div>
    </div>

    <!-- 数据表格 -->
    <div class="table-wrapper">
        <table class="data-table">
            <thead>
                <tr>
                    <th class="text-left">仓房编号</th>
                    <th class="text-left">仓房名</th>
                    <th class="text-left">仓房类型</th>
                    <th class="text-center">仓间 PH3</th>
                    <th class="text-center">仓间 O₂</th>
                    <th class="text-center">仓间 CO₂</th>
                    <th class="text-center">层 PH3 最高</th>
                    <th class="text-center">层 PH3 最低</th>
                    <th class="text-center">层 PH3 平均</th>
                    <th class="text-left">时间</th>
                    <th class="text-center">操作</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="(row, idx) in list?.records" :key="idx">
                    <td class="text-white font-medium">{{ row.houseNo }}</td>
                    <td class="text-white">{{ row.houseName }}</td>
                    <td>
                        <span class="type-tag">{{ row.house_type || '--' }}</span>
                    </td>
                    <td class="text-center">
                        <span class="temp-badge temp-badge--orange">{{ row.housePh3 != null ? row.housePh3 + ' ppm' : '--' }}</span>
                    </td>
                    <td class="text-center">
                        <span class="temp-badge temp-badge--green">{{ row.oAir != null ? row.oAir + '%' : '--' }}</span>
                    </td>
                    <td class="text-center">
                        <span class="temp-badge temp-badge--cyan">{{ row.co2Air != null ? row.co2Air + ' ppm' : '--' }}</span>
                    </td>
                    <td class="text-center">
                        <span class="layer-badge">{{ row.layerMax || '--' }}</span>
                    </td>
                    <td class="text-center">
                        <span class="layer-badge">{{ row.layerMin || '--' }}</span>
                    </td>
                    <td class="text-center">
                        <span class="layer-badge">{{ row.layerAvg || '--' }}</span>
                    </td>
                    <td class="text-[#b0d0f0]">{{ row.testDate || '--' }}</td>
                    <td class="text-center">
                        <button class="action-btn action-btn--primary" @click="() => $router.push({
                            path: '/data/detail',
                            query: {
                                id: row.id
                            }
                        })">
                            <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                            </svg>
                            详情
                        </button>
                    </td>
                </tr>
                <tr v-if="!list?.records || list.records.length === 0">
                    <td colspan="11" class="text-center text-[#b0d0f0] py-10">
                        <svg class="w-10 h-10 mx-auto mb-2 opacity-40" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                        </svg>
                        暂无数据
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
</template>
<script setup lang="ts">
import { ref, computed } from 'vue'
import Binner from '@/components/common/Binner.vue';
import data, { type type_Data } from '@/api/data';
import warehouse from '@/api/warehouse';
import { useCurrentUserStore } from '@/stores/currentUser';
import { useCurrentWareHouse } from '@/stores/currentWareHouse';
const currentWareHouse = useCurrentWareHouse()
const kv = ref<any[]>([])
const currentUser = useCurrentUserStore()
const list = ref<Page<type_Data>>()
const from = ref<string | null>(null)
const to = ref<string | null>(null)
const e = '@#$%^&*()_+'
const houseName = ref<string | null>(null)
const warehouseName = ref<string | null>(null)
const currentpage = ref(1)
const size = ref(10)
const reset = () => {
    from.value = ''
    to.value = ''
    houseName.value = ''
    warehouseName.value = ''
    fetchData()
}
warehouse.belongKv().then(res => {
    kv.value = res.data.value
    const item = kv.value[0]
    currentWareHouse.init({
        wareHouseName: item.value,
        wareHouseNO: item.no,
        waerhouseId: item.key

    })
    warehouseName.value = currentWareHouse.getWareHouse().wareHouseName as string
    fetchData()
})
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

const query = () => {
    const item = kv.value.filter(x => x.value == warehouseName.value)[0]
    currentWareHouse.setWareHouse({
        wareHouseNO: item.no,
        wareHouseName: item.value,
        waerhouseId: item.key
    })
    fetchData()
}
function fetchData() {
    data.list(from.value, to.value, houseName.value, warehouseName.value == e ? null : warehouseName.value, currentpage.value, size.value).then(res => {
        list.value = res.data.value
    })
}

</script>
<style scoped>
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

/* 输入框 */
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
.custom-input::placeholder {
    color: rgba(255, 255, 255, 0.35);
}
.custom-input:hover {
    border-color: rgba(255, 255, 255, 0.4);
    background: rgba(255, 255, 255, 0.12);
}
.custom-input:focus {
    border-color: #1E88E5;
    box-shadow: 0 0 0 2px rgba(30, 136, 229, 0.3);
}

/* 选择器 */
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

/* 按钮 */
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

/* 表格 */
.table-wrapper {
    background: rgba(0, 0, 0, 0.15);
    border-radius: 8px;
    overflow-x: auto;
    border: 1px solid rgba(255, 255, 255, 0.08);
}
.data-table {
    width: 100%;
    border-collapse: collapse;
    min-width: 1200px;
}
.data-table thead {
    background: linear-gradient(135deg, #2968a8 0%, #1e5f8a 100%);
}
.data-table thead th {
    padding: 12px 14px;
    color: white;
    font-weight: 600;
    font-size: 13px;
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
    padding: 10px 14px;
    color: white;
    font-size: 13px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

/* 类型标签 */
.type-tag {
    display: inline-block;
    padding: 2px 8px;
    background: rgba(100, 181, 246, 0.15);
    color: #90CAF9;
    border: 1px solid rgba(100, 181, 246, 0.3);
    border-radius: 4px;
    font-size: 12px;
}

/* 温度徽章 */
.temp-badge {
    display: inline-block;
    min-width: 54px;
    padding: 3px 8px;
    border-radius: 10px;
    font-size: 12px;
    font-weight: 600;
    text-align: center;
}
.temp-badge--orange {
    background: rgba(251, 140, 0, 0.2);
    color: #FFCC80;
    border: 1px solid rgba(251, 140, 0, 0.4);
}
.temp-badge--cyan {
    background: rgba(0, 188, 212, 0.2);
    color: #80DEEA;
    border: 1px solid rgba(0, 188, 212, 0.4);
}
.temp-badge--red {
    background: rgba(229, 57, 53, 0.2);
    color: #EF9A9A;
    border: 1px solid rgba(229, 57, 53, 0.4);
}
.temp-badge--blue {
    background: rgba(30, 136, 229, 0.2);
    color: #90CAF9;
    border: 1px solid rgba(30, 136, 229, 0.4);
}
.temp-badge--green {
    background: rgba(67, 160, 71, 0.2);
    color: #A5D6A7;
    border: 1px solid rgba(67, 160, 71, 0.4);
}

/* 层数据徽章 */
.layer-badge {
    display: inline-block;
    min-width: 40px;
    padding: 2px 8px;
    background: rgba(255, 255, 255, 0.08);
    color: #b0d0f0;
    border: 1px solid rgba(255, 255, 255, 0.15);
    border-radius: 8px;
    font-size: 12px;
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
</style>
