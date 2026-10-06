import { defineStore } from 'pinia'
import { ref } from 'vue'
import { NotificationApi } from '../api/notification.api'
import type { NotificationDto } from '../types/notification'

const PAGE_SIZE = 20

export const useNotificationStore = defineStore('notifications', () => {
  const items = ref<NotificationDto[]>([])
  const unreadCount = ref(0)
  const loading = ref(false)
  const page = ref(0)
  const hasMore = ref(true)
  const error = ref<string | null>(null)

  async function fetch(reset = false): Promise<void> {
    if (reset) {
      page.value = 0
      items.value = []
      hasMore.value = true
      error.value = null
    }
    loading.value = true
    try {
      const data = await NotificationApi.list(page.value, PAGE_SIZE)
      items.value = reset ? data.items : [...items.value, ...data.items]
      hasMore.value = data.hasMore
      if (data.hasMore) page.value++
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to load notifications'
      console.error('[notifications] fetch failed', e)
    } finally {
      loading.value = false
    }
  }

  async function fetchUnreadCount(): Promise<void> {
    try {
      const data = await NotificationApi.unreadCount()
      unreadCount.value = data.count
    } catch (e: unknown) {
      console.error('[notifications] unreadCount failed', e)
    }
  }

  async function markRead(id: string): Promise<void> {
    const n = items.value.find((x) => x.id === id)
    if (!n || n.read) return
    n.read = true
    n.readAt = new Date().toISOString()
    unreadCount.value = Math.max(0, unreadCount.value - 1)
    try {
      await NotificationApi.markRead(id)
    } catch (e: unknown) {
      n.read = false
      n.readAt = null
      unreadCount.value++
      throw e
    }
  }

  async function markAllRead(): Promise<void> {
    const unreadIds = items.value.filter((n) => !n.read).map((n) => n.id)
    if (!unreadIds.length) return
    const prevCount = unreadCount.value
    items.value.forEach((n) => {
      if (!n.read) {
        n.read = true
        n.readAt = new Date().toISOString()
      }
    })
    unreadCount.value = 0
    try {
      await NotificationApi.markAllRead()
    } catch (e: unknown) {
      items.value
        .filter((n) => unreadIds.includes(n.id))
        .forEach((n) => {
          n.read = false
          n.readAt = null
        })
      unreadCount.value = prevCount
      throw e
    }
  }

  async function remove(id: string): Promise<void> {
    const idx = items.value.findIndex((n) => n.id === id)
    if (idx === -1) return
    const [removed] = items.value.splice(idx, 1)
    if (!removed.read) unreadCount.value = Math.max(0, unreadCount.value - 1)
    try {
      await NotificationApi.remove(id)
    } catch (e: unknown) {
      items.value.splice(idx, 0, removed)
      if (!removed.read) unreadCount.value++
      throw e
    }
  }

  function prepend(n: NotificationDto): void {
    if (items.value.some((x) => x.id === n.id)) return
    items.value.unshift(n)
    if (!n.read) unreadCount.value++
  }

  function reset(): void {
    items.value = []
    unreadCount.value = 0
    page.value = 0
    hasMore.value = true
    error.value = null
  }

  return {
    items,
    unreadCount,
    loading,
    hasMore,
    error,
    fetch,
    fetchUnreadCount,
    markRead,
    markAllRead,
    remove,
    prepend,
    reset,
  }
})