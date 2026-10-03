import apiClient from './client'
import type { Lease, PaginatedLeases } from '../types/lease'

export interface CreateLeasePayload {
  propertyId: string
  tenantId: string
  landlordId: string
  startDate: string   // yyyy-MM-dd
  endDate: string     // yyyy-MM-dd
  rentAmount: number
  depositAmount?: number
  termsAndConditions?: string
}

export const leaseApi = {
  list: (page = 0, size = 20) =>
    apiClient.get<PaginatedLeases>('/leases', { params: { page, size } }),

  get: (id: string) => apiClient.get<Lease>(`/leases/${id}`),

  create: (payload: CreateLeasePayload) =>
    apiClient.post<Lease>('/leases', payload),

  update: (id: string, payload: Partial<Lease>) =>
    apiClient.put<Lease>(`/leases/${id}`, payload),

  remove: (id: string) => apiClient.delete(`/leases/${id}`),

  terminate: (id: string, terminationDate: string) =>
    apiClient.post<Lease>(`/leases/${id}/terminate`, null, {
      params: { terminationDate },
    }),

  renew: (id: string, newEndDate: string) =>
    apiClient.post<Lease>(`/leases/${id}/renew`, null, {
      params: { newEndDate },
    }),

  approve: (id: string) => apiClient.post<Lease>(`/leases/${id}/approve`),
}