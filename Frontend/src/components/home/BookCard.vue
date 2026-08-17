<script setup lang="ts">
import type { Book } from '@/types/book'
import BookCover from '@/components/common/BookCover.vue'

defineProps<{ book: Book }>()

function priceLabel(book: Book): string {
  if (book.price !== null) return `¥${book.price}`
  if (book.tradeType === '交换') return '交换'
  return '赠送'
}
</script>

<template>
  <article class="card">
    <router-link :to="`/books/${book.id}`" class="card__link">
      <div class="card__cover">
        <BookCover :book="book" height="100%" />
        <span class="card__type" :class="`card__type--${book.tradeType}`">
          {{ priceLabel(book) }}
        </span>
      </div>

      <div class="card__body">
        <h3 class="card__title">{{ book.title }}</h3>
        <p class="card__author">{{ book.author }}</p>
        <div class="card__meta">
          <span class="card__cond">{{ book.condition }} · {{ book.category }}</span>
          <span class="card__credit">信誉 {{ book.credit }}</span>
        </div>
      </div>
    </router-link>
  </article>
</template>

<style scoped lang="scss">
.card {
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  overflow: hidden;
  transition: box-shadow 0.2s ease, transform 0.2s ease;
  cursor: pointer;

  &:hover {
    box-shadow: var(--shadow-lift);
    transform: translateY(-4px);
  }

  &__link {
    display: block;
    height: 100%;
  }

  &__cover {
    position: relative;
    aspect-ratio: 3 / 3.8;
    padding: 16px 20px;
    background: var(--paper);
  }

  &__type {
    position: absolute;
    left: 12px;
    top: 12px;
    padding: 2px 10px;
    font-size: 0.78rem;
    font-weight: 600;
    border-radius: 999px;
    background: var(--ink);
    color: #fff;
    letter-spacing: 0.04em;

    &--交换 {
      background: var(--card);
      color: var(--ink);
      box-shadow: 0 0 0 1px var(--line) inset;
    }

    &--赠送 {
      background: var(--mark);
      color: #fff;
    }
  }

  &__body {
    padding: var(--sp-4);
  }

  &__title {
    font-family: var(--font-serif);
    font-size: 1.08rem;
    font-weight: 700;
    line-height: 1.5;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__author {
    margin-top: 2px;
    font-size: 0.85rem;
    color: var(--muted);
  }

  &__meta {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: var(--sp-3);
    padding-top: var(--sp-3);
    border-top: 1px dashed var(--line);
    font-size: 0.8rem;
    color: var(--muted);
  }

  &__credit {
    color: var(--mark);
    font-variant-numeric: tabular-nums;
  }
}
</style>
