<template>
    <PageFrame>
        <template #title>{{ houseName || houseNo || '3D粮仓温度' }}</template>
        <template #subtitle>{{ SENSOR_METRICS.temperature.subtitle }}</template>
        <template #actions>
            <div @click="$router.push('/warehousePanorama')" class="size-[20px] cursor-pointer"></div>
        </template>

        <Granary3d :house-no="houseNo" />
    </PageFrame>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import PageFrame from '@/components/common/PageFrame.vue'
import Granary3d from '@/components/Granary3d.vue'
import { SENSOR_METRICS } from '@/features/receiverData3d/config'

const route = useRoute()
// 仓房编号（从仓房卡片进入时由 query 携带，如 /granary3d?houseNo=HH001&houseName=xx）
const houseNo = computed(() => (route.query.houseNo as string) || '')
const houseName = computed(() => (route.query.houseName as string) || '')
</script>
