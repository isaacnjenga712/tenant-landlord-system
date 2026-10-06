export interface NotificationDto {
  id: string
  recipientPublicId: string
  eventType: string
  subject: string | null
  body: string | null
  read: boolean
  createdAt: string   // ISO LocalDateTime from Jackson
  readAt: string | null
}

export interface PageResponse<T> {
  items: T[]
  total: number
  page: number
  size: number
  hasMore: boolean
}

export interface UnreadCountResponse {
  count: number
}

export interface MarkAllReadResponse {
  updated: number
}

export type NotificationFilter = 'all' | 'unread'