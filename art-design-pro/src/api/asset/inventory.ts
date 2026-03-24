import http from '@/utils/http'

/**
 * 查询盘点任务列表
 */
export function listAssetInventory(query?: any) {
  return http.request({ url: '/asset/inventory/list', method: 'get', params: query })
}

/**
 * 查询盘点任务详情
 */
export function getAssetInventory(taskId: number | string) {
  return http.request({ url: '/asset/inventory/' + taskId, method: 'get' })
}

/**
 * 新增盘点任务
 */
export function addAssetInventory(data: any) {
  return http.request({ url: '/asset/inventory', method: 'post', data })
}

/**
 * 修改盘点任务
 */
export function updateAssetInventory(data: any) {
  return http.request({ url: '/asset/inventory', method: 'put', data })
}

/**
 * 删除盘点任务
 */
export function delAssetInventory(taskId: number | string) {
  return http.request({ url: '/asset/inventory/' + taskId, method: 'delete' })
}

/**
 * 开始盘点
 */
export function startAssetInventory(taskId: number | string) {
  return http.request({ url: '/asset/inventory/start/' + taskId, method: 'post' })
}

/**
 * 查询盘点任务明细
 */
export function getAssetInventoryItems(taskId: number | string) {
  return http.request({ url: '/asset/inventory/' + taskId + '/items', method: 'get' })
}

/**
 * 盘点扫码/录入
 */
export function scanAssetInventory(taskId: number | string, data: any) {
  return http.request({ url: '/asset/inventory/' + taskId + '/scan', method: 'post', data })
}

/**
 * 结束盘点
 */
export function finishAssetInventory(taskId: number | string) {
  return http.request({ url: '/asset/inventory/finish/' + taskId, method: 'post' })
}

/**
 * 处理盘点差异
 */
export function processAssetInventoryDiff(taskId: number | string, data: any) {
  return http.request({ url: '/asset/inventory/processDiff/' + taskId, method: 'post', data })
}

/**
 * 导出盘点任务
 */
export function exportAssetInventory(query?: any) {
  return http.request({
    url: '/asset/inventory/export',
    method: 'post',
    params: query,
    responseType: 'blob'
  })
}
