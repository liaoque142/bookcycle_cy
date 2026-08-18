<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Plus, ChatLineRound, Star, View } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { fetchForumBoards, fetchForumPosts, createForumPost } from '@/api/forum'
import type { ForumBoard, ForumPostItem, CreateForumPostPayload } from '@/types/forum'

const router = useRouter()
const auth = useAuthStore()

const boards = ref<ForumBoard[]>([])
const boardId = ref<number | undefined>(undefined)
const keyword = ref('')
const sort = ref<'latest' | 'hot'>('latest')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const posts = ref<ForumPostItem[]>([])
const loading = ref(false)

// 发帖弹窗
const publishVisible = ref(false)
const publishing = ref(false)
const form = reactive<CreateForumPostPayload>({ boardId: 0, title: '', content: '' })

async function loadPosts() {
  loading.value = true
  try {
    const result = await fetchForumPosts({
      boardId: boardId.value,
      keyword: keyword.value || undefined,
      sort: sort.value,
      page: page.value,
      size: size.value,
    })
    posts.value = result.list
    total.value = result.total
  } finally {
    loading.value = false
  }
}

function reload() {
  page.value = 1
  loadPosts()
}

function openPublish() {
  if (!auth.isLoggedIn) {
    router.push({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })
    return
  }
  form.boardId = boardId.value ?? boards.value[0]?.id ?? 0
  form.title = ''
  form.content = ''
  publishVisible.value = true
}

async function submitPost() {
  if (!form.boardId || !form.title.trim() || !form.content.trim()) return
  publishing.value = true
  try {
    await createForumPost({ boardId: form.boardId, title: form.title.trim(), content: form.content.trim() })
    ElMessage.success('发布成功')
    publishVisible.value = false
    reload()
  } finally {
    publishing.value = false
  }
}

function formatTime(t?: string) {
  return t ? t.slice(0, 10) : ''
}

onMounted(async () => {
  boards.value = await fetchForumBoards()
  loadPosts()
})
</script>

<template>
  <section class="section forum">
    <div class="container">
      <div class="head">
        <div>
          <h1 class="section-title">书友论坛</h1>
          <p class="section-sub">共 {{ total }} 个帖子 · 读书心得 · 书评 · 闲置交流</p>
        </div>
        <el-button type="primary" round :icon="Plus" @click="openPublish">发帖</el-button>
      </div>

      <!-- 板块 Tab -->
      <div class="boards">
        <button
          class="boards__pill"
          :class="{ 'is-active': boardId === undefined }"
          @click="boardId = undefined; reload()"
        >
          全部
        </button>
        <button
          v-for="b in boards"
          :key="b.id"
          class="boards__pill"
          :class="{ 'is-active': boardId === b.id }"
          @click="boardId = b.id; reload()"
        >
          {{ b.name }}
        </button>
      </div>

      <div class="toolbar">
        <el-input
          v-model="keyword"
          class="toolbar__search"
          placeholder="搜索帖子标题 / 内容"
          :prefix-icon="Search"
          clearable
          @keyup.enter="reload"
          @clear="reload"
        />
        <el-select v-model="sort" class="toolbar__select" @change="reload">
          <el-option label="最新发布" value="latest" />
          <el-option label="热门" value="hot" />
        </el-select>
      </div>

      <div v-loading="loading" class="list">
        <div v-for="p in posts" :key="p.id" class="post" @click="router.push(`/forum/posts/${p.id}`)">
          <div class="post__main">
            <div class="post__top">
              <span class="post__board">{{ p.boardName }}</span>
              <span class="post__title">{{ p.title }}</span>
            </div>
            <p class="post__excerpt">{{ p.excerpt }}</p>
            <div class="post__meta">
              <span class="post__author">{{ p.author }}</span>
              <span class="post__time">{{ formatTime(p.createTime) }}</span>
            </div>
          </div>
          <div class="post__stats">
            <span class="post__stat"><el-icon><ChatLineRound /></el-icon>{{ p.replyCount }}</span>
            <span class="post__stat"><el-icon><Star /></el-icon>{{ p.likeCount }}</span>
            <span class="post__stat"><el-icon><View /></el-icon>{{ p.viewCount }}</span>
          </div>
        </div>
        <div v-if="total === 0 && !loading" class="empty">
          <p>还没有帖子，来发第一帖吧</p>
        </div>
      </div>

      <div v-if="total > size" class="pager">
        <el-pagination
          v-model:current-page="page"
          :page-size="size"
          :total="total"
          layout="prev, pager, next"
          background
          @current-change="loadPosts"
        />
      </div>
    </div>
  </section>

  <!-- 发帖弹窗 -->
  <el-dialog v-model="publishVisible" title="发布帖子" width="560px">
    <el-form label-width="56px">
      <el-form-item label="板块">
        <el-select v-model="form.boardId" placeholder="请选择板块">
          <el-option v-for="b in boards" :key="b.id" :label="b.name" :value="b.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="标题">
        <el-input v-model="form.title" maxlength="100" show-word-limit placeholder="一句话说清主题" />
      </el-form-item>
      <el-form-item label="内容">
        <el-input v-model="form.content" type="textarea" :rows="6" maxlength="10000" show-word-limit placeholder="展开说说…" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="publishVisible = false">取消</el-button>
      <el-button type="primary" :loading="publishing" @click="submitPost">发布</el-button>
    </template>
  </el-dialog>
</template>

<style scoped lang="scss">
.head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--sp-4);
  margin-bottom: var(--sp-6);
}

.boards {
  display: flex;
  flex-wrap: wrap;
  gap: var(--sp-2);
  margin-bottom: var(--sp-4);

  &__pill {
    padding: 6px 16px;
    border: 1px solid var(--line);
    border-radius: 999px;
    background: var(--card);
    font-size: 0.88rem;
    color: var(--muted);
    cursor: pointer;
    transition: all 0.2s ease;

    &:hover {
      border-color: var(--mark);
      color: var(--mark);
    }

    &.is-active {
      background: var(--ink);
      border-color: var(--ink);
      color: #fff;
    }
  }
}

.toolbar {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  margin-bottom: var(--sp-5);

  &__search {
    width: 300px;
  }

  &__select {
    width: 140px;
  }
}

.list {
  min-height: 240px;
}

.post {
  display: flex;
  align-items: center;
  gap: var(--sp-5);
  padding: var(--sp-4) 0;
  border-bottom: 1px solid var(--line);
  cursor: pointer;

  &:hover .post__title {
    color: var(--mark);
  }

  &__main {
    flex: 1;
    min-width: 0;
  }

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
    padding: 1px 10px;
    white-space: nowrap;
  }

  &__title {
    font-size: 1rem;
    font-weight: 500;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    transition: color 0.2s ease;
  }

  &__excerpt {
    margin-top: var(--sp-2);
    font-size: 0.88rem;
    color: var(--muted);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__meta {
    display: flex;
    gap: var(--sp-4);
    margin-top: var(--sp-2);
    font-size: 0.8rem;
    color: var(--muted);
  }

  &__stats {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: var(--sp-2);
    flex-shrink: 0;
  }

  &__stat {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    font-size: 0.82rem;
    color: var(--muted);
    font-variant-numeric: tabular-nums;
  }
}

.empty {
  text-align: center;
  padding: var(--sp-8) 0;
  color: var(--muted);
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: var(--sp-6);
}

@media (max-width: 720px) {
  .post {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--sp-3);
  }

  .post__stats {
    flex-direction: row;
    gap: var(--sp-4);
  }

  .toolbar__search {
    width: 100%;
  }
}
</style>
