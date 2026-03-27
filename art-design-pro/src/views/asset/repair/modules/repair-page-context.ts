export interface RepairPageContext extends Record<string, any> {
  sourcePage: string
  bridgeSource: string
  bridgeKey: string
  sourceBizType: string
  sourceBizId: string
  assetId: string
  repairId: string
}

const DEFAULT_SOURCE_PAGE = 'manual'

/**
 * 维修页面只认一套标准化上下文，避免不同入口各自拼参导致草稿和返回态串掉。
 */
export const normalizeRepairPageContext = (
  context: Record<string, any> = {}
): RepairPageContext => {
  return {
    ...context,
    sourcePage: String(context.sourcePage || DEFAULT_SOURCE_PAGE),
    bridgeSource: String(context.bridgeSource || ''),
    bridgeKey: String(context.bridgeKey || ''),
    sourceBizType: String(context.sourceBizType || ''),
    sourceBizId:
      context.sourceBizId === undefined || context.sourceBizId === null
        ? ''
        : String(context.sourceBizId),
    assetId: String(context.assetId || ''),
    repairId: String(context.repairId || '')
  }
}

/**
 * 草稿必须按来源和单据身份隔离，否则 create/edit/detail 会互相覆盖本地缓存。
 */
export const buildRepairDraftScope = (context: Record<string, any> = {}) => {
  const pageMode = String(context.pageMode || 'create').toLowerCase()
  const sourcePage = String(context.sourcePage || DEFAULT_SOURCE_PAGE)
  const bridgeSource = String(context.bridgeSource || 'manual')
  const sourceBizType = String(context.sourceBizType || '')
  const sourceBizId =
    context.sourceBizId === undefined || context.sourceBizId === null
      ? ''
      : String(context.sourceBizId)
  const assetId = String(context.assetId || '')
  const repairId = String(context.repairId || '')

  return [pageMode, sourcePage, bridgeSource, sourceBizType, sourceBizId, assetId, repairId].join(
    '|'
  )
}

export const buildRepairDraftStorageKey = (context: Record<string, any> = {}) =>
  `asset-repair-${String(context.pageMode || 'create')}-draft:${buildRepairDraftScope(context)}`

export const safeParseRepairPageContext = (value: string | null) => {
  if (!value) {
    return undefined
  }

  try {
    const parsed = JSON.parse(value)
    return parsed && typeof parsed === 'object' ? parsed : undefined
  } catch (error) {
    console.error('解析维修页面上下文失败:', error)
    return undefined
  }
}

export const readRepairPageContextFromStorage = (
  bridgeKey: string | null
): Record<string, any> | undefined => {
  if (!bridgeKey) return undefined
  const storageVal = window.localStorage.getItem(`asset-repair-bridge:${bridgeKey}`)
  return safeParseRepairPageContext(storageVal)
}
