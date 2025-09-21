<template>
  <div class="company-wrap">
    <div class="bg-blobs">
      <div class="blob b1" />
      <div class="blob b2" />
      <div class="blob b3" />
    </div>
    <div class="card">
      <div class="brand-area">
        <div class="logo">CM</div>
        <div class="title-group">
          <h2 class="title">企业管理</h2>
          <p class="subtitle">企业信息管理 · 合作项目管理 · 导师分配</p>
        </div>
      </div>

      <!-- 筛选与操作区 -->
      <div class="filters">
        <el-select v-model="query.industry" placeholder="行业" clearable style="width: 140px">
          <el-option v-for="i in industries" :key="i" :label="i" :value="i" />
        </el-select>
        <el-select v-model="query.scale" placeholder="规模" clearable style="width: 120px">
          <el-option label="小型" value="SMALL" />
          <el-option label="中型" value="MEDIUM" />
          <el-option label="大型" value="LARGE" />
        </el-select>
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px">
          <el-option label="待审核" value="PENDING" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="已拒绝" value="REJECTED" />
          <el-option label="已暂停" value="SUSPENDED" />
        </el-select>
        <el-input v-model="query.keyword" placeholder="搜索企业名称/联系人" clearable style="width: 200px" />
        <el-button type="primary" @click="loadCompanies">查询</el-button>
        <el-button type="success" @click="showCreateDialog = true">新增企业</el-button>
      </div>

      <!-- 企业表格 -->
      <el-table :data="companies.items" v-loading="loading" stripe class="table">
        <el-table-column prop="name" label="企业名称" width="180" />
        <el-table-column prop="industry" label="行业" width="120" />
        <el-table-column prop="scale" label="规模" width="100">
          <template #default="{ row }">
            <el-tag :type="getScaleType(row.scale)">{{ getScaleLabel(row.scale) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="contactPerson" label="联系人" width="100" />
        <el-table-column prop="contactTitle" label="职位" width="120" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column prop="mentorCount" label="导师数" width="80" />
        <el-table-column prop="projectCount" label="项目数" width="80" />
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
            <el-button size="small" @click="editCompany(row)">编辑</el-button>
            <el-button size="small" type="info" @click="manageMentors(row)">导师</el-button>
            <el-button size="small" type="danger" @click="deleteCompany(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="companies.total"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadCompanies"
          @size-change="loadCompanies"
        />
      </div>
    </div>

    <!-- 新增/编辑企业对话框 -->
    <el-dialog v-model="showCreateDialog" :title="editingCompany ? '编辑企业' : '新增企业'" width="700px">
      <el-form :model="companyForm" :rules="companyRules" ref="companyFormRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="企业名称" prop="name">
              <el-input v-model="companyForm.name" placeholder="请输入企业名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="行业" prop="industry">
              <el-select v-model="companyForm.industry" placeholder="请选择行业">
                <el-option v-for="i in industries" :key="i" :label="i" :value="i" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="企业规模" prop="scale">
              <el-select v-model="companyForm.scale" placeholder="请选择规模">
                <el-option label="小型" value="SMALL" />
                <el-option label="中型" value="MEDIUM" />
                <el-option label="大型" value="LARGE" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="官网" prop="website">
              <el-input v-model="companyForm.website" placeholder="请输入官网地址" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="企业描述" prop="description">
          <el-input v-model="companyForm.description" type="textarea" :rows="3" placeholder="请输入企业描述" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系人" prop="contactPerson">
              <el-input v-model="companyForm.contactPerson" placeholder="请输入联系人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职位" prop="contactTitle">
              <el-input v-model="companyForm.contactTitle" placeholder="请输入联系人职位" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="companyForm.email" placeholder="请输入邮箱地址" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="电话" prop="phone">
              <el-input v-model="companyForm.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="地址" prop="address">
          <el-input v-model="companyForm.address" placeholder="请输入企业地址" />
        </el-form-item>
        <el-form-item label="状态" prop="status" v-if="editingCompany">
          <el-select v-model="companyForm.status" placeholder="请选择状态">
            <el-option label="待审核" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
            <el-option label="已暂停" value="SUSPENDED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="saveCompany" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 导师管理对话框 -->
    <el-dialog v-model="showMentorDialog" title="导师管理" width="500px">
      <div class="mentor-management">
        <div class="mentor-list">
          <h4>企业导师 ({{ currentCompany?.mentorCount || 0 }})</h4>
          <el-table :data="currentCompany?.mentors || []" size="small">
            <el-table-column prop="mentorId" label="导师ID" />
            <el-table-column label="操作">
              <template #default="{ row }">
                <el-button size="small" type="danger" @click="removeMentorFromCompany(row)">移除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <div class="add-mentor">
          <h4>添加导师</h4>
          <el-input v-model="newMentorId" placeholder="输入导师ID" style="margin-bottom: 10px" />
          <el-button type="primary" @click="addMentorToCompany">添加</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  listCompanies, createCompany, updateCompany, deleteCompany as deleteCompanyApi, 
  assignMentor, removeMentor, getIndustries, getScales,
  type Company, type CreateCompanyRequest, type UpdateCompanyRequest 
} from '@/api/companies'

const companies = reactive({ items: [] as Company[], total: 0 })
const industries = ref<string[]>([])
const scales = ref<string[]>([])
const loading = ref(false)
const saving = ref(false)
const showCreateDialog = ref(false)
const showMentorDialog = ref(false)
const editingCompany = ref<Company | null>(null)
const currentCompany = ref<Company | null>(null)
const newMentorId = ref('')
const companyFormRef = ref()

const query = reactive({
  page: 1,
  pageSize: 10,
  industry: '',
  scale: '',
  status: '',
  keyword: ''
})

const companyForm = reactive<CreateCompanyRequest & UpdateCompanyRequest & { status: string }>({
  name: '',
  description: '',
  industry: '',
  scale: '',
  website: '',
  email: '',
  phone: '',
  address: '',
  contactPerson: '',
  contactTitle: '',
  status: 'PENDING'
})

const companyRules = {
  name: [{ required: true, message: '请输入企业名称', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }]
}

function getScaleLabel(scale: string) {
  const labels: Record<string, string> = {
    'SMALL': '小型',
    'MEDIUM': '中型',
    'LARGE': '大型'
  }
  return labels[scale] || scale
}

function getScaleType(scale: string) {
  const types: Record<string, string> = {
    'SMALL': 'success',
    'MEDIUM': 'warning',
    'LARGE': 'danger'
  }
  return types[scale] || ''
}

function getStatusLabel(status: string) {
  const labels: Record<string, string> = {
    'PENDING': '待审核',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝',
    'SUSPENDED': '已暂停'
  }
  return labels[status] || status
}

function getStatusType(status: string) {
  const types: Record<string, string> = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger',
    'SUSPENDED': 'info'
  }
  return types[status] || ''
}

function formatDate(dateStr: string) {
  return new Date(dateStr).toLocaleString('zh-CN')
}

async function loadCompanies() {
  loading.value = true
  try {
    Object.assign(companies, await listCompanies(query))
  } catch (error) {
    ElMessage.error('加载企业列表失败')
  } finally {
    loading.value = false
  }
}

async function loadIndustries() {
  try {
    industries.value = await getIndustries()
  } catch (error) {
    console.error('加载行业列表失败', error)
  }
}

async function loadScales() {
  try {
    scales.value = await getScales()
  } catch (error) {
    console.error('加载规模列表失败', error)
  }
}

function editCompany(company: Company) {
  editingCompany.value = company
  Object.assign(companyForm, {
    name: company.name,
    description: company.description || '',
    industry: company.industry || '',
    scale: company.scale || '',
    website: company.website || '',
    email: company.email || '',
    phone: company.phone || '',
    address: company.address || '',
    contactPerson: company.contactPerson || '',
    contactTitle: company.contactTitle || '',
    status: company.status
  })
  showCreateDialog.value = true
}

async function saveCompany() {
  try {
    await companyFormRef.value?.validate()
    saving.value = true

    if (editingCompany.value) {
      const updateData: UpdateCompanyRequest = {
        name: companyForm.name,
        description: companyForm.description,
        industry: companyForm.industry,
        scale: companyForm.scale,
        website: companyForm.website,
        email: companyForm.email,
        phone: companyForm.phone,
        address: companyForm.address,
        contactPerson: companyForm.contactPerson,
        contactTitle: companyForm.contactTitle,
        status: companyForm.status
      }
      await updateCompany(editingCompany.value.id, updateData)
      ElMessage.success('企业信息更新成功')
    } else {
      const createData: CreateCompanyRequest = {
        name: companyForm.name,
        description: companyForm.description,
        industry: companyForm.industry,
        scale: companyForm.scale,
        website: companyForm.website,
        email: companyForm.email,
        phone: companyForm.phone,
        address: companyForm.address,
        contactPerson: companyForm.contactPerson,
        contactTitle: companyForm.contactTitle
      }
      await createCompany(createData)
      ElMessage.success('企业创建成功')
    }

    showCreateDialog.value = false
    editingCompany.value = null
    resetForm()
    await loadCompanies()
  } catch (error) {
    ElMessage.error(editingCompany.value ? '企业信息更新失败' : '企业创建失败')
  } finally {
    saving.value = false
  }
}

async function deleteCompany(id: string) {
  try {
    await ElMessageBox.confirm('确定要删除此企业吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteCompanyApi(id)
    ElMessage.success('企业删除成功')
    await loadCompanies()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('企业删除失败')
    }
  }
}

function manageMentors(company: Company) {
  currentCompany.value = company
  showMentorDialog.value = true
}

async function addMentorToCompany() {
  if (!newMentorId.value.trim() || !currentCompany.value) return
  
  try {
    await assignMentor(currentCompany.value.id, newMentorId.value.trim())
    ElMessage.success('导师添加成功')
    newMentorId.value = ''
    await loadCompanies()
    // Refresh current company data
    const updatedCompanies = await listCompanies({ page: 1, pageSize: 1000 })
    const company = updatedCompanies.items.find(c => c.id === currentCompany.value?.id)
    if (company) currentCompany.value = company
  } catch (error) {
    ElMessage.error('导师添加失败')
  }
}

async function removeMentorFromCompany(mentorId: string) {
  if (!currentCompany.value) return
  
  try {
    await ElMessageBox.confirm('确定要移除此导师吗？', '确认移除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await removeMentor(currentCompany.value.id, mentorId)
    ElMessage.success('导师移除成功')
    await loadCompanies()
    // Refresh current company data
    const updatedCompanies = await listCompanies({ page: 1, pageSize: 1000 })
    const company = updatedCompanies.items.find(c => c.id === currentCompany.value?.id)
    if (company) currentCompany.value = company
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('导师移除失败')
    }
  }
}

function resetForm() {
  Object.assign(companyForm, {
    name: '',
    description: '',
    industry: '',
    scale: '',
    website: '',
    email: '',
    phone: '',
    address: '',
    contactPerson: '',
    contactTitle: '',
    status: 'PENDING'
  })
}

onMounted(() => {
  loadCompanies()
  loadIndustries()
  loadScales()
})
</script>

<style scoped>
.company-wrap { min-height: 100vh; display:grid; align-items: start; justify-items: center; padding-top: 18px; background: radial-gradient(1200px 600px at 10% 10%, rgba(99,102,241,0.25), transparent 40%), radial-gradient(1000px 500px at 90% 20%, rgba(16,185,129,0.25), transparent 45%), linear-gradient(135deg, #0f172a 0%, #1f2937 100%); position: relative; overflow: hidden; }
.bg-blobs { position: absolute; inset: 0; pointer-events: none; filter: blur(40px); opacity: .6; }
.blob { position: absolute; border-radius: 50%; transform: translate(-50%, -50%); }
.b1 { width: 420px; height: 420px; left: 12%; top: 18%; background: #6366f1; animation: float 12s ease-in-out infinite; }
.b2 { width: 380px; height: 380px; right: 1%; top: 10%; background: #22c55e; animation: float 14s ease-in-out infinite 1s; }
.b3 { width: 220px; height: 220px; left: 70%; bottom: -5%; background: #06b6d4; animation: float 16s ease-in-out infinite 2s; }
@keyframes float { 0%,100% { transform: translate(-50%, -50%) translateY(0); } 50% { transform: translate(-50%, -50%) translateY(-16px); } }

.card { width: min(96vw, 1400px); padding: 22px; border-radius: 20px; background: rgba(255,255,255,0.06); backdrop-filter: saturate(160%) blur(16px); box-shadow: 0 20px 50px rgba(0,0,0,0.45); color: #e5e7eb; border: 1px solid rgba(255,255,255,0.08); position: relative; }
.brand-area { display: flex; align-items: center; gap: 14px; margin-bottom: 16px; }
.logo { width: 46px; height: 46px; border-radius: 12px; display:grid; place-items:center; font-weight: 800; color:#0b1220; background: linear-gradient(135deg, #93c5fd 0%, #a78bfa 100%); box-shadow: inset 0 0 30px rgba(255,255,255,0.5), 0 10px 25px rgba(0,0,0,0.25); }
.title { margin: 0; font-size: 20px; color:#fff; }
.subtitle { margin: 2px 0 8px; color:#cbd5e1; font-size: 12px; }
.filters { display:flex; gap:10px; flex-wrap: wrap; margin-bottom: 16px; }
.table { background: transparent; }
.pagination { margin-top: 16px; display: flex; justify-content: center; }

.mentor-management { display: flex; gap: 20px; }
.mentor-list { flex: 1; }
.add-mentor { width: 200px; }
.mentor-management h4 { margin: 0 0 10px; color: #e5e7eb; }
</style>

