<template>
  <el-dialog v-model="visible" :show-close="false" width="560" class="dark-dialog">
    <template #header>
      <div class="dialog-header">
        <span>{{ record?.id ? '编辑熏蒸记录' : '新增熏蒸记录' }}</span>
        <button class="close-btn" type="button" @click="close">×</button>
      </div>
    </template>

    <div class="dialog-body">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="dark-form">
        <el-form-item label="仓房编号" prop="houseNo">
          <el-select v-model="form.houseNo" placeholder="请选择仓房" filterable style="width: 100%">
            <el-option
              v-for="item in houses"
              :key="item.houseNo"
              :label="`${item.houseNo}${item.houseName ? ` - ${item.houseName}` : ''}`"
              :value="item.houseNo"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间" prop="startDt">
          <el-date-picker
            v-model="form.startDt"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择开始时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="stopDt">
          <el-date-picker
            v-model="form.stopDt"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="未结束时可不填写"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item>
          <div class="dialog-actions">
            <button type="button" class="btn-primary" @click="submit">保存</button>
            <button type="button" class="btn-cancel" @click="close">取消</button>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import type { type_House } from '@/api/house'
import xunzheng, { type type_Xunzheng } from '@/api/xunzheng'

const props = defineProps<{
  dialogVisible: boolean
  record?: type_Xunzheng
  houses: type_House[]
}>()
const emit = defineEmits<{
  (event: 'close'): void
  (event: 'refresh'): void
}>()

const visible = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<type_Xunzheng>({
  houseNo: '',
  startDt: '',
  stopDt: null
})

const rules: FormRules<typeof form> = {
  houseNo: [{ required: true, message: '请选择仓房', trigger: 'change' }],
  startDt: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  stopDt: [{
    validator: (_rule, value, callback) => {
      if (value && form.startDt && value < form.startDt) {
        callback(new Error('结束时间不能早于开始时间'))
        return
      }
      callback()
    },
    trigger: 'change'
  }]
}

watch(() => props.dialogVisible, (value) => {
  visible.value = value
  if (!value) return
  form.id = props.record?.id
  form.houseNo = props.record?.houseNo ?? ''
  form.startDt = props.record?.startDt ?? ''
  form.stopDt = props.record?.stopDt ?? null
  form.setDatetime = props.record?.setDatetime
  formRef.value?.clearValidate()
}, { immediate: true })

function close() {
  emit('close')
}

function submit() {
  formRef.value?.validate(async valid => {
    if (!valid) return
    const request = form.id ? xunzheng.update(form) : xunzheng.add(form)
    const response = await request
    if (response.data.code === '200') {
      emit('refresh')
      close()
    }
  })
}
</script>

<style scoped>
.dialog-header { display: flex; align-items: center; justify-content: space-between; color: white; font-size: 18px; font-weight: 600; }
.dialog-body { padding: 18px 10px 4px; }
.close-btn { border: 0; background: transparent; color: #b0d0f0; font-size: 26px; cursor: pointer; }
.dialog-actions { display: flex; gap: 12px; }
.btn-primary, .btn-cancel { padding: 8px 22px; border-radius: 6px; border: 0; color: white; cursor: pointer; }
.btn-primary { background: #1e88e5; }
.btn-cancel { background: rgba(255, 255, 255, 0.12); }
</style>
