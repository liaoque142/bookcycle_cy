/** 私信消息（双方之一视角可读，第三者不可见） */
export interface MessageItem {
  id: number
  senderId: number
  receiverId: number
  content: string
  /** 图片 URL（可选，与文字可并存） */
  imageUrl?: string
  /** 1 已读 / 0 未读 */
  isRead: number
  /** 发送人昵称（缺省回退用户名） */
  senderName?: string
  /** 发送人头像（可空，前端回退首字） */
  senderAvatar?: string
  createTime: string
}

/** 发送私信参数 */
export interface SendMessagePayload {
  toUserId: number
  /** 文字内容（与图片至少一项） */
  content: string
  /** 图片 URL（可选） */
  imageUrl?: string
}

/** 按好友未读数（好友行角标） */
export interface PeerUnread {
  userId: number
  unreadCount: number
}
