import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  { path: '/', redirect: '/home' },
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue') },
  { path: '/home', name: 'Home', component: () => import('@/views/Home.vue') },
  { path: '/camera', name: 'Camera', component: () => import('@/views/Camera.vue') },
  { path: '/records', name: 'Records', component: () => import('@/views/Records.vue') },
  { path: '/records/:id', name: 'RecordDetail', component: () => import('@/views/RecordDetail.vue') },
  { path: '/prediction', name: 'Prediction', component: () => import('@/views/Prediction.vue') },
  { path: '/stats', name: 'Stats', component: () => import('@/views/Stats.vue') },
  { path: '/settings', name: 'Settings', component: () => import('@/views/Settings.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  if (!authStore.token && to.path !== '/login') {
    next('/login')
  } else if (authStore.token && to.path === '/login') {
    next('/home')
  } else {
    next()
  }
})

export default router
