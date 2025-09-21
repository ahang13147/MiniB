import { apiClient } from './client'

export interface InternshipPosition {
  id: string
  companyId: string
  companyName: string
  title: string
  description: string
  department: string
  location: string
  positionType: 'FULL_TIME' | 'PART_TIME' | 'REMOTE'
  durationMonths?: number
  salaryMin?: number
  salaryMax?: number
  currency: string
  requirements?: string
  responsibilities?: string
  skillsRequired?: string[]
  majorPreferred?: string[]
  gradeRequirement: 'FRESHMAN' | 'SOPHOMORE' | 'JUNIOR' | 'SENIOR' | 'GRADUATE' | 'ANY'
  positionsAvailable: number
  positionsFilled: number
  applicationDeadline: string
  startDate: string
  endDate?: string
  status: 'DRAFT' | 'PUBLISHED' | 'CLOSED' | 'CANCELLED'
  createdBy: string
  createdByName: string
  createdAt: string
  updatedAt: string
  applicationCount?: number
}

export interface InternshipApplication {
  id: string
  positionId: string
  positionTitle: string
  companyName: string
  studentId: string
  studentName: string
  studentEmail: string
  studentPhone: string
  studentMajor: string
  studentGrade: string
  coverLetter?: string
  resumeUrl?: string
  portfolioUrl?: string
  gpa?: number
  skills?: string[]
  experience?: string
  status: 'SUBMITTED' | 'UNDER_REVIEW' | 'INTERVIEW_SCHEDULED' | 'INTERVIEWED' | 'ACCEPTED' | 'REJECTED' | 'WITHDRAWN'
  interviewDate?: string
  interviewFeedback?: string
  rejectionReason?: string
  appliedAt: string
  updatedAt: string
}

export interface InternshipRecord {
  id: string
  applicationId: string
  studentId: string
  studentName: string
  companyId: string
  companyName: string
  positionTitle: string
  mentorId?: string
  mentorName?: string
  supervisorId?: string
  supervisorName?: string
  startDate: string
  endDate?: string
  actualEndDate?: string
  department?: string
  location?: string
  salary?: number
  status: 'ONGOING' | 'COMPLETED' | 'TERMINATED' | 'EXTENDED'
  performanceScore?: number
  mentorFeedback?: string
  supervisorFeedback?: string
  studentFeedback?: string
  certificateUrl?: string
  createdAt: string
  updatedAt: string
}

export interface InternshipStats {
  totalPositions: number
  activePositions: number
  totalApplications: number
  acceptedApplications: number
  ongoingInternships: number
  completedInternships: number
  averageSalary: number
}

export interface CreatePositionRequest {
  title: string
  description?: string
  department?: string
  location: string
  positionType: string
  durationMonths?: number
  salaryMin?: number
  salaryMax?: number
  currency?: string
  requirements?: string
  responsibilities?: string
  skillsRequired?: string[]
  majorPreferred?: string[]
  gradeRequirement?: string
  positionsAvailable?: number
  applicationDeadline: string
  startDate: string
  endDate?: string
}

export interface ApplyInternshipRequest {
  coverLetter?: string
  resumeUrl?: string
  portfolioUrl?: string
  gpa?: number
  skills?: string[]
  experience?: string
}

export interface PageResult<T> {
  items: T[]
  total: number
  page: number
  size: number
  totalPages: number
}

export const internshipApi = {
  // 职位管理
  getPositions: (params?: {
    page?: number
    size?: number
    search?: string
    companyId?: string
    type?: string
    status?: string
    location?: string
  }) => {
    return apiClient.get<PageResult<InternshipPosition>>('/api/internships/positions', { params })
  },

  getPosition: (id: string) => {
    return apiClient.get<InternshipPosition>(`/api/internships/positions/${id}`)
  },

  createPosition: (data: CreatePositionRequest) => {
    return apiClient.post<InternshipPosition>('/api/internships/positions', data)
  },

  updatePosition: (id: string, data: Partial<CreatePositionRequest>) => {
    return apiClient.put<InternshipPosition>(`/api/internships/positions/${id}`, data)
  },

  deletePosition: (id: string) => {
    return apiClient.delete(`/api/internships/positions/${id}`)
  },

  // 申请管理
  getApplications: (params?: {
    page?: number
    size?: number
    positionId?: string
    studentId?: string
    status?: string
  }) => {
    return apiClient.get<PageResult<InternshipApplication>>('/api/internships/applications', { params })
  },

  getApplication: (id: string) => {
    return apiClient.get<InternshipApplication>(`/api/internships/applications/${id}`)
  },

  applyPosition: (positionId: string, data: ApplyInternshipRequest) => {
    return apiClient.post<InternshipApplication>(`/api/internships/positions/${positionId}/apply`, data)
  },

  updateApplication: (id: string, data: any) => {
    return apiClient.put<InternshipApplication>(`/api/internships/applications/${id}`, data)
  },

  // 实习记录管理
  getRecords: (params?: {
    page?: number
    size?: number
    studentId?: string
    companyId?: string
    status?: string
  }) => {
    return apiClient.get<PageResult<InternshipRecord>>('/api/internships/records', { params })
  },

  getRecord: (id: string) => {
    return apiClient.get<InternshipRecord>(`/api/internships/records/${id}`)
  },

  createRecord: (data: any) => {
    return apiClient.post<InternshipRecord>('/api/internships/records', data)
  },

  updateRecord: (id: string, data: any) => {
    return apiClient.put<InternshipRecord>(`/api/internships/records/${id}`, data)
  },

  // 统计信息
  getStats: () => {
    return apiClient.get<InternshipStats>('/api/internships/stats')
  }
}
