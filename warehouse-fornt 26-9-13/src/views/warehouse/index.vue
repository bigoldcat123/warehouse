<template>
    <!-- 工具栏 -->
    <div class="flex items-center gap-4 mb-5">
        <button v-if="!currentUser.isGuest()" class="btn-primary" @click="addDialog = true">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
            </svg>
            新增
        </button>
    </div>

    <!-- 数据表格 -->
    <div class="table-wrapper">
        <table class="data-table">
            <thead>
                <tr>
                    <th class="text-left">ID</th>
                    <th class="text-left">仓库编号</th>
                    <th class="text-left">仓库名</th>
                    <th class="text-left">地址</th>
                    <th class="text-center">操作</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="(row, idx) in list?.records" :key="idx">
                    <td class="text-white font-medium">{{ row.id }}</td>
                    <td class="text-white">{{ row.warehouseNo }}</td>
                    <td class="text-white">{{ row.warehouseName }}</td>
                    <td class="text-[#b0d0f0]">{{ row.warehouseAddress || '--' }}</td>
                    <td class="text-center">
                        <div v-if="!currentUser.isGuest()" class="flex items-center justify-center gap-2">
                            <button class="action-btn action-btn--primary"
                                @click="() => { current = row; updateDialog = true; }">
                                <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                        d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                                </svg>
                                编辑
                            </button>
                            <el-popconfirm title="确认删除?"
                                @confirm="() => warehouse.deleteById(row.id!).then(() => fetchData())">
                                <template #reference>
                                    <button class="action-btn action-btn--danger">
                                        <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                                d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                                        </svg>
                                        删除
                                    </button>
                                </template>
                            </el-popconfirm>
                        </div>
                    </td>
                </tr>
                <tr v-if="!list?.records || list.records.length === 0">
                    <td colspan="5" class="text-center text-[#b0d0f0] py-10">
                        <svg class="w-10 h-10 mx-auto mb-2 opacity-40" fill="none" stroke="currentColor"
                            viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
                                d="M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                        </svg>
                        暂无数据
                    </td>
                </tr>
            </tbody>
        </table>
    </div>

    <!-- 分页 -->
    <div v-if="list?.pages && list.pages > 1" class="flex items-center justify-center gap-2 mt-5">
        <button class="page-btn" :class="{ 'page-btn--disabled': currentpage <= 1 }" :disabled="currentpage <= 1"
            @click="pagechange(currentpage - 1)">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
            </svg>
            上一页
        </button>

        <template v-for="page in pageList" :key="page">
            <button v-if="page !== '...'" class="page-btn" :class="{ 'page-btn--active': page === currentpage }"
                @click="pagechange(Number(page))">
                {{ page }}
            </button>
            <span v-else class="text-[#b0d0f0] px-2">…</span>
        </template>

        <button class="page-btn" :class="{ 'page-btn--disabled': currentpage >= (list?.pages || 1) }"
            :disabled="currentpage >= (list?.pages || 1)" @click="pagechange(currentpage + 1)">
            下一页
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
            </svg>
        </button>
    </div>

    <AddDialog :dialog-visible="addDialog" @refresh="fetchData" @close="addDialog = false"></AddDialog>
    <UpdateDialog :warehouse="current!" :dialog-visible="updateDialog" @refresh="fetchData"
        @close="updateDialog = false"></UpdateDialog>
</template>
<script setup lang="ts">
import { ref, computed } from 'vue'
import warehouse, { type type_WareHouse } from '@/api/warehouse';
import AddDialog from './AddDialog.vue';
import UpdateDialog from './UpdateDialog.vue';
import { useCurrentUserStore } from '@/stores/currentUser';
const currentUser = useCurrentUserStore()
const list = ref<Page<type_WareHouse>>()
const addDialog = ref(false)
const updateDialog = ref(false)
const current = ref<type_WareHouse>()
const currentpage = ref(1)
const size = ref(10)

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

function fetchData() {
    warehouse.list(currentpage.value, size.value).then(res => {
        list.value = res.data.value
    })
}
fetchData()
function pagechange(page: number) {
    currentpage.value = page
    fetchData()
}
</script>
<style scoped>
/* 按钮 */
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
    background: rgba(229, 57, 53, 0.85);
    color: white;
    border-color: rgba(255, 255, 255, 0.15);
}

.action-btn--danger:hover {
    background: #E53935;
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
<style>
/* popconfirm 深色覆盖 */
.el-popconfirm.el-popover {
    background: #1a3a5c !important;
    border: 1px solid rgba(255, 255, 255, 0.15) !important;
    padding: 14px 16px !important;
}

.el-popconfirm .el-popconfirm__main {
    color: #b0d0f0 !important;
    font-size: 13px;
}

.el-popconfirm .el-popconfirm__action {
    margin-top: 12px;
    display: flex;
    gap: 8px;
    justify-content: flex-end;
}

.el-popconfirm .el-popconfirm__action .el-button--primary {
    background: linear-gradient(135deg, #1E88E5 0%, #1565C0 100%) !important;
    border: none !important;
    color: white !important;
    font-weight: 600;
}

.el-popconfirm .el-popconfirm__action .el-button--primary:hover {
    background: linear-gradient(135deg, #1976D2 0%, #0D47A1 100%) !important;
}

.el-popconfirm .el-popconfirm__action .el-button--default {
    background: rgba(255, 255, 255, 0.08) !important;
    border: 1px solid rgba(255, 255, 255, 0.2) !important;
    color: #b0d0f0 !important;
}

.el-popconfirm .el-popconfirm__action .el-button--default:hover {
    background: rgba(255, 255, 255, 0.15) !important;
    color: white !important;
    border-color: rgba(255, 255, 255, 0.4) !important;
}

.el-popconfirm .el-popper__arrow::before {
    background: #1a3a5c !important;
    border-color: rgba(255, 255, 255, 0.15) !important;
}
</style>
