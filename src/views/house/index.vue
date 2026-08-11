<template>
    <Binner />

    <!-- 搜索工具栏 -->
    <div class="flex items-center gap-4 mb-5">
        <!-- 新增按钮 -->
        <button v-if="!currentUser.isGuest()" class="btn-primary" @click="addDialog = true">
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
            v-for="house in list?.records"
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
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { onBeforeRouteLeave } from 'vue-router'
import house, { type type_House } from '@/api/house';
import HouseInfo, { type UrlInfo } from '@/components/HouseInfo.vue';
import AddDialog from './AddDialog.vue';
import UpdateDialog from './UpdateDialog.vue';
import warehouse from '@/api/warehouse';
import { useCurrentUserStore } from '@/stores/currentUser';
import Binner from '@/components/common/Binner.vue';
import { useCurrentWareHouse } from '@/stores/currentWareHouse';
import ImageDialog from './ImageDialog.vue';
defineOptions({ name: 'HousePage' })

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
console.log("Hello")
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
        // 数据加载完成后恢复滚动位置
        const savedScroll = sessionStorage.getItem('house_scroll_position')
        if (savedScroll) {
            setTimeout(() => {
                window.scrollTo(0, parseInt(savedScroll))
                sessionStorage.removeItem('house_scroll_position')
            }, 100)
        }
    })
}

// 离开页面前保存滚动位置
onBeforeRouteLeave((to, from, next) => {
    const scrollPosition = window.scrollY || document.documentElement.scrollTop
    sessionStorage.setItem('house_scroll_position', scrollPosition.toString())
    next()
})
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
