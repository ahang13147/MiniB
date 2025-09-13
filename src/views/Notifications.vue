<template>
  <div class="notifications-wrap">
    <div class="bg-blobs">
      <div class="blob b1" />
      <div class="blob b2" />
      <div class="blob b3" />
    </div>

    <div class="page">
      <div class="brand-area glass">
        <div class="logo">🔔</div>
        <div class="title-group">
          <h2 class="brand">消息通知</h2>
          <p class="subtitle">重要信息 · 及时掌握</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" @click="markAllRead" :disabled="unreadCount === 0">
            全部已读
          </el-button>
          <el-button @click="clearAll" :disabled="notifications.length === 0">
            清空消息
          </el-button>
        </div>
      </div>

      <div class="notifications-content">
        <!-- 左侧：分类和筛选 -->
        <div class="left-section">
          <el-card class="glass filter-card">
            <h4 class="filter-title">消息分类</h4>
            <div class="category-list">
              <div
                v-for="category in categories"
                :key="category.key"
                :class="['category-item', { active: activeCategory === category.key }]"
                @click="setActiveCategory(category.key)"
              >
                <span class="category-icon">{{ category.icon }}</span>
                <span class="category-name">{{ category.name }}</span>
                <span class="category-count" v-if="category.count > 0">{{ category.count }}</span>
              </div>
            </div>
          </el-card>

          <el-card class="glass settings-card">
            <h4 class="filter-title">通知设置</h4>
            <div class="setting-item">
              <span class="setting-label">系统通知</span>
              <el-switch v-model="notificationSettings.system" />
            </div>
            <div class="setting-item">
              <span class="setting-label">项目通知</span>
              <el-switch v-model="notificationSettings.project" />
            </div>
            <div class="setting-item">
              <span class="setting-label">合作通知</span>
              <el-switch v-model="notificationSettings.collaboration" />
            </div>
            <div class="setting-item">
              <span class="setting-label">邮件通知</span>
              <el-switch v-model="notificationSettings.email" />
            </div>
          </el-card>
        </div>

        <!-- 右侧：消息列表 -->
        <div class="right-section">
          <el-card class="glass">
            <div class="message-header">
              <div class="search-bar">
                <el-input
                  v-model="searchKeyword"
                  placeholder="搜索消息内容..."
                  clearable
                  @input="filterMessages"
                >
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                </el-input>
              </div>
              <div class="sort-options">
                <el-select v-model="sortBy" placeholder="排序方式" @change="sortMessages">
                  <el-option label="最新优先" value="newest" />
                  <el-option label="最旧优先" value="oldest" />
                  <el-option label="未读优先" value="unread" />
                </el-select>
              </div>
            </div>

            <div class="message-list">
              <div
                v-for="message in filteredMessages"
                :key="message.id"
                :class="['message-item', { unread: !message.read, urgent: message.urgent }]"
                @click="toggleRead(message)"
              >
                <div class="message-avatar">
                  <el-avatar :size="40" :src="message.avatar">
                    {{ message.sender.charAt(0) }}
                  </el-avatar>
                  <div v-if="!message.read" class="unread-dot"></div>
                </div>
                
                <div class="message-content">
                  <div class="message-header">
                    <span class="sender">{{ message.sender }}</span>
                    <span class="category-tag" :class="message.category">
                      {{ getCategoryName(message.category) }}
                    </span>
                    <span class="time">{{ formatTime(message.createdAt) }}</span>
                  </div>
                  
                  <div class="message-title">{{ message.title }}</div>
                  <div class="message-preview">{{ message.content }}</div>
                  
                  <div class="message-actions" v-if="message.actions && message.actions.length > 0">
                    <el-button
                      v-for="action in message.actions"
                      :key="action.key"
                      :type="action.type"
                      size="small"
                      @click.stop="handleAction(message, action)"
                    >
                      {{ action.label }}
                    </el-button>
                  </div>
                </div>
                
                <div class="message-meta">
                  <el-icon v-if="message.urgent" class="urgent-icon"><Warning /></el-icon>
                  <el-button
                    text
                    type="danger"
                    size="small"
                    @click.stop="deleteMessage(message.id)"
                  >
                    删除
                  </el-button>
                </div>
              </div>
              
              <div v-if="filteredMessages.length === 0" class="empty-state">
                <el-icon size="48" class="empty-icon"><ChatDotRound /></el-icon>
                <p>暂无消息</p>
              </div>
            </div>
          </el-card>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Warning, ChatDotRound } from '@element-plus/icons-vue'

// 消息类型定义
interface Message {
  id: string
  title: string
  content: string
  sender: string
  avatar?: string
  category: 'system' | 'project' | 'collaboration' | 'security'
  read: boolean
  urgent: boolean
  createdAt: string
  actions?: Array<{
    key: string
    label: string
    type: 'primary' | 'success' | 'warning' | 'danger'
  }>
}

// 响应式数据
const notifications = ref<Message[]>([])
const activeCategory = ref<string>('all')
const searchKeyword = ref('')
const sortBy = ref('newest')

const notificationSettings = reactive({
  system: true,
  project: true,
  collaboration: true,
  email: false
})

// 分类配置
const categories = computed(() => [
  { key: 'all', name: '全部消息', icon: '📋', count: notifications.value.length },
  { key: 'system', name: '系统通知', icon: '⚙️', count: getCategoryCount('system') },
  { key: 'project', name: '项目通知', icon: '📁', count: getCategoryCount('project') },
  { key: 'collaboration', name: '合作通知', icon: '🤝', count: getCategoryCount('collaboration') },
  { key: 'security', name: '安全通知', icon: '🔒', count: getCategoryCount('security') }
])

// 计算属性
const unreadCount = computed(() => notifications.value.filter(m => !m.read).length)

const filteredMessages = computed(() => {
  let filtered = notifications.value

  // 按分类筛选
  if (activeCategory.value !== 'all') {
    filtered = filtered.filter(m => m.category === activeCategory.value)
  }

  // 按关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(m => 
      m.title.toLowerCase().includes(keyword) ||
      m.content.toLowerCase().includes(keyword) ||
      m.sender.toLowerCase().includes(keyword)
    )
  }

  // 排序
  filtered.sort((a, b) => {
    switch (sortBy.value) {
      case 'newest':
        return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      case 'oldest':
        return new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
      case 'unread':
        if (a.read !== b.read) return a.read ? 1 : -1
        return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      default:
        return 0
    }
  })

  return filtered
})

// 方法
function getCategoryCount(category: string) {
  return notifications.value.filter(m => m.category === category).length
}

function getCategoryName(category: string) {
  const categoryMap = {
    system: '系统',
    project: '项目',
    collaboration: '合作',
    security: '安全'
  }
  return categoryMap[category] || '未知'
}

function setActiveCategory(category: string) {
  activeCategory.value = category
}

function formatTime(dateStr: string) {
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  
  return date.toLocaleDateString('zh-CN')
}

function toggleRead(message: Message) {
  message.read = !message.read
  ElMessage.success(message.read ? '标记为已读' : '标记为未读')
}

function markAllRead() {
  notifications.value.forEach(m => m.read = true)
  ElMessage.success('全部标记为已读')
}

function clearAll() {
  ElMessageBox.confirm('确定要清空所有消息吗？', '确认清空', {
    type: 'warning'
  }).then(() => {
    notifications.value = []
    ElMessage.success('消息已清空')
  })
}

function deleteMessage(id: string) {
  const index = notifications.value.findIndex(m => m.id === id)
  if (index > -1) {
    notifications.value.splice(index, 1)
    ElMessage.success('消息已删除')
  }
}

function handleAction(message: Message, action: any) {
  ElMessage.info(`执行操作: ${action.label}`)
  // 这里可以添加具体的操作逻辑
}

function filterMessages() {
  // 搜索逻辑已在 computed 中处理
}

function sortMessages() {
  // 排序逻辑已在 computed 中处理
}

// 初始化数据
onMounted(() => {
  loadNotifications()
})

function loadNotifications() {
  // 模拟数据
  notifications.value = [
    {
      id: '1',
      title: '新项目邀请',
      content: '您被邀请参与"智能推荐系统"项目，请及时查看详情。',
      sender: '张教授',
      category: 'project',
      read: false,
      urgent: true,
      createdAt: new Date(Date.now() - 1000 * 60 * 30).toISOString(),
      actions: [
        { key: 'accept', label: '接受', type: 'primary' },
        { key: 'decline', label: '拒绝', type: 'danger' }
      ]
    },
    {
      id: '2',
      title: '系统维护通知',
      content: '系统将于今晚22:00-24:00进行维护升级，期间可能影响正常使用。',
      sender: '系统管理员',
      category: 'system',
      read: true,
      urgent: false,
      createdAt: new Date(Date.now() - 1000 * 60 * 60 * 2).toISOString()
    },
    {
      id: '3',
      title: '合作申请审核',
      content: '您的合作申请"校企联合实验室"已通过初步审核，等待最终确认。',
      sender: '李经理',
      category: 'collaboration',
      read: false,
      urgent: false,
      createdAt: new Date(Date.now() - 1000 * 60 * 60 * 4).toISOString(),
      actions: [
        { key: 'view', label: '查看详情', type: 'primary' }
      ]
    },
    {
      id: '4',
      title: '密码安全提醒',
      content: '检测到您的密码已使用超过90天，建议及时更新以确保账户安全。',
      sender: '安全中心',
      category: 'security',
      read: false,
      urgent: false,
      createdAt: new Date(Date.now() - 1000 * 60 * 60 * 24).toISOString(),
      actions: [
        { key: 'change', label: '立即修改', type: 'warning' }
      ]
    },
    {
      id: '5',
      title: '资源借用确认',
      content: '您借用的"高性能计算服务器"已确认，请在规定时间内使用。',
      sender: '资源管理员',
      category: 'project',
      read: true,
      urgent: false,
      createdAt: new Date(Date.now() - 1000 * 60 * 60 * 48).toISOString()
    }
  ]
}
</script>

<style scoped>
.notifications-wrap {
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
  justify-content: space-between;
  padding: 10px 14px;
  margin-bottom: 16px;
}

.brand-area .title-group {
  display: flex;
  align-items: center;
  gap: 12px;
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

.header-actions {
  display: flex;
  gap: 8px;
}

.notifications-content {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 20px;
}

.left-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-card, .settings-card {
  padding: 16px;
}

.filter-title {
  margin: 0 0 12px 0;
  color: #fff;
  font-size: 16px;
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.category-item:hover {
  background: rgba(255,255,255,0.05);
}

.category-item.active {
  background: rgba(99,102,241,0.2);
  border: 1px solid rgba(99,102,241,0.3);
}

.category-icon {
  font-size: 16px;
}

.category-name {
  flex: 1;
  color: #e5e7eb;
}

.category-count {
  background: rgba(99,102,241,0.3);
  color: #a5b4fc;
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 12px;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid rgba(255,255,255,0.05);
}

.setting-item:last-child {
  border-bottom: none;
}

.setting-label {
  color: #e5e7eb;
  font-size: 14px;
}

.right-section {
  padding: 0;
}

.message-header {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  padding: 16px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}

.search-bar {
  flex: 1;
}

.sort-options {
  width: 120px;
}

.message-list {
  max-height: 600px;
  overflow-y: auto;
}

.message-item {
  display: flex;
  gap: 12px;
  padding: 16px;
  border-bottom: 1px solid rgba(255,255,255,0.05);
  cursor: pointer;
  transition: all 0.2s ease;
}

.message-item:hover {
  background: rgba(255,255,255,0.02);
}

.message-item.unread {
  background: rgba(99,102,241,0.05);
  border-left: 3px solid #6366f1;
}

.message-item.urgent {
  border-left: 3px solid #f59e0b;
}

.message-avatar {
  position: relative;
  flex-shrink: 0;
}

.unread-dot {
  position: absolute;
  top: 0;
  right: 0;
  width: 8px;
  height: 8px;
  background: #ef4444;
  border-radius: 50%;
  border: 2px solid #1f2937;
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.sender {
  font-weight: 600;
  color: #fff;
}

.category-tag {
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
  background: rgba(99,102,241,0.2);
  color: #a5b4fc;
}

.time {
  margin-left: auto;
  color: #94a3b8;
  font-size: 12px;
}

.message-title {
  font-weight: 600;
  color: #e5e7eb;
  margin-bottom: 4px;
}

.message-preview {
  color: #94a3b8;
  font-size: 14px;
  line-height: 1.4;
  margin-bottom: 8px;
}

.message-actions {
  display: flex;
  gap: 8px;
}

.message-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.urgent-icon {
  color: #f59e0b;
  font-size: 16px;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #94a3b8;
}

.empty-icon {
  margin-bottom: 16px;
  opacity: 0.5;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .notifications-content {
    grid-template-columns: 1fr;
  }
  
  .brand-area {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-end;
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
  .category-item, .message-item { transition: none; }
}
</style>