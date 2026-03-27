<template>
  <ElDialog
    v-model="visible"
    title="Complete Repair"
    width="960px"
    destroy-on-close
    append-to-body
    @closed="handleClosed"
  >
    <ElAlert
      class="mb-4"
      type="info"
      :closable="false"
      show-icon
      title="Completing the repair will sync asset status and repair events."
      description="If this repair order contains multiple assets, confirm each item below. Header fields act as defaults for unchanged rows."
    />

    <ElAlert
      v-if="repairItems.length > 1"
      class="mb-4"
      type="warning"
      :closable="false"
      show-icon
      :title="`This repair order contains ${repairItems.length} assets.`"
      description="The dialog will submit itemList for per-asset completion. Back-end can still fall back to whole-order completion if needed."
    />

    <ElAlert
      v-if="isSuggestDisposal"
      class="mb-4"
      type="warning"
      :closable="false"
      show-icon
      title="Current result suggests disposal"
      description="Please include the disposal reason and current condition in the remark."
    />

    <ElForm ref="formRef" :model="formData" :rules="formRules" label-width="120px">
      <ElRow :gutter="16">
        <ElCol :span="12">
          <ElFormItem label="Default Result" prop="resultType">
            <ElSelect v-model="formData.resultType" class="w-full" placeholder="Select result">
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
          <ElFormItem label="Finish Time" prop="finishTime">
            <ElDatePicker
              v-model="formData.finishTime"
              class="w-full"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="Select finish time"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="Repair Cost" prop="repairCost">
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
          <ElFormItem label="Downtime (h)" prop="downtimeHours">
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
          <ElFormItem label="Vendor" prop="vendorName">
            <ElInput
              v-model="formData.vendorName"
              maxlength="100"
              placeholder="Enter vendor name"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="Rework" prop="reworkFlag">
            <ElSwitch v-model="reworkSwitch" inline-prompt active-text="Yes" inactive-text="No" />
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElFormItem label="Remark" prop="remark">
        <ElInput
          v-model="formData.remark"
          type="textarea"
          :rows="3"
          maxlength="500"
          show-word-limit
          :placeholder="
            isSuggestDisposal
              ? 'Enter disposal reason and current condition'
              : 'Enter repair summary and follow-up suggestion'
          "
        />
      </ElFormItem>

      <ElDivider content-position="left">Asset Items</ElDivider>
      <ElTable
        :data="repairItems"
        border
        stripe
        row-key="rowKey"
        max-height="420"
        empty-text="No repair items"
      >
        <ElTableColumn type="index" width="56" label="#" />
        <ElTableColumn prop="assetCode" label="Asset Code" min-width="140" />
        <ElTableColumn prop="assetName" label="Asset Name" min-width="180" />
        <ElTableColumn label="Before Status" width="120" align="center">
          <template #default="{ row }">
            <DictTag :options="asset_status" :value="row.beforeStatus" />
          </template>
        </ElTableColumn>
        <ElTableColumn label="Result" width="150">
          <template #default="{ row }">
            <ElSelect v-model="row.resultType" placeholder="Select" style="width: 100%">
              <ElOption
                v-for="item in resultTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </ElSelect>
          </template>
        </ElTableColumn>
        <ElTableColumn label="After Status" width="150">
          <template #default="{ row }">
            <ElSelect v-model="row.afterStatus" placeholder="Select" style="width: 100%">
              <ElOption
                v-for="item in asset_status"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </ElSelect>
          </template>
        </ElTableColumn>
        <ElTableColumn label="Fault Description" min-width="220">
          <template #default="{ row }">
            <ElInput
              v-model="row.faultDesc"
              type="textarea"
              :rows="2"
              maxlength="300"
              show-word-limit
              placeholder="Enter item fault description"
            />
          </template>
        </ElTableColumn>
        <ElTableColumn label="Item Remark" min-width="180">
          <template #default="{ row }">
            <ElInput v-model="row.remark" maxlength="200" placeholder="Enter item remark" />
          </template>
        </ElTableColumn>
      </ElTable>
    </ElForm>

    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="visible = false">Cancel</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit"> Confirm </ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { computed, reactive, ref, watch } from 'vue'
  import type { FormRules } from 'element-plus'
  import DictTag from '@/components/DictTag/index.vue'
  import { useDict } from '@/utils/dict'

  const { asset_status } = useDict('asset_status')

  const resultTypeOptions = [
    { label: 'Resume Use', value: 'RESUME_USE' },
    { label: 'To Idle', value: 'TO_IDLE' },
    { label: 'Suggest Disposal', value: 'SUGGEST_DISPOSAL' }
  ]

  const resultToStatusMap: Record<string, string> = {
    RESUME_USE: 'IN_USE',
    TO_IDLE: 'IDLE',
    SUGGEST_DISPOSAL: 'PENDING_DISPOSAL'
  }

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

  const createFallbackItem = (repair?: any) => {
    if (!repair?.assetId) return undefined
    return {
      rowKey: `asset-${repair.assetId}`,
      repairItemId: undefined,
      assetId: repair.assetId,
      assetCode: repair.assetCode || '',
      assetName: repair.assetName || '',
      beforeStatus: repair.beforeStatus || '',
      afterStatus: repair.afterStatus || repair.beforeStatus || 'IN_USE',
      resultType: repair.resultType || 'RESUME_USE',
      faultDesc: repair.faultDesc || '',
      remark: repair.remark || ''
    }
  }

  const formData = reactive(createInitialFormData())
  const repairItems = ref<any[]>([])
  const isSuggestDisposal = computed(() => formData.resultType === 'SUGGEST_DISPOSAL')

  const reworkSwitch = computed({
    get: () => formData.reworkFlag === '1',
    set: (value: boolean) => {
      formData.reworkFlag = value ? '1' : '0'
    }
  })

  const formRules: FormRules = {
    resultType: [{ required: true, message: 'Select result', trigger: 'change' }],
    finishTime: [{ required: true, message: 'Select finish time', trigger: 'change' }],
    remark: [
      {
        validator: (_rule, value, callback) => {
          if (isSuggestDisposal.value && !String(value || '').trim()) {
            callback(new Error('Remark is required for disposal suggestion'))
            return
          }
          callback()
        },
        trigger: 'blur'
      }
    ]
  }

  const normalizeRepairItems = (repair?: any) => {
    const sourceItems = Array.isArray(repair?.itemList)
      ? repair.itemList
      : Array.isArray(repair?.repairItems)
        ? repair.repairItems
        : []
    if (sourceItems.length > 0) {
      return sourceItems.map((item: any, index: number) => ({
        ...item,
        rowKey: item.repairItemId || item.assetId || `${index}`,
        resultType: item.resultType || repair?.resultType || 'RESUME_USE',
        afterStatus:
          item.afterStatus ||
          resultToStatusMap[item.resultType || repair?.resultType || 'RESUME_USE'] ||
          item.beforeStatus ||
          'IN_USE',
        faultDesc: item.faultDesc || repair?.faultDesc || '',
        remark: item.remark || ''
      }))
    }
    const fallbackItem = createFallbackItem(repair)
    return fallbackItem ? [fallbackItem] : []
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
        repairItems.value = normalizeRepairItems(props.repairData)
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

  watch(
    () => formData.resultType,
    (value) => {
      // 这里保留整单默认值，避免多资产场景下遗漏明细结果。
      repairItems.value = repairItems.value.map((item) => {
        const nextResultType = item.resultType || value
        return {
          ...item,
          resultType: nextResultType,
          afterStatus:
            item.afterStatus || resultToStatusMap[nextResultType] || item.beforeStatus || 'IN_USE'
        }
      })
    }
  )

  const handleSubmit = async () => {
    if (!formRef.value) return
    await formRef.value.validate((valid: boolean) => {
      if (!valid) return
      submitLoading.value = true
      try {
        const itemList = repairItems.value.map((item) => {
          const resultType = item.resultType || formData.resultType
          return {
            repairItemId: item.repairItemId,
            assetId: item.assetId,
            assetCode: item.assetCode,
            assetName: item.assetName,
            beforeStatus: item.beforeStatus,
            afterStatus:
              item.afterStatus || resultToStatusMap[resultType] || item.beforeStatus || 'IN_USE',
            resultType,
            faultDesc: item.faultDesc || formData.remark || '',
            remark: item.remark || formData.remark || ''
          }
        })
        emit('confirm', { ...formData, itemList })
      } finally {
        submitLoading.value = false
      }
    })
  }

  const handleClosed = () => {
    Object.assign(formData, createInitialFormData())
    repairItems.value = []
    submitLoading.value = false
    formRef.value?.clearValidate?.()
  }
</script>
