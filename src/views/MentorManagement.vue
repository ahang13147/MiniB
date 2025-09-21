<template>
  <div class="mentor-management">
    <div class="page-header">
      <div class="header-content">
        <h1>双导师课堂管理</h1>
        <p>校企合作双导师教学课程管理平台</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateCourseDialog = true" v-if="canCreateCourse">
          <el-icon><Plus /></el-icon>
          创建课程
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalCourses }}</div>
          <div class="stat-label">总课程数</div>
        </div>
        <el-icon class="stat-icon"><Reading /></el-icon>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-value">{{ stats.activeCourses }}</div>
          <div class="stat-label">进行中</div>
        </div>
        <el-icon class="stat-icon"><Loading /></el-icon>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalMentors }}</div>
          <div class="stat-label">企业导师</div>
        </div>
        <el-icon class="stat-icon"><User /></el-icon>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalEnrollments }}</div>
          <div class="stat-label">选课人数</div>
        </div>
        <el-icon class="stat-icon"><UserFilled /></el-icon>
      </el-card>
    </div>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" @tab-click="handleTabChange">
      <el-tab-pane label="双导师课堂" name="courses">
        <!-- 课程筛选 -->
        <el-card class="filter-card">
          <div class="filter-row">
            <el-input
              v-model="courseSearch.search"
              placeholder="搜索课程名称或描述"
              style="width: 300px"
              @input="handleCourseSearch"
              clearable
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            
            <el-select v-model="courseSearch.status" placeholder="课程状态" clearable @change="loadCourses">
              <el-option label="筹划中" value="PLANNING" />
              <el-option label="招生中" value="RECRUITING" />
              <el-option label="进行中" value="IN_PROGRESS" />
              <el-option label="已完成" value="COMPLETED" />
              <el-option label="已取消" value="CANCELLED" />
            </el-select>

            <el-select v-model="courseSearch.mode" placeholder="教学模式" clearable @change="loadCourses">
              <el-option label="线上" value="ONLINE" />
              <el-option label="线下" value="OFFLINE" />
              <el-option label="混合" value="HYBRID" />
            </el-select>
          </div>
        </el-card>

        <!-- 课程列表 -->
        <el-card class="table-card">
          <el-table :data="courses" v-loading="courseLoading" stripe>
            <el-table-column prop="courseName" label="课程名称" min-width="200">
              <template #default="{ row }">
                <div class="course-title">
                  <div class="title">{{ row.courseName }}</div>
                  <div class="code">{{ row.courseCode }}</div>
                </div>
              </template>
            </el-table-column>
            
            <el-table-column prop="universityName" label="合作高校" width="150" />
            <el-table-column prop="companyName" label="合作企业" width="150" />
            
            <el-table-column label="导师" width="200">
              <template #default="{ row }">
                <div class="mentors">
                  <div class="mentor">
                    <el-tag size="small">高校</el-tag>
                    {{ row.universityTeacherName }}
                  </div>
                  <div class="mentor">
                    <el-tag type="success" size="small">企业</el-tag>
                    {{ row.enterpriseMentorName }}
                  </div>
                </div>
              </template>
            </el-table-column>
            
            <el-table-column prop="courseType" label="类型" width="100">
              <template #default="{ row }">
                <el-tag :type="getCourseTypeTagType(row.courseType)" size="small">
                  {{ getCourseTypeDisplayName(row.courseType) }}
                </el-tag>
              </template>
            </el-table-column>
            
            <el-table-column prop="teachingMode" label="模式" width="80">
              <template #default="{ row }">
                <el-tag :type="getTeachingModeTagType(row.teachingMode)" size="small">
                  {{ getTeachingModeDisplayName(row.teachingMode) }}
                </el-tag>
              </template>
            </el-table-column>
            
            <el-table-column label="学分/学时" width="100">
              <template #default="{ row }">
                {{ row.credits }}学分 / {{ row.totalHours }}学时
              </template>
            </el-table-column>
            
            <el-table-column label="选课人数" width="100">
              <template #default="{ row }">
                {{ row.enrolledStudents }} / {{ row.maxStudents }}
              </template>
            </el-table-column>
            
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getCourseStatusTagType(row.status)">
                  {{ getCourseStatusDisplayName(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="viewCourse(row)">
                  查看
                </el-button>
                <el-button link type="primary" @click="editCourse(row)" v-if="canEditCourse(row)">
                  编辑
                </el-button>
                <el-dropdown @command="handleCourseCommand($event, row)">
                  <el-button link type="primary">
                    更多<el-icon><ArrowDown /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="enrollments">选课管理</el-dropdown-item>
                      <el-dropdown-item command="delete" v-if="canDeleteCourse(row)" divided>
                        删除课程
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="coursePagination.page"
              v-model:page-size="coursePagination.size"
              :total="coursePagination.total"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
              @size-change="loadCourses"
              @current-change="loadCourses"
            />
          </div>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="企业导师" name="mentors">
        <!-- 导师筛选 -->
        <el-card class="filter-card">
          <div class="filter-row">
            <el-input
              v-model="mentorSearch.search"
              placeholder="搜索导师姓名或专业领域"
              style="width: 300px"
              @input="handleMentorSearch"
              clearable
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            
            <el-select v-model="mentorSearch.status" placeholder="导师状态" clearable @change="loadMentors">
              <el-option label="活跃" value="ACTIVE" />
              <el-option label="非活跃" value="INACTIVE" />
              <el-option label="已暂停" value="SUSPENDED" />
            </el-select>
          </div>
        </el-card>

        <!-- 导师列表 -->
        <el-card class="table-card">
          <el-table :data="mentors" v-loading="mentorLoading" stripe>
            <el-table-column prop="name" label="导师姓名" width="120" />
            <el-table-column prop="companyName" label="所属企业" width="150" />
            <el-table-column prop="position" label="职位" width="150" />
            <el-table-column prop="department" label="部门" width="120" />
            <el-table-column prop="workYears" label="工作年限" width="100">
              <template #default="{ row }">
                {{ row.workYears }}年
              </template>
            </el-table-column>
            <el-table-column prop="expertise" label="专业领域" min-width="200" show-overflow-tooltip />
            <el-table-column label="教学情况" width="120">
              <template #default="{ row }">
                {{ row.courseCount }}门课程<br>
                {{ row.studentCount }}名学生
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getMentorStatusTagType(row.status)">
                  {{ getMentorStatusDisplayName(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="viewMentor(row)">
                  查看详情
                </el-button>
                <el-button link type="primary" @click="editMentor(row)" v-if="canEditMentor(row)">
                  编辑
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="mentorPagination.page"
              v-model:page-size="mentorPagination.size"
              :total="mentorPagination.total"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
              @size-change="loadMentors"
              @current-change="loadMentors"
            />
          </div>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="选课管理" name="enrollments">
        <!-- 选课列表 -->
        <el-card class="table-card">
          <el-table :data="enrollments" v-loading="enrollmentLoading" stripe>
            <el-table-column prop="courseName" label="课程名称" min-width="180" />
            <el-table-column prop="studentName" label="学生姓名" width="120" />
            <el-table-column prop="studentMajor" label="专业" width="150" />
            <el-table-column prop="enrollmentDate" label="选课时间" width="120">
              <template #default="{ row }">
                {{ formatDate(row.enrollmentDate) }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getEnrollmentStatusTagType(row.status)">
                  {{ getEnrollmentStatusDisplayName(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="成绩" width="150">
              <template #default="{ row }">
                <span v-if="row.finalScore">{{ row.finalScore }}分 ({{ row.finalGrade }})</span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="attendanceRate" label="出勤率" width="100">
              <template #default="{ row }">
                {{ row.attendanceRate ? row.attendanceRate + '%' : '-' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="gradeStudent(row)" v-if="canGrade">
                  成绩录入
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="enrollmentPagination.page"
              v-model:page-size="enrollmentPagination.size"
              :total="enrollmentPagination.total"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
              @size-change="loadEnrollments"
              @current-change="loadEnrollments"
            />
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 创建/编辑课程对话框 -->
    <el-dialog
      v-model="showCreateCourseDialog"
      :title="editingCourse ? '编辑课程' : '创建双导师课程'"
      width="800px"
      @closed="resetCourseForm"
    >
      <el-form :model="courseForm" :rules="courseRules" ref="courseFormRef" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="课程代码" prop="courseCode">
              <el-input v-model="courseForm.courseCode" placeholder="请输入课程代码" />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="课程名称" prop="courseName">
              <el-input v-model="courseForm.courseName" placeholder="请输入课程名称" />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="合作高校" prop="universityId">
              <el-select v-model="courseForm.universityId" placeholder="选择合作高校">
                <el-option label="清华大学" value="U-001" />
                <el-option label="北京大学" value="U-002" />
                <el-option label="复旦大学" value="U-003" />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="合作企业" prop="companyId">
              <el-select v-model="courseForm.companyId" placeholder="选择合作企业">
                <el-option label="腾讯科技" value="C-001" />
                <el-option label="阿里巴巴" value="C-002" />
                <el-option label="百度" value="C-003" />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="高校导师" prop="universityTeacherId">
              <el-select v-model="courseForm.universityTeacherId" placeholder="选择高校导师">
                <el-option label="张教授" value="T-001" />
                <el-option label="李教授" value="T-002" />
                <el-option label="王教授" value="T-003" />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="企业导师" prop="enterpriseMentorId">
              <el-select v-model="courseForm.enterpriseMentorId" placeholder="选择企业导师">
                <el-option label="李导师" value="M-001" />
                <el-option label="王导师" value="M-002" />
                <el-option label="张导师" value="M-003" />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="8">
            <el-form-item label="学分" prop="credits">
              <el-input-number v-model="courseForm.credits" :min="1" :max="6" />
            </el-form-item>
          </el-col>
          
          <el-col :span="8">
            <el-form-item label="总学时" prop="totalHours">
              <el-input-number v-model="courseForm.totalHours" :min="16" :max="128" />
            </el-form-item>
          </el-col>
          
          <el-col :span="8">
            <el-form-item label="最大人数" prop="maxStudents">
              <el-input-number v-model="courseForm.maxStudents" :min="10" :max="100" />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="课程类型" prop="courseType">
              <el-select v-model="courseForm.courseType" placeholder="选择课程类型">
                <el-option label="理论课程" value="THEORY" />
                <el-option label="实践课程" value="PRACTICE" />
                <el-option label="理实结合" value="MIXED" />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="教学模式" prop="teachingMode">
              <el-select v-model="courseForm.teachingMode" placeholder="选择教学模式">
                <el-option label="线上" value="ONLINE" />
                <el-option label="线下" value="OFFLINE" />
                <el-option label="混合" value="HYBRID" />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="开始日期" prop="startDate">
              <el-date-picker v-model="courseForm.startDate" type="date" placeholder="选择开始日期" />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="结束日期" prop="endDate">
              <el-date-picker v-model="courseForm.endDate" type="date" placeholder="选择结束日期" />
            </el-form-item>
          </el-col>
          
          <el-col :span="24">
            <el-form-item label="课程描述" prop="description">
              <el-input v-model="courseForm.description" type="textarea" :rows="4" placeholder="请输入课程详细描述" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <el-button @click="showCreateCourseDialog = false">取消</el-button>
        <el-button type="primary" @click="saveCourse" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Reading, Loading, User, UserFilled, ArrowDown } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { mentorApi } from '@/api/mentors'

const auth = useAuthStore()

// 响应式数据
const activeTab = ref('courses')
const courseLoading = ref(false)
const mentorLoading = ref(false)
const enrollmentLoading = ref(false)
const saving = ref(false)

const courses = ref([])
const mentors = ref([])
const enrollments = ref([])
const stats = ref({
  totalCourses: 0,
  activeCourses: 0,
  totalMentors: 0,
  totalEnrollments: 0
})

// 分页
const coursePagination = reactive({ page: 1, size: 10, total: 0 })
const mentorPagination = reactive({ page: 1, size: 10, total: 0 })
const enrollmentPagination = reactive({ page: 1, size: 10, total: 0 })

// 搜索表单
const courseSearch = reactive({
  search: '',
  status: '',
  mode: ''
})

const mentorSearch = reactive({
  search: '',
  status: ''
})

// 对话框状态
const showCreateCourseDialog = ref(false)
const editingCourse = ref(null)

// 表单数据
const courseForm = reactive({
  courseCode: '',
  courseName: '',
  description: '',
  universityId: '',
  companyId: '',
  universityTeacherId: '',
  enterpriseMentorId: '',
  credits: 3,
  totalHours: 48,
  maxStudents: 30,
  courseType: 'MIXED',
  teachingMode: 'HYBRID',
  startDate: '',
  endDate: ''
})

// 表单验证规则
const courseRules = {
  courseCode: [{ required: true, message: '请输入课程代码', trigger: 'blur' }],
  courseName: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  universityId: [{ required: true, message: '请选择合作高校', trigger: 'change' }],
  companyId: [{ required: true, message: '请选择合作企业', trigger: 'change' }],
  universityTeacherId: [{ required: true, message: '请选择高校导师', trigger: 'change' }],
  enterpriseMentorId: [{ required: true, message: '请选择企业导师', trigger: 'change' }]
}

const courseFormRef = ref()

// 计算属性
const canCreateCourse = computed(() => {
  return auth.hasPermission('MENTOR_CREATE') || auth.hasPermission('COURSE_CREATE')
})

const canGrade = computed(() => {
  return auth.user?.role === 'TEACHER' || auth.user?.role === 'ENTERPRISE_MENTOR'
})

// 方法
const handleTabChange = (tab) => {
  switch (tab.name) {
    case 'courses':
      loadCourses()
      break
    case 'mentors':
      loadMentors()
      break
    case 'enrollments':
      loadEnrollments()
      break
  }
}

const loadCourses = async () => {
  courseLoading.value = true
  try {
    const params = {
      page: coursePagination.page - 1,
      size: coursePagination.size,
      ...courseSearch
    }
    const response = await mentorApi.getCourses(params)
    courses.value = response.data.items
    coursePagination.total = response.data.total
  } catch (error) {
    ElMessage.error('加载课程列表失败')
  } finally {
    courseLoading.value = false
  }
}

const loadMentors = async () => {
  mentorLoading.value = true
  try {
    const params = {
      page: mentorPagination.page - 1,
      size: mentorPagination.size,
      ...mentorSearch
    }
    const response = await mentorApi.getMentors(params)
    mentors.value = response.data.items
    mentorPagination.total = response.data.total
  } catch (error) {
    ElMessage.error('加载导师列表失败')
  } finally {
    mentorLoading.value = false
  }
}

const loadEnrollments = async () => {
  enrollmentLoading.value = true
  try {
    const params = {
      page: enrollmentPagination.page - 1,
      size: enrollmentPagination.size
    }
    const response = await mentorApi.getEnrollments(params)
    enrollments.value = response.data.items
    enrollmentPagination.total = response.data.total
  } catch (error) {
    ElMessage.error('加载选课列表失败')
  } finally {
    enrollmentLoading.value = false
  }
}

const loadStats = async () => {
  try {
    const response = await mentorApi.getStats()
    stats.value = response.data
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const handleCourseSearch = () => {
  coursePagination.page = 1
  loadCourses()
}

const handleMentorSearch = () => {
  mentorPagination.page = 1
  loadMentors()
}

const viewCourse = (course) => {
  // 显示课程详情
}

const editCourse = (course) => {
  editingCourse.value = course
  Object.assign(courseForm, course)
  showCreateCourseDialog.value = true
}

const handleCourseCommand = (command, course) => {
  switch (command) {
    case 'enrollments':
      // 显示选课管理
      break
    case 'delete':
      deleteCourse(course)
      break
  }
}

const deleteCourse = async (course) => {
  try {
    await ElMessageBox.confirm(`确定要删除课程"${course.courseName}"吗？`, '确认删除', {
      type: 'warning'
    })
    
    await mentorApi.deleteCourse(course.id)
    ElMessage.success('课程删除成功')
    loadCourses()
    loadStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除课程失败')
    }
  }
}

const saveCourse = async () => {
  if (!courseFormRef.value) return
  
  const valid = await courseFormRef.value.validate()
  if (!valid) return
  
  saving.value = true
  try {
    if (editingCourse.value) {
      await mentorApi.updateCourse(editingCourse.value.id, courseForm)
      ElMessage.success('课程更新成功')
    } else {
      await mentorApi.createCourse(courseForm)
      ElMessage.success('课程创建成功')
    }
    
    showCreateCourseDialog.value = false
    loadCourses()
    loadStats()
  } catch (error) {
    ElMessage.error('保存课程失败')
  } finally {
    saving.value = false
  }
}

const resetCourseForm = () => {
  editingCourse.value = null
  Object.assign(courseForm, {
    courseCode: '',
    courseName: '',
    description: '',
    universityId: '',
    companyId: '',
    universityTeacherId: '',
    enterpriseMentorId: '',
    credits: 3,
    totalHours: 48,
    maxStudents: 30,
    courseType: 'MIXED',
    teachingMode: 'HYBRID',
    startDate: '',
    endDate: ''
  })
  if (courseFormRef.value) {
    courseFormRef.value.resetFields()
  }
}

const viewMentor = (mentor) => {
  // 显示导师详情
}

const editMentor = (mentor) => {
  // 编辑导师信息
}

const gradeStudent = (enrollment) => {
  // 成绩录入
}

// 权限检查
const canEditCourse = (course) => {
  return auth.hasPermission('MENTOR_UPDATE') || auth.hasPermission('COURSE_UPDATE')
}

const canDeleteCourse = (course) => {
  return auth.hasPermission('MENTOR_DELETE') || auth.hasPermission('COURSE_DELETE')
}

const canEditMentor = (mentor) => {
  return auth.hasPermission('MENTOR_UPDATE')
}

// 辅助方法
const getCourseTypeDisplayName = (type) => {
  const names = { THEORY: '理论', PRACTICE: '实践', MIXED: '理实结合' }
  return names[type] || type
}

const getCourseTypeTagType = (type) => {
  const types = { THEORY: 'primary', PRACTICE: 'success', MIXED: 'warning' }
  return types[type] || ''
}

const getTeachingModeDisplayName = (mode) => {
  const names = { ONLINE: '线上', OFFLINE: '线下', HYBRID: '混合' }
  return names[mode] || mode
}

const getTeachingModeTagType = (mode) => {
  const types = { ONLINE: 'primary', OFFLINE: 'success', HYBRID: 'warning' }
  return types[mode] || ''
}

const getCourseStatusDisplayName = (status) => {
  const names = {
    PLANNING: '筹划中',
    RECRUITING: '招生中',
    IN_PROGRESS: '进行中',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return names[status] || status
}

const getCourseStatusTagType = (status) => {
  const types = {
    PLANNING: 'info',
    RECRUITING: 'warning',
    IN_PROGRESS: 'primary',
    COMPLETED: 'success',
    CANCELLED: 'danger'
  }
  return types[status] || ''
}

const getMentorStatusDisplayName = (status) => {
  const names = { ACTIVE: '活跃', INACTIVE: '非活跃', SUSPENDED: '已暂停' }
  return names[status] || status
}

const getMentorStatusTagType = (status) => {
  const types = { ACTIVE: 'success', INACTIVE: 'info', SUSPENDED: 'danger' }
  return types[status] || ''
}

const getEnrollmentStatusDisplayName = (status) => {
  const names = { ENROLLED: '已选课', COMPLETED: '已完成', DROPPED: '已退课', FAILED: '不及格' }
  return names[status] || status
}

const getEnrollmentStatusTagType = (status) => {
  const types = { ENROLLED: 'primary', COMPLETED: 'success', DROPPED: 'info', FAILED: 'danger' }
  return types[status] || ''
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

// 生命周期
onMounted(() => {
  loadCourses()
  loadStats()
})
</script>

<style scoped>
.mentor-management {
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

.course-title .title {
  font-weight: 500;
  color: #303133;
}

.course-title .code {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.mentors {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.mentor {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.pagination-wrapper {
  padding: 20px;
  display: flex;
  justify-content: center;
}
</style>
