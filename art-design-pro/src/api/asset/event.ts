import http from '@/utils/http'

/**
 * 查询资产流水列表
 */
export function listAssetEvent(query?: any) {
  return http.request({ url: '/asset/event/list', method: 'get', params: query })
}

/**
 * 查询资产流水详情
 */
export function getAssetEvent(eventId: number | string) {
  return http.request({ url: '/asset/event/' + eventId, method: 'get' })
}

/**
 * 查询某项资产最近的流水记录
 */
export function getAssetEventByAssetId(assetId: number | string, query?: any) {
  return http.request({ url: '/asset/event/asset/' + assetId, method: 'get', params: query })
}

/**
 * 导出资产流水
 */
export function exportAssetEvent(query?: any) {
  return http.request({
    url: '/asset/event/export',
    method: 'post',
    params: query,
    responseType: 'blob'
  })
}
