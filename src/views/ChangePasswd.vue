<template>
  <el-dialog
    v-model="v"
    :before-close="handleClose"
    width="480"
    :show-close="false"
    class="dark-dialog"
    :modal-class="'dark-dialog__overlay'"
  >
    <!-- 自定义标题 -->
    <template #header>
      <div class="dialog-header">
        <svg class="w-6 h-6 text-[#64b5f6] flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
        </svg>
        <span class="text-white text-lg font-semibold">修改密码</span>
        <button class="close-btn" @click="$emit('close')" aria-label="关闭">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
    </template>

    <div class="p-6">
      <el-form ref="ruleFormRef" style="max-width: 600px" :model="ruleForm" status-icon :rules="rules"
        label-width="90px" class="dark-form">
        <el-form-item label="旧密码" prop="old">
          <el-input type="password" v-model="ruleForm.old" placeholder="请输入旧密码" class="dark-input" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newP">
          <el-input type="password" v-model="ruleForm.newP" placeholder="请输入新密码" class="dark-input" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="rectify">
          <el-input type="password" v-model="ruleForm.rectify" placeholder="请再次输入新密码" class="dark-input" show-password />
        </el-form-item>
        <el-form-item>
          <div class="flex items-center gap-3">
            <button type="button" class="btn-primary" @click="submitForm(ruleFormRef)">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
              提交
            </button>
            <button type="button" class="btn-cancel" @click="$emit('close')">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
              取消
            </button>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </el-dialog>
</template>
<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus';
import { onUpdated, reactive, ref } from 'vue'
import user from '@/api/user';
const prop = defineProps<{
  dialogVisible: boolean
}>()
const emit = defineEmits<{
  (event: 'close'): void,
  (event: 'refresh'): void
}>()
const handleClose = (done: () => void) => {
  emit('close')
}
const ruleFormRef = ref<FormInstance>()

const validate = (rule: any, value: any, callback: any) => {
  if (!value) {
      return callback(new Error('不能为空'))
  }
  if(value.length >= 200){
      return callback(new Error("长度超出限制 -> " + value.length + "/200"))
  }
  return callback()
}

const validateR = (rule: any, value: any, callback: any) => {
  if (!value) {
      return callback(new Error('不能为空'))
  }
  if(value.length >= 200){
      return callback(new Error("长度超出限制 -> " + value.length + "/200"))
  }
  if(value != ruleForm.newP){
      return callback(new Error("两次密码不一致"))
  }
  return callback()
}

const v = ref(false)
onUpdated(() => {
  v.value = prop.dialogVisible
})
const ruleForm = reactive({
  old: '',
  newP:'',
  rectify:''
})

const rules = reactive<FormRules<typeof ruleForm>>({
  old: [{ validator: validate, trigger: 'blur' }],
  newP: [{ validator: validate, trigger: 'blur' }],
  rectify: [{ validator: validateR, trigger: 'blur' }],
})


const submitForm = (formEl: FormInstance | undefined) => {
  if (!formEl) return
  formEl.validate((valid) => {
      if (valid) {
          user.changePasswd(ruleForm.old,ruleForm.newP).then(res => {

          emit('close')
          })
      } else {
          console.log('error submit!')
      }
  })
}


</script>
<style scoped>
/* 对话框头部 */
.dialog-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 20px;
  background: linear-gradient(135deg, #2968a8 0%, #1a3a5c 100%);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}
.close-btn {
  margin-left: auto;
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 4px;
  color: #b0d0f0;
  padding: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}
.close-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border-color: rgba(255, 255, 255, 0.4);
}

/* 按钮 */
.btn-primary {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 20px;
  background: linear-gradient(135deg, #1E88E5 0%, #1565C0 100%);
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
}
.btn-primary:hover {
  background: linear-gradient(135deg, #1976D2 0%, #0D47A1 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3);
}
.btn-cancel {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 20px;
  background: rgba(255, 255, 255, 0.08);
  color: #b0d0f0;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}
.btn-cancel:hover {
  background: rgba(255, 255, 255, 0.15);
  color: white;
  border-color: rgba(255, 255, 255, 0.4);
}
</style>
<style>
/* 深色对话框 */
.dark-dialog.el-dialog {
  background: #1a3a5c !important;
  border-radius: 10px !important;
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.6) !important;
  overflow: hidden;
}
.dark-dialog .el-dialog__body {
  background: #1a3a5c !important;
  padding: 0 !important;
  color: white;
}
.dark-dialog .el-dialog__header {
  display: none;
}

/* 表单 */
.dark-form .el-form-item__label {
  color: #b0d0f0 !important;
  font-weight: 500;
}
.dark-form .el-form-item__error {
  color: #F44336 !important;
}

/* 输入框 */
.dark-input .el-input__wrapper {
  background: rgba(255, 255, 255, 0.08) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  box-shadow: none !important;
  border-radius: 6px !important;
  color: white !important;
}
.dark-input .el-input__wrapper:hover,
.dark-input .el-input__wrapper.is-focus {
  background: rgba(255, 255, 255, 0.12) !important;
  border-color: #1E88E5 !important;
}
.dark-input .el-input__inner {
  color: white !important;
}
.dark-input .el-input__inner::placeholder {
  color: rgba(255, 255, 255, 0.35) !important;
}
.dark-input .el-input__suffix {
  color: #b0d0f0 !important;
}
.dark-input .el-input__suffix .el-icon:hover {
  color: white !important;
}

/* 遮罩 */
.el-overlay {
  background: rgba(0, 0, 0, 0.7) !important;
  backdrop-filter: blur(4px);
}
</style>
