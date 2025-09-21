import axios from 'axios'

export interface Student {
  id: string
  username: string
  displayName: string
  email?: string
  phone?: string
  studentId: string
  department?: string
  major?: string
  grade?: string
  class_?: string
  status: string
  enrollmentDate: string
  createdAt: string
  lastLoginAt?: string
  courses: string[]
  courseCount: number
  grades: Grade[]
  gpa: number
  projects: string[]
  projectCount: number
}

export interface Grade {
  courseId: string
  courseName: string
  courseCode: string
  credits: number
  score: string
  grade: string
  semester?: string
  examDate: string
}

export interface PageResult<T> {
  items: T[]
  total: number
}

export interface CreateStudentRequest {
  username: string
  password: string
  displayName?: string
  email?: string
  phone?: string
  studentId: string
  department?: string
  major?: string
  grade?: string
  class_?: string
}

export interface UpdateStudentRequest {
  displayName?: string
  email?: string
  phone?: string
  department?: string
  major?: string
  grade?: string
  class_?: string
  status?: string
}

export interface AddGradeRequest {
  courseId: string
  courseName: string
  courseCode: string
  credits: number
  score: string
  grade: string
  semester?: string
}

export interface UpdateGradeRequest {
  score?: string
  grade?: string
}

export async function listStudents(params: Partial<{
  page: number
  pageSize: number
  department: string
  major: string
  grade: string
  status: string
  keyword: string
}>) {
  const { data } = await axios.get<PageResult<Student>>('/api/students', { params })
  return data
}

export async function getStudent(id: string) {
  const { data } = await axios.get<Student>(`/api/students/${id}`)
  return data
}

export async function getStudentGrades(id: string) {
  const { data } = await axios.get<Grade[]>(`/api/students/${id}/grades`)
  return data
}

export async function addGrade(studentId: string, grade: AddGradeRequest) {
  await axios.post(`/api/students/${studentId}/grades`, grade)
}

export async function updateGrade(studentId: string, gradeId: string, grade: UpdateGradeRequest) {
  await axios.put(`/api/students/${studentId}/grades/${gradeId}`, grade)
}

export async function deleteGrade(studentId: string, gradeId: string) {
  await axios.delete(`/api/students/${studentId}/grades/${gradeId}`)
}

export async function getDepartments() {
  const { data } = await axios.get<string[]>('/api/students/departments')
  return data
}

export async function getMajors() {
  const { data } = await axios.get<string[]>('/api/students/majors')
  return data
}

export async function getGrades() {
  const { data } = await axios.get<string[]>('/api/students/grades')
  return data
}

