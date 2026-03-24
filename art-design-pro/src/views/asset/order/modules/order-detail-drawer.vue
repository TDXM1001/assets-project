<template>
  <ElDrawer
    v-model="visible"
    :title="drawerTitle"
    size="78%"
    append-to-body
    destroy-on-close
    @closed="handleClosed"
  >
    <div class="order-detail-drawer">
      <div class="order-detail-drawer__header">
        <div>
          <div class="order-detail-drawer__title">{{ displayText(orderData?.orderNo) }}</div>
          <div class="order-detail-drawer__subtitle">
            {{ displayText(scopeSummary) }}
          </div>
        </div>
        <div class="order-detail-drawer__actions">
          <ElTag effect="light" type="info">单据状态</ElTag>
          <DictTag :options="asset_order_status" :value="currentStatus" />
        </div>
      </div>

      <ElAlert
        class="mb-4"
        type="warning"
        :closable="false"
        show-icon
        title="详情抽屉先承载流程操作和单据摘要，后端明细列表接通后这里会补齐单据项。"
      />

      <ElCard shadow="never" class="mb-4">
        <template #header>基础信息</template>
        <ElDescriptions :column="2" border>
          <ElDescriptionsItem label="单据编号">
            {{ displayText(orderData?.orderNo) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="单据类型">
            <DictTag :options="asset_order_type" :value="orderData?.orderType" />
          </ElDescriptionsItem>
          <ElDescriptionsItem label="业务时间">
            {{ displayText(orderData?.bizDate) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="发起部门">
            {{ displayText(orderData?.applyDeptName || orderData?.applyDeptId) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="发起人">
            {{ displayText(orderData?.applyUserName || orderData?.applyUserId) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="附件数量">
            {{ displayText(orderData?.attachmentCount ?? 0) }}
          </ElDescriptionsItem>
        </ElDescriptions>
      </ElCard>

      <ElCard shadow="never" class="mb-4">
        <template #header>流转范围</template>
        <ElDescriptions :column="2" border>
          <ElDescriptionsItem label="来源部门">
            {{ displayText(orderData?.fromDeptName || orderData?.fromDeptId) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="目标部门">
            {{ displayText(orderData?.toDeptName || orderData?.toDeptId) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="来源责任人">
            {{ displayText(orderData?.fromUserName || orderData?.fromUserId) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="目标责任人">
            {{ displayText(orderData?.toUserName || orderData?.toUserId) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="来源位置">
            {{ displayText(orderData?.fromLocationName || orderData?.fromLocationId) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="目标位置">
            {{ displayText(orderData?.toLocationName || orderData?.toLocationId) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="预计归还日">
            {{ displayText(orderData?.expectedReturnDate) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="处置金额">
            {{ displayText(orderData?.disposalAmount ?? '-') }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="报废原因" :span="2">
            {{ displayText(orderData?.disposalReason) }}
          </ElDescriptionsItem>
        </ElDescriptions>
      </ElCard>

      <ElCard shadow="never" class="mb-4">
        <template #header>审批信息</template>
        <ElDescriptions :column="2" border>
          <ElDescriptionsItem label="审批人">
            {{ displayText(orderData?.approveUserName || orderData?.approveUserId) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="审批时间">
            {{ displayText(orderData?.approveTime) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="审批结果">
            {{ displayText(orderData?.approveResult) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="备注" :span="2">
            {{ displayText(orderData?.remark) }}
          </ElDescriptionsItem>
        </ElDescriptions>
      </ElCard>

      <ElCard shadow="never">
        <template #header>
          <div class="flex items-center justify-between">
            <span>单据项明细</span>
            <ElTag effect="plain" type="info">后端接通后展示单据项列表</ElTag>
          </div>
        </template>

        <template v-if="detailItems.length">
          <ElTable :data="detailItems" border stripe>
            <ElTableColumn prop="assetId" label="资产ID" width="120" />
            <ElTableColumn prop="beforeStatus" label="变更前状态" width="120" />
            <ElTableColumn prop="afterStatus" label="变更后状态" width="120" />
            <ElTableColumn prop="itemStatus" label="处理状态" width="120" />
            <ElTableColumn prop="itemResult" label="处理说明" min-width="180" />
          </ElTable>
        </template>
        <template v-else>
          <ElEmpty
            description="当前单据还没有接入明细列表，先通过单据头信息和流程操作支撑业务流转。"
          />
        </template>
      </ElCard>
    </div>

    <template #footer>
      <div class="order-detail-drawer__footer">
        <ElSpace wrap>
          <ElButton
            v-if="canEdit && hasPermission('asset:order:edit')"
            type="primary"
            plain
            @click="emit('edit')"
          >
            编辑
          </ElButton>
          <ElButton
            v-if="canSubmit && hasPermission('asset:order:submit')"
            type="primary"
            @click="emit('submit')"
          >
            提交
          </ElButton>
          <ElButton
            v-if="canApprove && hasPermission('asset:order:approve')"
            type="success"
            @click="emit('approve')"
          >
            通过
          </ElButton>
          <ElButton
            v-if="canReject && hasPermission('asset:order:reject')"
            type="danger"
            plain
            @click="emit('reject')"
          >
            驳回
          </ElButton>
          <ElButton
            v-if="canFinish && hasPermission('asset:order:finish')"
            type="warning"
            @click="emit('finish')"
          >
            完成
          </ElButton>
          <ElButton
            v-if="canCancel && hasPermission('asset:order:cancel')"
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
  import { useDict } from '@/utils/dict'
  import { useUserStore } from '@/store/modules/user'
  import DictTag from '@/components/DictTag/index.vue'

  const { asset_order_type, asset_order_status } = useDict('asset_order_type', 'asset_order_status')
  const userStore = useUserStore()

  const props = defineProps<{
    modelValue: boolean
    orderData?: any
  }>()

  const emit = defineEmits<{
    (e: 'update:modelValue', value: boolean): void
    (e: 'edit'): void
    (e: 'submit'): void
    (e: 'approve'): void
    (e: 'reject'): void
    (e: 'finish'): void
    (e: 'cancel'): void
  }>()

  const visible = ref(false)

  const currentStatus = computed(() => props.orderData?.orderStatus || 'DRAFT')

  const detailItems = computed(() => {
    const source =
      props.orderData?.itemList || props.orderData?.items || props.orderData?.detailList
    return Array.isArray(source) ? source : []
  })

  const drawerTitle = computed(() => {
    const title = props.orderData?.orderNo ? `单据详情 - ${props.orderData.orderNo}` : '单据详情'
    return title
  })

  const scopeSummary = computed(() => {
    const orderType = props.orderData?.orderType
    const fromDept =
      props.orderData?.fromDeptName || props.orderData?.fromDeptId || '来源部门待补充'
    const toDept = props.orderData?.toDeptName || props.orderData?.toDeptId || '目标部门待补充'
    const fromUser =
      props.orderData?.fromUserName || props.orderData?.fromUserId || '来源责任人待补充'
    const toUser = props.orderData?.toUserName || props.orderData?.toUserId || '目标责任人待补充'
    const fromLocation =
      props.orderData?.fromLocationName || props.orderData?.fromLocationId || '来源位置待补充'
    const toLocation =
      props.orderData?.toLocationName || props.orderData?.toLocationId || '目标位置待补充'

    switch (orderType) {
      case 'INBOUND':
        return `入库至 ${toDept} / ${toLocation}`
      case 'ASSIGN':
        return `${fromDept} / ${fromUser} 领用到 ${toDept} / ${toUser}`
      case 'BORROW':
        return `借用给 ${toUser}，预计归还日 ${props.orderData?.expectedReturnDate || '待补充'}`
      case 'RETURN':
        return `归还到 ${toDept} / ${toLocation}`
      case 'TRANSFER':
        return `${fromDept} / ${fromLocation} 调拨到 ${toDept} / ${toLocation}`
      case 'DISPOSAL':
        return `报废原因：${props.orderData?.disposalReason || '待补充'}`
      default:
        return `${fromDept} -> ${toDept}`
    }
  })

  /**
   * 这里统一判断单据流转状态，保证列表页和详情抽屉看到的是同一套动作规则。
   */
  const canEdit = computed(() => ['DRAFT', 'REJECTED'].includes(currentStatus.value))
  const canSubmit = computed(() => ['DRAFT', 'REJECTED'].includes(currentStatus.value))
  const canApprove = computed(() => ['SUBMITTED', 'APPROVING'].includes(currentStatus.value))
  const canReject = computed(() => ['SUBMITTED', 'APPROVING'].includes(currentStatus.value))
  const canFinish = computed(() => ['APPROVED'].includes(currentStatus.value))
  const canCancel = computed(() =>
    ['DRAFT', 'SUBMITTED', 'APPROVING', 'APPROVED'].includes(currentStatus.value)
  )

  /**
   * 详情展示时把空值兜底成短横线，避免抽屉里出现一堆空白字段。
   */
  const displayText = (value: unknown) => {
    if (value === null || value === undefined || value === '') return '-'
    return String(value)
  }

  const hasPermission = (permission: string) => {
    const permissions = userStore.permissions || []
    return permissions.includes('*:*:*') || permissions.includes(permission)
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
