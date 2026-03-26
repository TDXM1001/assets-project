import http from '@/utils/http'

/**
 * 查询业务单据列表
 */
export function listAssetOrder(query?: any) {
  return http.request({ url: '/asset/order/list', method: 'get', params: query })
}

/**
 * 查询业务单据详情
 */
export function getAssetOrder(orderId: number | string) {
  return http.request({ url: '/asset/order/' + orderId, method: 'get' })
}

export function getLinkedAssetOrder(query: {
  orderType: string
  sourceBizType: string
  sourceBizId: number | string
}) {
  return http.request({ url: '/asset/order/linked', method: 'get', params: query })
}

/**
 * 新增业务单据
 */
export function addAssetOrder(data: any) {
  return http.request({ url: '/asset/order', method: 'post', data })
}

/**
 * 修改业务单据
 */
export function updateAssetOrder(data: any) {
  return http.request({ url: '/asset/order', method: 'put', data })
}

/**
 * 删除业务单据
 */
export function delAssetOrder(orderId: number | string) {
  return http.request({ url: '/asset/order/' + orderId, method: 'delete' })
}

/**
 * 提交业务单据
 */
export function submitAssetOrder(orderId: number | string) {
  return http.request({ url: '/asset/order/submit/' + orderId, method: 'post' })
}

/**
 * 审批通过业务单据
 */
export function approveAssetOrder(orderId: number | string, data?: any) {
  return http.request({ url: '/asset/order/approve/' + orderId, method: 'post', data })
}

/**
 * 审批驳回业务单据
 */
export function rejectAssetOrder(orderId: number | string, data?: any) {
  return http.request({ url: '/asset/order/reject/' + orderId, method: 'post', data })
}

/**
 * 完成业务单据
 */
export function finishAssetOrder(orderId: number | string) {
  return http.request({ url: '/asset/order/finish/' + orderId, method: 'post' })
}

/**
 * 取消业务单据
 */
export function cancelAssetOrder(orderId: number | string) {
  return http.request({ url: '/asset/order/cancel/' + orderId, method: 'post' })
}

/**
 * 导出业务单据
 */
export function exportAssetOrder(query?: any) {
  return http.request({
    url: '/asset/order/export',
    method: 'post',
    params: query,
    responseType: 'blob'
  })
}
