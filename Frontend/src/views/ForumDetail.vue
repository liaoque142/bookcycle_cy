<script setup lang="ts">
import { computed, reactive, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, ChatLineRound, Star, StarFilled, View, EditPen, Delete } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import ImageUploader from '@/components/common/ImageUploader.vue'
import {
  fetchForumBoards,
  fetchForumPostDetail,
  fetchForumComments,
  createForumComment,
  likeForumPost,
  unlikeForumPost,
  updateForumPost,
  deleteForumPost,
} from '@/api/forum'
import type { ForumBoard, ForumPostDetail, ForumComment, CreateForumPostPayload } from '@/types/forum'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const post = ref<ForumPostDetail | null>(null)
const comments = ref<ForumComment[]>([])
const loading = ref(false)
const liking = ref(false)

const commentText = ref('')
const commentImage = ref('')
const submitting = ref(false)

const boards = ref<ForumBoard[]>([])
const editVisible = ref(false)
const editing = ref(false)
const editForm = reactive<CreateForumPostPayload>({ boardId: 0, title: '', content: '' })

const isMine = computed(() => !!post.value && !!auth.user && auth.user.id === post.value.authorId)

async function load() {
  const id = Number(route.params.id)
  loading.value = true
  try {
    const [p, c] = await Promise.all([fetchForumPostDetail(id), fetchForumComments(id)])
    post.value = p
    comments.value = c
  } finally {
    loading.value = false
  }
}

async function toggleLike() {
  if (!post.value || liking.value) return
  if (!auth.isLoggedIn) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  liking.value = true
  try {
    const r = post.value.liked ? await unlikeForumPost(post.value.id) : await likeForumPost(post.value.id)
    post.value.liked = r.liked
    post.value.likeCount = r.likeCount
  } finally {
    liking.value = false
  }
}

async function submitComment() {
  if (!post.value) return
  if (!auth.isLoggedIn) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  const content = commentText.value.trim()
  const imageUrl = commentImage.value
  if (!content && !imageUrl) return
  submitting.value = true
  try {
    await createForumComment({ postId: post.value.id, content, imageUrl })
    commentText.value = ''
    commentImage.value = ''
    ElMessage.success('评论成功')
    comments.value = await fetchForumComments(post.value.id)
    post.value.replyCount += 1
  } finally {
    submitting.value = false
  }
}

function openEdit() {
  if (!post.value) return
  editForm.boardId = post.value.boardId
  editForm.title = post.value.title
  editForm.content = post.value.content
  editVisible.value = true
}

async function submitEdit() {
  if (!post.value || !editForm.title.trim() || !editForm.content.trim()) return
  editing.value = true
  try {
    post.value = await updateForumPost(post.value.id, {
      boardId: editForm.boardId,
      title: editForm.title.trim(),
      content: editForm.content.trim(),
    })
    ElMessage.success('已保存')
    editVisible.value = false
  } finally {
    editing.value = false
  }
}

async function removePost() {
  if (!post.value) return
  try {
    await ElMessageBox.confirm('确定删除这篇帖子吗？删除后不可恢复。', '删除帖子', { type: 'warning' })
  } catch {
    return
  }
  await deleteForumPost(post.value.id)
  ElMessage.success('已删除')
  router.push('/forum')
}

function formatTime(t?: string) {
  return t ? t.slice(0, 10) : ''
}

onMounted(async () => {
  boards.value = await fetchForumBoards()
  load()
})
</script>

<template>
  <section class="section detail" v-loading="loading">
    <div class="container container--narrow">
      <button class="back" @click="router.push('/forum')">
        <el-icon><ArrowLeft /></el-icon> 返回论坛
      </button>

      <template v-if="post">
        <div class="post-head">
          <div class="post-head__top">
            <span class="post-head__board">{{ post.boardName }}</span>
            <h1 class="post-head__title">{{ post.title }}</h1>
          </div>
          <div class="post-head__meta">
            <span class="author">{{ post.author }}</span>
            <span>{{ formatTime(post.createTime) }}</span>
            <span class="stat"><el-icon><View /></el-icon> {{ post.viewCount }}</span>
          </div>
          <div v-if="isMine" class="post-head__ops">
            <el-button size="small" :icon="EditPen" @click="openEdit">编辑</el-button>
            <el-button size="small" type="danger" plain :icon="Delete" @click="removePost">删除</el-button>
          </div>
        </div>

        <div class="post-content">{{ post.content }}</div>

        <div class="actions">
          <el-button
            :type="post.liked ? 'primary' : 'default'"
            round
            :icon="post.liked ? StarFilled : Star"
            :loading="liking"
            @click="toggleLike"
          >
            {{ post.liked ? '已赞' : '点赞' }} {{ post.likeCount }}
          </el-button>
          <span class="actions__reply"><el-icon><ChatLineRound /></el-icon> {{ comments.length }} 条评论</span>
        </div>

        <div class="comments">
          <h2 class="comments__title">评论</h2>
          <div class="comments__form">
            <el-input
              v-model="commentText"
              type="textarea"
              :rows="3"
              maxlength="500"
              show-word-limit
              placeholder="友善评论，理性发言…"
            />
            <div class="comments__submit">
              <ImageUploader v-model="commentImage" />
              <el-button type="primary" round :loading="submitting" @click="submitComment">发表评论</el-button>
            </div>
          </div>
          <ul class="comments__list">
            <li v-for="c in comments" :key="c.id" class="comment">
              <div class="comment__head">
                <span class="comment__author">{{ c.author }}</span>
                <span class="comment__time">{{ formatTime(c.createTime) }}</span>
              </div>
              <div class="comment__body">
                <img v-if="c.imageUrl" :src="c.imageUrl" class="comment__img" alt="图片" />
                <p v-if="c.content" class="comment__content">{{ c.content }}</p>
              </div>
            </li>
            <li v-if="comments.length === 0" class="comment__empty">还没有评论，来抢沙发</li>
          </ul>
        </div>
      </template>

      <div v-else-if="!loading" class="empty">帖子不存在或已删除</div>
    </div>
  </section>

  <!-- 编辑弹窗 -->
  <el-dialog v-model="editVisible" title="编辑帖子" width="560px">
    <el-form label-width="56px">
      <el-form-item label="板块">
        <el-select v-model="editForm.boardId" placeholder="请选择板块">
          <el-option v-for="b in boards" :key="b.id" :label="b.name" :value="b.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="标题">
        <el-input v-model="editForm.title" maxlength="100" show-word-limit />
      </el-form-item>
      <el-form-item label="内容">
        <el-input v-model="editForm.content" type="textarea" :rows="6" maxlength="10000" show-word-limit />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="editVisible = false">取消</el-button>
      <el-button type="primary" :loading="editing" @click="submitEdit">保存</el-button>
    </template>
  </el-dialog>
</template>

<style scoped lang="scss">
.detail {
  min-height: 60vh;
}

.container--narrow {
  max-width: 780px;
}

.back {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-bottom: var(--sp-5);
  font-size: 0.88rem;
  color: var(--muted);
  cursor: pointer;
  background: none;
  border: none;
  padding: 0;
  transition: color 0.2s ease;

  &:hover {
    color: var(--mark);
  }
}

.post-head {
  padding-bottom: var(--sp-5);
  border-bottom: 1px solid var(--line);

  &__top {
    display: flex;
    align-items: center;
    gap: var(--sp-3);
  }

  &__board {
    flex-shrink: 0;
    font-size: 0.78rem;
    color: var(--mark);
    border: 1px solid rgba(176, 137, 78, 0.35);
    border-radius: 999px;
    padding: 2px 12px;
    white-space: nowrap;
  }

  &__title {
    font-size: 1.5rem;
    font-weight: 600;
    line-height: 1.4;
  }

  &__meta {
    display: flex;
    align-items: center;
    gap: var(--sp-4);
    margin-top: var(--sp-3);
    font-size: 0.84rem;
    color: var(--muted);

    .author {
      font-weight: 500;
      color: var(--ink);
    }

    .stat {
      display: inline-flex;
      align-items: center;
      gap: 4px;
    }
  }

  &__ops {
    margin-top: var(--sp-3);
  }
}

.post-content {
  padding: var(--sp-6) 0;
  font-size: 1rem;
  line-height: 1.9;
  white-space: pre-wrap;
  word-break: break-word;
}

.actions {
  display: flex;
  align-items: center;
  gap: var(--sp-4);
  padding: var(--sp-4) 0;
  border-top: 1px solid var(--line);
  border-bottom: 1px solid var(--line);

  &__reply {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    font-size: 0.84rem;
    color: var(--muted);
  }
}

.comments {
  margin-top: var(--sp-6);

  &__title {
    font-size: 1.1rem;
    font-weight: 600;
    margin-bottom: var(--sp-4);
  }

  &__form {
    background: var(--paper);
    border-radius: var(--radius-sm);
    padding: var(--sp-4);
    margin-bottom: var(--sp-5);
  }

  &__submit {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: var(--sp-2);
    margin-top: var(--sp-3);
  }

  &__list {
    list-style: none;
    margin: 0;
    padding: 0;
  }
}

.comment {
  padding: var(--sp-4) 0;
  border-bottom: 1px solid var(--line);

  &__head {
    display: flex;
    align-items: center;
    gap: var(--sp-3);
  }

  &__author {
    font-size: 0.88rem;
    font-weight: 500;
  }

  &__time {
    font-size: 0.78rem;
    color: var(--muted);
  }

  &__body {
    margin-top: var(--sp-2);
  }

  &__img {
    max-width: 260px;
    max-height: 320px;
    border-radius: var(--radius-sm);
    border: 1px solid var(--line);
    object-fit: contain;
    margin-bottom: var(--sp-2);
  }

  &__content {
    font-size: 0.95rem;
    line-height: 1.7;
    white-space: pre-wrap;
    word-break: break-word;
  }

  &__empty {
    padding: var(--sp-6) 0;
    text-align: center;
    color: var(--muted);
    font-size: 0.9rem;
  }
}

.empty {
  text-align: center;
  padding: var(--sp-8) 0;
  color: var(--muted);
}
</style>
