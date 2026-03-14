import request, { ApiResponse } from '@/utils/request'

export interface SysNotice {
  noticeId?: number
  noticeTitle?: string
  noticeType?: number
  noticeContent?: string
  status?: number
  createBy?: string
  createTime?: string
  updateBy?: string
  updateTime?: string
  remark?: string
}

export interface NoticeQuery {
  pageNum?: number
  pageSize?: number
  noticeTitle?: string
  noticeType?: number
  status?: number
}

/**
 * 获取公告列表
 */
export function listNotice(query?: NoticeQuery): Promise<ApiResponse<SysNotice[]>> {
  return request({
    url: '/system/notice/list',
    method: 'get',
    params: query
  })
}

/**
 * 获取公告详情
 */
export function getNotice(noticeId: number): Promise<ApiResponse<SysNotice>> {
  return request({
    url: '/system/notice/' + noticeId,
    method: 'get'
  })
}

/**
 * 新增公告
 */
export function addNotice(data: SysNotice): Promise<ApiResponse<void>> {
  return request({
    url: '/system/notice',
    method: 'post',
    data
  })
}

/**
 * 修改公告
 */
export function updateNotice(data: SysNotice): Promise<ApiResponse<void>> {
  return request({
    url: '/system/notice',
    method: 'put',
    data
  })
}

/**
 * 删除公告
 */
export function deleteNotice(noticeIds: string): Promise<ApiResponse<void>> {
  return request({
    url: '/system/notice/' + noticeIds,
    method: 'delete'
  })
}
