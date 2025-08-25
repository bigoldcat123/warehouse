<template>
  <el-dialog v-model="prop.dialogVisible" title="入库" :before-close="handleClose" width="500" header-class="p-5"
    body-class="p-5">

    <el-form ref="ruleFormRef" style="max-width: 600px" :model="ruleForm" status-icon :rules="rules" label-width="auto"
      class="demo-ruleForm">

      <el-form-item label="仓库" prop="houseID">
        <!-- <el-input v-model="ruleForm.houseID" autocomplete="off" /> -->
        <el-select @change="warehouseChange" v-model="p" placeholder="Select" style="width: 240px">
          <el-option v-for="item in warehouekv" :key="item.key" :label="item.value" :value="item.key" />
        </el-select>
      </el-form-item>
      <el-form-item label="仓房" prop="houseID">
        <!-- <el-input v-model="ruleForm.houseID" autocomplete="off" /> -->
        <el-select v-model="ruleForm.houseID" placeholder="Select" style="width: 240px">
          <el-option v-for="item in kv" :key="item.key" :label="item.value" :value="item.key" />
        </el-select>
      </el-form-item>
      <el-form-item label="管理人员" prop="stockman">
        <!-- <el-input v-model="ruleForm.houseID" autocomplete="off" /> -->
        <el-select v-model="ruleForm.stockman" placeholder="Select" style="width: 240px">
          <el-option v-for="item in userKv" :key="item.key" :label="item.value" :value="item.key" />
        </el-select>
      </el-form-item>
      <el-form-item label="品种" prop="breed">
        <el-input v-model="ruleForm.breed" autocomplete="off" />
      </el-form-item>
      <el-form-item label="入库时间" prop="entryTime">
        <el-date-picker v-model="ruleForm.entryTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss"
          placeholder="选择入库时间" />
      </el-form-item>
      <el-form-item label="GrainWater" prop="water">
        <el-input-number v-model="ruleForm.water" :precision="2" :step="0.1" :max="99999" />
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
import { reactive, ref } from 'vue'
import house from '@/api/house';
import warehouse from '@/api/warehouse';
import entry, { type type_Entry } from '@/api/entry';
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


</script>
<style scoped></style>