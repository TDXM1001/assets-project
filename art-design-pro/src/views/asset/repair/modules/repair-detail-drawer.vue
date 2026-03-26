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
          <div class="repair-detail-drawer__subtitle">
            {{ displayText(repairData?.assetName) }} / {{ displayText(repairData?.assetCode) }}
          </div>
        </div>
        <div class="repair-detail-drawer__actions">
          <ElTag effect="light" type="info">维修状态</ElTag>
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
            创建报废单
          </ElButton>
        </template>
      </ElAlert>

      <ElCard v-if="relatedDisposalOrder" shadow="never" class="mb-4">
        <template #header>关联报废单</template>
        <ElDescriptions :column="2" border>
          <ElDescriptionsItem label="报废单号">
            {{ displayText(relatedDisposalOrder.orderNo) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="来源业务单号">
            {{ displayText(relatedDisposalOrder.sourceBizNo) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="单据状态">
            <DictTag :options="asset_order_status" :value="relatedDisposalOrder.orderStatus" />
          </ElDescriptionsItem>
          <ElDescriptionsItem label="处置金额">
            {{ displayAmount(relatedDisposalOrder.disposalAmount) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="审批信息">
            {{
              displayApproval(
                relatedDisposalOrder.approveUserName,
                relatedDisposalOrder.approveTime
              )
            }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="报废原因" :span="2">
            {{ displayText(relatedDisposalOrder.disposalReason) }}
          </ElDescriptionsItem>
        </ElDescriptions>
      </ElCard>

      <div
        v-if="relatedDisposalOrder && canViewRelatedDisposal"
        class="repair-detail-drawer__linked-actions"
      >
        <ElButton type="primary" @click="emit('viewDisposal', relatedDisposalOrder)">
          查看报废单
        </ElButton>
      </div>

      <ElCard shadow="never" class="mb-4">
        <template #header>资产信息</template>
        <ElDescriptions :column="2" border>
          <ElDescriptionsItem label="资产编码">
            {{ displayText(repairData?.assetCode) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="资产名称">
            {{ displayText(repairData?.assetName) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="维修前状态">
            <DictTag :options="asset_status" :value="repairData?.beforeStatus" />
          </ElDescriptionsItem>
          <ElDescriptionsItem label="维修后状态">
            <DictTag :options="asset_status" :value="repairData?.afterStatus" />
          </ElDescriptionsItem>
        </ElDescriptions>
      </ElCard>

      <ElCard shadow="never" class="mb-4">
        <template #header>报修信息</template>
        <ElDescriptions :column="2" border>
          <ElDescriptionsItem label="报修时间">
            {{ displayText(repairData?.reportTime) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="维修方式">
            {{ displayText(repairModeLabel) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="发起部门">
            {{ displayText(repairData?.applyDeptName || repairData?.applyDeptId) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="发起人">
            {{ displayText(repairData?.applyUserName || repairData?.applyUserId) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="供应商">
            {{ displayText(repairData?.vendorName) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="故障描述" :span="2">
            {{ displayText(repairData?.faultDesc) }}
          </ElDescriptionsItem>
        </ElDescriptions>
      </ElCard>

      <ElCard shadow="never" class="mb-4">
        <template #header>审批与处理</template>
        <ElDescriptions :column="2" border>
          <ElDescriptionsItem label="审批人">
            {{ displayText(repairData?.approveUserName || repairData?.approveUserId) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="审批时间">
            {{ displayText(repairData?.approveTime) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="审批结果">
            {{ displayText(approveResultLabel) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="送修时间">
            {{ displayText(repairData?.sendRepairTime) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="维修费用">
            {{ displayAmount(repairData?.repairCost) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="停用时长">
            {{ displayDowntime(repairData?.downtimeHours) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="完成时间">
            {{ displayText(repairData?.finishTime) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="完成结果">
            {{ displayText(resultTypeLabel) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="是否返修">
            {{ repairData?.reworkFlag === '1' ? '是' : '否' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="处理说明" :span="2">
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
            附件
          </ElButton>
          <ElButton
            v-if="canEdit && hasPermission('asset:repair:edit')"
            type="primary"
            plain
            @click="emit('edit')"
          >
            编辑
          </ElButton>
          <ElButton
            v-if="canSubmit && hasPermission('asset:repair:submit')"
            type="primary"
            @click="emit('submit')"
          >
            提交
          </ElButton>
          <ElButton
            v-if="canApprove && hasPermission('asset:repair:approve')"
            type="success"
            @click="emit('approve')"
          >
            通过
          </ElButton>
          <ElButton
            v-if="canReject && hasPermission('asset:repair:reject')"
            type="danger"
            plain
            @click="emit('reject')"
          >
            驳回
          </ElButton>
          <ElButton
            v-if="canFinish && hasPermission('asset:repair:finish')"
            type="warning"
            @click="emit('finish')"
          >
            完成维修
          </ElButton>
          <ElButton
            v-if="canCancel && hasPermission('asset:repair:cancel')"
            type="info"
            plain
            @click="emit('cancel')"
          >
            取消
          </ElButton>
          <ElButton @click="visible = false">关闭</ElButton>
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
    DRAFT: { label: '草稿', type: 'info' },
    SUBMITTED: { label: '待审批', type: 'warning' },
    APPROVED: { label: '维修中', type: 'warning' },
    REJECTED: { label: '已驳回', type: 'danger' },
    FINISHED: { label: '已完成', type: 'success' },
    CANCELED: { label: '已取消', type: 'info' }
  }

  const repairModeMap: Record<string, string> = {
    IN_HOUSE: '内部维修',
    VENDOR: '外部送修',
    ONSITE: '上门维修'
  }

  const resultTypeMap: Record<string, string> = {
    RESUME_USE: '恢复在用',
    TO_IDLE: '转闲置',
    SUGGEST_DISPOSAL: '建议报废'
  }

  const approveResultMap: Record<string, string> = {
    APPROVED: '审批通过',
    REJECTED: '审批驳回'
  }

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
      !relatedDisposalOrder.value?.orderId &&
      hasPermission('asset:order:query') &&
      hasPermission('asset:order:add')
  )
  const canViewRelatedDisposal = computed(
    () => Boolean(relatedDisposalOrder.value?.orderId) && hasPermission('asset:order:query')
  )
  const bridgeAlertTitle = computed(() =>
    relatedDisposalOrder.value?.orderNo
      ? `已关联报废单 ${relatedDisposalOrder.value.orderNo}`
      : '维修已完成，建议报废'
  )
  const bridgeAlertDescription = computed(() =>
    relatedDisposalOrder.value?.orderNo
      ? `来源业务单号：${relatedDisposalOrder.value.sourceBizNo || '-'}`
      : '下一步应创建报废单草稿。'
  )
  const drawerTitle = computed(() =>
    props.repairData?.repairNo ? `维修详情 - ${props.repairData.repairNo}` : '维修详情'
  )

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
    return `${value} 元`
  }

  const displayDowntime = (value: unknown) => {
    if (value === null || value === undefined || value === '') return '-'
    return `${value} 小时`
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
