import apiClient from './client'

export type LineItemCategory = 'rent' | 'utility' | 'fee' | 'deposit'

export interface InvoiceLineItem {
  id: string
  invoiceId: string
  description: string
  category: LineItemCategory
  quantity: number
  unitPrice: number
  total: number
  taxRate?: number
  createdAt: string
  updatedAt: string
}

export interface LineItemCreatePayload {
  invoiceId: string
  description: string
  category: LineItemCategory
  quantity: number
  unitPrice: number
  taxRate?: number
}

export const invoiceLineItemApi = {
  create: (payload: LineItemCreatePayload) =>
    apiClient.post<InvoiceLineItem>('/invoice-line-items', payload),

  listByInvoice: (invoiceId: string) =>
    apiClient.get<InvoiceLineItem[]>(`/invoice-line-items/by-invoice/${invoiceId}`),

  get: (id: string) => apiClient.get<InvoiceLineItem>(`/invoice-line-items/${id}`),

  update: (id: string, payload: Partial<LineItemCreatePayload>) =>
    apiClient.patch<InvoiceLineItem>(`/invoice-line-items/${id}`, payload),

  remove: (id: string) => apiClient.delete(`/invoice-line-items/${id}`),

  removeAllByInvoice: (invoiceId: string) =>
    apiClient.delete(`/invoice-line-items/by-invoice/${invoiceId}`),
}