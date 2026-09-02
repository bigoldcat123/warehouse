<template>
    <div class="house-card">
        <!-- 标题栏 -->
        <div
            class="flex items-center gap-2 px-4 pt-4 pb-2 border-b border-white/10"
        >
            <svg
                class="w-5 h-5 text-[#64b5f6] flex-shrink-0"
                fill="currentColor"
                viewBox="0 0 24 24"
            >
                <path
                    d="M12 3L2 12h3v8h6v-6h2v6h6v-8h3L12 3zm0 2.84L18 11v9h-2v-6H8v6H6v-9l6-5.16z"
                />
            </svg>
            <span class="text-white text-base font-bold truncate">
                {{ house.houseNo }} 号仓房（{{ house.houseName }}）
            </span>
        </div>

        <!-- 基础信息（只展示种类、水分、入库时间） -->
        <div class="px-4 py-3 flex flex-col gap-y-1.5 text-sm">
            <div class="flex items-center gap-2">
                <span class="text-[#b0d0f0] text-xs">种类:</span>
                <span class="text-white font-semibold">{{
                    house.breed || "--"
                }}</span>
            </div>
            <div class="flex items-center gap-2">
                <span class="text-[#b0d0f0] text-xs">水分:</span>
                <span class="text-white font-semibold">{{
                    house.water != null ? house.water + "%" : "--"
                }}</span>
            </div>
            <div class="flex items-center gap-2">
                <span class="text-[#b0d0f0] text-xs">入库时间:</span>
                <span class="text-white font-semibold">{{
                    house.entryTime || "--"
                }}</span>
            </div>
        </div>

        <!-- 操作按钮区 -->
        <div class="px-4 pb-4 grid grid-cols-2 gap-2">
            <!-- <button
                v-if="isWind"
                class="action-btn"
                @click="show_tongfeng_water"
            >
                通风水势图
            </button>
            <button
                style="display: none"
                v-if="isWind"
                class="action-btn"
                @click="show_tongfeng_window"
            >
                通风窗口图
            </button>

            <button v-if="!isWind" class="action-btn" @click="show_yuntu">
                云图
            </button>
            <button v-if="!isWind" class="action-btn" @click="show_quxian">
                曲线
            </button>
            <button v-if="!isWind" class="action-btn" @click="show_3D">
                3D图
            </button>
            <button v-if="!isWind" class="action-btn" @click="show_shuishi">
                水势图
            </button>
            <button
                style="display: none"
                v-if="!isWind"
                class="action-btn"
                @click="show_window"
            >
                窗口图
            </button>
            <button
                v-if="!isWind && house.has_kt"
                @click="router.push('/wsss?houseno=' + house.houseNo)"
                class="action-btn"
            >
                光伏
            </button>

            <button @click="show_dynamic_yuntu" class="action-btn">
                动态云图
            </button>
            <button @click="show_3d" class="action-btn">动态3D图</button>
            -->
            <button class="action-btn">温度剖面图</button>
            <button @click="show_3d_temp" class="action-btn">3D温度图</button>

            <button class="action-btn">湿度剖面图</button>
            <button @click="show_humidity3d" class="action-btn">
                3D湿度图
            </button>

            <button class="action-btn">气体浓度剖面图</button>
            <button @click="show_gas3d" class="action-btn">3D气体浓度</button>
            <button
                @click="
                    router.push({
                        path: '/tempLine',
                        query: {
                            houseNo: house.houseNo,
                            houseName: house.houseName,
                            x: house.x,
                            y: house.y,
                            z: house.z,
                        },
                    })
                "
                class="action-btn"
            >
                气体浓度
            </button>
            <button
                v-if="!isWind && !currentUser.isGuest()"
                @click="emit('update', house)"
                class="action-btn"
            >
                编辑
            </button>
        </div>
    </div>
</template>
<script setup lang="ts">
import { api_PreFix } from "@/api";
import { useRouter } from "vue-router";
import type { type_House } from "@/api/house";
import houseApi from "@/api/house";
import { useCurrentWareHouse } from "@/stores/currentWareHouse";
import { useCurrentUserStore } from "@/stores/currentUser";
const w = useCurrentWareHouse();
const currentUser = useCurrentUserStore();

const { house, isWind } = defineProps<{
    house: type_House;
    isWind?: boolean;
}>();
const router = useRouter();

export type UrlInfo = {
    name: string;
    urls: Array<string>;
    houseName?: string;
    is_yuntu_model?: boolean;
};

const emit = defineEmits<{
    (event: "delete", id: number): void;
    (event: "update", house: type_House): void;
    (event: "show_image", data: Array<UrlInfo>): void;
}>();

const map_fn = (x: string) => {
    if (x.startsWith("/") || x.startsWith("\\")) {
        return (
            "/" + api_PreFix + "/static/" + x.substring(1).replace("\\", "/")
        );
    } else {
        return "/" + api_PreFix + "/static/" + x.replace("\\", "/");
    }
};
const show_tongfeng_window = () => {
    const tongfengZt = house.tongfengTu?.split(",").map(map_fn) ?? [];
    router.push({
        path: "/show",
        query: {
            name: "通风窗口图",
            urls: tongfengZt,
            houseName: house.houseName,
        },
    });
};
const show_3d = async () => {
    console.log(w.getWareHouse());
    console.log(house);
    const res = await houseApi.getImageUrl(
        "threeD",
        w.getWareHouse().wareHouseNO ?? "",
        house.houseNo,
    );

    router.push({
        path: "/showCarousel",
        query: {
            name: "动态3D图",
            urls: res.data.value,
            houseName: house.houseName,
        },
    });
};
const show_dynamic_yuntu = async () => {
    const res = await houseApi.getImageUrl(
        "mlcc",
        w.getWareHouse().wareHouseNO ?? "",
        house.houseNo,
    );

    router.push({
        path: "/showCarousel",
        query: {
            name: "动态云图",
            urls: res.data.value,
            houseName: house.houseName,
        },
    });
};
const show_tongfeng_water = () => {
    const tongFengSst = house.tongFengSst?.split(",").map(map_fn) ?? [];
    router.push({
        path: "/show",
        query: {
            name: "通风水势图",
            urls: tongFengSst,
            houseName: house.houseName,
        },
    });
};
const show_yuntu = () => {
    const yuntu = house.yuntu?.split(",").map(map_fn) ?? [];
    router.push({
        path: "/show",
        query: {
            name: "云图",
            urls: yuntu,
            houseName: house.houseName,
        },
    });
};
const show_quxian = () => {
    const quxian = house.quxian?.split(",").map(map_fn) ?? [];
    router.push({
        path: "/show",
        query: {
            name: "曲线图",
            urls: quxian,
            houseName: house.houseName,
        },
    });
};
const show_3D = () => {
    const threeD = house.threeD?.split(",").map(map_fn) ?? [];
    router.push({
        path: "/show",
        query: {
            name: "3D图",
            urls: threeD,
            houseName: house.houseName,
        },
    });
};
const show_3d_temp = () => {
    router.push({
        path: "/granary3d",
        query: {
            houseNo: house.houseNo,
            houseName: house.houseName,
        },
    });
};
const show_humidity3d = () => {
    router.push({
        path: "/humidity3d",
        query: {
            houseNo: house.houseNo,
            houseName: house.houseName,
        },
    });
};
const show_gas3d = () => {
    router.push({
        path: "/gas3d",
        query: {
            houseNo: house.houseNo,
            houseName: house.houseName,
        },
    });
};
const show_shuishi = () => {
    const shuishi = house.tfModSst?.split(",").map(map_fn) ?? [];
    router.push({
        path: "/show",
        query: {
            name: "水势图",
            urls: shuishi,
            houseName: house.houseName,
        },
    });
};
const show_window = () => {
    const shuishi = house.tfmodeWin?.split(",").map(map_fn) ?? [];
    router.push({
        path: "/show",
        query: {
            name: "窗口图",
            urls: shuishi,
            houseName: house.houseName,
        },
    });
};
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
    background: #1e88e5;
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
    background: #1976d2;
    border-color: rgba(255, 255, 255, 0.3);
}
</style>
