export interface OrderListRestoreState {
  orderType: string
  orderNo: string
  orderStatus: string
  applyUserName: string
  applyDeptName: string
  bizDateRange: string[]
}

const DEFAULT_ORDER_TYPE = 'ALL'

const toStringValue = (value: unknown) => {
  if (value === null || value === undefined) return ''
  return String(value).trim()
}

export const resolveOrderListRestoreState = (
  query: Record<string, any> = {}
): OrderListRestoreState => {
  const bizDateStart = toStringValue(query.bizDateStart)
  const bizDateEnd = toStringValue(query.bizDateEnd)

  return {
    orderType: toStringValue(query.orderType).toUpperCase() || DEFAULT_ORDER_TYPE,
    orderNo: toStringValue(query.orderNo),
    orderStatus: toStringValue(query.orderStatus),
    applyUserName: toStringValue(query.applyUserName),
    applyDeptName: toStringValue(query.applyDeptName),
    bizDateRange: [bizDateStart, bizDateEnd].filter(Boolean)
  }
}

export const buildOrderListRestoreQuery = (state: Partial<OrderListRestoreState> = {}) => {
  const orderType = toStringValue(state.orderType).toUpperCase()
  const bizDateRange = Array.isArray(state.bizDateRange) ? state.bizDateRange : []
  const [bizDateStart = '', bizDateEnd = ''] = bizDateRange
  const query: Record<string, string> = {}

  if (state.orderNo?.trim()) query.orderNo = state.orderNo.trim()
  if (state.orderStatus?.trim()) query.orderStatus = state.orderStatus.trim()
  if (state.applyUserName?.trim()) query.applyUserName = state.applyUserName.trim()
  if (state.applyDeptName?.trim()) query.applyDeptName = state.applyDeptName.trim()
  if (bizDateStart) query.bizDateStart = bizDateStart
  if (bizDateEnd) query.bizDateEnd = bizDateEnd
  if (orderType && orderType !== DEFAULT_ORDER_TYPE) query.orderType = orderType

  return query
}
