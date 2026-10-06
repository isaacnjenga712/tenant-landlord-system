import apiClient from './client'

export type InvoiceStatus = 'pending' | 'partial' | 'paid' | 'overdue' | 'voided'

export interface Invoice {
  id: string
  leaseId: string
  invoiceNumber: string
  periodStart: string
  periodEnd: string
  dueDate: string
  totalAmount: number
  paidAmount: number
  status: InvoiceStatus
  lateFeeApplied?: boolean
  gracePeriodDays?: number
  metadata?: string
  createdAt: string | null
  updatedAt: string | null
}

export interface InvoiceCreatePayload {
  leaseId: string
  invoiceNumber: string
  periodStart: string
  periodEnd: string
  dueDate: string
  totalAmount: number
  paidAmount?: number
  gracePeriodDays?: number
  metadata?: string
}

export interface PaginatedInvoices {
  content: Invoice[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export function generateInvoiceNumber(): string {
  const d = new Date()
  const ym = `${d.getFullYear()}${String(d.getMonth() + 1).padStart(2, '0')}`
  const rand = Math.random().toString(36).substring(2, 8).toUpperCase()
  return `INV-${ym}-${rand}`
}

export const invoiceApi = {
  list: (params?: { leaseId?: string; status?: InvoiceStatus; page?: number; size?: number }) =>
    apiClient.get<PaginatedInvoices>('/invoices', { params }),

  listAll: async (params?: { leaseId?: string; status?: InvoiceStatus }) => {
    const { data } = await invoiceApi.list({ ...params, size: 100 })
    return data.content
  },

  get: (id: string) => apiClient.get<Invoice>(`/invoices/${id}`),

  create: (payload: InvoiceCreatePayload) =>
    apiClient.post<Invoice>('/invoices', payload),

  update: (id: string, payload: Partial<InvoiceCreatePayload>) =>
    apiClient.patch<Invoice>(`/invoices/${id}`, payload),

  pay: (id: string, amount: number) =>
    apiClient.patch<Invoice>(`/invoices/${id}/pay`, null, { params: { amount } }),

  void: (id: string) => apiClient.patch(`/invoices/${id}/void`),
}