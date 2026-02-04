import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'

// 创建 axios 实例
const service: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 60000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers['Authorization'] = userStore.token
    }
    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    
    // 成功
    if (res.code === 200) {
      return res
    }
    
    // 未登录
    if (res.code === 401) {
      ElMessageBox.confirm('登录已过期，请重新登录', '提示', {
        confirmButtonText: '重新登录',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        const userStore = useUserStore()
        userStore.logout()
      })
      return Promise.reject(new Error(res.msg || '登录已过期'))
    }
    
    // 其他错误
    ElMessage.error(res.msg || '请求失败')
    return Promise.reject(new Error(res.msg || '请求失败'))
  },
  (error) => {
    console.error('响应错误:', error)
    let message = error.message
    if (error.response?.status === 401) {
      message = '登录已过期'
    } else if (error.response?.status === 403) {
      message = '没有权限'
    } else if (error.response?.status === 404) {
      message = '请求资源不存在'
    } else if (error.response?.status === 500) {
      message = '服务器错误'
    }
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export interface ApiResponse<T = any> {
  code: number
  msg: string
  data: T
  timestamp: number
}

export interface PageResult<T = any> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
  pages: number
}

export default service
