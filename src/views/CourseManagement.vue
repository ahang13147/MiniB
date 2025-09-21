<template>
  <div class="course-wrap">
    <div class="bg-blobs">
      <div class="blob b1" />
      <div class="blob b2" />
      <div class="blob b3" />
    </div>
    <div class="card">
      <div class="brand-area">
        <div class="logo">CM</div>
        <div class="title-group">
          <h2 class="title">课程管理</h2>
          <p class="subtitle">课程CRUD · 教师分配 · 学生管理</p>
        </div>
      </div>

      <!-- 筛选与操作区 -->
      <div class="filters">
        <el-select v-model="query.teacherId" placeholder="教师" clearable style="width: 140px">
          <el-option v-for="t in teachers" :key="t.id" :label="t.name" :value="t.id" />
        </el-select>
        <el-select v-model="query.department" placeholder="学院" clearable style="width: 140px">
          <el-option v-for="d in departments" :key="d" :label="d" :value="d" />
        </el-select>
        <el-select v-model="query.semester" placeholder="学期" clearable style="width: 120px">
          <el-option v-for="s in semesters" :key="s" :label="s" :value="s" />
        </el-select>
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px">
          <el-option label="草稿" value="DRAFT" />
          <el-option label="已发布" value="PUBLISHED" />
          <el-option label="已归档" value="ARCHIVED" />
        </el-select>
        <el-input v-model="query.keyword" placeholder="搜索课程代码/名称" clearable style="width: 200px" />
        <el-button type="primary" @click="loadCourses">查询</el-button>
        <el-button type="success" @click="showCreateDialog = true">新增课程</el-button>
      </div>

      <!-- 课程表格 -->
      <el-table :data="courses.items" v-loading="loading" stripe class="table">
        <el-table-column prop="code" label="课程代码" width="100" />
        <el-table-column prop="name" label="课程名称" width="180" />
        <el-table-column prop="teacherName" label="授课教师" width="120" />
        <el-table-column prop="department" label="学院" width="120" />
        <el-table-column prop="semester" label="学期" width="100" />
        <el-table-column prop="credits" label="学分" width="80" />
        <el-table-column prop="hours" label="学时" width="80" />
        <el-table-column prop="studentCount" label="选课人数" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="editCourse(row)">编辑</el-button>
            <el-button size="small" type="info" @click="manageStudents(row)">学生</el-button>
            <el-button size="small" type="danger" @click="deleteCourse(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="courses.total"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadCourses"
          @size-change="loadCourses"
        />
      </div>
    </div>

    <!-- 新增/编辑课程对话框 -->
    <el-dialog v-model="showCreateDialog" :title="editingCourse ? '编辑课程' : '新增课程'" width="600px">
      <el-form :model="courseForm" :rules="courseRules" ref="courseFormRef" label-width="80px">
        <el-form-item label="课程代码" prop="code" v-if="!editingCourse">
          <el-input v-model="courseForm.code" placeholder="请输入课程代码" />
        </el-form-item>
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="courseForm.name" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="课程描述" prop="description">
          <el-input v-model="courseForm.description" type="textarea" :rows="3" placeholder="请输入课程描述" />
        </el-form-item>
        <el-form-item label="授课教师" prop="teacherId">
          <el-select v-model="courseForm.teacherId" placeholder="请选择教师">
            <el-option v-for="t in teachers" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学院" prop="department">
          <el-input v-model="courseForm.department" placeholder="请输入学院" />
        </el-form-item>
        <el-form-item label="学期" prop="semester">
          <el-input v-model="courseForm.semester" placeholder="请输入学期" />
        </el-form-item>
        <el-form-item label="学分" prop="credits">
          <el-input-number v-model="courseForm.credits" :min="1" :max="10" />
        </el-form-item>
        <el-form-item label="学时" prop="hours">
          <el-input-number v-model="courseForm.hours" :min="1" :max="200" />
        </el-form-item>
        <el-form-item label="状态" prop="status" v-if="editingCourse">
          <el-select v-model="courseForm.status" placeholder="请选择状态">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已发布" value="PUBLISHED" />
            <el-option label="已归档" value="ARCHIVED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="saveCourse" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 学生管理对话框 -->
    <el-dialog v-model="showStudentDialog" title="学生管理" width="500px">
      <div class="student-management">
        <div class="student-list">
          <h4>已选课学生 ({{ currentCourse?.studentCount || 0 }})</h4>
          <el-table :data="currentCourse?.students || []" size="small">
            <el-table-column prop="studentId" label="学生ID" />
            <el-table-column label="操作">
              <template #default="{ row }">
                <el-button size="small" type="danger" @click="removeStudentFromCourse(row)">移除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <div class="add-student">
          <h4>添加学生</h4>
          <el-input v-model="newStudentId" placeholder="输入学生ID" style="margin-bottom: 10px" />
          <el-button type="primary" @click="addStudentToCourse">添加</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  listCourses, createCourse, updateCourse, deleteCourse as deleteCourseApi, 
  assignStudent, removeStudent, getTeachers, getDepartments, getSemesters,
  type Course, type CreateCourseRequest, type UpdateCourseRequest, type Teacher 
} from '@/api/courses'

const courses = reactive({ items: [] as Course[], total: 0 })
const teachers = ref<Teacher[]>([])
const departments = ref<string[]>([])
const semesters = ref<string[]>([])
const loading = ref(false)
const saving = ref(false)
const showCreateDialog = ref(false)
const showStudentDialog = ref(false)
const editingCourse = ref<Course | null>(null)
const currentCourse = ref<Course | null>(null)
const newStudentId = ref('')
const courseFormRef = ref()

const query = reactive({
  page: 1,
  pageSize: 10,
  teacherId: '',
  department: '',
  semester: '',
  status: '',
  keyword: ''
})

const courseForm = reactive<CreateCourseRequest & UpdateCourseRequest & { status: string }>({
  code: '',
  name: '',
  description: '',
  teacherId: '',
  department: '',
  semester: '',
  credits: 3,
  hours: 48,
  status: 'DRAFT'
})

const courseRules = {
  code: [{ required: true, message: '请输入课程代码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  teacherId: [{ required: true, message: '请选择教师', trigger: 'change' }],
  credits: [{ required: true, message: '请输入学分', trigger: 'blur' }],
  hours: [{ required: true, message: '请输入学时', trigger: 'blur' }]
}

function getStatusLabel(status: string) {
  const labels: Record<string, string> = {
    'DRAFT': '草稿',
    'PUBLISHED': '已发布',
    'ARCHIVED': '已归档'
  }
  return labels[status] || status
}

function getStatusType(status: string) {
  const types: Record<string, string> = {
    'DRAFT': 'warning',
    'PUBLISHED': 'success',
    'ARCHIVED': 'info'
  }
  return types[status] || ''
}

function formatDate(dateStr: string) {
  return new Date(dateStr).toLocaleString('zh-CN')
}

async function loadCourses() {
  loading.value = true
  try {
    Object.assign(courses, await listCourses(query))
  } catch (error) {
    ElMessage.error('加载课程列表失败')
  } finally {
    loading.value = false
  }
}

async function loadTeachers() {
  try {
    teachers.value = await getTeachers()
  } catch (error) {
    console.error('加载教师列表失败', error)
  }
}

async function loadDepartments() {
  try {
    departments.value = await getDepartments()
  } catch (error) {
    console.error('加载学院列表失败', error)
  }
}

async function loadSemesters() {
  try {
    semesters.value = await getSemesters()
  } catch (error) {
    console.error('加载学期列表失败', error)
  }
}

function editCourse(course: Course) {
  editingCourse.value = course
  Object.assign(courseForm, {
    name: course.name,
    description: course.description || '',
    teacherId: course.teacherId,
    department: course.department || '',
    semester: course.semester || '',
    credits: course.credits,
    hours: course.hours,
    status: course.status
  })
  showCreateDialog.value = true
}

async function saveCourse() {
  try {
    await courseFormRef.value?.validate()
    saving.value = true

    if (editingCourse.value) {
      const updateData: UpdateCourseRequest = {
        name: courseForm.name,
        description: courseForm.description,
        teacherId: courseForm.teacherId,
        department: courseForm.department,
        semester: courseForm.semester,
        credits: courseForm.credits,
        hours: courseForm.hours,
        status: courseForm.status
      }
      await updateCourse(editingCourse.value.id, updateData)
      ElMessage.success('课程更新成功')
    } else {
      const createData: CreateCourseRequest = {
        code: courseForm.code,
        name: courseForm.name,
        description: courseForm.description,
        teacherId: courseForm.teacherId,
        department: courseForm.department,
        semester: courseForm.semester,
        credits: courseForm.credits,
        hours: courseForm.hours
      }
      await createCourse(createData)
      ElMessage.success('课程创建成功')
    }

    showCreateDialog.value = false
    editingCourse.value = null
    resetForm()
    await loadCourses()
  } catch (error) {
    ElMessage.error(editingCourse.value ? '课程更新失败' : '课程创建失败')
  } finally {
    saving.value = false
  }
}

async function deleteCourse(id: string) {
  try {
    await ElMessageBox.confirm('确定要删除此课程吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteCourseApi(id)
    ElMessage.success('课程删除成功')
    await loadCourses()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('课程删除失败')
    }
  }
}

function manageStudents(course: Course) {
  currentCourse.value = course
  showStudentDialog.value = true
}

async function addStudentToCourse() {
  if (!newStudentId.value.trim() || !currentCourse.value) return
  
  try {
    await assignStudent(currentCourse.value.id, newStudentId.value.trim())
    ElMessage.success('学生添加成功')
    newStudentId.value = ''
    await loadCourses()
    // Refresh current course data
    const updatedCourse = await listCourses({ page: 1, pageSize: 1000 })
    const course = updatedCourse.items.find(c => c.id === currentCourse.value?.id)
    if (course) currentCourse.value = course
  } catch (error) {
    ElMessage.error('学生添加失败')
  }
}

async function removeStudentFromCourse(studentId: string) {
  if (!currentCourse.value) return
  
  try {
    await ElMessageBox.confirm('确定要移除此学生吗？', '确认移除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await removeStudent(currentCourse.value.id, studentId)
    ElMessage.success('学生移除成功')
    await loadCourses()
    // Refresh current course data
    const updatedCourse = await listCourses({ page: 1, pageSize: 1000 })
    const course = updatedCourse.items.find(c => c.id === currentCourse.value?.id)
    if (course) currentCourse.value = course
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('学生移除失败')
    }
  }
}

function resetForm() {
  Object.assign(courseForm, {
    code: '',
    name: '',
    description: '',
    teacherId: '',
    department: '',
    semester: '',
    credits: 3,
    hours: 48,
    status: 'DRAFT'
  })
}

onMounted(() => {
  loadCourses()
  loadTeachers()
  loadDepartments()
  loadSemesters()
})
</script>

<style scoped>
.course-wrap { min-height: 100vh; display:grid; align-items: start; justify-items: center; padding-top: 18px; background: radial-gradient(1200px 600px at 10% 10%, rgba(99,102,241,0.25), transparent 40%), radial-gradient(1000px 500px at 90% 20%, rgba(16,185,129,0.25), transparent 45%), linear-gradient(135deg, #0f172a 0%, #1f2937 100%); position: relative; overflow: hidden; }
.bg-blobs { position: absolute; inset: 0; pointer-events: none; filter: blur(40px); opacity: .6; }
.blob { position: absolute; border-radius: 50%; transform: translate(-50%, -50%); }
.b1 { width: 420px; height: 420px; left: 12%; top: 18%; background: #6366f1; animation: float 12s ease-in-out infinite; }
.b2 { width: 380px; height: 380px; right: 1%; top: 10%; background: #22c55e; animation: float 14s ease-in-out infinite 1s; }
.b3 { width: 220px; height: 220px; left: 70%; bottom: -5%; background: #06b6d4; animation: float 16s ease-in-out infinite 2s; }
@keyframes float { 0%,100% { transform: translate(-50%, -50%) translateY(0); } 50% { transform: translate(-50%, -50%) translateY(-16px); } }

.card { width: min(96vw, 1200px); padding: 22px; border-radius: 20px; background: rgba(255,255,255,0.06); backdrop-filter: saturate(160%) blur(16px); box-shadow: 0 20px 50px rgba(0,0,0,0.45); color: #e5e7eb; border: 1px solid rgba(255,255,255,0.08); position: relative; }
.brand-area { display: flex; align-items: center; gap: 14px; margin-bottom: 16px; }
.logo { width: 46px; height: 46px; border-radius: 12px; display:grid; place-items:center; font-weight: 800; color:#0b1220; background: linear-gradient(135deg, #93c5fd 0%, #a78bfa 100%); box-shadow: inset 0 0 30px rgba(255,255,255,0.5), 0 10px 25px rgba(0,0,0,0.25); }
.title { margin: 0; font-size: 20px; color:#fff; }
.subtitle { margin: 2px 0 8px; color:#cbd5e1; font-size: 12px; }
.filters { display:flex; gap:10px; flex-wrap: wrap; margin-bottom: 16px; }
.table { background: transparent; }
.pagination { margin-top: 16px; display: flex; justify-content: center; }

.student-management { display: flex; gap: 20px; }
.student-list { flex: 1; }
.add-student { width: 200px; }
.student-management h4 { margin: 0 0 10px; color: #e5e7eb; }
</style>

