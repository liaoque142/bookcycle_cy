<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, ChatDotRound } from '@element-plus/icons-vue'
import {
  searchUsers,
  fetchFriends,
  fetchFriendDetail,
  sendFriendRequest,
  fetchFriendRequests,
  handleFriendRequest,
  deleteFriend,
  fetchBlacklist,
  addBlacklist,
  removeBlacklist,
} from '@/api/social'
import { sendMessage, fetchHistory, markPeerRead, fetchMessageUnreadSummary } from '@/api/message'
import { fetchFriendFavorites } from '@/api/favorite'
import { USE_MOCK } from '@/api/mock'
import { fetchCategories, type CategoryOption } from '@/api/category'
import ImageUploader from '@/components/common/ImageUploader.vue'
import Avatar from '@/components/common/Avatar.vue'
import BookCover from '@/components/common/BookCover.vue'
import type { UserBrief, Friend, FriendDetail, FriendRequestItem, BlacklistItem } from '@/types/social'
import type { MessageItem } from '@/types/message'
import type { Book } from '@/types/book'
import { useMessageStore } from '@/stores/message'
import { useAuthStore } from '@/stores/auth'

type TabKey = 'friends' | 'requests' | 'blacklist'

const route = useRoute()
const router = useRouter()
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

// 好友信息卡片
const detailVisible = ref(false)
const currentDetail = ref<FriendDetail | null>(null)
const friendFavs = ref<Book[]>([])
const favLoading = ref(false)
const categories = ref<CategoryOption[]>([])
const detailGenres = computed(() => {
  const ids = new Set(currentDetail.value?.genreIds ?? [])
  return categories.value.filter((c) => ids.has(c.id))
})

// 好友私信
const messageStore = useMessageStore()
const authStore = useAuthStore()
const meId = computed(() => authStore.user?.id)
const unreadMap = ref<Record<number, number>>({})
const drawerOpen = ref(false)
const activeFriend = ref<Friend | null>(null)
const messages = ref<MessageItem[]>([])
const draft = ref('')
const draftImage = ref('')
const sending = ref(false)
const msgLoading = ref(false)
const msgListEl = ref<HTMLElement>()
let pollTimer: ReturnType<typeof setInterval> | undefined

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
  refreshUnread()
  // 消息中心「去聊天」跳转：定位到指定好友并自动打开抽屉
  const chatTo = Number(route.query.chatTo)
  if (chatTo) {
    router.replace({ path: '/friends' })
    const f = friends.value.find((x) => x.userId === chatTo)
    if (f) openChat(f)
    else ElMessage.warning('该用户已不是好友，无法打开聊天')
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
    ElMessage.success('已解除拉黑，好友关系已恢复')
    load()
  } catch (e) {
    ElMessage.error((e as Error).message || '操作失败')
  }
}

// ---------- 好友信息卡片 ----------
async function openDetail(f: Friend) {
  try {
    favLoading.value = true
    const [detail, fav] = await Promise.all([
      fetchFriendDetail(f.userId),
      fetchFriendFavorites(f.userId),
    ])
    // 后端详情返回 UserVO（无 createTime/userId），归一化为 FriendDetail 形状：
    // createTime = 好友列表行的「成为好友时间」，userId = 详情 id
    currentDetail.value = { ...detail, userId: detail.id, createTime: f.createTime }
    friendFavs.value = fav.list
    detailVisible.value = true
  } catch (e) {
    ElMessage.error((e as Error).message || '获取好友信息失败')
  } finally {
    favLoading.value = false
  }
}

function goChatFromDetail() {
  if (!currentDetail.value) return
  detailVisible.value = false
  openChat(currentDetail.value)
}

// ---------- 好友私信 ----------
async function refreshUnread() {
  try {
    const list = await fetchMessageUnreadSummary()
    unreadMap.value = {}
    for (const p of list) unreadMap.value[p.userId] = p.unreadCount
    messageStore.refresh()
  } catch {
    /* 非关键，忽略 */
  }
}

function unreadOf(f: Friend) {
  return unreadMap.value[f.userId] || 0
}

function scrollToBottom() {
  nextTick(() => {
    if (msgListEl.value) msgListEl.value.scrollTop = msgListEl.value.scrollHeight
  })
}

async function openChat(f: Friend) {
  activeFriend.value = f
  drawerOpen.value = true
  msgLoading.value = true
  try {
    messages.value = await fetchHistory(f.userId)
    // 打开即视为已读
    await markPeerRead(f.userId)
    delete unreadMap.value[f.userId]
    messageStore.refresh()
    scrollToBottom()
  } finally {
    msgLoading.value = false
  }
  startPoll()
}

function stopPoll() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = undefined
  }
}

function startPoll() {
  stopPoll()
  if (USE_MOCK) return // mock 无后端：fetchHistory 返回空列表，轮询会把本地刚发的消息清空
  pollTimer = setInterval(async () => {
    const peer = activeFriend.value
    if (!peer || !drawerOpen.value) return
    try {
      const list = await fetchHistory(peer.userId)
      if (list.length !== messages.value.length) {
        messages.value = list
        scrollToBottom()
      }
      // 正在看会话，对方新消息自动标记已读
      await markPeerRead(peer.userId)
      messageStore.refresh()
    } catch {
      /* 非关键，忽略 */
    }
  }, 5000)
}

function onDrawerClose() {
  stopPoll()
  activeFriend.value = null
  messages.value = []
  draft.value = ''
  draftImage.value = ''
  refreshUnread()
}

async function onChatSend() {
  const peer = activeFriend.value
  const content = draft.value.trim()
  const imageUrl = draftImage.value
  if (!peer || (!content && !imageUrl)) return
  sending.value = true
  try {
    await sendMessage({ toUserId: peer.userId, content, imageUrl })
    messages.value.push({
      id: Date.now(),
      senderId: meId.value!,
      receiverId: peer.userId,
      content,
      imageUrl,
      isRead: 1,
      createTime: new Date().toISOString(),
    })
    draft.value = ''
    draftImage.value = ''
    scrollToBottom()
  } catch (e) {
    ElMessage.error((e as Error).message || '发送失败')
  } finally {
    sending.value = false
  }
}

const formatTime = (t: string) => t.slice(11, 16)

const statusLabel = (s: number) => (s === 0 ? '待处理' : s === 1 ? '已同意' : '已拒绝')

onMounted(() => {
  load()
  // 书籍喜好多选选项（8 大分类），供好友信息卡片展示
  fetchCategories().then((cs) => (categories.value = cs)).catch(() => {})
})
onUnmounted(stopPoll)
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
            <Avatar :src="u.avatar" :name="u.nickname || u.username || ''" :size="40" />
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
              <div class="row__click" @click="openDetail(f)">
                <Avatar :src="f.avatar" :name="f.nickname || f.username || ''" :size="40" />
                <div class="row__info">
                  <p class="row__name">
                    {{ f.nickname || f.username }}
                    <span v-if="unreadOf(f)" class="row__msg-badge">{{ unreadOf(f) }}</span>
                  </p>
                  <p class="row__meta">@{{ f.username }} · 信誉 {{ f.credit }}</p>
                </div>
              </div>
              <div class="row__actions">
                <el-button size="small" round :icon="ChatDotRound" type="primary" @click="openChat(f)">发消息</el-button>
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
              <Avatar :src="r.fromAvatar" :name="r.fromNickname || r.fromUsername || ''" :size="40" />
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
              <Avatar :src="b.avatar" :name="b.nickname || b.username || ''" :size="40" muted />
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

      <!-- 好友聊天抽屉 -->
      <el-drawer
        v-model="drawerOpen"
        :title="activeFriend ? `与 ${activeFriend.nickname || activeFriend.username} 的对话` : ''"
        size="420px"
        :with-header="true"
        @close="onDrawerClose"
      >
        <div ref="msgListEl" v-loading="msgLoading" class="chat">
          <div v-if="messages.length" class="chat__body">
            <div
              v-for="m in messages"
              :key="m.id"
              class="chat__row"
              :class="{ 'chat__row--me': m.senderId === meId }"
            >
              <div class="chat__bubble" :class="{ 'chat__bubble--me': m.senderId === meId }">
                <img v-if="m.imageUrl" :src="m.imageUrl" class="chat__img" alt="图片" />
                <p v-if="m.content" class="chat__text">{{ m.content }}</p>
              </div>
              <span class="chat__time">{{ formatTime(m.createTime) }}</span>
            </div>
          </div>
          <div v-else-if="!msgLoading" class="chat__empty">开始和 TA 打个招呼吧</div>
        </div>
        <div class="chat__input">
          <el-input
            v-model="draft"
            type="textarea"
            :rows="2"
            maxlength="500"
            show-word-limit
            placeholder="输入消息…"
            resize="none"
            @keydown.enter.exact.prevent="onChatSend"
          />
          <div class="chat__input-foot">
            <ImageUploader v-model="draftImage" />
            <el-button type="primary" :loading="sending" @click="onChatSend">发送</el-button>
          </div>
        </div>
      </el-drawer>

      <!-- 好友信息卡片 -->
      <el-dialog v-model="detailVisible" width="420px" align-center>
        <div v-if="currentDetail" class="card">
          <div class="card__head">
            <Avatar :src="currentDetail.avatar" :name="currentDetail.nickname || currentDetail.username || ''" :size="56" />
            <div class="card__info">
              <p class="card__name">{{ currentDetail.nickname || currentDetail.username }}</p>
              <p class="card__meta">@{{ currentDetail.username }} · 信誉 {{ currentDetail.credit }}</p>
            </div>
          </div>
          <div class="card__body">
            <div class="card__row">
              <span class="card__label">简介</span>
              <span class="card__value">{{ currentDetail.bio || '这个人很懒，什么都没写' }}</span>
            </div>
            <div class="card__row">
              <span class="card__label">书籍喜好</span>
              <span class="card__value">
                <template v-if="detailGenres.length">
                  <el-tag v-for="g in detailGenres" :key="g.id" size="small" effect="plain" round>{{ g.name }}</el-tag>
                </template>
                <template v-else>未设置</template>
              </span>
            </div>
            <div class="card__row card__row--top">
              <span class="card__label">喜欢书籍</span>
              <span class="card__value">
                <div v-if="friendFavs.length" v-loading="favLoading" class="card__favs">
                  <router-link
                    v-for="b in friendFavs"
                    :key="b.id"
                    :to="`/books/${b.id}`"
                    class="card__fav"
                    @click="detailVisible = false"
                  >
                    <BookCover :book="b" height="72px" />
                    <span class="card__fav-title">{{ b.title }}</span>
                  </router-link>
                </div>
                <template v-else>还没有收藏书籍</template>
              </span>
            </div>
            <div class="card__row">
              <span class="card__label">成为好友</span>
              <span class="card__value">{{ currentDetail.createTime.slice(0, 10) }}</span>
            </div>
          </div>
          <div class="card__foot">
            <el-button type="primary" round :icon="ChatDotRound" @click="goChatFromDetail">发消息</el-button>
            <el-button round @click="detailVisible = false">关闭</el-button>
          </div>
        </div>
      </el-dialog>
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

  &__click {
    display: flex;
    align-items: center;
    gap: var(--sp-3);
    flex: 1;
    min-width: 0;
    border-radius: 8px;
    cursor: pointer;
    transition: background 0.2s ease;

    &:hover {
      background: var(--paper);
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

  &__msg-badge {
    display: inline-grid;
    place-items: center;
    min-width: 16px;
    height: 16px;
    margin-left: 6px;
    padding: 0 4px;
    border-radius: 999px;
    background: #d9534f;
    color: #fff;
    font-size: 0.72rem;
    vertical-align: 2px;
    font-variant-numeric: tabular-nums;
  }
}

.chat {
  height: min(56vh, 420px);
  overflow-y: auto;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  padding: var(--sp-3);
  background: var(--paper);

  &__body {
    display: flex;
    flex-direction: column;
    gap: var(--sp-2);
  }

  &__row {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    max-width: 80%;

    &--me {
      align-self: flex-end;
      align-items: flex-end;
    }
  }

  &__bubble {
    padding: 8px 12px;
    border-radius: 12px;
    background: var(--card);
    border: 1px solid var(--line);
    font-size: 0.9rem;
    line-height: 1.5;
    word-break: break-word;

    &--me {
      background: var(--ink);
      border-color: var(--ink);
      color: #fff;

      .chat__img {
        border: 1px solid rgba(255, 255, 255, 0.15);
      }
    }
  }

  &__img {
    max-width: 180px;
    max-height: 240px;
    display: block;
    border-radius: 8px;
    border: 1px solid var(--line);
    object-fit: contain;
  }

  &__text {
    margin: 0;
    white-space: pre-wrap;
    word-break: break-word;
  }

  &__time {
    margin-top: 2px;
    font-size: 0.72rem;
    color: var(--muted);
  }

  &__empty {
    text-align: center;
    padding: var(--sp-6) 0;
    color: var(--muted);
  }
}

.chat__input {
  margin-top: var(--sp-3);

  &-foot {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: var(--sp-2);
    margin-top: var(--sp-2);

    .el-button {
      flex-shrink: 0;
    }
  }
}

.card {
  &__head {
    display: flex;
    align-items: center;
    gap: var(--sp-3);
  }

  &__info {
    min-width: 0;
  }

  &__name {
    font-family: var(--font-serif);
    font-size: 1.1rem;
    font-weight: 700;
  }

  &__meta {
    margin-top: 2px;
    font-size: 0.8rem;
    color: var(--muted);
  }

  &__body {
    display: flex;
    flex-direction: column;
    gap: var(--sp-2);
    margin-top: var(--sp-5);
    padding: var(--sp-3) var(--sp-4);
    background: var(--paper);
    border: 1px solid var(--line);
    border-radius: var(--radius);
  }

  &__row {
    display: flex;
    align-items: baseline;
    gap: var(--sp-3);
    font-size: 0.9rem;

    &--top {
      align-items: flex-start;
    }
  }

  &__label {
    flex-shrink: 0;
    width: 72px;
    font-size: 0.82rem;
    color: var(--muted);
  }

  &__value {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    line-height: 1.5;
    word-break: break-word;
  }

  &__favs {
    width: 100%;
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: var(--sp-2);
  }

  &__fav {
    min-width: 0;
    color: var(--ink);

    &:hover .card__fav-title {
      color: var(--mark);
    }

    &-title {
      display: block;
      margin-top: 4px;
      font-size: 0.72rem;
      line-height: 1.3;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  &__foot {
    display: flex;
    justify-content: flex-end;
    gap: var(--sp-2);
    margin-top: var(--sp-5);
  }
}

@media (max-width: 620px) {
  .head__search {
    width: 100%;
  }
}
</style>
