<template>
    <Binner />
    <div class="mb-4">
        <el-button @click="addDialog = true" type="primary"><el-icon>
                <Plus />
            </el-icon>新增</el-button>
        <span class=" ml-16">
            <!-- <el-input v-model="warehouseName" class="max-w-32 m-2" placeholder="请输入仓库名" /> -->

            <el-select v-model="warehouseName" placeholder="请选择仓库名" style="width: 140px">
                <el-option v-for="item in kv" :key="item.value" :label="item.value" :value="item.key" />
            </el-select>

            <el-input v-model="houseNo" class="max-w-32 m-2" placeholder="请输入仓房编号" />
            <el-button type="success" @click="searchList"><el-icon>
                    <Search />
                </el-icon>搜索</el-button>
        </span>
    </div>
    <div>
        <div class=" flex flex-wrap gap-x-4 gap-y-4">
            <HouseInfo :key="house.id" v-for="house in list?.records" :house="house" @delete="handle_delete"
                @update="(house:type_House) => { current = house; updateDialog = true; }"
                @show_image="handle_show_iamge"></HouseInfo>
        </div>
        
        <!-- <el-pagination @current-change="pagechange" :default-page-size="size" :page-count="list?.pages"
            layout="prev, pager, next" /> -->
    </div>

    <AddDialog :kv="kv" :dialog-visible="addDialog" @refresh="fetchData" @close="addDialog = false"></AddDialog>
    <UpdateDialog :kv="kv" :house="current!" :dialog-visible="updateDialog" @refresh="fetchData"
        @close="updateDialog = false"></UpdateDialog>
    <ImageDialog :dialog-visible="imageDialog" @close="imageDialog = false" @refresh="fetchData" :urlInfo="urlInfo"></ImageDialog>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import house, { type type_House } from '@/api/house';
import HouseInfo, { type UrlInfo } from '@/components/HouseInfo.vue';
import AddDialog from './AddDialog.vue';
import UpdateDialog from './UpdateDialog.vue';
import warehouse from '@/api/warehouse';
import { useCurrentUserStore } from '@/stores/currentUser';
import Binner from '@/components/common/Binner.vue';
import { useCurrentWareHouse } from '@/stores/currentWareHouse';
import { Plus } from '@element-plus/icons-vue';
import { Search ,EditPen,Delete } from '@element-plus/icons-vue';
import ImageDialog from './ImageDialog.vue';
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
const e = '@#$%^&*()_+'
const warehouseName = ref('')
const houseNo = ref('')

warehouse.belongKv().then(res => {
    kv.value = res.data.value
    let item = kv.value[0]
    currentWareHouse.init({
        wareHouseNO: item.no,
        wareHouseName: item.value,
        waerhouseId: item.key
    })
    warehouseName.value = currentWareHouse.getWareHouse().waerhouseId as string
    // searchList()
    fetchData()
})

const currentpage = ref(1)
const size = ref(1000)
function fetchData() {
    house.list(currentpage.value, size.value,warehouseName.value,houseNo.value).then(res => {
        list.value = res.data.value
        sapre_list = (res.data.value.records)!;
        current.value = list.value.records![0]
    })
}
function pagechange(page: number) {
    currentpage.value = page
    fetchData()
}
function searchList() {
    // debugger
    const item = kv.value.filter(y => y.key == warehouseName.value)[0]
    currentWareHouse.setWareHouse({
        wareHouseNO: item.no,
        wareHouseName: item.value,
        waerhouseId: item.key
    })
    fetchData()
}
const handle_delete = (id:number) => {
    console.log("id ->" + id);
    
    house.deleteById(id).then(() => fetchData())
}
const handle_show_iamge = (data:Array<UrlInfo>) => {
    console.log(data);
    imageDialog.value = true
    urlInfo.value = data

}
</script>
<style scoped></style>
