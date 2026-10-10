import axios, { type AxiosInstance } from 'axios'
import { useAuthStore } from '../stores/auth'
import type {
  NotificationDto,
  PageResponse,
  UnreadCountResponse,
  MarkAllReadResponse,
} from '../types/notification'

const notificationApi: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 15_000,
})

notificationApi.interceptors.request.use((config) => {
  const auth = useAuthStore()

  if (auth.token) {
    config.headers.set('Authorization', `Bearer ${auth.token}`)
  }

  const publicId = auth.user?.id
  if (publicId) {
    config.headers.set('X-User-PublicId', publicId)
  } else {
    console.warn('[notification.api] No auth.user.id — X-User-PublicId header omitted')
  }

  return config
})

notificationApi.interceptors.response.use(
  (r) => r,
  (err) => {
    if (err.response?.status === 401) {
      console.warn('[notification.api] 401 Unauthorized — token may be expired')
    }
    return Promise.reject(err)
  }
)

export const NotificationApi = {
  list(page: number, size: number): Promise<PageResponse<NotificationDto>> {
    return notificationApi
      .get<PageResponse<NotificationDto>>('/api/v1/notifications/inbox', {
        params: { page, size },
      })
      .then((r) => r.data)
  },

  unreadCount(): Promise<UnreadCountResponse> {
    return notificationApi
      .get<UnreadCountResponse>('/api/v1/notifications/inbox/unread-count')
      .then((r) => r.data)
  },

  markRead(id: string): Promise<void> {
    return notificationApi
      .patch<void>(`/api/v1/notifications/inbox/${id}/read`)
      .then(() => undefined)
  },

  markAllRead(): Promise<MarkAllReadResponse> {
    return notificationApi
      .post<MarkAllReadResponse>('/api/v1/notifications/inbox/read-all')
      .then((r) => r.data)
  },

  remove(id: string): Promise<void> {
    return notificationApi
      .delete<void>(`/api/v1/notifications/inbox/${id}`)
      .then(() => undefined)
  },
}

export default notificationApi
