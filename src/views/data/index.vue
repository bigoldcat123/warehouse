<template>
    <Binner />
    <div class="mb-4">
        起始时间 <el-date-picker v-model="from" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择入库时间" />
        结束时间 <el-date-picker v-model="to" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择入库时间" />
        <el-input v-model="houseName" class="max-w-48 m-2" placeholder="仓房名" />


        <!-- <el-input v-model="warehouseName" class="max-w-48 m-2" placeholder="仓库名" /> -->

        <el-select v-model="warehouseName" placeholder="请选择仓库名" style="width: 140px">
            <el-option v-for="item in kv" :key="item.value" :label="item.value" :value="item.value" />
        </el-select>
        <el-button type="success" @click="query"> <el-icon><Search /></el-icon> 搜索</el-button>
        <!-- <el-button type="danger" @click="reset">重置</el-button> -->
    </div>
    <div>
        <el-table :data="list?.records" border>
            <!-- <el-table-column prop="id" label="ID" /> -->
            <el-table-column prop="houseNo" label="仓房编号" width="100" />
            <el-table-column prop="houseName" label="仓房名" width="100" />
            <el-table-column prop="house_type" label="仓房类型" width="100" />
            <el-table-column prop="inTemperature" label="仓温" width="60" />
            <el-table-column prop="inHumidity" label="仓湿" width="60" />
            <el-table-column prop="maxTemperature" label="高温" width="60" />
            <el-table-column prop="minTemperature" label="低温" width="60" />
            <el-table-column prop="avgTemperature" label="均温" width="60" />
            <el-table-column prop="layerMax" label="层高" />
            <el-table-column prop="layerMin" label="层底" />
            <el-table-column prop="layerAvg" label="层均" />
            <el-table-column prop="testDate" label="时间" width="90" />

            <el-table-column label="操作" width="90">
                <template #default="scope">
                    <el-button type="primary" size="small" @click="() => $router.push({
                        path: '/data/detail',
                        query: {
                            id: scope.row.id, maxTemperature: scope.row.maxTemperature, minTemperature: scope.row.minTemperature,
                            avgTemperature: scope.row.avgTemperature, layerAvg: scope.row.layerAvg, house_type: scope.row.house_type
                        }
                    })"> <el-icon><Reading /></el-icon> 详情</el-button>
                </template>
            </el-table-column>
        </el-table>

        <el-pagination @current-change="pagechange" :default-page-size="size" :page-count="list?.pages"
            layout="prev, pager, next" />
    </div>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import {Search, Reading} from '@element-plus/icons-vue';
import Binner from '@/components/common/Binner.vue';
import data, { type type_Data } from '@/api/data';
import warehouse from '@/api/warehouse';
import { useCurrentUserStore } from '@/stores/currentUser';
import { useCurrentWareHouse } from '@/stores/currentWareHouse';
const currentWareHouse = useCurrentWareHouse()
const kv = ref<any[]>([])
const currentUser = useCurrentUserStore()
const list = ref<Page<type_Data>>()
const from = ref<string | null>(null)
const to = ref<string | null>(null)
const e = '@#$%^&*()_+'
const houseName = ref<string | null>(null)
const warehouseName = ref<string | null>(null)
const currentpage = ref(1)
const size = ref(10)
const reset = () => {
    from.value = ''
    to.value = ''
    houseName.value = ''
    warehouseName.value = ''
    fetchData()
}
warehouse.belongKv().then(res => {
    kv.value = res.data.value
    const item = kv.value[0]
    currentWareHouse.init({
        wareHouseName: item.value,
        wareHouseNO: item.no,
        waerhouseId: item.key

    })
    warehouseName.value = currentWareHouse.getWareHouse().wareHouseName as string
    fetchData()
})
function pagechange(page: number) {
    currentpage.value = page
    fetchData()
}
const query = () => {
    const item = kv.value.filter(x => x.value == warehouseName.value)[0]
    currentWareHouse.setWareHouse({
        wareHouseNO: item.no,
        wareHouseName: item.value,
        waerhouseId: item.key
    })
    fetchData()
}
function fetchData() {
    data.list(from.value, to.value, houseName.value, warehouseName.value == e ? null : warehouseName.value, currentpage.value, size.value).then(res => {
        list.value = res.data.value
    })
}

</script>
<style scoped></style>
