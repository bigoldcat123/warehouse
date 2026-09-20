<template>
<div class="c1">
    <component :is="authList[currentAuth]"></component>
</div>
</template>
<script setup lang="ts">
// 66 133 244
import { ref } from 'vue'
import UserNamePasswordAuth from '@/components/Login/UserNamePasswordAuth.vue';
import MailAuth from '@/components/Login/MailAuth.vue';
const authList = [UserNamePasswordAuth, MailAuth]
const currentAuth = ref(0)
</script>
<style scoped>

</style>
