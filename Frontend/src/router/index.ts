import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  scrollBehavior: () => ({ top: 0 }),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/Home.vue'),
    },
    {
      path: '/books',
      name: 'books',
      component: () => import('@/views/Books.vue'),
    },
    {
      path: '/books/:id',
      name: 'book-detail',
      component: () => import('@/views/BookDetail.vue'),
    },
    {
      path: '/forum',
      name: 'forum',
      component: () => import('@/views/Forum.vue'),
    },
    {
      path: '/forum/posts/:id',
      name: 'forum-post-detail',
      component: () => import('@/views/ForumDetail.vue'),
    },
    {
      path: '/addresses',
      name: 'addresses',
      component: () => import('@/views/Address.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/orders',
      name: 'orders',
      component: () => import('@/views/Orders.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/credit',
      name: 'credit',
      component: () => import('@/views/Credit.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/friends',
      name: 'friends',
      component: () => import('@/views/Friends.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/notifications',
      name: 'notifications',
      component: () => import('@/views/Notifications.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/admin',
      component: () => import('@/views/admin/AdminLayout.vue'),
      redirect: '/admin/users',
      meta: { requiresAdmin: true },
      children: [
        {
          path: 'users',
          name: 'admin-users',
          component: () => import('@/views/admin/AdminUsers.vue'),
          meta: { requiresAdmin: true },
        },
        {
          path: 'categories',
          name: 'admin-categories',
          component: () => import('@/views/admin/AdminCategories.vue'),
          meta: { requiresAdmin: true },
        },
        {
          path: 'books',
          name: 'admin-books',
          component: () => import('@/views/admin/AdminBooks.vue'),
          meta: { requiresAdmin: true },
        },
        {
          path: 'orders',
          name: 'admin-orders',
          component: () => import('@/views/admin/AdminOrders.vue'),
          meta: { requiresAdmin: true },
        },
        {
          path: 'refunds',
          name: 'admin-refunds',
          component: () => import('@/views/admin/AdminRefunds.vue'),
          meta: { requiresAdmin: true },
        },
        {
          path: 'stats',
          name: 'admin-stats',
          component: () => import('@/views/admin/AdminStats.vue'),
          meta: { requiresAdmin: true },
        },
        {
          path: 'config',
          name: 'admin-config',
          component: () => import('@/views/admin/AdminConfig.vue'),
          meta: { requiresAdmin: true },
        },
        {
          path: 'logs',
          name: 'admin-logs',
          component: () => import('@/views/admin/AdminOperationLogs.vue'),
          meta: { requiresAdmin: true },
        },
      ],
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('@/views/About.vue'),
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/Login.vue'),
    },
    {
      path: '/publish',
      name: 'publish',
      component: () => import('@/views/Publish.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('@/views/Profile.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('@/views/NotFound.vue'),
    },
  ],
})

// 鉴权守卫：受保护页未登录时重定向到登录页，并携带回跳地址；管理后台仅 admin 角色
router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.requiresAdmin) {
    if (!auth.isLoggedIn) {
      return { path: '/login', query: { redirect: to.fullPath } }
    }
    if (auth.user?.role !== 'admin') {
      return { path: '/' }
    }
    return
  }
  if (to.meta.requiresAuth && !auth.isLoggedIn) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
})

export default router
