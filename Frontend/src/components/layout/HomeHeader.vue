<script setup lang="ts">
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import { Search } from '@element-plus/icons-vue'

const route = useRoute()
const isActive = (to: string) => (to === '/' ? route.path === '/' : route.path.startsWith(to))

const query = ref('')
const navs = [
  { name: '首页', to: '/' },
  { name: '书市', to: '/books' },
  { name: '论坛', to: '/forum' },
  { name: '关于', to: '/about' },
]
</script>

<template>
  <header class="header">
    <div class="container header__inner">
      <router-link to="/" class="brand">
        <span class="brand__seal" aria-hidden="true">书</span>
        <span class="brand__text">书循环<em>BookCycle</em></span>
      </router-link>

      <nav class="nav" aria-label="主导航">
        <router-link
          v-for="n in navs"
          :key="n.name"
          :to="n.to"
          class="nav__link"
          :class="{ 'is-active': isActive(n.to) }"
        >
          {{ n.name }}
        </router-link>
      </nav>

      <div class="header__right">
        <el-input
          v-model="query"
          class="search"
          placeholder="搜索书名 / 作者 / ISBN"
          :prefix-icon="Search"
          clearable
        />
        <el-button type="primary" round class="login-btn">登录 / 注册</el-button>
      </div>
    </div>
  </header>
</template>

<style scoped lang="scss">
.header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--line);

  &__inner {
    display: flex;
    align-items: center;
    gap: var(--sp-6);
    height: 64px;
  }
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;

  &__seal {
    display: grid;
    place-items: center;
    width: 34px;
    height: 34px;
    border-radius: 8px;
    background: var(--ink);
    color: #fff;
    font-family: var(--font-serif);
    font-size: 18px;
    line-height: 1;
  }

  &__text {
    font-family: var(--font-serif);
    font-size: 1.25rem;
    font-weight: 700;
    letter-spacing: 0.06em;

    em {
      font-style: normal;
      font-family: var(--font-sans);
      font-size: 0.62rem;
      font-weight: 600;
      letter-spacing: 0.14em;
      color: var(--muted);
      margin-left: 6px;
    }
  }
}

.nav {
  display: flex;
  gap: var(--sp-5);
  flex-shrink: 0;

  &__link {
    font-size: 0.95rem;
    color: var(--muted);
    position: relative;
    transition: color 0.2s ease;
    padding: 4px 0;

    &:hover {
      color: var(--ink);
    }

    &.is-active {
      color: var(--ink);
      font-weight: 600;

      &::after {
        content: '';
        position: absolute;
        left: 0;
        right: 0;
        bottom: -2px;
        height: 2px;
        border-radius: 2px;
        background: var(--mark);
      }
    }
  }
}

.header__right {
  display: flex;
  align-items: center;
  gap: var(--sp-4);
  margin-left: auto;
}

.search {
  width: 240px;

  :deep(.el-input__wrapper) {
    border-radius: 999px;
    box-shadow: 0 0 0 1px var(--line) inset;
    background: var(--card);
    padding: 2px 14px;
  }
}

.login-btn {
  flex-shrink: 0;
}

@media (max-width: 860px) {
  .nav {
    display: none;
  }
  .search {
    width: 160px;
  }
}
</style>
