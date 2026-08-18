<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import {
  searchUsers,
  fetchFriends,
  sendFriendRequest,
  fetchFriendRequests,
  handleFriendRequest,
  deleteFriend,
  fetchBlacklist,
  addBlacklist,
  removeBlacklist,
} from '@/api/social'
import type { UserBrief, Friend, FriendRequestItem, BlacklistItem } from '@/types/social'

type TabKey = 'friends' | 'requests' | 'blacklist'

const tab = ref<TabKey>('friends')
const friends = ref<Friend[]>([])
const requests = ref<FriendRequestItem[]>([])
const blacklist = ref<BlacklistItem[]>([])
const loading = ref(false)

// 搜索添加
const keyword = ref('')
const results = ref<UserBrief[]>([])
const searching = ref(false)

const pendingCount = computed(() => requests.value.filter((r) => r.status === 0).length)
const friendIds = computed(() => new Set(friends.value.map((f) => f.userId)))
const blackIds = computed(() => new Set(blacklist.value.map((b) => b.userId)))

async function load() {
  loading.value = true
  try {
    const [f, r, b] = await Promise.all([fetchFriends(), fetchFriendRequests(), fetchBlacklist()])
    friends.value = f
    requests.value = r
    blacklist.value = b
  } finally {
    loading.value = false
  }
}

function isFriend(id: number) {
  return friendIds.value.has(id)
}

function isBlacked(id: number) {
  return blackIds.value.has(id)
}

async function onSearch() {
  const kw = keyword.value.trim()
  if (!kw) {
    results.value = []
    return
  }
  searching.value = true
  try {
    results.value = await searchUsers(kw)
  } finally {
    searching.value = false
  }
}

async function onSend(u: UserBrief) {
  try {
    await sendFriendRequest(u.id)
    ElMessage.success(`已向「${u.nickname}」发送好友申请`)
  } catch (e) {
    ElMessage.error((e as Error).message || '发送失败')
  }
}

async function onHandle(r: FriendRequestItem, action: 'accept' | 'reject') {
  try {
    await handleFriendRequest(r.id, action)
    ElMessage.success(action === 'accept' ? '已同意，你们现在是好友' : '已拒绝该申请')
    load()
  } catch (e) {
    ElMessage.error((e as Error).message || '操作失败')
  }
}

async function onDeleteFriend(f: Friend) {
  try {
    await ElMessageBox.confirm(`确定删除好友「${f.nickname}」吗？`, '删除好友', { type: 'warning' })
  } catch {
    return
  }
  try {
    await deleteFriend(f.userId)
    ElMessage.success('已删除好友')
    load()
  } catch (e) {
    ElMessage.error((e as Error).message || '删除失败')
  }
}

async function onBlacklist(u: UserBrief) {
  try {
    await ElMessageBox.confirm(
      `确定拉黑「${u.nickname}」吗？拉黑后双方无法发送好友申请与交易。`,
      '拉黑用户',
      { type: 'warning' },
    )
  } catch {
    return
  }
  try {
    await addBlacklist(u.id)
    ElMessage.success('已拉黑')
    load()
  } catch (e) {
    ElMessage.error((e as Error).message || '拉黑失败')
  }
}

async function onUnblack(b: BlacklistItem) {
  try {
    await removeBlacklist(b.userId)
    ElMessage.success('已解除拉黑')
    load()
  } catch (e) {
    ElMessage.error((e as Error).message || '操作失败')
  }
}

const statusLabel = (s: number) => (s === 0 ? '待处理' : s === 1 ? '已同意' : '已拒绝')

onMounted(load)
</script>

<template>
  <section class="section">
    <div class="container">
      <div class="page">
        <div class="head">
          <div>
            <h1 class="page-title">我的好友</h1>
            <p class="page-sub">加好友一起读书 · 拉黑后双方无法互动</p>
          </div>

          <!-- 搜索添加 -->
          <div class="head__search">
            <el-input
              v-model="keyword"
              placeholder="搜索用户名 / 昵称"
              :prefix-icon="Search"
              clearable
              @keyup.enter="onSearch"
              @clear="results = []"
            />
            <el-button type="primary" round :loading="searching" @click="onSearch">查找</el-button>
          </div>
        </div>

        <!-- 搜索结果 -->
        <div v-if="results.length" class="search">
          <div v-for="u in results" :key="u.id" class="search__item">
            <span class="search__avatar">{{ (u.nickname || u.username).slice(0, 1) }}</span>
            <div class="search__info">
              <p class="search__name">{{ u.nickname || u.username }}</p>
              <p class="search__meta">信誉 {{ u.credit }}</p>
            </div>
            <div class="search__actions">
              <el-button
                v-if="!isFriend(u.id)"
                size="small"
                type="primary"
                round
                :disabled="isBlacked(u.id)"
                @click="onSend(u)"
              >
                {{ isBlacked(u.id) ? '已拉黑' : '加好友' }}
              </el-button>
              <span v-else class="search__done">已是好友</span>
              <el-button size="small" round text @click="onBlacklist(u)">拉黑</el-button>
            </div>
          </div>
        </div>

        <!-- Tab -->
        <div class="tabs">
          <button class="tabs__pill" :class="{ 'is-active': tab === 'friends' }" @click="tab = 'friends'">
            好友 <em v-if="friends.length">{{ friends.length }}</em>
          </button>
          <button class="tabs__pill" :class="{ 'is-active': tab === 'requests' }" @click="tab = 'requests'">
            申请 <em v-if="pendingCount" class="tabs__badge">{{ pendingCount }}</em>
          </button>
          <button class="tabs__pill" :class="{ 'is-active': tab === 'blacklist' }" @click="tab = 'blacklist'">
            黑名单 <em v-if="blacklist.length">{{ blacklist.length }}</em>
          </button>
        </div>

        <!-- 好友列表 -->
        <div v-loading="loading" v-show="tab === 'friends'" class="panel">
          <div v-if="friends.length" class="rows">
            <div v-for="f in friends" :key="f.id" class="row">
              <span class="row__avatar">{{ (f.nickname || f.username).slice(0, 1) }}</span>
              <div class="row__info">
                <p class="row__name">{{ f.nickname || f.username }}</p>
                <p class="row__meta">@{{ f.username }} · 信誉 {{ f.credit }}</p>
              </div>
              <div class="row__actions">
                <el-button size="small" round text @click="onBlacklist({ id: f.userId, username: f.username, nickname: f.nickname, credit: f.credit })">
                  拉黑
                </el-button>
                <el-button size="small" round text type="danger" @click="onDeleteFriend(f)">删除</el-button>
              </div>
            </div>
          </div>
          <div v-else-if="!loading" class="panel__empty">还没有好友，去上方搜索添加吧</div>
        </div>

        <!-- 申请列表 -->
        <div v-loading="loading" v-show="tab === 'requests'" class="panel">
          <div v-if="requests.length" class="rows">
            <div v-for="r in requests" :key="r.id" class="row">
              <span class="row__avatar">{{ (r.fromNickname || r.fromUsername).slice(0, 1) }}</span>
              <div class="row__info">
                <p class="row__name">{{ r.fromNickname || r.fromUsername }}</p>
                <p class="row__meta">@{{ r.fromUsername }} · 信誉 {{ r.fromCredit }} · {{ r.createTime.slice(0, 10) }}</p>
              </div>
              <div class="row__actions">
                <template v-if="r.status === 0">
                  <el-button size="small" type="primary" round @click="onHandle(r, 'accept')">同意</el-button>
                  <el-button size="small" round @click="onHandle(r, 'reject')">拒绝</el-button>
                </template>
                <span v-else class="row__status">{{ statusLabel(r.status) }}</span>
              </div>
            </div>
          </div>
          <div v-else-if="!loading" class="panel__empty">暂无好友申请</div>
        </div>

        <!-- 黑名单列表 -->
        <div v-loading="loading" v-show="tab === 'blacklist'" class="panel">
          <div v-if="blacklist.length" class="rows">
            <div v-for="b in blacklist" :key="b.id" class="row">
              <span class="row__avatar row__avatar--muted">{{ (b.nickname || b.username).slice(0, 1) }}</span>
              <div class="row__info">
                <p class="row__name">{{ b.nickname || b.username }}</p>
                <p class="row__meta">@{{ b.username }} · {{ b.createTime.slice(0, 10) }}</p>
              </div>
              <el-button size="small" round @click="onUnblack(b)">解除拉黑</el-button>
            </div>
          </div>
          <div v-else-if="!loading" class="panel__empty">黑名单为空</div>
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
  flex-wrap: wrap;
  margin-bottom: var(--sp-5);

  &__search {
    display: flex;
    gap: var(--sp-2);
    width: 340px;
    max-width: 100%;
  }
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

.search {
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  margin-bottom: var(--sp-5);
  overflow: hidden;

  &__item {
    display: flex;
    align-items: center;
    gap: var(--sp-3);
    padding: var(--sp-3) var(--sp-5);
    border-bottom: 1px solid var(--line);

    &:last-child {
      border-bottom: none;
    }
  }

  &__avatar {
    display: grid;
    place-items: center;
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background: var(--mark);
    color: #fff;
    font-family: var(--font-serif);
  }

  &__info {
    flex: 1;
    min-width: 0;
  }

  &__name {
    font-weight: 600;
  }

  &__meta {
    font-size: 0.8rem;
    color: var(--muted);
  }

  &__actions {
    display: flex;
    align-items: center;
    gap: var(--sp-1);
    flex-shrink: 0;
  }

  &__done {
    font-size: 0.85rem;
    color: var(--muted);
  }
}

.tabs {
  display: flex;
  gap: var(--sp-2);
  margin-bottom: var(--sp-4);

  &__pill {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 6px 16px;
    border: 1px solid var(--line);
    border-radius: 999px;
    background: var(--card);
    font-size: 0.88rem;
    color: var(--muted);
    cursor: pointer;
    transition: all 0.2s ease;

    em {
      font-style: normal;
      font-size: 0.78rem;
      color: var(--muted);
    }

    &:hover {
      border-color: var(--mark);
      color: var(--mark);
    }

    &.is-active {
      background: var(--ink);
      border-color: var(--ink);
      color: #fff;

      em {
        color: #fff;
        opacity: 0.8;
      }
    }
  }

  &__badge {
    display: inline-grid;
    place-items: center;
    min-width: 16px;
    height: 16px;
    padding: 0 4px;
    border-radius: 999px;
    background: #d9534f;
    color: #fff !important;
    font-size: 0.72rem !important;
  }
}

.panel {
  min-height: 120px;
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

.row {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  padding: var(--sp-3) var(--sp-5);
  border-bottom: 1px solid var(--line);

  &:last-child {
    border-bottom: none;
  }

  &__avatar {
    display: grid;
    place-items: center;
    width: 40px;
    height: 40px;
    flex-shrink: 0;
    border-radius: 50%;
    background: var(--mark);
    color: #fff;
    font-family: var(--font-serif);

    &--muted {
      background: #999;
    }
  }

  &__info {
    flex: 1;
    min-width: 0;
  }

  &__name {
    font-weight: 600;
  }

  &__meta {
    font-size: 0.8rem;
    color: var(--muted);
  }

  &__actions {
    display: flex;
    align-items: center;
    gap: var(--sp-1);
    flex-shrink: 0;
  }

  &__status {
    font-size: 0.82rem;
    color: var(--muted);
  }
}

@media (max-width: 620px) {
  .head__search {
    width: 100%;
  }
}
</style>
