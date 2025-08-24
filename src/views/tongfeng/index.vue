<template>
    <div class="flex gap-x-4 items-center">
        <el-button type="primary" @click="$router.go(-1)">
            <el-icon>
                <ArrowLeft />
            </el-icon>返回</el-button>
        <div>类型：{{ tongfengInfo?.tongfengLx }}</div>
        <div>状态： {{ tongfengInfo?.tongfengZt }}</div>
    </div>
    <div class=" flex justify-evenly pt-5 pb-5">
        <button @click="() => currentImageInfo = 0"
            :style="currentImageInfo == 0 ? 'background-color:rgb(66, 133, 244)' : ''"
            class="cursor-pointer flex flex-1 justify-center text-slate-800 ">
            通风状态图</button>
        <button @click="() => currentImageInfo = 1"
            :style="currentImageInfo == 1 ? 'background-color:rgb(66, 133, 244)' : ''"
            class="  cursor-pointer flex flex-1 justify-center text-slate-800  border-l-2 border-r-2">通风水势图</button>
        <button @click="() => currentImageInfo = 2"
            :style="currentImageInfo == 2 ? 'background-color:rgb(66, 133, 244)' : ''"
            class="  cursor-pointer flex flex-1 justify-center text-slate-800 ">通风窗口模型位图</button>
    </div>
    <!-- <div>{{ urls }}</div> -->
    <div v-if="urls.length > 0" v-for="item in urls" class="content">
        <img :src="item" alt="">
    </div>
    <div v-else class="content">
        没有图片
    </div>
</template>
<script setup lang="ts">
import { ArrowLeft } from '@element-plus/icons-vue';
import { computed, ref } from 'vue'
import server, { type TongfengInfo } from '@/api/house'
import { useRoute } from 'vue-router';
import { api_PreFix } from '@/api';

let route = useRoute();
const tongfengInfo = ref<TongfengInfo | null>(null)
const currentImageInfo = ref(0); // 0 通风状态图 1通风水势图 2通风窗口模型位图
const prefix = computed(() => {
    if (currentImageInfo.value == 0) {
        return "tongfeng/"
    } else if (currentImageInfo.value == 1) {
        return "TongFeng_sst/"
    } else {
        return "TFModeWin/"
    }
})
const orign_urls = computed(() => {
    if (currentImageInfo.value == 0) {
        return tongfengInfo.value?.tongfengTu
    } else if (currentImageInfo.value == 1) {
        return tongfengInfo.value?.tongFengSst
    }else {
        return tongfengInfo.value?.tfmodeWin
    }
})
const urls = computed(() => {
    return orign_urls.value ? orign_urls.value.split(',').map(x => {
        if (x.startsWith('/') || x.startsWith('\\')) {
            return '/' + api_PreFix + '/static/' + x.substring(1).replace('\\', '/')
        } else {
            return '/' + api_PreFix + '/static/'  + x.replace('\\', '/')
        }
    }) : []
})
let id = route.query.houseId;
console.log(id);
server.getTongFengInfo(Number(id)).then(res => {
    tongfengInfo.value = res.data.value
})

</script>
<style scoped></style>