<template>
  <AssetPageShell
    v-if="pageMode"
    eyebrow="资产维修单"
    title="维修单详情"
    :description="assetSummaryText"
  >
    <template #tags>
      <ElSpace wrap>
        <ElTag type="info" effect="light">独立页面</ElTag>
        <ElTag :type="statusTagType" effect="light">{{ currentStatusLabel }}</ElTag>
        <ElTag type="info" effect="plain">多资产明细</ElTag>
      </ElSpace>
    </template>

    <div class="repair-detail-page">
      <ElAlert
        v-if="showDisposalBridgeAlert"
        class="repair-detail-page__summary"
        type="warning"
        :closable="false"
        show-icon
        :title="bridgeAlertTitle"
        :description="bridgeAlertDescription"
      >
        <template #default>
          <div class="repair-detail-page__bridge-text">
            {{ bridgeAlertDescription }}
          </div>
          <ElButton
            v-if="canCreateDisposal"
            type="warning"
            size="small"
            class="repair-detail-page__bridge-btn"
            @click="emit('createDisposal')"
          >
            创建处置单
          </ElButton>
        </template>
      </ElAlert>

      <ElCard v-if="relatedDisposalOrder" shadow="never" class="mb-4">
        <template #header>关联处置单</template>
        <ElDescriptions :column="2" border>
          <ElDescriptionsItem label="单号">
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
          <ElDescriptionsItem label="处置原因" :span="2">
            {{ displayText(relatedDisposalOrder.disposalReason) }}
          </ElDescriptionsItem>
        </ElDescriptions>
      </ElCard>

      <div
        v-if="relatedDisposalOrder && canViewRelatedDisposal"
        class="repair-detail-page__linked-actions"
      >
        <ElButton type="primary" @click="emit('viewDisposal', relatedDisposalOrder)">
          查看处置单
        </ElButton>
      </div>

      <ElCard shadow="never" class="mb-4">
        <template #header>维修资产明细</template>
        <div class="repair-detail-page__asset-summary">
          <ElTag type="info" effect="plain">资产数量：{{ repairItems.length }}</ElTag>
        </div>
        <ElTable
          :data="repairItems"
          border
          stripe
          size="small"
          row-key="rowKey"
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
          <ElTableColumn label="维修后状态" width="120" align="center">
            <template #default="{ row }">
              <DictTag :options="asset_status" :value="row.afterStatus" />
            </template>
          </ElTableColumn>
          <ElTableColumn label="处理结果" width="140" align="center">
            <template #default="{ row }">
              {{ displayText(resultTypeMap[row.resultType] || row.resultType) }}
            </template>
          </ElTableColumn>
          <ElTableColumn prop="faultDesc" label="故障描述" min-width="220" show-overflow-tooltip />
          <ElTableColumn prop="remark" label="明细备注" min-width="180" show-overflow-tooltip />
        </ElTable>
      </ElCard>

      <ElCard shadow="never" class="mb-4">
        <template #header>维修概览</template>
        <ElDescriptions :column="2" border>
          <ElDescriptionsItem label="报修时间">
            {{ displayText(repairData?.reportTime) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="维修方式">
            {{ displayText(repairModeLabel) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="申请部门">
            {{ displayText(repairData?.applyDeptName || repairData?.applyDeptId) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="申请人">
            {{ displayText(repairData?.applyUserName || repairData?.applyUserId) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="维修厂商">
            {{ displayText(repairData?.vendorName) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="资产数量">
            {{ repairItems.length || '-' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="主单故障描述" :span="2">
            {{ displayText(repairData?.faultDesc) }}
          </ElDescriptionsItem>
        </ElDescriptions>
      </ElCard>

      <ElCard shadow="never" class="mb-4">
        <template #header>审批与完工</template>
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
          <ElDescriptionsItem label="停机时长">
            {{ displayDowntime(repairData?.downtimeHours) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="完成时间">
            {{ displayText(repairData?.finishTime) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="主单结果">
            {{ displayText(resultTypeLabel) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="是否返修">
            {{ repairData?.reworkFlag === '1' ? '是' : '否' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="备注" :span="2">
            {{ displayText(repairData?.remark) }}
          </ElDescriptionsItem>
        </ElDescriptions>
      </ElCard>
    </div>
  </AssetPageShell>

  <component
    v-else
    :is="EpDrawer"
    v-model="visible"
    :title="drawerTitle"
    size="78%"
    append-to-body
    modal
    show-close
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
            创建处置单
          </ElButton>
        </template>
      </ElAlert>

      <ElCard v-if="relatedDisposalOrder" shadow="never" class="mb-4">
        <template #header>关联处置单</template>
        <ElDescriptions :column="2" border>
          <ElDescriptionsItem label="单号">
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
          <ElDescriptionsItem label="处置原因" :span="2">
            {{ displayText(relatedDisposalOrder.disposalReason) }}
          </ElDescriptionsItem>
        </ElDescriptions>
      </ElCard>

      <div
        v-if="relatedDisposalOrder && canViewRelatedDisposal"
        class="repair-detail-drawer__linked-actions"
      >
        <ElButton type="primary" @click="emit('viewDisposal', relatedDisposalOrder)">
          查看处置单
        </ElButton>
      </div>

      <ElCard shadow="never" class="mb-4">
        <template #header>维修资产明细</template>
        <div class="repair-detail-drawer__asset-summary">
          <ElTag type="info" effect="plain">资产数量：{{ repairItems.length }}</ElTag>
        </div>
        <ElTable
          :data="repairItems"
          border
          stripe
          size="small"
          row-key="rowKey"
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
          <ElTableColumn label="维修后状态" width="120" align="center">
            <template #default="{ row }">
              <DictTag :options="asset_status" :value="row.afterStatus" />
            </template>
          </ElTableColumn>
          <ElTableColumn label="处理结果" width="140" align="center">
            <template #default="{ row }">
              {{ displayText(resultTypeMap[row.resultType] || row.resultType) }}
            </template>
          </ElTableColumn>
          <ElTableColumn prop="faultDesc" label="故障描述" min-width="220" show-overflow-tooltip />
          <ElTableColumn prop="remark" label="明细备注" min-width="180" show-overflow-tooltip />
        </ElTable>
      </ElCard>

      <ElCard shadow="never" class="mb-4">
        <template #header>维修概览</template>
        <ElDescriptions :column="2" border>
          <ElDescriptionsItem label="报修时间">
            {{ displayText(repairData?.reportTime) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="维修方式">
            {{ displayText(repairModeLabel) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="申请部门">
            {{ displayText(repairData?.applyDeptName || repairData?.applyDeptId) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="申请人">
            {{ displayText(repairData?.applyUserName || repairData?.applyUserId) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="维修厂商">
            {{ displayText(repairData?.vendorName) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="资产数量">
            {{ repairItems.length || '-' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="主单故障描述" :span="2">
            {{ displayText(repairData?.faultDesc) }}
          </ElDescriptionsItem>
        </ElDescriptions>
      </ElCard>

      <ElCard shadow="never" class="mb-4">
        <template #header>审批与完工</template>
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
          <ElDescriptionsItem label="停机时长">
            {{ displayDowntime(repairData?.downtimeHours) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="完成时间">
            {{ displayText(repairData?.finishTime) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="主单结果">
            {{ displayText(resultTypeLabel) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="是否返修">
            {{ repairData?.reworkFlag === '1' ? '是' : '否' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="备注" :span="2">
            {{ displayText(repairData?.remark) }}
          </ElDescriptionsItem>
        </ElDescriptions>
      </ElCard>
    </div>

    <template v-if="!pageMode" #footer>
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
            审批通过
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
            完工
          </ElButton>
          <ElButton
            v-if="canCancel && hasPermission('asset:repair:cancel')"
            type="info"
            plain
            @click="emit('cancel')"
          >
            作废
          </ElButton>
          <ElButton @click="handleCancel">关闭</ElButton>
        </ElSpace>
      </div>
    </template>
  </component>

  <div v-if="pageMode" class="repair-detail-drawer__footer repair-detail-drawer__page-footer">
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
        审批通过
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
        完工
      </ElButton>
      <ElButton
        v-if="canCancel && hasPermission('asset:repair:cancel')"
        type="info"
        plain
        @click="emit('cancel')"
      >
        作废
      </ElButton>
      <ElButton @click="handleCancel">关闭</ElButton>
    </ElSpace>
  </div>
</template>

<script setup lang="ts">
  import { computed, ref, watch } from 'vue'
  import { ElDrawer as EpDrawer } from 'element-plus'
  import DictTag from '@/components/DictTag/index.vue'
  import { useDict } from '@/utils/dict'
  import { useUserStore } from '@/store/modules/user'
  import AssetPageShell from '../../shared/asset-page-shell.vue'
  import { resolveRepairItems } from './repair-item-normalize'

  const { asset_status, asset_order_status } = useDict('asset_status', 'asset_order_status')
  const userStore = useUserStore()

  const props = defineProps<{
    modelValue: boolean
    pageMode?: boolean
    repairData?: any
  }>()

  // 该组件用于“局部详情承载”，在抽屉和页面内嵌两种场景复用同一套展示结构。
  const emit = defineEmits<{
    (e: 'update:modelValue', value: boolean): void
    (e: 'close'): void
    (e: 'cancel'): void
    (e: 'edit'): void
    (e: 'submit'): void
    (e: 'approve'): void
    (e: 'reject'): void
    (e: 'finish'): void
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
    CANCELED: { label: '已作废', type: 'info' }
  }

  const repairModeMap: Record<string, string> = {
    IN_HOUSE: '内部维修',
    VENDOR: '厂商维修',
    ONSITE: '上门维修'
  }

  const resultTypeMap: Record<string, string> = {
    RESUME_USE: '恢复使用',
    TO_IDLE: '转闲置',
    SUGGEST_DISPOSAL: '建议处置'
  }

  const approveResultMap: Record<string, string> = {
    APPROVED: '已通过',
    REJECTED: '已驳回'
  }

  // 兼容旧数据：如果后端还未返回 itemList，则退化为单资产明细。
  const repairItems = computed(() => resolveRepairItems(props.repairData))

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
      ? `已关联处置单 ${relatedDisposalOrder.value.orderNo}`
      : '当前维修结果为建议处置'
  )
  const bridgeAlertDescription = computed(() =>
    relatedDisposalOrder.value?.orderNo
      ? `来源业务单号：${relatedDisposalOrder.value.sourceBizNo || '-'}`
      : repairItems.value.length > 1
        ? '当前维修单包含多项资产，请先逐项确认维修结果后再创建处置单。'
        : '你可以继续创建处置单草稿。'
  )
  const drawerTitle = computed(() =>
    props.repairData?.repairNo ? `维修详情 - ${props.repairData.repairNo}` : '维修详情'
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
    () => props.pageMode,
    (value) => {
      if (value) {
        // 页面模式下由外层页面控制显示，不再让抽屉状态影响主流程入口。
        visible.value = true
      }
    },
    { immediate: true }
  )

  watch(
    () => visible.value,
    (value) => {
      if (!props.pageMode) {
        emit('update:modelValue', value)
      } else if (value) {
        emit('update:modelValue', true)
      }
    }
  )

  const handleCancel = () => {
    if (props.pageMode) {
      emit('close')
      return
    }
    visible.value = false
  }

  const handleClosed = () => {
    visible.value = false
  }
</script>

<style scoped>
  .repair-detail-page {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .repair-detail-page :deep(.el-card) {
    border-radius: 18px;
    border-color: rgb(15 23 42 / 8%);
  }

  .repair-detail-page__summary {
    border-radius: 16px;
  }

  .repair-detail-page__bridge-text {
    margin-top: 8px;
  }

  .repair-detail-page__bridge-btn {
    margin-top: 12px;
  }

  .repair-detail-page__linked-actions {
    display: flex;
    justify-content: flex-end;
  }

  .repair-detail-page__asset-summary {
    margin-bottom: 12px;
  }

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

  .repair-detail-drawer--page {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .repair-detail-drawer__page-footer {
    position: sticky;
    bottom: 0;
    z-index: 10;
    padding: 14px 18px;
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 16px;
    border: 1px solid rgb(15 23 42 / 8%);
    border-radius: 18px;
    background: rgb(255 255 255 / 92%);
    backdrop-filter: blur(14px);
  }
</style>
