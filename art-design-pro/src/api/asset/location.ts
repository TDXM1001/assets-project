import http from '@/utils/http'

/**
 * 查询资产位置列表
 */
export function listLocation(query?: any) {
  return http.request({ url: '/asset/location/list', method: 'get', params: query })
}

/**
 * 查询资产位置树
 */
export function treeLocationSelect(query?: any) {
  return http.request({ url: '/asset/location/treeSelect', method: 'get', params: query })
}

/**
 * 查询资产位置详情
 */
export function getLocation(locationId: number | string) {
  return http.request({ url: '/asset/location/' + locationId, method: 'get' })
}

/**
 * 新增资产位置
 */
export function addLocation(data: any) {
  return http.request({ url: '/asset/location', method: 'post', data })
}

/**
 * 修改资产位置
 */
export function updateLocation(data: any) {
  return http.request({ url: '/asset/location', method: 'put', data })
}

/**
 * 删除资产位置
 */
export function delLocation(locationId: number | string) {
  return http.request({ url: '/asset/location/' + locationId, method: 'delete' })
}
