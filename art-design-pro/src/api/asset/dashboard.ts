import http from '@/utils/http'

export interface AssetDashboardSummaryResponse {
  assetTotal?: number
  orderTotal?: number
  inventoryTaskTotal?: number
  recentEventTotal?: number
  recentEventDays?: number
  statusList?: Array<{
    status?: string
    label?: string
    totalCount?: number
  }>
}

export interface AssetDashboardTodoResponseItem {
  key?: string
  label?: string
  count?: number
  permitted?: boolean
  routePath?: string
}

export interface AssetDashboardTodoResponse {
  itemList?: AssetDashboardTodoResponseItem[]
}

export interface AssetDashboardTrendResponseItem {
  statDate?: string
  eventCount?: number
  orderCount?: number
  inventoryCount?: number
  diffCount?: number
}

export interface AssetDashboardTrendResponse {
  days?: number
  pointList?: AssetDashboardTrendResponseItem[]
}

/**
 * 查询资产看板汇总数据。
 * 这里统一关闭默认错误提示，由页面自己决定如何优雅降级。
 */
export function getAssetDashboardSummary() {
  return http.request<AssetDashboardSummaryResponse>({
    url: '/asset/dashboard/summary',
    method: 'get',
    showErrorMessage: false
  })
}

/**
 * 查询资产看板待办数据。
 * 待办区允许单独降级，因此不在全局弹错误提示。
 */
export function getAssetDashboardTodo() {
  return http.request<AssetDashboardTodoResponse>({
    url: '/asset/dashboard/todo',
    method: 'get',
    showErrorMessage: false
  })
}

/**
 * 查询资产看板趋势数据。
 * 趋势接口为空或无权限时，页面直接展示空态即可。
 */
export function getAssetDashboardTrend() {
  return http.request<AssetDashboardTrendResponse>({
    url: '/asset/dashboard/trend',
    method: 'get',
    showErrorMessage: false
  })
}
