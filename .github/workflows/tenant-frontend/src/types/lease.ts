export type LeaseStatus = 'DRAFT' | 'PENDING' | 'ACTIVE' | 'EXPIRED' | 'TERMINATED'

export interface Lease {
  id: string
  propertyId: string
  tenantId: string
  landlordId: string
  monthlyRent: number
  rentAmount?: number
  startDate: string
  endDate: string
  status: LeaseStatus
}

export interface PaymentRecord {
  id: string
  leaseId: string
  amount: number
  method: 'MPESA' | 'BANK' | 'CARD'
  status: 'PAID' | 'PENDING'
  paidAt?: string
  dueDate: string
}

