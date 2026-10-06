import apiClient from './client'
import { invoiceApi } from './invoice.api'

export interface PaymentSplit {
  id: string
  paymentId: string
  invoiceId: string
  allocatedAmount: number
  createdAt: string
}

export interface PaymentSplitCreatePayload {
  paymentId: string
  invoiceId: string
  allocatedAmount: number
}

export interface PaginatedSplits {
  content: PaymentSplit[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export const paymentSplitApi = {
  /**
   * No list-all endpoint exists on the backend.
   * Aggregate splits per invoice across all invoices.
   */
  listAll: async (): Promise<PaymentSplit[]> => {
    const { data: invoicePage } = await invoiceApi.list({ size: 100 })
    const invoices = invoicePage.content

    const all: PaymentSplit[] = []
    for (const inv of invoices) {
      try {
        const { data } = await apiClient.get<PaginatedSplits>(
          `/payment-splits/by-invoice/${inv.id}`,
          { params: { size: 100 } },
        )
        all.push(...(data.content ?? []))
      } catch {
        // invoice has no splits
      }
    }
    all.sort((a, b) => (b.createdAt || '').localeCompare(a.createdAt || ''))
    return all
  },

  get: (id: string) => apiClient.get<PaymentSplit>(`/payment-splits/${id}`),

  byPayment: (paymentId: string) =>
    apiClient.get<PaymentSplit[]>(`/payment-splits/by-payment/${paymentId}`),

  byInvoicePaged: (invoiceId: string, page = 0, size = 100) =>
    apiClient.get<PaginatedSplits>(`/payment-splits/by-invoice/${invoiceId}`, {
      params: { page, size },
    }),

  create: (payload: PaymentSplitCreatePayload) =>
    apiClient.post<PaymentSplit>('/payment-splits', payload),

  remove: (id: string) => apiClient.delete(`/payment-splits/${id}`),
}