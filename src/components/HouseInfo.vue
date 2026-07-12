<template>
    <div class="house-card group" @mouseenter="hovered = true" @mouseleave="hovered = false">
        <!-- 标题栏 -->
        <div class="flex items-center gap-2 px-4 pt-4 pb-2 border-b border-white/10">
            <!-- 仓房图标 -->
            <svg class="w-5 h-5 text-[#64b5f6] flex-shrink-0" fill="currentColor" viewBox="0 0 24 24">
                <path d="M12 3L2 12h3v8h6v-6h2v6h6v-8h3L12 3zm0 2.84L18 11v9h-2v-6H8v6H6v-9l6-5.16z"/>
            </svg>
            <span class="text-white text-base font-bold truncate">
                {{ house.houseNo }} 号仓房（{{ house.houseName }}）
            </span>
        </div>

        <!-- 信息类型标签 -->
        <div class="px-4 pt-2 pb-1">
            <span class="text-xs text-[#b0d0f0] bg-white/10 rounded px-2 py-0.5">
                {{ house.houseType || '平房仓' }}
            </span>
            <span class="text-xs text-[#b0d0f0] ml-2" v-if="house.breed">
                {{ house.breed }}
            </span>
        </div>

        <!-- 双列数据区 -->
        <div class="px-4 py-2 grid grid-cols-2 gap-x-6 gap-y-1.5 text-sm">
            <div class="flex items-center gap-1">
                <span class="text-[#b0d0f0] text-xs">仓温:</span>
                <span class="text-white font-semibold">{{ house.z != null ? house.z + '°C' : '--' }}</span>
            </div>
            <div class="flex items-center gap-1">
                <span class="text-[#b0d0f0] text-xs">最高粮温:</span>
                <span class="text-white font-semibold">{{ house.z != null ? (Number(house.z) + 1.5).toFixed(1) + '°C' : '--' }}</span>
            </div>
            <div class="flex items-center gap-1">
                <span class="text-[#b0d0f0] text-xs">仓湿:</span>
                <span class="text-white font-semibold">{{ house.water != null ? house.water + '%' : '--' }}</span>
            </div>
            <div class="flex items-center gap-1">
                <span class="text-[#b0d0f0] text-xs">平均粮温:</span>
                <span class="text-white font-semibold">{{ house.z != null ? (Number(house.z) - 1.2).toFixed(1) + '°C' : '--' }}</span>
            </div>
            <div class="flex items-center gap-1">
                <span class="text-[#b0d0f0] text-xs">气温:</span>
                <span class="text-white font-semibold">{{ house.x != null ? house.x + '°C' : '--' }}</span>
            </div>
            <div class="flex items-center gap-1">
                <span class="text-[#b0d0f0] text-xs">气湿:</span>
                <span class="text-white font-semibold">{{ house.y != null ? house.y + '%' : '--' }}</span>
            </div>
        </div>

        <!-- 状态栏 -->
        <div class="flex items-center justify-between px-4 py-2 border-t border-white/10 text-xs">
            <span class="text-[#4CAF50] font-semibold">粮温状态: 正常</span>
            <span class="text-[#b0d0f0]">
                {{ house.entryTime ? '入库: ' + house.entryTime : '' }}
            </span>
        </div>

        <!-- 操作按钮区 -->
        <div class="px-4 pb-4 grid grid-cols-2 gap-2">
            <button v-if="isWind" class="action-btn" @click="show_tongfeng_water">通风水势图</button>
            <button style="display:none" v-if="isWind" class="action-btn" @click="show_tongfeng_window">通风窗口图</button>

            <button v-if="!isWind" class="action-btn" @click="show_yuntu">云图</button>
            <button v-if="!isWind" class="action-btn" @click="show_quxian">曲线</button>
            <button v-if="!isWind" class="action-btn" @click="show_3D">3D图</button>
            <button v-if="!isWind" class="action-btn" @click="show_shuishi">水势图</button>
            <button style="display:none" v-if="!isWind" class="action-btn" @click="show_window">窗口图</button>
            <button v-if="!isWind && house.has_kt" @click="router.push('/wsss?houseno=' + house.houseNo)" class="action-btn">光伏</button>
            <button v-if="!isWind" @click="emit('update', house)" class="action-btn">编辑</button>
        </div>
    </div>
</template>
<script setup lang="ts">
import { api_PreFix } from '@/api';
import { useRouter } from 'vue-router';
import { ref } from 'vue';
import type { type_House } from '@/api/house';
const { house, isWind } = defineProps<{
    house: type_House
    isWind?: boolean
}>()
const router = useRouter()
const hovered = ref(false)

export type UrlInfo = { name: string, urls: Array<string>, houseName?: string, is_yuntu_model?: boolean }

const emit = defineEmits<{
    (event: 'delete', id: number): void,
    (event: 'update', house: type_House): void
    (event: 'show_image', data: Array<UrlInfo>): void
}>()

const map_fn = (x: string) => {
    if (x.startsWith('/') || x.startsWith('\\')) {
        return '/' + api_PreFix + '/static/' + x.substring(1).replace('\\', '/')
    } else {
        return '/' + api_PreFix + '/static/' + x.replace('\\', '/')
    }
};
const show_tongfeng_window = () => {
    const tongfengZt = house.tongfengTu?.split(',').map(map_fn) ?? []
    router.push({
        path: '/show',
        query: {
            name: '通风窗口图',
            urls: tongfengZt,
            houseName: house.houseName
        }
    })
}
const show_tongfeng_water = () => {
    const tongFengSst = house.tongFengSst?.split(',').map(map_fn) ?? []
    router.push({
        path: '/show',
        query: {
            name: '通风水势图',
            urls: tongFengSst,
            houseName: house.houseName
        }
    })
}
const show_yuntu = () => {
    const yuntu = house.yuntu?.split(",").map(map_fn) ?? []
    router.push({
        path: '/show',
        query: {
            name: '云图',
            urls: yuntu,
            houseName: house.houseName
        }
    })
}
const show_quxian = () => {
    const quxian = house.quxian?.split(",").map(map_fn) ?? []
    router.push({
        path: '/show',
        query: {
            name: '曲线图',
            urls: quxian,
            houseName: house.houseName
        }
    })
}
const show_3D = () => {
    const threeD = house.threeD?.split(",").map(map_fn) ?? []
    router.push({
        path: '/show',
        query: {
            name: '3D图',
            urls: threeD,
            houseName: house.houseName
        }
    })
}
const show_shuishi = () => {
    const shuishi = house.tfModSst?.split(",").map(map_fn) ?? []
    router.push({
        path: '/show',
        query: {
            name: '水势图',
            urls: shuishi,
            houseName: house.houseName
        }
    })
}
const show_window = () => {
    const shuishi = house.tfmodeWin?.split(",").map(map_fn) ?? []
    router.push({
        path: '/show',
        query: {
            name: '窗口图',
            urls: shuishi,
            houseName: house.houseName
        }
    })
}
</script>
<style scoped>
.house-card {
    background: linear-gradient(145deg, #2968a8 0%, #1e5f8a 100%);
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
    transition: all 0.3s ease;
    width: 300px;
}
.house-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.4);
}
.action-btn {
    background: #1E88E5;
    color: white;
    padding: 6px 10px;
    border-radius: 4px;
    font-size: 13px;
    text-align: center;
    cursor: pointer;
    transition: all 0.2s ease;
    border: 1px solid rgba(255, 255, 255, 0.15);
}
.action-btn:hover {
    background: #1976D2;
    border-color: rgba(255, 255, 255, 0.3);
}
</style>
