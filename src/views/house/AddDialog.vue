<template>
  <el-dialog v-model="visible" title="新增仓房" header-class="p-5" :before-close="handleClose" width="500">
    <div class="p-4">
      <el-form ref="ruleFormRef" style="max-width: 600px" :model="ruleForm" status-icon :rules="rules"
        label-width="auto" class="demo-ruleForm">

        <el-form-item label="仓房编号" prop="houseNo">
          <el-input v-model="ruleForm.houseNo" />
        </el-form-item>
        <el-form-item label="仓房名" prop="houseName">
          <el-input v-model="ruleForm.houseName" />
        </el-form-item>
        <el-form-item label="仓房地址" prop="houseAddr">
          <el-input v-model="ruleForm.houseAddr" />
        </el-form-item>
        <el-form-item label="仓房类型" prop="houseType">
          <el-select v-model="ruleForm.houseType" placeholder="Select" style="width: 240px">
            <el-option key="平房仓" label="平房仓" value="平房仓" />
            <el-option key="筒仓" label="筒仓" value="筒仓" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属仓库" prop="warehouseID">
          <!-- <el-input v-model="ruleForm.warehouseID" /> -->
          <el-select v-model="ruleForm.warehouseID" placeholder="Select" style="width: 240px">
            <el-option v-for="item in kv" :key="item.key" :label="item.value" :value="item.key" />
          </el-select>
        </el-form-item>
        <el-form-item label="列数(X)" prop="y">
          <el-input v-model.number="ruleForm.y" />
        </el-form-item>
        <el-form-item label="行数(Y)" prop="x">
          <el-input v-model.number="ruleForm.x" />
        </el-form-item>

        <el-form-item label="层数(Z)" prop="z">
          <el-input v-model.number="ruleForm.z" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitForm(ruleFormRef)">
            提交
          </el-button>
          <el-button @click="$emit('close')">取消</el-button>
        </el-form-item>
      </el-form>
    </div>

  </el-dialog>
</template>
<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus';
import { onUpdated, reactive, ref } from 'vue'
import house, { type type_House } from '@/api/house';
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
<style scoped></style>