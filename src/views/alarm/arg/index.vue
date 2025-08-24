<template>
            <div class=" ml-3 flex justify-right items-right" >
            <el-button type="primary"> <el-icon><Share/></el-icon>  <a href="/api/alarm/arg_excel"  > 导出报警信息统计</a></el-button>

        </div>
<p> &nbsp;</p>
    <div>
        <el-table :data="list.records" border>
            <el-table-column prop="wareHouseNO" label="单位编号" width="130"/>
            <el-table-column prop="wareHouseName" label="单位名称" min-width="25%"/>
            <el-table-column prop="common" label="一般" min-width="15%"/>
            <el-table-column prop="serious" label="严重" min-width="15%"/>
            <el-table-column prop="hot" label="发热" min-width="15%"/>
            <el-table-column prop="dewAndMould" label="结露霉变" min-width="15%"/>
            <el-table-column prop="exception" label="异常" min-width="15%"/>
            <el-table-column label="操作" min-width="15%">
                <template #default="scope">
                    <ElButton type="primary" @click="navigateTo(scope.row.wareHouseNO)" size="small"><el-icon><Search /></el-icon> 查看详细</ElButton>
                </template>
            </el-table-column>
        </el-table>

        <el-pagination @current-change="pagechange" :default-page-size="size"
        :page-count="list.pages" layout="prev, pager, next" />
    </div>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import alarm, { type AlarmArgDTO } from '@/api/alarm';
import { ElButton } from 'element-plus';
import { useRouter } from 'vue-router';
import { Share ,Search} from '@element-plus/icons-vue';
const router = useRouter()
const list= ref<Page<AlarmArgDTO>>({})
const size = 10
const currentPage = ref(1)
function fetchData () {
    alarm.arg(currentPage.value -1,size).then(res => {
    console.log(res.data.value);
    list.value = res.data.value
})
}

const navigateTo = (wareHouseNO:string) => {
    router.push({path:'/alarm',query:{wareHouseNO:wareHouseNO}})
}
const pagechange = (page: number) => {
    currentPage.value = page
    fetchData()
}
fetchData()
</script>
<style scoped>
</style>
