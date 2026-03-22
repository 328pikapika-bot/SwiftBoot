import request, { ApiResponse, PageResult } from '@/utils/request'

export interface SysFile {
  id: number
  fileName: string
  originalName: string
  fileSuffix?: string
  filePath?: string
  fileSize: number
  storageType: string
  storageBucket?: string
  mimeType?: string
  visibility?: string
  bizType?: string
  bizId?: number
  url?: string
  createTime?: string
}

export interface FileQuery {
  pageNum?: number
  pageSize?: number
  keyword?: string
  bizType?: string
  bizId?: number
  storageType?: string
}

export function listFiles(params: FileQuery): Promise<ApiResponse<PageResult<SysFile>>> {
  return request({
    url: '/system/file/list',
    method: 'get',
    params
  })
}

export function uploadFile(
  file: File,
  extra?: { bizType?: string; bizId?: number; visibility?: string }
): Promise<ApiResponse<SysFile>> {
  const formData = new FormData()
  formData.append('file', file)
  if (extra?.bizType) formData.append('bizType', extra.bizType)
  if (extra?.bizId !== undefined && extra?.bizId !== null) formData.append('bizId', String(extra.bizId))
  if (extra?.visibility) formData.append('visibility', extra.visibility)

  return request({
    url: '/system/file/upload',
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data: formData
  })
}

export function deleteFile(fileId: number): Promise<ApiResponse<void>> {
  return request({
    url: `/system/file/${fileId}`,
    method: 'delete'
  })
}

export function renameFile(fileId: number, newName: string): Promise<ApiResponse<void>> {
  return request({
    url: `/system/file/${fileId}/rename`,
    method: 'put',
    data: { newName }
  })
}

export function getPreviewUrl(fileId: number): Promise<ApiResponse<string>> {
  return request({
    url: `/system/file/${fileId}/preview-url`,
    method: 'get'
  })
}

export function getDownloadUrl(fileId: number): Promise<ApiResponse<string>> {
  return request({
    url: `/system/file/${fileId}/download-url`,
    method: 'get'
  })
}
