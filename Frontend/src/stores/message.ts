import { ref } from 'vue'
import { defineStore } from 'pinia'
import { fetchMessageUnreadCount } from '@/api/message'

/**
 * 私信未读总数：Header 红点与好友页共享，标记已读/发送后即时刷新，
 * 避免 30s 轮询周期内角标过期。
 */
export const useMessageStore = defineStore('message', () => {
  const unread = ref(0)

  async function refresh() {
    try {
      unread.value = await fetchMessageUnreadCount()
    } catch {
      /* 非关键，忽略 */
    }
  }

  return { unread, refresh }
})
