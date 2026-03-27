import http from '@/utils/http'
import type { AssetCategoryFieldTemplate } from './types'

/**
 * 查询资产分类列表
 */
export function listCategory(query?: any) {
  return http.request({ url: '/asset/category/list', method: 'get', params: query })
}

/**
 * 查询资产分类树
 */
export function treeCategorySelect(query?: any) {
  return http.request({ url: '/asset/category/treeSelect', method: 'get', params: query })
}

/**
 * 查询资产分类详情
 */
export function getCategory(categoryId: number | string) {
  return http.request({ url: '/asset/category/' + categoryId, method: 'get' })
}

/**
 * 新增资产分类
 */
export function addCategory(data: any) {
  return http.request({ url: '/asset/category', method: 'post', data })
}

/**
 * 修改资产分类
 */
export function updateCategory(data: any) {
  return http.request({ url: '/asset/category', method: 'put', data })
}

/**
 * 删除资产分类
 */
export function delCategory(categoryId: number | string) {
  return http.request({ url: '/asset/category/' + categoryId, method: 'delete' })
}

/**
 * 查询分类字段模板
 */
export function getCategoryFieldTemplate(categoryId: number | string, templateVersion?: number) {
  return http.request<AssetCategoryFieldTemplate>({
    url: '/asset/category/' + categoryId + '/fieldTemplate',
    method: 'get',
    params: templateVersion ? { templateVersion } : undefined
  })
}

/**
 * 修改分类字段模板
 */
export function updateCategoryFieldTemplate(
  categoryId: number | string,
  data: AssetCategoryFieldTemplate
) {
  return http.request({
    url: '/asset/category/' + categoryId + '/fieldTemplate',
    method: 'put',
    data
  })
}
