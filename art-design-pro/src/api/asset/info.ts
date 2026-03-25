import http from '@/utils/http'

/**
 * 查询资产台账列表
 */
export function listAssetInfo(query?: any) {
  return http.request({ url: '/asset/info/list', method: 'get', params: query })
}

/**
 * 查询资产台账详情
 */
export function getAssetInfo(assetId: number | string) {
  return http.request({ url: '/asset/info/' + assetId, method: 'get' })
}

/**
 * 新增资产台账
 */
export function addAssetInfo(data: any) {
  return http.request({ url: '/asset/info', method: 'post', data })
}

/**
 * 修改资产台账
 */
export function updateAssetInfo(data: any) {
  return http.request({ url: '/asset/info', method: 'put', data })
}

/**
 * 删除资产台账
 */
export function delAssetInfo(assetId: number | string) {
  return http.request({ url: '/asset/info/' + assetId, method: 'delete' })
}

/**
 * 导出资产台账
 */
export function exportAssetInfo(query?: any) {
  return http.request({
    url: '/asset/info/export',
    method: 'post',
    params: query,
    responseType: 'blob'
  })
}

/**
 * 导入资产台账
 */
export function importAssetInfoFile(formData: FormData) {
  return http.request({
    url: '/asset/info/importData',
    method: 'post',
    data: formData
  })
}

/**
 * 下载资产台账导入模板
 */
export function downloadAssetInfoTemplate() {
  return http.request({
    url: '/asset/info/importTemplate',
    method: 'post',
    responseType: 'blob'
  })
}
