/** 书籍讨论（顶级评论含子回复） */
export interface DiscussionItem {
  id: number
  bookId: number
  userId: number
  /** 评论人昵称（缺省回退用户名） */
  author: string
  /** 评论人头像（可空，前端回退首字） */
  authorAvatar?: string
  content: string
  /** 图片 URL（可选，与文字可并存） */
  imageUrl?: string
  /** 父评论 ID，null 为顶级 */
  parentId: number | null
  createTime: string
  replies: DiscussionItem[]
}
