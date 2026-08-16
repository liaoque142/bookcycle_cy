<script setup lang="ts">
import type { Book } from '@/types/book'

const props = withDefaults(
  defineProps<{
    book: Pick<Book, 'title' | 'author' | 'coverTone' | 'coverToneText'>
    height?: string
  }>(),
  { height: 'auto' },
)
</script>

<template>
  <div
    class="book-cover"
    :style="{ background: book.coverTone, color: book.coverToneText, height: height }"
  >
    <span class="book-cover__spine" aria-hidden="true" />
    <span class="book-cover__mark" aria-hidden="true" />
    <div class="book-cover__title">{{ book.title }}</div>
    <div class="book-cover__author">{{ book.author }}</div>
  </div>
</template>

<style scoped lang="scss">
.book-cover {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 20px 16px;
  border-radius: 4px 8px 8px 4px;
  box-shadow: var(--shadow-card), inset 0 0 0 1px rgba(26, 26, 26, 0.05);
  overflow: hidden;
  font-family: var(--font-serif);
  user-select: none;

  &__spine {
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 6px;
    background: linear-gradient(90deg, rgba(26, 26, 26, 0.12), transparent);
  }

  &__mark {
    position: absolute;
    right: 12px;
    top: 0;
    width: 3px;
    height: 26px;
    background: var(--mark);
    opacity: 0.9;
  }

  &__title {
    font-size: 1.02rem;
    font-weight: 700;
    letter-spacing: 0.05em;
    text-align: center;
    line-height: 1.55;
  }

  &__author {
    font-size: 0.76rem;
    opacity: 0.72;
    letter-spacing: 0.1em;
  }
}
</style>
