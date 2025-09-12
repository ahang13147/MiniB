<template>
  <div class="rc-wrap">
    <div class="bg-blobs">
      <div class="blob b1" />
      <div class="blob b2" />
      <div class="blob b3" />
    </div>
    <div class="card">
      <div class="brand-area">
        <div class="logo">RC</div>
        <div class="title-group">
          <h2 class="title">资源中心</h2>
          <p class="subtitle">科研资源共享 · 课程资源库</p>
        </div>
      </div>

      <el-tabs v-model="active" class="tabs">
        <el-tab-pane label="科研资源共享" name="research">
          <div class="filters">
            <el-select v-model="qResearch.type" placeholder="资源类型" clearable style="width: 150px">
              <el-option v-for="t in researchTypes" :key="t.value" :label="t.label" :value="t.value" />
            </el-select>
            <el-select v-model="qResearch.ownership" placeholder ="归属" clearable style="width: 140px">
              <el-option label="企业" value="ENTERPRISE" />
              <el-option label="学校" value="SCHOOL" />
            </el-select>
            <el-select v-model="qResearch.status" placeholder="状态" clearable style="width: 140px">
              <el-option label="可被借用" value="AVAILABLE" />
              <el-option label="已被借用" value="BORROWED" />
            </el-select>
            <el-input v-model="qResearch.keyword" placeholder="搜索名称/描述" clearable style="width: 220px" />
            <el-button type="primary" @click="loadResearch">查询</el-button>
          </div>
          <el-table :data="research.items" v-loading="loadingResearch" stripe class="table">
            <el-table-column prop="id" label="资源ID" width="110" />
            <el-table-column prop="type" label="类型" width="120" />
            <el-table-column prop="name" label="名称" min-width="160" />
            <el-table-column prop="description" label="描述" min-width="220" />
            <el-table-column prop="ownership" label="归属" width="90" />
            <el-table-column prop="status" label="状态" width="100" />
            <el-table-column prop="publisher" label="发布人" width="110" />
            <el-table-column prop="borrower" label="借用人" width="110" />
            <el-table-column prop="location" label="位置" width="140" />
            <el-table-column label="操作" width="140">
              <template #default="{ row }">
                <el-button v-if="row.status==='AVAILABLE'" size="small" type="primary" @click="doBorrow(row.id)">借用</el-button>
                <el-button v-else size="small" @click="doReturn(row.id)">归还</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="课程资源库" name="courses">
          <div class="filters">
            <el-select v-model="qCourse.type" placeholder="资源类型" clearable style="width: 180px">
              <el-option v-for="t in courseTypes" :key="t.value" :label="t.label" :value="t.value" />
            </el-select>
            <el-select v-model="qCourse.ownership" placeholder="归属" clearable style="width: 140px">
              <el-option label="企业" value="ENTERPRISE" />
              <el-option label="学校" value="SCHOOL" />
            </el-select>
            <el-input v-model="qCourse.keyword" placeholder="搜索名称/描述" clearable style="width: 220px" />
            <el-button type="primary" @click="loadCourses">查询</el-button>
          </div>
          <el-table :data="courses.items" v-loading="loadingCourses" stripe class="table">
            <el-table-column prop="id" label="资源ID" width="110" />
            <el-table-column prop="type" label="类型" width="140" />
            <el-table-column prop="name" label="名称" min-width="180" />
            <el-table-column prop="description" label="描述" min-width="240" />
            <el-table-column prop="ownership" label="归属" width="100" />
            <el-table-column prop="publisher" label="发布人" width="120" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { listResearch, listCourses, borrowResearch, returnResearch, type ResearchResourceType, type CourseResourceType } from '@/api/resources'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const active = ref<'research' | 'courses'>('research')

const research = reactive({ items: [] as any[], total: 0 })
const courses = reactive({ items: [] as any[], total: 0 })
const loadingResearch = ref(false)
const loadingCourses = ref(false)

const qResearch = reactive<{ page:number; pageSize:number; type?: ResearchResourceType; ownership?: 'ENTERPRISE'|'SCHOOL'; status?: 'AVAILABLE'|'BORROWED'; keyword?: string }>({ page:1, pageSize:10 })
const qCourse = reactive<{ page:number; pageSize:number; type?: CourseResourceType; ownership?: 'ENTERPRISE'|'SCHOOL'; keyword?: string }>({ page:1, pageSize:10 })

const researchTypes = [
  { label: '实验设备', value: 'LAB_EQUIPMENT' },
  { label: '科研数据', value: 'RESEARCH_DATA' },
  { label: '技术文件', value: 'TECHNICAL_DOC' }
]
const courseTypes = [
  { label: '优质课程', value: 'QUALITY_COURSE' },
  { label: '教学案例', value: 'TEACHING_CASE' },
  { label: '课件', value: 'COURSEWARE' },
  { label: '其他', value: 'OTHER' }
]

async function loadResearch() {
  loadingResearch.value = true
  try { Object.assign(research, await listResearch(qResearch)) } finally { loadingResearch.value = false }
}
async function loadCourses() {
  loadingCourses.value = true
  try { Object.assign(courses, await listCourses(qCourse)) } finally { loadingCourses.value = false }
}

async function doBorrow(id: string) {
  const borrower = auth.user?.displayName || 'unknown'
  await borrowResearch(id, borrower)
  await loadResearch()
}
async function doReturn(id: string) {
  await returnResearch(id)
  await loadResearch()
}

onMounted(() => { loadResearch(); loadCourses() })
</script>

<style scoped>
.rc-wrap { min-height: 100vh; display:grid; align-items: start; justify-items: center; padding-top: 32px; background: radial-gradient(1200px 600px at 10% 10%, rgba(99,102,241,0.25), transparent 40%), radial-gradient(1000px 500px at 90% 20%, rgba(16,185,129,0.25), transparent 45%), linear-gradient(135deg, #0f172a 0%, #1f2937 100%); position: relative; overflow: hidden; }
.bg-blobs { position: absolute; inset: 0; pointer-events: none; filter: blur(40px); opacity: .6; }
.blob { position: absolute; border-radius: 50%; transform: translate(-50%, -50%); }
.b1 { width: 420px; height: 420px; left: 12%; top: 18%; background: #6366f1; animation: float 12s ease-in-out infinite; }
.b2 { width: 380px; height: 380px; right: 1%; top: 10%; background: #22c55e; animation: float 14s ease-in-out infinite 1s; }
.b3 { width: 220px; height: 220px; left: 70%; bottom: -5%; background: #06b6d4; animation: float 16s ease-in-out infinite 2s; }
@keyframes float { 0%,100% { transform: translate(-50%, -50%) translateY(0); } 50% { transform: translate(-50%, -50%) translateY(-20px); } }

.card { width: min(96vw, 1100px); padding: 22px; border-radius: 20px; background: rgba(255,255,255,0.06); backdrop-filter: saturate(160%) blur(16px); box-shadow: 0 20px 50px rgba(0,0,0,0.45); color: #e5e7eb; border: 1px solid rgba(255,255,255,0.08); position: relative; }
.brand-area { display: flex; align-items: center; gap: 14px; margin-bottom: 6px; }
.logo { width: 46px; height: 46px; border-radius: 12px; display:grid; place-items:center; font-weight: 800; color:#0b1220; background: linear-gradient(135deg, #93c5fd 0%, #a78bfa 100%); box-shadow: inset 0 0 30px rgba(255,255,255,0.5), 0 10px 25px rgba(0,0,0,0.25); }
.title { margin: 0; font-size: 20px; color:#fff; }
.subtitle { margin: 2px 0 8px; color:#cbd5e1; font-size: 12px; }
.tabs { margin-top: 6px; }
.tabs :deep(.el-tabs__nav-wrap::after){ background: transparent; }
.tabs :deep(.el-tabs__item){
  color:#cbd5e1; font-weight:600; letter-spacing:.2px; transition: color .2s ease, transform .2s ease;
}
.tabs :deep(.el-tabs__item:hover){ color:#a5b4fc; transform: translateY(-1px); }
.tabs :deep(.el-tabs__item.is-active){ color:#ffffff; text-shadow: 0 0 10px rgba(147,197,253,.5); }
.tabs :deep(.el-tabs__active-bar){ height:3px; background: linear-gradient(90deg, #60a5fa 0%, #a78bfa 100%); box-shadow: 0 0 16px rgba(96,165,250,.6); }
.tabs :deep(.el-tabs__header){
  padding: 2px 6px; border-radius: 12px; backdrop-filter: blur(6px);
}
.filters { display:flex; gap:10px; flex-wrap: wrap; margin-bottom: 10px; }
.table { background: transparent; }
</style>


