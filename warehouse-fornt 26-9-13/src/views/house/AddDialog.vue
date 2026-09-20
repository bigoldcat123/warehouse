<template>
  <el-dialog
    v-model="visible"
    :before-close="handleClose"
    width="520"
    :show-close="false"
    class="dark-dialog"
    :modal-class="'dark-dialog__overlay'"
  >
    <!-- 自定义标题 -->
    <template #header>
      <div class="dialog-header">
        <svg class="w-6 h-6 text-[#64b5f6] flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
        </svg>
        <span class="text-white text-lg font-semibold">新增仓房</span>
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

        <el-form-item label="仓房编号" prop="houseNo">
          <el-input v-model="ruleForm.houseNo" placeholder="请输入仓房编号" class="dark-input" />
        </el-form-item>
        <el-form-item label="仓房名" prop="houseName">
          <el-input v-model="ruleForm.houseName" placeholder="请输入仓房名称" class="dark-input" />
        </el-form-item>
        <el-form-item label="仓房地址" prop="houseAddr">
          <el-input v-model="ruleForm.houseAddr" placeholder="请输入仓房地址" class="dark-input" />
        </el-form-item>
        <el-form-item label="仓房类型" prop="houseType">
          <el-select v-model="ruleForm.houseType" placeholder="请选择类型" style="width: 240px" class="dark-select">
            <el-option key="平房仓" label="平房仓" value="平房仓" />
            <el-option key="筒仓" label="筒仓" value="筒仓" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属仓库" prop="warehouseID">
          <el-select v-model="ruleForm.warehouseID" placeholder="请选择仓库" style="width: 240px" class="dark-select">
            <el-option v-for="item in kv" :key="item.key" :label="item.value" :value="item.key" />
          </el-select>
        </el-form-item>
        <el-form-item label="列数(X)" prop="y">
          <el-input v-model.number="ruleForm.y" placeholder="请输入列数" class="dark-input" />
        </el-form-item>
        <el-form-item label="行数(Y)" prop="x">
          <el-input v-model.number="ruleForm.x" placeholder="请输入行数" class="dark-input" />
        </el-form-item>

        <el-form-item label="层数(Z)" prop="z">
          <el-input v-model.number="ruleForm.z" placeholder="请输入层数" class="dark-input" />
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
import house from '@/api/house';
const prop = defineProps<{
  dialogVisible: boolean,
  kv:any[]
}>()
const emit = defineEmits<{
  (event: 'close'): void,
  (event: 'refresh'): void
}>()
const visible = ref(false)
function submit() {
  emit('close')
}
const handleClose = (done: () => void) => {
  emit('close')
}
onUpdated(() => {
  visible.value = prop.dialogVisible
})
const ruleFormRef = ref<FormInstance>()

const validate = (rule: any, value: any, callback: any) => {
  if (!value) {
    return callback(new Error('不能为空'))
  }
  return callback()
}



const ruleForm = reactive<any>({
  houseNo: '',
  houseName: '',
  warehouseID: undefined,
  houseAddr: '',
  houseType: '',
  z: undefined,
  x: undefined,
  y: undefined,
})

const rules = reactive<FormRules<typeof ruleForm>>({
  houseNo: [{ validator: validate, trigger: 'blur' }],
  houseName: [{ validator: validate, trigger: 'blur' }],
  warehouseID: [{ validator: validate, trigger: 'blur' }],
  houseAddr: [{ validator: validate, trigger: 'blur' }],
  houseType: [{ validator: validate, trigger: 'blur' }],
  x: [{ validator: validate, trigger: 'blur' }],
  y: [{ validator: validate, trigger: 'blur' }],
  z: [{ validator: validate, trigger: 'blur' }],
})


const submitForm = (formEl: FormInstance | undefined) => {
  if (!formEl) return
  formEl.validate((valid) => {
    if (valid) {
      console.log('submit!')
      house.add(ruleForm).then(res => {
        emit('refresh')
      })
      ruleForm.houseNo = ''
      ruleForm.houseName = ''
      ruleForm.warehouseID = 0
      ruleForm.houseAddr = ''
      ruleForm.houseType = ''
      ruleForm.z = 0
      ruleForm.x = 0
      ruleForm.y = 0
      emit('close')
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
.el-select-dropdown.is-dark-dialog,
.el-popper.el-select__popper {
  background: #1a3a5c !important;
  border: 1px solid rgba(255, 255, 255, 0.15) !important;
}
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
