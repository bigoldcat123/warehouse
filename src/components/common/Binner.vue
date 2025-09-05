<template>
    <div class=" text-gray-950  bg-slate-50 text-center text-2xl font-bold" style="height:40px;text-align:center;margin-bottom:10px;line-height: 40px;">
        <!-- <span class="text-lime-400">当前仓库</span>&nbsp;&nbsp;<span>仓库编号：{{ w.getWareHouse().wareHouseNO }}</span>&nbsp;&nbsp; <span>仓库名称：{{ w.getWareHouse().wareHouseName }}</span> -->
        {{ w.getWareHouse().wareHouseName }}({{ w.getWareHouse().wareHouseNO }})
    </div>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import {useCurrentWareHouse} from '@/stores/currentWareHouse'
const w = useCurrentWareHouse()
</script>
<style scoped>
</style>
