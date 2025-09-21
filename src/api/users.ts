import axios from 'axios'
import type { Role } from '@/stores/auth'

export interface User {
  id: string
  username: string
  displayName: string
  email?: string
  phone?: string
  role: Role
  department?: string
  position?: string
  enabled: boolean
  createdAt: string
  lastLoginAt?: string
}

export interface PageResult<T> {
  items: T[]
  total: number
}

export interface CreateUserRequest {
  username: string
  password: string
  displayName?: string
  email?: string
  phone?: string
  role: Role
  department?: string
  position?: string
}

export interface UpdateUserRequest {
  displayName?: string
  email?: string
  phone?: string
  role?: Role
  department?: string
  position?: string
  enabled?: boolean
}

export async function listUsers(params: Partial<{
  page: number
  pageSize: number
  role: Role
  department: string
  enabled: boolean
  keyword: string
}>) {
  const { data } = await axios.get<PageResult<User>>('/api/users', { params })
  return data
}

export async function getUser(id: string) {
  const { data } = await axios.get<User>(`/api/users/${id}`)
  return data
}

export async function createUser(user: CreateUserRequest) {
  const { data } = await axios.post<User>('/api/users', user)
  return data
}

export async function updateUser(id: string, user: UpdateUserRequest) {
  const { data } = await axios.put<User>(`/api/users/${id}`, user)
  return data
}

export async function deleteUser(id: string) {
  await axios.delete(`/api/users/${id}`)
}

export async function getDepartments() {
  const { data } = await axios.get<string[]>('/api/users/departments')
  return data
}

