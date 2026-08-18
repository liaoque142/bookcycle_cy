import request from './request'
import { USE_MOCK, delay } from './mock'
import type { UserBrief, Friend, FriendRequestItem, BlacklistItem } from '@/types/social'

/** 搜索书友（用户名/昵称模糊） */
export async function searchUsers(keyword: string): Promise<UserBrief[]> {
  if (USE_MOCK) {
    await delay(120)
    return []
  }
  return (await request.get('/friends/search', { params: { keyword } })) as unknown as UserBrief[]
}

/** 好友列表 */
export async function fetchFriends(): Promise<Friend[]> {
  if (USE_MOCK) {
    await delay(120)
    return []
  }
  return (await request.get('/friends')) as unknown as Friend[]
}

/** 发送好友申请 */
export async function sendFriendRequest(toUserId: number): Promise<void> {
  if (USE_MOCK) {
    await delay(120)
    return
  }
  await request.post('/friends/requests', { toUserId })
}

/** 我收到的好友申请 */
export async function fetchFriendRequests(): Promise<FriendRequestItem[]> {
  if (USE_MOCK) {
    await delay(120)
    return []
  }
  return (await request.get('/friends/requests')) as unknown as FriendRequestItem[]
}

/** 处理好友申请：accept / reject */
export async function handleFriendRequest(id: number, action: 'accept' | 'reject'): Promise<void> {
  if (USE_MOCK) {
    await delay(120)
    return
  }
  await request.put(`/friends/requests/${id}`, { action })
}

/** 删除好友 */
export async function deleteFriend(userId: number): Promise<void> {
  if (USE_MOCK) {
    await delay(120)
    return
  }
  await request.delete(`/friends/${userId}`)
}

/** 黑名单列表 */
export async function fetchBlacklist(): Promise<BlacklistItem[]> {
  if (USE_MOCK) {
    await delay(120)
    return []
  }
  return (await request.get('/blacklist')) as unknown as BlacklistItem[]
}

/** 拉黑用户 */
export async function addBlacklist(userId: number): Promise<void> {
  if (USE_MOCK) {
    await delay(120)
    return
  }
  await request.post('/blacklist', { toUserId: userId })
}

/** 解除拉黑 */
export async function removeBlacklist(userId: number): Promise<void> {
  if (USE_MOCK) {
    await delay(120)
    return
  }
  await request.delete(`/blacklist/${userId}`)
}
