import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'

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

type ErrorResponse = Partial<ApiResponse> & {
  msg?: string
}

type RequestInstance = AxiosInstance & {
  <T = any>(config: AxiosRequestConfig): Promise<ApiResponse<T>>
  request<T = any>(config: AxiosRequestConfig): Promise<ApiResponse<T>>
  get<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>>
  delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>>
  head<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>>
  options<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>>
  post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>>
  put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>>
  patch<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>>
}

const service = axios.create({
  baseURL: '/api',
  timeout: 60000,
  headers: {
    'Content-Type': 'application/json'
  }
})

service.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = userStore.token
    }
    return config
  },
  (error) => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  (response: AxiosResponse): any => {
    if (response.config.responseType === 'blob') {
      return response.data
    }

    const res = response.data as ApiResponse
    if (res.code === 200) {
      return res
    }

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

    ElMessage.error(res.msg || '请求失败')
    return Promise.reject(new Error(res.msg || '请求失败'))
  },
  async (error) => {
    console.error('Response error:', error)

    if (error.config?.responseType === 'blob' && error.response?.data instanceof Blob) {
      try {
        const text = await error.response.data.text()
        const payload = JSON.parse(text) as ErrorResponse
        const message = payload?.msg || '文件下载失败'
        ElMessage.error(message)
        return Promise.reject(new Error(message))
      } catch {
        ElMessage.error('文件下载失败')
        return Promise.reject(new Error('文件下载失败'))
      }
    }

    const responseData = error.response?.data as ErrorResponse | undefined
    let message = responseData?.msg || error.message

    if (!responseData?.msg) {
      if (error.response?.status === 401) {
        message = '登录已过期'
      } else if (error.response?.status === 403) {
        message = '没有权限'
      } else if (error.response?.status === 404) {
        message = '请求资源不存在'
      } else if (error.response?.status === 409) {
        message = '数据状态冲突'
      } else if (error.response?.status === 422) {
        message = '业务校验未通过'
      } else if (error.response?.status === 500) {
        message = '服务器错误'
      }
    }

    ElMessage.error(message)
    return Promise.reject(new Error(message))
  }
)

export default service as RequestInstance
