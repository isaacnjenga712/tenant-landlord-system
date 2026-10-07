import axios, { type AxiosInstance } from 'axios'
import { useAuthStore } from '../stores/auth'
import type {
  OverviewDto,
  AdminUserDto,
  PageResponse,
  ServiceHealthDto,
  AuditLogDto,
  ApiEnvelope,
} from '../types/admin'

const adminApi: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 15_000,
})

adminApi.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (auth.token) config.headers.set('Authorization', `Bearer ${auth.token}`)
  return config
})

/**
 * Unwrap the {success, data, message, timestamp} envelope.
 * Throws when success=false so callers get a normal Error.
 */
async function unwrap<T>(p: Promise<{ data: ApiEnvelope<T> }>): Promise<T> {
  const { data } = await p
  if (!data.success) throw new Error(data.message ?? 'Request failed')
  return data.data
}

export const AdminApi = {
  overview(): Promise<OverviewDto> {
    return unwrap(adminApi.get('/api/v1/admin/overview'))
  },

  listUsers(params: {
    page?: number
    size?: number
    role?: string
    status?: string
    search?: string
  }): Promise<PageResponse<AdminUserDto>> {
    return unwrap(adminApi.get('/api/v1/admin/users', { params }))
  },

  getUser(id: string): Promise<AdminUserDto> {
    return unwrap(adminApi.get(`/api/v1/admin/users/${id}`))
  },

  updateRole(id: string, role: string): Promise<void> {
    return unwrap(adminApi.patch(`/api/v1/admin/users/${id}/role`, { role }))
  },

  updateStatus(id: string, active: boolean): Promise<void> {
    return unwrap(adminApi.patch(`/api/v1/admin/users/${id}/status`, { active }))
  },

  resetPassword(id: string): Promise<void> {
    return unwrap(adminApi.post(`/api/v1/admin/users/${id}/reset-password`))
  },

  services(): Promise<ServiceHealthDto[]> {
    return unwrap(adminApi.get('/api/v1/admin/system/services'))
  },

  auditLogs(params: {
    page?: number
    size?: number
    actor?: string
    action?: string
  }): Promise<PageResponse<AuditLogDto>> {
    return unwrap(adminApi.get('/api/v1/admin/audit-logs', { params }))
  },
}

export default adminApi