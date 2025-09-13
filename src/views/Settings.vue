<template>
  <div class="settings-wrap">
    <div class="bg-blobs">
      <div class="blob b1" />
      <div class="blob b2" />
      <div class="blob b3" />
    </div>

    <div class="page">
      <div class="brand-area glass">
        <div class="logo">⚙️</div>
        <div class="title-group">
          <h2 class="brand">系统设置</h2>
          <p class="subtitle">个性化配置 · 安全防护</p>
        </div>
      </div>

      <div class="settings-content">
        <!-- 左侧：设置导航 -->
        <div class="left-section">
          <el-card class="glass nav-card">
            <div class="nav-list">
              <div
                v-for="item in navItems"
                :key="item.key"
                :class="['nav-item', { active: activeTab === item.key }]"
                @click="activeTab = item.key"
              >
                <span class="nav-icon">{{ item.icon }}</span>
                <span class="nav-label">{{ item.label }}</span>
              </div>
            </div>
          </el-card>
        </div>

        <!-- 右侧：设置内容 -->
        <div class="right-section">
          <el-card class="glass">
            <!-- 账户安全设置 -->
            <div v-if="activeTab === 'security'" class="settings-panel">
              <h3 class="panel-title">账户安全</h3>
              
              <div class="setting-group">
                <h4 class="group-title">密码安全</h4>
                <div class="setting-item">
                  <div class="setting-info">
                    <span class="setting-label">当前密码</span>
                    <span class="setting-desc">定期更新密码以确保账户安全</span>
                  </div>
                  <el-button type="primary" @click="showChangePassword = true">修改密码</el-button>
                </div>
                
                <div class="setting-item">
                  <div class="setting-info">
                    <span class="setting-label">两步验证</span>
                    <span class="setting-desc">为账户添加额外的安全保护</span>
                  </div>
                  <el-switch v-model="securitySettings.twoFactor" />
                </div>
              </div>

              <div class="setting-group">
                <h4 class="group-title">登录安全</h4>
                <div class="setting-item">
                  <div class="setting-info">
                    <span class="setting-label">登录通知</span>
                    <span class="setting-desc">新设备登录时发送邮件通知</span>
                  </div>
                  <el-switch v-model="securitySettings.loginNotification" />
                </div>
                
                <div class="setting-item">
                  <div class="setting-info">
                    <span class="setting-label">会话超时</span>
                    <span class="setting-desc">自动登出时间设置</span>
                  </div>
                  <el-select v-model="securitySettings.sessionTimeout" style="width: 120px">
                    <el-option label="30分钟" value="30" />
                    <el-option label="1小时" value="60" />
                    <el-option label="2小时" value="120" />
                    <el-option label="4小时" value="240" />
                  </el-select>
                </div>
              </div>

              <div class="setting-group">
                <h4 class="group-title">登录记录</h4>
                <div class="login-history">
                  <div v-for="record in loginHistory" :key="record.id" class="history-item">
                    <div class="history-info">
                      <span class="device">{{ record.device }}</span>
                      <span class="location">{{ record.location }}</span>
                      <span class="time">{{ formatTime(record.time) }}</span>
                    </div>
                    <el-tag :type="record.current ? 'success' : 'info'">
                      {{ record.current ? '当前设备' : '其他设备' }}
                    </el-tag>
                  </div>
                </div>
              </div>
            </div>

            <!-- 隐私设置 -->
            <div v-if="activeTab === 'privacy'" class="settings-panel">
              <h3 class="panel-title">隐私设置</h3>
              
              <div class="setting-group">
                <h4 class="group-title">个人信息可见性</h4>
                <div class="setting-item">
                  <div class="setting-info">
                    <span class="setting-label">个人资料</span>
                    <span class="setting-desc">其他用户是否可以看到您的个人资料</span>
                  </div>
                  <el-select v-model="privacySettings.profileVisibility" style="width: 120px">
                    <el-option label="公开" value="public" />
                    <el-option label="仅好友" value="friends" />
                    <el-option label="私密" value="private" />
                  </el-select>
                </div>
                
                <div class="setting-item">
                  <div class="setting-info">
                    <span class="setting-label">联系方式</span>
                    <span class="setting-desc">是否显示邮箱、电话等联系方式</span>
                  </div>
                  <el-switch v-model="privacySettings.showContact" />
                </div>
                
                <div class="setting-item">
                  <div class="setting-info">
                    <span class="setting-label">在线状态</span>
                    <span class="setting-desc">是否显示在线/离线状态</span>
                  </div>
                  <el-switch v-model="privacySettings.showOnlineStatus" />
                </div>
              </div>

              <div class="setting-group">
                <h4 class="group-title">数据使用</h4>
                <div class="setting-item">
                  <div class="setting-info">
                    <span class="setting-label">数据分析</span>
                    <span class="setting-desc">允许系统分析使用数据以改善服务</span>
                  </div>
                  <el-switch v-model="privacySettings.dataAnalysis" />
                </div>
                
                <div class="setting-item">
                  <div class="setting-info">
                    <span class="setting-label">个性化推荐</span>
                    <span class="setting-desc">基于使用习惯提供个性化内容推荐</span>
                  </div>
                  <el-switch v-model="privacySettings.personalizedRecommendation" />
                </div>
              </div>
            </div>

            <!-- 通知偏好设置 -->
            <div v-if="activeTab === 'notifications'" class="settings-panel">
              <h3 class="panel-title">通知偏好</h3>
              
              <div class="setting-group">
                <h4 class="group-title">通知方式</h4>
                <div class="setting-item">
                  <div class="setting-info">
                    <span class="setting-label">浏览器通知</span>
                    <span class="setting-desc">在浏览器中显示桌面通知</span>
                  </div>
                  <el-switch v-model="notificationSettings.browser" />
                </div>
                
                <div class="setting-item">
                  <div class="setting-info">
                    <span class="setting-label">邮件通知</span>
                    <span class="setting-desc">通过邮件发送重要通知</span>
                  </div>
                  <el-switch v-model="notificationSettings.email" />
                </div>
                
                <div class="setting-item">
                  <div class="setting-info">
                    <span class="setting-label">短信通知</span>
                    <span class="setting-desc">通过短信发送紧急通知</span>
                  </div>
                  <el-switch v-model="notificationSettings.sms" />
                </div>
              </div>

              <div class="setting-group">
                <h4 class="group-title">通知类型</h4>
                <div class="setting-item">
                  <div class="setting-info">
                    <span class="setting-label">系统通知</span>
                    <span class="setting-desc">系统维护、更新等重要信息</span>
                  </div>
                  <el-switch v-model="notificationSettings.system" />
                </div>
                
                <div class="setting-item">
                  <div class="setting-info">
                    <span class="setting-label">项目通知</span>
                    <span class="setting-desc">项目相关的重要更新</span>
                  </div>
                  <el-switch v-model="notificationSettings.project" />
                </div>
                
                <div class="setting-item">
                  <div class="setting-info">
                    <span class="setting-label">合作通知</span>
                    <span class="setting-desc">合作申请、审批等消息</span>
                  </div>
                  <el-switch v-model="notificationSettings.collaboration" />
                </div>
                
                <div class="setting-item">
                  <div class="setting-info">
                    <span class="setting-label">营销通知</span>
                    <span class="setting-desc">产品更新、活动推广等信息</span>
                  </div>
                  <el-switch v-model="notificationSettings.marketing" />
                </div>
              </div>

              <div class="setting-group">
                <h4 class="group-title">免打扰时间</h4>
                <div class="setting-item">
                  <div class="setting-info">
                    <span class="setting-label">启用免打扰</span>
                    <span class="setting-desc">在指定时间段内不接收通知</span>
                  </div>
                  <el-switch v-model="notificationSettings.dndEnabled" />
                </div>
                
                <div v-if="notificationSettings.dndEnabled" class="setting-item">
                  <div class="setting-info">
                    <span class="setting-label">免打扰时间</span>
                    <span class="setting-desc">设置免打扰的时间段</span>
                  </div>
                  <el-time-picker
                    v-model="notificationSettings.dndStart"
                    placeholder="开始时间"
                    format="HH:mm"
                    style="width: 120px; margin-right: 8px"
                  />
                  <span style="color: #94a3b8; margin: 0 8px">至</span>
                  <el-time-picker
                    v-model="notificationSettings.dndEnd"
                    placeholder="结束时间"
                    format="HH:mm"
                    style="width: 120px"
                  />
                </div>
              </div>
            </div>

            <!-- 界面主题选择 -->
            <div v-if="activeTab === 'appearance'" class="settings-panel">
              <h3 class="panel-title">界面设置</h3>
              
              <div class="setting-group">
                <h4 class="group-title">主题选择</h4>
                <div class="theme-options">
                  <div
                    v-for="theme in themes"
                    :key="theme.key"
                    :class="['theme-item', { active: currentTheme === theme.key }]"
                    @click="changeTheme(theme.key)"
                  >
                    <div class="theme-preview" :class="theme.key">
                      <div class="preview-header"></div>
                      <div class="preview-content">
                        <div class="preview-card"></div>
                        <div class="preview-card"></div>
                      </div>
                    </div>
                    <span class="theme-name">{{ theme.name }}</span>
                  </div>
                </div>
              </div>

              <div class="setting-group">
                <h4 class="group-title">界面设置</h4>
                <div class="setting-item">
                  <div class="setting-info">
                    <span class="setting-label">语言</span>
                    <span class="setting-desc">选择界面显示语言</span>
                  </div>
                  <el-select v-model="appearanceSettings.language" style="width: 120px">
                    <el-option label="简体中文" value="zh-CN" />
                    <el-option label="English" value="en-US" />
                  </el-select>
                </div>
                
                <div class="setting-item">
                  <div class="setting-info">
                    <span class="setting-label">字体大小</span>
                    <span class="setting-desc">调整界面字体大小</span>
                  </div>
                  <el-select v-model="appearanceSettings.fontSize" style="width: 120px">
                    <el-option label="小" value="small" />
                    <el-option label="中" value="medium" />
                    <el-option label="大" value="large" />
                  </el-select>
                </div>
                
                <div class="setting-item">
                  <div class="setting-info">
                    <span class="setting-label">紧凑模式</span>
                    <span class="setting-desc">使用更紧凑的界面布局</span>
                  </div>
                  <el-switch v-model="appearanceSettings.compactMode" />
                </div>
              </div>
            </div>

            <!-- 保存按钮 -->
            <div class="settings-actions">
              <el-button type="primary" @click="saveSettings" :loading="saving">
                保存设置
              </el-button>
              <el-button @click="resetSettings">重置</el-button>
            </div>
          </el-card>
        </div>
      </div>
    </div>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="showChangePassword" title="修改密码" width="400px">
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef">
        <el-form-item label="当前密码" prop="currentPassword">
          <el-input v-model="passwordForm.currentPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showChangePassword = false">取消</el-button>
        <el-button type="primary" @click="changePassword">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// 响应式数据
const activeTab = ref('security')
const saving = ref(false)
const showChangePassword = ref(false)

// 导航项
const navItems = [
  { key: 'security', label: '账户安全', icon: '🔒' },
  { key: 'privacy', label: '隐私设置', icon: '🛡️' },
  { key: 'notifications', label: '通知偏好', icon: '🔔' },
  { key: 'appearance', label: '界面设置', icon: '🎨' }
]

// 安全设置
const securitySettings = reactive({
  twoFactor: false,
  loginNotification: true,
  sessionTimeout: '120'
})

// 隐私设置
const privacySettings = reactive({
  profileVisibility: 'public',
  showContact: true,
  showOnlineStatus: true,
  dataAnalysis: true,
  personalizedRecommendation: true
})

// 通知设置
const notificationSettings = reactive({
  browser: true,
  email: false,
  sms: false,
  system: true,
  project: true,
  collaboration: true,
  marketing: false,
  dndEnabled: false,
  dndStart: '',
  dndEnd: ''
})

// 界面设置
const appearanceSettings = reactive({
  language: 'zh-CN',
  fontSize: 'medium',
  compactMode: false
})

// 主题选项
const themes = [
  { key: 'dark', name: '深色主题' },
  { key: 'light', name: '浅色主题' },
  { key: 'auto', name: '跟随系统' }
]

const currentTheme = ref('dark')

// 登录历史
const loginHistory = ref([
  {
    id: '1',
    device: 'Chrome on Windows',
    location: '北京市',
    time: new Date(Date.now() - 1000 * 60 * 30).toISOString(),
    current: true
  },
  {
    id: '2',
    device: 'Safari on iPhone',
    location: '上海市',
    time: new Date(Date.now() - 1000 * 60 * 60 * 2).toISOString(),
    current: false
  },
  {
    id: '3',
    device: 'Firefox on Mac',
    location: '广州市',
    time: new Date(Date.now() - 1000 * 60 * 60 * 24).toISOString(),
    current: false
  }
])

// 密码修改表单
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordFormRef = ref()

const passwordRules = {
  currentPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: Function) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 方法
function formatTime(dateStr: string) {
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

function changeTheme(theme: string) {
  currentTheme.value = theme
  // 这里可以添加主题切换逻辑
  ElMessage.success(`已切换到${themes.find(t => t.key === theme)?.name}`)
}

async function saveSettings() {
  saving.value = true
  try {
    // 这里应该调用API保存设置
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success('设置保存成功')
  } catch (error) {
    ElMessage.error('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

function resetSettings() {
  ElMessageBox.confirm('确定要重置所有设置吗？', '确认重置', {
    type: 'warning'
  }).then(() => {
    // 重置所有设置到默认值
    Object.assign(securitySettings, {
      twoFactor: false,
      loginNotification: true,
      sessionTimeout: '120'
    })
    Object.assign(privacySettings, {
      profileVisibility: 'public',
      showContact: true,
      showOnlineStatus: true,
      dataAnalysis: true,
      personalizedRecommendation: true
    })
    Object.assign(notificationSettings, {
      browser: true,
      email: false,
      sms: false,
      system: true,
      project: true,
      collaboration: true,
      marketing: false,
      dndEnabled: false,
      dndStart: '',
      dndEnd: ''
    })
    Object.assign(appearanceSettings, {
      language: 'zh-CN',
      fontSize: 'medium',
      compactMode: false
    })
    currentTheme.value = 'dark'
    ElMessage.success('设置已重置')
  })
}

async function changePassword() {
  try {
    await passwordFormRef.value?.validate()
    // 这里应该调用API修改密码
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success('密码修改成功')
    showChangePassword.value = false
    Object.assign(passwordForm, {
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    })
  } catch (error) {
    ElMessage.error('密码修改失败，请重试')
  }
}
</script>

<style scoped>
.settings-wrap {
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

.settings-content {
  display: grid;
  grid-template-columns: 250px 1fr;
  gap: 20px;
}

.left-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.nav-card {
  padding: 16px;
}

.nav-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.nav-item:hover {
  background: rgba(255,255,255,0.05);
}

.nav-item.active {
  background: rgba(99,102,241,0.2);
  border: 1px solid rgba(99,102,241,0.3);
}

.nav-icon {
  font-size: 16px;
}

.nav-label {
  color: #e5e7eb;
  font-size: 14px;
}

.right-section {
  padding: 0;
}

.settings-panel {
  padding: 24px;
}

.panel-title {
  margin: 0 0 24px 0;
  color: #fff;
  font-size: 20px;
}

.setting-group {
  margin-bottom: 32px;
}

.group-title {
  margin: 0 0 16px 0;
  color: #e5e7eb;
  font-size: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid rgba(255,255,255,0.05);
}

.setting-item:last-child {
  border-bottom: none;
}

.setting-info {
  flex: 1;
}

.setting-label {
  display: block;
  color: #fff;
  font-size: 14px;
  margin-bottom: 4px;
}

.setting-desc {
  color: #94a3b8;
  font-size: 12px;
}

.login-history {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: rgba(255,255,255,0.02);
  border-radius: 8px;
}

.history-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.device {
  color: #fff;
  font-size: 14px;
}

.location, .time {
  color: #94a3b8;
  font-size: 12px;
}

.theme-options {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 16px;
}

.theme-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 2px solid transparent;
}

.theme-item:hover {
  background: rgba(255,255,255,0.05);
}

.theme-item.active {
  border-color: #6366f1;
  background: rgba(99,102,241,0.1);
}

.theme-preview {
  width: 80px;
  height: 60px;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid rgba(255,255,255,0.1);
}

.theme-preview.dark {
  background: #1f2937;
}

.theme-preview.light {
  background: #f9fafb;
}

.theme-preview.auto {
  background: linear-gradient(45deg, #1f2937 50%, #f9fafb 50%);
}

.preview-header {
  height: 16px;
  background: rgba(99,102,241,0.3);
}

.preview-content {
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.preview-card {
  height: 8px;
  background: rgba(255,255,255,0.1);
  border-radius: 2px;
}

.theme-name {
  color: #e5e7eb;
  font-size: 12px;
}

.settings-actions {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid rgba(255,255,255,0.1);
  text-align: right;
}

.settings-actions .el-button {
  margin-left: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .settings-content {
    grid-template-columns: 1fr;
  }
  
  .page {
    width: min(98vw, 1200px);
    padding: 8px;
  }
  
  .b1 { width: 300px; height: 300px; }
  .b2 { width: 260px; height: 260px; }
  .b3 { width: 180px; height: 180px; }
}

/* 减少动效偏好支持 */
@media (prefers-reduced-motion: reduce) {
  .b1, .b2, .b3 { animation: none; }
  .nav-item, .theme-item { transition: none; }
}
</style>