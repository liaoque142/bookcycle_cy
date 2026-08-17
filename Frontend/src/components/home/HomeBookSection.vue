<script setup lang="ts">
import type { Book } from '@/types/book'
import BookCard from './BookCard.vue'

defineProps<{
  title: string
  subtitle?: string
  books: Book[]
}>()
</script>

<template>
  <section class="section">
    <div class="container">
      <div class="section-head">
        <div>
          <h2 class="section-title">{{ title }}</h2>
          <p v-if="subtitle" class="section-sub">{{ subtitle }}</p>
        </div>
        <router-link to="/books" class="section-more">查看全部 →</router-link>
      </div>

      <div class="grid">
        <BookCard v-for="book in books" :key="book.id" :book="book" />
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(196px, 1fr));
  gap: var(--sp-5);
}
</style>
