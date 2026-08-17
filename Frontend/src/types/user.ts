/** 登录用户（对应 sys_user + user_profile） */
export interface User {
  id: number
  username: string
  nickname: string
  avatar?: string
  phone?: string
  email?: string
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
