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

export interface ResolveRepairItemsOptions {
  // 列表页严格模式：当仅有单头快照且明确是多资产时，不回填伪单资产明细。
  strictListMode?: boolean
}

const buildRepairItemRowKey = (item: RepairItemPayload, index: number) => {
  const identity = item.rowKey || item.repairItemId || item.assetId || item.assetCode || index
  return `repair-${String(identity)}-${index}`
}

const normalizeSourceItem = (item: RepairItemPayload, index: number) => ({
  ...item,
  rowKey: String(item.rowKey || buildRepairItemRowKey(item, index))
})

export const stripRepairItemRowKey = (item: RepairItemPayload) => {
  const payloadItem: Record<string, any> = { ...item }
  delete payloadItem.rowKey
  return payloadItem
}

export const serializeRepairItems = (items: RepairItemPayload[] = []) =>
  items.map((item) => stripRepairItemRowKey(item))

export const resolveRepairItems = (
  repair?: Record<string, any>,
  options: ResolveRepairItemsOptions = {}
) => {
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
