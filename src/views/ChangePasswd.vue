<template>
  <el-dialog v-model="v" title="修改密码" :before-close="handleClose" width="500">

      <el-form ref="ruleFormRef" style="max-width: 600px" :model="ruleForm" status-icon :rules="rules"
          label-width="auto" class="demo-ruleForm">
          <el-form-item label="旧密码" prop="old">
              <el-input type="password"  v-model="ruleForm.old" />
          </el-form-item>
          <el-form-item label="新密码" prop="newP">
              <el-input type="password"  v-model="ruleForm.newP" />
          </el-form-item>
          <el-form-item label="确认密码" prop="rectify">
              <el-input type="password"  v-model="ruleForm.rectify" />
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
import user from '@/api/user';
const prop = defineProps<{
  dialogVisible: boolean
}>()
const emit = defineEmits<{
  (event: 'close'): void,
  (event: 'refresh'): void
}>()
const handleClose = (done: () => void) => {
  emit('close')
}
const ruleFormRef = ref<FormInstance>()

const validate = (rule: any, value: any, callback: any) => {
  if (!value) {
      return callback(new Error('不能为空'))
  }
  if(value.length >= 200){
      return callback(new Error("长度超出限制 -> " + value.length + "/200"))
  }
  return callback()
}

const validateR = (rule: any, value: any, callback: any) => {
  if (!value) {
      return callback(new Error('不能为空'))
  }
  if(value.length >= 200){
      return callback(new Error("长度超出限制 -> " + value.length + "/200"))
  }
  if(value != ruleForm.newP){
      return callback(new Error("两次密码不一致"))
  }
  return callback()
}

const v = ref(false)
onUpdated(() => {
  v.value = prop.dialogVisible
})
const ruleForm = reactive({
  old: '',
  newP:'',
  rectify:''
})

const rules = reactive<FormRules<typeof ruleForm>>({
  old: [{ validator: validate, trigger: 'blur' }],
  newP: [{ validator: validate, trigger: 'blur' }],
  rectify: [{ validator: validateR, trigger: 'blur' }],
})


const submitForm = (formEl: FormInstance | undefined) => {
  if (!formEl) return
  formEl.validate((valid) => {
      if (valid) {
          user.changePasswd(ruleForm.old,ruleForm.newP).then(res => {

          emit('close')
          })
      } else {
          console.log('error submit!')
      }
  })
}


</script>
<style scoped></style>