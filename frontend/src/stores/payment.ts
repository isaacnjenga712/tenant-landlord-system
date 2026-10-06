import { defineStore } from 'pinia'

export interface PaymentSummary {
  id: string
  tenant: string
  amount: number
  status: 'PAID' | 'PENDING'
  method: 'MPESA' | 'BANK' | 'CARD'
  dueDate: string
}

export const usePaymentStore = defineStore('payment', {
  state: () => ({
    summary: [] as PaymentSummary[],
  }),
  actions: {
    setSummary(records: PaymentSummary[]) {
      this.summary = records
    },
  },
})
