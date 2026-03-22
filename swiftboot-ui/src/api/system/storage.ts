import request, { ApiResponse } from '@/utils/request'

export interface StorageConfig {
  activeType: 'local' | 'minio' | 'oss' | 'cos'
  accessUrlExpireSeconds: number
  local: {
    basePath: string
    publicRead: boolean
  }
  minio: {
    endpoint: string
    accessKey: string
    secretKey: string
    bucket: string
    domain: string
    publicRead: boolean
  }
  oss: {
    endpoint: string
    accessKeyId: string
    accessKeySecret: string
    bucket: string
    domain: string
    publicRead: boolean
  }
  cos: {
    region: string
    secretId: string
    secretKey: string
    bucket: string
    domain: string
    publicRead: boolean
  }
}

export function getStorageConfig(): Promise<ApiResponse<StorageConfig>> {
  return request({
    url: '/system/storage/config',
    method: 'get'
  })
}

export function updateStorageConfig(data: StorageConfig): Promise<ApiResponse<void>> {
  return request({
    url: '/system/storage/config',
    method: 'put',
    data
  })
}
