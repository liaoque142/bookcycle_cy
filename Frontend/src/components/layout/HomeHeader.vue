<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, ArrowDown } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const isActive = (to: string) => (to === '/' ? route.path === '/' : route.path.startsWith(to))

const query = ref('')

async function onLogout() {
  await authStore.logout()
  ElMessage.success('已退出登录')
  if (route.path === '/profile' || route.path === '/publish') router.push('/')
}

function onCommand(cmd: string) {
  if (cmd === 'logout') onLogout()
  else router.push(`/${cmd}`)
}
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
        <router-link v-if="!authStore.isLoggedIn" to="/login" class="login-btn">
          <el-button type="primary" round>登录 / 注册</el-button>
        </router-link>
        <el-dropdown v-else trigger="click" @command="onCommand">
          <button class="user-btn">
            <span class="user-btn__avatar">{{ authStore.user?.nickname?.slice(0, 1) || authStore.user?.username.slice(0, 1) }}</span>
            <span class="user-btn__name">{{ authStore.user?.nickname || authStore.user?.username }}</span>
            <el-icon class="user-btn__arrow"><ArrowDown /></el-icon>
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item command="publish">发布书籍</el-dropdown-item>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
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

.user-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 10px 4px 4px;
  border: 1px solid var(--line);
  border-radius: 999px;
  background: var(--card);
  cursor: pointer;
  transition: box-shadow 0.2s ease;

  &:hover {
    box-shadow: var(--shadow-card);
  }

  &__avatar {
    display: grid;
    place-items: center;
    width: 30px;
    height: 30px;
    border-radius: 50%;
    background: var(--mark);
    color: #fff;
    font-family: var(--font-serif);
    font-size: 0.9rem;
  }

  &__name {
    font-size: 0.9rem;
    font-weight: 600;
  }

  &__arrow {
    color: var(--muted);
    font-size: 12px;
  }
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
