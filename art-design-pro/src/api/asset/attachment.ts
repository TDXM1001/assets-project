import http from '@/utils/http'

/**
 * 查询资产附件列表
 */
export function listAssetAttachment(query?: any) {
  return http.request({ url: '/asset/attachment/list', method: 'get', params: query })
}

/**
 * 上传资产附件
 */
export function uploadAssetAttachment(bizType: string, bizId: number | string, file: File) {
  const formData = new FormData()
  formData.append('file', file)

  return http.request({
    url: `/asset/attachment/upload/${bizType}/${bizId}`,
    method: 'post',
    data: formData
  })
}

/**
 * 删除资产附件
 */
export function delAssetAttachment(attachmentIds: number | string | Array<number | string>) {
  const ids = Array.isArray(attachmentIds) ? attachmentIds.join(',') : attachmentIds
  return http.request({ url: '/asset/attachment/' + ids, method: 'delete' })
}
