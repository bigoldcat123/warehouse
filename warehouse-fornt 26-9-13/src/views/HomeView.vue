<script setup lang="ts">
import { ref } from 'vue'
import { useCurrentUserStore } from '@/stores/currentUser';
import { useRouter } from 'vue-router';
import auth from '@/api/auth';
import server from '@/api';
import {
    Avatar,
    Bell,
    Box,
    Flag,
    EditPen,
    ArrowLeft,
    Odometer
} from '@element-plus/icons-vue'
import ChangePasswd from './ChangePasswd.vue';
import { ElTag } from 'element-plus';
import warehouse from '@/api/warehouse';
import { useCurrentWareHouse } from '@/stores/currentWareHouse';
const router = useRouter()
const currentWareHouse = useCurrentWareHouse()
const currentUser = useCurrentUserStore()
const kv = ref<any[]>([{
    value: '分公司',
    key: -1
}])
warehouse.kv().then(res => {
    kv.value.push(...res.data.value)
})
function logout() {

    auth.logout().then(res => {
        currentUser.logout()
        currentWareHouse.clear()
        router.push({ path: '/login' })
    })
}
const v = ref(false)
</script>

<template>
    <div class="app-layout">
        <!-- 左侧管理导航 -->
        <aside class="sidebar">
            <div class="sidebar__brand py-3">
                <img class="object-contain" src="/banner1.png" alt="logo">
            </div>
            <el-menu class="sidebar-menu" :default-active="$route.path" :router="true"
                background-color="transparent" text-color="#b0d0f0" active-text-color="#ffffff">
                <el-menu-item index="/warehousePanorama">
                    <el-icon>
                        <ArrowLeft />
                    </el-icon>
                    <span>回首页</span>
                </el-menu-item>

                <el-menu-item index="/alarmarg">
                    <el-icon>
                        <Flag />
                    </el-icon>
                    <span>报警统计</span>
                </el-menu-item>

                <el-menu-item index="/alarm">
                    <el-icon>
                        <Bell />
                    </el-icon>
                    <span>报警信息</span>
                </el-menu-item>

                <el-menu-item index="/warehouseSettings">
                    <el-icon>
                        <EditPen />
                    </el-icon>
                    <span>基础信息设置</span>
                </el-menu-item>

                <el-menu-item index="/xunzhengSettings">
                    <el-icon>
                        <Odometer />
                    </el-icon>
                    <span>熏蒸设置</span>
                </el-menu-item>

                <el-menu-item index="/warehouse">
                    <el-icon>
                        <Box />
                    </el-icon>
                    <span>仓库管理</span>
                </el-menu-item>


                <el-menu-item index="/user">
                    <el-icon>
                        <Avatar />
                    </el-icon>
                    <span>用户管理</span>
                </el-menu-item>
            </el-menu>
        </aside>

        <!-- 右侧主区域 -->
        <main class="main-area">
            <!-- 顶部导航条 -->
            <header class="topbar">
                <div class="topbar__logo">
                    <img src="/banner2.png" alt="banner">
                </div>
                <div class="topbar__user">
                    <span class="topbar__company">
                        {{ kv.filter(x => x.key == currentUser.getUserDetail()!.companyID)[0]?.value }}
                    </span>
                    <svg class="w-4 h-4 text-[#5f7f98]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                    </svg>
                    <span class="text-[#29465d] text-sm font-semibold">
                        {{ currentUser.getUserDetail()?.name }}
                    </span>
                    <span class="text-[#6b879d] text-sm">您好！</span>

                    <button class="topbar__btn topbar__btn--primary" @click="v = true">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                        </svg>
                        修改密码
                    </button>
                    <button class="topbar__btn topbar__btn--danger" @click="logout">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                        </svg>
                        退出
                    </button>
                </div>
            </header>

            <div class="content-area">
                <RouterView />
            </div>
        </main>
    </div>
    <ChangePasswd :dialog-visible="v" @close="v = false"></ChangePasswd>
</template>
<style scoped>
.app-layout {
    display: flex;
    width: 100%;
    min-height: 100vh;
    background: #f4f8fb;
    overflow-x: auto;
}

/* 侧边栏 */
.sidebar {
    width: 180px;
    min-width: 180px;
    height: 100vh;
    background: linear-gradient(180deg, #f4faff 0%, #e8f3fb 100%);
    border-right: 1px solid #d5e5f1;
    display: flex;
    flex-direction: column;
    overflow-y: auto;
}
.sidebar__brand {
    padding: 12px 16px;
    background: #ffffff;
    border-bottom: 1px solid #dceaf4;
}

/* 主区域 */
.main-area {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-height: 100vh;
    background: #f4f8fb;
}

/* 顶部导航条 */
.topbar {
    height: 80px;
    background: linear-gradient(135deg, #ffffff 0%, #edf7ff 100%);
    border-bottom: 1px solid #d5e5f1;
    position: relative;
    display: flex;
    align-items: center;
    padding: 0 24px;
    box-shadow: 0 2px 10px rgba(62, 105, 137, 0.08);
}
.topbar__logo {
    height: 60px;
    width: 400px;
}
.topbar__logo img {
    height: 100%;
    object-fit: contain;
}
.topbar__user {
    margin-left: auto;
    display: flex;
    align-items: center;
    gap: 12px;
}
.topbar__company {
    background: #e4f2fc;
    color: #356887;
    border: 1px solid #c8e0f1;
    padding: 4px 10px;
    border-radius: 4px;
    font-size: 13px;
    font-weight: 500;
}
.topbar__btn {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 6px 12px;
    border-radius: 4px;
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s ease;
    border: none;
}
.topbar__btn--primary {
    background: #e5f2fc;
    color: #28688f;
    border: 1px solid #c3deef;
}
.topbar__btn--primary:hover {
    background: #d6eafa;
    border-color: #9ec8e2;
}
.topbar__btn--danger {
    background: #fff1f0;
    color: #c94b48;
    border: 1px solid #f3c5c3;
}
.topbar__btn--danger:hover {
    background: #ffe4e2;
}

/* 内容区 */
.content-area {
    flex: 1;
    padding: 20px 24px;
    background: #f4f8fb;
}
</style>
<style>
/* 侧边栏办公主题 */
.sidebar-menu.el-menu {
    background: transparent !important;
    border-right: none !important;
    padding: 8px 0;
}
.sidebar-menu .el-menu-item {
    color: #527189 !important;
    background: transparent !important;
    border-left: 3px solid transparent;
    margin: 2px 0;
    transition: all 0.2s ease;
}
.sidebar-menu .el-menu-item:hover {
    color: #236b98 !important;
    background: #deeffb !important;
}
.sidebar-menu .el-menu-item.is-active {
    color: #176795 !important;
    background: linear-gradient(90deg, #d9edfb 0%, #eef8ff 100%) !important;
    border-left-color: #4ba3d3 !important;
    font-weight: 600;
}
.sidebar-menu .el-menu-item .el-icon {
    color: #6a9fbe;
    margin-right: 8px;
}
.sidebar-menu .el-menu-item.is-active .el-icon {
    color: #2785b8;
}
.sidebar-menu .el-menu-item.is-disabled {
    color: #a8bac7 !important;
    cursor: not-allowed;
    opacity: 0.6;
}
.sidebar-menu .el-menu-item.is-disabled .el-icon {
    color: #afc3d1;
}

/* 后台子页面统一使用白色 + 淡蓝色办公主题 */
.app-layout .table-wrapper {
    background: #ffffff !important;
    border: 1px solid #d9e7f1 !important;
    box-shadow: 0 6px 20px rgba(55, 96, 126, 0.06);
}

.app-layout .data-table thead {
    background: #eaf4fb !important;
}

.app-layout .data-table thead th {
    color: #35566f !important;
    border-bottom: 1px solid #d4e5f0 !important;
}

.app-layout .data-table tbody tr,
.app-layout .data-table tbody tr:nth-child(even) {
    background: #ffffff !important;
}

.app-layout .data-table tbody tr:nth-child(even) {
    background: #f8fbfd !important;
}

.app-layout .data-table tbody tr:hover {
    background: #eef7fd !important;
}

.app-layout .data-table tbody td,
.app-layout .data-table tbody td.text-white,
.app-layout .data-table tbody td.text-\[\#b0d0f0\] {
    color: #3d5669 !important;
    border-bottom: 1px solid #e5eef4 !important;
}

.app-layout .text-white:not(.action-btn):not(.btn-primary):not(.btn-success),
.app-layout h2.text-white,
.app-layout .text-\[\#b0d0f0\] {
    color: #35566f !important;
}

.app-layout .text-\[\#64b5f6\] {
    color: #438fbb !important;
}

.app-layout .filter-bar,
.app-layout .toolbar {
    color: #49677e;
}

.app-layout .filter-bar {
    background: #ffffff !important;
    border: 1px solid #d9e7f1 !important;
    box-shadow: 0 4px 14px rgba(55, 96, 126, 0.05) !important;
}

.app-layout .custom-input,
.app-layout .custom-select,
.app-layout .dark-input,
.app-layout .dark-select,
.app-layout .dark-textarea {
    background: #ffffff !important;
    color: #334e62 !important;
    border-color: #c9dce9 !important;
}

.app-layout .custom-input::placeholder,
.app-layout .dark-input::placeholder,
.app-layout .dark-textarea::placeholder {
    color: #91a6b5 !important;
}

.app-layout .page-btn,
.app-layout .pagination button {
    background: #ffffff !important;
    color: #527189 !important;
    border-color: #cddfea !important;
}

.app-layout .page-btn:hover:not(:disabled),
.app-layout .pagination button:hover:not(:disabled) {
    background: #eaf4fb !important;
    color: #2579a9 !important;
    border-color: #9fc9e2 !important;
}

.app-layout .page-btn--active {
    background: #4b9dca !important;
    color: #ffffff !important;
    border-color: #4b9dca !important;
}

.app-layout .warehouse-label,
.app-layout .pagination,
.app-layout .empty {
    color: #6f889a !important;
}

.app-layout .el-input__wrapper,
.app-layout .el-select__wrapper,
.app-layout .el-textarea__inner {
    background: #ffffff !important;
    box-shadow: 0 0 0 1px #c9dce9 inset !important;
}

.app-layout .el-input__inner,
.app-layout .el-select__selected-item,
.app-layout .el-textarea__inner {
    color: #334e62 !important;
}

/* 弹窗会挂载到 body，需单独覆盖原深色样式 */
body .dark-dialog.el-dialog {
    background: #ffffff !important;
    border: 1px solid #d5e5f1 !important;
    box-shadow: 0 18px 46px rgba(42, 77, 104, 0.18) !important;
}

body .dark-dialog .dialog-header {
    background: linear-gradient(135deg, #edf7ff 0%, #dceefa 100%) !important;
    border-bottom: 1px solid #cfe2ef !important;
}

body .dark-dialog .dialog-header .text-white,
body .dark-dialog .dialog-header h2 {
    color: #294e67 !important;
}

body .dark-dialog .dialog-body,
body .dark-dialog .el-dialog__body {
    background: #ffffff !important;
    color: #3d5669 !important;
}

body .dark-dialog .dark-form .el-form-item__label {
    color: #49677e !important;
}

body .dark-dialog .el-input__wrapper,
body .dark-dialog .el-select__wrapper,
body .dark-dialog .el-textarea__inner {
    background: #f8fbfd !important;
    box-shadow: 0 0 0 1px #c9dce9 inset !important;
}

body .dark-dialog .el-input__inner,
body .dark-dialog .el-select__selected-item,
body .dark-dialog .el-textarea__inner {
    color: #334e62 !important;
}

body .dark-dialog .btn-cancel {
    background: #eef5fa !important;
    color: #49677e !important;
    border-color: #c9dce9 !important;
}

body .el-popconfirm.el-popover {
    background: #ffffff !important;
    border-color: #d5e5f1 !important;
    box-shadow: 0 10px 28px rgba(42, 77, 104, 0.15) !important;
}

body .el-popconfirm .el-popconfirm__main {
    color: #3d5669 !important;
}
</style>
