<template>
  <el-dialog v-model="visiable" title="更新入库" :before-close="handleClose" width="500">

    <el-form ref="ruleFormRef" style="max-width: 600px" :model="ruleForm" status-icon :rules="rules" label-width="auto"
      class="demo-ruleForm">
      <el-form-item label="id">
        <el-input disabled v-model="ruleForm.id" />
      </el-form-item>
      <el-form-item label="仓库">
        <!-- <el-input v-model="ruleForm.houseID" autocomplete="off" /> -->
        <el-select @change="warehouseChange" v-model="p" placeholder="Select" style="width: 240px">
          <el-option v-for="item in warehousekv" :key="item.key" :label="item.value" :value="item.key" />
        </el-select>
      </el-form-item>
      <el-form-item label="仓房">
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
      <el-form-item label="入库人" prop="stockman">
        <!-- <el-input v-model="ruleForm.houseID" autocomplete="off" /> -->
        <el-select disabled v-model="ruleForm.entryUserId" placeholder="Select" style="width: 240px">
          <el-option v-for="item in AlluserKv" :key="item.key" :label="item.value" :value="item.key" />
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
import { onUpdated, reactive, ref } from 'vue'
import house from '@/api/house';
import entry, { type type_Entry } from '@/api/entry';
import user from '@/api/user';
import warehouse from '@/api/warehouse';
const prop = defineProps<{
  dialogVisible: boolean,
  entry: type_Entry | undefined
}>()
const emit = defineEmits<{
  (event: 'close'): void,
  (event: 'refresh'): void
}>()
const visiable = ref(false)
const kv = ref<any[]>([])
const AlluserKv = ref<any[]>([])
const p = ref()
user.kv().then(res => {
  AlluserKv.value = res.data.value
})
const userKv = ref<{ key: string, value: string }[]>([])

const handleClose = (done: () => void) => {
  emit('close')
}
const ruleFormRef = ref<FormInstance>()
const warehousekv = ref<any[]>([])
warehouse.belongKv().then(res => {
  warehousekv.value = res.data.value
})

function warehouseChange(value: any) {
  console.log(value);
  house.findByWarehouseId(value).then(res => {
    kv.value = res.data.value
  })
  ruleForm.Houseid = undefined
}

const validate = (rule: any, value: any, callback: any) => {
  if (!value) {
    return callback(new Error('不能为空'))
  }
  return callback()
}

const ruleForm = reactive<any>({
  id: 0,
  houseID: 0,
  breed: '',
  entryTime: '',
  water: 0,
  stockman: 0,
  entryUserId: 0
})
onUpdated(() => {
  visiable.value = prop.dialogVisible
  ruleForm.id = prop.entry?.id as number
  ruleForm.houseID = prop.entry?.houseID
  ruleForm.breed = prop.entry?.breed
  ruleForm.entryTime = prop.entry?.entryTime
  ruleForm.water = prop.entry?.water
  ruleForm.stockman = prop.entry?.stockman
  ruleForm.entryUserId = prop.entry?.entryUserId
  warehouse.getWarehouseIdBy(ruleForm.houseID).then(res => {
    p.value = res.data.value
    house.findByWarehouseId(p.value).then(res => {
      kv.value = res.data.value
    })
    user.getUsersByWarehouseId(p.value).then(res => {
      userKv.value = res.data.value
    })
  })

})
const rules = reactive<FormRules<typeof ruleForm>>({

  id: [{ validator: validate, trigger: 'blur' }],
  houseid: [{ validator: validate, trigger: 'blur' }],
  breed: [{ validator: validate, trigger: 'blur' }],
  entryTime: [{ validator: validate, trigger: 'blur' }],
  water: [{ validator: validate, trigger: 'blur' }],
})


const submitForm = (formEl: FormInstance | undefined) => {
  if (!formEl) return
  formEl.validate((valid) => {
    if (valid) {
      console.log('submit!')
      entry.update(ruleForm).then(res => {
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