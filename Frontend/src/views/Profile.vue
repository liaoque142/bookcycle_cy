<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import BookCard from '@/components/home/BookCard.vue'
import Avatar from '@/components/common/Avatar.vue'
import AvatarUploader from '@/components/common/AvatarUploader.vue'
import { useAuthStore } from '@/stores/auth'
import { fetchBooks } from '@/api/book'
import { fetchCreditMe } from '@/api/credit'
import { updateProfile } from '@/api/user'
import { fetchCategories, type CategoryOption } from '@/api/category'
import type { Book } from '@/types/book'
import type { User } from '@/types/user'

const authStore = useAuthStore()
const user = computed(() => authStore.user)

const myBooks = ref<Book[]>([])
const loading = ref(true)

const menuItems = [
  { name: '我的订单', desc: '买入与卖出的交易', to: '/orders' },
  { name: '我的换书', desc: '以书换书的交换单', to: '/swaps' },
  { name: '收货地址', desc: '管理邮寄地址', to: '/addresses' },
  { name: '我的好友', desc: '好友与黑名单管理', to: '/friends' },
  { name: '消息通知', desc: '交易与好友消息', to: '/notifications' },
  { name: '信誉记录', desc: '查看信誉分变动', to: '/credit' },
]

// ---------- 个人信息 ----------
const editVisible = ref(false)
const saving = ref(false)
const categories = ref<CategoryOption[]>([])
const form = ref<Partial<User>>({})

/** 已保存的书籍喜好分类名（展示用） */
const genreNames = computed(() => {
  const ids = new Set(user.value?.genreIds ?? [])
  return categories.value.filter((c) => ids.has(c.id))
})

function openEdit() {
  form.value = {
    nickname: user.value?.nickname || '',
    bio: user.value?.bio || '',
    avatar: user.value?.avatar || '',
    genreIds: user.value?.genreIds ? [...user.value.genreIds] : [],
  }
  editVisible.value = true
}

async function onSave() {
  saving.value = true
  try {
    const latest = await updateProfile({
      nickname: form.value.nickname,
      bio: form.value.bio,
      avatar: form.value.avatar,
      genreIds: form.value.genreIds,
    })
    authStore.setUser(latest)
    editVisible.value = false
    ElMessage.success('资料已更新')
  } catch (e) {
    ElMessage.error((e as Error).message || '保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  try {
    const result = await fetchBooks({ page: 1, size: 100 })
    // mock 下「书友小明」为新发布书的卖家；对接后端后改为 seller 查询
    myBooks.value = result.list.filter((b) => b.seller === user.value?.nickname)
  } finally {
    loading.value = false
  }
  // 刷新信誉分（交易动作会改变）
  try {
    const credit = await fetchCreditMe()
    if (authStore.user) authStore.user.credit = credit
  } catch {
    /* 非关键，忽略 */
  }
  // 书籍喜好多选选项（8 大分类）
  try {
    categories.value = await fetchCategories()
  } catch {
    /* 非关键，忽略 */
  }
})
</script>

<template>
  <section class="section">
    <div class="container">
      <div class="profile">
        <div class="profile__head">
          <Avatar :src="user?.avatar" :name="user?.nickname || user?.username || ''" :size="64" />
          <div class="profile__info">
            <h1 class="profile__name">{{ user?.nickname || user?.username }}</h1>
            <p class="profile__meta">
              <span class="profile__tag">{{ user?.role === 'admin' ? '管理员' : '书友' }}</span>
              <span>信誉 <b class="profile__credit">{{ user?.credit }}</b></span>
            </p>
          </div>
          <div class="profile__actions">
            <el-button round class="profile__edit" @click="openEdit">编辑资料</el-button>
            <router-link to="/publish" class="profile__publish">+ 发布书籍</router-link>
          </div>
        </div>

        <div class="profile__info-card">
          <h2 class="profile__section-title">个人信息</h2>
          <div class="profile__info-row">
            <span class="profile__info-label">昵称</span>
            <span>{{ user?.nickname || '未设置' }}</span>
          </div>
          <div class="profile__info-row">
            <span class="profile__info-label">用户名</span>
            <span>@{{ user?.username }}</span>
          </div>
          <div class="profile__info-row">
            <span class="profile__info-label">简介</span>
            <span>{{ user?.bio || '这个人很懒，什么都没写' }}</span>
          </div>
          <div class="profile__info-row">
            <span class="profile__info-label">书籍喜好</span>
            <span class="profile__genres">
              <template v-if="genreNames.length">
                <el-tag v-for="g in genreNames" :key="g.id" size="small" effect="plain" round>{{ g.name }}</el-tag>
              </template>
              <template v-else>未设置</template>
            </span>
          </div>
        </div>

        <div class="profile__mine">
          <div class="profile__mine-head">
            <h2 class="profile__mine-title">我的发布</h2>
            <span v-if="!loading" class="profile__mine-count">{{ myBooks.length }} 本</span>
          </div>

          <div v-loading="loading" class="profile__mine-grid">
            <BookCard v-for="book in myBooks" :key="book.id" :book="book" />
          </div>

          <div v-if="!loading && myBooks.length === 0" class="profile__empty">
            <p>还没有发布过书，快去分享第一本吧</p>
            <router-link to="/publish" class="profile__empty-btn">去发布</router-link>
          </div>
        </div>

        <ul class="profile__menu">
          <li v-for="item in menuItems" :key="item.name" class="profile__menu-item">
            <router-link v-if="item.to" :to="item.to" class="profile__menu-link">
              <div>
                <p class="profile__menu-name">{{ item.name }}</p>
                <p class="profile__menu-desc">{{ item.desc }}</p>
              </div>
              <span class="profile__menu-badge">进入</span>
            </router-link>
            <div v-else class="profile__menu-link">
              <div>
                <p class="profile__menu-name">{{ item.name }}</p>
                <p class="profile__menu-desc">{{ item.desc }}</p>
              </div>
              <span class="profile__menu-badge">开发中</span>
            </div>
          </li>
        </ul>

        <el-dialog v-model="editVisible" title="编辑个人资料" width="440px" align-center>
          <div class="edit">
            <div class="edit__avatar">
              <AvatarUploader v-model="form.avatar" :name="form.nickname || user?.username || ''" :size="96" />
            </div>
            <el-form label-position="top">
              <el-form-item label="昵称">
                <el-input v-model="form.nickname" maxlength="50" placeholder="给自己起个昵称" />
              </el-form-item>
              <el-form-item label="简介">
                <el-input v-model="form.bio" type="textarea" :rows="3" maxlength="255" show-word-limit placeholder="介绍一下自己" />
              </el-form-item>
              <el-form-item label="书籍喜好">
                <el-select v-model="form.genreIds" multiple clearable placeholder="选择你喜欢的书籍分类" class="edit__genres">
                  <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
                </el-select>
              </el-form-item>
            </el-form>
          </div>
          <template #footer>
            <el-button round @click="editVisible = false">取消</el-button>
            <el-button type="primary" round :loading="saving" @click="onSave">保存</el-button>
          </template>
        </el-dialog>
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
.profile {
  max-width: 860px;
  margin: 0 auto;
}

.profile__head {
  display: flex;
  align-items: center;
  gap: var(--sp-4);
  padding: var(--sp-6);
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: var(--radius);
}

.profile__actions {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  margin-left: auto;
  flex-shrink: 0;
}

.profile__info-card {
  margin-top: var(--sp-5);
  padding: var(--sp-5) var(--sp-6);
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: var(--radius);
}

.profile__section-title {
  font-family: var(--font-serif);
  font-size: 1.15rem;
  font-weight: 700;
  margin-bottom: var(--sp-4);
}

.profile__info-row {
  display: flex;
  gap: var(--sp-4);
  padding: var(--sp-2) 0;
  font-size: 0.92rem;

  & + & {
    border-top: 1px dashed var(--line);
  }
}

.profile__info-label {
  width: 80px;
  flex-shrink: 0;
  color: var(--muted);
}

.profile__genres {
  display: flex;
  flex-wrap: wrap;
  gap: var(--sp-2);
  min-height: 22px;
}

.edit {
  &__avatar {
    display: flex;
    justify-content: center;
    margin-bottom: var(--sp-5);
  }

  &__genres {
    width: 100%;
  }
}

.profile__name {
  font-family: var(--font-serif);
  font-size: 1.4rem;
  font-weight: 700;
}

.profile__meta {
  margin-top: 4px;
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  font-size: 0.9rem;
  color: var(--muted);
}

.profile__tag {
  padding: 2px 10px;
  border-radius: 999px;
  background: var(--paper);
  border: 1px solid var(--line);
  font-size: 0.8rem;
}

.profile__credit {
  color: var(--mark);
  font-variant-numeric: tabular-nums;
}

.profile__publish {
  padding: 8px 20px;
  border-radius: 999px;
  background: var(--ink);
  color: #fff;
  font-size: 0.9rem;

  &:hover {
    opacity: 0.85;
  }
}

.profile__mine {
  margin-top: var(--sp-6);
}

.profile__mine-head {
  display: flex;
  align-items: baseline;
  gap: var(--sp-3);
  margin-bottom: var(--sp-4);
}

.profile__mine-title {
  font-family: var(--font-serif);
  font-size: 1.3rem;
  font-weight: 700;
}

.profile__mine-count {
  font-size: 0.85rem;
  color: var(--muted);
}

.profile__mine-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: var(--sp-4);
  min-height: 80px;
}

.profile__empty {
  text-align: center;
  padding: var(--sp-7) 0;
  color: var(--muted);

  &-btn {
    display: inline-block;
    margin-top: var(--sp-4);
    padding: 8px 24px;
    border-radius: 999px;
    background: var(--ink);
    color: #fff;
    font-size: 0.9rem;
  }
}

.profile__menu {
  margin-top: var(--sp-6);
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  overflow: hidden;
}

.profile__menu-item {
  border-bottom: 1px solid var(--line);

  &:last-child {
    border-bottom: none;
  }
}

.profile__menu-link {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--sp-3);
  padding: var(--sp-4) var(--sp-5);
  color: inherit;
  cursor: pointer;
  transition: background 0.2s ease;

  &:hover {
    background: var(--paper);
  }
}

.profile__menu-name {
  font-weight: 600;
}

.profile__menu-desc {
  margin-top: 2px;
  font-size: 0.82rem;
  color: var(--muted);
}

.profile__menu-badge {
  padding: 2px 10px;
  border-radius: 999px;
  background: var(--paper);
  border: 1px solid var(--line);
  font-size: 0.78rem;
  color: var(--muted);
}
</style>
