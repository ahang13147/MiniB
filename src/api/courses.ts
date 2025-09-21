import axios from 'axios'

export interface Course {
  id: string
  code: string
  name: string
  description?: string
  teacherId: string
  teacherName: string
  department?: string
  semester?: string
  credits: number
  hours: number
  status: string
  createdAt: string
  updatedAt: string
  students: string[]
  studentCount: number
}

export interface PageResult<T> {
  items: T[]
  total: number
}

export interface CreateCourseRequest {
  code: string
  name: string
  description?: string
  teacherId: string
  department?: string
  semester?: string
  credits: number
  hours: number
}

export interface UpdateCourseRequest {
  name?: string
  description?: string
  teacherId?: string
  department?: string
  semester?: string
  credits?: number
  hours?: number
  status?: string
}

export interface AssignStudentRequest {
  studentId: string
}

export interface RemoveStudentRequest {
  studentId: string
}

export interface Teacher {
  id: string
  name: string
}

export async function listCourses(params: Partial<{
  page: number
  pageSize: number
  teacherId: string
  department: string
  semester: string
  status: string
  keyword: string
}>) {
  const { data } = await axios.get<PageResult<Course>>('/api/courses', { params })
  return data
}

export async function getCourse(id: string) {
  const { data } = await axios.get<Course>(`/api/courses/${id}`)
  return data
}

export async function createCourse(course: CreateCourseRequest) {
  const { data } = await axios.post<Course>('/api/courses', course)
  return data
}

export async function updateCourse(id: string, course: UpdateCourseRequest) {
  const { data } = await axios.put<Course>(`/api/courses/${id}`, course)
  return data
}

export async function deleteCourse(id: string) {
  await axios.delete(`/api/courses/${id}`)
}

export async function assignStudent(courseId: string, studentId: string) {
  await axios.post(`/api/courses/${courseId}/students`, { studentId })
}

export async function removeStudent(courseId: string, studentId: string) {
  await axios.delete(`/api/courses/${courseId}/students`, { data: { studentId } })
}

export async function getTeachers() {
  const { data } = await axios.get<Teacher[]>('/api/courses/teachers')
  return data
}

export async function getDepartments() {
  const { data } = await axios.get<string[]>('/api/courses/departments')
  return data
}

export async function getSemesters() {
  const { data } = await axios.get<string[]>('/api/courses/semesters')
  return data
}

