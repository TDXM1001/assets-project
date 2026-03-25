<template>
  <ElDialog
    :title="dialogType === 'add' ? '新增业务单据' : '编辑业务单据'"
    v-model="visible"
    width="1280px"
    destroy-on-close
    append-to-body
    @closed="handleClosed"
  >
    <ElAlert
      class="mb-4"
      type="info"
      show-icon
      :closable="false"
      title="单据头负责定义流转方向，单据明细负责圈定具体资产；两者一起提交后，后端才会生成完整业务快照。"
    />

    <ElForm
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="112px"
      v-loading="loading"
    >
      <ElDivider content-position="left">基础信息</ElDivider>
      <ElRow :gutter="16">
        <ElCol :span="12">
          <ElFormItem label="单据编号" prop="orderNo">
            <ElInput
              v-model="formData.orderNo"
              placeholder="提交后由后端生成"
              maxlength="64"
              disabled
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="单据类型" prop="orderType">
            <ElSelect v-model="formData.orderType" class="w-full" placeholder="请选择单据类型">
              <ElOption
                v-for="item in asset_order_type"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="业务时间" prop="bizDate">
            <ElDatePicker
              v-model="formData.bizDate"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              class="w-full"
              placeholder="请选择业务时间"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="单据状态" prop="orderStatus">
            <ElSelect v-model="formData.orderStatus" class="w-full" disabled>
              <ElOption
                v-for="item in asset_order_status"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElDivider content-position="left">发起信息</ElDivider>
      <ElRow :gutter="16">
        <ElCol :span="12">
          <ElFormItem label="发起部门" prop="applyDeptId">
            <ElTreeSelect
              v-model="formData.applyDeptId"
              :data="deptOptions"
              :props="{ value: 'id', label: 'label', children: 'children' }"
              value-key="id"
              filterable
              clearable
              check-strictly
              class="w-full"
              placeholder="请选择发起部门"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="发起人" prop="applyUserId">
            <ElSelect v-model="formData.applyUserId" filterable clearable class="w-full">
              <ElOption
                v-for="item in userOptions"
                :key="item.userId"
                :label="item.displayName"
                :value="item.userId"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElDivider content-position="left">流转范围</ElDivider>
      <ElRow :gutter="16">
        <ElCol :span="12">
          <ElFormItem label="来源部门" prop="fromDeptId">
            <ElTreeSelect
              v-model="formData.fromDeptId"
              :data="deptOptions"
              :props="{ value: 'id', label: 'label', children: 'children' }"
              value-key="id"
              filterable
              clearable
              check-strictly
              class="w-full"
              placeholder="请选择来源部门"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="目标部门" prop="toDeptId">
            <ElTreeSelect
              v-model="formData.toDeptId"
              :data="deptOptions"
              :props="{ value: 'id', label: 'label', children: 'children' }"
              value-key="id"
              filterable
              clearable
              check-strictly
              class="w-full"
              placeholder="请选择目标部门"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="来源责任人" prop="fromUserId">
            <ElSelect v-model="formData.fromUserId" filterable clearable class="w-full">
              <ElOption
                v-for="item in userOptions"
                :key="item.userId"
                :label="item.displayName"
                :value="item.userId"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="目标责任人" prop="toUserId">
            <ElSelect v-model="formData.toUserId" filterable clearable class="w-full">
              <ElOption
                v-for="item in userOptions"
                :key="item.userId"
                :label="item.displayName"
                :value="item.userId"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="来源位置" prop="fromLocationId">
            <ElTreeSelect
              v-model="formData.fromLocationId"
              :data="locationOptions"
              :props="{ value: 'id', label: 'label', children: 'children' }"
              value-key="id"
              filterable
              clearable
              check-strictly
              class="w-full"
              placeholder="请选择来源位置"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="目标位置" prop="toLocationId">
            <ElTreeSelect
              v-model="formData.toLocationId"
              :data="locationOptions"
              :props="{ value: 'id', label: 'label', children: 'children' }"
              value-key="id"
              filterable
              clearable
              check-strictly
              class="w-full"
              placeholder="请选择目标位置"
            />
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElDivider content-position="left">业务补充</ElDivider>
      <ElRow :gutter="16">
        <ElCol :span="12">
          <ElFormItem label="预计归还日" prop="expectedReturnDate">
            <ElDatePicker
              v-model="formData.expectedReturnDate"
              type="date"
              value-format="YYYY-MM-DD"
              class="w-full"
              placeholder="请选择预计归还日"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="处置金额" prop="disposalAmount">
            <ElInputNumber
              v-model="formData.disposalAmount"
              :min="0"
              :precision="2"
              :step="100"
              controls-position="right"
              class="w-full"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="附件数量" prop="attachmentCount">
            <ElInputNumber
              v-model="formData.attachmentCount"
              :min="0"
              controls-position="right"
              class="w-full"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="审批结果" prop="approveResult">
            <ElInput v-model="formData.approveResult" disabled placeholder="审批通过后回填" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="24">
          <ElFormItem label="报废原因" prop="disposalReason">
            <ElInput
              v-model="formData.disposalReason"
              type="textarea"
              :rows="2"
              maxlength="500"
              show-word-limit
              placeholder="当单据类型为报废时填写原因"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="24">
          <ElFormItem label="备注" prop="remark">
            <ElInput
              v-model="formData.remark"
              type="textarea"
              :rows="3"
              maxlength="500"
              show-word-limit
              placeholder="请输入备注说明"
            />
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElDivider content-position="left">单据明细</ElDivider>
      <ElAlert
        class="mb-4"
        type="warning"
        show-icon
        :closable="false"
        title="至少选择一项资产才能保存单据。单据头变更后，明细里的预期去向会自动跟随更新。"
      />

      <div class="order-item-toolbar">
        <ElSpace wrap>
          <ElButton type="primary" plain @click="openSelectorDialog">选择资产</ElButton>
          <ElButton :disabled="!formData.itemList.length" @click="handleClearItems"
            >清空明细</ElButton
          >
          <ElTag effect="light" type="info">已选 {{ formData.itemList.length }} 项</ElTag>
        </ElSpace>
      </div>

      <ElTable
        :data="formData.itemList"
        border
        stripe
        row-key="assetId"
        empty-text="请先选择需要进入单据的资产"
      >
        <ElTableColumn prop="assetCode" label="资产编码" min-width="150" />
        <ElTableColumn prop="assetName" label="资产名称" min-width="180" />
        <ElTableColumn label="变更前状态" width="120" align="center">
          <template #default="{ row }">
            <DictTag :options="asset_status" :value="row.beforeStatus" />
          </template>
        </ElTableColumn>
        <ElTableColumn label="变更后状态" width="120" align="center">
          <template #default="{ row }">
            <DictTag :options="asset_status" :value="row.afterStatus" />
          </template>
        </ElTableColumn>
        <ElTableColumn label="当前责任人" min-width="140">
          <template #default="{ row }">{{
            formatUserName(row.beforeUserId, row.beforeUserName)
          }}</template>
        </ElTableColumn>
        <ElTableColumn label="目标责任人" min-width="140">
          <template #default="{ row }">{{
            formatUserName(row.afterUserId, row.afterUserName)
          }}</template>
        </ElTableColumn>
        <ElTableColumn label="当前位置" min-width="160">
          <template #default="{ row }">
            {{ formatLocationName(row.beforeLocationId, row.beforeLocationName) }}
          </template>
        </ElTableColumn>
        <ElTableColumn label="目标位置" min-width="160">
          <template #default="{ row }">
            {{ formatLocationName(row.afterLocationId, row.afterLocationName) }}
          </template>
        </ElTableColumn>
        <ElTableColumn label="操作" width="88" fixed="right" align="center">
          <template #default="{ row, $index }">
            <ElButton link type="danger" @click="handleRemoveItem(row.assetId, $index)"
              >移除</ElButton
            >
          </template>
        </ElTableColumn>
      </ElTable>
    </ElForm>

    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="visible = false">取消</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">确定</ElButton>
      </div>
    </template>

    <ElDialog
      v-model="selectorVisible"
      title="选择资产"
      width="1080px"
      append-to-body
      destroy-on-close
      @open="handleSelectorOpen"
      @closed="handleSelectorClosed"
    >
      <ElForm :model="selectorQuery" inline class="order-selector-search">
        <ElFormItem label="资产编码">
          <ElInput v-model="selectorQuery.assetCode" clearable placeholder="请输入资产编码" />
        </ElFormItem>
        <ElFormItem label="资产名称">
          <ElInput v-model="selectorQuery.assetName" clearable placeholder="请输入资产名称" />
        </ElFormItem>
        <ElFormItem label="资产状态">
          <ElSelect
            v-model="selectorQuery.assetStatus"
            clearable
            placeholder="请选择资产状态"
            style="width: 160px"
          >
            <ElOption
              v-for="item in asset_status"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem>
          <ElSpace>
            <ElButton type="primary" @click="handleSelectorSearch">查询</ElButton>
            <ElButton @click="handleSelectorReset">重置</ElButton>
          </ElSpace>
        </ElFormItem>
      </ElForm>

      <ElTable
        ref="selectorTableRef"
        v-loading="selectorLoading"
        :data="selectorData"
        row-key="assetId"
        border
        stripe
        height="420"
        @selection-change="handleSelectorSelectionChange"
      >
        <ElTableColumn type="selection" width="50" />
        <ElTableColumn prop="assetCode" label="资产编码" min-width="150" />
        <ElTableColumn prop="assetName" label="资产名称" min-width="180" />
        <ElTableColumn label="资产状态" width="120" align="center">
          <template #default="{ row }">
            <DictTag :options="asset_status" :value="row.assetStatus" />
          </template>
        </ElTableColumn>
        <ElTableColumn label="责任人" min-width="140">
          <template #default="{ row }">{{ formatUserName(row.currentUserId) }}</template>
        </ElTableColumn>
        <ElTableColumn label="使用部门" min-width="160">
          <template #default="{ row }">{{ formatDeptName(row.useOrgDeptId) }}</template>
        </ElTableColumn>
        <ElTableColumn label="当前位置" min-width="160">
          <template #default="{ row }">{{ formatLocationName(row.currentLocationId) }}</template>
        </ElTableColumn>
      </ElTable>

      <div class="order-selector-pagination">
        <ElPagination
          background
          layout="total, sizes, prev, pager, next"
          :current-page="selectorQuery.pageNum"
          :page-size="selectorQuery.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="selectorTotal"
          @current-change="handleSelectorPageChange"
          @size-change="handleSelectorSizeChange"
        />
      </div>

      <template #footer>
        <div class="dialog-footer">
          <ElButton @click="selectorVisible = false">取消</ElButton>
          <ElButton type="primary" @click="handleAppendItems">加入单据</ElButton>
        </div>
      </template>
    </ElDialog>
  </ElDialog>
</template>

<script setup lang="ts">
  import { computed, nextTick, reactive, ref, watch } from 'vue'
  import type { FormInstance, FormRules, TableInstance } from 'element-plus'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { listUser, deptTreeSelect } from '@/api/system/user'
  import { treeLocationSelect } from '@/api/asset/location'
  import { listAssetInfo } from '@/api/asset/info'
  import { addAssetOrder, getAssetOrder, updateAssetOrder } from '@/api/asset/order'
  import DictTag from '@/components/DictTag/index.vue'
  import { useDict } from '@/utils/dict'
  import { useUserStore } from '@/store/modules/user'

  interface TreeOption {
    id: number
    label: string
    children?: TreeOption[]
  }

  interface UserOption {
    userId: number
    displayName: string
  }

  interface AssetCandidate {
    assetId: number
    assetCode: string
    assetName: string
    assetStatus: string
    currentUserId?: number
    useOrgDeptId?: number
    currentLocationId?: number
  }

  interface OrderItemForm {
    itemId?: number
    orderId?: number
    assetId: number
    assetCode: string
    assetName: string
    beforeStatus?: string
    afterStatus?: string
    beforeUserId?: number
    afterUserId?: number
    beforeUserName?: string
    afterUserName?: string
    beforeDeptId?: number
    afterDeptId?: number
    beforeDeptName?: string
    afterDeptName?: string
    beforeLocationId?: number
    afterLocationId?: number
    beforeLocationName?: string
    afterLocationName?: string
    itemStatus?: string
    itemResult?: string
  }

  const { asset_order_type, asset_order_status, asset_status } = useDict(
    'asset_order_type',
    'asset_order_status',
    'asset_status'
  )
  const userStore = useUserStore()
  const currentUserInfo = computed(() => userStore.getUserInfo)

  const props = defineProps<{
    modelValue: boolean
    dialogType: 'add' | 'edit'
    orderData?: any
  }>()

  const emit = defineEmits<{
    (e: 'update:modelValue', value: boolean): void
    (e: 'success'): void
  }>()

  const visible = ref(false)
  const loading = ref(false)
  const submitLoading = ref(false)
  const selectorVisible = ref(false)
  const selectorLoading = ref(false)
  const formRef = ref<FormInstance>()
  const selectorTableRef = ref<TableInstance>()

  const deptOptions = ref<TreeOption[]>([])
  const locationOptions = ref<TreeOption[]>([])
  const userOptions = ref<UserOption[]>([])
  const deptLabelMap = ref<Record<string, string>>({})
  const locationLabelMap = ref<Record<string, string>>({})
  const userLabelMap = ref<Record<string, string>>({})

  const selectorData = ref<AssetCandidate[]>([])
  const selectorTotal = ref(0)
  const selectorSelection = ref<AssetCandidate[]>([])

  const createInitialFormData = () => ({
    orderId: undefined as number | undefined,
    orderNo: '',
    orderType: 'INBOUND',
    bizDate: '',
    applyUserId: undefined as number | undefined,
    applyDeptId: undefined as number | undefined,
    fromDeptId: undefined as number | undefined,
    toDeptId: undefined as number | undefined,
    fromUserId: undefined as number | undefined,
    toUserId: undefined as number | undefined,
    fromLocationId: undefined as number | undefined,
    toLocationId: undefined as number | undefined,
    expectedReturnDate: '',
    disposalReason: '',
    disposalAmount: undefined as number | undefined,
    attachmentCount: 0,
    orderStatus: 'DRAFT',
    approveResult: '',
    remark: '',
    itemList: [] as OrderItemForm[]
  })

  const formData = reactive(createInitialFormData())

  const selectorQuery = reactive({
    pageNum: 1,
    pageSize: 10,
    assetCode: '',
    assetName: '',
    assetStatus: ''
  })

  const formRules: FormRules = {
    orderType: [{ required: true, message: '单据类型不能为空', trigger: 'change' }],
    bizDate: [{ required: true, message: '业务时间不能为空', trigger: 'change' }],
    applyUserId: [{ required: true, message: '发起人不能为空', trigger: 'change' }],
    applyDeptId: [{ required: true, message: '发起部门不能为空', trigger: 'change' }]
  }

  const flattenTreeLabels = (nodes: TreeOption[], map: Record<string, string>) => {
    nodes.forEach((node) => {
      map[String(node.id)] = node.label
      if (node.children?.length) {
        flattenTreeLabels(node.children, map)
      }
    })
  }

  const normalizeTreeOptions = (list: any[]): TreeOption[] => {
    return (list || []).map((item) => ({
      id: item.id ?? item.value ?? item.deptId ?? item.locationId,
      label: item.label ?? item.deptName ?? item.locationName ?? item.name ?? String(item.id),
      children: item.children ? normalizeTreeOptions(item.children) : []
    }))
  }

  /**
   * 统一加载明细编辑会用到的关联选项，并顺手建立标签映射，方便明细表直接显示人名/部门名/位置名。
   */
  const loadRelationOptions = async () => {
    const [deptRes, locationRes, userRes] = await Promise.all([
      deptTreeSelect(),
      treeLocationSelect(),
      listUser({ pageNum: 1, pageSize: 500 })
    ])

    const deptData = Array.isArray(deptRes) ? deptRes : (deptRes as any)?.data || []
    const locationData = Array.isArray(locationRes) ? locationRes : (locationRes as any)?.data || []
    const userData = Array.isArray(userRes)
      ? userRes
      : (userRes as any)?.rows || (userRes as any)?.data || []

    deptOptions.value = normalizeTreeOptions(deptData)
    locationOptions.value = normalizeTreeOptions(locationData)
    userOptions.value = userData.map((item: any) => ({
      userId: item.userId,
      displayName: item.nickName ? `${item.nickName} (${item.userName})` : item.userName
    }))

    const nextDeptLabelMap: Record<string, string> = {}
    const nextLocationLabelMap: Record<string, string> = {}
    flattenTreeLabels(deptOptions.value, nextDeptLabelMap)
    flattenTreeLabels(locationOptions.value, nextLocationLabelMap)
    deptLabelMap.value = nextDeptLabelMap
    locationLabelMap.value = nextLocationLabelMap
    userLabelMap.value = userOptions.value.reduce<Record<string, string>>((acc, item) => {
      acc[String(item.userId)] = item.displayName
      return acc
    }, {})
  }

  const fillDefaultApplicant = () => {
    const userInfo = (currentUserInfo.value || {}) as Record<string, any>
    if (userInfo.userId) {
      formData.applyUserId = userInfo.userId
    }
    if (userInfo.deptId) {
      formData.applyDeptId = userInfo.deptId
    }
  }

  const resetForm = () => {
    Object.assign(formData, createInitialFormData())
    fillDefaultApplicant()
  }

  const formatUserName = (userId?: number, fallbackName?: string) => {
    if (fallbackName) return fallbackName
    if (userId === null || userId === undefined) return '-'
    return userLabelMap.value[String(userId)] || String(userId)
  }

  const formatDeptName = (deptId?: number, fallbackName?: string) => {
    if (fallbackName) return fallbackName
    if (deptId === null || deptId === undefined) return '-'
    return deptLabelMap.value[String(deptId)] || String(deptId)
  }

  const formatLocationName = (locationId?: number, fallbackName?: string) => {
    if (fallbackName) return fallbackName
    if (locationId === null || locationId === undefined) return '-'
    return locationLabelMap.value[String(locationId)] || String(locationId)
  }

  /**
   * 这里和后端服务层保持同一套单据头推导逻辑，保证前端看到的预期去向和最终落账口径一致。
   */
  const resolveHeaderTargetSnapshot = (item: Partial<OrderItemForm>) => {
    switch (formData.orderType) {
      case 'INBOUND':
        return {
          afterStatus: 'IDLE',
          afterDeptId: formData.toDeptId,
          afterUserId: undefined,
          afterLocationId: formData.toLocationId
        }
      case 'ASSIGN':
      case 'TRANSFER':
        return {
          afterStatus: 'IN_USE',
          afterDeptId: formData.toDeptId,
          afterUserId: formData.toUserId,
          afterLocationId: formData.toLocationId
        }
      case 'BORROW':
        return {
          afterStatus: 'BORROWED',
          afterDeptId: formData.toDeptId,
          afterUserId: formData.toUserId,
          afterLocationId: formData.toLocationId
        }
      case 'RETURN':
        return {
          afterStatus: 'IDLE',
          afterDeptId: formData.toDeptId,
          afterUserId: undefined,
          afterLocationId: formData.toLocationId
        }
      case 'DISPOSAL':
        return {
          afterStatus: 'DISPOSED',
          afterDeptId: item.beforeDeptId,
          afterUserId: item.beforeUserId,
          afterLocationId: formData.toLocationId || item.beforeLocationId
        }
      default:
        return {
          afterStatus: item.beforeStatus,
          afterDeptId: item.beforeDeptId,
          afterUserId: item.beforeUserId,
          afterLocationId: item.beforeLocationId
        }
    }
  }

  const syncItemTargetsWithHeader = () => {
    formData.itemList = formData.itemList.map((item) => {
      const target = resolveHeaderTargetSnapshot(item)
      return {
        ...item,
        ...target
      }
    })
  }

  const normalizeOrderItems = (items: any[] = []): OrderItemForm[] => {
    return items.map((item) => ({
      itemId: item.itemId,
      orderId: item.orderId,
      assetId: item.assetId,
      assetCode: item.assetCode,
      assetName: item.assetName,
      beforeStatus: item.beforeStatus,
      afterStatus: item.afterStatus,
      beforeUserId: item.beforeUserId,
      afterUserId: item.afterUserId,
      beforeUserName: item.beforeUserName,
      afterUserName: item.afterUserName,
      beforeDeptId: item.beforeDeptId,
      afterDeptId: item.afterDeptId,
      beforeDeptName: item.beforeDeptName,
      afterDeptName: item.afterDeptName,
      beforeLocationId: item.beforeLocationId,
      afterLocationId: item.afterLocationId,
      beforeLocationName: item.beforeLocationName,
      afterLocationName: item.afterLocationName,
      itemStatus: item.itemStatus,
      itemResult: item.itemResult
    }))
  }

  /**
   * 从台账选中资产后，前端只负责把必要快照带入单据；服务层仍会再兜底补齐并做重复校验。
   */
  const buildOrderItemFromAsset = (asset: AssetCandidate): OrderItemForm => {
    const seedItem: Partial<OrderItemForm> = {
      assetId: asset.assetId,
      assetCode: asset.assetCode,
      assetName: asset.assetName,
      beforeStatus: asset.assetStatus,
      beforeUserId: asset.currentUserId,
      beforeDeptId: asset.useOrgDeptId,
      beforeLocationId: asset.currentLocationId
    }
    return {
      ...(seedItem as OrderItemForm),
      ...resolveHeaderTargetSnapshot(seedItem),
      beforeUserName: asset.currentUserId
        ? userLabelMap.value[String(asset.currentUserId)]
        : undefined,
      beforeDeptName: asset.useOrgDeptId
        ? deptLabelMap.value[String(asset.useOrgDeptId)]
        : undefined,
      beforeLocationName: asset.currentLocationId
        ? locationLabelMap.value[String(asset.currentLocationId)]
        : undefined,
      itemStatus: formData.orderStatus
    }
  }

  const loadSelectorData = async () => {
    selectorLoading.value = true
    try {
      const response: any = await listAssetInfo({ ...selectorQuery })
      selectorData.value = response?.rows || response?.data?.rows || response?.data || []
      selectorTotal.value = Number(response?.total || response?.data?.total || 0)
      await nextTick()
      restoreSelectorSelection()
    } finally {
      selectorLoading.value = false
    }
  }

  const restoreSelectorSelection = () => {
    const selectedIds = new Set(selectorSelection.value.map((item) => item.assetId))
    selectorData.value.forEach((row) => {
      if (selectedIds.has(row.assetId)) {
        selectorTableRef.value?.toggleRowSelection?.(row, true)
      }
    })
  }

  const handleSelectorSelectionChange = (rows: AssetCandidate[]) => {
    selectorSelection.value = rows
  }

  const handleSelectorSearch = () => {
    selectorQuery.pageNum = 1
    loadSelectorData()
  }

  const handleSelectorReset = () => {
    selectorQuery.assetCode = ''
    selectorQuery.assetName = ''
    selectorQuery.assetStatus = ''
    selectorQuery.pageNum = 1
    loadSelectorData()
  }

  const handleSelectorPageChange = (pageNum: number) => {
    selectorQuery.pageNum = pageNum
    loadSelectorData()
  }

  const handleSelectorSizeChange = (pageSize: number) => {
    selectorQuery.pageSize = pageSize
    selectorQuery.pageNum = 1
    loadSelectorData()
  }

  const openSelectorDialog = () => {
    selectorVisible.value = true
  }

  const handleSelectorOpen = async () => {
    selectorSelection.value = []
    await loadSelectorData()
  }

  const handleSelectorClosed = () => {
    selectorSelection.value = []
  }

  const handleAppendItems = () => {
    if (!selectorSelection.value.length) {
      ElMessage.warning('请至少选择一项资产')
      return
    }

    const itemMap = new Map<number, OrderItemForm>()
    formData.itemList.forEach((item) => {
      itemMap.set(item.assetId, item)
    })
    selectorSelection.value.forEach((asset) => {
      itemMap.set(asset.assetId, buildOrderItemFromAsset(asset))
    })
    formData.itemList = Array.from(itemMap.values())
    selectorVisible.value = false
    ElMessage.success(`已加入 ${selectorSelection.value.length} 项资产`)
  }

  const handleRemoveItem = async (assetId?: number, index?: number) => {
    if (assetId === undefined && index === undefined) return
    try {
      await ElMessageBox.confirm('确认将该资产从单据明细中移除吗？', '提示', { type: 'warning' })
      formData.itemList = formData.itemList.filter((item, currentIndex) => {
        if (assetId !== undefined) {
          return item.assetId !== assetId
        }
        return currentIndex !== index
      })
    } catch (error) {
      if (error !== 'cancel') {
        console.error('移除单据明细失败:', error)
      }
    }
  }

  const handleClearItems = async () => {
    try {
      await ElMessageBox.confirm('确认清空当前单据明细吗？', '提示', { type: 'warning' })
      formData.itemList = []
    } catch (error) {
      if (error !== 'cancel') {
        console.error('清空单据明细失败:', error)
      }
    }
  }

  watch(
    () => props.modelValue,
    async (value) => {
      visible.value = value
      if (!value) return

      loading.value = true
      try {
        await loadRelationOptions()
        if (props.dialogType === 'edit' && props.orderData?.orderId) {
          const detail: any = await getAssetOrder(props.orderData.orderId)
          const order = detail?.data || detail || props.orderData
          Object.assign(formData, createInitialFormData(), order, {
            itemList: normalizeOrderItems(
              order?.itemList || order?.items || order?.detailList || []
            )
          })
        } else {
          resetForm()
        }
      } catch (error) {
        console.error('加载业务单据详情失败:', error)
        if (props.dialogType === 'edit') {
          Object.assign(formData, createInitialFormData(), props.orderData || {}, {
            itemList: normalizeOrderItems(
              props.orderData?.itemList ||
                props.orderData?.items ||
                props.orderData?.detailList ||
                []
            )
          })
        } else {
          resetForm()
        }
      } finally {
        loading.value = false
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
    () => [formData.orderType, formData.toDeptId, formData.toUserId, formData.toLocationId],
    () => {
      if (formData.itemList.length) {
        syncItemTargetsWithHeader()
      }
    }
  )

  const handleSubmit = async () => {
    if (!formRef.value) return

    const valid = await formRef.value.validate().catch(() => false)
    if (!valid) return

    if (!formData.itemList.length) {
      ElMessage.warning('请至少选择一项资产后再保存单据')
      return
    }

    submitLoading.value = true
    try {
      const payload = {
        ...formData,
        itemList: formData.itemList.map((item) => ({
          itemId: item.itemId,
          orderId: item.orderId,
          assetId: item.assetId,
          assetCode: item.assetCode,
          assetName: item.assetName,
          beforeStatus: item.beforeStatus,
          afterStatus: item.afterStatus,
          beforeUserId: item.beforeUserId,
          afterUserId: item.afterUserId,
          beforeDeptId: item.beforeDeptId,
          afterDeptId: item.afterDeptId,
          beforeLocationId: item.beforeLocationId,
          afterLocationId: item.afterLocationId,
          itemStatus: item.itemStatus,
          itemResult: item.itemResult
        }))
      }

      if (props.dialogType === 'add') {
        await addAssetOrder(payload)
      } else {
        await updateAssetOrder(payload)
      }
      ElMessage.success(props.dialogType === 'add' ? '新增成功' : '修改成功')
      visible.value = false
      emit('success')
    } catch (error) {
      console.error('提交业务单据失败:', error)
    } finally {
      submitLoading.value = false
    }
  }

  const handleClosed = () => {
    resetForm()
    selectorVisible.value = false
    selectorSelection.value = []
    selectorData.value = []
    selectorTotal.value = 0
    selectorQuery.pageNum = 1
    selectorQuery.pageSize = 10
    selectorQuery.assetCode = ''
    selectorQuery.assetName = ''
    selectorQuery.assetStatus = ''
    loading.value = false
    submitLoading.value = false
    formRef.value?.clearValidate?.()
  }
</script>

<style lang="scss" scoped>
  .order-item-toolbar {
    display: flex;
    justify-content: space-between;
    margin-bottom: 16px;
  }

  .order-selector-search {
    margin-bottom: 16px;
  }

  .order-selector-pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }
</style>
