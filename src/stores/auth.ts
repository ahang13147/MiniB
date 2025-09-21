import { defineStore } from 'pinia'
import axios from 'axios'

export type Role = 'SUPER_ADMIN' | 'UNIVERSITY_ADMIN' | 'ENTERPRISE_ADMIN' | 'TEACHER' | 'STUDENT' | 'ENTERPRISE_MENTOR'
export type Permission = 
  // 基础权限
  | 'DASHBOARD_VIEW' | 'PROFILE_MANAGE'
  // 用户管理权限
  | 'USER_VIEW' | 'USER_CREATE' | 'USER_UPDATE' | 'USER_DELETE' | 'USER_MANAGE'
  // 企业管理权限
  | 'COMPANY_VIEW' | 'COMPANY_CREATE' | 'COMPANY_UPDATE' | 'COMPANY_DELETE' | 'COMPANY_MANAGE' | 'COMPANY_APPROVE'
  // 课程管理权限
  | 'COURSE_VIEW' | 'COURSE_CREATE' | 'COURSE_UPDATE' | 'COURSE_DELETE' | 'COURSE_MANAGE'
  // 学生管理权限
  | 'STUDENT_VIEW' | 'STUDENT_CREATE' | 'STUDENT_UPDATE' | 'STUDENT_DELETE' | 'STUDENT_MANAGE'
  // 教师管理权限
  | 'TEACHER_VIEW' | 'TEACHER_CREATE' | 'TEACHER_UPDATE' | 'TEACHER_DELETE' | 'TEACHER_MANAGE'
  // 项目管理权限
  | 'PROJECT_VIEW' | 'PROJECT_CREATE' | 'PROJECT_UPDATE' | 'PROJECT_DELETE' | 'PROJECT_MANAGE' | 'PROJECT_APPROVE'
  // 实习管理权限
  | 'INTERNSHIP_VIEW' | 'INTERNSHIP_CREATE' | 'INTERNSHIP_UPDATE' | 'INTERNSHIP_DELETE' | 'INTERNSHIP_MANAGE' | 'INTERNSHIP_APPLY'
  // 资源管理权限
  | 'RESOURCE_VIEW' | 'RESOURCE_CREATE' | 'RESOURCE_UPDATE' | 'RESOURCE_DELETE' | 'RESOURCE_MANAGE' | 'RESOURCE_BORROW'
  // 成果管理权限
  | 'ACHIEVEMENT_VIEW' | 'ACHIEVEMENT_CREATE' | 'ACHIEVEMENT_UPDATE' | 'ACHIEVEMENT_DELETE' | 'ACHIEVEMENT_MANAGE' | 'ACHIEVEMENT_VERIFY'
  // 导师管理权限
  | 'MENTOR_VIEW' | 'MENTOR_CREATE' | 'MENTOR_UPDATE' | 'MENTOR_DELETE' | 'MENTOR_MANAGE'

interface UserProfile {
  id: string
  displayName: string
  role: Role
  permissions: Permission[]
}

interface LoginResponse {
  token: string
  user: UserProfile
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: null as UserProfile | null
  }),
  getters: {
    isLoggedIn: (s) => !!s.token,
    hasPermission: (s) => (permission: Permission) => {
      return s.user?.permissions?.includes(permission) ?? false
    },
    hasAnyPermission: (s) => (permissions: Permission[]) => {
      return permissions.some(permission => s.user?.permissions?.includes(permission) ?? false)
    },
    hasAllPermissions: (s) => (permissions: Permission[]) => {
      return permissions.every(permission => s.user?.permissions?.includes(permission) ?? false)
    }
  },
  actions: {
    async login(username: string, password: string) {
      const { data } = await axios.post<LoginResponse>('/api/auth/login', { username, password })
      this.token = data.token
      this.user = data.user
      axios.defaults.headers.common['Authorization'] = `Bearer ${this.token}`
      localStorage.setItem('token', this.token)
    },
    async fetchMe() {
      if (!this.token) throw new Error('no token')
      axios.defaults.headers.common['Authorization'] = `Bearer ${this.token}`
      const { data } = await axios.get<UserProfile>('/api/auth/me')
      this.user = data
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('token')
      delete axios.defaults.headers.common['Authorization']
    }
  }
})




