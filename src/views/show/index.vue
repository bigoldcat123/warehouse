<template>
    <div>
        <el-button type="primary" @click="$router.go(-1)"><el-icon><ArrowLeft /></el-icon>返回</el-button>
    </div>
    <div v-if="$route.query.imgs" v-for="item in urls" class="content">
        <img  :src="item" alt="">
    </div>
    <div v-else class="content">
            没有图片
    </div>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import { api_PreFix } from '@/api';
import { useRoute } from 'vue-router';
import { ArrowLeft } from '@element-plus/icons-vue';
const route = useRoute();
const prefix = route.query.prefix
const lx=route.query.lx;
const urls = route.query.imgs ? (route.query.imgs as string).split(',').map(x => {
    if(x.startsWith('/') || x.startsWith('\\')) {
        return api_PreFix + '/static/' + x.substring(1).replace('\\','/')
    }else {
        return api_PreFix +  '/static/' + x.replace('\\','/')
    }
}) : []

</script>
<style >
.content{
  width:100%;

  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom:10px;
  margin-top:30px;
}

</style>
