import { defineStore } from 'pinia'
import type { Lease, PaymentRecord } from '../types/lease'

export const useLeaseStore = defineStore('lease', {
  state: () => ({
    leases: [] as Lease[],
    paymentHistory: [] as PaymentRecord[],
  }),
  actions: {
    setLeases(leases: Lease[]) {
      this.leases = leases
    },
    setPayments(payments: PaymentRecord[]) {
      this.paymentHistory = payments
    },
    applyForLease(payload: Partial<Lease> & { propertyId: string | number; startDate?: string; endDate?: string }) {
      const lease: Lease = {
        id: `lease-${Date.now()}`,
        propertyId: String(payload.propertyId),
        tenantId: 'tenant-current',
        landlordId: 'landlord-current',
        monthlyRent: 35000,
        rentAmount: 35000,
        startDate: payload.startDate || new Date().toISOString().split('T')[0],
        endDate: payload.endDate || new Date(Date.now() + 365 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
        status: 'PENDING',
      }
      this.leases.unshift(lease)
      return lease
    },
  },
})
