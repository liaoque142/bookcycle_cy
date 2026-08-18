<script setup lang="ts">
import { ChatLineRound } from '@element-plus/icons-vue'
import type { ForumPostItem } from '@/types/forum'

defineProps<{ posts: ForumPostItem[] }>()
</script>

<template>
  <section class="section forum">
    <div class="container">
      <div class="forum__inner">
        <div class="forum__head">
          <h2 class="section-title">书友论坛</h2>
          <p class="section-sub">读书心得 · 书评 · 闲置交流</p>
          <router-link to="/forum" class="forum__btn">进入论坛</router-link>
        </div>

        <ul class="posts">
          <li v-for="p in posts" :key="p.id" class="posts__row">
            <span class="posts__board">{{ p.boardName }}</span>
            <router-link :to="`/forum/posts/${p.id}`" class="posts__title">{{ p.title }}</router-link>
            <span class="posts__by">{{ p.author }}</span>
            <span class="posts__stat">
              <el-icon><ChatLineRound /></el-icon>{{ p.replyCount }}
            </span>
          </li>
        </ul>
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
.forum {
  background: var(--card);
  border-top: 1px solid var(--line);
  border-bottom: 1px solid var(--line);

  &__inner {
    display: grid;
    grid-template-columns: 0.72fr 1.28fr;
    gap: var(--sp-7);
    align-items: start;
  }

  &__head {
    position: sticky;
    top: 96px;
  }

  &__btn {
    display: inline-block;
    margin-top: var(--sp-5);
    padding: 10px 24px;
    border: 1px solid var(--line-strong, rgba(0, 0, 0, 0.12));
    border-radius: 999px;
    font-size: 0.92rem;
    transition: all 0.2s ease;

    &:hover {
      border-color: var(--mark);
      color: var(--mark);
    }
  }
}

.posts__row {
  display: grid;
  grid-template-columns: 72px 1fr auto auto;
  align-items: center;
  gap: var(--sp-4);
  padding: var(--sp-4) 0;
  border-bottom: 1px solid var(--line);

  &:last-child {
    border-bottom: none;
  }

  &__board {
    font-size: 0.78rem;
    color: var(--mark);
    border: 1px solid rgba(176, 137, 78, 0.35);
    border-radius: 999px;
    padding: 1px 10px;
    text-align: center;
    white-space: nowrap;
  }

  &__title {
    font-size: 0.98rem;
    font-weight: 500;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    transition: color 0.2s ease;

    &:hover {
      color: var(--mark);
    }
  }

  &__by {
    font-size: 0.82rem;
    color: var(--muted);
    white-space: nowrap;
  }

  &__stat {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    font-size: 0.82rem;
    color: var(--muted);
    font-variant-numeric: tabular-nums;
    white-space: nowrap;
  }
}

@media (max-width: 860px) {
  .forum__inner {
    grid-template-columns: 1fr;
    gap: var(--sp-5);
  }
  .forum__head {
    position: static;
  }
  .posts__row {
    grid-template-columns: 64px 1fr auto;
  }
  .posts__by {
    display: none;
  }
}
</style>
