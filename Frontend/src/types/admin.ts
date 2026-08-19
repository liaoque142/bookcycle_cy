/** 管理后台用户（AdminUserVO：sys_user + role + user_profile） */
export interface AdminUser {
  id: number
  username: string
  /** 昵称（缺省回退用户名） */
  nickname: string
  avatar?: string
  bio?: string
  phone?: string
  email?: string
  /** 信誉分 */
  credit: number
  /** 用户状态：1 正常 / 0 禁用 */
  status: number
  roleCode: string
  roleName: string
  lastLoginTime?: string
  createTime: string
}

/** 管理后台分类（category 实体） */
export interface AdminCategory {
  id: number
  /** 父分类 id，0 表示根分类 */
  parentId: number
  name: string
  sort: number
  /** 1 启用 / 0 禁用 */
  status: number
  createTime?: string
  /** 组树后的子节点（后端返回扁平列表，前端组装） */
  children?: AdminCategory[]
}

/** 分类新增/编辑入参（CategoryRequest） */
export interface AdminCategoryPayload {
  /** 父分类 id，0 或空表示根分类 */
  parentId?: number
  name: string
  /** 排序值，越小越靠前 */
  sort?: number
  /** 1 启用 / 0 禁用 */
  status?: number
}

/** 管理后台书籍（BookVO，含状态与分类 id） */
export interface AdminBook {
  id: number
  title: string
  author: string
  isbn?: string
  publisher?: string
  categoryId: number
  category: string
  /** 售价；交换/赠送为 null */
  price: number | null
  /** 出售 / 赠送 / 交换 */
  tradeType: string
  /** 全新 / 九成新 / 八成新 / 七成新及以下 */
  condition: string
  description?: string
  /** 1 在售 / 2 已预订 / 3 已售出 / 4 已下架 */
  status: number
  statusName: string
  sellerId: number
  /** 卖家昵称 */
  seller: string
  /** 卖家信誉分 */
  credit: number
  createdAt: string
}

/** 操作日志（OperationLogVO） */
export interface OperationLog {
  id: number
  action: string
  detail: string
  operatorId: number
  operatorName: string
  createTime: string
}

/** 管理后台订单（对应后端 OrderVO，含交易方式/状态编码） */
export interface AdminOrder {
  id: number
  orderNo: string
  bookId: number
  bookTitle: string
  buyerId: number
  buyerName: string
  sellerId: number
  sellerName: string
  amount: number
  /** 出售 / 赠送 / 交换 */
  tradeType: string
  tradeTypeCode: number
  /** 邮寄 / 自提 */
  shipType: string
  shipTypeCode: number
  /** 待支付 / 待发货 / 待收货 / 已完成 / 已取消 / 售后中 */
  status: string
  statusCode: number
  receiverName?: string
  receiverPhone?: string
  receiverAddress?: string
  cancelReason?: string
  /** 支付状态：0 未支付 / 1 已支付 / 2 已退款 */
  payStatus?: number
  payTime?: string
  evaluatedByMe?: boolean
  createTime: string
  updateTime: string
}

/** 信誉规则（credit_rule，系统配置） */
export interface AdminCreditRule {
  id: number
  ruleCode: string
  name: string
  triggerCondition: string
  /** 分值（正加负扣） */
  changeValue: number
  priority: number
  /** 1 启用 / 0 禁用 */
  status: number
}

/** 管理后台统计概览 */
export interface StatsOverview {
  userCount: number
  bookCount: number
  orderCount: number
  todayOrders: number
  todayNewUsers: number
  totalAmount: number
  pendingRefund: number
}

/** 每日统计趋势（近 N 天订单数 / 新增用户数） */
export interface StatsTrend {
  /** yyyy-MM-dd */
  date: string
  orderCount: number
  userCount: number
}

/** 管理后台售后/退款申请（RefundVO） */
export interface AdminRefund {
  id: number
  orderId: number
  orderNo: string
  bookId: number
  bookTitle: string
  amount: number
  /** 仅退款 / 退货退款 */
  type: string
  typeCode: number
  reason?: string
  /** 待处理 / 同意 / 拒绝 / 已完成 */
  status: string
  statusCode: number
  applicantId: number
  applicantName: string
  handlerName?: string
  handleResult?: string
  createTime: string
  handleTime?: string
}
