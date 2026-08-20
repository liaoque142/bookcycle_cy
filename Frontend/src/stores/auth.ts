import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { login as loginApi, register as registerApi, logout as logoutApi } from '@/api/auth'
import type { LoginParams, RegisterParams, User } from '@/types/user'

const TOKEN_KEY = 'bc_token'
const USER_KEY = 'bc_user'

function readStoredUser(): User | null {
  try {
    return JSON.parse(localStorage.getItem(USER_KEY) ?? 'null') as User | null
  } catch {
    return null
  }
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem(TOKEN_KEY) ?? '')
  const user = ref<User | null>(readStoredUser())

  const isLoggedIn = computed(() => !!token.value)

  async function login(params: LoginParams) {
    const result = await loginApi(params)
    token.value = result.token
    user.value = result.user
    localStorage.setItem(TOKEN_KEY, result.token)
    localStorage.setItem(USER_KEY, JSON.stringify(result.user))
  }

  /** 注册成功后自动登录 */
  async function register(params: RegisterParams) {
    await registerApi(params)
    await login({ account: params.account, password: params.password })
  }

  async function logout() {
    try {
      // 无状态 JWT：调服务端登出接口（目前为空实现），失败不影响本地登出
      await logoutApi()
    } finally {
      // 无论接口是否成功，本地登录态必须清干净
      token.value = ''
      user.value = null
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
    }
  }

  return { token, user, isLoggedIn, login, register, logout }
})
