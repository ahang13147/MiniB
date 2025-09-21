<template>
  <div class="achievement-center">
    <div class="page-header">
      <div class="header-content">
        <h1>成果展示中心</h1>
        <p>学生学术成果与竞赛获奖展示平台</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateDialog = true" v-if="canCreate">
          <el-icon><Plus /></el-icon>
          添加成果
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalAchievements }}</div>
          <div class="stat-label">总成果数</div>
        </div>
        <el-icon class="stat-icon"><Trophy /></el-icon>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-value">{{ stats.verifiedAchievements }}</div>
          <div class="stat-label">已验证</div>
        </div>
        <el-icon class="stat-icon"><Check /></el-icon>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-value">{{ stats.pendingVerification }}</div>
          <div class="stat-label">待审核</div>
        </div>
        <el-icon class="stat-icon"><Clock /></el-icon>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-value">{{ topStudents.length }}</div>
          <div class="stat-label">优秀学生</div>
        </div>
        <el-icon class="stat-icon"><User /></el-icon>
      </el-card>
    </div>

    <!-- 筛选和搜索 -->
    <el-card class="filter-card">
      <div class="filter-row">
        <el-input
          v-model="searchForm.search"
          placeholder="搜索成果名称或描述"
          style="width: 300px"
          @input="handleSearch"
          clearable
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <el-select v-model="searchForm.type" placeholder="成果类型" clearable @change="loadAchievements">
          <el-option label="竞赛获奖" value="COMPETITION" />
          <el-option label="科研成果" value="RESEARCH" />
          <el-option label="项目作品" value="PROJECT" />
          <el-option label="专利发明" value="PATENT" />
          <el-option label="论文发表" value="PUBLICATION" />
          <el-option label="资格证书" value="CERTIFICATE" />
          <el-option label="其他成果" value="OTHER" />
        </el-select>

        <el-select v-model="searchForm.level" placeholder="成果级别" clearable @change="loadAchievements">
          <el-option label="校级" value="SCHOOL" />
          <el-option label="市级" value="CITY" />
          <el-option label="省级" value="PROVINCE" />
          <el-option label="国家级" value="NATIONAL" />
          <el-option label="国际级" value="INTERNATIONAL" />
        </el-select>

        <el-select v-model="searchForm.status" placeholder="验证状态" clearable @change="loadAchievements" v-if="canManage">
          <el-option label="草稿" value="DRAFT" />
          <el-option label="已发布" value="PUBLISHED" />
          <el-option label="已验证" value="VERIFIED" />
          <el-option label="已拒绝" value="REJECTED" />
        </el-select>

        <el-select v-model="searchForm.visibility" placeholder="可见性" clearable @change="loadAchievements">
          <el-option label="私有" value="PRIVATE" />
          <el-option label="校内可见" value="UNIVERSITY" />
          <el-option label="公开" value="PUBLIC" />
        </el-select>
      </div>
    </el-card>

    <!-- 成果列表 -->
    <el-row :gutter="20">
      <el-col :span="18">
        <div class="achievement-list">
          <el-card v-for="achievement in achievements" :key="achievement.id" class="achievement-card">
            <div class="achievement-header">
              <div class="achievement-info">
                <h3 class="achievement-title">{{ achievement.title }}</h3>
                <div class="achievement-meta">
                  <el-tag :type="getTypeTagType(achievement.achievementType)" size="small">
                    {{ getTypeDisplayName(achievement.achievementType) }}
                  </el-tag>
                  <el-tag :type="getLevelTagType(achievement.level)" size="small">
                    {{ getLevelDisplayName(achievement.level) }}
                  </el-tag>
                  <el-tag :type="getStatusTagType(achievement.status)" size="small">
                    {{ getStatusDisplayName(achievement.status) }}
                  </el-tag>
                  <span class="date">{{ formatDate(achievement.achievementDate) }}</span>
                </div>
              </div>
              <div class="achievement-actions">
                <el-dropdown @command="handleCommand($event, achievement)">
                  <el-button link type="primary">
                    操作<el-icon><ArrowDown /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="view">查看详情</el-dropdown-item>
                      <el-dropdown-item command="edit" v-if="canEdit(achievement)">编辑</el-dropdown-item>
                      <el-dropdown-item command="verify" v-if="canVerify(achievement)" divided>验证</el-dropdown-item>
                      <el-dropdown-item command="publish" v-if="canPublish(achievement)">发布</el-dropdown-item>
                      <el-dropdown-item command="delete" v-if="canDelete(achievement)" divided>删除</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
            
            <div class="achievement-content">
              <div class="student-info">
                <el-avatar :size="40" :src="getStudentAvatar(achievement.studentId)">
                  {{ achievement.studentName.charAt(0) }}
                </el-avatar>
                <div class="student-details">
                  <div class="student-name">{{ achievement.studentName }}</div>
                  <div class="student-major">{{ achievement.studentMajor }} · {{ achievement.studentGrade }}</div>
                </div>
              </div>
              
              <p class="achievement-description">{{ achievement.description }}</p>
              
              <div class="achievement-details">
                <div class="detail-item" v-if="achievement.organization">
                  <span class="label">颁发机构：</span>
                  <span class="value">{{ achievement.organization }}</span>
                </div>
                <div class="detail-item" v-if="achievement.awardRank">
                  <span class="label">获奖等级：</span>
                  <span class="value">{{ achievement.awardRank }}</span>
                </div>
                <div class="detail-item" v-if="achievement.category">
                  <span class="label">成果分类：</span>
                  <span class="value">{{ achievement.category }}</span>
                </div>
              </div>
              
              <div class="achievement-attachments" v-if="achievement.certificateUrl || achievement.attachmentUrls?.length">
                <el-button link type="primary" @click="viewCertificate(achievement)" v-if="achievement.certificateUrl">
                  <el-icon><Document /></el-icon>
                  查看证书
                </el-button>
                <el-button link type="primary" @click="viewAttachments(achievement)" v-if="achievement.attachmentUrls?.length">
                  <el-icon><Paperclip /></el-icon>
                  相关附件 ({{ achievement.attachmentUrls.length }})
                </el-button>
              </div>
            </div>
          </el-card>

          <!-- 分页 -->
          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="pagination.page"
              v-model:page-size="pagination.size"
              :total="pagination.total"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
              @size-change="loadAchievements"
              @current-change="loadAchievements"
            />
          </div>
        </div>
      </el-col>
      
      <el-col :span="6">
        <!-- 侧边栏 - 优秀学生排行 -->
        <el-card class="sidebar-card">
          <template #header>
            <div class="sidebar-header">
              <el-icon><Star /></el-icon>
              优秀学生排行
            </div>
          </template>
          <div class="student-ranking">
            <div v-for="(student, index) in topStudents" :key="student.studentId" class="ranking-item">
              <div class="rank-number" :class="getRankClass(index)">{{ index + 1 }}</div>
              <div class="student-info">
                <div class="student-name">{{ student.studentName }}</div>
                <div class="student-major">{{ student.studentMajor }}</div>
                <div class="achievement-count">{{ student.verifiedCount }} 个成果</div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 成果类型统计 -->
        <el-card class="sidebar-card">
          <template #header>
            <div class="sidebar-header">
              <el-icon><PieChart /></el-icon>
              成果类型分布
            </div>
          </template>
          <div class="type-stats">
            <div v-for="typeStat in typeStats" :key="typeStat.type" class="stat-item">
              <div class="stat-label">{{ getTypeDisplayName(typeStat.type) }}</div>
              <div class="stat-bar">
                <div class="stat-progress" :style="{ width: getStatPercentage(typeStat.verifiedCount) + '%' }"></div>
                <span class="stat-count">{{ typeStat.verifiedCount }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 创建/编辑成果对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      :title="editingAchievement ? '编辑成果' : '添加新成果'"
      width="800px"
      @closed="resetForm"
    >
      <el-form :model="achievementForm" :rules="achievementRules" ref="achievementFormRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="成果名称" prop="title">
              <el-input v-model="achievementForm.title" placeholder="请输入成果名称" />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="成果类型" prop="achievementType">
              <el-select v-model="achievementForm.achievementType" placeholder="选择成果类型">
                <el-option label="竞赛获奖" value="COMPETITION" />
                <el-option label="科研成果" value="RESEARCH" />
                <el-option label="项目作品" value="PROJECT" />
                <el-option label="专利发明" value="PATENT" />
                <el-option label="论文发表" value="PUBLICATION" />
                <el-option label="资格证书" value="CERTIFICATE" />
                <el-option label="其他成果" value="OTHER" />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="成果级别" prop="level">
              <el-select v-model="achievementForm.level" placeholder="选择成果级别">
                <el-option label="校级" value="SCHOOL" />
                <el-option label="市级" value="CITY" />
                <el-option label="省级" value="PROVINCE" />
                <el-option label="国家级" value="NATIONAL" />
                <el-option label="国际级" value="INTERNATIONAL" />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="获奖等级" prop="awardRank">
              <el-input v-model="achievementForm.awardRank" placeholder="如：一等奖、金奖等" />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="颁发机构" prop="organization">
              <el-input v-model="achievementForm.organization" placeholder="请输入颁发机构" />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="成果分类" prop="category">
              <el-input v-model="achievementForm.category" placeholder="请输入成果分类" />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="获得日期" prop="achievementDate">
              <el-date-picker v-model="achievementForm.achievementDate" type="date" placeholder="选择获得日期" />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="可见性" prop="visibility">
              <el-select v-model="achievementForm.visibility" placeholder="选择可见性">
                <el-option label="私有" value="PRIVATE" />
                <el-option label="校内可见" value="UNIVERSITY" />
                <el-option label="公开" value="PUBLIC" />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="24">
            <el-form-item label="成果描述" prop="description">
              <el-input v-model="achievementForm.description" type="textarea" :rows="4" placeholder="请详细描述成果内容" />
            </el-form-item>
          </el-col>
          
          <el-col :span="24">
            <el-form-item label="证书链接" prop="certificateUrl">
              <el-input v-model="achievementForm.certificateUrl" placeholder="请输入证书文件链接" />
            </el-form-item>
          </el-col>
          
          <el-col :span="24">
            <el-form-item label="相关附件" prop="attachmentUrls">
              <el-input v-model="attachmentUrlsInput" placeholder="请输入附件链接，多个链接用逗号分隔" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="saveAchievement" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 成果详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="成果详情" width="900px">
      <div v-if="currentAchievement" class="achievement-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="成果名称">{{ currentAchievement.title }}</el-descriptions-item>
          <el-descriptions-item label="成果类型">{{ getTypeDisplayName(currentAchievement.achievementType) }}</el-descriptions-item>
          <el-descriptions-item label="成果级别">{{ getLevelDisplayName(currentAchievement.level) }}</el-descriptions-item>
          <el-descriptions-item label="获奖等级">{{ currentAchievement.awardRank || '-' }}</el-descriptions-item>
          <el-descriptions-item label="颁发机构">{{ currentAchievement.organization || '-' }}</el-descriptions-item>
          <el-descriptions-item label="获得日期">{{ formatDate(currentAchievement.achievementDate) }}</el-descriptions-item>
          <el-descriptions-item label="学生姓名">{{ currentAchievement.studentName }}</el-descriptions-item>
          <el-descriptions-item label="专业年级">{{ currentAchievement.studentMajor }} · {{ currentAchievement.studentGrade }}</el-descriptions-item>
          <el-descriptions-item label="验证状态">
            <el-tag :type="getStatusTagType(currentAchievement.status)">
              {{ getStatusDisplayName(currentAchievement.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="可见性">
            <el-tag :type="getVisibilityTagType(currentAchievement.visibility)">
              {{ getVisibilityDisplayName(currentAchievement.visibility) }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
        
        <el-divider content-position="left">成果描述</el-divider>
        <p>{{ currentAchievement.description }}</p>
        
        <el-divider content-position="left">验证备注</el-divider>
        <p v-if="currentAchievement.verificationNotes">{{ currentAchievement.verificationNotes }}</p>
        <p v-else class="no-notes">暂无验证备注</p>
      </div>
    </el-dialog>

    <!-- 验证成果对话框 -->
    <el-dialog v-model="showVerifyDialog" title="验证成果" width="600px">
      <el-form :model="verifyForm" :rules="verifyRules" ref="verifyFormRef" label-width="100px">
        <el-form-item label="验证结果" prop="status">
          <el-radio-group v-model="verifyForm.status">
            <el-radio label="VERIFIED">通过验证</el-radio>
            <el-radio label="REJECTED">拒绝验证</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="验证备注" prop="verificationNotes">
          <el-input v-model="verifyForm.verificationNotes" type="textarea" :rows="4" 
                   placeholder="请输入验证意见或拒绝理由" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showVerifyDialog = false">取消</el-button>
        <el-button type="primary" @click="submitVerification" :loading="verifying">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, Search, Trophy, Check, Clock, User, Star, PieChart, 
  ArrowDown, Document, Paperclip 
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { achievementApi } from '@/api/achievements'

const auth = useAuthStore()

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const verifying = ref(false)
const achievements = ref([])
const stats = ref({
  totalAchievements: 0,
  verifiedAchievements: 0,
  pendingVerification: 0
})
const topStudents = ref([])
const typeStats = ref([])

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 搜索表单
const searchForm = reactive({
  search: '',
  type: '',
  level: '',
  status: '',
  visibility: ''
})

// 对话框状态
const showCreateDialog = ref(false)
const showDetailDialog = ref(false)
const showVerifyDialog = ref(false)
const editingAchievement = ref(null)
const currentAchievement = ref(null)
const verifyingAchievement = ref(null)

// 表单数据
const achievementForm = reactive({
  title: '',
  description: '',
  achievementType: '',
  category: '',
  level: '',
  awardRank: '',
  organization: '',
  achievementDate: '',
  visibility: 'UNIVERSITY',
  certificateUrl: '',
  attachmentUrls: []
})

const verifyForm = reactive({
  status: 'VERIFIED',
  verificationNotes: ''
})

const attachmentUrlsInput = ref('')

// 表单验证规则
const achievementRules = {
  title: [{ required: true, message: '请输入成果名称', trigger: 'blur' }],
  achievementType: [{ required: true, message: '请选择成果类型', trigger: 'change' }],
  level: [{ required: true, message: '请选择成果级别', trigger: 'change' }],
  achievementDate: [{ required: true, message: '请选择获得日期', trigger: 'change' }]
}

const verifyRules = {
  status: [{ required: true, message: '请选择验证结果', trigger: 'change' }]
}

const achievementFormRef = ref()
const verifyFormRef = ref()

// 计算属性
const canCreate = computed(() => {
  return auth.user?.role === 'STUDENT'
})

const canManage = computed(() => {
  return auth.hasPermission('ACHIEVEMENT_MANAGE') || auth.hasPermission('ACHIEVEMENT_VERIFY')
})

// 方法
const loadAchievements = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      ...searchForm
    }
    
    // 如果是学生，只显示自己的成果
    if (auth.user?.role === 'STUDENT') {
      params.studentId = auth.user.id
    }
    
    const response = await achievementApi.getAchievements(params)
    achievements.value = response.data.items
    pagination.total = response.data.total
  } catch (error) {
    ElMessage.error('加载成果列表失败')
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  try {
    const response = await achievementApi.getStats()
    stats.value = response.data
    topStudents.value = response.data.topStudents?.slice(0, 10) || []
    typeStats.value = response.data.typeStats || []
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadAchievements()
}

const handleCommand = (command, achievement) => {
  switch (command) {
    case 'view':
      viewAchievement(achievement)
      break
    case 'edit':
      editAchievement(achievement)
      break
    case 'verify':
      verifyAchievement(achievement)
      break
    case 'publish':
      publishAchievement(achievement)
      break
    case 'delete':
      deleteAchievement(achievement)
      break
  }
}

const viewAchievement = (achievement) => {
  currentAchievement.value = achievement
  showDetailDialog.value = true
}

const editAchievement = (achievement) => {
  editingAchievement.value = achievement
  Object.assign(achievementForm, {
    title: achievement.title,
    description: achievement.description,
    achievementType: achievement.achievementType,
    category: achievement.category,
    level: achievement.level,
    awardRank: achievement.awardRank,
    organization: achievement.organization,
    achievementDate: achievement.achievementDate,
    visibility: achievement.visibility,
    certificateUrl: achievement.certificateUrl
  })
  attachmentUrlsInput.value = achievement.attachmentUrls?.join(', ') || ''
  showCreateDialog.value = true
}

const verifyAchievement = (achievement) => {
  verifyingAchievement.value = achievement
  verifyForm.status = 'VERIFIED'
  verifyForm.verificationNotes = ''
  showVerifyDialog.value = true
}

const publishAchievement = async (achievement) => {
  try {
    await achievementApi.publishAchievement(achievement.id)
    ElMessage.success('成果发布成功')
    loadAchievements()
  } catch (error) {
    ElMessage.error('发布成果失败')
  }
}

const deleteAchievement = async (achievement) => {
  try {
    await ElMessageBox.confirm(`确定要删除成果"${achievement.title}"吗？`, '确认删除', {
      type: 'warning'
    })
    
    await achievementApi.deleteAchievement(achievement.id)
    ElMessage.success('成果删除成功')
    loadAchievements()
    loadStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除成果失败')
    }
  }
}

const saveAchievement = async () => {
  if (!achievementFormRef.value) return
  
  const valid = await achievementFormRef.value.validate()
  if (!valid) return
  
  saving.value = true
  try {
    // 处理附件链接
    achievementForm.attachmentUrls = attachmentUrlsInput.value
      .split(',')
      .map(url => url.trim())
      .filter(url => url)
    
    if (editingAchievement.value) {
      await achievementApi.updateAchievement(editingAchievement.value.id, achievementForm)
      ElMessage.success('成果更新成功')
    } else {
      await achievementApi.createAchievement(achievementForm)
      ElMessage.success('成果添加成功')
    }
    
    showCreateDialog.value = false
    loadAchievements()
    loadStats()
  } catch (error) {
    ElMessage.error('保存成果失败')
  } finally {
    saving.value = false
  }
}

const submitVerification = async () => {
  if (!verifyFormRef.value || !verifyingAchievement.value) return
  
  const valid = await verifyFormRef.value.validate()
  if (!valid) return
  
  verifying.value = true
  try {
    await achievementApi.verifyAchievement(verifyingAchievement.value.id, verifyForm)
    ElMessage.success('验证提交成功')
    showVerifyDialog.value = false
    loadAchievements()
    loadStats()
  } catch (error) {
    ElMessage.error('提交验证失败')
  } finally {
    verifying.value = false
  }
}

const resetForm = () => {
  editingAchievement.value = null
  Object.assign(achievementForm, {
    title: '',
    description: '',
    achievementType: '',
    category: '',
    level: '',
    awardRank: '',
    organization: '',
    achievementDate: '',
    visibility: 'UNIVERSITY',
    certificateUrl: '',
    attachmentUrls: []
  })
  attachmentUrlsInput.value = ''
  if (achievementFormRef.value) {
    achievementFormRef.value.resetFields()
  }
}

// 权限检查
const canEdit = (achievement) => {
  return achievement.studentId === auth.user?.id || auth.hasPermission('ACHIEVEMENT_MANAGE')
}

const canDelete = (achievement) => {
  return achievement.studentId === auth.user?.id || auth.hasPermission('ACHIEVEMENT_MANAGE')
}

const canVerify = (achievement) => {
  return auth.hasPermission('ACHIEVEMENT_VERIFY') && achievement.status === 'PUBLISHED'
}

const canPublish = (achievement) => {
  return achievement.studentId === auth.user?.id && achievement.status === 'DRAFT'
}

// 辅助方法
const getTypeDisplayName = (type) => {
  const names = {
    COMPETITION: '竞赛获奖',
    RESEARCH: '科研成果',
    PROJECT: '项目作品',
    PATENT: '专利发明',
    PUBLICATION: '论文发表',
    CERTIFICATE: '资格证书',
    OTHER: '其他成果'
  }
  return names[type] || type
}

const getTypeTagType = (type) => {
  const types = {
    COMPETITION: 'warning',
    RESEARCH: 'primary',
    PROJECT: 'success',
    PATENT: 'danger',
    PUBLICATION: 'info',
    CERTIFICATE: '',
    OTHER: 'info'
  }
  return types[type] || ''
}

const getLevelDisplayName = (level) => {
  const names = {
    SCHOOL: '校级',
    CITY: '市级',
    PROVINCE: '省级',
    NATIONAL: '国家级',
    INTERNATIONAL: '国际级'
  }
  return names[level] || level
}

const getLevelTagType = (level) => {
  const types = {
    SCHOOL: 'info',
    CITY: '',
    PROVINCE: 'warning',
    NATIONAL: 'success',
    INTERNATIONAL: 'danger'
  }
  return types[level] || ''
}

const getStatusDisplayName = (status) => {
  const names = {
    DRAFT: '草稿',
    PUBLISHED: '已发布',
    VERIFIED: '已验证',
    REJECTED: '已拒绝'
  }
  return names[status] || status
}

const getStatusTagType = (status) => {
  const types = {
    DRAFT: 'info',
    PUBLISHED: 'primary',
    VERIFIED: 'success',
    REJECTED: 'danger'
  }
  return types[status] || ''
}

const getVisibilityDisplayName = (visibility) => {
  const names = {
    PRIVATE: '私有',
    UNIVERSITY: '校内可见',
    PUBLIC: '公开'
  }
  return names[visibility] || visibility
}

const getVisibilityTagType = (visibility) => {
  const types = {
    PRIVATE: 'info',
    UNIVERSITY: 'warning',
    PUBLIC: 'success'
  }
  return types[visibility] || ''
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

const getStudentAvatar = (studentId) => {
  return null // 实际应从API获取
}

const getRankClass = (index) => {
  if (index === 0) return 'rank-gold'
  if (index === 1) return 'rank-silver'
  if (index === 2) return 'rank-bronze'
  return ''
}

const getStatPercentage = (count) => {
  const maxCount = Math.max(...typeStats.value.map(s => s.verifiedCount))
  return maxCount > 0 ? (count / maxCount) * 100 : 0
}

const viewCertificate = (achievement) => {
  window.open(achievement.certificateUrl, '_blank')
}

const viewAttachments = (achievement) => {
  // 显示附件列表对话框
}

// 生命周期
onMounted(() => {
  loadAchievements()
  loadStats()
})
</script>

<style scoped>
.achievement-center {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-content h1 {
  margin: 0 0 5px 0;
  color: #303133;
}

.header-content p {
  margin: 0;
  color: #909399;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  position: relative;
  overflow: hidden;
}

.stat-card :deep(.el-card__body) {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.stat-icon {
  font-size: 40px;
  color: #409eff;
  opacity: 0.3;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-row {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

.achievement-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.achievement-card {
  transition: all 0.3s ease;
}

.achievement-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.achievement-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
}

.achievement-title {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}

.achievement-meta {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.achievement-meta .date {
  color: #909399;
  font-size: 14px;
  margin-left: auto;
}

.achievement-content {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.student-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.student-details {
  flex: 1;
}

.student-name {
  font-weight: 500;
  color: #303133;
}

.student-major {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.achievement-description {
  margin: 0;
  color: #606266;
  line-height: 1.6;
}

.achievement-details {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.detail-item {
  display: flex;
  align-items: center;
  font-size: 14px;
}

.detail-item .label {
  color: #909399;
  margin-right: 4px;
}

.detail-item .value {
  color: #303133;
  font-weight: 500;
}

.achievement-attachments {
  display: flex;
  gap: 15px;
}

.sidebar-card {
  margin-bottom: 20px;
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.student-ranking {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.rank-number {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  color: white;
  background: #909399;
}

.rank-gold {
  background: linear-gradient(45deg, #ffd700, #ffed4e);
}

.rank-silver {
  background: linear-gradient(45deg, #c0c0c0, #e5e5e5);
}

.rank-bronze {
  background: linear-gradient(45deg, #cd7f32, #daa520);
}

.ranking-item .student-info {
  flex: 1;
  gap: 4px;
}

.ranking-item .student-name {
  font-weight: 500;
  color: #303133;
  font-size: 14px;
}

.ranking-item .student-major {
  font-size: 12px;
  color: #909399;
}

.achievement-count {
  font-size: 12px;
  color: #409eff;
  font-weight: 500;
}

.type-stats {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.stat-label {
  font-size: 14px;
  color: #303133;
}

.stat-bar {
  position: relative;
  height: 20px;
  background: #f5f7fa;
  border-radius: 10px;
  overflow: hidden;
}

.stat-progress {
  height: 100%;
  background: linear-gradient(90deg, #409eff, #67c23a);
  border-radius: 10px;
  transition: width 0.3s ease;
}

.stat-count {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 12px;
  color: #303133;
  font-weight: 500;
}

.pagination-wrapper {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

.achievement-detail p {
  margin: 10px 0;
  line-height: 1.6;
  color: #606266;
}

.no-notes {
  color: #c0c4cc;
  font-style: italic;
}
</style>
