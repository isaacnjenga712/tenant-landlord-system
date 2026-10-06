import { computed, type ComputedRef } from 'vue'
import { useAuthStore } from '../stores/auth'
import type { NotificationDto } from '../types/notification'
import type { UserRole } from '../types/auth'

export type NotificationCategory =
  | 'leases'
  | 'payments'
  | 'maintenance'
  | 'account'
  | 'other'

/** Which categories each role cares about. Order = tab order. */
const ROLE_CATEGORIES: Record<UserRole, NotificationCategory[]> = {
  TENANT: ['leases', 'payments', 'maintenance', 'account', 'other'],
  LANDLORD: ['maintenance', 'payments', 'leases', 'account', 'other'],
  ADMIN: ['account', 'leases', 'payments', 'maintenance', 'other'],
}

const CATEGORY_LABELS: Record<NotificationCategory, string> = {
  leases: 'Leases',
  payments: 'Payments',
  maintenance: 'Maintenance',
  account: 'Account',
  other: 'Other',
}

/** Map backend eventType to a category. Extend as you add events. */
export function categorizeEventType(eventType: string): NotificationCategory {
  switch (eventType) {
    case 'LEASE_CREATED':
    case 'LEASE_APPROVED':
    case 'LEASE_TERMINATED':
      return 'leases'

    case 'INVOICE_CREATED':
    case 'PAYMENT_RECEIVED_MPESA':
    case 'PAYMENT_FAILED':
      return 'payments'

    case 'MAINTENANCE_CREATED':
    case 'MAINTENANCE_RESOLVED':
      return 'maintenance'

    case 'USER_REGISTERED':
    case 'USER_LOGIN':
      return 'account'

    default:
      return 'other'
  }
}

export function useNotificationCategories() {
  const auth = useAuthStore()

  const role: ComputedRef<UserRole> = computed(
    () => (auth.role as UserRole) ?? 'TENANT'
  )

  const categories: ComputedRef<NotificationCategory[]> = computed(
    () => ROLE_CATEGORIES[role.value] ?? ROLE_CATEGORIES.TENANT
  )

  const labels = CATEGORY_LABELS

  /** Filter a list of notifications to only those relevant to the current role. */
  function relevantTo(n: NotificationDto): boolean {
    return categories.value.includes(categorizeEventType(n.eventType))
  }

  return { role, categories, labels, relevantTo, categorizeEventType }
}