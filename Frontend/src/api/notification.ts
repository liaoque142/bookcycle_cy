import request from './request'
import { USE_MOCK, delay, toPage } from './mock'
import type { Page } from '@/types/api'
import type { NotificationItem } from '@/types/notification'

export interface NotificationQuery {
  unreadOnly?: boolean
  page?: number
  size?: number
}

/** 通知分页列表 */
export async function fetchNotifications(query: NotificationQuery = {}): Promise<Page<NotificationItem>> {
  if (USE_MOCK) {
    await delay(120)
    return toPage([], query.page, query.size)
  }
  return (await request.get('/notifications', { params: query })) as unknown as Page<NotificationItem>
}

/** 未读数（Header 铃铛红点） */
export async function fetchUnreadCount(): Promise<number> {
  if (USE_MOCK) {
    await delay(60)
    return 0
  }
  return (await request.get('/notifications/unread-count')) as unknown as number
}

/** 标记单条已读 */
export async function markNotificationRead(id: number): Promise<void> {
  if (USE_MOCK) {
    await delay(60)
    return
  }
  await request.put(`/notifications/${id}/read`)
}

/** 全部已读 */
export async function markAllNotificationsRead(): Promise<void> {
  if (USE_MOCK) {
    await delay(60)
    return
  }
  await request.put('/notifications/read-all')
}
