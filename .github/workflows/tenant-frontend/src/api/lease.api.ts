import apiClient from './client'
import type { Lease, PaginatedLeases } from '../types/lease'

export const leaseApi = {
  list: (page = 0, size = 20) =>
    apiClient.get<PaginatedLeases>('/leases', { params: { page, size } }),

  get: (id: string) => apiClient.get<Lease>(`/leases/${id}`),

  create: (payload: Partial<Lease>) => apiClient.post<Lease>('/leases', payload),

  update: (id: string, payload: Partial<Lease>) =>
    apiClient.put<Lease>(`/leases/${id}`, payload),

  remove: (id: string) => apiClient.delete(`/leases/${id}`),

  terminate: (id: string) => apiClient.post<Lease>(`/leases/${id}/terminate`),

  renew: (id: string) => apiClient.post<Lease>(`/leases/${id}/renew`),
}