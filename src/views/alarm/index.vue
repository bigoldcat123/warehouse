<template>
    <Binner :warehouseName="binerProp_warehouseName" :warehouseNO="binerProp_warehouseNO" />

    <div class=" flex mb-4">
        <div>

            <div class=" flex">
                <div>报警级别
                    <el-checkbox-group v-model="levels">
                        <el-checkbox :disabled="!user.getPriv()?.includes('1')" label="一般报警" value="一般报警"
                            size="large" />
                        <el-checkbox :disabled="!user.getPriv()?.includes('2')" label="严重报警" value="严重报警"
                            size="large" />
                    </el-checkbox-group>
                </div>
                <div style="display: none;">
                    类型
                    <el-checkbox-group v-model="types">
                        <el-checkbox value="发热" label="发热" size="large" />
                        <el-checkbox value="不连续" label="不连续" size="large" />
                        <el-checkbox value="霉变趋势" label="霉变趋势" size="large" />
                        <el-checkbox value="结露趋势" label="结露趋势" size="large" />
                        <el-checkbox value="空仓" label="空仓" size="large" />
                        <el-checkbox value="备用类型" label="备用类型" size="large" />
                    </el-checkbox-group>
                </div>

                <div class="ml-8 pl-4 border-l ">
                    <div>审核</div>
                    <el-checkbox-group v-model="verifies">
                        <el-checkbox label="已审核" value="true" size="large" />
                        <el-checkbox label="未审核" value="false" size="large" />
                    </el-checkbox-group>
                </div>
                <div class="ml-8 pl-4 border-l">
                    <div class="mb-1">仓库</div>
                    <el-select v-model="warehouseID" placeholder="请选择仓库名" style="width: 200px">
                        <el-option v-for="item in kv" :key="item.value" :label="item.value" :value="item.key" />
                    </el-select>
                </div>

            </div>
        </div>
        <div class=" ml-3 flex justify-center items-center">
            <el-button type="primary" @click="query"> <el-icon>
                    <Search />
                </el-icon> 搜索</el-button>
        </div>

        <div class=" ml-3 flex justify-center items-center">
            <el-button type="primary"><el-icon>
                    <Share />
                </el-icon> <a href="/api/alarm/excel">导出报警信息</a></el-button>

        </div>
    </div>
    <div>
        <el-table :data="alarmList?.records" ref="tableref" border>
            <!-- <el-table-column prop="id" label="ID" /> -->
            <el-table-column prop="houseNo" label="仓房编号" />
            <el-table-column prop="alertPos" label="位置" v-if="false" />
            <el-table-column prop="alertLevel" label="等级">
                <template #default="scope">
                    <el-tag v-if="scope.row.alertLevel == '一般报警'" type="warning">{{ scope.row.alertLevel }}</el-tag>
                    <el-tag v-else type="danger">{{ scope.row.alertLevel }}</el-tag>
                </template>
            </el-table-column>
            <el-table-column prop="alertType" label="类型" />
            <el-table-column prop="alertTime" label="时间" width="180" />
            <el-table-column prop="yuntu" label="云图" width="90">
                <template #default="scope">
                    <el-button type="primary" size="small"
                        @click="show_yuntu(scope.row.yuntu)">
                        <el-icon>
                            <Search />
                        </el-icon> 查看</el-button>
                </template>
            </el-table-column>
            <el-table-column label="审核" width="180">
                <template #default="scope">
                    <el-tag v-if="scope.row.isVerify" type="success">已审核</el-tag>
                    <el-tag v-else type="danger">未审核</el-tag>
                    <el-button v-if="scope.row.isVerify" type="primary" size="small"
                        @click="showHandleDialog = true; currentHandle = scope.row.handle"> <el-icon>
                            <Search />
                        </el-icon> 查看审核</el-button>
                    <el-button :disabled="!user.getPriv()?.includes('3')" v-else type="success" size="small"
                        @click="handleDialog = true; currentid = scope.row.id"><el-icon>
                            <ChatLineSquare />
                        </el-icon> 审核</el-button>
                </template>
            </el-table-column>
        </el-table>

        <el-pagination @current-change="pagechange" :default-page-size="size"
            :page-count="alarmList?.pages ? alarmList?.pages : 0" layout="prev, pager, next" />
    </div>
    <handle :id="currentid" :dialogVisible="handleDialog" @close="handleDialog = false" @refresh="fetchData"></handle>
    <ShowHandle :handle="currentHandle" :dialogVisible="showHandleDialog" @close="showHandleDialog = false">
    </ShowHandle>
    <ImageDialog :dialogVisible="dialog" :urlInfo="url_info" @close="dialog = false"></ImageDialog>
</template>
<script setup lang="ts">
import Binner from '@/components/common/Binner.vue';
import { ref } from 'vue'
import alarm, { type type_Alarm } from '@/api/alarm';
import handle from './Handle.vue';
import ShowHandle from './ShowHandle.vue';
import { useCurrentUserStore } from '@/stores/currentUser';
import warehouse from '@/api/warehouse';
import { useRoute } from 'vue-router';
import { useCurrentWareHouse } from '@/stores/currentWareHouse';
import {Search , ChatLineSquare, Share} from '@element-plus/icons-vue'
import ImageDialog from '../house/ImageDialog.vue';
import type { UrlInfo } from '@/components/HouseInfo.vue';
import { api_PreFix } from '@/api';
const currentWareHouse = useCurrentWareHouse()
const route = useRoute()
const dialog = ref(false)
const url_info = ref<Array<UrlInfo> | undefined>()

console.log(route.query);

const levels = ref<string[]>([])
const types = ref<string[]>([])
const verifies = ref(<string[]>[])
const alarmList = ref<Page<type_Alarm>>()
const currentid = ref(undefined)
const currentHandle = ref('')
const handleDialog = ref(false)
const showHandleDialog = ref(false)
const user = useCurrentUserStore()
const currentpage = ref(1)
const size = ref(10)
const kv = ref<any[]>([])
const binerProp_warehouseNO = ref('')
const binerProp_warehouseName = ref('')
const warehouseID = ref('')
const show_yuntu = (yt:string) => {
    const map_fn = (x: string) => {
        if (x.startsWith('/') || x.startsWith('\\')) {
            return '/' + api_PreFix + '/static/' + x.substring(1).replace('\\', '/')
        } else {
            return '/' + api_PreFix + '/static/' + x.replace('\\', '/')
        }
    };
    const yuntu = yt?.split(",").map(map_fn) ?? []
    url_info.value = [
        {
            name:'云图',
            urls:yuntu
        }
    ]
    dialog.value = true
}
warehouse.belongKv().then(res => {
    kv.value = res.data.value
    let item
    if (route.query.wareHouseNO) {
        item = kv.value.filter(x => x.no == route.query.wareHouseNO)[0]
        currentWareHouse.setWareHouse({
            wareHouseNO: item.no,
            wareHouseName: item.value,
            waerhouseId: item.key
        })
    } else {
        item = kv.value[0]
        currentWareHouse.init({
            wareHouseNO: item.no,
            wareHouseName: item.value,
            waerhouseId: item.key
        })
    }
    warehouseID.value = currentWareHouse.getWareHouse().waerhouseId as string
    fetchData()
})
function pagechange(page: number) {
    currentpage.value = page
    fetchData()
}

const query = () => {
    const item = kv.value.filter(x => x.key == warehouseID.value)[0]
    currentWareHouse.setWareHouse({
        wareHouseNO: item.no,
        wareHouseName: item.value,
        waerhouseId: item.key
    })
    fetchData()
}
function fetchData() {
    alarm.list(levels.value, types.value, verifies.value, warehouseID.value, currentpage.value, size.value).then(res => {
        alarmList.value = res.data.value
    })
}


</script>
<style scoped></style>
