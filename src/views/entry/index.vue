<template>
<Binner :warehouseName="binerProp_warehouseName" :warehouseNO="binerProp_warehouseNO"/>
    <el-button @click="addDialog = true" type="primary"><el-icon><Plus /></el-icon> 入库</el-button>
    <div class="mb-4 mt-2 flex gap-2
    ">
        <el-select @change="warehouseChange" v-model="waerhouseID" placeholder="选择仓库" style="width: 180px">
            <el-option v-for="item in waerhouseKv" :key="item.key" :label="item.value" :value="item.key" />
        </el-select>
        <el-select v-model="houseID" placeholder="选择仓房" style="width: 180px">
            <el-option v-for="item in houseKv" :key="item.key" :label="item.value" :value="item.key" />
        </el-select>
        <el-date-picker v-model="from" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="起始时间" />
        <el-date-picker v-model="to" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="结束时间" />
        <el-button @click="query"  type="success"><el-icon><Search /></el-icon> 查询</el-button>
        <el-button @click="reset"  type="warnning">重置</el-button>
    </div>
    <div>
        <el-table :data="list?.records" border>
            <!-- <el-table-column prop="id" label="ID" /> -->
            <el-table-column label="仓房">
                <template #default="scope">
                    {{ kv.filter(x => x.key == scope.row.houseID)[0]?.value }}
                </template>
            </el-table-column>
            <el-table-column prop="breed" label="品种" />
            <el-table-column label="入库人">
                <template #default="scope">
                    {{ userKv.filter(x => x.key == scope.row.entryUserId)[0]?.value }}
                </template>
            </el-table-column>
            <el-table-column label="管理人">
                <template #default="scope">
                    {{ userKv.filter(x => x.key == scope.row.stockman)[0]?.value }}
                </template>
            </el-table-column>
            <el-table-column prop="entryTime" label="入库时间" />
            <el-table-column prop="water" label="含水量" />
            <el-table-column label="操作">
                <template #default="scope">
                    <!-- {{ scope.row.id }} -->
                    <el-button type="primary" size="small"
                        @click="() => { current = scope.row; updateDialog = true; }"><el-icon><EditPen /></el-icon> 编辑</el-button>

                        <el-popconfirm title="确认删除?" @confirm="() => entry.deleteById(scope.row.id).then(() => fetchData())">
                    <template #reference>
                    <el-button type="danger" size="small"><el-icon><Delete /></el-icon>删除</el-button>
                    </template>
                </el-popconfirm>

                    <!-- <el-button type="danger" size="small"
                        @click="() => entry.deleteById(scope.row.id).then(() => fetchData())">删除</el-button> -->
                </template>
            </el-table-column>
        </el-table>

        <el-pagination @current-change="pagechange" :default-page-size="size" :page-count="list?.pages"
            layout="prev, pager, next" />
    </div>
    <AddDialog :dialog-visible="addDialog" @refresh="fetchData" @close="addDialog = false"></AddDialog>
    <UpdateDialog :entry="current!" :dialog-visible="updateDialog" @refresh="fetchData" @close="updateDialog = false">
    </UpdateDialog>
</template>
<script setup lang="ts">
import {Plus, Reading,Search,EditPen,Delete} from '@element-plus/icons-vue';
import { ref } from 'vue'
import entry, { type type_Entry } from '@/api/entry';
import house from '@/api/house';
import AddDialog from './AddDialog.vue';
import UpdateDialog from './UpdateDialog.vue';
import user from '@/api/user';
import warehouse from '@/api/warehouse';
import Binner from '@/components/common/Binner.vue';
import { useCurrentUserStore } from '@/stores/currentUser';
import { useCurrentWareHouse } from '@/stores/currentWareHouse';
const currentWareHouse = useCurrentWareHouse()
const binerProp_warehouseNO = ref('')
const binerProp_warehouseName = ref('')
const currentUser = useCurrentUserStore()
const list = ref<Page<type_Entry>>()
const addDialog = ref(false)
const updateDialog = ref(false)
const current = ref<type_Entry>()
const currentpage = ref(1)
const size = ref(import.meta.env.ENV_PAGESIZE)
const waerhouseKv = ref<any[]>([])
const houseKv = ref<any[]>([])
const waerhouseID = ref<number | undefined>(undefined)
const houseID = ref<number | undefined>(undefined)
const from = ref('')
const to = ref('')
function reset () {
    waerhouseID.value = undefined
    houseID.value = undefined
    from.value = ''
    to.value = ''
    if(currentUser.isMainComp()) {
        binerProp_warehouseNO.value = '--'
        binerProp_warehouseName.value = '全部'
    }
    fetchData()
}
warehouse.belongKv().then(res => {
    waerhouseKv.value = res.data.value

    let item = waerhouseKv.value[0]

    currentWareHouse.init({
        wareHouseNO: item.no,
        wareHouseName: item.value,
        waerhouseId: item.key
    })
    waerhouseID.value = Number.parseInt( currentWareHouse.getWareHouse().waerhouseId as string)
    warehouseChange(waerhouseID.value)
    fetchData()
})
function warehouseChange(value: any) {
    house.findByWarehouseId(value).then(res => {
        houseKv.value = res.data.value
    })
}
const query = () => {
    const item = waerhouseKv.value.filter(x => x.key == waerhouseID.value)[0]
    currentWareHouse.setWareHouse({
        wareHouseNO: item.no,
        wareHouseName: item.value,
        waerhouseId: item.key
    })
    fetchData()
}
function fetchData() {

    entry.list({ warehouseId: waerhouseID.value, houseId:houseID.value,from:from.value,to:to.value},currentpage.value, size.value).then(res => {
        list.value = res.data.value
    })
}
function pagechange(page: number) {
    currentpage.value = page
    fetchData()
}
const kv = ref<any[]>([])
house.kv().then(res => {
    kv.value = res.data.value
})
const userKv = ref<any[]>([])
user.kv().then(res => {
    userKv.value = res.data.value
})

</script>
<style scoped></style>
