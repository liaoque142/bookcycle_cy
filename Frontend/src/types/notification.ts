/** 站内通知 */
export interface NotificationItem {
  id: number
  /** 1 系统 / 2 交易 / 3 好友 */
  type: number
  typeName: string
  content: string
  /** 1 已读 / 0 未读 */
  isRead: number
  createTime: string
}
