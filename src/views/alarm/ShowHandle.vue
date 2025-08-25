<template>
    <el-dialog v-model="v" title="处理" :before-close="handleClose" width="500" header-class="p-5" body-class="p-5">

        <el-form ref="ruleFormRef" style="max-width: 600px" :model="ruleForm" status-icon label-width="auto"
            class="demo-ruleForm">
            <el-form-item label="审批" prop="handle">
                <el-input :autosize="{ minRows: 4, maxRows: 100 }" disabled v-model="ruleForm.handle"
                    autocomplete="off" />
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
    handle:string
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
    return callback()
}
const v = ref(false)
onUpdated(() => {
    v.value = prop.dialogVisible
    ruleForm.handle = prop.handle
})
const ruleForm = reactive({
    handle: '',
})




</script>
<style scoped></style>