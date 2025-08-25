<template>
  <el-dialog v-model="visible" title="修改仓房" header-class="p-5" body-class="p-5" :before-close="handleClose" width="500">

    <el-form ref="ruleFormRef" style="max-width: 600px" :model="ruleForm" status-icon :rules="rules" label-width="auto"
      class="demo-ruleForm">

      <el-form-item label="仓房ID">
        <el-input disabled v-model="ruleForm.id" />
      </el-form-item>
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
      <el-form-item label="行数(X)" prop="x">
        <el-input v-model.number="ruleForm.x" />
      </el-form-item>
      <el-form-item label="列数(Y)" prop="y">
        <el-input v-model.number="ruleForm.y" />
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
  </el-dialog>
</template>
<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus';
import { onUpdated, reactive, ref } from 'vue'
import house, { type type_House } from '@/api/house';
const prop = defineProps<{
  dialogVisible: boolean,
  kv:any[],
  house:type_House | undefined
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
  ruleForm.id = prop.house?.id
  ruleForm.houseNo = prop.house?.houseNo
  ruleForm.houseName = prop.house?.houseName
  ruleForm.houseAddr = prop.house?.houseAddr
  ruleForm.houseType = prop.house?.houseType
  ruleForm.warehouseID = prop.house?.warehouseID
  ruleForm.x = prop.house?.x
  ruleForm.y = prop.house?.y
  ruleForm.z = prop.house?.z
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
  z: 0,
  x: 0,
  y: 0,
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
      house.update(ruleForm).then(res => {
        emit('refresh')
      })
      emit('close')
    } else {
      console.log('error submit!')
    }
  })
}


</script>
<style scoped></style>