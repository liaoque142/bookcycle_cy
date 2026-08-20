/** 用户摘要（搜索书友 / 好友 / 黑名单展示，昵称缺省回退用户名） */
export interface UserBrief {
  id: number
  username: string
  nickname: string
  avatar?: string
  credit: number
}

/** 好友（id 为 friend 表主键，userId 为好友侧用户 ID） */
export interface Friend extends UserBrief {
  userId: number
  /** 成为好友时间 */
  createTime: string
}

/** 我收到的好友申请 */
export interface FriendRequestItem {
  id: number
  fromUserId: number
  fromUsername: string
  fromNickname: string
  fromAvatar?: string
  fromCredit: number
  toUserId: number
  /** 0 待处理 / 1 同意 / 2 拒绝 */
  status: 0 | 1 | 2
  createTime: string
}

/** 黑名单条目（userId 为被拉黑用户 ID） */
export interface BlacklistItem extends UserBrief {
  userId: number
  createTime: string
}
