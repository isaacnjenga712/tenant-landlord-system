import apiClient from './client'

export type PaymentStatus = 'PAID' | 'COMPLETED' | 'PENDING' | 'FAILED'

export interface Payment {
  id: string
  leaseId?: string
  tenantId?: string
  amount: number
  method: 'MPESA' | 'BANK' | 'CARD'
  status: PaymentStatus
  createdAt: string
  dueDate?: string
}

export interface PaginatedPayments {
  content: Payment[]
  pageable: any
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export const paymentApi = {
  list: (page = 0, size = 20) =>
    apiClient.get<PaginatedPayments>('/payments', { params: { page, size } }),

  listAll: async (page = 0, size = 20): Promise<Payment[]> => {
    const { data } = await paymentApi.list(page, size)
    return data.content
  },

  get: (id: string) => apiClient.get<Payment>(`/payments/${id}`),

  create: (payload: Partial<Payment>) =>
    apiClient.post<Payment>('/payments', payload),

  remove: (id: string) => apiClient.delete(`/payments/${id}`),
}