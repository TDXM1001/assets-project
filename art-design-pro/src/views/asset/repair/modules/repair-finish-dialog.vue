<template>
  <ElDialog
    v-model="visible"
    title="完成维修"
    width="720px"
    destroy-on-close
    append-to-body
    @closed="handleClosed"
  >
    <ElAlert
      class="mb-4"
      type="info"
      :closable="false"
      show-icon
      title="维修完成后，需要明确资产去向、维修费用和停用时长，这些信息会直接影响资产状态联动和维修流水。"
    />
    <ElAlert
      v-if="isSuggestDisposal"
      class="mb-4"
      type="warning"
      :closable="false"
      show-icon
      title="当前选择了“建议报废”"
      description="提交后会引导进入 DISPOSAL 草稿创建。请在处理说明里写清楚建议报废的原因、现状和后续处理建议。"
    />

    <ElForm ref="formRef" :model="formData" :rules="formRules" label-width="110px">
      <ElRow :gutter="16">
        <ElCol :span="12">
          <ElFormItem label="完成结果" prop="resultType">
            <ElSelect v-model="formData.resultType" class="w-full" placeholder="请选择维修结果">
              <ElOption
                v-for="item in resultTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="完成时间" prop="finishTime">
            <ElDatePicker
              v-model="formData.finishTime"
              class="w-full"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="请选择完成时间"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="维修费用" prop="repairCost">
            <ElInputNumber
              v-model="formData.repairCost"
              class="w-full"
              controls-position="right"
              :min="0"
              :precision="2"
              :step="100"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="停用时长(小时)" prop="downtimeHours">
            <ElInputNumber
              v-model="formData.downtimeHours"
              class="w-full"
              controls-position="right"
              :min="0"
              :precision="1"
              :step="1"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="外部供应商" prop="vendorName">
            <ElInput v-model="formData.vendorName" maxlength="100" placeholder="请输入供应商名称" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="是否返修" prop="reworkFlag">
            <ElSwitch v-model="reworkSwitch" inline-prompt active-text="是" inactive-text="否" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="24">
          <ElFormItem label="处理说明" prop="remark">
            <ElInput
              v-model="formData.remark"
              type="textarea"
              :rows="4"
              maxlength="500"
              show-word-limit
              :placeholder="
                isSuggestDisposal
                  ? '请补充建议报废原因、资产现状、已做处理和后续建议'
                  : '请输入维修处理说明，例如更换部件、测试结果和后续建议'
              "
            />
          </ElFormItem>
        </ElCol>
      </ElRow>
    </ElForm>

    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="visible = false">取消</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">确认完成</ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { computed, reactive, ref, watch } from 'vue'
  import type { FormRules } from 'element-plus'

  const resultTypeOptions = [
    { label: '恢复在用', value: 'RESUME_USE' },
    { label: '转闲置', value: 'TO_IDLE' },
    { label: '建议报废', value: 'SUGGEST_DISPOSAL' }
  ]

  const props = defineProps<{
    modelValue: boolean
    repairData?: any
  }>()

  const emit = defineEmits<{
    (e: 'update:modelValue', value: boolean): void
    (e: 'confirm', payload: any): void
  }>()

  const visible = ref(false)
  const submitLoading = ref(false)
  const formRef = ref()

  const createInitialFormData = () => ({
    resultType: 'RESUME_USE',
    finishTime: '',
    repairCost: undefined as number | undefined,
    downtimeHours: undefined as number | undefined,
    vendorName: '',
    reworkFlag: '0',
    remark: ''
  })

  const formData = reactive(createInitialFormData())
  const isSuggestDisposal = computed(() => formData.resultType === 'SUGGEST_DISPOSAL')

  const reworkSwitch = computed({
    get: () => formData.reworkFlag === '1',
    set: (value: boolean) => {
      formData.reworkFlag = value ? '1' : '0'
    }
  })

  const formRules: FormRules = {
    resultType: [{ required: true, message: '请选择维修结果', trigger: 'change' }],
    finishTime: [{ required: true, message: '请选择完成时间', trigger: 'change' }],
    remark: [
      {
        validator: (_rule, value, callback) => {
          if (isSuggestDisposal.value && !String(value || '').trim()) {
            callback(new Error('建议报废时请填写处理说明，明确报废原因和现状'))
            return
          }
          callback()
        },
        trigger: 'blur'
      }
    ]
  }

  watch(
    () => props.modelValue,
    (value) => {
      visible.value = value
      if (value) {
        Object.assign(formData, createInitialFormData(), {
          resultType: props.repairData?.resultType || 'RESUME_USE',
          repairCost: props.repairData?.repairCost,
          downtimeHours: props.repairData?.downtimeHours,
          vendorName: props.repairData?.vendorName || '',
          reworkFlag: props.repairData?.reworkFlag || '0',
          remark: props.repairData?.remark || ''
        })
      }
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
        emit('confirm', { ...formData })
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
