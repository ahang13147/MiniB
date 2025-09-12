<template>
  <div class="auth-wrap">
    <div class="card">
      <h2 class="title">找回密码</h2>
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
.auth-wrap { min-height: 100vh; display:grid; place-items:center; background: linear-gradient(135deg, #0f172a, #1f2937); }
.card { width: min(92vw, 480px); padding: 26px; border-radius: 16px; background: rgba(255,255,255,0.06); color:#e5e7eb; backdrop-filter: blur(10px); border: 1px solid rgba(255,255,255,0.1); }
.title { margin: 0 0 10px; color:#fff; }
.form { margin-top: 6px; }
.submit { width:100%; }
.links { text-align:center; margin-top: 10px; color:#94a3b8; }
.links a { color:#93c5fd; text-decoration:none; }
.links a:hover { text-decoration:underline; }
.tip { margin-top: 10px; color:#a7f3d0; font-size:12px; }
</style>


