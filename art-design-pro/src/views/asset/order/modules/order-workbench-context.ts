export interface OrderWorkbenchContext extends Record<string, any> {
  orderType: string
  bridgeSource: string
  bridgeKey: string
  sourceBizType: string
  sourceBizId: string
  repairId: string
  // 标识桥接数据是否真实存在，供创建页做降级处理。
  bridgePayloadFound?: boolean
}

export interface ResolveOrderWorkbenchPageContextInput {
  orderTypeQuery?: string
  bridgeSourceQuery?: string
  bridgeKeyQuery?: string
  bridgeDataQuery?: string
  sourceBizTypeQuery?: string
  sourceBizIdQuery?: string
  repairIdQuery?: string
}

const DEFAULT_ORDER_TYPE = 'INBOUND'

// 单据工作台会从 sessionStorage 里短暂接收桥接数据，避免直接依赖来源页状态。
const STORAGE_KEYS = [
  'asset-order-disposal-bridge',
  'asset.order.disposal.bridge',
  'asset.order.bridge.payload'
]

const STORAGE_KEYS_WITH_BRIDGE = (bridgeKey: string) => [
  `asset-order-disposal-bridge:${bridgeKey}`,
  `asset.order.disposal.bridge:${bridgeKey}`,
  ...STORAGE_KEYS
]

export const safeParseOrderWorkbenchContext = (value: string | null) => {
  if (!value) {
    return undefined
  }

  try {
    const parsed = JSON.parse(value)
    // 只接受对象，避免把字符串、数组或空值误当成可用上下文。
    return parsed && typeof parsed === 'object' ? parsed : undefined
  } catch (error) {
    console.error('解析单据工作台上下文失败:', error)
    return undefined
  }
}

export const normalizeOrderWorkbenchContext = (
  context: Record<string, any> = {}
): OrderWorkbenchContext => {
  const orderType = String(context.orderType || '').toUpperCase()
  // 把不完整的路由参数补齐成工作台可用结构，后续页面只认这一份标准上下文。
  return {
    ...context,
    orderType: orderType || DEFAULT_ORDER_TYPE,
    bridgeSource: String(context.bridgeSource || ''),
    bridgeKey: String(context.bridgeKey || ''),
    sourceBizType: String(context.sourceBizType || ''),
    sourceBizId:
      context.sourceBizId === undefined || context.sourceBizId === null
        ? ''
        : String(context.sourceBizId),
    repairId: String(context.repairId || '')
  }
}

export const buildOrderWorkbenchDraftScope = (context: Record<string, any> = {}) => {
  const orderType = String(context.orderType || DEFAULT_ORDER_TYPE).toUpperCase()
  const bridgeSource = String(context.bridgeSource || 'manual')
  const sourceBizType = String(context.sourceBizType || '')
  const sourceBizId =
    context.sourceBizId === undefined || context.sourceBizId === null
      ? ''
      : String(context.sourceBizId)
  const repairId = String(context.repairId || '')

  // 草稿必须按来源和单据类型一起隔离，否则不同入口会互相覆盖。
  return [orderType, bridgeSource, sourceBizType, sourceBizId, repairId].join('|')
}

export const buildOrderWorkbenchDraftStorageKey = (context: Record<string, any> = {}) =>
  `asset-order-create-draft:${buildOrderWorkbenchDraftScope(context)}`

export const readOrderWorkbenchContextFromStorage = (bridgeKey?: string) => {
  const storageKeys = bridgeKey ? STORAGE_KEYS_WITH_BRIDGE(bridgeKey) : STORAGE_KEYS

  for (const storageKey of storageKeys) {
    const storedValue = sessionStorage.getItem(storageKey)
    if (!storedValue) {
      continue
    }

    // 桥接数据只消费一次，避免页面刷新后重复使用旧上下文。
    sessionStorage.removeItem(storageKey)
    const parsed = safeParseOrderWorkbenchContext(storedValue)
    if (parsed) {
      return parsed
    }
  }

  return undefined
}

export const resolveOrderWorkbenchPageContext = (
  input: ResolveOrderWorkbenchPageContextInput = {}
): OrderWorkbenchContext => {
  const parsedBridgeContext =
    safeParseOrderWorkbenchContext(input.bridgeDataQuery || null) ||
    readOrderWorkbenchContextFromStorage(input.bridgeKeyQuery)
  const bridgePayloadFound = Boolean(parsedBridgeContext)
  const mergedContext: Record<string, any> = {
    ...(parsedBridgeContext || {})
  }

  if (input.orderTypeQuery) mergedContext.orderType = input.orderTypeQuery
  if (input.bridgeSourceQuery) mergedContext.bridgeSource = input.bridgeSourceQuery
  if (input.bridgeKeyQuery) mergedContext.bridgeKey = input.bridgeKeyQuery
  if (input.sourceBizTypeQuery) mergedContext.sourceBizType = input.sourceBizTypeQuery
  if (
    input.sourceBizIdQuery !== undefined &&
    input.sourceBizIdQuery !== null &&
    input.sourceBizIdQuery !== ''
  ) {
    mergedContext.sourceBizId = input.sourceBizIdQuery
  }
  if (input.repairIdQuery) mergedContext.repairId = input.repairIdQuery

  return {
    ...normalizeOrderWorkbenchContext(mergedContext),
    bridgePayloadFound
  }
}
