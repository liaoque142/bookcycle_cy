<script setup lang="ts">
import BookCover from '@/components/common/BookCover.vue'
import { books } from '@/data/books'

// 书架展示的 5 本书（不同高度错落）
const shelf = [
  { book: books[0], height: '168px', tilt: '-2deg', offset: '0' },
  { book: books[1], height: '212px', tilt: '1.5deg', offset: '14px' },
  { book: books[4], height: '184px', tilt: '-1deg', offset: '-6px' },
  { book: books[2], height: '226px', tilt: '2deg', offset: '20px' },
  { book: books[3], height: '160px', tilt: '-1.5deg', offset: '0' },
]
</script>

<template>
  <section class="hero">
    <div class="container hero__inner">
      <div class="hero__copy">
        <p class="hero__eyebrow">二手书 · 交换 · 赠与 · 信誉社区</p>
        <h1 class="hero__title">
          让书，<br />继续它的<em>旅程</em>
        </h1>
        <p class="hero__sub">
          在书循环，出售、交换或赠送你的闲置书，<br />
          也为它找到下一双愿意翻开的手。
        </p>
        <div class="hero__actions">
          <router-link to="/books">
            <el-button type="primary" size="large" round class="hero__cta">浏览书市</el-button>
          </router-link>
          <router-link to="/publish">
            <el-button size="large" round class="hero__cta hero__cta--ghost">发布闲置书</el-button>
          </router-link>
        </div>
        <p class="hero__note">每笔交易双方互评 · 信誉分透明可查 · 优先逻辑删除保障安全</p>
      </div>

      <div class="hero__shelf-wrap" aria-label="示例书架">
        <div class="shelf">
          <div
            v-for="(s, i) in shelf"
            :key="s.book.id"
            class="shelf__item"
            :style="{ transform: `rotate(${s.tilt}) translateY(${s.offset})`, zIndex: i + 1 }"
          >
            <BookCover :book="s.book" :height="s.height" />
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
.hero {
  background:
    radial-gradient(1200px 500px at 85% -10%, rgba(176, 137, 78, 0.08), transparent 60%),
    var(--paper);

  &__inner {
    display: grid;
    grid-template-columns: 1.05fr 0.95fr;
    align-items: center;
    gap: var(--sp-8);
    padding-top: var(--sp-8);
    padding-bottom: var(--sp-8);
  }

  &__eyebrow {
    font-size: 0.85rem;
    letter-spacing: 0.18em;
    color: var(--mark);
    font-weight: 600;
    margin-bottom: var(--sp-4);
  }

  &__title {
    font-family: var(--font-serif);
    font-size: 3.4rem;
    line-height: 1.25;
    font-weight: 700;
    letter-spacing: 0.02em;

    em {
      font-style: normal;
      position: relative;

      &::after {
        content: '';
        position: absolute;
        left: 0;
        right: 0;
        bottom: 6px;
        height: 10px;
        background: rgba(176, 137, 78, 0.22);
        z-index: -1;
      }
    }
  }

  &__sub {
    margin-top: var(--sp-5);
    font-size: 1.05rem;
    color: var(--muted);
    line-height: 1.9;
  }

  &__actions {
    display: flex;
    gap: var(--sp-3);
    margin-top: var(--sp-6);
  }

  &__cta {
    padding: 22px 30px;
    font-size: 1rem;
  }

  &__note {
    margin-top: var(--sp-5);
    font-size: 0.82rem;
    color: var(--muted);
    letter-spacing: 0.04em;
  }
}

.shelf {
  display: flex;
  align-items: flex-end;
  justify-content: center;
  gap: 6px;
  padding: 48px 32px 44px;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid var(--line);
  border-radius: 20px;
  box-shadow: var(--shadow-card);
  min-height: 340px;

  &__item {
    flex-shrink: 0;
    transition: transform 0.25s ease;

    &:hover {
      transform: translateY(-6px) !important;
      z-index: 20;
    }
  }
}

@media (max-width: 860px) {
  .hero__inner {
    grid-template-columns: 1fr;
    gap: var(--sp-6);
    padding-top: var(--sp-6);
    padding-bottom: var(--sp-6);
  }
  .hero__title {
    font-size: 2.5rem;
  }
  .shelf {
    min-height: 260px;
  }
}
</style>
