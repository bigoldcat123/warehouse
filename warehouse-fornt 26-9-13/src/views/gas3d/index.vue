<template>
    <PageFrame>
        <template #title>{{ houseName || houseNo || CUBE_METRICS.gas.title }}</template>
        <template #subtitle>{{ CUBE_METRICS.gas.subtitle }}</template>
        <template #actions>
            <div @click="$router.push('/house')" class="size-[20px] cursor-pointer"></div>
        </template>

        <Granary3d :house-no="houseNo" metric="gas" />
    </PageFrame>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import PageFrame from '@/components/common/PageFrame.vue'
import Granary3d from '@/components/Granary3d.vue'
import { CUBE_METRICS } from '@/stores/cube'

const route = useRoute()
// 仓房编号（从仓房卡片进入时由 query 携带，如 /gas3d?houseNo=HH001&houseName=xx）
const houseNo = computed(() => (route.query.houseNo as string) || '')
const houseName = computed(() => (route.query.houseName as string) || '')
</script>
