<template>
  <div class="login-wrap">
    <div class="bg-blobs">
      <div class="blob b1" />
      <div class="blob b2" />
      <div class="blob b3" />
    </div>
    <div class="login-card">
      <div class="brand-area">
        <div class="logo">MB</div>
        <div class="title-group">
          <h1 class="brand">MiniB 管理平台</h1>
          <p class="subtitle">校企协同 · 多角色一体化管理</p>
        </div>
      </div>
      <el-form :model="form" ref="formRef" @keyup.enter="onSubmit" label-position="top" class="form">
        <el-form-item label="用户名" prop="username" :rules="[{ required: true, message: '请输入用户名' }]">
          <el-input v-model="form.username" placeholder="admin/uadmin/eadmin/teacher/student/mentor" clearable />
        </el-form-item>
        <el-form-item label="密码" prop="password" :rules="[{ required: true, message: '请输入密码' }]">
          <el-input v-model="form.password" type="password" placeholder="任意" show-password />
        </el-form-item>

        <div class="role-quick">
          <span class="role-label">快速选择角色</span>
          <div class="role-list">
            <button v-for="r in roles" :key="r.v" type="button" class="role-btn" :class="{ active: quickRole === r.v }" @click="quickRole = r.v">
              <span class="emoji">{{ r.e }}</span>
              <span>{{ r.l }}</span>
            </button>
          </div>
        </div>

        <div class="actions">
          <el-checkbox v-model="remember">记住我</el-checkbox>
          <router-link class="link" to="/forgot">忘记密码？</router-link>
        </div>

        <el-button type="primary" class="submit" :loading="loading" size="large" @click="onSubmit">登录</el-button>
      </el-form>
      <div class="footer">还没有账号？<router-link to="/register">立即注册</router-link> · © {{ year }} MiniB</div>
    </div>
  </div>
  </template>

<script setup lang="ts">
import { reactive, ref, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const form = reactive({ username: '', password: '' })
const formRef = ref()
const loading = ref(false)
const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const remember = ref(true)
const year = computed(() => new Date().getFullYear())

const roles = [
  { l: '超级管理员', v: 'admin', e: '👑' },
  { l: '高校管理员', v: 'uadmin', e: '🏫' },
  { l: '企业管理员', v: 'eadmin', e: '🏢' },
  { l: '教师', v: 'teacher', e: '👩‍🏫' },
  { l: '学生', v: 'student', e: '🎓' },
  { l: '企业导师', v: 'mentor', e: '🧭' }
]
const quickRole = ref('')
watch(quickRole, (v) => { form.username = v })

async function onSubmit() {
  // @ts-ignore
  await formRef.value?.validate?.()
  loading.value = true
  try {
    await auth.login(form.username, form.password)
    const redirect = (route.query.redirect as string) || '/dashboard'
    router.replace(redirect)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrap {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: radial-gradient(1200px 600px at 10% 10%, rgba(99,102,241,0.25), transparent 40%),
              radial-gradient(1000px 500px at 90% 20%, rgba(16,185,129,0.25), transparent 45%),
              linear-gradient(135deg, #0f172a 0%, #1f2937 100%);
  overflow: hidden;
}
.bg-blobs { position: absolute; inset: 0; pointer-events: none; filter: blur(40px); opacity: .6; }
.blob { position: absolute; border-radius: 50%; transform: translate(-50%, -50%); }
.b1 { width: 420px; height: 420px; left: 12%; top: 18%; background: #6366f1; animation: float 12s ease-in-out infinite; }
.b2 { width: 380px; height: 380px; right: 1%; top: 10%; background: #22c55e; animation: float 14s ease-in-out infinite 1s; }
.b3 { width: 220px; height: 220px; left: 70%; bottom: -5%; background: #06b6d4; animation: float 16s ease-in-out infinite 2s; }
@keyframes float { 0%,100% { transform: translate(-50%, -50%) translateY(0); } 50% { transform: translate(-50%, -50%) translateY(-20px); } }

.login-card {
  width: min(92vw, 440px);
  padding: 28px;
  border-radius: 20px;
  background: rgba(255,255,255,0.06);
  backdrop-filter: saturate(160%) blur(16px);
  box-shadow: 0 20px 50px rgba(0,0,0,0.45);
  color: #e5e7eb;
  border: 1px solid rgba(255,255,255,0.08);
}
.brand-area { display: flex; align-items: center; gap: 14px; margin-bottom: 10px; }
.logo { width: 46px; height: 46px; border-radius: 12px; display:grid; place-items:center; font-weight: 800; color:#0b1220; background: linear-gradient(135deg, #93c5fd 0%, #a78bfa 100%); box-shadow: inset 0 0 30px rgba(255,255,255,0.5), 0 10px 25px rgba(0,0,0,0.25); }
.brand { margin: 0; font-size: 22px; letter-spacing: .5px; color: #fff; }
.subtitle { margin: 2px 0 8px; color: #cbd5e1; font-size: 12px; }
.form { margin-top: 8px; }
::v-deep(.el-form-item__label) { color: #cbd5e1; }

.role-quick { margin: 4px 0 8px; }
.role-label { display:block; color:#cbd5e1; margin: 6px 0 8px; font-size: 12px; }
.role-list { display: grid; grid-template-columns: repeat(3, minmax(0,1fr)); gap: 8px; }
.role-btn {
  display:flex; align-items:center; justify-content:center; gap:6px;
  padding: 10px 12px; border-radius: 12px; border: 1px solid rgba(255,255,255,0.12);
  background: rgba(255,255,255,0.06); color:#e5e7eb; cursor: pointer; transition: all .2s ease;
}
.role-btn:hover { transform: translateY(-1px); background: rgba(255,255,255,0.12); }
.role-btn.active { border-color: rgba(59,130,246,.6); box-shadow: 0 8px 20px rgba(59,130,246,.25) inset; }
.emoji { font-size: 16px; }

.actions { display:flex; align-items:center; justify-content: space-between; margin: 2px 0 12px; }
.link { color: #93c5fd; text-decoration: none; font-size: 12px; }
.link:hover { text-decoration: underline; }

.submit { width: 100%; box-shadow: 0 10px 30px rgba(59,130,246,.35); }
.footer { margin-top: 12px; text-align:center; color:#94a3b8; font-size: 12px; }

@media (max-width: 420px) {
  .role-list { grid-template-columns: repeat(2, minmax(0,1fr)); }
}
</style>




