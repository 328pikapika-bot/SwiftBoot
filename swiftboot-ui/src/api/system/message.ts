import request, { ApiResponse } from '@/utils/request'

export interface SysMessage {
  msgId?: number
  msgTitle?: string
  msgType?: number
  msgContent?: string
  sender?: string
  receiver?: string
  status?: number
  readTime?: string
  createTime?: string
  updateTime?: string
  remark?: string
}

export interface MessageQuery {
  pageNum?: number
  pageSize?: number
  msgTitle?: string
  msgType?: number
  status?: number
  receiver?: string
}

/**
 * 获取消息列表
 */
export function listMessage(query?: MessageQuery): Promise<ApiResponse<SysMessage[]>> {
  return request({
    url: '/system/message/list',
    method: 'get',
    params: query
  })
}

/**
 * 获取消息详情
 */
export function getMessage(msgId: number): Promise<ApiResponse<SysMessage>> {
  return request({
    url: '/system/message/' + msgId,
    method: 'get'
  })
}

/**
 * 发送消息
 */
export function addMessage(data: SysMessage): Promise<ApiResponse<void>> {
  return request({
    url: '/system/message',
    method: 'post',
    data
  })
}

/**
 * 修改消息
 */
export function updateMessage(data: SysMessage): Promise<ApiResponse<void>> {
  return request({
    url: '/system/message',
    method: 'put',
    data
  })
}

/**
 * 删除消息
 */
export function deleteMessage(msgIds: string): Promise<ApiResponse<void>> {
  return request({
    url: '/system/message/' + msgIds,
    method: 'delete'
  })
}
