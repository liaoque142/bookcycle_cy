import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { login as loginApi, logout as logoutApi } from '@/api/auth'
import type { LoginParams, User } from '@/types/user'

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

  async function logout() {
    await logoutApi()
    token.value = ''
    user.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  return { token, user, isLoggedIn, login, logout }
})
