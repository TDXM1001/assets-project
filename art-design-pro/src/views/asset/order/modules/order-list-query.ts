export interface OrderListRestoreState {
  orderType: string
  orderNo: string
  orderStatus: string
  applyUserName: string
  applyDeptName: string
  bizDateRange: string[]
  pageNum: number
  pageSize: number
}

const DEFAULT_ORDER_TYPE = 'ALL'
const DEFAULT_PAGE_NUM = 1
const DEFAULT_PAGE_SIZE = 10

const toStringValue = (value: unknown) => {
  if (value === null || value === undefined) return ''
  return String(value).trim()
}

const toNumberValue = (value: unknown, fallback: number) => {
  if (value === null || value === undefined || value === '') return fallback
  const parsed = Number(value)
  return Number.isFinite(parsed) && parsed > 0 ? parsed : fallback
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
    bizDateRange: [bizDateStart, bizDateEnd].filter(Boolean),
    pageNum: toNumberValue(query.pageNum, DEFAULT_PAGE_NUM),
    pageSize: toNumberValue(query.pageSize, DEFAULT_PAGE_SIZE)
  }
}

export const buildOrderListRestoreQuery = (state: Partial<OrderListRestoreState> = {}) => {
  const orderType = toStringValue(state.orderType).toUpperCase()
  const bizDateRange = Array.isArray(state.bizDateRange) ? state.bizDateRange : []
  const [bizDateStart = '', bizDateEnd = ''] = bizDateRange
  const pageNum = toNumberValue(state.pageNum, DEFAULT_PAGE_NUM)
  const pageSize = toNumberValue(state.pageSize, DEFAULT_PAGE_SIZE)
  const query: Record<string, string> = {}

  if (state.orderNo?.trim()) query.orderNo = state.orderNo.trim()
  if (state.orderStatus?.trim()) query.orderStatus = state.orderStatus.trim()
  if (state.applyUserName?.trim()) query.applyUserName = state.applyUserName.trim()
  if (state.applyDeptName?.trim()) query.applyDeptName = state.applyDeptName.trim()
  if (bizDateStart) query.bizDateStart = bizDateStart
  if (bizDateEnd) query.bizDateEnd = bizDateEnd
  if (pageNum && pageNum !== DEFAULT_PAGE_NUM) query.pageNum = String(pageNum)
  if (pageSize && pageSize !== DEFAULT_PAGE_SIZE) query.pageSize = String(pageSize)
  if (orderType && orderType !== DEFAULT_ORDER_TYPE) query.orderType = orderType

  return query
}
