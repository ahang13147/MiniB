<template>
  <div class="dash-wrap">
    <div class="bg-blobs">
      <div class="blob b1" />
      <div class="blob b2" />
      <div class="blob b3" />
    </div>

    <div class="page">
    <div class="topbar glass">
      <div class="left">欢迎，{{ auth.user?.displayName }}（{{ auth.user?.role }}）</div>
      <div class="right">
        <el-button link @click="logout">退出登录</el-button>
      </div>
    </div>
    <!-- 标题区 -->
    <div class="brand-area glass">
      <div class="logo">DB</div>
      <div class="title-group">
        <h2 class="brand">仪表盘</h2>
        <p class="subtitle">角色定制化总览 · 一目了然</p>
      </div>
    </div>

    <!-- 关键指标统计卡片 -->
    <div class="kpi-grid">
      <el-card v-for="k in kpis" :key="k.key" class="kpi-card">
        <div class="kpi-title">{{ k.title }}</div>
        <div class="kpi-value">{{ k.value }}</div>
        <div class="kpi-sub">{{ k.sub }}</div>
      </el-card>
    </div>

    <div class="layout">
      <!-- 左侧：趋势图 + 活动时间线 -->
      <div class="left-col">
        <el-card class="block glass">
          <div class="block-title">项目趋势</div>
          <div class="line-chart">
            <svg :viewBox="`0 0 ${lineChartWidth} ${lineChartHeight}`" preserveAspectRatio="none">
              <defs>
                <linearGradient id="lineGrad" x1="0%" y1="0%" x2="100%" y2="0%">
                  <stop offset="0%" stop-color="#60a5fa" />
                  <stop offset="100%" stop-color="#a78bfa" />
                </linearGradient>
              </defs>
              <polyline :points="linePoints" fill="none" stroke="url(#lineGrad)" stroke-width="2.5" />
              <g v-for="(p, idx) in normalizedPoints" :key="idx">
                <circle :cx="p.x" :cy="p.y" r="3" fill="#409eff" />
              </g>
            </svg>
            <div class="line-legend">
              <span v-for="(m, i) in months" :key="m" :class="['tick', { first: i===0 }]">{{ m }}</span>
            </div>
          </div>
        </el-card>

        <el-card class="block glass">
          <div class="block-title">近期活动</div>
          <el-timeline>
            <el-timeline-item v-for="(a, i) in activities" :key="i" :timestamp="a.time" :type="a.type">
              {{ a.text }}
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </div>

      <!-- 右侧：快速入口 + 待办 + 人才分布 -->
      <div class="right-col">
        <el-card class="block glass">
          <div class="block-title">快速操作</div>
          <div class="quick-actions">
            <el-button v-for="qa in quickActions" :key="qa.text" type="primary" plain @click="qa.onClick()">{{ qa.text }}</el-button>
          </div>
        </el-card>

        <el-card class="block glass">
          <div class="block-title">待办事项</div>
          <div class="todo-input">
            <el-input v-model="newTodo" placeholder="输入待办后回车添加" @keyup.enter="addTodo" />
          </div>
          <div class="todo-list">
            <div v-for="t in todos" :key="t.id" class="todo-item">
              <el-checkbox v-model="t.done" @change="persistTodos">{{ t.text }}</el-checkbox>
              <el-button text type="danger" size="small" @click="removeTodo(t.id)">删除</el-button>
            </div>
          </div>
        </el-card>

        <el-card class="block glass">
          <div class="block-title">人才分布</div>
          <div class="donut">
            <svg viewBox="0 0 120 120">
              <g transform="translate(60,60)">
                <template v-for="(seg, idx) in donutSegments" :key="idx">
                  <circle r="40" fill="transparent" stroke-width="20"
                          :stroke="seg.color" stroke-linecap="butt"
                          :stroke-dasharray="`${seg.arc} ${donutCircumference - seg.arc}`"
                          :transform="`rotate(${seg.rotate})`" />
                </template>
                <circle r="20" fill="#fff" />
                <text text-anchor="middle" dy="6" font-size="12">总计 {{ talentTotal }}</text>
              </g>
            </svg>
            <div class="legend">
              <div v-for="s in talent" :key="s.name" class="legend-item">
                <span class="dot" :style="{ background: s.color }"></span>
                <span class="name">{{ s.name }}</span>
                <span class="value">{{ s.value }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 角色定制化数据看板：按权限展示功能入口 -->
    <div class="grid">
      <el-card v-for="m in menus" :key="m.path" class="card" @click="go(m.path)">
        <div class="title">{{ m.title }}</div>
        <div class="desc">{{ m.desc }}</div>
      </el-card>
    </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore, type Permission } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

const allMenus = [
  { title: '仪表盘', desc: '总览数据', path: '/dashboard', perm: 'DASHBOARD_VIEW' },
  { title: '个人资料', desc: '个人信息管理', path: '/profile', perm: 'DASHBOARD_VIEW' },
  { title: '消息通知', desc: '查看系统消息', path: '/notifications', perm: 'DASHBOARD_VIEW' },
  { title: '系统设置', desc: '个性化配置', path: '/settings', perm: 'DASHBOARD_VIEW' },
  { title: '用户管理', desc: '管理员可见', path: '/users', perm: 'USER_MANAGE' },
  { title: '课程管理', desc: '教师/高校管理员', path: '/courses', perm: 'COURSE_MANAGE' },
  { title: '企业管理', desc: '企业管理员', path: '/companies', perm: 'COMPANY_MANAGE' },
  { title: '项目协作', desc: '校企合作项目管理', path: '/projects', perm: 'PROJECT_VIEW' },
  { title: '实习就业', desc: '实习职位与申请管理', path: '/internships', perm: 'INTERNSHIP_VIEW' },
  { title: '成果展示', desc: '学生成果与竞赛获奖', path: '/achievements', perm: 'ACHIEVEMENT_VIEW' },
  { title: '双导师课堂', desc: '企业导师合作教学', path: '/mentors', perm: 'MENTOR_VIEW' },
  { title: '学生信息', desc: '可查看学生', path: '/students', perm: 'STUDENT_VIEW' },
  { title: '教师信息', desc: '可查看教师', path: '/teachers', perm: 'TEACHER_VIEW' },
  { title: '资源中心', desc: '科研共享与课程资源', path: '/resources', perm: 'DASHBOARD_VIEW' }
]

const menus = computed(() => {
  const perms = new Set(auth.user?.permissions || [])
  return allMenus.filter(m => perms.has(m.perm as Permission))
})

function go(path: string) { router.push(path) }
function logout() { auth.logout(); router.replace('/login') }

// 关键指标（示例数据，可后续替换为真实接口）
const kpis = computed(() => {
  const role = auth.user?.role || '访客'
  const base = role.includes('管理员') ? 1.2 : 1
  function n(v: number) { return Math.round(v * base) }
  return [
    { key: 'projects', title: '项目数', value: n(128), sub: '本月 +12' },
    { key: 'users', title: '用户数', value: n(864), sub: '活跃 63%' },
    { key: 'partners', title: '合作数', value: n(42), sub: '新增 3' },
    { key: 'resources', title: '资源数', value: n(560), sub: '共享占比 48%' }
  ]
})

// 趋势图（简单 SVG 折线图）
const months = ['3月', '4月', '5月', '6月', '7月', '8月']
const trend = ref<number[]>([20, 28, 26, 34, 40, 48])
const lineChartWidth = 300
const lineChartHeight = 120
const normalizedPoints = computed(() => {
  const max = Math.max(...trend.value, 1)
  const stepX = lineChartWidth / (trend.value.length - 1)
  return trend.value.map((v, i) => ({ x: i * stepX, y: lineChartHeight - (v / max) * (lineChartHeight - 6) - 3 }))
})
const linePoints = computed(() => normalizedPoints.value.map(p => `${p.x},${p.y}`).join(' '))

// 活动时间线
const activities = reactive([
  { time: '09:20', text: '创建新项目《智能推荐平台》', type: 'primary' },
  { time: '昨天', text: '审批通过合作单位《XX 科研院》', type: 'success' },
  { time: '前天', text: '发布课程资源 12 个', type: 'warning' }
])

// 快速入口
const quickActions = [
  { text: '新建项目', onClick: () => router.push('/projects/new') },
  { text: '上传资源', onClick: () => router.push('/resources') },
  { text: '邀请成员', onClick: () => router.push('/users') }
]

// 待办事项（本地持久化）
type Todo = { id: string; text: string; done: boolean }
const newTodo = ref('')
const todos = ref<Todo[]>([])
const todoKey = computed(() => `dashboard.todos.${auth.user?.id || 'guest'}`)
function loadTodos() {
  try {
    const raw = localStorage.getItem(todoKey.value)
    todos.value = raw ? JSON.parse(raw) : []
  } catch { todos.value = [] }
}
function persistTodos() { localStorage.setItem(todoKey.value, JSON.stringify(todos.value)) }
function addTodo() {
  const text = newTodo.value.trim()
  if (!text) return
  todos.value.unshift({ id: String(Date.now()), text, done: false })
  newTodo.value = ''
  persistTodos()
}
function removeTodo(id: string) {
  todos.value = todos.value.filter(t => t.id !== id)
  persistTodos()
}
watch(todoKey, loadTodos)
onMounted(loadTodos)

// 人才分布（简单环形图）
const talent = reactive([
  { name: '学生', value: 120, color: '#67C23A' },
  { name: '教师', value: 48, color: '#409EFF' },
  { name: '企业导师', value: 24, color: '#E6A23C' }
])
const talentTotal = computed(() => talent.reduce((s, t) => s + t.value, 0))
const donutCircumference = 2 * Math.PI * 40
const donutSegments = computed(() => {
  let acc = -90
  return talent.map(t => {
    const ratio = t.value / (talentTotal.value || 1)
    const arc = donutCircumference * ratio
    const item = { arc, rotate: acc, color: t.color }
    acc += ratio * 360
    return item
  })
})
</script>

<style scoped>
.dash-wrap { min-height: 100vh; display:grid; align-items: start; justify-items: center; padding-top: 18px; background: radial-gradient(1200px 600px at 10% 10%, rgba(99,102,241,0.25), transparent 40%), radial-gradient(1000px 500px at 90% 20%, rgba(16,185,129,0.25), transparent 45%), linear-gradient(135deg, #0f172a 0%, #1f2937 100%); position: relative; overflow: hidden; }
.bg-blobs { position: absolute; inset: 0; pointer-events: none; filter: blur(40px); opacity: .6; }
.blob { position: absolute; border-radius: 50%; transform: translate(-50%, -50%); }
.b1 { width: 420px; height: 420px; left: 12%; top: 18%; background: #6366f1; animation: float 12s ease-in-out infinite; }
.b2 { width: 380px; height: 380px; right: 1%; top: 10%; background: #22c55e; animation: float 14s ease-in-out infinite 1s; }
.b3 { width: 220px; height: 220px; left: 70%; bottom: -5%; background: #06b6d4; animation: float 16s ease-in-out infinite 2s; }
@keyframes float { 0%,100% { transform: translate(-50%, -50%) translateY(0); } 50% { transform: translate(-50%, -50%) translateY(-16px); } }

.page { width: min(96vw, 1100px); padding: 12px; }
.glass { background: rgba(255,255,255,0.06); backdrop-filter: saturate(160%) blur(16px); box-shadow: 0 20px 50px rgba(0,0,0,0.45); color: #e5e7eb; border: 1px solid rgba(255,255,255,0.08); border-radius: 16px; }
.topbar { display:flex; justify-content: space-between; align-items:center; margin-bottom: 12px; padding: 10px 14px; }
.topbar .left { font-weight: 600; letter-spacing: .3px; }
.kpi-grid { display:grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 12px; margin-bottom: 16px; }
.kpi-card { padding: 8px 12px; border-radius: 14px; background: rgba(255,255,255,0.06); border: 1px solid rgba(255,255,255,0.08); color:#e5e7eb; }
.kpi-title { color:#666; font-size: 13px; }
.kpi-value { font-size: 28px; font-weight: 700; margin: 6px 0; }
.kpi-sub { color:#909399; font-size: 12px; }
 .kpi-card { transition: transform .2s ease, box-shadow .2s ease; }
 .kpi-card:hover { transform: translateY(-2px); box-shadow: 0 12px 30px rgba(0,0,0,.35); }

.layout { display: grid; grid-template-columns: 2fr 1fr; gap: 16px; margin-bottom: 20px; }
.left-col, .right-col { display: flex; flex-direction: column; gap: 16px; }
.block-title { font-weight: 600; margin-bottom: 8px; }

.line-chart { width: 100%; }
.line-chart svg { width: 100%; height: 160px; }
.line-legend { display:flex; justify-content: space-between; color:#909399; font-size: 12px; margin-top: 4px; }
.line-legend .tick.first { margin-left: 2px; }

.quick-actions { display:flex; flex-wrap: wrap; gap: 8px; }
 .quick-actions .el-button { transition: transform .15s ease; }
 .quick-actions .el-button:hover { transform: translateY(-1px); }

.todo-input { margin-bottom: 10px; }
.todo-list { display:flex; flex-direction: column; gap: 8px; }
.todo-item { display:flex; justify-content: space-between; align-items:center; }

.donut { display:flex; align-items:center; gap: 12px; }
.donut svg { width: 160px; height: 160px; }
.legend { display:flex; flex-direction: column; gap: 6px; }
.legend-item { display:flex; align-items:center; gap: 8px; }
.legend-item .dot { width: 10px; height: 10px; border-radius: 50%; display:inline-block; }

.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 16px; }
.card { cursor: pointer; background: rgba(255,255,255,0.06); border: 1px solid rgba(255,255,255,0.08); color:#e5e7eb; border-radius: 14px; }
 .card { transition: transform .2s ease, box-shadow .2s ease; }
 .card:hover { transform: translateY(-2px); box-shadow: 0 12px 30px rgba(0,0,0,.35); }

.brand-area { display:flex; align-items:center; gap:12px; padding: 10px 14px; margin-bottom: 10px; }
.logo { width: 40px; height: 40px; border-radius: 12px; display:grid; place-items:center; font-weight: 800; color:#0b1220; background: linear-gradient(135deg, #93c5fd 0%, #a78bfa 100%); box-shadow: inset 0 0 30px rgba(255,255,255,0.5), 0 10px 25px rgba(0,0,0,0.25); }
.brand { margin: 0; font-size: 18px; color:#fff; }
.subtitle { margin: 0; color:#cbd5e1; font-size: 12px; }
.title { font-weight: 600; margin-bottom: 8px; }
.desc { color: #cbd5e1; }

@media (max-width: 1080px) {
  .layout { grid-template-columns: 1fr; }
}
</style>




