import { buildRepairHeaderSnapshot, serializeRepairItems } from './repair-item-normalize'

export interface RepairDraftItemPayload extends Record<string, any> {
  rowKey?: string
  repairItemId?: number | string
  assetId?: number | string
  assetCode?: string
}

const buildDraftRowKey = (item: RepairDraftItemPayload, index: number) => {
  const identity = item.rowKey || item.repairItemId || item.assetId || item.assetCode || index
  return `draft-${String(identity)}-${index}`
}

export const normalizeRepairDraftItems = (items: RepairDraftItemPayload[] = []) =>
  items.map((item, index) => ({
    ...item,
    rowKey: String(item.rowKey || buildDraftRowKey(item, index))
  }))

export const resolveRepairDraftItems = (payload: Record<string, any> = {}) => {
  const sourceItems = Array.isArray(payload.repairItems)
    ? payload.repairItems
    : Array.isArray(payload.itemList)
      ? payload.itemList
      : []

  return normalizeRepairDraftItems(sourceItems)
}

export const normalizeRepairDraftPayload = (
  payload: Record<string, any> = {}
): Record<string, any> & { repairItems: RepairDraftItemPayload[] } => ({
  ...payload,
  repairItems: resolveRepairDraftItems(payload)
})

export const resolveRepairDraftState = (payload: Record<string, any> = {}) => {
  const normalizedPayload = normalizeRepairDraftPayload(payload)
  return {
    ...normalizedPayload,
    ...buildRepairHeaderStateFromItems(normalizedPayload.repairItems, normalizedPayload.faultDesc)
  }
}

// 统一单头快照回填：由第一条明细反推旧链路仍依赖的单头字段。
export const buildRepairHeaderStateFromItems = (
  repairItems: RepairDraftItemPayload[] = [],
  fallbackFaultDesc?: unknown
) => buildRepairHeaderSnapshot(repairItems[0], { faultDesc: fallbackFaultDesc })

// 统一保存 payload：避免 create/edit/dialog 各自重复拼装单头 + 明细字段。
export const buildRepairSubmitPayload = <T extends Record<string, any>>(formData: T) => {
  const itemList = serializeRepairItems(
    Array.isArray(formData.repairItems) ? formData.repairItems : []
  )
  const headerSnapshot = buildRepairHeaderStateFromItems(itemList, formData.faultDesc)

  return {
    ...formData,
    ...headerSnapshot,
    itemList
  }
}

// 统一草稿缓存结构：草稿回写优先读 repairItems，同时兼容 itemList。
export const buildRepairDraftCachePayload = <T extends Record<string, any>>(formData: T) => {
  const payload = buildRepairSubmitPayload(formData)
  return {
    ...payload,
    repairItems: Array.isArray(payload.itemList) ? [...payload.itemList] : []
  }
}
