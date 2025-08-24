<template>
    <div>
        <div>id</div>
        <div>name</div>
        <div>x*y*z</div>
        <div>通风类型</div>
        <div>通风状态</div>
        <div>
            a log of buttons
        </div>
        <div>
            edit- delete
        </div>
    </div>
</template>
<script setup lang="ts">
import type { type_House } from '@/api/house';
import { ref } from 'vue'
const props = defineProps<{
    house:type_House
}>()
</script>
<style scoped>
</style>