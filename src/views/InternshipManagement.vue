<template>
  <div class="internship-management">
    <div class="page-header">
      <div class="header-content">
        <h1>实习就业管理</h1>
        <p>企业实习职位发布与学生申请管理</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateDialog = true" v-if="canCreatePosition">
          <el-icon><Plus /></el-icon>
          发布职位
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalPositions }}</div>
          <div class="stat-label">总职位数</div>
        </div>
        <el-icon class="stat-icon"><Briefcase /></el-icon>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-value">{{ stats.activePositions }}</div>
          <div class="stat-label">招聘中</div>
        </div>
        <el-icon class="stat-icon"><Loading /></el-icon>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalApplications }}</div>
          <div class="stat-label">总申请数</div>
        </div>
        <el-icon class="stat-icon"><Document /></el-icon>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-value">{{ formatCurrency(stats.averageSalary) }}</div>
          <div class="stat-label">平均薪资</div>
        </div>
        <el-icon class="stat-icon"><Money /></el-icon>
      </el-card>
    </div>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" @tab-click="handleTabChange">
      <el-tab-pane label="实习职位" name="positions">
        <!-- 职位筛选 -->
        <el-card class="filter-card">
          <div class="filter-row">
            <el-input
              v-model="positionSearch.search"
              placeholder="搜索职位名称或公司"
              style="width: 300px"
              @input="handlePositionSearch"
              clearable
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            
            <el-select v-model="positionSearch.type" placeholder="职位类型" clearable @change="loadPositions">
              <el-option label="全职" value="FULL_TIME" />
              <el-option label="兼职" value="PART_TIME" />
              <el-option label="远程" value="REMOTE" />
            </el-select>

            <el-select v-model="positionSearch.status" placeholder="职位状态" clearable @change="loadPositions">
              <el-option label="草稿" value="DRAFT" />
              <el-option label="已发布" value="PUBLISHED" />
              <el-option label="已关闭" value="CLOSED" />
              <el-option label="已取消" value="CANCELLED" />
            </el-select>

            <el-input v-model="positionSearch.location" placeholder="工作地点" clearable @change="loadPositions" />
          </div>
        </el-card>

        <!-- 职位列表 -->
        <el-card class="table-card">
          <el-table :data="positions" v-loading="positionLoading" stripe>
            <el-table-column prop="title" label="职位名称" min-width="200">
              <template #default="{ row }">
                <div class="position-title">
                  <div class="title">{{ row.title }}</div>
                  <div class="company">{{ row.companyName }}</div>
                </div>
              </template>
            </el-table-column>
            
            <el-table-column prop="positionType" label="类型" width="80">
              <template #default="{ row }">
                <el-tag :type="getPositionTypeTagType(row.positionType)" size="small">
                  {{ getPositionTypeDisplayName(row.positionType) }}
                </el-tag>
              </template>
            </el-table-column>
            
            <el-table-column prop="location" label="地点" width="120" />
            <el-table-column prop="durationMonths" label="期限" width="80">
              <template #default="{ row }">
                {{ row.durationMonths ? row.durationMonths + '个月' : '不限' }}
              </template>
            </el-table-column>
            
            <el-table-column label="薪资" width="150">
              <template #default="{ row }">
                <span v-if="row.salaryMin && row.salaryMax">
                  {{ formatSalary(row.salaryMin) }} - {{ formatSalary(row.salaryMax) }}
                </span>
                <span v-else>面议</span>
              </template>
            </el-table-column>
            
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getPositionStatusTagType(row.status)">
                  {{ getPositionStatusDisplayName(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            
            <el-table-column label="申请/名额" width="100">
              <template #default="{ row }">
                {{ row.applicationCount || 0 }} / {{ row.positionsAvailable }}
              </template>
            </el-table-column>
            
            <el-table-column prop="applicationDeadline" label="截止日期" width="120">
              <template #default="{ row }">
                {{ formatDate(row.applicationDeadline) }}
              </template>
            </el-table-column>
            
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="viewPosition(row)">
                  查看
                </el-button>
                <el-button link type="primary" @click="applyPosition(row)" v-if="canApply && row.status === 'PUBLISHED'">
                  申请
                </el-button>
                <el-button link type="primary" @click="editPosition(row)" v-if="canEditPosition(row)">
                  编辑
                </el-button>
                <el-button link type="danger" @click="deletePosition(row)" v-if="canDeletePosition(row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="positionPagination.page"
              v-model:page-size="positionPagination.size"
              :total="positionPagination.total"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
              @size-change="loadPositions"
              @current-change="loadPositions"
            />
          </div>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="我的申请" name="applications">
        <!-- 申请列表 -->
        <el-card class="table-card">
          <el-table :data="applications" v-loading="applicationLoading" stripe>
            <el-table-column prop="positionTitle" label="职位名称" min-width="200">
              <template #default="{ row }">
                <div class="position-title">
                  <div class="title">{{ row.positionTitle }}</div>
                  <div class="company">{{ row.companyName }}</div>
                </div>
              </template>
            </el-table-column>
            
            <el-table-column prop="status" label="申请状态" width="120">
              <template #default="{ row }">
                <el-tag :type="getApplicationStatusTagType(row.status)">
                  {{ getApplicationStatusDisplayName(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            
            <el-table-column prop="appliedAt" label="申请时间" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.appliedAt) }}
              </template>
            </el-table-column>
            
            <el-table-column prop="interviewDate" label="面试时间" width="180">
              <template #default="{ row }">
                {{ row.interviewDate ? formatDateTime(row.interviewDate) : '-' }}
              </template>
            </el-table-column>
            
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="viewApplication(row)">
                  查看详情
                </el-button>
                <el-button link type="danger" @click="withdrawApplication(row)" 
                          v-if="row.status === 'SUBMITTED' || row.status === 'UNDER_REVIEW'">
                  撤回
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="applicationPagination.page"
              v-model:page-size="applicationPagination.size"
              :total="applicationPagination.total"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
              @size-change="loadApplications"
              @current-change="loadApplications"
            />
          </div>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="实习记录" name="records">
        <!-- 实习记录列表 -->
        <el-card class="table-card">
          <el-table :data="records" v-loading="recordLoading" stripe>
            <el-table-column prop="positionTitle" label="实习职位" min-width="180" />
            <el-table-column prop="companyName" label="实习公司" width="150" />
            <el-table-column prop="startDate" label="开始日期" width="120">
              <template #default="{ row }">
                {{ formatDate(row.startDate) }}
              </template>
            </el-table-column>
            <el-table-column prop="endDate" label="结束日期" width="120">
              <template #default="{ row }">
                {{ formatDate(row.endDate) }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getRecordStatusTagType(row.status)">
                  {{ getRecordStatusDisplayName(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="performanceScore" label="表现评分" width="100">
              <template #default="{ row }">
                <el-rate v-model="row.performanceScore" disabled show-score />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="viewRecord(row)">
                  查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="recordPagination.page"
              v-model:page-size="recordPagination.size"
              :total="recordPagination.total"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
              @size-change="loadRecords"
              @current-change="loadRecords"
            />
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 创建/编辑职位对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      :title="editingPosition ? '编辑职位' : '发布新职位'"
      width="800px"
      @closed="resetPositionForm"
    >
      <el-form :model="positionForm" :rules="positionRules" ref="positionFormRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="职位名称" prop="title">
              <el-input v-model="positionForm.title" placeholder="请输入职位名称" />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="职位类型" prop="positionType">
              <el-select v-model="positionForm.positionType" placeholder="选择职位类型">
                <el-option label="全职" value="FULL_TIME" />
                <el-option label="兼职" value="PART_TIME" />
                <el-option label="远程" value="REMOTE" />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="实习期限" prop="durationMonths">
              <el-input-number v-model="positionForm.durationMonths" :min="1" :max="12" placeholder="月" />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="工作地点" prop="location">
              <el-input v-model="positionForm.location" placeholder="请输入工作地点" />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="所属部门" prop="department">
              <el-input v-model="positionForm.department" placeholder="请输入所属部门" />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="薪资下限" prop="salaryMin">
              <el-input-number v-model="positionForm.salaryMin" :min="0" placeholder="元/月" />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="薪资上限" prop="salaryMax">
              <el-input-number v-model="positionForm.salaryMax" :min="0" placeholder="元/月" />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="招聘人数" prop="positionsAvailable">
              <el-input-number v-model="positionForm.positionsAvailable" :min="1" />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="年级要求" prop="gradeRequirement">
              <el-select v-model="positionForm.gradeRequirement" placeholder="选择年级要求">
                <el-option label="大一" value="FRESHMAN" />
                <el-option label="大二" value="SOPHOMORE" />
                <el-option label="大三" value="JUNIOR" />
                <el-option label="大四" value="SENIOR" />
                <el-option label="研究生" value="GRADUATE" />
                <el-option label="不限" value="ANY" />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="申请截止" prop="applicationDeadline">
              <el-date-picker v-model="positionForm.applicationDeadline" type="date" placeholder="选择截止日期" />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="开始日期" prop="startDate">
              <el-date-picker v-model="positionForm.startDate" type="date" placeholder="选择开始日期" />
            </el-form-item>
          </el-col>
          
          <el-col :span="24">
            <el-form-item label="职位描述" prop="description">
              <el-input v-model="positionForm.description" type="textarea" :rows="4" placeholder="请输入职位详细描述" />
            </el-form-item>
          </el-col>
          
          <el-col :span="24">
            <el-form-item label="岗位要求" prop="requirements">
              <el-input v-model="positionForm.requirements" type="textarea" :rows="3" placeholder="请输入岗位要求" />
            </el-form-item>
          </el-col>
          
          <el-col :span="24">
            <el-form-item label="工作职责" prop="responsibilities">
              <el-input v-model="positionForm.responsibilities" type="textarea" :rows="3" placeholder="请输入工作职责" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="savePosition" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 申请职位对话框 -->
    <el-dialog v-model="showApplyDialog" title="申请实习职位" width="600px">
      <el-form :model="applicationForm" :rules="applicationRules" ref="applicationFormRef" label-width="100px">
        <el-form-item label="求职信" prop="coverLetter">
          <el-input v-model="applicationForm.coverLetter" type="textarea" :rows="4" 
                   placeholder="请简要介绍自己的背景和申请理由" />
        </el-form-item>
        <el-form-item label="简历链接" prop="resumeUrl">
          <el-input v-model="applicationForm.resumeUrl" placeholder="请输入简历下载链接" />
        </el-form-item>
        <el-form-item label="作品集" prop="portfolioUrl">
          <el-input v-model="applicationForm.portfolioUrl" placeholder="请输入作品集链接（可选）" />
        </el-form-item>
        <el-form-item label="GPA" prop="gpa">
          <el-input-number v-model="applicationForm.gpa" :min="0" :max="4" :precision="2" placeholder="请输入GPA" />
        </el-form-item>
        <el-form-item label="相关经验" prop="experience">
          <el-input v-model="applicationForm.experience" type="textarea" :rows="3" 
                   placeholder="请描述相关实习或项目经验" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showApplyDialog = false">取消</el-button>
        <el-button type="primary" @click="submitApplication" :loading="applying">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Briefcase, Loading, Document, Money } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { internshipApi } from '@/api/internships'

const auth = useAuthStore()

// 响应式数据
const activeTab = ref('positions')
const positionLoading = ref(false)
const applicationLoading = ref(false)
const recordLoading = ref(false)
const saving = ref(false)
const applying = ref(false)

const positions = ref([])
const applications = ref([])
const records = ref([])
const stats = ref({
  totalPositions: 0,
  activePositions: 0,
  totalApplications: 0,
  averageSalary: 0
})

// 分页
const positionPagination = reactive({ page: 1, size: 10, total: 0 })
const applicationPagination = reactive({ page: 1, size: 10, total: 0 })
const recordPagination = reactive({ page: 1, size: 10, total: 0 })

// 搜索表单
const positionSearch = reactive({
  search: '',
  type: '',
  status: '',
  location: ''
})

// 对话框状态
const showCreateDialog = ref(false)
const showApplyDialog = ref(false)
const editingPosition = ref(null)
const applyingPosition = ref(null)

// 表单数据
const positionForm = reactive({
  title: '',
  description: '',
  positionType: '',
  durationMonths: 6,
  location: '',
  department: '',
  salaryMin: null,
  salaryMax: null,
  positionsAvailable: 1,
  gradeRequirement: 'ANY',
  applicationDeadline: '',
  startDate: '',
  requirements: '',
  responsibilities: ''
})

const applicationForm = reactive({
  coverLetter: '',
  resumeUrl: '',
  portfolioUrl: '',
  gpa: null,
  experience: ''
})

// 表单验证规则
const positionRules = {
  title: [{ required: true, message: '请输入职位名称', trigger: 'blur' }],
  positionType: [{ required: true, message: '请选择职位类型', trigger: 'change' }],
  location: [{ required: true, message: '请输入工作地点', trigger: 'blur' }],
  applicationDeadline: [{ required: true, message: '请选择申请截止日期', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }]
}

const applicationRules = {
  coverLetter: [{ required: true, message: '请填写求职信', trigger: 'blur' }],
  resumeUrl: [{ required: true, message: '请提供简历链接', trigger: 'blur' }]
}

const positionFormRef = ref()
const applicationFormRef = ref()

// 计算属性
const canCreatePosition = computed(() => {
  return auth.hasPermission('INTERNSHIP_CREATE') && auth.user?.role === 'ENTERPRISE_ADMIN'
})

const canApply = computed(() => {
  return auth.user?.role === 'STUDENT'
})

// 方法
const handleTabChange = (tab) => {
  switch (tab.name) {
    case 'positions':
      loadPositions()
      break
    case 'applications':
      loadApplications()
      break
    case 'records':
      loadRecords()
      break
  }
}

const loadPositions = async () => {
  positionLoading.value = true
  try {
    const params = {
      page: positionPagination.page - 1,
      size: positionPagination.size,
      ...positionSearch
    }
    const response = await internshipApi.getPositions(params)
    positions.value = response.data.items
    positionPagination.total = response.data.total
  } catch (error) {
    ElMessage.error('加载职位列表失败')
  } finally {
    positionLoading.value = false
  }
}

const loadApplications = async () => {
  if (!canApply.value) return
  
  applicationLoading.value = true
  try {
    const params = {
      page: applicationPagination.page - 1,
      size: applicationPagination.size,
      studentId: auth.user?.id
    }
    const response = await internshipApi.getApplications(params)
    applications.value = response.data.items
    applicationPagination.total = response.data.total
  } catch (error) {
    ElMessage.error('加载申请列表失败')
  } finally {
    applicationLoading.value = false
  }
}

const loadRecords = async () => {
  recordLoading.value = true
  try {
    const params = {
      page: recordPagination.page - 1,
      size: recordPagination.size,
      studentId: auth.user?.role === 'STUDENT' ? auth.user.id : undefined
    }
    const response = await internshipApi.getRecords(params)
    records.value = response.data.items
    recordPagination.total = response.data.total
  } catch (error) {
    ElMessage.error('加载实习记录失败')
  } finally {
    recordLoading.value = false
  }
}

const loadStats = async () => {
  try {
    const response = await internshipApi.getStats()
    stats.value = response.data
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const handlePositionSearch = () => {
  positionPagination.page = 1
  loadPositions()
}

const viewPosition = (position) => {
  // 显示职位详情
}

const editPosition = (position) => {
  editingPosition.value = position
  Object.assign(positionForm, position)
  showCreateDialog.value = true
}

const deletePosition = async (position) => {
  try {
    await ElMessageBox.confirm(`确定要删除职位"${position.title}"吗？`, '确认删除', {
      type: 'warning'
    })
    
    await internshipApi.deletePosition(position.id)
    ElMessage.success('职位删除成功')
    loadPositions()
    loadStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除职位失败')
    }
  }
}

const applyPosition = (position) => {
  applyingPosition.value = position
  showApplyDialog.value = true
}

const savePosition = async () => {
  if (!positionFormRef.value) return
  
  const valid = await positionFormRef.value.validate()
  if (!valid) return
  
  saving.value = true
  try {
    if (editingPosition.value) {
      await internshipApi.updatePosition(editingPosition.value.id, positionForm)
      ElMessage.success('职位更新成功')
    } else {
      await internshipApi.createPosition(positionForm)
      ElMessage.success('职位发布成功')
    }
    
    showCreateDialog.value = false
    loadPositions()
    loadStats()
  } catch (error) {
    ElMessage.error('保存职位失败')
  } finally {
    saving.value = false
  }
}

const submitApplication = async () => {
  if (!applicationFormRef.value || !applyingPosition.value) return
  
  const valid = await applicationFormRef.value.validate()
  if (!valid) return
  
  applying.value = true
  try {
    await internshipApi.applyPosition(applyingPosition.value.id, applicationForm)
    ElMessage.success('申请提交成功')
    showApplyDialog.value = false
    loadApplications()
  } catch (error) {
    ElMessage.error('提交申请失败')
  } finally {
    applying.value = false
  }
}

const resetPositionForm = () => {
  editingPosition.value = null
  Object.assign(positionForm, {
    title: '',
    description: '',
    positionType: '',
    durationMonths: 6,
    location: '',
    department: '',
    salaryMin: null,
    salaryMax: null,
    positionsAvailable: 1,
    gradeRequirement: 'ANY',
    applicationDeadline: '',
    startDate: '',
    requirements: '',
    responsibilities: ''
  })
  if (positionFormRef.value) {
    positionFormRef.value.resetFields()
  }
}

// 权限检查
const canEditPosition = (position) => {
  return auth.hasPermission('INTERNSHIP_UPDATE') && 
         (position.createdBy === auth.user?.id || auth.hasPermission('INTERNSHIP_MANAGE'))
}

const canDeletePosition = (position) => {
  return auth.hasPermission('INTERNSHIP_DELETE') && 
         (position.createdBy === auth.user?.id || auth.hasPermission('INTERNSHIP_MANAGE'))
}

// 辅助方法
const getPositionTypeDisplayName = (type) => {
  const names = { FULL_TIME: '全职', PART_TIME: '兼职', REMOTE: '远程' }
  return names[type] || type
}

const getPositionTypeTagType = (type) => {
  const types = { FULL_TIME: 'primary', PART_TIME: 'success', REMOTE: 'warning' }
  return types[type] || ''
}

const getPositionStatusDisplayName = (status) => {
  const names = { DRAFT: '草稿', PUBLISHED: '已发布', CLOSED: '已关闭', CANCELLED: '已取消' }
  return names[status] || status
}

const getPositionStatusTagType = (status) => {
  const types = { DRAFT: 'info', PUBLISHED: 'success', CLOSED: 'warning', CANCELLED: 'danger' }
  return types[status] || ''
}

const getApplicationStatusDisplayName = (status) => {
  const names = {
    SUBMITTED: '已提交',
    UNDER_REVIEW: '审核中',
    INTERVIEW_SCHEDULED: '面试安排',
    INTERVIEWED: '已面试',
    ACCEPTED: '已录取',
    REJECTED: '已拒绝',
    WITHDRAWN: '已撤回'
  }
  return names[status] || status
}

const getApplicationStatusTagType = (status) => {
  const types = {
    SUBMITTED: 'primary',
    UNDER_REVIEW: 'warning',
    INTERVIEW_SCHEDULED: 'warning',
    INTERVIEWED: 'info',
    ACCEPTED: 'success',
    REJECTED: 'danger',
    WITHDRAWN: 'info'
  }
  return types[status] || ''
}

const getRecordStatusDisplayName = (status) => {
  const names = { ONGOING: '进行中', COMPLETED: '已完成', TERMINATED: '已终止', EXTENDED: '已延期' }
  return names[status] || status
}

const getRecordStatusTagType = (status) => {
  const types = { ONGOING: 'primary', COMPLETED: 'success', TERMINATED: 'danger', EXTENDED: 'warning' }
  return types[status] || ''
}

const formatCurrency = (amount) => {
  if (!amount) return '未设定'
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY'
  }).format(amount)
}

const formatSalary = (amount) => {
  return new Intl.NumberFormat('zh-CN').format(amount)
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

const formatDateTime = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

// 生命周期
onMounted(() => {
  loadPositions()
  loadStats()
})
</script>

<style scoped>
.internship-management {
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

.position-title .title {
  font-weight: 500;
  color: #303133;
}

.position-title .company {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.pagination-wrapper {
  padding: 20px;
  display: flex;
  justify-content: center;
}
</style>
