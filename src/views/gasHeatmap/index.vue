<template>
    <PageFrame>
        <template #title>{{ houseName || houseNo || '粮仓气体浓度剖面图' }}</template>
        <template #subtitle>气体浓度剖面图</template>
        <template #actions>
            <div @click="$router.push('/house')" class="size-[20px] cursor-pointer"></div>
        </template>

        <CubeHeatmap :house-no="houseNo" metric="gas" />
    </PageFrame>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import PageFrame from '@/components/common/PageFrame.vue'
import CubeHeatmap from '@/components/CubeHeatmap.vue'

const route = useRoute()
// 仓房编号（从仓房卡片进入时由 query 携带，如 /gasHeatmap?houseNo=HH001&houseName=xx）
const houseNo = computed(() => (route.query.houseNo as string) || '')
const houseName = computed(() => (route.query.houseName as string) || '')
</script>
