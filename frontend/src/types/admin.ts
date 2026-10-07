export interface OverviewDto {
  totalUsers: number
  activeUsers: number
  totalLandlords: number
  totalTenants: number
  activeLeases: number
  openTickets: number
  propertyStats: PropertyStatsDto
  paymentSummary: PaymentSummaryDto
  unreadNotifications: number
}

export interface PropertyStatsDto {
  totalProperties: number
  totalUnits: number
  occupiedUnits: number
  vacantUnits: number
  occupancyRate: number
}

export interface PaymentSummaryDto {
  totalCollected: number
  totalPending: number
  totalFailed: number
  paymentCount: number
  failedCount: number
}

export interface AdminUserDto {
  id: string
  email: string
  fullName: string
  role: 'TENANT' | 'LANDLORD' | 'ADMIN'
  active: boolean
  phone: string | null
  createdAt: string
  lastLoginAt: string | null
}

export interface PageResponse<T> {
  items: T[]
  total: number
  page: number
  size: number
  hasMore: boolean
}

export interface ServiceHealthDto {
  serviceName: string
  status: string
  version: string | null
  instanceId: string | null
  lastHeartbeat: string | null
}

export interface AuditLogDto {
  id: string
  actorEmail: string
  action: string
  resource: string
  resourceId: string | null
  ipAddress: string | null
  createdAt: string
}

export interface ApiEnvelope<T> {
  success: boolean
  data: T
  message: string | null
  timestamp: string
}