import { onUnmounted, watch, type WatchStopHandle } from 'vue'
import SockJS from 'sockjs-client'
import { Client, type IMessage, type StompSubscription } from '@stomp/stompjs'
import { useAuthStore } from '../stores/auth'
import { useNotificationStore } from '../stores/notifications'   // plural = inbox
import { useNotificationStore as useToastStore } from '../stores/notification'                  // see note below
import type { NotificationDto } from '../types/notification'

interface Options {
  onNotification?: (n: NotificationDto) => void
  onConnected?: () => void
  onDisconnected?: () => void
}

export function useNotificationSocket(opts: Options = {}) {
  const auth = useAuthStore()
  const inbox = useNotificationStore()   // plural store → has .prepend()
  const toast = useToastStore()          // toast store → has .show()

  let client: Client | null = null
  let subscription: StompSubscription | null = null
  let stopAuthWatch: WatchStopHandle | null = null

  function connect(): void {
    const publicId = auth.user?.id
    if (!publicId || !auth.token) return

    client = new Client({
      webSocketFactory: () =>
        new SockJS(`${import.meta.env.VITE_API_URL}/ws/notifications`),
      reconnectDelay: 5_000,
      heartbeatIncoming: 10_000,
      heartbeatOutgoing: 10_000,
      connectHeaders: {
        Authorization: `Bearer ${auth.token}`,
      },
      debug: import.meta.env.DEV
        ? (msg: string) => console.debug('[STOMP]', msg)
        : () => {},
      onConnect: () => {
        opts.onConnected?.()

        subscription = client!.subscribe(
          '/user/queue/notifications',
          (message: IMessage) => {
            try {
              const dto = JSON.parse(message.body) as NotificationDto

              // 1. Push into inbox (plural store)
              inbox.prepend(dto)

              // 2. Show a toast (toast store)
              const text = dto.subject ?? dto.body ?? 'You have a new notification'
              toast.show(text, 'info')

              opts.onNotification?.(dto)
            } catch (e: unknown) {
              console.error('[STOMP] Failed to parse payload:', message.body, e)
            }
          }
        )
      },
      onDisconnect: () => {
        opts.onDisconnected?.()
      },
      onStompError: (frame: { headers: { message?: string }; body?: string }) => {
        console.error('[STOMP] Broker error:', frame.headers.message, frame.body)
      },
      onWebSocketError: (event: Event) => {
        console.warn('[STOMP] WebSocket error', event)
      },
    })

    client.activate()
  }

  function disconnect(): void {
    subscription?.unsubscribe()
    subscription = null
    void client?.deactivate()
    client = null
  }

  stopAuthWatch = watch(
    () => auth.user?.id,
    (newId, oldId) => {
      if (newId === oldId) return
      disconnect()
      if (newId) connect()
    },
    { immediate: true }
  )

  onUnmounted(() => {
    stopAuthWatch?.()
    disconnect()
  })

  return {
    connect,
    disconnect,
    isConnected: () => client?.connected ?? false,
  }
}