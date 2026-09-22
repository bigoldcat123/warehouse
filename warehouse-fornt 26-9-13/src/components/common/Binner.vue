<template>
    <div class="warehouse-banner flex items-center gap-3 px-5 py-3 mb-5 rounded-lg">
        <svg class="warehouse-banner__icon w-6 h-6 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
        </svg>
        <span class="warehouse-banner__name text-lg font-semibold tracking-wide">
            {{ w.getWareHouse().wareHouseName }}
        </span>
        <span class="warehouse-banner__no text-base">
            ({{ w.getWareHouse().wareHouseNO }})
        </span>
    </div>
</template>
<script setup lang="ts">
import {useCurrentWareHouse} from '@/stores/currentWareHouse'
const w = useCurrentWareHouse()
</script>
<style scoped>
.warehouse-banner {
    background: linear-gradient(135deg, #ffffff 0%, #eaf5fc 100%);
    border: 1px solid #d5e6f2;
    box-shadow: 0 4px 14px rgba(55, 96, 126, 0.06);
}

.warehouse-banner__icon {
    color: #4e94bc;
}

.warehouse-banner__name {
    color: #294e67;
}

.warehouse-banner__no {
    color: #6c8799;
}
</style>
