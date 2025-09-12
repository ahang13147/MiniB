<template>
  <div class="auth-wrap">
    <div class="bg-blobs">
      <div class="blob b1" />
      <div class="blob b2" />
      <div class="blob b3" />
    </div>
    <div class="card">
      <div class="brand-area">
        <div class="logo">MB</div>
        <div class="title-group">
          <h2 class="title">找回密码</h2>
          <p class="subtitle">输入用户名获取重置 Token（演示）</p>
        </div>
      </div>
      <el-form :model="form" ref="formRef" label-position="top" class="form" @keyup.enter="onSubmit">
        <el-form-item label="用户名" prop="username" :rules="[{ required: true, message: '请输入用户名' }]">
          <el-input v-model="form.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-button type="primary" class="submit" :loading="loading" @click="onSubmit">发送重置链接</el-button>
        <div v-if="resetToken" class="tip">演示环境：重置 Token 为 {{ resetToken }}</div>
        <div class="links">
          记起密码了？<router-link to="/login">返回登录</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import axios from 'axios'

const form = reactive({ username: '' })
const formRef = ref()
const loading = ref(false)
const resetToken = ref('')

async function onSubmit() {
  // @ts-ignore
  await formRef.value?.validate?.()
  loading.value = true
  try {
    const { data } = await axios.post('/api/auth/forgot', { username: form.username })
    resetToken.value = data.resetToken
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-wrap { min-height: 100vh; display:grid; place-items:center; background: radial-gradient(1200px 600px at 10% 10%, rgba(99,102,241,0.25), transparent 40%), radial-gradient(1000px 500px at 90% 20%, rgba(16,185,129,0.25), transparent 45%), linear-gradient(135deg, #0f172a 0%, #1f2937 100%); position: relative; overflow: hidden; }
.bg-blobs { position: absolute; inset: 0; pointer-events: none; filter: blur(40px); opacity: .6; }
.blob { position: absolute; border-radius: 50%; transform: translate(-50%, -50%); }
.b1 { width: 420px; height: 420px; left: 12%; top: 18%; background: #6366f1; animation: float 12s ease-in-out infinite; }
.b2 { width: 380px; height: 380px; right: 1%; top: 10%; background: #22c55e; animation: float 14s ease-in-out infinite 1s; }
.b3 { width: 220px; height: 220px; left: 70%; bottom: -5%; background: #06b6d4; animation: float 16s ease-in-out infinite 2s; }
@keyframes float { 0%,100% { transform: translate(-50%, -50%) translateY(0); } 50% { transform: translate(-50%, -50%) translateY(-20px); } }

.card { width: min(92vw, 440px); padding: 28px; border-radius: 20px; background: rgba(255,255,255,0.06); backdrop-filter: saturate(160%) blur(16px); box-shadow: 0 20px 50px rgba(0,0,0,0.45); color: #e5e7eb; border: 1px solid rgba(255,255,255,0.08); position: relative; }
.brand-area { display: flex; align-items: center; gap: 14px; margin-bottom: 6px; }
.logo { width: 46px; height: 46px; border-radius: 12px; display:grid; place-items:center; font-weight: 800; color:#0b1220; background: linear-gradient(135deg, #93c5fd 0%, #a78bfa 100%); box-shadow: inset 0 0 30px rgba(255,255,255,0.5), 0 10px 25px rgba(0,0,0,0.25); }
.title { margin: 0; font-size: 20px; color:#fff; }
.subtitle { margin: 2px 0 8px; color:#cbd5e1; font-size: 12px; }
.form { margin-top: 8px; }
.submit { width:100%; box-shadow: 0 10px 30px rgba(59,130,246,.35); }
.links { text-align:center; margin-top: 10px; color:#94a3b8; }
.links a { color:#93c5fd; text-decoration:none; }
.links a:hover { text-decoration:underline; }
.tip { margin-top: 10px; color:#a7f3d0; font-size:12px; }
</style>


