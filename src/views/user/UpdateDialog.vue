<template>
  <el-dialog v-model="visiable" title="更改用户" :before-close="handleClose" width="500">

    <el-form ref="ruleFormRef" style="max-width: 600px" :model="ruleForm" status-icon :rules="rules" label-width="auto"
      class="demo-ruleForm">
      <el-form-item label="id" >
        <el-input disabled v-model="ruleForm.id" />
      </el-form-item>
      <el-form-item label="用户名" prop="username">
        <el-input :disabled="prop.current?.username == 'admin'" v-model="ruleForm.username" />
      </el-form-item>
      <el-form-item label="密码">
        <el-input :disabled="prop.current?.username == 'admin'" type="password" v-model="ruleForm.password" />
      </el-form-item>
      <el-form-item label="姓名" prop="name">
        <el-input v-model="ruleForm.name" />
      </el-form-item>
      <el-form-item label="性别" prop="sex">
        <el-select v-model="ruleForm.sex" placeholder="选择性别" style="width: 240px">
          <el-option key="男" label="男" value="男" />
          <el-option key="女" label="女" value="女" />
        </el-select>
      </el-form-item>
      <el-form-item label="公司" prop="companyID">
        <el-select v-model="ruleForm.companyID" placeholder="选择公司" style="width: 240px">
          <el-option v-for="item in kv" :key="item.key" :label="item.value" :value="item.key" />
        </el-select>
      </el-form-item>
      <el-form-item label="职位" prop="position">
        <el-input v-model="ruleForm.position" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="ruleForm.phone" />
      </el-form-item>
      <el-form-item label="权利">
        <!-- <el-input v-model="ruleForm.priv" /> -->
         <div @click="add_del_Priv(key)" class=" hover:bg-green-300 m-2 px-2 cursor-pointer outline outline-1 rounded-md" :class="privLChecked.includes(key)? 'bg-green-300' :''" v-for="item,key in privList">{{ item + '-' + key }}</div>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="submitForm(ruleFormRef)">提交</el-button>
        <el-button @click="$emit('close')">取消</el-button>
      </el-form-item>
    </el-form>
  </el-dialog>
</template>
<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus';
import { onUpdated, reactive, ref } from 'vue'
import user, { type type_User } from '@/api/user';
const prop = defineProps<{
  dialogVisible: boolean,
  kv: any[],
  privList: string[],
  current: type_User | undefined
}>()
const emit = defineEmits<{
  (event: 'close'): void,
  (event: 'refresh'): void
}>()
const visiable = ref(false)
onUpdated(() => {
  visiable.value = prop.dialogVisible
  ruleForm.id = prop.current?.id
  ruleForm.username = prop.current?.username
  ruleForm.name = prop.current?.name
  ruleForm.sex = prop.current?.sex
  ruleForm.companyID = prop.current?.companyID
  ruleForm.position = prop.current?.position
  ruleForm.password=''
  ruleForm.phone = prop.current?.phone
  if(prop.current!.priv.length != 0)
  privLChecked.value = prop.current!.priv.split(',').map((item:string) => Number(item))
})
const privLChecked = ref<number[]>([])
const handleClose = (done: () => void) => {
  emit('close')
}
const add_del_Priv = (key:number) => {
  // if(key == 0 && !privLChecked.value.includes(key)){ 
  //   if(privLChecked.value.includes(1))
  //   privLChecked.value.splice(privLChecked.value.indexOf(1),1)
  //   if(privLChecked.value.includes(2))
  //   privLChecked.value.splice(privLChecked.value.indexOf(2),1)
  // }
  // if((key == 1 || key == 2) && privLChecked.value.includes(0)){
  //  return 
  // }
  privLChecked.value.includes(key)? privLChecked.value.splice(privLChecked.value.indexOf(key),1):privLChecked.value.push(key)
  // if(privLChecked.value.includes(1) && privLChecked.value.includes(2)){
  //   privLChecked.value.splice(privLChecked.value.indexOf(2),1)
  //   privLChecked.value.splice(privLChecked.value.indexOf(1),1)
  //   privLChecked.value.push(0)
  // }
}
const ruleFormRef = ref<FormInstance>()

const validate = (rule: any, value: any, callback: any) => {
  if(value == 0){
    return callback()
  }
  if (!value) {
    return callback(new Error('不能为空'))
  }
  return callback()
}
const validatePhoneNumber = (rule: any, value: any, callback: any) => {
  if (!value) {
    return callback(new Error('不能为空'))
  }
  if(!/[1234567890]{11}$/.test(value)){
    return callback(new Error('请输入正确的手机号码'))
  }
  return callback()
}

const ruleForm = reactive<any>({
  id:0,
  username: '',
  password: '',
  name: '',
  sex: '',
  companyID: undefined,
  position: '',
  phone: '',
  priv: null,
})

const rules = reactive<FormRules<typeof ruleForm>>({
  username: [{ validator: validate, trigger: 'blur' }],
  name: [{ validator: validate, trigger: 'blur' }],
  sex: [{ validator: validate, trigger: 'blur' }],
  companyID: [{ validator: validate, trigger: 'blur' }],
  position: [{ validator: validate, trigger: 'blur' }],
  phone: [{ validator: validatePhoneNumber, trigger: 'blur' }],
})


const submitForm = (formEl: FormInstance | undefined) => {
  if (!formEl) return
  formEl.validate((valid) => {
    if (valid) {
      ruleForm.priv = privLChecked.value.join(',')
      user.update(ruleForm).then(res => {
        emit('refresh')
        emit('close')
      })
    } else {
      console.log('error submit!')
    }
  })
}


</script>
<style scoped></style>