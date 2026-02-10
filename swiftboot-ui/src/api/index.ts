import request from '@/utils/request'

// 获取首页统计信息
export function getIndexStats() {
  return request({
    url: '/index/stats',
    method: 'get'
  })
}
