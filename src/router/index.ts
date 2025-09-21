import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const Login = () => import('@/views/Login.vue')
const Dashboard = () => import('@/views/Dashboard.vue')
const Register = () => import('@/views/Register.vue')
const Forgot = () => import('@/views/Forgot.vue')
const ResourceCenter = () => import('@/views/ResourceCenter.vue')
const UserManagement = () => import('@/views/UserManagement.vue')
const CourseManagement = () => import('@/views/CourseManagement.vue')
const CompanyManagement = () => import('@/views/CompanyManagement.vue')
const Profile = () => import('@/views/Profile.vue')
const Notifications = () => import('@/views/Notifications.vue')
const Settings = () => import('@/views/Settings.vue')
const ProjectManagement = () => import('@/views/ProjectManagement.vue')
const InternshipManagement = () => import('@/views/InternshipManagement.vue')
const AchievementCenter = () => import('@/views/AchievementCenter.vue')
const MentorManagement = () => import('@/views/MentorManagement.vue')

export const constantRoutes: RouteRecordRaw[] = [
  { path: '/login', name: 'Login', component: Login, meta: { public: true } },
  { path: '/register', name: 'Register', component: Register, meta: { public: true } },
  { path: '/forgot', name: 'Forgot', component: Forgot, meta: { public: true } },
  { path: '/', name: 'Root', redirect: '/dashboard' },
  { path: '/dashboard', name: 'Dashboard', component: Dashboard },
  { path: '/resources', name: 'ResourceCenter', component: ResourceCenter },
  { path: '/users', name: 'UserManagement', component: UserManagement },
  { path: '/courses', name: 'CourseManagement', component: CourseManagement },
  { path: '/companies', name: 'CompanyManagement', component: CompanyManagement },
  { path: '/projects', name: 'ProjectManagement', component: ProjectManagement },
  { path: '/internships', name: 'InternshipManagement', component: InternshipManagement },
  { path: '/achievements', name: 'AchievementCenter', component: AchievementCenter },
  { path: '/mentors', name: 'MentorManagement', component: MentorManagement },
  { path: '/profile', name: 'Profile', component: Profile },
  { path: '/notifications', name: 'Notifications', component: Notifications },
  { path: '/settings', name: 'Settings', component: Settings }
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




