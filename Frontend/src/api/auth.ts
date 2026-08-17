import request from './request'
import { USE_MOCK, delay } from './mock'
import type { LoginParams, RegisterParams, LoginResult, User } from '@/types/user'

const mockUser: User = {
  id: 1,
  username: 'demo',
  nickname: '书友小明',
  email: 'demo@bookcycle.dev',
  credit: 100,
  role: 'user',
}

/** 登录（mock 返回假 token + 用户） */
export async function login(params: LoginParams): Promise<LoginResult> {
  if (USE_MOCK) {
    await delay(300)
    if (!params.account || !params.password) {
      throw new Error('请输入账号和密码')
    }
    return { token: `mock-token-${Date.now()}`, user: mockUser }
  }
  return (await request.post('/auth/login', params)) as unknown as LoginResult
}

/** 注册 */
export async function register(params: RegisterParams): Promise<void> {
  if (USE_MOCK) {
    await delay(300)
    return
  }
  await request.post('/auth/register', params)
}

/** 登出 */
export async function logout(): Promise<void> {
  if (USE_MOCK) {
    await delay(100)
    return
  }
  await request.post('/auth/logout')
}
