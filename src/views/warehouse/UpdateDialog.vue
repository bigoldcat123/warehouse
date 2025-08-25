<template>
  <el-dialog v-model="visible" title="新增仓库" :before-close="handleClose" width="500" header-class="p-5" body-class="p-5">

    <el-form ref="ruleFormRef" style="max-width: 600px" :model="ruleForm" status-icon :rules="rules" label-width="auto"
      class="demo-ruleForm">

      <el-form-item label="仓库ID">
        <el-input disabled v-model="ruleForm.id" autocomplete="off" />
      </el-form-item>

      <el-form-item label="仓库编号" prop="warehouseNo">
        <el-input v-model="ruleForm.warehouseNo" autocomplete="off" />
      </el-form-item>
      <el-form-item label="仓库名" prop="warehouseName">
        <el-input v-model="ruleForm.warehouseName" autocomplete="off" />
      </el-form-item>
      <el-form-item label="仓库地址" prop="warehouseAddress">
        <el-input v-model="ruleForm.warehouseAddress" />
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
import { onMounted, onUpdated, reactive, ref } from 'vue'
import warehouse, { type type_WareHouse } from '@/api/warehouse';
const visible = ref(false)
const prop = defineProps<{
    dialogVisible: boolean,
    warehouse: type_WareHouse | undefined
}>()
const emit = defineEmits<{
    (event: 'close'): void,
    (event:'refresh'):void
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
<style scoped></style>