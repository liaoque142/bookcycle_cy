import request from './request'
import { USE_MOCK, delay, toPage } from './mock'
import { boards as mockBoards, posts as mockPosts, comments as mockComments } from '@/data/posts'
import type { Page } from '@/types/api'
import type {
  ForumBoard,
  ForumPostItem,
  ForumPostDetail,
  ForumComment,
  CreateForumPostPayload,
  CreateForumCommentPayload,
  ForumLikeResult,
  ForumQuery,
} from '@/types/forum'

/** 论坛板块列表 */
export async function fetchForumBoards(): Promise<ForumBoard[]> {
  if (USE_MOCK) {
    await delay(120)
    return mockBoards
  }
  return (await request.get('/forum/boards')) as unknown as ForumBoard[]
}

/** 帖子分页列表（板块/关键词过滤 + latest/hot 排序） */
export async function fetchForumPosts(query: ForumQuery = {}): Promise<Page<ForumPostItem>> {
  if (USE_MOCK) {
    await delay(150)
    const { boardId, keyword, sort = 'latest', page = 1, size = 10 } = query
    let list = mockPosts.filter((p) => {
      if (boardId && p.boardId !== boardId) return false
      if (keyword && !(p.title.includes(keyword) || p.excerpt.includes(keyword))) return false
      return true
    })
    if (sort === 'hot') list = [...list].sort((a, b) => b.likeCount + b.replyCount - (a.likeCount + a.replyCount))
    return toPage(list, page, size)
  }
  return (await request.get('/forum/posts', { params: query })) as unknown as Page<ForumPostItem>
}

/** 帖子详情 */
export async function fetchForumPostDetail(id: number): Promise<ForumPostDetail> {
  if (USE_MOCK) {
    await delay(120)
    const p = mockPosts.find((x) => x.id === id)
    if (!p) throw new Error('帖子不存在')
    return { ...p, content: p.excerpt, liked: false } as ForumPostDetail
  }
  return (await request.get(`/forum/posts/${id}`)) as unknown as ForumPostDetail
}

/** 发帖 */
export async function createForumPost(payload: CreateForumPostPayload): Promise<ForumPostDetail> {
  if (USE_MOCK) {
    await delay(150)
    const board = mockBoards.find((b) => b.id === payload.boardId)
    return {
      ...mockPosts[0],
      id: Date.now(),
      boardId: payload.boardId,
      boardName: board?.name ?? '',
      title: payload.title,
      excerpt: payload.content.slice(0, 100),
      content: payload.content,
      replyCount: 0,
      likeCount: 0,
      viewCount: 0,
      createTime: '2026-08-18 00:00:00',
      updateTime: '2026-08-18 00:00:00',
    } as ForumPostDetail
  }
  return (await request.post('/forum/posts', payload)) as unknown as ForumPostDetail
}

/** 编辑帖子 */
export async function updateForumPost(id: number, payload: CreateForumPostPayload): Promise<ForumPostDetail> {
  if (USE_MOCK) {
    await delay(150)
    return createForumPost(payload)
  }
  return (await request.put(`/forum/posts/${id}`, payload)) as unknown as ForumPostDetail
}

/** 删除帖子 */
export async function deleteForumPost(id: number): Promise<void> {
  if (USE_MOCK) {
    await delay(100)
    return
  }
  await request.delete(`/forum/posts/${id}`)
}

/** 帖子评论列表 */
export async function fetchForumComments(postId: number): Promise<ForumComment[]> {
  if (USE_MOCK) {
    await delay(120)
    return mockComments.filter((c) => c.postId === postId)
  }
  return (await request.get(`/forum/posts/${postId}/comments`)) as unknown as ForumComment[]
}

/** 发表评论 */
export async function createForumComment(payload: CreateForumCommentPayload): Promise<void> {
  if (USE_MOCK) {
    await delay(100)
    return
  }
  await request.post('/forum/comments', payload)
}

/** 点赞（幂等） */
export async function likeForumPost(id: number): Promise<ForumLikeResult> {
  if (USE_MOCK) {
    await delay(100)
    return { liked: true, likeCount: 1 }
  }
  return (await request.post(`/forum/posts/${id}/like`)) as unknown as ForumLikeResult
}

/** 取消点赞（幂等） */
export async function unlikeForumPost(id: number): Promise<ForumLikeResult> {
  if (USE_MOCK) {
    await delay(100)
    return { liked: false, likeCount: 0 }
  }
  return (await request.delete(`/forum/posts/${id}/like`)) as unknown as ForumLikeResult
}
