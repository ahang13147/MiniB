<template>
  <div class="auth-wrap">
    <div class="card">
      <h2 class="title">创建账户</h2>
      <el-form :model="form" ref="formRef" label-position="top" class="form" @keyup.enter="onSubmit">
        <el-form-item label="用户名" prop="username" :rules="[{ required: true, message: '请输入用户名' }]">
          <el-input v-model="form.username" placeholder="例如：newuser" clearable />
        </el-form-item>
        <el-form-item label="显示名称" prop="displayName">
          <el-input v-model="form.displayName" placeholder="例如：新同学" clearable />
        </el-form-item>
        <el-form-item label="密码" prop="password" :rules="[{ required: true, message: '请输入密码' }]">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role" placeholder="选择角色">
            <el-option v-for="r in roles" :key="r.v" :label="r.l" :value="r.v" />
          </el-select>
        </el-form-item>
        <el-button type="primary" class="submit" :loading="loading" @click="onSubmit">注册</el-button>
        <div class="links">
          已有账号？<router-link to="/login">去登录</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import axios from 'axios'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

const form = reactive({ username: '', password: '', displayName: '', role: 'STUDENT' })
const formRef = ref()
const loading = ref(false)
const router = useRouter()
const auth = useAuthStore()

const roles = [
  { l: '学生', v: 'STUDENT' },
  { l: '教师', v: 'TEACHER' },
  { l: '高校管理员', v: 'UNIVERSITY_ADMIN' },
  { l: '企业管理员', v: 'ENTERPRISE_ADMIN' },
  { l: '企业导师', v: 'ENTERPRISE_MENTOR' },
  { l: '超级管理员', v: 'SUPER_ADMIN' }
]

async function onSubmit() {
  // @ts-ignore
  await formRef.value?.validate?.()
  loading.value = true
  try {
    const { data } = await axios.post('/api/auth/register', {
      username: form.username,
      password: form.password,
      displayName: form.displayName,
      role: form.role
    })
    await auth.login(form.username, form.password)
    router.replace('/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-wrap { min-height: 100vh; display:grid; place-items:center; background: linear-gradient(135deg, #0f172a, #1f2937); }
.card { width: min(92vw, 520px); padding: 26px; border-radius: 16px; background: rgba(255,255,255,0.06); color:#e5e7eb; backdrop-filter: blur(10px); border: 1px solid rgba(255,255,255,0.1); }
.title { margin: 0 0 10px; color:#fff; }
.form { margin-top: 6px; }
.submit { width:100%; }
.links { text-align:center; margin-top: 10px; color:#94a3b8; }
.links a { color:#93c5fd; text-decoration:none; }
.links a:hover { text-decoration:underline; }
</style>


