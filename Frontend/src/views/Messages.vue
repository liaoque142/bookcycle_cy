<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ChatDotRound } from '@element-plus/icons-vue'
import Avatar from '@/components/common/Avatar.vue'
import { fetchInbox, markMessageRead } from '@/api/message'
import { useMessageStore } from '@/stores/message'
import type { MessageItem } from '@/types/message'

const router = useRouter()
const messageStore = useMessageStore()

const list = ref<MessageItem[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const loading = ref(false)

const current = ref<MessageItem | null>(null)
const cardVisible = ref(false)

async function load() {
  loading.value = true
  try {
    const result = await fetchInbox({ page: page.value, size: size.value })
    list.value = result.list
    total.value = result.total
  } finally {
    loading.value = false
  }
}

function onPageChange(p: number) {
  page.value = p
  load()
}

/** 完整时间带日期：2026-08-20T21:04:05 -> 2026-08-20 21:04 */
function formatTime(t: string) {
  return t ? t.slice(0, 16).replace('T', ' ') : ''
}

function preview(m: MessageItem) {
  if (m.content) return m.content.length > 50 ? m.content.slice(0, 50) + '…' : m.content
  return m.imageUrl ? '图片消息' : ''
}

/** 点击消息：弹信息卡片 + 未读则标记单条已读 + 刷新红点（点一条减一条，仍>0 保持） */
async function openCard(m: MessageItem) {
  current.value = m
  cardVisible.value = true
  if (!m.isRead) {
    try {
      await markMessageRead(m.id)
      m.isRead = 1
      messageStore.refresh()
    } catch {
      /* 标记失败不阻断卡片展示 */
    }
  }
}

function goChat() {
  if (!current.value) return
  cardVisible.value = false
  router.push({ path: '/friends', query: { chatTo: current.value.senderId } })
}

onMounted(load)
</script>

<template>
  <section class="section">
    <div class="container">
      <div class="page">
        <div class="head">
          <div>
            <h1 class="page-title">消息中心</h1>
            <p class="page-sub">好友私信都汇聚在这里，点击消息查看详情并标记已读</p>
          </div>
        </div>

        <div v-loading="loading" class="list">
          <div v-if="list.length" class="items">
            <div
              v-for="m in list"
              :key="m.id"
              class="item"
              :class="{ 'item--unread': !m.isRead }"
              @click="openCard(m)"
            >
              <Avatar :src="m.senderAvatar" :name="m.senderName || ''" :size="40" />
              <div class="item__main">
                <div class="item__top">
                  <span class="item__name">{{ m.senderName || '书友' }}</span>
                  <span class="item__time">{{ formatTime(m.createTime) }}</span>
                </div>
                <p class="item__content">{{ preview(m) }}</p>
              </div>
              <img v-if="m.imageUrl" :src="m.imageUrl" class="item__thumb" alt="图片" />
              <span v-if="!m.isRead" class="item__unread" />
            </div>
          </div>
          <div v-else-if="!loading" class="list__empty">还没有收到私信</div>
        </div>

        <div v-if="total > size" class="pager">
          <el-pagination
            v-model:current-page="page"
            :page-size="size"
            :total="total"
            layout="prev, pager, next"
            background
            @current-change="onPageChange"
          />
        </div>

        <el-dialog v-model="cardVisible" width="420px" align-center>
          <div v-if="current" class="card">
            <div class="card__head">
              <Avatar :src="current.senderAvatar" :name="current.senderName || ''" :size="56" />
              <div class="card__info">
                <p class="card__name">{{ current.senderName || '书友' }}</p>
                <p class="card__time">{{ formatTime(current.createTime) }}</p>
              </div>
            </div>
            <div class="card__body">
              <img v-if="current.imageUrl" :src="current.imageUrl" class="card__img" alt="图片" />
              <p v-if="current.content" class="card__content">{{ current.content }}</p>
            </div>
            <div class="card__foot">
              <el-button type="primary" round :icon="ChatDotRound" @click="goChat">去聊天</el-button>
              <el-button round @click="cardVisible = false">关闭</el-button>
            </div>
          </div>
        </el-dialog>
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
.page {
  max-width: 760px;
  margin: 0 auto;
}

.head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: var(--sp-4);
  margin-bottom: var(--sp-5);
}

.page-title {
  font-family: var(--font-serif);
  font-size: 1.5rem;
  font-weight: 700;
}

.page-sub {
  margin-top: 4px;
  font-size: 0.85rem;
  color: var(--muted);
}

.list {
  min-height: 160px;
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  overflow: hidden;

  &__empty {
    text-align: center;
    padding: var(--sp-8) 0;
    color: var(--muted);
  }
}

.item {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  padding: var(--sp-4) var(--sp-5);
  border-bottom: 1px solid var(--line);
  cursor: pointer;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: var(--paper);
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

  &__name {
    font-size: 0.95rem;
    font-weight: 600;
  }

  &__time {
    font-size: 0.78rem;
    color: var(--muted);
  }

  &__content {
    margin-top: 2px;
    font-size: 0.88rem;
    color: var(--muted);
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }

  &__thumb {
    width: 44px;
    height: 44px;
    flex-shrink: 0;
    border-radius: var(--radius-sm);
    object-fit: cover;
    border: 1px solid var(--line);
  }

  &__unread {
    width: 8px;
    height: 8px;
    flex-shrink: 0;
    border-radius: 50%;
    background: #d9534f;
  }

  &--unread {
    .item__content {
      color: var(--ink);
    }

    .item__name {
      color: var(--ink);
    }
  }
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: var(--sp-5);
}

.card {
  &__head {
    display: flex;
    align-items: center;
    gap: var(--sp-4);
  }

  &__info {
    min-width: 0;
  }

  &__name {
    font-family: var(--font-serif);
    font-size: 1.1rem;
    font-weight: 700;
  }

  &__time {
    margin-top: 2px;
    font-size: 0.82rem;
    color: var(--muted);
  }

  &__body {
    margin-top: var(--sp-5);
    padding: var(--sp-4);
    background: var(--paper);
    border: 1px solid var(--line);
    border-radius: var(--radius-sm);
  }

  &__img {
    max-width: 100%;
    max-height: 320px;
    border-radius: var(--radius-sm);
    display: block;
  }

  &__content {
    margin-top: var(--sp-3);
    font-size: 0.95rem;
    line-height: 1.7;
    white-space: pre-wrap;
    word-break: break-word;
  }

  &__foot {
    display: flex;
    justify-content: flex-end;
    gap: var(--sp-3);
    margin-top: var(--sp-5);
  }
}
</style>
