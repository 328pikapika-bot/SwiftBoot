import request from '@/utils/request'

// 查询会话列表
export function listAiSession(query: any) {
  return request({
    url: '/monitor/ai-session/list',
    method: 'get',
    params: query
  })
}

// 删除会话记录
export function delAiSession(ids: string) {
  return request({
    url: '/monitor/ai-session/' + ids,
    method: 'delete'
  })
}

// 清空会话记录
export function cleanAiSession() {
  return request({
    url: '/monitor/ai-session/clean',
    method: 'delete'
  })
}

// 获取仪表盘统计数据
export function getDashboardStats() {
  return request({
    url: '/monitor/ai-session/stats',
    method: 'get'
  })
}
