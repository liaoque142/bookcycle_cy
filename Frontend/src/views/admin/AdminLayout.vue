<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Files, Notebook, ShoppingCart, Service, TrendCharts, Setting, Document, HomeFilled, SwitchButton } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const activeMenu = computed(() => route.path)

const menus = [
  { path: '/admin/users', label: '用户管理', icon: User },
  { path: '/admin/categories', label: '分类管理', icon: Files },
  { path: '/admin/books', label: '书籍管理', icon: Notebook },
  { path: '/admin/orders', label: '交易管理', icon: ShoppingCart },
  { path: '/admin/refunds', label: '售后管理', icon: Service },
  { path: '/admin/stats', label: '数据统计', icon: TrendCharts },
  { path: '/admin/config', label: '系统配置', icon: Setting },
  { path: '/admin/logs', label: '操作日志', icon: Document },
]

const title = computed(() => menus.find((m) => route.path.startsWith(m.path))?.label ?? '管理后台')

async function onLogout() {
  await authStore.logout()
  ElMessage.success('已退出登录')
  router.push('/')
}
</script>

<template>
  <div class="admin">
    <aside class="admin__side">
      <router-link to="/" class="admin__brand">
        <span class="admin__seal">书</span>
        <span class="admin__name">书循环 · 管理后台</span>
      </router-link>

      <el-menu
        class="admin__menu"
        :default-active="activeMenu"
        router
        background-color="transparent"
        text-color="#c6cbd4"
        active-text-color="#fff"
      >
        <el-menu-item v-for="m in menus" :key="m.path" :index="m.path">
          <el-icon><component :is="m.icon" /></el-icon>
          <span>{{ m.label }}</span>
        </el-menu-item>
      </el-menu>
    </aside>

    <div class="admin__main">
      <header class="admin__top">
        <h1 class="admin__title">{{ title }}</h1>
        <div class="admin__top-right">
          <router-link to="/" class="admin__link">
            <el-icon><HomeFilled /></el-icon>
            <span>返回前台</span>
          </router-link>
          <span class="admin__who">{{ authStore.user?.nickname || authStore.user?.username }}</span>
          <el-button text :icon="SwitchButton" @click="onLogout">退出</el-button>
        </div>
      </header>

      <main class="admin__content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<style scoped lang="scss">
.admin {
  display: flex;
  min-height: 100vh;
  background: var(--paper);

  &__side {
    width: 224px;
    flex-shrink: 0;
    background: var(--ink);
    display: flex;
    flex-direction: column;
    position: sticky;
    top: 0;
    height: 100vh;
  }

  &__brand {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 20px;
    color: #fff;
    border-bottom: 1px solid rgba(255, 255, 255, 0.08);

    &:hover {
      color: #fff;
    }
  }

  &__seal {
    display: grid;
    place-items: center;
    width: 32px;
    height: 32px;
    border-radius: 8px;
    background: #fff;
    color: var(--ink);
    font-family: var(--font-serif);
    font-size: 16px;
  }

  &__name {
    font-family: var(--font-serif);
    font-size: 0.95rem;
    font-weight: 700;
    letter-spacing: 0.04em;
  }

  &__menu {
    flex: 1;
    padding: 12px 8px;
    border-right: none;

    :deep(.el-menu-item) {
      border-radius: 8px;
      margin-bottom: 4px;
      height: 44px;

      &.is-active {
        background: rgba(255, 255, 255, 0.14);
      }

      &:hover {
        background: rgba(255, 255, 255, 0.08);
      }
    }
  }

  &__main {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
  }

  &__top {
    height: 60px;
    padding: 0 24px;
    background: var(--card);
    border-bottom: 1px solid var(--line);
    display: flex;
    align-items: center;
    justify-content: space-between;
    position: sticky;
    top: 0;
    z-index: 10;
  }

  &__title {
    font-family: var(--font-serif);
    font-size: 1.1rem;
    font-weight: 700;
  }

  &__top-right {
    display: flex;
    align-items: center;
    gap: 14px;
    font-size: 0.9rem;
  }

  &__link {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    color: var(--muted);

    &:hover {
      color: var(--mark);
    }
  }

  &__who {
    color: var(--muted);
  }

  &__content {
    flex: 1;
    padding: 24px;
  }
}
</style>
