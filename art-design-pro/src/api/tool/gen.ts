import http from '@/utils/http'

/**
 * 查询代码生成业务表列表
 */
export function listGenTable(query?: any) {
  return http.request({ url: '/tool/gen/list', method: 'get', params: query })
}

/**
 * 查询数据库中的可导入表列表
 */
export function listGenDbTable(query?: any) {
  return http.request({ url: '/tool/gen/db/list', method: 'get', params: query })
}
