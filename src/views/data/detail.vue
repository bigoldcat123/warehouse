<template>
<div class=" h-full overflow-auto">

    <div class=" text-gray-950  bg-slate-50 text-center text-2xl font-bold" style="height:40px;text-align:center;margin-bottom:10px;line-height: 40px;">
        <!-- <span class="text-lime-400">当前仓库</span>&nbsp;&nbsp;<span>仓库编号：{{ w.getWareHouse().wareHouseNO }}</span>&nbsp;&nbsp; <span>仓库名称：{{ w.getWareHouse().wareHouseName }}</span> -->
        {{ detail?.wareHouseName }}-{{ detail?.houseNo }}({{detail?.houseName}})
    </div>
        <div>
            <ElButton type="primary" @click="$router.go(-1)"> <el-icon><ArrowLeft /></el-icon> 返回</ElButton>
        </div>
        <div class=" flex flex-wrap">
            <el-tag type="info" class=" item">品种：{{ detail?.breed }}</el-tag>
            <el-tag type="info" class=" item" >水分：{{ detail?.water }}</el-tag>
            <el-tag type="info" class=" item" >入库时间：{{ detail?.entryTime }}</el-tag>
            <el-tag type="info" class=" item" >保管：{{ detail?.keeper }}</el-tag>
            <el-tag type="info" class=" item" >仓温：{{ detail?.inTemperature }}</el-tag>
            <el-tag type="info" class=" item" >外温度：{{ detail?.outTemperature }}</el-tag>
            <el-tag type="info" class=" item" >仓湿：{{ detail?.inHumidity }}</el-tag>
            <el-tag type="info" class=" item" >外湿：{{ detail?.outHumidity }}</el-tag>
            <el-tag type="info" class=" item" >高温：{{ $route.query.maxTemperature }}</el-tag>
            <el-tag type="info" class=" item" >低温：{{ $route.query.minTemperature }}</el-tag>
            <el-tag type="info" class=" item" >均温：{{ $route.query.avgTemperature }}</el-tag>
            <el-tag type="info" class=" item" v-show="item.length > 0" v-for="item,key in ($route.query.layerAvg as unknown as string ).split('|')">{{ (key + 1) + ' 层：' + item }}</el-tag>

            <el-tag type="info" class=" item" >检测时间：{{ detail?.testTime }}</el-tag>

        </div>
        <div class="flex flex-wrap" style="justify-content: center;">
        <div >
            <div class="m-2" v-for="item, key in detail?.list">
                <div>
                    <span v-if="route.query.house_type=='平房仓'" class="titem" style="background-color:#e7e5e4;">{{ key +1 }} 层</span>
                    <span v-else class="titem" style="background-color:#e7e5e4;">点位</span>
                    <span  class="titem side"  v-for="i,kk in item[0]">{{ kk + 1 }}</span>
                </div>
                <div v-for="x,keyx in item">
                    <span class="titem side">{{ keyx+1 }} </span>
                    <span class="titem" v-for="y in x" >
                        {{ cal_temperature(y) }}
                        <!-- <div v-if="is_ok(y)" class="absolute h-screen w-screen bg-red">???</div> -->
                    </span>
                </div>
            </div>
            </div>
        </div>
</div>
</template>
<script setup lang="ts">
import Banner from '@/components/common/Binner.vue'
import { ElButton } from 'element-plus';
import data, { type type_Data_Detail } from '@/api/data';
import { ref } from 'vue'
import {ArrowLeft} from '@element-plus/icons-vue'
import { useRoute } from 'vue-router';
const route = useRoute()
console.log();

const detail = ref<type_Data_Detail>()
data.getDetil(route.query.id as unknown as number).then(res => {
    detail.value = res.data.value
})
function cal_temperature(num:string) {
    const n = Number(num)
   if (n && n >= -40 && n <= 80) {
    return num
   }else {
    return "-"
   }
}


function is_ok(num:string) {
    const n = Number(num)
   if (n && n >= -40 && n <= 80) {
    return true
   }else {
    return false
   }
}

</script>
<style scoped>
.item{
    font-size: 1rem;
    padding: 1rem;
    border-radius: 0.5rem;
    margin-right: 1rem;
    margin-top: 1rem;
    cursor: pointer;
}
.titem{
    background-color: #ffffff;
    padding: 0.25rem;
    /*border-radius: 0.5rem;
    margin-right: 0.25rem;
    margin-top: 0.25rem;*/
    height:2.5rem;
    cursor: pointer;
    min-width: 4rem;
    display: inline-block;
    width:4.5rem;
    border: 1px solid #ccc;
}
.side {
    background-color: #f5f5f4;
    text-align: center;
}
.lie {
    background-color: blueviolet;
}

.container {
  display: flex;
  justify-content: center;   /* 水平方向居中对齐 */
}
</style>
