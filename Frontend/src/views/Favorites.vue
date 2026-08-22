<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { fetchMyFavorites } from '@/api/favorite'
import BookCard from '@/components/home/BookCard.vue'
import type { Book } from '@/types/book'

const books = ref<Book[]>([])
const loading = ref(true)
const total = ref(0)
const page = ref(1)
const size = 50

async function load() {
  loading.value = true
  try {
    const res = await fetchMyFavorites(page.value, size)
    books.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <section class="section">
    <div class="container">
      <div class="page">
        <div class="page__head">
          <h1 class="page__title">我的收藏</h1>
          <span v-if="!loading" class="page__count">{{ total }} 本</span>
        </div>

        <div v-loading="loading" class="page__grid">
          <BookCard v-for="book in books" :key="book.id" :book="book" />
        </div>

        <div v-if="!loading && books.length === 0" class="page__empty">
          <p>还没有收藏书籍，去书市逛逛吧</p>
          <router-link to="/books" class="page__empty-btn">去书市</router-link>
        </div>

        <el-pagination
          v-if="!loading && total > size"
          v-model:current-page="page"
          :page-size="size"
          :total="total"
          layout="prev, pager, next"
          class="page__pager"
          @current-change="load"
        />
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
.page {
  max-width: 860px;
  margin: 0 auto;

  &__head {
    display: flex;
    align-items: baseline;
    justify-content: space-between;
    gap: var(--sp-3);
    margin-bottom: var(--sp-5);
  }

  &__title {
    font-family: var(--font-serif);
    font-size: 1.5rem;
    font-weight: 700;
  }

  &__count {
    color: var(--muted);
    font-size: 0.9rem;
  }

  &__grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: var(--sp-4);
    min-height: 120px;
  }

  &__empty {
    padding: var(--sp-8) 0;
    text-align: center;
    color: var(--muted);

    &-btn {
      display: inline-block;
      margin-top: var(--sp-4);
      padding: var(--sp-2) var(--sp-5);
      border-radius: 999px;
      background: var(--mark);
      color: #fff;
      font-size: 0.9rem;
    }
  }

  &__pager {
    margin-top: var(--sp-6);
    justify-content: center;
  }
}
</style>
