<template>
    <div class="login-container">
        <div class="login-card">
            <el-form
                ref="ruleFormRef"
                :model="ruleForm"
                status-icon
                :rules="rules"
                label-position="top"
                class="login-form"
            >
                <div class="login-header">
                    <img src="../../assets/image/login_title.png" class="login-logo" />
                    <h2 class="login-title">用户登录</h2>
                </div>

                <el-form-item label="用户名" prop="username" class="form-item">
                    <el-input
                        v-model="ruleForm.username"
                        type="text"
                        size="large"
                        placeholder="请输入用户名"
                        autocomplete="off"
                        class="custom-input"
                        prefix-icon="User"
                    />
                </el-form-item>

                <el-form-item label="密码" prop="password" class="form-item">
                    <el-input
                        v-model="ruleForm.password"
                        type="password"
                        size="large"
                        placeholder="请输入密码"
                        autocomplete="off"
                        class="custom-input"
                        prefix-icon="Lock"
                        show-password
                        @keyup.enter="submitForm(ruleFormRef)"
                    />
                </el-form-item>

                <div class="button-group">
                    <el-button
                        type="primary"
                        size="large"
                        class="login-button"
                        @click="submitForm(ruleFormRef)"
                        :loading="loading"
                    >
                        {{ loading ? '登录中...' : '登 录' }}
                    </el-button>
                    <el-button
                        size="large"
                        class="reset-button"
                        @click="resetForm(ruleFormRef)"
                    >
                        重 置
                    </el-button>
                </div>
            </el-form>
        </div>

        <!-- Background decoration -->
        <div class="bg-decoration">

        </div>
    </div>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import Auth from "@/api/auth";
import { useCurrentUserStore } from "@/stores/currentUser";
import { useRouter } from "vue-router";

const router = useRouter();
const currentUser = useCurrentUserStore();
const ruleFormRef = ref<FormInstance>();
const loading = ref(false);

const checkUsername = (rule: any, value: string, callback: any) => {
    if (!value) {
        return callback(new Error("请输入用户名！"));
    }
    setTimeout(() => {
        if (value.length < 1) {
            callback(new Error("用户名太短!"));
        } else {
            callback();
        }
    }, 100);
};

const validatePass = (rule: any, value: any, callback: any) => {
    if (value === "") {
        callback(new Error("请输入密码！"));
    } else {
        callback();
    }
};

const ruleForm = reactive<DaoLoginUser>({
    username: "",
    password: "",
});

const rules = reactive<FormRules<DaoLoginUser>>({
    username: [{ validator: checkUsername, trigger: "blur" }],
    password: [{ validator: validatePass, trigger: "blur" }],
});

const submitForm = (formEl: FormInstance | undefined) => {
    if (!formEl) return;
    formEl.validate((valid) => {
        if (valid) {
            loading.value = true;
            Auth.daoLogin(ruleForm)
                .then((res) => {
                    currentUser.setValue(res.data.value);
                    if (currentUser.isMainComp()) {
                        router.push("/alarmarg");
                    } else {
                        router.push("/alarm");
                    }
                })
                .catch((err) => {
                    console.log(err);
                })
                .finally(() => {
                    loading.value = false;
                });
        } else {
            console.log("登录错误!");
        }
    });
};

const resetForm = (formEl: FormInstance | undefined) => {
    if (!formEl) return;
    formEl.resetFields();
};
</script>

<style scoped>
.login-container {
    min-height: 100vh;
    min-width: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    background: url("/bg.jpg");
    background-size: cover;
    padding: 20px;
    position: relative;
    overflow: hidden;
}

.login-card {
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(10px);
    border-radius: 20px;
    padding: 40px;
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
    width: 100%;
    max-width: 420px;
    position: relative;
    z-index: 10;
    border: 1px solid rgba(255, 255, 255, 0.3);
}

.login-form {
    width: 100%;
}

.login-header {
    text-align: center;
    margin-bottom: 40px;
}

.login-logo {
    width: 280px;
    height: auto;
    margin-bottom: 20px;
    filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.1));
}

.login-title {
    color: rgb(66,133,244);
    font-size: 28px;
    font-weight: 600;
    margin: 0;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.form-item {
    margin-bottom: 25px;
}

.form-item :deep(.el-form-item__label) {
    font-size: 16px;
    color: #333;
    font-weight: 500;
    margin-bottom: 8px;
}

.custom-input :deep(.el-input__wrapper) {
    border-radius: 12px;
    border: 2px solid #e8f4fd;
    box-shadow: none;
    transition: all 0.3s ease;
    background: #f8fbff;
}

.custom-input :deep(.el-input__wrapper):hover {
    border-color: rgb(66,133,244);
    background: #fff;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
    border-color: rgb(66,133,244);
    box-shadow: 0 0 0 3px rgba(66,133,244, 0.1);
    background: #fff;
}

.custom-input :deep(.el-input__inner) {
    font-size: 16px;
    color: #333;
    height: 24px;
}

.custom-input :deep(.el-input__prefix-inner) {
    color: rgb(66,133,244);
}

.button-group {
    margin-top: 35px;
    display: flex;
    gap: 15px;
}

.login-button {
    flex: 2;
    height: 48px;
    border-radius: 12px;
    font-size: 16px;
    font-weight: 600;
    background: linear-gradient(135deg, rgb(66,133,244), rgb(52,108,201));
    border: none;
    box-shadow: 0 4px 12px rgba(66,133,244, 0.3);
    transition: all 0.3s ease;
}

.login-button:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(66,133,244, 0.4);
    background: linear-gradient(135deg, rgb(52,108,201), rgb(66,133,244));
}

.login-button:active {
    transform: translateY(0);
}

.reset-button {
    flex: 1;
    height: 48px;
    border-radius: 12px;
    font-size: 16px;
    font-weight: 500;
    background: #fff;
    border: 2px solid #e8f4fd;
    color: #666;
    transition: all 0.3s ease;
}

.reset-button:hover {
    border-color: rgb(66,133,244);
    color: rgb(66,133,244);
    background: #f8fbff;
}

/* Background decoration */
.bg-decoration {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    pointer-events: none;
    overflow: hidden;

    z-index: 2;
    /*filter: blur(3px);*/
    backdrop-filter: blur(3px);
}

.circle {
    position: absolute;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.15);
    animation: float 6s ease-in-out infinite;
}


@keyframes float {
    0%, 100% {
        transform: translateY(0px) scale(1);
        opacity: 0.7;
    }
    50% {
        transform: translateY(-20px) scale(1.05);
        opacity: 0.9;
    }
}

/* Form validation styles */
.form-item :deep(.el-form-item__error) {
    color: #f56c6c;
    font-size: 14px;
    margin-top: 5px;
}

.form-item.is-error :deep(.el-input__wrapper) {
    border-color: #f56c6c;
    box-shadow: 0 0 0 2px rgba(245, 108, 108, 0.1);
}

/* Responsive design */
@media (max-width: 480px) {
    .login-card {
        padding: 30px 25px;
        margin: 10px;
    }

    .login-logo {
        width: 240px;
    }

    .login-title {
        font-size: 24px;
    }

    .button-group {
        flex-direction: column;
    }

    .login-button,
    .reset-button {
        flex: none;
    }
}
</style>
