<script setup lang="ts">
import { computed } from 'vue'
import type { Book } from '@/types/book'

const props = withDefaults(
  defineProps<{
    book: Pick<Book, 'title' | 'author' | 'category' | 'coverTone' | 'coverToneText'>
    height?: string
  }>(),
  { height: 'auto' },
)

/** 分类书封色板：后端 BookVO 无 coverTone 字段，真实数据按分类取色（对齐 mock 色感） */
const CATEGORY_PALETTE: Record<string, { bg: string; text: string }> = {
  文学小说: { bg: '#EFEDE8', text: '#1A1A1A' },
  教材教辅: { bg: '#E4E7E4', text: '#1A1A1A' },
  人文社科: { bg: '#EBE3D9', text: '#1A1A1A' },
  科学技术: { bg: '#DFE1E6', text: '#1A1A1A' },
  少儿绘本: { bg: '#E8E6EB', text: '#333333' },
  生活艺术: { bg: '#E5E7E4', text: '#1A1A1A' },
  杂志期刊: { bg: '#E6E2DB', text: '#1A1A1A' },
  其他: { bg: '#E3E1DC', text: '#1A1A1A' },
}
const DEFAULT_TONE = { bg: '#EAE6DF', text: '#1A1A1A' }

const tone = computed(() => {
  const palette = CATEGORY_PALETTE[props.book.category] ?? DEFAULT_TONE
  return {
    bg: props.book.coverTone || palette.bg,
    text: props.book.coverToneText || palette.text,
  }
})
</script>

<template>
  <div
    class="book-cover"
    :style="{ background: tone.bg, color: tone.text, height: height }"
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
