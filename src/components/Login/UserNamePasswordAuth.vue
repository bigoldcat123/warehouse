<template>


    <el-form ref="ruleFormRef" style="max-width: 100%;" :model="ruleForm" status-icon :rules="rules" label-width="95px"
    class="demo-ruleForm" >
    <p style="font-size: 28px;color:white;text-align: center;" ><img src="../../assets/image/login_title.png" width="300px"></p>
    <p> &emsp;</p>

    <el-form-item label="用户名" prop="username" class="item" >
        <el-input v-model="ruleForm.username" type="text" size="small" style="width: 150px;" autocomplete="off" />
    </el-form-item>
    <p> &emsp;</p>
    <el-form-item label="密  码" prop="password" class="item">
        <el-input v-model="ruleForm.password" type="password" size="small" style="width: 150px" autocomplete="off" />
    </el-form-item>
    <p> &emsp;</p>
    <el-form-item>
        <el-button type="primary"  @click="submitForm(ruleFormRef)">确 定</el-button>
        <el-button @click="resetForm(ruleFormRef)">重 置</el-button>
    </el-form-item>
</el-form>
</template>
<script setup lang="ts">
import { reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import Auth from '@/api/auth';
import { useCurrentUserStore } from '@/stores/currentUser';
import { useRouter } from 'vue-router';
const router = useRouter()
const currentUser = useCurrentUserStore()
const ruleFormRef = ref<FormInstance>()

const checkUsername = (rule: any, value: string, callback: any) => {
    if (!value) {
        return callback(new Error('请输入用户名！'))
    }
    setTimeout(() => {
        if (value.length < 1) {
            callback(new Error('用户名太短!'))
        } else {
            callback()
        }
    }, 100)
}

const validatePass = (rule: any, value: any, callback: any) => {
    if (value === '') {
        callback(new Error('请输入密码！'))
    } else {
        callback()
    }
}

const ruleForm = reactive<DaoLoginUser>({
    username: '',
    password: ''
})

const rules = reactive<FormRules<DaoLoginUser>>({
    username: [{ validator: checkUsername, trigger: 'blur' }],
    password: [{ validator: validatePass, trigger: 'blur' }],
})

const submitForm = (formEl: FormInstance | undefined) => {
    if (!formEl) return
    formEl.validate((valid) => {
        if (valid) {
            Auth.daoLogin(ruleForm).then((res) => {
                currentUser.setValue(res.data.value)
                if(currentUser.isMainComp()) {
                    router.push('/alarmarg')
                }else{
                    router.push('/alarm')
                }
            }).catch((err) => {
                console.log(err);
            })
        } else {
            console.log('登录错误!')
        }
    })
}

const resetForm = (formEl: FormInstance | undefined) => {
    if (!formEl) return
    formEl.resetFields()
}
</script>
<style>
.item .el-form-item__label{
    font-size: 16px;
    color:rgb(8, 8, 8);
  }
</style>