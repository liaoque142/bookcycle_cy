import axios from 'axios'
import type { AxiosResponse } from 'axios'
import type { Result } from '@/types/api'

const TOKEN_KEY = 'bc_token'
const USER_KEY = 'bc_user'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  timeout: 10000,
})

// 请求拦截：带 token（不 import store，避免 auth store ↔ request 循环依赖）
request.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截：解包 Result<T>，非 200 时 reject
request.interceptors.response.use(
  (response) => {
    const body = response.data as Result<unknown>
    if (body.code !== 200) {
      if (body.code === 401) {
        // 登录态失效，清 token；登录页就绪后再跳转
        localStorage.removeItem(TOKEN_KEY)
        localStorage.removeItem(USER_KEY)
        console.warn('[api] 401 未认证，登录态已清除')
        // 跳转登录页（动态 import 避免 request ↔ router 循环依赖）
        if (window.location.pathname !== '/login') {
          const redirect = window.location.pathname + window.location.search
          void import('@/router').then(({ default: router }) =>
            router.push({ path: '/login', query: { redirect } }),
          )
        }
      }
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    // 拦截器解包 Result<T>，调用方拿到 data 本身（axios 类型仍按 AxiosResponse 标注，由调用方断言）
    return body.data as unknown as AxiosResponse
  },
  (error) => Promise.reject(error),
)

export default request
