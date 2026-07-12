<template>
    <div class="flex items-center gap-3 px-5 py-3 mb-5 rounded-lg" style="background: linear-gradient(135deg, #2968a8 0%, #1a3a5c 100%);">
        <svg class="w-6 h-6 text-[#b0d0f0] flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
        </svg>
        <span class="text-white text-lg font-semibold tracking-wide">
            {{ w.getWareHouse().wareHouseName }}
        </span>
        <span class="text-[#b0d0f0] text-base">
            ({{ w.getWareHouse().wareHouseNO }})
        </span>
    </div>
</template>
<script setup lang="ts">
import {useCurrentWareHouse} from '@/stores/currentWareHouse'
const w = useCurrentWareHouse()
</script>
<style scoped>
</style>
