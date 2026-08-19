/** 私信消息（双方之一视角可读，第三者不可见） */
export interface MessageItem {
  id: number
  senderId: number
  receiverId: number
  content: string
  /** 1 已读 / 0 未读 */
  isRead: number
  createTime: string
}

/** 按好友未读数（好友行角标） */
export interface PeerUnread {
  userId: number
  unreadCount: number
}
