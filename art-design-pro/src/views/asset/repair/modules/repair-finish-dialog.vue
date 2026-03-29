<template>
  <ElDialog
    v-model="visible"
    title="维修完工"
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
      title="完成维修后会同步资产状态并写入维修事件。"
      description="如果当前维修单包含多项资产，请逐项确认明细。表头字段会作为未单独修改明细的默认值。"
    />

    <ElAlert
      v-if="repairItems.length > 1"
      class="mb-4"
      type="warning"
      :closable="false"
      show-icon
      :title="`当前维修单包含 ${repairItems.length} 项资产。`"
      description="提交时会按资产明细上传 itemList；如果后端仍按整单处理，也会回退到整单完工逻辑。"
    />

    <ElAlert
      v-if="isSuggestDisposal"
      class="mb-4"
      type="warning"
      :closable="false"
      show-icon
      title="当前结果为建议处置"
      description="请在备注中补充处置原因和资产当前情况。"
    />

    <ElForm ref="formRef" :model="formData" :rules="formRules" label-width="120px">
      <ElRow :gutter="16">
        <ElCol :span="12">
          <ElFormItem label="默认结果" prop="resultType">
            <ElSelect v-model="formData.resultType" class="w-full" placeholder="请选择结果">
              <ElOption
                v-for="item in asset_repair_result_type"
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
          <ElFormItem label="停机时长(小时)" prop="downtimeHours">
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
          <ElFormItem label="维修厂商" prop="vendorName">
            <ElInput v-model="formData.vendorName" maxlength="100" placeholder="请输入维修厂商" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="是否返修" prop="reworkFlag">
            <ElSwitch v-model="reworkSwitch" inline-prompt active-text="是" inactive-text="否" />
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElFormItem label="备注" prop="remark">
        <ElInput
          v-model="formData.remark"
          type="textarea"
          :rows="3"
          maxlength="500"
          show-word-limit
          :placeholder="
            isSuggestDisposal ? '请输入处置原因和资产当前情况' : '请输入维修总结和后续建议'
          "
        />
      </ElFormItem>

      <ElDivider content-position="left">维修资产明细</ElDivider>
      <ElTable
        :data="repairItems"
        border
        stripe
        row-key="rowKey"
        max-height="420"
        empty-text="暂无维修资产"
      >
        <ElTableColumn type="index" width="56" label="#" />
        <ElTableColumn prop="assetCode" label="资产编码" min-width="140" />
        <ElTableColumn prop="assetName" label="资产名称" min-width="180" />
        <ElTableColumn label="维修前状态" width="120" align="center">
          <template #default="{ row }">
            <DictTag :options="asset_status" :value="row.beforeStatus" />
          </template>
        </ElTableColumn>
        <ElTableColumn label="处理结果" width="150">
          <template #default="{ row }">
            <ElSelect v-model="row.resultType" placeholder="请选择" style="width: 100%">
              <ElOption
                v-for="item in asset_repair_result_type"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </ElSelect>
          </template>
        </ElTableColumn>
        <ElTableColumn label="维修后状态" width="150">
          <template #default="{ row }">
            <ElSelect v-model="row.afterStatus" placeholder="请选择" style="width: 100%">
              <ElOption
                v-for="item in asset_status"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </ElSelect>
          </template>
        </ElTableColumn>
        <ElTableColumn label="故障描述" min-width="220">
          <template #default="{ row }">
            <ElInput
              v-model="row.faultDesc"
              type="textarea"
              :rows="2"
              maxlength="300"
              show-word-limit
              placeholder="请输入该资产的故障描述"
            />
          </template>
        </ElTableColumn>
        <ElTableColumn label="明细备注" min-width="180">
          <template #default="{ row }">
            <ElInput v-model="row.remark" maxlength="200" placeholder="请输入明细备注" />
          </template>
        </ElTableColumn>
      </ElTable>
    </ElForm>

    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="visible = false">取消</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">确认</ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { computed, reactive, ref, watch } from 'vue'
  import type { FormRules } from 'element-plus'
  import DictTag from '@/components/DictTag/index.vue'
  import { useDict } from '@/utils/dict'
  import { resolveRepairItems } from './repair-item-normalize'

  defineOptions({ name: 'RepairFinishDialog' })

  const { asset_status, asset_repair_result_type } = useDict('asset_status', 'asset_repair_result_type')


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
    resultType: [{ required: true, message: '请选择处理结果', trigger: 'change' }],
    finishTime: [{ required: true, message: '请选择完成时间', trigger: 'change' }],
    remark: [
      {
        validator: (_rule, value, callback) => {
          if (isSuggestDisposal.value && !String(value || '').trim()) {
            callback(new Error('建议处置时必须填写备注'))
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
        repairItems.value = resolveRepairItems(props.repairData).map((item) => {
          const resultType = item.resultType || props.repairData?.resultType || 'RESUME_USE'
          return {
            ...item,
            resultType,
            afterStatus:
              item.afterStatus || resultToStatusMap[resultType] || item.beforeStatus || 'IN_USE'
          }
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

  // 完工弹窗只维护局部表单态，真正的业务提交流程由外层页面承接。
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
