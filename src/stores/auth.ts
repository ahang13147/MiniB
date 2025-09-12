import { defineStore } from 'pinia'
import axios from 'axios'

export type Role = 'SUPER_ADMIN' | 'UNIVERSITY_ADMIN' | 'ENTERPRISE_ADMIN' | 'TEACHER' | 'STUDENT' | 'ENTERPRISE_MENTOR'
export type Permission = 'USER_MANAGE' | 'COURSE_MANAGE' | 'COMPANY_MANAGE' | 'STUDENT_VIEW' | 'TEACHER_VIEW' | 'DASHBOARD_VIEW'

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
    isLoggedIn: (s) => !!s.token
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




