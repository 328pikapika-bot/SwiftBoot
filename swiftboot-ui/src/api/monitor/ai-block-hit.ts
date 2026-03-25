import request from '@/utils/request'

export interface AiBlockHitLog {
  id: number
  userId?: number
  username?: string
  nickname?: string
  categoryId?: number
  categoryName?: string
  wordText?: string
  questionContent?: string
  loginIp?: string
  createTime?: string
}

export interface AiBlockHitStats {
  totalCount: number
  todayCount: number
  weekCount: number
  topCategory: string
  topWord: string
  latestHitAt?: string
  trend: Array<{ date: string; count: number }>
}

export function getAiBlockHitStats() {
  return request({
    url: '/monitor/ai-block-hit/stats',
    method: 'get'
  })
}

export function listAiBlockHitLogs(params: any) {
  return request({
    url: '/monitor/ai-block-hit/list',
    method: 'get',
    params
  })
}
