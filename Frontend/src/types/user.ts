/** 登录用户（对应 sys_user + user_profile） */
export interface User {
  id: number
  username: string
  nickname: string
  avatar?: string
  phone?: string
  email?: string
  /** 个人简介 */
  bio?: string
  /** 兴趣标签（逗号分隔字符串） */
  interestTags?: string
  /** 书籍喜好分类ID（多选） */
  genreIds?: number[]
  /** 信誉分 0-100 */
  credit: number
  /** 角色名，如 user / admin */
  role: string
}

export interface LoginParams {
  /** 手机号或邮箱 */
  account: string
  password: string
}

export interface RegisterParams extends LoginParams {
  nickname?: string
}

export interface LoginResult {
  token: string
  user: User
}
