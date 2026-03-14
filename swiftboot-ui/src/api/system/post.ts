import request, { ApiResponse } from '@/utils/request'

export interface SysPost {
  postId?: number
  postCode?: string
  postName?: string
  postSort?: number
  status?: string
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface PostQuery {
  pageNum?: number
  pageSize?: number
  postCode?: string
  postName?: string
  status?: string
}

/**
 * 获取岗位列表
 */
export function listPost(query?: PostQuery): Promise<ApiResponse<SysPost[]>> {
  return request({
    url: '/system/post/list',
    method: 'get',
    params: query
  })
}

/**
 * 获取岗位详情
 */
export function getPost(postId: number): Promise<ApiResponse<SysPost>> {
  return request({
    url: '/system/post/' + postId,
    method: 'get'
  })
}

/**
 * 获取所有启用的岗位
 */
export function listAllPost(): Promise<ApiResponse<SysPost[]>> {
  return request({
    url: '/system/post/all',
    method: 'get'
  })
}

/**
 * 新增岗位
 */
export function addPost(data: SysPost): Promise<ApiResponse<void>> {
  return request({
    url: '/system/post',
    method: 'post',
    data
  })
}

/**
 * 修改岗位
 */
export function updatePost(data: SysPost): Promise<ApiResponse<void>> {
  return request({
    url: '/system/post',
    method: 'put',
    data
  })
}

/**
 * 删除岗位
 */
export function deletePost(postIds: string): Promise<ApiResponse<void>> {
  return request({
    url: '/system/post/' + postIds,
    method: 'delete'
  })
}
