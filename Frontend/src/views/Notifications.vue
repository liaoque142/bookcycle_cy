<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Bell, Check } from '@element-plus/icons-vue'
import { fetchNotifications, markAllNotificationsRead, markNotificationRead } from '@/api/notification'
import type { NotificationItem } from '@/types/notification'

const list = ref<NotificationItem[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const filter = ref<'all' | 'unread'>('all')
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const result = await fetchNotifications({
      unreadOnly: filter.value === 'unread' ? true : undefined,
      page: page.value,
      size: size.value,
    })
    list.value = result.list
    total.value = result.total
  } finally {
    loading.value = false
  }
}

async function onSwitchFilter(f: 'all' | 'unread') {
  filter.value = f
  page.value = 1
  load()
}

function onPageChange(p: number) {
  page.value = p
  load()
}

async function onRead(n: NotificationItem) {
  if (n.isRead) return
  try {
    await markNotificationRead(n.id)
    n.isRead = 1
  } catch (e) {
    ElMessage.error((e as Error).message || '操作失败')
  }
}

async function onReadAll() {
  try {
    await markAllNotificationsRead()
    list.value.forEach((n) => (n.isRead = 1))
    ElMessage.success('已全部标记为已读')
  } catch (e) {
    ElMessage.error((e as Error).message || '操作失败')
  }
}

onMounted(load)
</script>

<template>
  <section class="section">
    <div class="container">
      <div class="page">
        <div class="head">
          <div>
            <h1 class="page-title">消息通知</h1>
            <p class="page-sub">交易与好友消息都会推送到这里</p>
          </div>
          <el-button round :icon="Check" :disabled="filter === 'unread' && list.length === 0" @click="onReadAll">
            全部已读
          </el-button>
        </div>

        <div class="tabs">
          <button class="tabs__pill" :class="{ 'is-active': filter === 'all' }" @click="onSwitchFilter('all')">全部</button>
          <button class="tabs__pill" :class="{ 'is-active': filter === 'unread' }" @click="onSwitchFilter('unread')">未读</button>
        </div>

        <div v-loading="loading" class="list">
          <div v-if="list.length" class="items">
            <div
              v-for="n in list"
              :key="n.id"
              class="item"
              :class="{ 'item--unread': !n.isRead }"
              @click="onRead(n)"
            >
              <span class="item__dot" :class="`item__dot--type${n.type}`">
                <el-icon><Bell /></el-icon>
              </span>
              <div class="item__main">
                <div class="item__top">
                  <span class="item__type">{{ n.typeName }}</span>
                  <span class="item__time">{{ n.createTime }}</span>
                </div>
                <p class="item__content">{{ n.content }}</p>
              </div>
              <span v-if="!n.isRead" class="item__unread" />
            </div>
          </div>
          <div v-else-if="!loading" class="list__empty">
            {{ filter === 'unread' ? '没有未读通知' : '还没有通知' }}
          </div>
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

.tabs {
  display: flex;
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
  align-items: flex-start;
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

  &__dot {
    display: grid;
    place-items: center;
    width: 36px;
    height: 36px;
    flex-shrink: 0;
    border-radius: 50%;
    color: #fff;

    &--type1 {
      background: #8a8f98; // 系统
    }
    &--type2 {
      background: var(--mark); // 交易
    }
    &--type3 {
      background: #4a7dbd; // 好友
    }
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

  &__type {
    font-size: 0.78rem;
    color: var(--mark);
    border: 1px solid rgba(176, 137, 78, 0.35);
    border-radius: 999px;
    padding: 1px 10px;
  }

  &__time {
    font-size: 0.78rem;
    color: var(--muted);
  }

  &__content {
    margin-top: var(--sp-1);
    font-size: 0.92rem;
    line-height: 1.6;
    color: var(--muted);
  }

  &__unread {
    width: 8px;
    height: 8px;
    flex-shrink: 0;
    border-radius: 50%;
    background: #d9534f;
    margin-top: 10px;
  }

  &--unread {
    .item__content {
      color: var(--ink);
    }
  }
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: var(--sp-5);
}
</style>
