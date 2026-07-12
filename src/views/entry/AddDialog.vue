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
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
        </svg>
        <span class="text-white text-lg font-semibold">入库登记</span>
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

        <el-form-item label="仓库" prop="houseID">
          <el-select @change="warehouseChange" v-model="p" placeholder="请选择仓库" style="width: 240px" class="dark-select">
            <el-option v-for="item in warehouekv" :key="item.key" :label="item.value" :value="item.key" />
          </el-select>
        </el-form-item>
        <el-form-item label="仓房" prop="houseID">
          <el-select v-model="ruleForm.houseID" placeholder="请选择仓房" style="width: 240px" class="dark-select">
            <el-option v-for="item in kv" :key="item.key" :label="item.value" :value="item.key" />
          </el-select>
        </el-form-item>
        <el-form-item label="管理人员" prop="stockman">
          <el-select v-model="ruleForm.stockman" placeholder="请选择管理员" style="width: 240px" class="dark-select">
            <el-option v-for="item in userKv" :key="item.key" :label="item.value" :value="item.key" />
          </el-select>
        </el-form-item>
        <el-form-item label="品种" prop="breed">
          <el-input v-model="ruleForm.breed" placeholder="请输入品种" class="dark-input" />
        </el-form-item>
        <el-form-item label="入库时间" prop="entryTime">
          <el-date-picker
            v-model="ruleForm.entryTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="选择入库时间"
            class="dark-date"
            :teleported="false"
          />
        </el-form-item>
        <el-form-item label="含水量" prop="water">
          <el-input-number v-model="ruleForm.water" :precision="2" :step="0.1" :max="99999" class="dark-number" />
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
import { reactive, ref } from 'vue'
import house from '@/api/house';
import warehouse from '@/api/warehouse';
import entry from '@/api/entry';
import user from '@/api/user';
const p = ref()
const prop = defineProps<{
  dialogVisible: boolean,
}>()
const emit = defineEmits<{
  (event: 'close'): void,
  (event: 'refresh'): void
}>()
const kv = ref<any[]>([])

const warehouekv = ref<any[]>([])
warehouse.belongKv().then(res => {
  warehouekv.value = res.data.value
})
const userKv = ref<{ key: string, value: string }[]>([])

function warehouseChange(value: any) {
  house.findByWarehouseId(value).then(res => {
    kv.value = res.data.value
  })
  user.getUsersByWarehouseId(value).then(res => {
    userKv.value = res.data.value
  })
  ruleForm.houseID = undefined
  ruleForm.stockman = undefined
}
const handleClose = (done: () => void) => {
  emit('close')
}
const ruleFormRef = ref<FormInstance>()

const validate = (rule: any, value: any, callback: any) => {
  if (!value) {
    return callback(new Error('不能为空'))
  }
  return callback()
}

const ruleForm = reactive({
  houseID: undefined,
  stockman: undefined,
  breed: '',
  entryTime: '',
  water: undefined
})

const rules = reactive<FormRules<typeof ruleForm>>({
  houseID: [{ validator: validate, trigger: 'blur' }],
  stockman: [{ validator: validate, trigger: 'blur' }],
  breed: [{ validator: validate, trigger: 'blur' }],
  entryTime: [{ validator: validate, trigger: 'blur' }],
  water: [{ validator: validate, trigger: 'blur' }],
})


const submitForm = (formEl: FormInstance | undefined) => {
  if (!formEl) return
  formEl.validate((valid) => {
    if (valid) {
      console.log('submit!')
      entry.add(ruleForm).then(res => {
        emit('refresh')
      })
      ruleForm.houseID = undefined
      ruleForm.breed = ''
      ruleForm.entryTime = ''
      ruleForm.water = undefined
      ruleForm.stockman = undefined
      emit('close')
    } else {
      console.log('error submit!')
    }
  })
}

const visible = ref(false)
import { onUpdated } from 'vue'
onUpdated(() => {
  visible.value = prop.dialogVisible
})
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
  background: linear-gradient(135deg, #43A047 0%, #2E7D32 100%);
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
  background: linear-gradient(135deg, #388E3C 0%, #1B5E20 100%);
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
  border-color: #43A047 !important;
}
.dark-input .el-input__inner,
.dark-form .el-input__inner {
  color: white !important;
}
.dark-input .el-input__inner::placeholder,
.dark-form .el-input__inner::placeholder {
  color: rgba(255, 255, 255, 0.35) !important;
}

/* 选择器 */
.dark-select .el-select__wrapper {
  background: rgba(255, 255, 255, 0.08) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  box-shadow: none !important;
  border-radius: 6px !important;
}
.dark-select .el-select__wrapper:hover,
.dark-select .el-select__wrapper.is-focused {
  background: rgba(255, 255, 255, 0.12) !important;
  border-color: #43A047 !important;
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

/* 日期选择器 */
.dark-date .el-input__wrapper {
  background: rgba(255, 255, 255, 0.08) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  box-shadow: none !important;
  border-radius: 6px !important;
}
.dark-date .el-input__wrapper:hover,
.dark-date .el-input__wrapper.is-focus {
  background: rgba(255, 255, 255, 0.12) !important;
  border-color: #43A047 !important;
}
.dark-date .el-input__inner {
  color: white !important;
}
.dark-date .el-input__inner::placeholder {
  color: rgba(255, 255, 255, 0.35) !important;
}
.dark-date .el-input__prefix,
.dark-date .el-input__suffix {
  color: #b0d0f0 !important;
}

/* 数字输入 */
.dark-number .el-input-number__decrease,
.dark-number .el-input-number__increase {
  background: rgba(255, 255, 255, 0.08) !important;
  border-color: rgba(255, 255, 255, 0.2) !important;
  color: #b0d0f0 !important;
}
.dark-number .el-input-number__decrease:hover,
.dark-number .el-input-number__increase:hover {
  color: #43A047 !important;
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
  background: rgba(67, 160, 71, 0.3) !important;
  color: white !important;
}
.el-popper.el-select__popper .el-select-dropdown__item.is-selected {
  background: rgba(67, 160, 71, 0.4) !important;
  color: white !important;
  font-weight: 600;
}

/* 遮罩 */
.el-overlay {
  background: rgba(0, 0, 0, 0.7) !important;
  backdrop-filter: blur(4px);
}
</style>
