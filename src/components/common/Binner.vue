<template>
    <div class=" text-gray-950 text-base bg-slate-50" style="height:40px;text-align:right;margin-bottom:10px;">
        <span class="text-lime-400">当前仓库</span>&nbsp;&nbsp;<span>仓库编号：{{ w.getWareHouse().wareHouseNO }}</span>&nbsp;&nbsp; <span>仓库名称：{{ w.getWareHouse().wareHouseName }}</span>

    </div>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import {useCurrentWareHouse} from '@/stores/currentWareHouse'
const w = useCurrentWareHouse()
</script>
<style scoped>
</style>
