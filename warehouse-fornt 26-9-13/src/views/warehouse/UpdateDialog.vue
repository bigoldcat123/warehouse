<template>
  <el-dialog v-model="visible" :before-close="handleClose" width="520" :show-close="false" class="dark-dialog"
    :modal-class="'dark-dialog__overlay'">
    <!-- 自定义标题 -->
    <template #header>
      <div class="dialog-header">
        <svg class="w-6 h-6 text-[#64b5f6] flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
        </svg>
        <span class="text-white text-lg font-semibold">编辑仓库</span>
        <button class="close-btn" @click="$emit('close')" aria-label="关闭">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
    </template>

    <div class="p-6">
      <el-form ref="ruleFormRef" style="max-width: 600px" :model="ruleForm" status-icon :rules="rules" label-width="90px"
        class="dark-form">

        <el-form-item label="仓库 ID">
          <el-input disabled v-model="ruleForm.id" class="dark-input dark-input--disabled" />
        </el-form-item>
        <el-form-item label="仓库编号" prop="warehouseNo">
          <el-input v-model="ruleForm.warehouseNo" placeholder="请输入仓库编号" class="dark-input" />
        </el-form-item>
        <el-form-item label="仓库名" prop="warehouseName">
          <el-input v-model="ruleForm.warehouseName" placeholder="请输入仓库名" class="dark-input" />
        </el-form-item>
        <el-form-item label="仓库地址" prop="warehouseAddress">
          <el-input v-model="ruleForm.warehouseAddress" placeholder="请输入仓库地址" class="dark-input" />
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
import { onMounted, onUpdated, reactive, ref } from 'vue'
import warehouse, { type type_WareHouse } from '@/api/warehouse';
const visible = ref(false)
const prop = defineProps<{
  dialogVisible: boolean,
  warehouse: type_WareHouse | undefined
}>()
const emit = defineEmits<{
  (event: 'close'): void,
  (event: 'refresh'): void
}>()
function submit() {
  emit('close')
}
const handleClose = (done: () => void) => {
  emit('close')
}
const ruleFormRef = ref<FormInstance>()

onUpdated(() => {
  visible.value = prop.dialogVisible
  ruleForm.id = prop.warehouse!.id as number
  ruleForm.warehouseAddress = prop.warehouse!.warehouseAddress
  ruleForm.warehouseName = prop.warehouse!.warehouseName
  ruleForm.warehouseNo = prop.warehouse!.warehouseNo
})

const validateWarehouseAddr = (rule: any, value: any, callback: any) => {
  if (!value) {
    return callback(new Error('请输入地址'))
  }
  return callback()
}

const validateWareHouseNo = (rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请输入仓库编号'))
  }
  callback()
}
const validateWarehouseName = (rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请输入仓库名'))
  } else {
    callback()
  }
}

const ruleForm = reactive({
  id: 0,
  warehouseNo: '',
  warehouseName: '',
  warehouseAddress: '',
})

const rules = reactive<FormRules<typeof ruleForm>>({
  warehouseNo: [{ validator: validateWareHouseNo, trigger: 'blur' }],
  warehouseName: [{ validator: validateWarehouseName, trigger: 'blur' }],
  warehouseAddress: [{ validator: validateWarehouseAddr, trigger: 'blur' }],
})


const submitForm = (formEl: FormInstance | undefined) => {
  if (!formEl) return
  formEl.validate((valid) => {
    if (valid) {
      console.log('submit!')
      warehouse.update(ruleForm).then(res => {
        emit('refresh')
      })
      ruleForm.warehouseAddress = ''
      ruleForm.warehouseName = ''
      ruleForm.warehouseNo = ''
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

/* disabled 输入框 */
.dark-input--disabled .el-input__wrapper {
  background: rgba(255, 255, 255, 0.04) !important;
  border-color: rgba(255, 255, 255, 0.1) !important;
  cursor: not-allowed;
}

.dark-input--disabled .el-input__inner {
  color: #b0d0f0 !important;
  opacity: 0.6;
}

/* 遮罩层 */
.el-overlay {
  background: rgba(0, 0, 0, 0.7) !important;
  backdrop-filter: blur(4px);
}
</style>
