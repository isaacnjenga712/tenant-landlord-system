import apiClient from './client'
import type { Lease, PaymentRecord } from '../types/lease'

export const leaseApi = {
  list: () => apiClient.get<Lease[]>('/leases'),
  apply: (payload: Partial<Lease>) => apiClient.post<Lease>('/leases/apply', payload),
  approve: (id: string) => apiClient.patch<Lease>(`/leases/${id}/approve`),
  getPayments: (leaseId: string) => apiClient.get<PaymentRecord[]>(`/leases/${leaseId}/payments`),
}
