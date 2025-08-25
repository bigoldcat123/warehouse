<template>
    <el-dialog v-model="v" title="处理" :before-close="handleClose" width="500" header-class="p-5" body-class="p-5">

        <el-form ref="ruleFormRef" style="max-width: 600px" :model="ruleForm" status-icon :rules="rules"
            label-width="auto" class="demo-ruleForm">
            <el-form-item label="审批" prop="handle">
                <el-input type="textarea" :autosize="{ minRows: 4, maxRows: 10 }" v-model="ruleForm.handle" />
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
import alarm from '@/api/alarm';
const prop = defineProps<{
    dialogVisible: boolean,
    id: number | undefined
}>()
const emit = defineEmits<{
    (event: 'close'): void,
    (event: 'refresh'): void
}>()
const kv = ref<any[]>([])

function submit() {
    emit('close')
}
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
const v = ref(false)
onUpdated(() => {
    v.value = prop.dialogVisible
    ruleForm.id = prop.id as number
})
const ruleForm = reactive({
    handle: '',
    id:0
})

const rules = reactive<FormRules<typeof ruleForm>>({
    handle: [{ validator: validate, trigger: 'blur' }],
})


const submitForm = (formEl: FormInstance | undefined) => {
    if (!formEl) return
    formEl.validate((valid) => {
        if (valid) {
            alarm.handle(ruleForm.id,ruleForm.handle).then(res => {
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