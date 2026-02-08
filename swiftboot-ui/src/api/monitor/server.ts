import request from '@/utils/request'

// 获取服务器监控信息
export function getServer() {
  return request({
    url: '/monitor/server',
    method: 'get'
  })
}

// 获取历史监控数据
export function getServerHistory(query: any) {
  return request({
    url: '/monitor/server/history',
    method: 'get',
    params: query
  })
}
