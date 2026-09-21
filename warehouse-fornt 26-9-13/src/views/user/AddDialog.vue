<template>
  <el-dialog v-model="visiable" :before-close="handleClose" width="560" :show-close="false" class="dark-dialog"
    :modal-class="'dark-dialog__overlay'">
    <!-- 自定义标题 -->
    <template #header>
      <div class="dialog-header">
        <svg class="w-6 h-6 text-[#64b5f6] flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
        </svg>
        <span class="text-white text-lg font-semibold">新增用户</span>
        <button class="close-btn" @click="$emit('close')" aria-label="关闭">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
    </template>

    <div class="p-6">
      <el-form ref="ruleFormRef" style="max-width: 600px" :model="ruleForm" status-icon :rules="rules" label-width="80px"
        class="dark-form">

        <el-form-item label="用户名" prop="username">
          <el-input v-model="ruleForm.username" placeholder="请输入用户名" class="dark-input" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input type="password" v-model="ruleForm.password" placeholder="请输入密码" class="dark-input" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="ruleForm.name" placeholder="请输入姓名" class="dark-input" />
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-select v-model="ruleForm.sex" placeholder="选择性别" class="dark-select">
            <el-option key="男" label="男" value="男" />
            <el-option key="女" label="女" value="女" />
          </el-select>
        </el-form-item>
        <el-form-item label="公司" prop="companyID">
          <el-select v-model="ruleForm.companyID" placeholder="选择公司" class="dark-select">
            <el-option v-for="item in kv" :key="item.key" :label="item.value" :value="item.key" />
          </el-select>
        </el-form-item>
        <el-form-item label="职位" prop="position">
          <el-input v-model="ruleForm.position" placeholder="请输入职位" class="dark-input" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="ruleForm.phone" placeholder="请输入手机号" class="dark-input" />
        </el-form-item>
        <el-form-item label="权利">
          <div class="priv-chips">
            <div v-for="(item, key) in privList" :key="key" @click="add_del_Priv(Number(key))" class="priv-chip"
              :class="[
                privLChecked.includes(Number(key)) ? 'priv-chip--active' : '',
                privChipColor(Number(key))
              ]">
              {{ item }}
            </div>
          </div>
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
import user, { type type_User } from '@/api/user';
const prop = defineProps<{
  dialogVisible: boolean,
  kv: any[],
  privList: string[]
}>()
const emit = defineEmits<{
  (event: 'close'): void,
  (event: 'refresh'): void
}>()
const visiable = ref(false)
onUpdated(() => {
  visiable.value = prop.dialogVisible
})
const privLChecked = ref<number[]>([])
const handleClose = (done: () => void) => {
  emit('close')
}
const add_del_Priv = (key: number) => {
  privLChecked.value.includes(key) ? privLChecked.value.splice(privLChecked.value.indexOf(key), 1) : privLChecked.value.push(key)
}

// 权利芯片颜色映射
const privChipColor = (key: number) => {
  const map: Record<number, string> = {
    0: 'priv-chip--success',
    1: 'priv-chip--primary',
    2: 'priv-chip--danger',
    3: 'priv-chip--warning',
  }
  return map[key] || ''
}

const ruleFormRef = ref<FormInstance>()

const validate = (rule: any, value: any, callback: any) => {
  if (!value) {
    return callback(new Error('不能为空'))
  }
  return callback()
}
const validatePhoneNumber = (rule: any, value: any, callback: any) => {
  if (!value) {
    return callback(new Error('不能为空'))
  }
  if (!/[1234567890]{11}$/.test(value)) {
    return callback(new Error('请输入正确的手机号码'))
  }
  return callback()
}

const ruleForm = reactive<any>({
  username: '',
  password: '',
  name: '',
  sex: '',
  companyID: '-1',
  position: '',
  phone: '',
  priv: null,
})

const rules = reactive<FormRules<typeof ruleForm>>({
  username: [{ validator: validate, trigger: 'blur' }],
  password: [{ validator: validate, trigger: 'blur' }],
  name: [{ validator: validate, trigger: 'blur' }],
  sex: [{ validator: validate, trigger: 'blur' }],
  companyID: [{ validator: validate, trigger: 'blur' }],
  position: [{ validator: validate, trigger: 'blur' }],
  phone: [{ validator: validatePhoneNumber, trigger: 'blur' }],
})


const submitForm = (formEl: FormInstance | undefined) => {
  if (!formEl) return
  formEl.validate((valid) => {
    if (valid) {
      console.log('submit!')
      ruleForm.priv = privLChecked.value.join(',')
      user.add(ruleForm).then(res => {
        emit('refresh')
        emit('close')
        ruleForm.username = ''
        ruleForm.password = ''
        ruleForm.name = ''
        ruleForm.sex = ''
        ruleForm.companyID = '-1'
        ruleForm.position = ''
        ruleForm.phone = ''
        privLChecked.value = []
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

/* 权利选择芯片 */
.priv-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.priv-chip {
  display: inline-flex;
  align-items: center;
  padding: 6px 14px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;
  font-size: 13px;
  color: #b0d0f0;
  font-weight: 500;
}

.priv-chip:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.3);
}

/* 激活态基础样式 */
.priv-chip--active {
  font-weight: 600;
  color: white;
}

/* 颜色变体 */
.priv-chip--success.priv-chip--active {
  background: rgba(67, 160, 71, 0.3);
  border-color: #43A047;
}

.priv-chip--primary.priv-chip--active {
  background: rgba(30, 136, 229, 0.3);
  border-color: #1E88E5;
}

.priv-chip--danger.priv-chip--active {
  background: rgba(229, 57, 53, 0.3);
  border-color: #E53935;
}

.priv-chip--warning.priv-chip--active {
  background: rgba(251, 140, 0, 0.3);
  border-color: #FB8C00;
}
</style>
<style>
/* 全局覆盖 el-dialog 样式（深蓝主题） */
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

/* 表单样式 */
.dark-form .el-form-item__label {
  color: #b0d0f0 !important;
  font-weight: 500;
}

.dark-form .el-form-item__error {
  color: #F44336 !important;
}

/* 输入框样式 */
.dark-input .el-input__wrapper,
.dark-form .el-input__wrapper {
  background: rgba(255, 255, 255, 0.08) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  box-shadow: none !important;
  border-radius: 6px !important;
  color: white !important;
}

.dark-input .el-input__wrapper:hover,
.dark-input .el-input__wrapper.is-focus,
.dark-form .el-input__wrapper:hover,
.dark-form .el-input__wrapper.is-focus {
  background: rgba(255, 255, 255, 0.12) !important;
  border-color: #1E88E5 !important;
}

.dark-input .el-input__inner,
.dark-form .el-input__inner {
  color: white !important;
}

.dark-input .el-input__inner::placeholder,
.dark-form .el-input__inner::placeholder {
  color: rgba(255, 255, 255, 0.35) !important;
}

/* 选择器样式 */
.dark-select .el-select__wrapper {
  background: rgba(255, 255, 255, 0.08) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  box-shadow: none !important;
  border-radius: 6px !important;
  width: 240px;
}

.dark-select .el-select__wrapper:hover,
.dark-select .el-select__wrapper.is-focused {
  background: rgba(255, 255, 255, 0.12) !important;
  border-color: #1E88E5 !important;
}

.dark-select .el-select__placeholder {
  color: rgba(255, 255, 255, 0.35) !important;
}

.dark-select .el-select__selected-item {
  color: white !important;
}

.dark-select .el-select__caret {
  color: #b0d0f0 !important;
}

/* 下拉弹出层 */
.el-popper.el-select__popper {
  background: #1a3a5c !important;
  border: 1px solid rgba(255, 255, 255, 0.15) !important;
}

.el-popper.el-select__popper .el-select-dropdown__item {
  color: #b0d0f0 !important;
}

.el-popper.el-select__popper .el-select-dropdown__item:hover,
.el-popper.el-select__popper .el-select-dropdown__item.is-hovering {
  background: rgba(30, 136, 229, 0.3) !important;
  color: white !important;
}

.el-popper.el-select__popper .el-select-dropdown__item.is-selected {
  background: rgba(30, 136, 229, 0.4) !important;
  color: white !important;
  font-weight: 600;
}

.el-popper.el-select__popper .el-scrollbar__bar.is-horizontal {
  display: none;
}

/* 遮罩层 */
.el-overlay {
  background: rgba(0, 0, 0, 0.7) !important;
  backdrop-filter: blur(4px);
}
</style>
