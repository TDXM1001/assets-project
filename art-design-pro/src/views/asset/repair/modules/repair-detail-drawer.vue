<template>
  <ElDrawer
    v-model="visible"
    :title="drawerTitle"
    size="78%"
    append-to-body
    destroy-on-close
    @closed="handleClosed"
  >
    <div class="repair-detail-drawer">
      <div class="repair-detail-drawer__header">
        <div>
          <div class="repair-detail-drawer__title">{{ displayText(repairData?.repairNo) }}</div>
          <div class="repair-detail-drawer__subtitle">{{ assetSummaryText }}</div>
        </div>
        <div class="repair-detail-drawer__actions">
          <ElTag effect="light" type="info">Repair Status</ElTag>
          <ElTag :type="statusTagType">{{ currentStatusLabel }}</ElTag>
        </div>
      </div>

      <ElAlert
        v-if="showDisposalBridgeAlert"
        class="repair-detail-drawer__summary"
        type="warning"
        :closable="false"
        show-icon
        :title="bridgeAlertTitle"
        :description="bridgeAlertDescription"
      >
        <template #default>
          <div class="repair-detail-drawer__bridge-text">
            {{ bridgeAlertDescription }}
          </div>
          <ElButton
            v-if="canCreateDisposal"
            type="warning"
            size="small"
            class="repair-detail-drawer__bridge-btn"
            @click="emit('createDisposal')"
          >
            Create Disposal Order
          </ElButton>
        </template>
      </ElAlert>

      <ElCard v-if="relatedDisposalOrder" shadow="never" class="mb-4">
        <template #header>Linked Disposal Order</template>
        <ElDescriptions :column="2" border>
          <ElDescriptionsItem label="Order No">
            {{ displayText(relatedDisposalOrder.orderNo) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="Source Biz No">
            {{ displayText(relatedDisposalOrder.sourceBizNo) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="Order Status">
            <DictTag :options="asset_order_status" :value="relatedDisposalOrder.orderStatus" />
          </ElDescriptionsItem>
          <ElDescriptionsItem label="Amount">
            {{ displayAmount(relatedDisposalOrder.disposalAmount) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="Approval">
            {{
              displayApproval(
                relatedDisposalOrder.approveUserName,
                relatedDisposalOrder.approveTime
              )
            }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="Reason" :span="2">
            {{ displayText(relatedDisposalOrder.disposalReason) }}
          </ElDescriptionsItem>
        </ElDescriptions>
      </ElCard>

      <div
        v-if="relatedDisposalOrder && canViewRelatedDisposal"
        class="repair-detail-drawer__linked-actions"
      >
        <ElButton type="primary" @click="emit('viewDisposal', relatedDisposalOrder)">
          View Disposal Order
        </ElButton>
      </div>

      <ElCard shadow="never" class="mb-4">
        <template #header>Asset Items</template>
        <div class="repair-detail-drawer__asset-summary">
          <ElTag type="info" effect="plain">Items: {{ repairItems.length }}</ElTag>
        </div>
        <ElTable
          :data="repairItems"
          border
          stripe
          size="small"
          row-key="repairItemId"
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
          <ElTableColumn label="After Status" width="120" align="center">
            <template #default="{ row }">
              <DictTag :options="asset_status" :value="row.afterStatus" />
            </template>
          </ElTableColumn>
          <ElTableColumn label="Result" width="140" align="center">
            <template #default="{ row }">
              {{ displayText(resultTypeMap[row.resultType] || row.resultType) }}
            </template>
          </ElTableColumn>
          <ElTableColumn
            prop="faultDesc"
            label="Fault Description"
            min-width="220"
            show-overflow-tooltip
          />
          <ElTableColumn prop="remark" label="Item Remark" min-width="180" show-overflow-tooltip />
        </ElTable>
      </ElCard>

      <ElCard shadow="never" class="mb-4">
        <template #header>Repair Summary</template>
        <ElDescriptions :column="2" border>
          <ElDescriptionsItem label="Report Time">
            {{ displayText(repairData?.reportTime) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="Repair Mode">
            {{ displayText(repairModeLabel) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="Apply Dept">
            {{ displayText(repairData?.applyDeptName || repairData?.applyDeptId) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="Apply User">
            {{ displayText(repairData?.applyUserName || repairData?.applyUserId) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="Vendor">
            {{ displayText(repairData?.vendorName) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="Item Count">
            {{ repairItems.length || '-' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="Header Fault Desc" :span="2">
            {{ displayText(repairData?.faultDesc) }}
          </ElDescriptionsItem>
        </ElDescriptions>
      </ElCard>

      <ElCard shadow="never" class="mb-4">
        <template #header>Approval & Finish</template>
        <ElDescriptions :column="2" border>
          <ElDescriptionsItem label="Approve User">
            {{ displayText(repairData?.approveUserName || repairData?.approveUserId) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="Approve Time">
            {{ displayText(repairData?.approveTime) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="Approve Result">
            {{ displayText(approveResultLabel) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="Send Repair Time">
            {{ displayText(repairData?.sendRepairTime) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="Repair Cost">
            {{ displayAmount(repairData?.repairCost) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="Downtime">
            {{ displayDowntime(repairData?.downtimeHours) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="Finish Time">
            {{ displayText(repairData?.finishTime) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="Header Result">
            {{ displayText(resultTypeLabel) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="Rework">
            {{ repairData?.reworkFlag === '1' ? 'Yes' : 'No' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="Remark" :span="2">
            {{ displayText(repairData?.remark) }}
          </ElDescriptionsItem>
        </ElDescriptions>
      </ElCard>
    </div>

    <template #footer>
      <div class="repair-detail-drawer__footer">
        <ElSpace wrap>
          <ElButton
            v-if="hasPermission('asset:repair:query')"
            type="primary"
            plain
            @click="emit('attachments')"
          >
            Attachments
          </ElButton>
          <ElButton
            v-if="canEdit && hasPermission('asset:repair:edit')"
            type="primary"
            plain
            @click="emit('edit')"
          >
            Edit
          </ElButton>
          <ElButton
            v-if="canSubmit && hasPermission('asset:repair:submit')"
            type="primary"
            @click="emit('submit')"
          >
            Submit
          </ElButton>
          <ElButton
            v-if="canApprove && hasPermission('asset:repair:approve')"
            type="success"
            @click="emit('approve')"
          >
            Approve
          </ElButton>
          <ElButton
            v-if="canReject && hasPermission('asset:repair:reject')"
            type="danger"
            plain
            @click="emit('reject')"
          >
            Reject
          </ElButton>
          <ElButton
            v-if="canFinish && hasPermission('asset:repair:finish')"
            type="warning"
            @click="emit('finish')"
          >
            Finish
          </ElButton>
          <ElButton
            v-if="canCancel && hasPermission('asset:repair:cancel')"
            type="info"
            plain
            @click="emit('cancel')"
          >
            Cancel
          </ElButton>
          <ElButton @click="visible = false">Close</ElButton>
        </ElSpace>
      </div>
    </template>
  </ElDrawer>
</template>

<script setup lang="ts">
  import { computed, ref, watch } from 'vue'
  import DictTag from '@/components/DictTag/index.vue'
  import { useDict } from '@/utils/dict'
  import { useUserStore } from '@/store/modules/user'

  const { asset_status, asset_order_status } = useDict('asset_status', 'asset_order_status')
  const userStore = useUserStore()

  const props = defineProps<{
    modelValue: boolean
    repairData?: any
  }>()

  const emit = defineEmits<{
    (e: 'update:modelValue', value: boolean): void
    (e: 'edit'): void
    (e: 'submit'): void
    (e: 'approve'): void
    (e: 'reject'): void
    (e: 'finish'): void
    (e: 'cancel'): void
    (e: 'attachments'): void
    (e: 'createDisposal'): void
    (e: 'viewDisposal', order?: any): void
  }>()

  const visible = ref(false)

  const statusMap: Record<
    string,
    { label: string; type: 'info' | 'warning' | 'success' | 'danger' }
  > = {
    DRAFT: { label: 'Draft', type: 'info' },
    SUBMITTED: { label: 'Submitted', type: 'warning' },
    APPROVED: { label: 'Repairing', type: 'warning' },
    REJECTED: { label: 'Rejected', type: 'danger' },
    FINISHED: { label: 'Finished', type: 'success' },
    CANCELED: { label: 'Canceled', type: 'info' }
  }

  const repairModeMap: Record<string, string> = {
    IN_HOUSE: 'In House',
    VENDOR: 'Vendor',
    ONSITE: 'On Site'
  }

  const resultTypeMap: Record<string, string> = {
    RESUME_USE: 'Resume Use',
    TO_IDLE: 'To Idle',
    SUGGEST_DISPOSAL: 'Suggest Disposal'
  }

  const approveResultMap: Record<string, string> = {
    APPROVED: 'Approved',
    REJECTED: 'Rejected'
  }

  // 兼容老数据：如果后端还未返回 itemList，则退化为单资产明细。
  const repairItems = computed(() => {
    if (Array.isArray(props.repairData?.itemList) && props.repairData.itemList.length > 0) {
      return props.repairData.itemList.filter(Boolean)
    }
    if (Array.isArray(props.repairData?.repairItems) && props.repairData.repairItems.length > 0) {
      return props.repairData.repairItems.filter(Boolean)
    }
    if (props.repairData?.assetId) {
      return [
        {
          assetId: props.repairData.assetId,
          assetCode: props.repairData.assetCode,
          assetName: props.repairData.assetName,
          beforeStatus: props.repairData.beforeStatus,
          afterStatus: props.repairData.afterStatus,
          resultType: props.repairData.resultType,
          faultDesc: props.repairData.faultDesc,
          remark: props.repairData.remark
        }
      ]
    }
    return []
  })

  const currentStatus = computed(() => props.repairData?.repairStatus || 'DRAFT')
  const currentStatusLabel = computed(
    () => statusMap[currentStatus.value]?.label || currentStatus.value || '-'
  )
  const statusTagType = computed(() => statusMap[currentStatus.value]?.type || 'info')
  const repairModeLabel = computed(
    () => repairModeMap[props.repairData?.repairMode] || props.repairData?.repairMode || '-'
  )
  const resultTypeLabel = computed(
    () => resultTypeMap[props.repairData?.resultType] || props.repairData?.resultType || '-'
  )
  const approveResultLabel = computed(
    () =>
      approveResultMap[props.repairData?.approveResult] || props.repairData?.approveResult || '-'
  )
  const relatedDisposalOrder = computed(() => props.repairData?.relatedDisposalOrder)
  const showDisposalBridgeAlert = computed(
    () =>
      props.repairData?.repairStatus === 'FINISHED' &&
      props.repairData?.resultType === 'SUGGEST_DISPOSAL'
  )
  const canCreateDisposal = computed(
    () =>
      showDisposalBridgeAlert.value &&
      repairItems.value.length === 1 &&
      !relatedDisposalOrder.value?.orderId &&
      hasPermission('asset:order:query') &&
      hasPermission('asset:order:add')
  )
  const canViewRelatedDisposal = computed(
    () => Boolean(relatedDisposalOrder.value?.orderId) && hasPermission('asset:order:query')
  )
  const bridgeAlertTitle = computed(() =>
    relatedDisposalOrder.value?.orderNo
      ? `Linked disposal order ${relatedDisposalOrder.value.orderNo}`
      : 'Repair finished with disposal suggestion'
  )
  const bridgeAlertDescription = computed(() =>
    relatedDisposalOrder.value?.orderNo
      ? `Source biz no: ${relatedDisposalOrder.value.sourceBizNo || '-'}`
      : repairItems.value.length > 1
        ? 'This repair order contains multiple assets. Split or confirm individual items before creating a disposal order.'
        : 'You can continue by creating a disposal order draft.'
  )
  const drawerTitle = computed(() =>
    props.repairData?.repairNo ? `Repair Detail - ${props.repairData.repairNo}` : 'Repair Detail'
  )
  const assetSummaryText = computed(() => {
    if (!repairItems.value.length) return '-'
    if (repairItems.value.length === 1) {
      const item = repairItems.value[0]
      return `${displayText(item.assetName)} / ${displayText(item.assetCode)}`
    }
    const firstItem = repairItems.value[0]
    return `${displayText(firstItem.assetName)} / ${displayText(firstItem.assetCode)} +${repairItems.value.length - 1}`
  })

  const canEdit = computed(() => ['DRAFT', 'REJECTED'].includes(currentStatus.value))
  const canSubmit = computed(() => ['DRAFT', 'REJECTED'].includes(currentStatus.value))
  const canApprove = computed(() => ['SUBMITTED'].includes(currentStatus.value))
  const canReject = computed(() => ['SUBMITTED'].includes(currentStatus.value))
  const canFinish = computed(() => ['APPROVED'].includes(currentStatus.value))
  const canCancel = computed(() =>
    ['DRAFT', 'SUBMITTED', 'REJECTED', 'APPROVED'].includes(currentStatus.value)
  )

  const hasPermission = (permission: string) => {
    const permissions = userStore.permissions || []
    return permissions.includes('*:*:*') || permissions.includes(permission)
  }

  const displayText = (value: unknown) => {
    if (value === null || value === undefined || value === '') return '-'
    return String(value)
  }

  const displayAmount = (value: unknown) => {
    if (value === null || value === undefined || value === '') return '-'
    return `${value} yuan`
  }

  const displayDowntime = (value: unknown) => {
    if (value === null || value === undefined || value === '') return '-'
    return `${value} h`
  }

  const displayApproval = (userName?: string, approveTime?: string) => {
    if (!userName && !approveTime) return '-'
    return [userName, approveTime].filter(Boolean).join(' / ')
  }

  watch(
    () => props.modelValue,
    (value) => {
      visible.value = value
    }
  )

  watch(
    () => visible.value,
    (value) => {
      emit('update:modelValue', value)
    }
  )

  const handleClosed = () => {
    visible.value = false
  }
</script>

<style scoped>
  .repair-detail-drawer {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .repair-detail-drawer__header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 16px;
  }

  .repair-detail-drawer__title {
    font-size: 18px;
    font-weight: 600;
    line-height: 1.4;
  }

  .repair-detail-drawer__subtitle {
    margin-top: 4px;
    color: var(--el-text-color-secondary);
  }

  .repair-detail-drawer__actions {
    display: flex;
    gap: 8px;
  }

  .repair-detail-drawer__summary {
    margin-bottom: 0;
  }

  .repair-detail-drawer__bridge-text {
    margin-top: 8px;
  }

  .repair-detail-drawer__bridge-btn {
    margin-top: 12px;
  }

  .repair-detail-drawer__linked-actions {
    display: flex;
    justify-content: flex-end;
  }

  .repair-detail-drawer__asset-summary {
    margin-bottom: 12px;
  }

  .repair-detail-drawer__footer {
    display: flex;
    justify-content: flex-end;
  }
</style>
