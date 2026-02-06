import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login, logout, getUserInfo } from '@/api/auth'
import type { LoginData, UserInfo } from '@/api/auth'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)

  // 登录
  async function loginAction(loginData: LoginData) {
    const res = await login(loginData)
    token.value = res.data.token
    localStorage.setItem('token', res.data.token)
  }

  // 获取用户信息
  async function getUserInfoAction() {
    const res = await getUserInfo()
    userInfo.value = res.data
    return res // 返回响应以便外部使用
  }

  // 登出
  async function logoutAction() {
    try {
      await logout()
    } finally {
      token.value = ''
      userInfo.value = null
      localStorage.removeItem('token')
      router.push('/login')
    }
  }

  return {
    token,
    userInfo,
    login: loginAction,
    getUserInfo: getUserInfoAction,
    logout: logoutAction
  }
})
