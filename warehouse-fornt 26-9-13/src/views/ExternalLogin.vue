<template>
    <div class="external-login">
        <div class="external-login__card">
            <div class="external-login__spinner"></div>
            <p class="external-login__title">{{ message }}</p>
            <p v-if="error" class="external-login__error">{{ error }}</p>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useCurrentUserStore } from '@/stores/currentUser'

const route = useRoute()
const router = useRouter()
const currentUser = useCurrentUserStore()

const message = ref('正在登录...')
const error = ref('')

// 外部应用通过 searchParam 携带 token 访问本页，自动以只读访客身份登录
const token = (route.query.token as string) || ''

if (!token) {
    error.value = '缺少登录凭证'
    message.value = '登录失败'
    ElMessage.error('缺少登录凭证')
    currentUser.logout()
    // 短暂停留后回到登录页
    setTimeout(() => router.replace('/login'), 1500)
} else {
    const guestUser: CurrentUser = {
        token,
        detail: {
            id: 'guest',
            name: '访客',
            sex: '',
            companyID: -1,
            position: '访客',
            phone: '',
            priv: '',
            username: 'guest'
        }
    }
    currentUser.setValue(guestUser)
    router.replace('/warehousePanorama')
}
</script>

<style scoped>
.external-login {
    position: fixed;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #0f2440 0%, #1e5f8a 100%);
    font-family: inherit;
}
.external-login__card {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16px;
    padding: 40px 56px;
    background: rgba(255, 255, 255, 0.06);
    border: 1px solid rgba(255, 255, 255, 0.12);
    border-radius: 12px;
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.35);
    backdrop-filter: blur(6px);
}
.external-login__spinner {
    width: 40px;
    height: 40px;
    border: 4px solid rgba(255, 255, 255, 0.2);
    border-top-color: #64b5f6;
    border-radius: 50%;
    animation: spin 0.9s linear infinite;
}
.external-login__title {
    color: #fff;
    font-size: 15px;
    font-weight: 600;
    margin: 0;
}
.external-login__error {
    color: #ff8a80;
    font-size: 13px;
    margin: 0;
}
@keyframes spin {
    to {
        transform: rotate(360deg);
    }
}
</style>
