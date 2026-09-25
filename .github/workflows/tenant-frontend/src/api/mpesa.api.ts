import apiClient from './client'

export interface StkPushRequest {
  phone: string
  amount: number
  leaseId: string
}

export interface StkPushResponse {
  checkoutRequestId: string
  merchantRequestId: string
  responseCode: string
  responseDescription: string
}

export interface MpesaTransaction {
  id: string
  transactionId: string
  amount: number
  phone: string
  status: 'PENDING' | 'SUCCESS' | 'FAILED'
  leaseId?: string
  tenantId?: string
  createdAt: string
}

export const mpesaApi = {
  stkPush: (payload: StkPushRequest) =>
    apiClient.post<StkPushResponse>('/mpesa/stk-push', payload),

  queryStatus: (checkoutRequestId: string) =>
    apiClient.post('/mpesa/query-status', { checkoutRequestId }),

  getTransaction: (id: string) =>
    apiClient.get<MpesaTransaction>(`/mpesa/transactions/${id}`),

  byTenant: (tenantId: string) =>
    apiClient.get<MpesaTransaction[]>(`/mpesa/transactions/tenant/${tenantId}`),

  byLease: (leaseId: string) =>
    apiClient.get<MpesaTransaction[]>(`/mpesa/transactions/lease/${leaseId}`),
}