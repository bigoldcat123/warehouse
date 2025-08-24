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
        <el-table :data="list?.records" border style="width: 100%">
            <!-- <el-table-column prop="id" label="ID" width="60" /> -->
            <el-table-column prop="houseNo" label="仓房编号" />
            <el-table-column prop="houseName" label="仓房名" />
            <el-table-column prop="" label="所属仓库">
                <template #default="scope">
                    {{ kv.filter(x => x.key == scope.row.warehouseID)[0]?.value }}
                </template>
            </el-table-column>
            <el-table-column prop="houseAddr" label="地址" />
            <el-table-column prop="houseType" label="仓房类型" width="70" />
            <el-table-column prop="y" label="列数(X)" width="60" />
            <el-table-column prop="x" label="行数(Y)" width="60" />

            <el-table-column prop="z" label="层数(Z)" width="60" />

            <el-table-column prop="tongfengLx" label="通风类型" width="60" />
            <el-table-column prop="tongfengZt" label="通风状态" width="60" />

            <el-table-column prop="tongfeng" label="通风图" width="90">
                <template #default="scope">
                    <el-button type="primary" size="small"
                        @click="() => $router.push({ path: '/tongfeng', query: { houseId:scope.row.id } })"><el-icon>
                            <Search />
                        </el-icon>查看</el-button>
                </template>
            </el-table-column>
            <el-table-column prop="yuntu" label="云图" width="90">
                <template #default="scope">
                    <el-button type="primary" size="small"
                        @click="() => $router.push({ path: '/show', query: { imgs: scope.row.yuntu, lx: 'img' } })"><el-icon>
                            <Search />
                        </el-icon>查看</el-button>
                </template>
            </el-table-column>
            <el-table-column prop="yuntu" label="曲线" width="90">
                <template #default="scope">
                    <el-button type="primary" size="small"
                        @click="() => $router.push({ path: '/show', query: { imgs: scope.row.quxian, lx: 'line' } })"><el-icon>
                            <Search />
                        </el-icon>查看</el-button>
                </template>
            </el-table-column>
            <el-table-column prop="threeD" label="3D图" width="90">
                <template #default="scope">
                    <el-button type="primary" size="small"
                        @click="() => $router.push({ path: '/show', query: { imgs: scope.row.threeD, lx: 'threeD' } })"><el-icon>
                            <Search />
                        </el-icon>查看</el-button>
                </template>
            </el-table-column>

            <el-table-column prop="valeWin" label="水势图" width="90">
                <template #default="scope">
                    <el-button type="primary" size="small"
                        @click="() => $router.push({ path: '/show', query: { imgs: scope.row.valeWin, lx: 'valeWin' } })"><el-icon>
                            <Search />
                        </el-icon>查看</el-button>
                </template>
            </el-table-column>

            <el-table-column label="操作" width="160">
                <template #default="scope">
                    <el-button type="primary" size="small"
                        @click="() => { current = scope.row; updateDialog = true; }"><el-icon>
                            <EditPen />
                        </el-icon>编辑</el-button>


                    <!-- <el-button type="danger" size="small" @click="() => house.deleteById(scope.row.id).then(() => fetchData())">删除</el-button> -->
                    <el-popconfirm title="确认删除?"
                        @confirm="() => house.deleteById(scope.row.id).then(() => fetchData())">
                        <template #reference>
                            <el-button type="danger" size="small"><el-icon>
                                    <Delete />
                                </el-icon>删除</el-button>
                        </template>
                    </el-popconfirm>

                </template>
            </el-table-column>
        </el-table>
        <el-pagination @current-change="pagechange" :default-page-size="size" :page-count="list?.pages"
            layout="prev, pager, next" />
    </div>
    <AddDialog :kv="kv" :dialog-visible="addDialog" @refresh="fetchData" @close="addDialog = false"></AddDialog>
    <UpdateDialog :kv="kv" :house="current!" :dialog-visible="updateDialog" @refresh="fetchData"
        @close="updateDialog = false"></UpdateDialog>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import house, { type type_House } from '@/api/house';
import AddDialog from './AddDialog.vue';
import UpdateDialog from './UpdateDialog.vue';
import warehouse from '@/api/warehouse';
import { useCurrentUserStore } from '@/stores/currentUser';
import Binner from '@/components/common/Binner.vue';
import { useCurrentWareHouse } from '@/stores/currentWareHouse';
import { Plus } from '@element-plus/icons-vue';
import { Search ,EditPen,Delete } from '@element-plus/icons-vue';
const currentWareHouse = useCurrentWareHouse()
const currentUser = useCurrentUserStore()
const list = ref<Page<type_House>>()
let sapre_list: type_House[] = []
const addDialog = ref(false)
const updateDialog = ref(false)
const current = ref<type_House>()
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
const size = ref(10)
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

</script>
<style scoped></style>
