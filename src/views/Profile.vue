<template>
  <div class="profile-wrap">
    <div class="bg-blobs">
      <div class="blob b1" />
      <div class="blob b2" />
      <div class="blob b3" />
    </div>

    <div class="page">
      <div class="brand-area glass">
        <div class="logo">👤</div>
        <div class="title-group">
          <h2 class="brand">个人资料</h2>
          <p class="subtitle">完善个人信息 · 展示专业形象</p>
        </div>
      </div>

      <div class="profile-content">
        <!-- 左侧：头像和基本信息 -->
        <div class="left-section">
          <el-card class="glass profile-card">
            <div class="avatar-section">
              <div class="avatar-container">
                <el-avatar :size="120" :src="profile.avatar" class="avatar">
                  {{ profile.displayName?.charAt(0) || 'U' }}
                </el-avatar>
                <el-button type="primary" size="small" class="upload-btn" @click="triggerUpload">
                  <el-icon><Upload /></el-icon>
                  更换头像
                </el-button>
                <input ref="fileInput" type="file" accept="image/*" @change="handleAvatarUpload" style="display: none" />
              </div>
              <div class="user-info">
                <h3 class="username">{{ profile.displayName || '未设置' }}</h3>
                <p class="role">{{ profile.role || '未知角色' }}</p>
                <p class="join-date">加入时间：{{ formatDate(profile.createdAt) }}</p>
              </div>
            </div>
          </el-card>

          <!-- 技能标签 -->
          <el-card class="glass skills-card">
            <div class="card-header">
              <h4>专业技能</h4>
              <el-button type="primary" size="small" @click="showSkillDialog = true">添加技能</el-button>
            </div>
            <div class="skills-list">
              <el-tag
                v-for="skill in profile.skills"
                :key="skill"
                closable
                @close="removeSkill(skill)"
                class="skill-tag"
              >
                {{ skill }}
              </el-tag>
              <el-tag v-if="profile.skills.length === 0" type="info" class="empty-tag">
                暂无技能标签
              </el-tag>
            </div>
          </el-card>
        </div>

        <!-- 右侧：详细信息编辑 -->
        <div class="right-section">
          <el-card class="glass">
            <el-form :model="profile" label-width="100px" class="profile-form">
              <h4 class="section-title">基本信息</h4>
              
              <el-form-item label="用户名">
                <el-input v-model="profile.username" disabled />
              </el-form-item>
              
              <el-form-item label="显示名称" required>
                <el-input v-model="profile.displayName" placeholder="请输入显示名称" />
              </el-form-item>
              
              <el-form-item label="邮箱">
                <el-input v-model="profile.email" placeholder="请输入邮箱地址" />
              </el-form-item>
              
              <el-form-item label="手机号">
                <el-input v-model="profile.phone" placeholder="请输入手机号" />
              </el-form-item>
              
              <el-form-item label="性别">
                <el-radio-group v-model="profile.gender">
                  <el-radio label="male">男</el-radio>
                  <el-radio label="female">女</el-radio>
                  <el-radio label="other">其他</el-radio>
                </el-radio-group>
              </el-form-item>
              
              <el-form-item label="生日">
                <el-date-picker
                  v-model="profile.birthday"
                  type="date"
                  placeholder="选择生日"
                  style="width: 100%"
                />
              </el-form-item>
              
              <h4 class="section-title">联系方式</h4>
              
              <el-form-item label="微信">
                <el-input v-model="profile.wechat" placeholder="请输入微信号" />
              </el-form-item>
              
              <el-form-item label="QQ">
                <el-input v-model="profile.qq" placeholder="请输入QQ号" />
              </el-form-item>
              
              <el-form-item label="个人网站">
                <el-input v-model="profile.website" placeholder="请输入个人网站" />
              </el-form-item>
              
              <h4 class="section-title">个人简介</h4>
              
              <el-form-item label="简介">
                <el-input
                  v-model="profile.bio"
                  type="textarea"
                  :rows="4"
                  placeholder="介绍一下自己..."
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>
              
              <el-form-item label="所在机构">
                <el-input v-model="profile.organization" placeholder="请输入所在机构" />
              </el-form-item>
              
              <el-form-item label="职位">
                <el-input v-model="profile.position" placeholder="请输入职位" />
              </el-form-item>
              
              <div class="form-actions">
                <el-button type="primary" @click="saveProfile" :loading="saving">
                  保存修改
                </el-button>
                <el-button @click="resetProfile">重置</el-button>
              </div>
            </el-form>
          </el-card>
        </div>
      </div>
    </div>

    <!-- 添加技能对话框 -->
    <el-dialog v-model="showSkillDialog" title="添加技能" width="400px">
      <el-form @submit.prevent="addSkill">
        <el-form-item label="技能名称">
          <el-input v-model="newSkill" placeholder="请输入技能名称" @keyup.enter="addSkill" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSkillDialog = false">取消</el-button>
        <el-button type="primary" @click="addSkill">添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'

const auth = useAuthStore()

// 用户资料数据
const profile = reactive({
  username: '',
  displayName: '',
  email: '',
  phone: '',
  gender: '',
  birthday: '',
  wechat: '',
  qq: '',
  website: '',
  bio: '',
  organization: '',
  position: '',
  avatar: '',
  role: '',
  createdAt: '',
  skills: [] as string[]
})

const originalProfile = ref({})
const saving = ref(false)
const showSkillDialog = ref(false)
const newSkill = ref('')
const fileInput = ref<HTMLInputElement>()

// 初始化用户资料
onMounted(() => {
  loadProfile()
})

function loadProfile() {
  // 从 auth store 获取用户信息
  const user = auth.user
  if (user) {
    Object.assign(profile, {
      username: user.username || '',
      displayName: user.displayName || '',
      email: user.email || '',
      phone: user.phone || '',
      gender: user.gender || '',
      birthday: user.birthday || '',
      wechat: user.wechat || '',
      qq: user.qq || '',
      website: user.website || '',
      bio: user.bio || '',
      organization: user.organization || '',
      position: user.position || '',
      avatar: user.avatar || '',
      role: user.role || '',
      createdAt: user.createdAt || '',
      skills: user.skills || []
    })
    originalProfile.value = { ...profile }
  }
}

function formatDate(dateStr: string) {
  if (!dateStr) return '未知'
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

function triggerUpload() {
  fileInput.value?.click()
}

function handleAvatarUpload(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!file) return
  
  // 简单的文件验证
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }
  
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过5MB')
    return
  }
  
  // 创建预览URL
  const reader = new FileReader()
  reader.onload = (e) => {
    profile.avatar = e.target?.result as string
  }
  reader.readAsDataURL(file)
}

function addSkill() {
  const skill = newSkill.value.trim()
  if (!skill) {
    ElMessage.warning('请输入技能名称')
    return
  }
  
  if (profile.skills.includes(skill)) {
    ElMessage.warning('该技能已存在')
    return
  }
  
  profile.skills.push(skill)
  newSkill.value = ''
  showSkillDialog.value = false
  ElMessage.success('技能添加成功')
}

function removeSkill(skill: string) {
  const index = profile.skills.indexOf(skill)
  if (index > -1) {
    profile.skills.splice(index, 1)
    ElMessage.success('技能删除成功')
  }
}

async function saveProfile() {
  saving.value = true
  try {
    // 这里应该调用API保存用户资料
    // await updateUserProfile(profile)
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 更新本地用户信息
    auth.user = { ...auth.user, ...profile }
    
    ElMessage.success('资料保存成功')
    originalProfile.value = { ...profile }
  } catch (error) {
    ElMessage.error('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

function resetProfile() {
  Object.assign(profile, originalProfile.value)
  ElMessage.info('已重置为原始数据')
}
</script>

<style scoped>
.profile-wrap {
  min-height: 100vh;
  display: grid;
  align-items: start;
  justify-items: center;
  padding-top: 18px;
  background: radial-gradient(1200px 600px at 10% 10%, rgba(99,102,241,0.25), transparent 40%),
              radial-gradient(1000px 500px at 90% 20%, rgba(16,185,129,0.25), transparent 45%),
              linear-gradient(135deg, #0f172a 0%, #1f2937 100%);
  position: relative;
  overflow: hidden;
}

.bg-blobs {
  position: absolute;
  inset: 0;
  pointer-events: none;
  filter: blur(40px);
  opacity: .6;
}

.blob {
  position: absolute;
  border-radius: 50%;
  transform: translate(-50%, -50%);
}

.b1 {
  width: 420px;
  height: 420px;
  left: 12%;
  top: 18%;
  background: #6366f1;
  animation: float 12s ease-in-out infinite;
}

.b2 {
  width: 380px;
  height: 380px;
  right: 1%;
  top: 10%;
  background: #22c55e;
  animation: float 14s ease-in-out infinite 1s;
}

.b3 {
  width: 220px;
  height: 220px;
  left: 70%;
  bottom: -5%;
  background: #06b6d4;
  animation: float 16s ease-in-out infinite 2s;
}

@keyframes float {
  0%, 100% { transform: translate(-50%, -50%) translateY(0); }
  50% { transform: translate(-50%, -50%) translateY(-16px); }
}

.page {
  width: min(96vw, 1200px);
  padding: 12px;
}

.glass {
  background: rgba(255,255,255,0.06);
  backdrop-filter: saturate(160%) blur(16px);
  box-shadow: 0 20px 50px rgba(0,0,0,0.45);
  color: #e5e7eb;
  border: 1px solid rgba(255,255,255,0.08);
  border-radius: 16px;
}

.brand-area {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  margin-bottom: 16px;
}

.logo {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  font-weight: 800;
  color: #0b1220;
  background: linear-gradient(135deg, #93c5fd 0%, #a78bfa 100%);
  box-shadow: inset 0 0 30px rgba(255,255,255,0.5), 0 10px 25px rgba(0,0,0,0.25);
}

.brand {
  margin: 0;
  font-size: 18px;
  color: #fff;
}

.subtitle {
  margin: 0;
  color: #cbd5e1;
  font-size: 12px;
}

.profile-content {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 20px;
}

.left-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.profile-card {
  padding: 20px;
}

.avatar-section {
  text-align: center;
}

.avatar-container {
  position: relative;
  display: inline-block;
  margin-bottom: 16px;
}

.avatar {
  border: 3px solid rgba(255,255,255,0.2);
  box-shadow: 0 8px 25px rgba(0,0,0,0.3);
}

.upload-btn {
  position: absolute;
  bottom: -8px;
  right: -8px;
  border-radius: 20px;
  padding: 4px 8px;
  font-size: 12px;
}

.user-info {
  text-align: center;
}

.username {
  margin: 0 0 8px 0;
  font-size: 20px;
  color: #fff;
}

.role {
  margin: 0 0 4px 0;
  color: #a78bfa;
  font-size: 14px;
}

.join-date {
  margin: 0;
  color: #94a3b8;
  font-size: 12px;
}

.skills-card {
  padding: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.card-header h4 {
  margin: 0;
  color: #fff;
  font-size: 16px;
}

.skills-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.skill-tag {
  background: rgba(99,102,241,0.2);
  border: 1px solid rgba(99,102,241,0.3);
  color: #a5b4fc;
}

.empty-tag {
  background: rgba(107,114,128,0.2);
  border: 1px solid rgba(107,114,128,0.3);
  color: #9ca3af;
}

.right-section {
  padding: 0;
}

.profile-form {
  padding: 20px;
}

.section-title {
  margin: 24px 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
  color: #fff;
  font-size: 16px;
}

.section-title:first-child {
  margin-top: 0;
}

.form-actions {
  margin-top: 24px;
  text-align: right;
}

.form-actions .el-button {
  margin-left: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .profile-content {
    grid-template-columns: 1fr;
  }
  
  .page {
    width: min(98vw, 1200px);
    padding: 8px;
  }
  
  .brand-area {
    padding: 8px 10px;
  }
  
  .logo {
    width: 34px;
    height: 34px;
    font-size: 14px;
  }
  
  .brand {
    font-size: 16px;
  }
  
  .b1 { width: 300px; height: 300px; }
  .b2 { width: 260px; height: 260px; }
  .b3 { width: 180px; height: 180px; }
}

/* 减少动效偏好支持 */
@media (prefers-reduced-motion: reduce) {
  .b1, .b2, .b3 { animation: none; }
}
</style>