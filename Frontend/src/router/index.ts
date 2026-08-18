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

// 鉴权守卫：受保护页未登录时重定向到登录页，并携带回跳地址
router.beforeEach((to) => {
  if (to.meta.requiresAuth && !useAuthStore().isLoggedIn) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
})

export default router
