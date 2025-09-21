import axios from 'axios'

export interface Company {
  id: string
  name: string
  description?: string
  industry?: string
  scale?: string
  website?: string
  email?: string
  phone?: string
  address?: string
  contactPerson?: string
  contactTitle?: string
  status: string
  createdAt: string
  updatedAt: string
  mentors: string[]
  mentorCount: number
  projects: string[]
  projectCount: number
}

export interface PageResult<T> {
  items: T[]
  total: number
}

export interface CreateCompanyRequest {
  name: string
  description?: string
  industry?: string
  scale?: string
  website?: string
  email?: string
  phone?: string
  address?: string
  contactPerson?: string
  contactTitle?: string
}

export interface UpdateCompanyRequest {
  name?: string
  description?: string
  industry?: string
  scale?: string
  website?: string
  email?: string
  phone?: string
  address?: string
  contactPerson?: string
  contactTitle?: string
  status?: string
}

export interface AssignMentorRequest {
  mentorId: string
}

export interface RemoveMentorRequest {
  mentorId: string
}

export async function listCompanies(params: Partial<{
  page: number
  pageSize: number
  industry: string
  scale: string
  status: string
  keyword: string
}>) {
  const { data } = await axios.get<PageResult<Company>>('/api/companies', { params })
  return data
}

export async function getCompany(id: string) {
  const { data } = await axios.get<Company>(`/api/companies/${id}`)
  return data
}

export async function createCompany(company: CreateCompanyRequest) {
  const { data } = await axios.post<Company>('/api/companies', company)
  return data
}

export async function updateCompany(id: string, company: UpdateCompanyRequest) {
  const { data } = await axios.put<Company>(`/api/companies/${id}`, company)
  return data
}

export async function deleteCompany(id: string) {
  await axios.delete(`/api/companies/${id}`)
}

export async function assignMentor(companyId: string, mentorId: string) {
  await axios.post(`/api/companies/${companyId}/mentors`, { mentorId })
}

export async function removeMentor(companyId: string, mentorId: string) {
  await axios.delete(`/api/companies/${companyId}/mentors`, { data: { mentorId } })
}

export async function getIndustries() {
  const { data } = await axios.get<string[]>('/api/companies/industries')
  return data
}

export async function getScales() {
  const { data } = await axios.get<string[]>('/api/companies/scales')
  return data
}

