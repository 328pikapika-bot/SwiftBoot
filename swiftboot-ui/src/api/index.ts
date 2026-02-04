import request from '@/utils/request'

// 获取首页统计信息
export function getIndexStats() {
  return request({
    url: '/index/stats',
    method: 'get'
  })
}

// 发送 AI 对话
export function sendAiChat(content: string) {
  return request({
    url: '/system/ai/chat',
    method: 'post',
    data: { content }
  })
}
