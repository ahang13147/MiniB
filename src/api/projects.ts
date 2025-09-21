import { apiClient } from './client'

export interface Project {
  id: string
  title: string
  description: string
  projectType: 'RESEARCH' | 'DEVELOPMENT' | 'CONSULTING' | 'TRAINING' | 'INTERNSHIP'
  initiatorType: 'UNIVERSITY' | 'COMPANY'
  initiatorId: string
  initiatorName: string
  partnerType?: 'UNIVERSITY' | 'COMPANY'
  partnerId?: string
  partnerName?: string
  status: 'DRAFT' | 'PUBLISHED' | 'MATCHING' | 'NEGOTIATING' | 'APPROVED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT'
  budget?: number
  currency: string
  startDate?: string
  endDate?: string
  actualStartDate?: string
  actualEndDate?: string
  tags?: string[]
  requirements?: string
  deliverables?: string
  progressPercentage?: number
  createdBy: string
  createdByName: string
  createdAt: string
  updatedAt: string
  participantCount?: number
  milestoneCount?: number
}

export interface ProjectStats {
  totalProjects: number
  activeProjects: number
  completedProjects: number
  myProjects: number
  totalBudget: number
  completedBudget: number
}

export interface CreateProjectRequest {
  title: string
  description?: string
  projectType: string
  initiatorType: string
  initiatorId: string
  partnerType?: string
  partnerId?: string
  priority?: string
  budget?: number
  currency?: string
  startDate?: string
  endDate?: string
  tags?: string[]
  requirements?: string
  deliverables?: string
}

export interface UpdateProjectRequest {
  title?: string
  description?: string
  projectType?: string
  partnerType?: string
  partnerId?: string
  status?: string
  priority?: string
  budget?: number
  currency?: string
  startDate?: string
  endDate?: string
  actualStartDate?: string
  actualEndDate?: string
  tags?: string[]
  requirements?: string
  deliverables?: string
  progressPercentage?: number
}

export interface PageResult<T> {
  items: T[]
  total: number
  page: number
  size: number
  totalPages: number
}

export const projectApi = {
  // 获取项目列表
  getProjects: (params?: {
    page?: number
    size?: number
    search?: string
    type?: string
    status?: string
    initiatorType?: string
  }) => {
    return apiClient.get<PageResult<Project>>('/api/projects', { params })
  },

  // 获取项目详情
  getProject: (id: string) => {
    return apiClient.get<Project>(`/api/projects/${id}`)
  },

  // 创建项目
  createProject: (data: CreateProjectRequest) => {
    return apiClient.post<Project>('/api/projects', data)
  },

  // 更新项目
  updateProject: (id: string, data: UpdateProjectRequest) => {
    return apiClient.put<Project>(`/api/projects/${id}`, data)
  },

  // 删除项目
  deleteProject: (id: string) => {
    return apiClient.delete(`/api/projects/${id}`)
  },

  // 获取统计信息
  getStats: () => {
    return apiClient.get<ProjectStats>('/api/projects/stats')
  },

  // 获取项目参与人员
  getParticipants: (projectId: string) => {
    return apiClient.get(`/api/projects/${projectId}/participants`)
  },

  // 添加参与人员
  addParticipant: (projectId: string, data: any) => {
    return apiClient.post(`/api/projects/${projectId}/participants`, data)
  },

  // 获取项目里程碑
  getMilestones: (projectId: string) => {
    return apiClient.get(`/api/projects/${projectId}/milestones`)
  },

  // 创建里程碑
  createMilestone: (projectId: string, data: any) => {
    return apiClient.post(`/api/projects/${projectId}/milestones`, data)
  },

  // 获取项目资金
  getFunds: (projectId: string) => {
    return apiClient.get(`/api/projects/${projectId}/funds`)
  },

  // 添加资金记录
  addFund: (projectId: string, data: any) => {
    return apiClient.post(`/api/projects/${projectId}/funds`, data)
  }
}
