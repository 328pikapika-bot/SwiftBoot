import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login, logout, getUserInfo } from '@/api/auth'
import type { LoginData, UserInfo } from '@/api/auth'
import router from '@/router'

const tokenStorage = typeof window !== 'undefined' ? window.sessionStorage : null

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(tokenStorage?.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)

  async function loginAction(loginData: LoginData) {
    const res = await login(loginData)
    token.value = res.data.token
    tokenStorage?.setItem('token', res.data.token)
  }

  async function getUserInfoAction() {
    const res = await getUserInfo()
    userInfo.value = res.data
    return res
  }

  async function logoutAction() {
    try {
      await logout()
    } finally {
      token.value = ''
      userInfo.value = null
      tokenStorage?.removeItem('token')
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
