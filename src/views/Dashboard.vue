<template>
  <div class="page">
    <div class="topbar">
      <div class="left">欢迎，{{ auth.user?.displayName }}（{{ auth.user?.role }}）</div>
      <div class="right">
        <el-button link @click="logout">退出登录</el-button>
      </div>
    </div>
    <div class="grid">
      <el-card v-for="m in menus" :key="m.path" class="card" @click="go(m.path)">
        <div class="title">{{ m.title }}</div>
        <div class="desc">{{ m.desc }}</div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore, type Permission } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

const allMenus = [
  { title: '仪表盘', desc: '总览数据', path: '/dashboard', perm: 'DASHBOARD_VIEW' },
  { title: '用户管理', desc: '管理员可见', path: '/users', perm: 'USER_MANAGE' },
  { title: '课程管理', desc: '教师/高校管理员', path: '/courses', perm: 'COURSE_MANAGE' },
  { title: '企业管理', desc: '企业管理员', path: '/companies', perm: 'COMPANY_MANAGE' },
  { title: '学生信息', desc: '可查看学生', path: '/students', perm: 'STUDENT_VIEW' },
  { title: '教师信息', desc: '可查看教师', path: '/teachers', perm: 'TEACHER_VIEW' },
  { title: '资源中心', desc: '科研共享与课程资源', path: '/resources', perm: 'DASHBOARD_VIEW' }
]

const menus = computed(() => {
  const perms = new Set(auth.user?.permissions || [])
  return allMenus.filter(m => perms.has(m.perm as Permission))
})

function go(path: string) { router.push(path) }
function logout() { auth.logout(); router.replace('/login') }
</script>

<style scoped>
.page { padding: 24px; }
.topbar { display:flex; justify-content: space-between; align-items:center; margin-bottom: 16px; }
.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 16px; }
.card { cursor: pointer; }
.title { font-weight: 600; margin-bottom: 8px; }
.desc { color: #666; }
</style>




