/** 论坛板块 */
export interface ForumBoard {
  id: number
  name: string
  description: string
  sort: number
}

/** 论坛帖子（列表项，无正文） */
export interface ForumPostItem {
  id: number
  boardId: number
  boardName: string
  authorId: number
  author: string
  /** 发帖人头像（可空，前端回退首字） */
  authorAvatar?: string
  title: string
  /** 列表摘要 */
  excerpt: string
  replyCount: number
  likeCount: number
  viewCount: number
  /** 当前用户是否已赞（游客为 null/false） */
  liked: boolean | null
  createTime: string
  updateTime: string
}

/** 论坛帖子详情（含正文与已赞标记） */
export interface ForumPostDetail extends ForumPostItem {
  content: string
}

/** 帖子评论 */
export interface ForumComment {
  id: number
  postId: number
  authorId: number
  author: string
  /** 评论人头像（可空，前端回退首字） */
  authorAvatar?: string
  content: string
  /** 图片 URL（可选，与文字可并存） */
  imageUrl?: string
  createTime: string
}

/** 发帖 / 编辑提交参数 */
export interface CreateForumPostPayload {
  boardId: number
  title: string
  content: string
}

/** 评论提交参数 */
export interface CreateForumCommentPayload {
  postId: number
  /** 文字内容（与图片至少一项） */
  content: string
  /** 图片 URL（可选） */
  imageUrl?: string
}

/** 点赞结果 */
export interface ForumLikeResult {
  liked: boolean
  likeCount: number
}

/** 帖子列表查询参数 */
export interface ForumQuery {
  boardId?: number
  keyword?: string
  sort?: 'latest' | 'hot'
  page?: number
  size?: number
}
