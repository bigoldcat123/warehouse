<script setup lang="ts">
import { ref } from 'vue'
import { useCurrentUserStore } from '@/stores/currentUser';
import { useRouter } from 'vue-router';
import auth from '@/api/auth';
import server from '@/api';
import {
    Menu as IconMenu,
    Location,
    Setting,
    Avatar,
    Bell,
    Box,
    Flag,
    EditPen,
    CloseBold,
    VideoCamera,
    PictureFilled,
    ArrowLeft
} from '@element-plus/icons-vue'
import ChangePasswd from './ChangePasswd.vue';
import { ElTag } from 'element-plus';
import warehouse from '@/api/warehouse';
import { useCurrentWareHouse } from '@/stores/currentWareHouse';
const router = useRouter()
const currentWareHouse = useCurrentWareHouse()
const currentUser = useCurrentUserStore()
const SPECIAL_NAME = import.meta.env.ENV_SPECIAL_USER;
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
        <!-- 左侧深色侧边栏 -->
        <aside class="sidebar">
            <div class="sidebar__brand py-3">
                <img class="object-contain" src="/banner.jpg" alt="logo">
            </div>
            <el-menu class="sidebar-menu" :default-active="'/house'" :router="true"
                background-color="transparent" text-color="#b0d0f0" active-text-color="#ffffff">
                <el-menu-item v-if="currentUser.getUserDetail()?.username == SPECIAL_NAME" index="/panel">
                    <el-icon>
                        <ArrowLeft />
                    </el-icon>
                    <span>回首页</span>
                </el-menu-item>

                <el-menu-item index="/house">
                    <el-icon>
                        <Location />
                    </el-icon>
                    <span>仓房管理</span>
                </el-menu-item>

                <el-menu-item index="/wind">
                    <el-icon>
                        <VideoCamera />
                    </el-icon>
                    <span>通风状态</span>
                </el-menu-item>
                <el-menu-item index="/yuntu">
                    <el-icon>
                        <PictureFilled />
                    </el-icon>
                    <span>云图播放</span>
                </el-menu-item>

                <el-menu-item v-if="currentUser.isMainComp()" index="/alarmarg">
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

                <el-menu-item index="/data">
                    <el-icon>
                        <IconMenu />
                    </el-icon>
                    <span>数据查看</span>
                </el-menu-item>
                <el-menu-item index="/entry">
                    <el-icon>
                        <setting />
                    </el-icon>
                    <span>仓房入库管理</span>
                </el-menu-item>
                <el-menu-item v-if="currentUser.getUserDetail()?.username == 'admin'" index="/warehouse">
                    <el-icon>
                        <Box />
                    </el-icon>
                    <span>仓库管理</span>
                </el-menu-item>


                <el-menu-item v-if="currentUser.getUserDetail()?.username == 'admin'" index="/user">
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
                    <svg class="w-4 h-4 text-[#b0d0f0]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                    </svg>
                    <span class="text-white text-sm font-semibold">
                        {{ currentUser.getUserDetail()?.name }}
                    </span>
                    <span class="text-[#b0d0f0] text-sm">您好！</span>

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
    background: #1a3a5c;
    overflow-x: auto;
}

/* 侧边栏 */
.sidebar {
    width: 180px;
    min-width: 180px;
    height: 100vh;
    background: linear-gradient(180deg, #1a3a5c 0%, #0f2440 100%);
    border-right: 1px solid rgba(255, 255, 255, 0.08);
    display: flex;
    flex-direction: column;
    overflow-y: auto;
}
.sidebar__brand {
    padding: 12px 16px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

/* 主区域 */
.main-area {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-height: 100vh;
    background: #1a3a5c;
}

/* 顶部导航条 */
.topbar {
    height: 80px;
    background: linear-gradient(135deg, #1E88E5 0%, #1565C0 100%);
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    position: relative;
    display: flex;
    align-items: center;
    padding: 0 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
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
    background: rgba(255, 255, 255, 0.15);
    color: white;
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
    background: rgba(255, 255, 255, 0.2);
    color: white;
    border: 1px solid rgba(255, 255, 255, 0.3);
}
.topbar__btn--primary:hover {
    background: rgba(255, 255, 255, 0.3);
}
.topbar__btn--danger {
    background: #E53935;
    color: white;
}
.topbar__btn--danger:hover {
    background: #C62828;
}

/* 内容区 */
.content-area {
    flex: 1;
    padding: 20px 24px;
    background: #1a3a5c;
}
</style>
<style>
/* 侧边栏菜单深色主题 */
.sidebar-menu.el-menu {
    background: transparent !important;
    border-right: none !important;
    padding: 8px 0;
}
.sidebar-menu .el-menu-item {
    color: #b0d0f0 !important;
    background: transparent !important;
    border-left: 3px solid transparent;
    margin: 2px 0;
    transition: all 0.2s ease;
}
.sidebar-menu .el-menu-item:hover {
    color: white !important;
    background: rgba(255, 255, 255, 0.06) !important;
}
.sidebar-menu .el-menu-item.is-active {
    color: white !important;
    background: linear-gradient(90deg, rgba(30, 136, 229, 0.35) 0%, rgba(30, 136, 229, 0.1) 100%) !important;
    border-left-color: #1E88E5 !important;
    font-weight: 600;
}
.sidebar-menu .el-menu-item .el-icon {
    color: #64b5f6;
    margin-right: 8px;
}
.sidebar-menu .el-menu-item.is-active .el-icon {
    color: #ffffff;
}
</style>
