import request, { ApiResponse, PageResult } from '@/utils/request'

export interface User {
  id?: number
  deptId?: number
  username?: string
  password?: string
  nickname?: string
  email?: string
  phone?: string
  gender?: number
  avatar?: string
  status?: number
  remark?: string
  deptName?: string
  roleIds?: number[]
}

export interface UserQuery {
  pageNum?: number
  pageSize?: number
  username?: string
  nickname?: string
  phone?: string
  status?: number
  deptId?: number
}

/**
 * 查询用户列表
 */
export function listUser(query: UserQuery): Promise<ApiResponse<PageResult<User>>> {
  return request({
    url: '/system/user/list',
    method: 'get',
    params: query
  })
}

/**
 * 查询用户详情
 */
export function getUser(userId: number): Promise<ApiResponse<User>> {
  return request({
    url: '/system/user/' + userId,
    method: 'get'
  })
}

/**
 * 新增用户
 */
export function addUser(data: User): Promise<ApiResponse<void>> {
  return request({
    url: '/system/user',
    method: 'post',
    data
  })
}

/**
 * 修改用户
 */
export function updateUser(data: User): Promise<ApiResponse<void>> {
  return request({
    url: '/system/user',
    method: 'put',
    data
  })
}

/**
 * 删除用户
 */
export function deleteUser(userIds: string): Promise<ApiResponse<void>> {
  return request({
    url: '/system/user/' + userIds,
    method: 'delete'
  })
}

/**
 * 重置密码
 */
export function resetPassword(data: { id: number; password: string }): Promise<ApiResponse<void>> {
  return request({
    url: '/system/user/resetPwd',
    method: 'put',
    data
  })
}

/**
 * 修改状态
 */
export function changeStatus(data: { id: number; status: number }): Promise<ApiResponse<void>> {
  return request({
    url: '/system/user/changeStatus',
    method: 'put',
    data
  })
}
