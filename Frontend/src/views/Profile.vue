<script setup lang="ts">
import { ref, onMounted } from 'vue'
import BookCard from '@/components/home/BookCard.vue'
import { useAuthStore } from '@/stores/auth'
import { fetchBooks } from '@/api/book'
import type { Book } from '@/types/book'

const authStore = useAuthStore()
const user = authStore.user

const myBooks = ref<Book[]>([])
const loading = ref(true)

const menuItems = [
  { name: '我的收藏', desc: '收藏的书都在这里' },
  { name: '收货地址', desc: '管理邮寄地址' },
  { name: '信誉记录', desc: '查看信誉分变动' },
  { name: '消息通知', desc: '交易与好友消息' },
]

onMounted(async () => {
  try {
    const result = await fetchBooks({ page: 1, size: 100 })
    // mock 下「书友小明」为新发布书的卖家；对接后端后改为 seller 查询
    myBooks.value = result.list.filter((b) => b.seller === user?.nickname)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <section class="section">
    <div class="container">
      <div class="profile">
        <div class="profile__head">
          <span class="profile__avatar">{{ user?.nickname?.slice(0, 1) || user?.username.slice(0, 1) }}</span>
          <div class="profile__info">
            <h1 class="profile__name">{{ user?.nickname || user?.username }}</h1>
            <p class="profile__meta">
              <span class="profile__tag">{{ user?.role === 'admin' ? '管理员' : '书友' }}</span>
              <span>信誉 <b class="profile__credit">{{ user?.credit }}</b></span>
            </p>
          </div>
          <router-link to="/publish" class="profile__publish">+ 发布书籍</router-link>
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
            <div>
              <p class="profile__menu-name">{{ item.name }}</p>
              <p class="profile__menu-desc">{{ item.desc }}</p>
            </div>
            <span class="profile__menu-badge">开发中</span>
          </li>
        </ul>
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

.profile__avatar {
  display: grid;
  place-items: center;
  width: 64px;
  height: 64px;
  flex-shrink: 0;
  border-radius: 50%;
  background: var(--mark);
  color: #fff;
  font-family: var(--font-serif);
  font-size: 1.6rem;
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
  margin-left: auto;
  padding: 8px 20px;
  border-radius: 999px;
  background: var(--ink);
  color: #fff;
  font-size: 0.9rem;
  flex-shrink: 0;

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
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--sp-4) var(--sp-5);
  border-bottom: 1px solid var(--line);
  cursor: pointer;
  transition: background 0.2s ease;

  &:last-child {
    border-bottom: none;
  }

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
