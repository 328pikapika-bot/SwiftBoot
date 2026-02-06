import request from '@/utils/request'

// 获取服务器监控信息
export function getServer() {
  return request({
    url: '/monitor/server',
    method: 'get'
  })
}
