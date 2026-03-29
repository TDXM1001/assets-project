/**
 * 资产维修明细标准化与映射模块
 * 
 * 核心职责：处理“单资产维修”与“多资产明细”之间的数据转换，支持从资产台账自动映射到维修项。
 */

/** 维修明细数据负载接口 */
export interface RepairItemPayload extends Record<string, any> {
  rowKey?: string
  repairItemId?: number | string
  assetId?: number | string
  assetCode?: string
  assetName?: string
  beforeStatus?: string
  afterStatus?: string
  resultType?: string
  faultDesc?: string
  remark?: string
  currentUserId?: number | string
  currentUserName?: string
  currentDeptId?: number | string
  currentDeptName?: string
  currentLocationId?: number | string
  currentLocationName?: string
  assetStatus?: string
  useOrgDeptId?: number | string
  useOrgDeptName?: string
}

/** 资产解析配置项 */
export interface ResolveRepairItemsOptions {
  /** 列表页严格模式：当仅有单头快照且明确是多资产时，不回填伪单资产明细 */
  strictListMode?: boolean
}

/** 构建明细行唯一键 */
const buildRepairItemRowKey = (item: RepairItemPayload, index: number) => {
  const identity = item.rowKey || item.repairItemId || item.assetId || item.assetCode || index
  return `repair-${String(identity)}-${index}`
}

/** 规范化源数据项 */
const normalizeSourceItem = (item: RepairItemPayload, index: number) => ({
  ...item,
  rowKey: String(item.rowKey || buildRepairItemRowKey(item, index))
})

/** 剥离 UI 层的辅助键（保存进数据库前使用） */
export const stripRepairItemRowKey = (item: RepairItemPayload) => {
  const payloadItem: Record<string, any> = { ...item }
  delete payloadItem.rowKey
  return payloadItem
}

/** 序列化明细列表 */
export const serializeRepairItems = (items: RepairItemPayload[] = []) =>
  items.map((item) => stripRepairItemRowKey(item))

/**
 * 解析维修单明细
 * 兼容处理：支持显式明细列表展示，也支持旧单据（只有单头关联资产）的自动回填封装。
 */
export const resolveRepairItems = (
  repair?: Record<string, any>,
  options: ResolveRepairItemsOptions = {}
) => {
  // 按照优先级获取源列表数据
  const sourceItems = Array.isArray(repair?.itemList)
    ? repair.itemList
    : Array.isArray(repair?.repairItems)
      ? repair.repairItems
      : []

  if (sourceItems.length > 0) {
    return sourceItems
      .filter(Boolean)
      .map((item: RepairItemPayload, index: number) => normalizeSourceItem(item, index))
  }

  if (!repair?.assetId) {
    return []
  }

  if (options.strictListMode && Number(repair?.itemCount || 0) > 1) {
    return []
  }

  return [
    {
      rowKey: String(repair.rowKey || buildRepairItemRowKey(repair, 0)),
      repairItemId: repair.repairItemId,
      assetId: repair.assetId,
      assetCode: repair.assetCode || '',
      assetName: repair.assetName || '',
      beforeStatus: repair.beforeStatus,
      afterStatus: repair.afterStatus,
      resultType: repair.resultType,
      faultDesc: repair.faultDesc,
      remark: repair.remark,
      currentUserId: repair.currentUserId,
      currentUserName: repair.currentUserName,
      currentDeptId: repair.currentDeptId || repair.useOrgDeptId,
      currentDeptName: repair.currentDeptName || repair.useOrgDeptName,
      currentLocationId: repair.currentLocationId,
      currentLocationName: repair.currentLocationName
    }
  ]
}

/**
 * 解析维修单详情记录
 * 处理整单快照与明细项的聚合，用于详情页展示。
 */
export const resolveRepairDetailRecord = (
  repair?: Record<string, any>,
  options: ResolveRepairItemsOptions = {}
) => {
  const itemList = resolveRepairItems(repair, options)
  const primaryItem = itemList[0]

  return {
    ...(repair || {}),
    itemList,
    itemCount: itemList.length,
    assetId: repair?.assetId ?? primaryItem?.assetId,
    assetCode: repair?.assetCode || primaryItem?.assetCode || '',
    assetName: repair?.assetName || primaryItem?.assetName || '',
    beforeStatus: repair?.beforeStatus || primaryItem?.beforeStatus || '',
    afterStatus: repair?.afterStatus || primaryItem?.afterStatus || primaryItem?.beforeStatus || '',
    faultDesc: repair?.faultDesc || primaryItem?.faultDesc || ''
  }
}

/** 格式化资产编码显示（支持“等 N 项”后缀） */
export const formatRepairAssetCode = (
  repair?: Record<string, any>,
  options: ResolveRepairItemsOptions = {}
) => {
  const items = resolveRepairItems(repair, options)
  if (!items.length) {
    if (repair?.assetCode) {
      return Number(repair?.itemCount || 0) > 1
        ? `${repair.assetCode} 等 ${repair.itemCount} 项`
        : repair.assetCode
    }
    return '-'
  }

  if (items.length === 1) return items[0].assetCode || '-'
  return `${items[0].assetCode || '-'} 等 ${items.length} 项`
}

/** 格式化资产名称显示（支持“等 N 项”后缀） */
export const formatRepairAssetName = (
  repair?: Record<string, any>,
  options: ResolveRepairItemsOptions = {}
) => {
  const items = resolveRepairItems(repair, options)
  if (!items.length) {
    if (repair?.assetName) {
      return Number(repair?.itemCount || 0) > 1
        ? `${repair.assetName} 等 ${repair.itemCount} 项`
        : repair.assetName
    }
    return '-'
  }

  if (items.length === 1) return items[0].assetName || '-'
  return `${items[0].assetName || '-'} 等 ${items.length} 项`
}

/** 构建维修单头快照（用于多资产单据在旧表结构下的投影） */
export const buildRepairHeaderSnapshot = (
  item?: Partial<RepairItemPayload>,
  options: { faultDesc?: string } = {}
) => ({
  assetId: item?.assetId,
  assetCode: item?.assetCode || '',
  assetName: item?.assetName || '',
  beforeStatus: item?.beforeStatus || '',
  afterStatus: item?.afterStatus || '',
  currentUserId: item?.currentUserId,
  currentDeptId: item?.currentDeptId,
  currentLocationId: item?.currentLocationId,
  faultDesc: String(item?.faultDesc || options.faultDesc || '').trim()
})

/** 解析工作台编辑状态 */
export const resolveRepairFormState = (
  repair?: Record<string, any>,
  options: ResolveRepairItemsOptions = {}
) => {
  const repairItems = resolveRepairItems(repair, options)
  const primaryItem = repairItems[0]

  return {
    repairItems,
    ...buildRepairHeaderSnapshot(primaryItem, { faultDesc: repair?.faultDesc })
  }
}

/** 从台账资产记录创建维修明细行 */
export const createRepairItemFromAsset = (asset?: Partial<RepairItemPayload>) => ({
  rowKey: String(asset?.rowKey || buildRepairItemRowKey(asset || {}, 0)),
  repairItemId: asset?.repairItemId,
  assetId: asset?.assetId,
  assetCode: asset?.assetCode || '',
  assetName: asset?.assetName || '',
  beforeStatus: asset?.assetStatus || asset?.beforeStatus || '',
  afterStatus: asset?.assetStatus || asset?.afterStatus || asset?.beforeStatus || '',
  resultType: asset?.resultType || '',
  faultDesc: asset?.faultDesc || '',
  remark: asset?.remark || '',
  currentUserId: asset?.currentUserId,
  currentUserName: asset?.currentUserName,
  currentDeptId: asset?.currentDeptId || asset?.useOrgDeptId,
  currentDeptName: asset?.currentDeptName || asset?.useOrgDeptName,
  currentLocationId: asset?.currentLocationId,
  currentLocationName: asset?.currentLocationName
})
