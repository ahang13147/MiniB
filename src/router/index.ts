import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const Login = () => import('@/views/Login.vue')
const Dashboard = () => import('@/views/Dashboard.vue')
const Register = () => import('@/views/Register.vue')
const Forgot = () => import('@/views/Forgot.vue')

export const constantRoutes: RouteRecordRaw[] = [
  { path: '/login', name: 'Login', component: Login, meta: { public: true } },
  { path: '/register', name: 'Register', component: Register, meta: { public: true } },
  { path: '/forgot', name: 'Forgot', component: Forgot, meta: { public: true } },
  { path: '/', name: 'Root', redirect: '/dashboard' },
  { path: '/dashboard', name: 'Dashboard', component: Dashboard }
]

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes
})

router.beforeEach(async (to, _from, next) => {
  const auth = useAuthStore()
  if (to.meta.public) return next()
  if (!auth.token) return next({ name: 'Login', query: { redirect: to.fullPath } })
  if (!auth.user) {
    try { await auth.fetchMe() } catch { return next({ name: 'Login' }) }
  }
  next()
})

export default router




