<template>
    <Binner />

    <!-- 筛选栏 -->
    <div class="filter-bar">
        <!-- 报警级别 -->
        <div class="filter-section">
            <div class="filter-section__title">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
                报警级别
            </div>
            <div class="filter-section__options">
                <label class="pill-toggle" :class="{ 'pill-toggle--active': levels.includes('一般报警'), 'pill-toggle--disabled': !user.getPriv()?.includes('1') }">
                    <input type="checkbox" value="一般报警" v-model="levels" :disabled="!user.getPriv()?.includes('1')" class="pill-toggle__input" />
                    <span class="pill-toggle__text">一般报警</span>
                </label>
                <label class="pill-toggle" :class="{ 'pill-toggle--active': levels.includes('严重报警'), 'pill-toggle--danger': levels.includes('严重报警'), 'pill-toggle--disabled': !user.getPriv()?.includes('2') }">
                    <input type="checkbox" value="严重报警" v-model="levels" :disabled="!user.getPriv()?.includes('2')" class="pill-toggle__input" />
                    <span class="pill-toggle__text">严重报警</span>
                </label>
            </div>
        </div>

        <!-- 审核状态 -->
        <div class="filter-section filter-section--divider">
            <div class="filter-section__title">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                审核
            </div>
            <div class="filter-section__options">
                <label class="pill-toggle" :class="{ 'pill-toggle--active': verifies.includes('true'), 'pill-toggle--success': verifies.includes('true') }">
                    <input type="checkbox" value="true" v-model="verifies" class="pill-toggle__input" />
                    <span class="pill-toggle__text">已审核</span>
                </label>
                <label class="pill-toggle" :class="{ 'pill-toggle--active': verifies.includes('false'), 'pill-toggle--danger': verifies.includes('false') }">
                    <input type="checkbox" value="false" v-model="verifies" class="pill-toggle__input" />
                    <span class="pill-toggle__text">未审核</span>
                </label>
            </div>
        </div>

        <!-- 仓库选择 -->
        <div class="filter-section filter-section--divider">
            <div class="filter-section__title">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
                </svg>
                仓库
            </div>
            <div class="filter-section__options">
                <select v-model="warehouseID" class="custom-select">
                    <option v-for="item in kv" :key="item.value" :value="item.key">{{ item.value }}</option>
                </select>
            </div>
        </div>

        <!-- 操作按钮 -->
        <div class="flex items-center gap-2 ml-auto self-end">
            <button class="btn-success" @click="query">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                </svg>
                搜索
            </button>
            <a href="/api/alarm/excel" class="btn-primary">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
                </svg>
                导出
            </a>
        </div>
    </div>

    <!-- 数据表格 -->
    <div class="table-wrapper">
        <table class="data-table">
            <thead>
                <tr>
                    <th class="text-left">仓房编号</th>
                    <th class="text-center">等级</th>
                    <th class="text-left">类型</th>
                    <th class="text-left">时间</th>
                    <th class="text-center">云图</th>
                    <th class="text-center">审核</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="(row, idx) in alarmList?.records" :key="idx">
                    <td class="text-white font-medium">{{ row.houseNo }}</td>
                    <td class="text-center">
                        <span class="level-badge" :class="row.alertLevel === '一般报警' ? 'level-badge--warn' : 'level-badge--danger'">
                            {{ row.alertLevel }}
                        </span>
                    </td>
                    <td>
                        <span class="type-tag">{{ row.alertType || '--' }}</span>
                    </td>
                    <td class="text-[#b0d0f0]">{{ row.alertTime || '--' }}</td>
                    <td class="text-center">
                        <button class="action-btn action-btn--primary" @click="show_yuntu(row.yuntu)">
                            <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                            </svg>
                            查看
                        </button>
                    </td>
                    <td class="text-center">
                        <div class="flex items-center justify-center gap-2">
                            <span v-if="row.isVerify" class="verify-badge verify-badge--success">已审核</span>
                            <span v-else class="verify-badge verify-badge--danger">未审核</span>
                            <button v-if="row.isVerify" class="action-btn action-btn--info"
                                @click="showHandleDialog = true; currentHandle = row.handle">
                                <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                                </svg>
                                查看
                            </button>
                            <button v-else-if="!user.isGuest()" :disabled="!user.getPriv()?.includes('3')"
                                class="action-btn action-btn--success"
                                :class="{ 'action-btn--disabled': !user.getPriv()?.includes('3') }"
                                @click="handleDialog = true; currentid = row.id">
                                <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
                                </svg>
                                审核
                            </button>
                        </div>
                    </td>
                </tr>
                <tr v-if="!alarmList?.records || alarmList.records.length === 0">
                    <td colspan="6" class="text-center text-[#b0d0f0] py-10">
                        <svg class="w-10 h-10 mx-auto mb-2 opacity-40" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                        </svg>
                        暂无报警信息
                    </td>
                </tr>
            </tbody>
        </table>
    </div>

    <!-- 分页 -->
    <div v-if="alarmList?.pages && alarmList.pages > 1" class="flex items-center justify-center gap-2 mt-5">
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
            :class="{ 'page-btn--disabled': currentpage >= (alarmList?.pages || 1) }"
            :disabled="currentpage >= (alarmList?.pages || 1)"
            @click="pagechange(currentpage + 1)"
        >
            下一页
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
            </svg>
        </button>
    </div>

    <handle :id="currentid" :dialogVisible="handleDialog" @close="handleDialog = false" @refresh="fetchData"></handle>
    <ShowHandle :handle="currentHandle" :dialogVisible="showHandleDialog" @close="showHandleDialog = false">
    </ShowHandle>
    <ImageDialog :dialogVisible="dialog" :urlInfo="url_info" @close="dialog = false"></ImageDialog>
</template>
<script setup lang="ts">
import Binner from '@/components/common/Binner.vue';
import { ref, computed } from 'vue'
import alarm, { type type_Alarm } from '@/api/alarm';
import handle from './Handle.vue';
import ShowHandle from './ShowHandle.vue';
import { useCurrentUserStore } from '@/stores/currentUser';
import warehouse from '@/api/warehouse';
import { useRoute } from 'vue-router';
import { useCurrentWareHouse } from '@/stores/currentWareHouse';
import ImageDialog from '../house/ImageDialog.vue';
import type { UrlInfo } from '@/components/HouseInfo.vue';
import { api_PreFix } from '@/api';
const currentWareHouse = useCurrentWareHouse()
const route = useRoute()
const dialog = ref(false)
const url_info = ref<Array<UrlInfo> | undefined>()

console.log(route.query);

const levels = ref<string[]>([])
const types = ref<string[]>([])
const verifies = ref(<string[]>[])
const alarmList = ref<Page<type_Alarm>>()
const currentid = ref<number | undefined>(undefined)
const currentHandle = ref('')
const handleDialog = ref(false)
const showHandleDialog = ref(false)
const user = useCurrentUserStore()
const currentpage = ref(1)
const size = ref(10)
const kv = ref<any[]>([])
const binerProp_warehouseNO = ref('')
const binerProp_warehouseName = ref('')
const warehouseID = ref('')
const show_yuntu = (yt:string) => {
    const map_fn = (x: string) => {
        if (x.startsWith('/') || x.startsWith('\\')) {
            return '/' + api_PreFix + '/static/' + x.substring(1).replace('\\', '/')
        } else {
            return '/' + api_PreFix + '/static/' + x.replace('\\', '/')
        }
    };
    const yuntu = yt?.split(",").map(map_fn) ?? []
    url_info.value = [
        {
            name:'云图',
            urls:yuntu
        }
    ]
    dialog.value = true
}
warehouse.belongKv().then(res => {
    kv.value = res.data.value
    let item
    if (route.query.wareHouseNO) {
        item = kv.value.filter(x => x.no == route.query.wareHouseNO)[0]
        currentWareHouse.setWareHouse({
            wareHouseNO: item.no,
            wareHouseName: item.value,
            waerhouseId: item.key
        })
    } else {
        item = kv.value[0]
        currentWareHouse.init({
            wareHouseNO: item.no,
            wareHouseName: item.value,
            waerhouseId: item.key
        })
    }
    warehouseID.value = currentWareHouse.getWareHouse().waerhouseId as string
    fetchData()
})
function pagechange(page: number) {
    currentpage.value = page
    fetchData()
}

// 分页页码列表
const pageList = computed(() => {
    const total = alarmList.value?.pages || 1
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
    const item = kv.value.filter(x => x.key == warehouseID.value)[0]
    currentWareHouse.setWareHouse({
        wareHouseNO: item.no,
        wareHouseName: item.value,
        waerhouseId: item.key
    })
    fetchData()
}
function fetchData() {
    alarm.list(levels.value, types.value, verifies.value, warehouseID.value, currentpage.value, size.value).then(res => {
        alarmList.value = res.data.value
    })
}


</script>
<style scoped>
/* 筛选栏 */
.filter-bar {
    display: flex;
    align-items: flex-end;
    gap: 20px;
    flex-wrap: wrap;
    padding: 16px 20px;
    background: rgba(0, 0, 0, 0.15);
    border: 1px solid rgba(255, 255, 255, 0.08);
    border-radius: 8px;
    margin-bottom: 16px;
}
.filter-section {
    display: flex;
    flex-direction: column;
    gap: 6px;
}
.filter-section--divider {
    padding-left: 20px;
    border-left: 1px solid rgba(255, 255, 255, 0.1);
}
.filter-section__title {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 12px;
    color: #b0d0f0;
    font-weight: 500;
}
.filter-section__options {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
}

/* 胶囊开关 */
.pill-toggle {
    display: inline-flex;
    align-items: center;
    padding: 6px 14px;
    background: rgba(255, 255, 255, 0.06);
    border: 1px solid rgba(255, 255, 255, 0.15);
    border-radius: 16px;
    cursor: pointer;
    transition: all 0.2s ease;
    user-select: none;
}
.pill-toggle:hover:not(.pill-toggle--disabled) {
    background: rgba(255, 255, 255, 0.1);
    border-color: rgba(255, 255, 255, 0.3);
}
.pill-toggle__input {
    position: absolute;
    opacity: 0;
    pointer-events: none;
}
.pill-toggle__text {
    font-size: 13px;
    color: #b0d0f0;
    font-weight: 500;
}
.pill-toggle--active {
    background: rgba(30, 136, 229, 0.25);
    border-color: #1E88E5;
}
.pill-toggle--active .pill-toggle__text {
    color: white;
    font-weight: 600;
}
.pill-toggle--success {
    background: rgba(67, 160, 71, 0.25);
    border-color: #43A047;
}
.pill-toggle--danger {
    background: rgba(229, 57, 53, 0.25);
    border-color: #E53935;
}
.pill-toggle--disabled {
    opacity: 0.4;
    cursor: not-allowed;
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
    min-width: 180px;
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
.btn-primary {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 7px 16px;
    background: linear-gradient(135deg, #1E88E5 0%, #1565C0 100%);
    color: white;
    border: none;
    border-radius: 6px;
    font-size: 13px;
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

/* 等级徽章 */
.level-badge {
    display: inline-block;
    padding: 3px 10px;
    border-radius: 10px;
    font-size: 12px;
    font-weight: 600;
}
.level-badge--warn {
    background: rgba(251, 140, 0, 0.25);
    color: #FFCC80;
    border: 1px solid rgba(251, 140, 0, 0.5);
}
.level-badge--danger {
    background: rgba(229, 57, 53, 0.25);
    color: #EF9A9A;
    border: 1px solid rgba(229, 57, 53, 0.5);
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

/* 审核徽章 */
.verify-badge {
    display: inline-block;
    padding: 2px 8px;
    border-radius: 8px;
    font-size: 11px;
    font-weight: 600;
}
.verify-badge--success {
    background: rgba(67, 160, 71, 0.25);
    color: #A5D6A7;
    border: 1px solid rgba(67, 160, 71, 0.5);
}
.verify-badge--danger {
    background: rgba(229, 57, 53, 0.2);
    color: #EF9A9A;
    border: 1px solid rgba(229, 57, 53, 0.4);
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
.action-btn--info {
    background: rgba(30, 136, 229, 0.6);
    color: white;
    border-color: rgba(30, 136, 229, 0.5);
}
.action-btn--info:hover {
    background: #1E88E5;
    transform: translateY(-1px);
}
.action-btn--success {
    background: rgba(67, 160, 71, 0.7);
    color: white;
    border-color: rgba(67, 160, 71, 0.5);
}
.action-btn--success:hover {
    background: #43A047;
    transform: translateY(-1px);
}
.action-btn--disabled {
    opacity: 0.4;
    cursor: not-allowed;
}
.action-btn--disabled:hover {
    transform: none;
    background: rgba(67, 160, 71, 0.7);
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
