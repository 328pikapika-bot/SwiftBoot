import request, { ApiResponse } from '@/utils/request'

export interface UploadResult {
  fileName: string
  originalName: string
  filePath: string
  fileSize: number
}

/**
 * 上传文件
 */
export function uploadFile(file: File): Promise<ApiResponse<UploadResult>> {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/system/file/upload',
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data: formData
  })
}

/**
 * 删除文件
 */
export function deleteFile(fileName: string): Promise<ApiResponse<void>> {
  return request({
    url: '/system/file/' + fileName,
    method: 'delete'
  })
}
