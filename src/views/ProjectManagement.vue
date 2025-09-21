<template>
  <div class="project-management">
    <div class="page-header">
      <div class="header-content">
        <h1>项目协作管理</h1>
        <p>大学企业协作项目的全生命周期管理</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateDialog = true" v-if="canCreate">
          <el-icon><Plus /></el-icon>
          发布项目
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalProjects }}</div>
          <div class="stat-label">总项目数</div>
        </div>
        <el-icon class="stat-icon"><Document /></el-icon>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-value">{{ stats.activeProjects }}</div>
          <div class="stat-label">进行中</div>
        </div>
        <el-icon class="stat-icon"><Loading /></el-icon>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-value">{{ stats.completedProjects }}</div>
          <div class="stat-label">已完成</div>
        </div>
        <el-icon class="stat-icon"><Check /></el-icon>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-value">{{ formatCurrency(stats.totalBudget) }}</div>
          <div class="stat-label">总预算</div>
        </div>
        <el-icon class="stat-icon"><Money /></el-icon>
      </el-card>
    </div>

    <!-- 筛选和搜索 -->
    <el-card class="filter-card">
      <div class="filter-row">
        <el-input
          v-model="searchForm.search"
          placeholder="搜索项目名称或描述"
          style="width: 300px"
          @input="handleSearch"
          clearable
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <el-select v-model="searchForm.type" placeholder="项目类型" clearable @change="loadProjects">
          <el-option label="科研合作" value="RESEARCH" />
          <el-option label="技术开发" value="DEVELOPMENT" />
          <el-option label="咨询服务" value="CONSULTING" />
          <el-option label="人才培训" value="TRAINING" />
          <el-option label="实习项目" value="INTERNSHIP" />
        </el-select>

        <el-select v-model="searchForm.status" placeholder="项目状态" clearable @change="loadProjects">
          <el-option label="草稿" value="DRAFT" />
          <el-option label="已发布" value="PUBLISHED" />
          <el-option label="匹配中" value="MATCHING" />
          <el-option label="协商中" value="NEGOTIATING" />
          <el-option label="已批准" value="APPROVED" />
          <el-option label="进行中" value="IN_PROGRESS" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>

        <el-select v-model="searchForm.initiatorType" placeholder="发起方" clearable @change="loadProjects">
          <el-option label="高校" value="UNIVERSITY" />
          <el-option label="企业" value="COMPANY" />
        </el-select>
      </div>
    </el-card>

    <!-- 项目列表 -->
    <el-card class="table-card">
      <el-table :data="projects" v-loading="loading" stripe>
        <el-table-column prop="title" label="项目名称" min-width="200">
          <template #default="{ row }">
            <div class="project-title">
              <div class="title">{{ row.title }}</div>
              <div class="subtitle">{{ row.description }}</div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="projectType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.projectType)">
              {{ getTypeDisplayName(row.projectType) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="initiatorName" label="发起方" width="150" />
        <el-table-column prop="partnerName" label="合作方" width="150">
          <template #default="{ row }">
            <span v-if="row.partnerName">{{ row.partnerName }}</span>
            <el-text v-else type="info">待匹配</el-text>
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusDisplayName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="{ row }">
            <el-tag :type="getPriorityTagType(row.priority)" size="small">
              {{ getPriorityDisplayName(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="budget" label="预算" width="120">
          <template #default="{ row }">
            <span v-if="row.budget">{{ formatCurrency(row.budget) }}</span>
            <el-text v-else type="info">未设定</el-text>
          </template>
        </el-table-column>
        
        <el-table-column prop="progressPercentage" label="进度" width="100">
          <template #default="{ row }">
            <el-progress :percentage="row.progressPercentage || 0" :stroke-width="6" />
          </template>
        </el-table-column>
        
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewProject(row)">
              查看
            </el-button>
            <el-button link type="primary" @click="editProject(row)" v-if="canEdit(row)">
              编辑
            </el-button>
            <el-dropdown @command="handleCommand($event, row)">
              <el-button link type="primary">
                更多<el-icon><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="participants">参与人员</el-dropdown-item>
                  <el-dropdown-item command="milestones">里程碑</el-dropdown-item>
                  <el-dropdown-item command="funds">资金管理</el-dropdown-item>
                  <el-dropdown-item command="delete" v-if="canDelete(row)" divided>
                    删除项目
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadProjects"
          @current-change="loadProjects"
        />
      </div>
    </el-card>

    <!-- 创建/编辑项目对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      :title="editingProject ? '编辑项目' : '发布新项目'"
      width="800px"
      @closed="resetForm"
    >
      <el-form :model="projectForm" :rules="projectRules" ref="projectFormRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="项目名称" prop="title">
              <el-input v-model="projectForm.title" placeholder="请输入项目名称" />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="项目类型" prop="projectType">
              <el-select v-model="projectForm.projectType" placeholder="选择项目类型">
                <el-option label="科研合作" value="RESEARCH" />
                <el-option label="技术开发" value="DEVELOPMENT" />
                <el-option label="咨询服务" value="CONSULTING" />
                <el-option label="人才培训" value="TRAINING" />
                <el-option label="实习项目" value="INTERNSHIP" />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority">
              <el-select v-model="projectForm.priority" placeholder="选择优先级">
                <el-option label="低" value="LOW" />
                <el-option label="中" value="MEDIUM" />
                <el-option label="高" value="HIGH" />
                <el-option label="紧急" value="URGENT" />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="预算" prop="budget">
              <el-input v-model="projectForm.budget" placeholder="请输入预算金额" type="number" />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="币种" prop="currency">
              <el-select v-model="projectForm.currency" placeholder="选择币种">
                <el-option label="人民币" value="CNY" />
                <el-option label="美元" value="USD" />
                <el-option label="欧元" value="EUR" />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="开始日期" prop="startDate">
              <el-date-picker v-model="projectForm.startDate" type="date" placeholder="选择开始日期" />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="结束日期" prop="endDate">
              <el-date-picker v-model="projectForm.endDate" type="date" placeholder="选择结束日期" />
            </el-form-item>
          </el-col>
          
          <el-col :span="24">
            <el-form-item label="项目描述" prop="description">
              <el-input v-model="projectForm.description" type="textarea" :rows="4" placeholder="请输入项目详细描述" />
            </el-form-item>
          </el-col>
          
          <el-col :span="24">
            <el-form-item label="项目需求" prop="requirements">
              <el-input v-model="projectForm.requirements" type="textarea" :rows="3" placeholder="请输入项目需求" />
            </el-form-item>
          </el-col>
          
          <el-col :span="24">
            <el-form-item label="交付物" prop="deliverables">
              <el-input v-model="projectForm.deliverables" type="textarea" :rows="3" placeholder="请输入项目交付物" />
            </el-form-item>
          </el-col>
          
          <el-col :span="24">
            <el-form-item label="项目标签" prop="tags">
              <el-input v-model="tagsInput" placeholder="请输入标签，用逗号分隔" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="saveProject" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 项目详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="项目详情" width="900px">
      <div v-if="currentProject" class="project-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="项目名称">{{ currentProject.title }}</el-descriptions-item>
          <el-descriptions-item label="项目类型">{{ getTypeDisplayName(currentProject.projectType) }}</el-descriptions-item>
          <el-descriptions-item label="发起方">{{ currentProject.initiatorName }}</el-descriptions-item>
          <el-descriptions-item label="合作方">{{ currentProject.partnerName || '待匹配' }}</el-descriptions-item>
          <el-descriptions-item label="项目状态">
            <el-tag :type="getStatusTagType(currentProject.status)">
              {{ getStatusDisplayName(currentProject.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="优先级">
            <el-tag :type="getPriorityTagType(currentProject.priority)">
              {{ getPriorityDisplayName(currentProject.priority) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="预算">{{ formatCurrency(currentProject.budget) }}</el-descriptions-item>
          <el-descriptions-item label="进度">
            <el-progress :percentage="currentProject.progressPercentage || 0" />
          </el-descriptions-item>
          <el-descriptions-item label="开始日期">{{ formatDate(currentProject.startDate) }}</el-descriptions-item>
          <el-descriptions-item label="结束日期">{{ formatDate(currentProject.endDate) }}</el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">{{ formatDate(currentProject.createdAt) }}</el-descriptions-item>
        </el-descriptions>
        
        <el-divider content-position="left">项目描述</el-divider>
        <p>{{ currentProject.description }}</p>
        
        <el-divider content-position="left">项目需求</el-divider>
        <p>{{ currentProject.requirements }}</p>
        
        <el-divider content-position="left">交付物</el-divider>
        <p>{{ currentProject.deliverables }}</p>
        
        <el-divider content-position="left">项目标签</el-divider>
        <el-tag v-for="tag in currentProject.tags" :key="tag" style="margin-right: 8px;">{{ tag }}</el-tag>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Document, Loading, Check, Money, ArrowDown } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { projectApi } from '@/api/projects'

const auth = useAuthStore()

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const projects = ref([])
const stats = ref({
  totalProjects: 0,
  activeProjects: 0,
  completedProjects: 0,
  totalBudget: 0
})

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
  status: '',
  initiatorType: ''
})

// 对话框状态
const showCreateDialog = ref(false)
const showDetailDialog = ref(false)
const editingProject = ref(null)
const currentProject = ref(null)

// 项目表单
const projectForm = reactive({
  title: '',
  description: '',
  projectType: '',
  priority: 'MEDIUM',
  budget: '',
  currency: 'CNY',
  startDate: '',
  endDate: '',
  requirements: '',
  deliverables: '',
  tags: []
})

const tagsInput = ref('')

// 表单验证规则
const projectRules = {
  title: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  projectType: [{ required: true, message: '请选择项目类型', trigger: 'change' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }]
}

const projectFormRef = ref()

// 计算属性
const canCreate = computed(() => {
  return auth.hasPermission('PROJECT_CREATE')
})

// 方法
const loadProjects = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      ...searchForm
    }
    const response = await projectApi.getProjects(params)
    projects.value = response.data.items
    pagination.total = response.data.total
  } catch (error) {
    ElMessage.error('加载项目列表失败')
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  try {
    const response = await projectApi.getStats()
    stats.value = response.data
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadProjects()
}

const viewProject = (project) => {
  currentProject.value = project
  showDetailDialog.value = true
}

const editProject = (project) => {
  editingProject.value = project
  Object.assign(projectForm, {
    title: project.title,
    description: project.description,
    projectType: project.projectType,
    priority: project.priority,
    budget: project.budget,
    currency: project.currency,
    startDate: project.startDate,
    endDate: project.endDate,
    requirements: project.requirements,
    deliverables: project.deliverables
  })
  tagsInput.value = project.tags?.join(', ') || ''
  showCreateDialog.value = true
}

const saveProject = async () => {
  if (!projectFormRef.value) return
  
  const valid = await projectFormRef.value.validate()
  if (!valid) return
  
  saving.value = true
  try {
    // 处理标签
    projectForm.tags = tagsInput.value.split(',').map(tag => tag.trim()).filter(tag => tag)
    
    if (editingProject.value) {
      await projectApi.updateProject(editingProject.value.id, projectForm)
      ElMessage.success('项目更新成功')
    } else {
      await projectApi.createProject(projectForm)
      ElMessage.success('项目创建成功')
    }
    
    showCreateDialog.value = false
    loadProjects()
    loadStats()
  } catch (error) {
    ElMessage.error('保存项目失败')
  } finally {
    saving.value = false
  }
}

const handleCommand = (command, row) => {
  switch (command) {
    case 'participants':
      // 跳转到参与人员管理
      break
    case 'milestones':
      // 跳转到里程碑管理
      break
    case 'funds':
      // 跳转到资金管理
      break
    case 'delete':
      deleteProject(row)
      break
  }
}

const deleteProject = async (project) => {
  try {
    await ElMessageBox.confirm(`确定要删除项目"${project.title}"吗？`, '确认删除', {
      type: 'warning'
    })
    
    await projectApi.deleteProject(project.id)
    ElMessage.success('项目删除成功')
    loadProjects()
    loadStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除项目失败')
    }
  }
}

const resetForm = () => {
  editingProject.value = null
  Object.assign(projectForm, {
    title: '',
    description: '',
    projectType: '',
    priority: 'MEDIUM',
    budget: '',
    currency: 'CNY',
    startDate: '',
    endDate: '',
    requirements: '',
    deliverables: '',
    tags: []
  })
  tagsInput.value = ''
  if (projectFormRef.value) {
    projectFormRef.value.resetFields()
  }
}

// 辅助方法
const canEdit = (project) => {
  return auth.hasPermission('PROJECT_UPDATE') && 
         (project.createdBy === auth.user?.id || auth.hasPermission('PROJECT_MANAGE'))
}

const canDelete = (project) => {
  return auth.hasPermission('PROJECT_DELETE') && 
         (project.createdBy === auth.user?.id || auth.hasPermission('PROJECT_MANAGE'))
}

const getTypeDisplayName = (type) => {
  const names = {
    RESEARCH: '科研合作',
    DEVELOPMENT: '技术开发',
    CONSULTING: '咨询服务',
    TRAINING: '人才培训',
    INTERNSHIP: '实习项目'
  }
  return names[type] || type
}

const getTypeTagType = (type) => {
  const types = {
    RESEARCH: 'primary',
    DEVELOPMENT: 'success',
    CONSULTING: 'info',
    TRAINING: 'warning',
    INTERNSHIP: 'danger'
  }
  return types[type] || ''
}

const getStatusDisplayName = (status) => {
  const names = {
    DRAFT: '草稿',
    PUBLISHED: '已发布',
    MATCHING: '匹配中',
    NEGOTIATING: '协商中',
    APPROVED: '已批准',
    IN_PROGRESS: '进行中',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return names[status] || status
}

const getStatusTagType = (status) => {
  const types = {
    DRAFT: 'info',
    PUBLISHED: 'primary',
    MATCHING: 'warning',
    NEGOTIATING: 'warning',
    APPROVED: 'success',
    IN_PROGRESS: 'primary',
    COMPLETED: 'success',
    CANCELLED: 'danger'
  }
  return types[status] || ''
}

const getPriorityDisplayName = (priority) => {
  const names = {
    LOW: '低',
    MEDIUM: '中',
    HIGH: '高',
    URGENT: '紧急'
  }
  return names[priority] || priority
}

const getPriorityTagType = (priority) => {
  const types = {
    LOW: 'info',
    MEDIUM: 'primary',
    HIGH: 'warning',
    URGENT: 'danger'
  }
  return types[priority] || ''
}

const formatCurrency = (amount) => {
  if (!amount) return '未设定'
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY'
  }).format(amount)
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

// 生命周期
onMounted(() => {
  loadProjects()
  loadStats()
})
</script>

<style scoped>
.project-management {
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

.table-card :deep(.el-card__body) {
  padding: 0;
}

.project-title .title {
  font-weight: 500;
  color: #303133;
}

.project-title .subtitle {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 200px;
}

.pagination-wrapper {
  padding: 20px;
  display: flex;
  justify-content: center;
}

.project-detail {
  max-height: 600px;
  overflow-y: auto;
}

.project-detail p {
  margin: 10px 0;
  line-height: 1.6;
  color: #606266;
}

.project-detail .el-tag {
  margin-right: 8px;
  margin-bottom: 8px;
}
</style>
