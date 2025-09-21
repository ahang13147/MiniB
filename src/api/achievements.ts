import { apiClient } from './client'

export interface StudentAchievement {
  id: string
  studentId: string
  studentName: string
  studentMajor: string
  studentGrade: string
  title: string
  description: string
  achievementType: 'COMPETITION' | 'RESEARCH' | 'PROJECT' | 'PATENT' | 'PUBLICATION' | 'CERTIFICATE' | 'OTHER'
  category?: string
  level: 'SCHOOL' | 'CITY' | 'PROVINCE' | 'NATIONAL' | 'INTERNATIONAL'
  awardRank?: string
  organization?: string
  achievementDate: string
  certificateUrl?: string
  attachmentUrls?: string[]
  visibility: 'PRIVATE' | 'UNIVERSITY' | 'PUBLIC'
  status: 'DRAFT' | 'PUBLISHED' | 'VERIFIED' | 'REJECTED'
  verificationNotes?: string
  createdAt: string
  updatedAt: string
}

export interface AchievementStats {
  totalAchievements: number
  verifiedAchievements: number
  pendingVerification: number
  topStudents: StudentStats[]
  typeStats: TypeStats[]
  levelStats: LevelStats[]
  categoryStats: CategoryStats[]
}

export interface StudentStats {
  studentId: string
  studentName: string
  studentMajor: string
  achievementCount: number
  verifiedCount: number
}

export interface TypeStats {
  type: string
  count: number
  verifiedCount: number
}

export interface LevelStats {
  level: string
  count: number
  verifiedCount: number
}

export interface CategoryStats {
  category: string
  count: number
  verifiedCount: number
}

export interface CreateAchievementRequest {
  title: string
  description?: string
  achievementType: string
  category?: string
  level: string
  awardRank?: string
  organization?: string
  achievementDate: string
  certificateUrl?: string
  attachmentUrls?: string[]
  visibility?: string
}

export interface UpdateAchievementRequest {
  title?: string
  description?: string
  achievementType?: string
  category?: string
  level?: string
  awardRank?: string
  organization?: string
  achievementDate?: string
  certificateUrl?: string
  attachmentUrls?: string[]
  visibility?: string
}

export interface VerifyAchievementRequest {
  status: 'VERIFIED' | 'REJECTED'
  verificationNotes?: string
}

export interface PageResult<T> {
  items: T[]
  total: number
  page: number
  size: number
  totalPages: number
}

export const achievementApi = {
  // 获取成果列表
  getAchievements: (params?: {
    page?: number
    size?: number
    search?: string
    studentId?: string
    type?: string
    level?: string
    status?: string
    visibility?: string
  }) => {
    return apiClient.get<PageResult<StudentAchievement>>('/api/achievements', { params })
  },

  // 获取成果详情
  getAchievement: (id: string) => {
    return apiClient.get<StudentAchievement>(`/api/achievements/${id}`)
  },

  // 创建成果
  createAchievement: (data: CreateAchievementRequest) => {
    return apiClient.post<StudentAchievement>('/api/achievements', data)
  },

  // 更新成果
  updateAchievement: (id: string, data: UpdateAchievementRequest) => {
    return apiClient.put<StudentAchievement>(`/api/achievements/${id}`, data)
  },

  // 删除成果
  deleteAchievement: (id: string) => {
    return apiClient.delete(`/api/achievements/${id}`)
  },

  // 验证成果
  verifyAchievement: (id: string, data: VerifyAchievementRequest) => {
    return apiClient.post<StudentAchievement>(`/api/achievements/${id}/verify`, data)
  },

  // 发布成果
  publishAchievement: (id: string) => {
    return apiClient.post<StudentAchievement>(`/api/achievements/${id}/publish`)
  },

  // 获取学生成果
  getStudentAchievements: (studentId: string) => {
    return apiClient.get<StudentAchievement[]>(`/api/achievements/student/${studentId}`)
  },

  // 获取公开成果
  getPublicAchievements: (params?: {
    page?: number
    size?: number
    type?: string
    level?: string
  }) => {
    return apiClient.get<PageResult<StudentAchievement>>('/api/achievements/public', { params })
  },

  // 获取统计信息
  getStats: () => {
    return apiClient.get<AchievementStats>('/api/achievements/stats')
  }
}
