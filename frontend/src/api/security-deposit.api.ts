import apiClient from './client'

export type DepositStatus = 'ACTIVE' | 'RETURNED' | 'PARTIALLY_DEDUCTED'

export interface SecurityDeposit {
  id: string
  leaseId: string
  totalDeposit: number
  heldInAccountId?: string
  currentBalance: number
  interestAccrued?: number
  deductionTotal?: number
  returnedDate?: string
  returnAmount?: number
  status: DepositStatus
  createdAt: string
  updatedAt: string
}

export interface DepositCreatePayload {
  leaseId: string
  totalDeposit: number
  heldInAccountId?: string
}

export interface DeductionPayload {
  amount: number
  description: string
}

export interface ReturnPayload {
  returnAmount: number
  returnedDate?: string
}

export interface PaginatedDeposits {
  content: SecurityDeposit[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export const securityDepositApi = {
  list: (params?: { leaseId?: string; activeOnly?: boolean; page?: number; size?: number }) =>
    apiClient.get<PaginatedDeposits>('/security-deposits', { params }),

  listAll: async (params?: { leaseId?: string; activeOnly?: boolean }) => {
    const { data } = await securityDepositApi.list({ ...params, size: 100 })
    return data.content
  },

  get: (id: string) => apiClient.get<SecurityDeposit>(`/security-deposits/${id}`),

  byLease: (leaseId: string) =>
    apiClient.get<SecurityDeposit>(`/security-deposits/by-lease/${leaseId}`),

  create: (payload: DepositCreatePayload) =>
    apiClient.post<SecurityDeposit>('/security-deposits', payload),

  addDeduction: (id: string, payload: DeductionPayload) =>
    apiClient.patch<SecurityDeposit>(`/security-deposits/${id}/deduction`, payload),

  returnDeposit: (id: string, payload: ReturnPayload) =>
    apiClient.patch<SecurityDeposit>(`/security-deposits/${id}/return`, payload),

  remove: (id: string) => apiClient.delete(`/security-deposits/${id}`),
}