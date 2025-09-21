import { apiClient } from './client'

export interface DualMentorCourse {
  id: string
  courseCode: string
  courseName: string
  description?: string
  universityId: string
  universityName: string
  companyId: string
  companyName: string
  universityTeacherId: string
  universityTeacherName: string
  enterpriseMentorId: string
  enterpriseMentorName: string
  department?: string
  majorId?: string
  majorName?: string
  semester?: string
  academicYear?: string
  credits: number
  totalHours: number
  theoryHours?: number
  practiceHours?: number
  maxStudents: number
  enrolledStudents: number
  courseType: 'THEORY' | 'PRACTICE' | 'MIXED'
  teachingMode: 'ONLINE' | 'OFFLINE' | 'HYBRID'
  status: 'PLANNING' | 'RECRUITING' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'
  startDate?: string
  endDate?: string
  createdAt: string
  updatedAt: string
}

export interface CourseEnrollment {
  id: string
  courseId: string
  courseName: string
  studentId: string
  studentName: string
  studentMajor: string
  enrollmentDate: string
  status: 'ENROLLED' | 'COMPLETED' | 'DROPPED' | 'FAILED'
  finalScore?: number
  finalGrade?: string
  theoryScore?: number
  practiceScore?: number
  attendanceRate?: number
  createdAt: string
  updatedAt: string
}

export interface EnterpriseMentor {
  id: string
  userId: string
  name: string
  email: string
  phone: string
  companyId: string
  companyName: string
  position: string
  department?: string
  workYears?: number
  expertise?: string
  education?: string
  certifications?: string
  introduction?: string
  status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED'
  createdAt: string
  updatedAt: string
  courses?: string[]
  courseCount: number
  students?: string[]
  studentCount: number
}

export interface MentorStats {
  totalCourses: number
  activeCourses: number
  completedCourses: number
  totalMentors: number
  activeMentors: number
  totalEnrollments: number
  completedEnrollments: number
  averageScore: number
}

export interface CreateDualCourseRequest {
  courseCode: string
  courseName: string
  description?: string
  universityId: string
  companyId: string
  universityTeacherId: string
  enterpriseMentorId: string
  department?: string
  majorId?: string
  semester?: string
  academicYear?: string
  credits?: number
  totalHours?: number
  theoryHours?: number
  practiceHours?: number
  maxStudents?: number
  courseType: string
  teachingMode: string
  startDate?: string
  endDate?: string
}

export interface PageResult<T> {
  items: T[]
  total: number
  page: number
  size: number
  totalPages: number
}

export const mentorApi = {
  // 双导师课堂管理
  getCourses: (params?: {
    page?: number
    size?: number
    search?: string
    universityId?: string
    companyId?: string
    status?: string
    mode?: string
  }) => {
    return apiClient.get<PageResult<DualMentorCourse>>('/api/mentors/courses', { params })
  },

  getCourse: (id: string) => {
    return apiClient.get<DualMentorCourse>(`/api/mentors/courses/${id}`)
  },

  createCourse: (data: CreateDualCourseRequest) => {
    return apiClient.post<DualMentorCourse>('/api/mentors/courses', data)
  },

  updateCourse: (id: string, data: Partial<CreateDualCourseRequest>) => {
    return apiClient.put<DualMentorCourse>(`/api/mentors/courses/${id}`, data)
  },

  deleteCourse: (id: string) => {
    return apiClient.delete(`/api/mentors/courses/${id}`)
  },

  // 选课管理
  getEnrollments: (params?: {
    page?: number
    size?: number
    courseId?: string
    studentId?: string
    status?: string
  }) => {
    return apiClient.get<PageResult<CourseEnrollment>>('/api/mentors/enrollments', { params })
  },

  enrollCourse: (data: { courseId: string; studentId: string }) => {
    return apiClient.post<CourseEnrollment>('/api/mentors/enroll', data)
  },

  updateEnrollment: (id: string, data: any) => {
    return apiClient.put<CourseEnrollment>(`/api/mentors/enrollments/${id}`, data)
  },

  // 企业导师管理
  getMentors: (params?: {
    page?: number
    size?: number
    search?: string
    companyId?: string
    status?: string
  }) => {
    return apiClient.get<PageResult<EnterpriseMentor>>('/api/mentors/enterprise-mentors', { params })
  },

  getMentor: (id: string) => {
    return apiClient.get<EnterpriseMentor>(`/api/mentors/enterprise-mentors/${id}`)
  },

  registerMentor: (data: any) => {
    return apiClient.post<EnterpriseMentor>('/api/mentors/enterprise-mentors', data)
  },

  updateMentor: (id: string, data: any) => {
    return apiClient.put<EnterpriseMentor>(`/api/mentors/enterprise-mentors/${id}`, data)
  },

  // 统计信息
  getStats: () => {
    return apiClient.get<MentorStats>('/api/mentors/stats')
  }
}
