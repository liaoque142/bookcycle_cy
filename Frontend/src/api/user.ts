import request from './request'
import { USE_MOCK, delay } from './mock'
import type { User } from '@/types/user'

const mockUser: User = {
  id: 1,
  username: 'demo',
  nickname: '书友小明',
  email: 'demo@bookcycle.dev',
  credit: 100,
  role: 'user',
}

/** 我的资料（需登录，含简介/兴趣标签/书籍喜好） */
export async function fetchMe(): Promise<User> {
  if (USE_MOCK) {
    await delay(100)
    return mockUser
  }
  return (await request.get('/user/me')) as unknown as User
}

/** 更新我的资料（返回最新 UserVO 供 store 同步） */
export async function updateProfile(payload: Partial<User>): Promise<User> {
  if (USE_MOCK) {
    await delay(120)
    return { ...mockUser, ...payload }
  }
  return (await request.put('/user/profile', payload)) as unknown as User
}
