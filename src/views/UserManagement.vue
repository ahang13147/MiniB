<template>
  <div class="user-wrap">
    <div class="bg-blobs">
      <div class="blob b1" />
      <div class="blob b2" />
      <div class="blob b3" />
    </div>
    <div class="card">
      <div class="brand-area">
        <div class="logo">UM</div>
        <div class="title-group">
          <h2 class="title">用户管理</h2>
          <p class="subtitle">用户增删改查 · 角色权限管理</p>
        </div>
      </div>

      <!-- 筛选与操作区 -->
      <div class="filters">
        <el-select v-model="query.role" placeholder="角色" clearable style="width: 140px">
          <el-option v-for="r in roleOptions" :key="r.value" :label="r.label" :value="r.value" />
        </el-select>
        <el-select v-model="query.department" placeholder="部门" clearable style="width: 160px">
          <el-option v-for="d in departments" :key="d" :label="d" :value="d" />
        </el-select>
        <el-select v-model="query.enabled" placeholder="状态" clearable style="width: 120px">
          <el-option label="启用" :value="true" />
          <el-option label="禁用" :value="false" />
        </el-select>
        <el-input v-model="query.keyword" placeholder="搜索用户名/姓名/邮箱" clearable style="width: 240px" />
        <el-button type="primary" @click="loadUsers">查询</el-button>
        <el-button type="success" @click="showCreateDialog = true">新增用户</el-button>
      </div>

      <!-- 用户表格 -->
      <el-table :data="users.items" v-loading="loading" stripe class="table">
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="displayName" label="姓名" width="120" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column prop="role" label="角色" width="140">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)">{{ getRoleLabel(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="department" label="部门" width="120" />
        <el-table-column prop="position" label="职位" width="120" />
        <el-table-column prop="enabled" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'">{{ row.enabled ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" @click="editUser(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteUser(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="users.total"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadUsers"
          @size-change="loadUsers"
        />
      </div>
    </div>

    <!-- 新增/编辑用户对话框 -->
    <el-dialog v-model="showCreateDialog" :title="editingUser ? '编辑用户' : '新增用户'" width="600px">
      <el-form :model="userForm" :rules="userRules" ref="userFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username" v-if="!editingUser">
          <el-input v-model="userForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!editingUser">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="姓名" prop="displayName">
          <el-input v-model="userForm.displayName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入电话" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" placeholder="请选择角色">
            <el-option v-for="r in roleOptions" :key="r.value" :label="r.label" :value="r.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="部门" prop="department">
          <el-input v-model="userForm.department" placeholder="请输入部门" />
        </el-form-item>
        <el-form-item label="职位" prop="position">
          <el-input v-model="userForm.position" placeholder="请输入职位" />
        </el-form-item>
        <el-form-item label="状态" prop="enabled" v-if="editingUser">
          <el-switch v-model="userForm.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="saveUser" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listUsers, createUser, updateUser, deleteUser as deleteUserApi, getDepartments, type User, type CreateUserRequest, type UpdateUserRequest } from '@/api/users'
import type { Role } from '@/stores/auth'

const users = reactive({ items: [] as User[], total: 0 })
const departments = ref<string[]>([])
const loading = ref(false)
const saving = ref(false)
const showCreateDialog = ref(false)
const editingUser = ref<User | null>(null)
const userFormRef = ref()

const query = reactive({
  page: 1,
  pageSize: 10,
  role: undefined as Role | undefined,
  department: '',
  enabled: undefined as boolean | undefined,
  keyword: ''
})

const userForm = reactive<CreateUserRequest & UpdateUserRequest & { enabled: boolean }>({
  username: '',
  password: '',
  displayName: '',
  email: '',
  phone: '',
  role: 'STUDENT' as Role,
  department: '',
  position: '',
  enabled: true
})

const userRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  displayName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const roleOptions = [
  { label: '超级管理员', value: 'SUPER_ADMIN' },
  { label: '高校管理员', value: 'UNIVERSITY_ADMIN' },
  { label: '企业管理员', value: 'ENTERPRISE_ADMIN' },
  { label: '教师', value: 'TEACHER' },
  { label: '学生', value: 'STUDENT' },
  { label: '企业导师', value: 'ENTERPRISE_MENTOR' }
]

function getRoleLabel(role: Role) {
  const option = roleOptions.find(r => r.value === role)
  return option?.label || role
}

function getRoleType(role: Role) {
  const types: Record<Role, string> = {
    'SUPER_ADMIN': 'danger',
    'UNIVERSITY_ADMIN': 'warning',
    'ENTERPRISE_ADMIN': 'warning',
    'TEACHER': 'primary',
    'STUDENT': 'success',
    'ENTERPRISE_MENTOR': 'info'
  }
  return types[role] || ''
}

function formatDate(dateStr: string) {
  return new Date(dateStr).toLocaleString('zh-CN')
}

async function loadUsers() {
  loading.value = true
  try {
    Object.assign(users, await listUsers(query))
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

async function loadDepartments() {
  try {
    departments.value = await getDepartments()
  } catch (error) {
    console.error('加载部门列表失败', error)
  }
}

function editUser(user: User) {
  editingUser.value = user
  Object.assign(userForm, {
    displayName: user.displayName,
    email: user.email || '',
    phone: user.phone || '',
    role: user.role,
    department: user.department || '',
    position: user.position || '',
    enabled: user.enabled
  })
  showCreateDialog.value = true
}

async function saveUser() {
  try {
    await userFormRef.value?.validate()
    saving.value = true

    if (editingUser.value) {
      const updateData: UpdateUserRequest = {
        displayName: userForm.displayName,
        email: userForm.email,
        phone: userForm.phone,
        role: userForm.role,
        department: userForm.department,
        position: userForm.position,
        enabled: userForm.enabled
      }
      await updateUser(editingUser.value.id, updateData)
      ElMessage.success('用户更新成功')
    } else {
      const createData: CreateUserRequest = {
        username: userForm.username,
        password: userForm.password,
        displayName: userForm.displayName,
        email: userForm.email,
        phone: userForm.phone,
        role: userForm.role,
        department: userForm.department,
        position: userForm.position
      }
      await createUser(createData)
      ElMessage.success('用户创建成功')
    }

    showCreateDialog.value = false
    editingUser.value = null
    resetForm()
    await loadUsers()
  } catch (error) {
    ElMessage.error(editingUser.value ? '用户更新失败' : '用户创建失败')
  } finally {
    saving.value = false
  }
}

async function deleteUser(id: string) {
  try {
    await ElMessageBox.confirm('确定要删除此用户吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteUserApi(id)
    ElMessage.success('用户删除成功')
    await loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('用户删除失败')
    }
  }
}

function resetForm() {
  Object.assign(userForm, {
    username: '',
    password: '',
    displayName: '',
    email: '',
    phone: '',
    role: 'STUDENT' as Role,
    department: '',
    position: '',
    enabled: true
  })
}

onMounted(() => {
  loadUsers()
  loadDepartments()
})
</script>

<style scoped>
.user-wrap { min-height: 100vh; display:grid; align-items: start; justify-items: center; padding-top: 18px; background: radial-gradient(1200px 600px at 10% 10%, rgba(99,102,241,0.25), transparent 40%), radial-gradient(1000px 500px at 90% 20%, rgba(16,185,129,0.25), transparent 45%), linear-gradient(135deg, #0f172a 0%, #1f2937 100%); position: relative; overflow: hidden; }
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
</style>

