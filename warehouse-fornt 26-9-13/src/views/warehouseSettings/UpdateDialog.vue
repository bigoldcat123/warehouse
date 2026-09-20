<template>
  <el-dialog v-model="visible" :before-close="handleClose" width="680" :show-close="false" class="dark-dialog"
    :modal-class="'dark-dialog__overlay'">
    <!-- 自定义标题 -->
    <template #header>
      <div class="dialog-header">
        <svg class="w-6 h-6 text-[#64b5f6] flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
        </svg>
        <span class="text-white text-lg font-semibold">编辑基础信息设置</span>
        <button class="close-btn" @click="$emit('close')" aria-label="关闭">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
    </template>

    <div class="p-6">
      <el-form ref="ruleFormRef" :model="ruleForm" status-icon :rules="rules" label-width="130px"
        class="dark-form">

        <div class="grid grid-cols-2 gap-x-6">
          <el-form-item label="设置 ID">
            <el-input disabled v-model="ruleForm.id" class="dark-input dark-input--disabled" />
          </el-form-item>
          <el-form-item label="仓房编号">
            <el-input disabled v-model="ruleForm.houseNo" class="dark-input dark-input--disabled" />
          </el-form-item>

          <el-form-item label="温度报警上限(℃)" prop="temperatureMax">
            <el-input v-model.number="ruleForm.temperatureMax" placeholder="请输入温度报警上限" class="dark-input" />
          </el-form-item>
          <el-form-item label="温度采集时间" prop="temperatureCollectTime">
            <el-time-select v-model="ruleForm.temperatureCollectTime" start="00:00" step="00:30" end="23:30"
              format="HH:mm:ss" placeholder="选择温度采集时间" style="width: 100%" class="dark-select" />
          </el-form-item>
          <el-form-item label="温度采集间隔(时)" prop="temperatureIntervalHours">
            <el-input v-model.number="ruleForm.temperatureIntervalHours" placeholder="请输入温度采集间隔小时数" class="dark-input" />
          </el-form-item>

          <el-form-item label="湿度报警上限(%)" prop="humidityMax">
            <el-input v-model.number="ruleForm.humidityMax" placeholder="请输入湿度报警上限" class="dark-input" />
          </el-form-item>
          <el-form-item label="湿度采集时间" prop="humidityCollectTime">
            <el-time-select v-model="ruleForm.humidityCollectTime" start="00:00" step="00:30" end="23:30"
              format="HH:mm:ss" placeholder="选择湿度采集时间" style="width: 100%" class="dark-select" />
          </el-form-item>
          <el-form-item label="湿度采集间隔(时)" prop="humidityIntervalHours">
            <el-input v-model.number="ruleForm.humidityIntervalHours" placeholder="请输入湿度采集间隔小时数" class="dark-input" />
          </el-form-item>

          <el-form-item label="气体报警上限(ppm)" prop="gasMax">
            <el-input v-model.number="ruleForm.gasMax" placeholder="请输入气体报警上限" class="dark-input" />
          </el-form-item>
          <el-form-item label="气体采集时间" prop="gasCollectTime">
            <el-time-select v-model="ruleForm.gasCollectTime" start="00:00" step="00:30" end="23:30"
              format="HH:mm:ss" placeholder="选择气体采集时间" style="width: 100%" class="dark-select" />
          </el-form-item>
          <el-form-item label="气体采集间隔(时)" prop="gasIntervalHours">
            <el-input v-model.number="ruleForm.gasIntervalHours" placeholder="请输入气体采集间隔小时数" class="dark-input" />
          </el-form-item>
        </div>

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
import warehouseSettings, { type type_WarehouseSettings } from '@/api/warehouseSettings';
const prop = defineProps<{
  dialogVisible: boolean,
  settings: type_WarehouseSettings | undefined
}>()
const emit = defineEmits<{
  (event: 'close'): void,
  (event: 'refresh'): void
}>()
const visible = ref(false)
const handleClose = (done: () => void) => {
  emit('close')
}
const ruleFormRef = ref<FormInstance>()

onUpdated(() => {
  visible.value = prop.dialogVisible
  ruleForm.id = prop.settings!.id as number
  ruleForm.houseNo = prop.settings!.houseNo
  ruleForm.temperatureMax = prop.settings!.temperatureMax
  ruleForm.temperatureCollectTime = prop.settings!.temperatureCollectTime
  ruleForm.temperatureIntervalHours = prop.settings!.temperatureIntervalHours
  ruleForm.humidityMax = prop.settings!.humidityMax
  ruleForm.humidityCollectTime = prop.settings!.humidityCollectTime
  ruleForm.humidityIntervalHours = prop.settings!.humidityIntervalHours
  ruleForm.gasMax = prop.settings!.gasMax
  ruleForm.gasCollectTime = prop.settings!.gasCollectTime
  ruleForm.gasIntervalHours = prop.settings!.gasIntervalHours
})

const validateRequired = (message: string) => (rule: any, value: any, callback: any) => {
  if (value === '' || value === null || value === undefined) {
    return callback(new Error(message))
  }
  return callback()
}
const validateNumber = (message: string) => (rule: any, value: any, callback: any) => {
  if (value === '' || value === null || value === undefined) {
    return callback()
  }
  if (typeof value !== 'number' || isNaN(value)) {
    return callback(new Error(message))
  }
  return callback()
}
const validateInterval = (rule: any, value: any, callback: any) => {
  if (value === '' || value === null || value === undefined) {
    return callback(new Error('请输入采集间隔小时数'))
  }
  if (typeof value !== 'number' || isNaN(value) || value <= 0 || !Number.isInteger(value)) {
    return callback(new Error('请输入大于 0 的整数'))
  }
  return callback()
}

const ruleForm = reactive({
  id: 0,
  houseNo: '',
  temperatureMax: null as number | null,
  temperatureCollectTime: '',
  temperatureIntervalHours: null as number | null,
  humidityMax: null as number | null,
  humidityCollectTime: '',
  humidityIntervalHours: null as number | null,
  gasMax: null as number | null,
  gasCollectTime: '',
  gasIntervalHours: null as number | null,
})

const rules = reactive<FormRules<typeof ruleForm>>({
  temperatureMax: [{ validator: validateRequired('请输入温度报警上限'), trigger: 'blur' },
  { validator: validateNumber('请输入数字'), trigger: 'blur' }],
  temperatureCollectTime: [{ validator: validateRequired('请选择温度采集时间'), trigger: 'change' }],
  temperatureIntervalHours: [{ validator: validateInterval, trigger: 'blur' }],
  humidityMax: [{ validator: validateRequired('请输入湿度报警上限'), trigger: 'blur' },
  { validator: validateNumber('请输入数字'), trigger: 'blur' }],
  humidityCollectTime: [{ validator: validateRequired('请选择湿度采集时间'), trigger: 'change' }],
  humidityIntervalHours: [{ validator: validateInterval, trigger: 'blur' }],
  gasMax: [{ validator: validateRequired('请输入气体报警上限'), trigger: 'blur' },
  { validator: validateNumber('请输入数字'), trigger: 'blur' }],
  gasCollectTime: [{ validator: validateRequired('请选择气体采集时间'), trigger: 'change' }],
  gasIntervalHours: [{ validator: validateInterval, trigger: 'blur' }],
})


const submitForm = (formEl: FormInstance | undefined) => {
  if (!formEl) return
  formEl.validate((valid) => {
    if (valid) {
      warehouseSettings.update(ruleForm).then(res => {
        emit('refresh')
        emit('close')
      })
    }
  })
}


</script>
<style scoped>
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

.dark-input--disabled .el-input__wrapper {
  background: rgba(255, 255, 255, 0.04) !important;
  border-color: rgba(255, 255, 255, 0.1) !important;
  cursor: not-allowed;
}

.dark-input--disabled .el-input__inner {
  color: #b0d0f0 !important;
  opacity: 0.6;
}

/* 选择器（采集时间下拉） */
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

/* 下拉弹出层（挂载到 body，需全局覆盖） */
.el-popper.el-select__popper {
  background: #1a3a5c !important;
  border: 1px solid rgba(255, 255, 255, 0.15) !important;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.5) !important;
}

.el-popper.el-select__popper .el-select-dropdown__item {
  color: #b0d0f0 !important;
}

.el-popper.el-select__popper .el-select-dropdown__item:hover,
.el-popper.el-select__popper .el-select-dropdown__item.is-hovering {
  background: rgba(30, 136, 229, 0.25) !important;
  color: white !important;
}

.el-popper.el-select__popper .el-select-dropdown__item.is-selected {
  background: rgba(30, 136, 229, 0.4) !important;
  color: white !important;
  font-weight: 600;
}

.el-popper.el-select__popper .el-popper__arrow::before {
  background: #1a3a5c !important;
  border-color: rgba(255, 255, 255, 0.15) !important;
}

/* 遮罩层 */
.el-overlay {
  background: rgba(0, 0, 0, 0.7) !important;
  backdrop-filter: blur(4px);
}
</style>