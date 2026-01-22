import request, { ApiResponse } from '@/utils/request'

export interface LoginData {
  username: string
  password: string
  code?: string
  uuid?: string
}

export interface LoginResult {
  token: string
}

export interface UserInfo {
  userId: number
  username: string
  nickname: string
  avatar: string
  roles: string[]
  permissions: string[]
  menus: any[]
}

/**
 * 登录
 */
export function login(data: LoginData): Promise<ApiResponse<LoginResult>> {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

/**
 * 登出
 */
export function logout(): Promise<ApiResponse<void>> {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}

/**
 * 获取用户信息
 */
export function getUserInfo(): Promise<ApiResponse<UserInfo>> {
  return request({
    url: '/auth/info',
    method: 'get'
  })
}
