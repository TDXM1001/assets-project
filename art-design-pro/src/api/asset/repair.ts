import http from '@/utils/http'

/**
 * 查询维修单列表
 */
export function listAssetRepair(query?: any) {
  return http.request({ url: '/asset/repair/list', method: 'get', params: query })
}

/**
 * 查询维修单详情
 */
export function getAssetRepair(repairId: number | string) {
  return http.request({ url: `/asset/repair/${repairId}`, method: 'get' })
}

/**
 * 新增维修单
 */
export function addAssetRepair(data: any) {
  return http.request({ url: '/asset/repair', method: 'post', data })
}

/**
 * 修改维修单
 */
export function updateAssetRepair(data: any) {
  return http.request({ url: '/asset/repair', method: 'put', data })
}

/**
 * 删除维修单
 */
export function delAssetRepair(repairId: number | string) {
  return http.request({ url: `/asset/repair/${repairId}`, method: 'delete' })
}

/**
 * 提交维修单
 */
export function submitAssetRepair(repairId: number | string) {
  return http.request({ url: `/asset/repair/submit/${repairId}`, method: 'post' })
}

/**
 * 审批通过维修单
 */
export function approveAssetRepair(repairId: number | string, data?: any) {
  return http.request({ url: `/asset/repair/approve/${repairId}`, method: 'post', data })
}

/**
 * 驳回维修单
 */
export function rejectAssetRepair(repairId: number | string, data?: any) {
  return http.request({ url: `/asset/repair/reject/${repairId}`, method: 'post', data })
}

/**
 * 完成维修单
 */
export function finishAssetRepair(repairId: number | string, data?: any) {
  return http.request({ url: `/asset/repair/finish/${repairId}`, method: 'post', data })
}

/**
 * 取消维修单
 */
export function cancelAssetRepair(repairId: number | string) {
  return http.request({ url: `/asset/repair/cancel/${repairId}`, method: 'post' })
}

/**
 * 导出维修单
 */
export function exportAssetRepair(query?: any) {
  return http.request({
    url: '/asset/repair/export',
    method: 'post',
    params: query,
    responseType: 'blob'
  })
}
