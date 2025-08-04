<template>
    <div class="mb-4">
        <el-button @click="addDialog = true" type="primary">新增</el-button>
    </div>
    <div>
        <el-table :data="list?.records" border>
            <el-table-column prop="id" label="ID" width="100"/>
            <el-table-column prop="warehouseNo" label="仓库编号" min-width="20%"/>
            <el-table-column prop="warehouseName" label="仓库名" min-width="20%"/>
            <el-table-column prop="warehouseAddress" label="地址" min-width="20%"/>
            <el-table-column label="操作" min-width="20%">
                <template #default="scope">
                    <!-- {{ scope.row.id }} -->
                    <el-button type="primary" size="small" @click="() => {current= scope.row;updateDialog = true;}">编辑</el-button>
                <el-popconfirm title="确认删除?" @confirm="() => warehouse.deleteById(scope.row.id).then(() => fetchData())">
                    <template #reference>
                    <el-button type="danger" size="small">删除</el-button>
                    </template>
                </el-popconfirm>
                </template>
            </el-table-column>
        </el-table>
        <el-pagination @current-change="pagechange" :default-page-size="size"  :page-count="list?.pages" layout="prev, pager, next"  />
    </div>  
    <AddDialog :dialog-visible="addDialog" @refresh="fetchData" @close="addDialog = false"></AddDialog>
    <UpdateDialog :warehouse="current!" :dialog-visible="updateDialog" @refresh="fetchData" @close="updateDialog = false"></UpdateDialog>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import warehouse, { type type_WareHouse } from '@/api/warehouse';
import AddDialog from './AddDialog.vue';
import UpdateDialog from './UpdateDialog.vue';
const list = ref<Page<type_WareHouse>>()
const visible = ref(false)
const addDialog = ref(false)
const updateDialog = ref(false)
const current = ref<type_WareHouse>()
const currentpage = ref(1)
const size = ref(10)
function fetchData() {
    warehouse.list(currentpage.value,size.value).then(res => {
        list.value = res.data.value
    })
}
fetchData()
function pagechange(page: number) {
    currentpage.value = page
    fetchData()
}
</script>
<style scoped></style>