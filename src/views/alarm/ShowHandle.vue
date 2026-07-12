<template>
    <el-dialog
        v-model="v"
        :before-close="handleClose"
        width="520"
        :show-close="false"
        class="dark-dialog"
        :modal-class="'dark-dialog__overlay'"
    >
        <!-- 自定义标题 -->
        <template #header>
            <div class="dialog-header">
                <svg class="w-6 h-6 text-[#64b5f6] flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
                <span class="text-white text-lg font-semibold">审核详情</span>
                <button class="close-btn" @click="$emit('close')" aria-label="关闭">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                    </svg>
                </button>
            </div>
        </template>

        <div class="p-6">
            <el-form ref="ruleFormRef" style="max-width: 600px" :model="ruleForm" label-width="70px"
                class="dark-form">
                <el-form-item label="审批" prop="handle">
                    <el-input :autosize="{ minRows: 4, maxRows: 100 }" disabled v-model="ruleForm.handle"
                        autocomplete="off" class="dark-textarea" />
                </el-form-item>
                <el-form-item>
                    <button type="button" class="btn-cancel" @click="$emit('close')">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                        </svg>
                        关闭
                    </button>
                </el-form-item>
            </el-form>
        </div>
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
<style scoped>
/* 对话框头部 */
.dialog-header {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 16px 20px;
    background: linear-gradient(135deg, #2968a8 0%, #1a3a5c 100%);
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}
.close-btn {
    margin-left: auto;
    background: transparent;
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 4px;
    color: #b0d0f0;
    padding: 4px;
    cursor: pointer;
    transition: all 0.2s ease;
    display: flex;
    align-items: center;
    justify-content: center;
}
.close-btn:hover {
    background: rgba(255, 255, 255, 0.1);
    color: white;
    border-color: rgba(255, 255, 255, 0.4);
}

/* 按钮 */
.btn-cancel {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 20px;
    background: rgba(255, 255, 255, 0.08);
    color: #b0d0f0;
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 6px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s ease;
}
.btn-cancel:hover {
    background: rgba(255, 255, 255, 0.15);
    color: white;
    border-color: rgba(255, 255, 255, 0.4);
}
</style>
<style>
/* 深色对话框 */
.dark-dialog.el-dialog {
    background: #1a3a5c !important;
    border-radius: 10px !important;
    border: 1px solid rgba(255, 255, 255, 0.1);
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.6) !important;
    overflow: hidden;
}
.dark-dialog .el-dialog__body {
    background: #1a3a5c !important;
    padding: 0 !important;
    color: white;
}
.dark-dialog .el-dialog__header {
    display: none;
}

/* 表单 */
.dark-form .el-form-item__label {
    color: #b0d0f0 !important;
    font-weight: 500;
}

/* 文本域 */
.dark-textarea .el-textarea__inner {
    background: rgba(0, 0, 0, 0.3) !important;
    border: 1px solid rgba(255, 255, 255, 0.1) !important;
    box-shadow: none !important;
    border-radius: 6px !important;
    color: rgba(255, 255, 255, 0.7) !important;
    resize: none;
    cursor: not-allowed;
}

/* 遮罩 */
.el-overlay {
    background: rgba(0, 0, 0, 0.7) !important;
    backdrop-filter: blur(4px);
}
</style>
