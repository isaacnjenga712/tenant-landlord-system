import apiClient from './client'

export type PaymentStatus = 'PAID' | 'COMPLETED' | 'PENDING' | 'FAILED'

export interface Payment {
  id: string
  amount: number
  method: 'MPESA' | 'BANK' | 'CARD'
  status: PaymentStatus
  createdAt: string
  dueDate?: string
}

export const paymentApi = {
  createMpesaRequest: (payload: { phone: string; amount: number; leaseId: string }) =>
    apiClient.post('/payments/mpesa', payload),
  listHistory: () => apiClient.get<Payment[]>('/payments/history'),
  payRent: (payload: { leaseId: string; amount: number; method: 'MPESA' | 'BANK' | 'CARD' }) =>
    apiClient.post('/payments/rent', payload),
}
