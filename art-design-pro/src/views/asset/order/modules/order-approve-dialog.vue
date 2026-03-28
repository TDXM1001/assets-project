<template>
  <ElDialog
    :title="dialogTitle"
    v-model="visible"
    width="640px"
    destroy-on-close
    append-to-body
    @closed="handleClosed"
  >
    <OrderWorkbenchShell
      :loading="submitLoading"
      eyebrow="审批流程"
      :title="shellTitle"
      :description="dialogTip"
    >
      <template #status>
        <ElSpace wrap>
          <ElTag :type="actionTagType" effect="light">{{ actionLabel }}</ElTag>
          <DictTag :options="asset_order_status" :value="orderData?.orderStatus" />
          <DictTag :options="asset_order_type" :value="orderData?.orderType" />
        </ElSpace>
      </template>

      <template #editor>
        <ElAlert
          class="mb-4"
          :type="actionType === 'reject' ? 'warning' : 'success'"
          show-icon
          :closable="false"
          :title="dialogTip"
        />

        <ElForm ref="formRef" :model="formData" :rules="formRules" label-width="100px">
          <ElFormItem :label="remarkLabel" prop="remark">
            <ElInput
              v-model="formData.remark"
              type="textarea"
              :rows="5"
              maxlength="500"
              show-word-limit
              :placeholder="remarkPlaceholder"
            />
          </ElFormItem>
        </ElForm>
      </template>

      <template #footer>
        <ElSpace wrap>
          <ElButton @click="visible = false">取消</ElButton>
          <ElButton :type="actionTagType" :loading="submitLoading" @click="handleSubmit">
            {{ actionType === 'reject' ? '驳回' : '通过' }}
          </ElButton>
        </ElSpace>
      </template>
    </OrderWorkbenchShell>
  </ElDialog>
</template>

<script setup lang="ts">
  import { computed, reactive, ref, watch } from 'vue'
  import type { FormRules } from 'element-plus'
  import { useDict } from '@/utils/dict'
  import DictTag from '@/components/DictTag/index.vue'
  import OrderWorkbenchShell from './order-workbench-shell.vue'

  const props = defineProps<{
    modelValue: boolean
    actionType: 'approve' | 'reject'
    orderData?: any
  }>()

  const emit = defineEmits<{
    (e: 'update:modelValue', value: boolean): void
    (e: 'confirm', payload: { remark: string; actionType: 'approve' | 'reject' }): void
  }>()

  const visible = ref(false)
  const submitLoading = ref(false)
  const formRef = ref()

  const { asset_order_status, asset_order_type } = useDict('asset_order_status', 'asset_order_type')

  const createInitialFormData = () => ({
    remark: ''
  })

  const formData = reactive(createInitialFormData())

  const actionLabel = computed(() => (props.actionType === 'reject' ? '驳回' : '审批'))
  const actionTagType = computed(() => (props.actionType === 'reject' ? 'danger' : 'success'))
  const shellTitle = computed(() => {
    if (props.orderData?.orderNo) {
      return `${actionLabel.value}单据 - ${props.orderData.orderNo}`
    }
    return `${actionLabel.value}单据`
  })
  const dialogTitle = computed(() => (props.actionType === 'reject' ? '驳回单据' : '审批单据'))
  const dialogTip = computed(() =>
    props.actionType === 'reject'
      ? '请填写驳回原因，方便发起人知道下一步怎么补充。'
      : '请填写审批意见，便于后续在详情抽屉里回溯审批过程。'
  )
  const remarkLabel = computed(() => (props.actionType === 'reject' ? '驳回原因' : '审批意见'))
  const remarkPlaceholder = computed(() =>
    props.actionType === 'reject' ? '请输入驳回原因' : '请输入审批意见'
  )

  const formRules = computed<FormRules>(() => ({
    remark: [
      {
        required: props.actionType === 'reject',
        message: remarkPlaceholder.value,
        trigger: 'blur'
      }
    ]
  }))

  watch(
    () => props.modelValue,
    (value) => {
      visible.value = value
      if (!value) {
        return
      }
      Object.assign(formData, createInitialFormData())
    },
    { immediate: true }
  )

  watch(
    () => visible.value,
    (value) => {
      emit('update:modelValue', value)
    }
  )

  const handleSubmit = async () => {
    if (!formRef.value) return

    await formRef.value.validate((valid: boolean) => {
      if (!valid) return

      submitLoading.value = true
      try {
        emit('confirm', {
          remark: formData.remark,
          actionType: props.actionType
        })
      } finally {
        submitLoading.value = false
      }
    })
  }

  const handleClosed = () => {
    Object.assign(formData, createInitialFormData())
    submitLoading.value = false
    formRef.value?.clearValidate?.()
  }
</script>
