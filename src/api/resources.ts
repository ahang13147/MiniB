import axios from 'axios'

export type Ownership = 'ENTERPRISE' | 'SCHOOL'
export type BorrowStatus = 'AVAILABLE' | 'BORROWED'
export type ResearchResourceType = 'LAB_EQUIPMENT' | 'RESEARCH_DATA' | 'TECHNICAL_DOC'
export type CourseResourceType = 'QUALITY_COURSE' | 'TEACHING_CASE' | 'COURSEWARE' | 'OTHER'

export interface PageResult<T> { items: T[]; total: number }

export interface ResearchResource {
  id: string
  type: ResearchResourceType
  name: string
  description?: string
  ownership: Ownership
  status: BorrowStatus
  publisher: string
  borrower?: string
  location?: string
}

export interface CourseResource {
  id: string
  type: CourseResourceType
  name: string
  description?: string
  ownership: Ownership
  publisher: string
}

export async function listResearch(params: Partial<{ page: number; pageSize: number; type: ResearchResourceType; ownership: Ownership; status: BorrowStatus; keyword: string }>) {
  const { data } = await axios.get<PageResult<ResearchResource>>('/api/resources/research', { params })
  return data
}

export async function borrowResearch(id: string, borrower: string) {
  const { data } = await axios.post<ResearchResource>('/api/resources/research/borrow', { id, borrower })
  return data
}

export async function returnResearch(id: string) {
  const { data } = await axios.post<ResearchResource>('/api/resources/research/return', { id })
  return data
}

export async function listCourses(params: Partial<{ page: number; pageSize: number; type: CourseResourceType; ownership: Ownership; keyword: string }>) {
  const { data } = await axios.get<PageResult<CourseResource>>('/api/resources/courses', { params })
  return data
}


