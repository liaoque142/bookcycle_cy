import request from './request'
import { USE_MOCK, delay, toPage } from './mock'
import type { Book } from '@/types/book'
import type { Page } from '@/types/api'

/** 收藏书籍 */
export async function addFavorite(bookId: number): Promise<void> {
  if (USE_MOCK) {
    await delay(120)
    return
  }
  await request.post(`/favorites/${bookId}`)
}

/** 取消收藏 */
export async function removeFavorite(bookId: number): Promise<void> {
  if (USE_MOCK) {
    await delay(120)
    return
  }
  await request.delete(`/favorites/${bookId}`)
}

/** 我的收藏分页 */
export async function fetchMyFavorites(page = 1, size = 50): Promise<Page<Book>> {
  if (USE_MOCK) {
    await delay(120)
    return toPage([], page, size)
  }
  return (await request.get('/favorites/me', { params: { page, size } })) as unknown as Page<Book>
}

/** 好友收藏的书籍分页（仅好友可见，好友弹窗「喜欢书籍」用） */
export async function fetchFriendFavorites(userId: number, page = 1, size = 50): Promise<Page<Book>> {
  if (USE_MOCK) {
    await delay(120)
    return toPage([], page, size)
  }
  return (await request.get(`/friends/${userId}/favorites`, { params: { page, size } })) as unknown as Page<Book>
}
