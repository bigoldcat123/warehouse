<template>
    <Binner />

    <!-- 工具栏 -->
    <div class="flex items-center justify-between mb-5">
        <div class="flex items-center gap-3">
            <svg class="w-6 h-6 text-[#64b5f6]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
            <h2 class="text-white text-xl font-bold tracking-wide">报警信息统计</h2>
        </div>
        <a href="/api/alarm/arg_excel" class="btn-primary">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
            </svg>
            导出报警信息统计
        </a>
    </div>

    <!-- 数据表格 -->
    <div class="table-wrapper">
        <table class="data-table">
            <thead>
                <tr>
                    <th class="text-left">单位编号</th>
                    <th class="text-left">单位名称</th>
                    <th class="text-center">
                        <span class="badge badge--info">一般</span>
                    </th>
                    <th class="text-center">
                        <span class="badge badge--warning">严重</span>
                    </th>
                    <th class="text-center">
                        <span class="badge badge--danger">发热</span>
                    </th>
                    <th class="text-center">
                        <span class="badge badge--purple">结露霉变</span>
                    </th>
                    <th class="text-center">
                        <span class="badge badge--yellow">异常</span>
                    </th>
                    <th class="text-center">操作</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="(row, idx) in list.records" :key="idx">
                    <td class="text-[#b0d0f0]">{{ row.wareHouseNO }}</td>
                    <td class="text-white font-medium">{{ row.wareHouseName }}</td>
                    <td class="text-center">
                        <span class="num-badge num-badge--info">{{ row.common ?? 0 }}</span>
                    </td>
                    <td class="text-center">
                        <span class="num-badge num-badge--warning">{{ row.serious ?? 0 }}</span>
                    </td>
                    <td class="text-center">
                        <span class="num-badge num-badge--danger">{{ row.hot ?? 0 }}</span>
                    </td>
                    <td class="text-center">
                        <span class="num-badge num-badge--purple">{{ row.dewAndMould ?? 0 }}</span>
                    </td>
                    <td class="text-center">
                        <span class="num-badge num-badge--yellow">{{ row.exception ?? 0 }}</span>
                    </td>
                    <td class="text-center">
                        <button class="detail-btn" @click="navigateTo(row.wareHouseNO)">
                            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                            </svg>
                            查看详细
                        </button>
                    </td>
                </tr>
                <tr v-if="!list.records || list.records.length === 0">
                    <td colspan="8" class="text-center text-[#b0d0f0] py-10">
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
    <div v-if="list.pages && list.pages > 1" class="flex items-center justify-center gap-2 mt-5">
        <button
            class="page-btn"
            :class="{ 'page-btn--disabled': currentPage <= 1 }"
            :disabled="currentPage <= 1"
            @click="pagechange(currentPage - 1)"
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
                :class="{ 'page-btn--active': page === currentPage }"
                @click="pagechange(Number(page))"
            >
                {{ page }}
            </button>
            <span v-else class="text-[#b0d0f0] px-2">…</span>
        </template>

        <button
            class="page-btn"
            :class="{ 'page-btn--disabled': currentPage >= (list.pages || 1) }"
            :disabled="currentPage >= (list.pages || 1)"
            @click="pagechange(currentPage + 1)"
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
import alarm, { type AlarmArgDTO } from '@/api/alarm';
import { useRouter } from 'vue-router';
import Binner from '@/components/common/Binner.vue';
const router = useRouter()
const list= ref<Page<AlarmArgDTO>>({})
const size = 10
const currentPage = ref(1)
function fetchData () {
    alarm.arg(currentPage.value -1,size).then(res => {
    console.log(res.data.value);
    list.value = res.data.value
})
}

const navigateTo = (wareHouseNO:string) => {
    router.push({path:'/alarm',query:{wareHouseNO:wareHouseNO}})
}
const pagechange = (page: number) => {
    currentPage.value = page
    fetchData()
}

// 计算分页页码列表
const pageList = computed(() => {
    const total = list.value.pages || 1
    const cur = currentPage.value
    const pages: (number | string)[] = []
    if (total <= 7) {
        for (let i = 1; i <= total; i++) pages.push(i)
        return pages
    }
    // 始终显示第一页、最后一页、当前页前后各1页，其余用省略号
    pages.push(1)
    if (cur > 3) pages.push('...')
    const start = Math.max(2, cur - 1)
    const end = Math.min(total - 1, cur + 1)
    for (let i = start; i <= end; i++) pages.push(i)
    if (cur < total - 2) pages.push('...')
    pages.push(total)
    return pages
})

fetchData()
</script>
<style scoped>
/* 导出按钮 */
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
    text-decoration: none;
}
.btn-primary:hover {
    background: linear-gradient(135deg, #1976D2 0%, #0D47A1 100%);
    transform: translateY(-1px);
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3);
}

/* 表格外壳 */
.table-wrapper {
    background: rgba(0, 0, 0, 0.15);
    border-radius: 8px;
    overflow: hidden;
    border: 1px solid rgba(255, 255, 255, 0.08);
}

/* 数据表格 */
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

/* 表头徽章（列标题） */
.badge {
    display: inline-block;
    padding: 2px 10px;
    border-radius: 10px;
    font-size: 12px;
    font-weight: 600;
}
.badge--info {
    background: rgba(30, 136, 229, 0.3);
    color: #90CAF9;
    border: 1px solid rgba(30, 136, 229, 0.5);
}
.badge--warning {
    background: rgba(251, 140, 0, 0.3);
    color: #FFCC80;
    border: 1px solid rgba(251, 140, 0, 0.5);
}
.badge--danger {
    background: rgba(229, 57, 53, 0.3);
    color: #EF9A9A;
    border: 1px solid rgba(229, 57, 53, 0.5);
}
.badge--purple {
    background: rgba(142, 36, 170, 0.3);
    color: #CE93D8;
    border: 1px solid rgba(142, 36, 170, 0.5);
}
.badge--yellow {
    background: rgba(253, 216, 53, 0.25);
    color: #FFF59D;
    border: 1px solid rgba(253, 216, 53, 0.5);
}

/* 单元格数值徽章 */
.num-badge {
    display: inline-block;
    min-width: 32px;
    padding: 3px 10px;
    border-radius: 12px;
    font-size: 13px;
    font-weight: 700;
    text-align: center;
}
.num-badge--info {
    background: rgba(30, 136, 229, 0.25);
    color: #90CAF9;
    border: 1px solid rgba(30, 136, 229, 0.4);
}
.num-badge--warning {
    background: rgba(251, 140, 0, 0.25);
    color: #FFCC80;
    border: 1px solid rgba(251, 140, 0, 0.4);
}
.num-badge--danger {
    background: rgba(229, 57, 53, 0.25);
    color: #EF9A9A;
    border: 1px solid rgba(229, 57, 53, 0.4);
}
.num-badge--purple {
    background: rgba(142, 36, 170, 0.25);
    color: #CE93D8;
    border: 1px solid rgba(142, 36, 170, 0.4);
}
.num-badge--yellow {
    background: rgba(253, 216, 53, 0.2);
    color: #FFF59D;
    border: 1px solid rgba(253, 216, 53, 0.4);
}

/* 操作按钮 */
.detail-btn {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 5px 12px;
    background: #1E88E5;
    color: white;
    border: 1px solid rgba(255, 255, 255, 0.15);
    border-radius: 4px;
    font-size: 13px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s ease;
}
.detail-btn:hover {
    background: #1976D2;
    border-color: rgba(255, 255, 255, 0.3);
    transform: translateY(-1px);
}

/* 分页按钮 */
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
