<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useNotificationStore } from '../stores/notifications'
import {
  useNotificationCategories,
  categorizeEventType,
  type NotificationCategory,
} from '../composables/useNotificationCategories'
import type { NotificationDto, NotificationFilter } from '../types/notification'

const store = useNotificationStore()
const { items, unreadCount, loading, hasMore, error } = storeToRefs(store)

const { role, categories, labels, relevantTo } = useNotificationCategories()

const filter = ref<NotificationFilter>('all')
const category = ref<NotificationCategory | 'all'>('all')

/** Apply role filter first, then unread, then category. */
const filtered = computed<NotificationDto[]>(() => {
  let list = items.value.filter(relevantTo)

  if (filter.value === 'unread') {
    list = list.filter((n) => !n.read)
  }
  if (category.value !== 'all') {
    list = list.filter((n) => categorizeEventType(n.eventType) === category.value)
  }
  return list
})

/** Hide category tabs with zero items so the UI stays clean. */
const visibleCategories = computed(() =>
  categories.value.filter((c) =>
    items.value.some((n) => relevantTo(n) && categorizeEventType(n.eventType) === c)
  )
)

/** Role-specific empty state copy. */
const emptyMessage = computed(() =>
  role.value === 'LANDLORD'
    ? 'No landlord notifications yet.'
    : 'No tenant notifications yet.'
)

onMounted(() => {
  void store.fetch(true)
  void store.fetchUnreadCount()
})

function timeAgo(iso: string): string {
  // Backend sends LocalDateTime without a timezone — treat as UTC
  const normalized =
    iso.endsWith('Z') || /[+-]\d{2}:\d{2}$/.test(iso) ? iso : `${iso}Z`

  const seconds = Math.floor((Date.now() - new Date(normalized).getTime()) / 1000)
  if (seconds < 0) return 'just now'
  if (seconds < 60) return `${seconds}s ago`
  if (seconds < 3600) return `${Math.floor(seconds / 60)}m ago`
  if (seconds < 86400) return `${Math.floor(seconds / 3600)}h ago`
  return `${Math.floor(seconds / 86400)}d ago`
}

function onMarkRead(id: string): void {
  void store.markRead(id).catch((e: unknown) => console.error('markRead failed', e))
}

function onRemove(id: string): void {
  void store.remove(id).catch((e: unknown) => console.error('remove failed', e))
}

function onMarkAllRead(): void {
  void store.markAllRead().catch((e: unknown) => console.error('markAllRead failed', e))
}

function onLoadMore(): void {
  void store.fetch().catch((e: unknown) => console.error('fetch failed', e))
}
</script>

<template>
  <div class="mx-auto max-w-3xl p-6">
    <header class="mb-6 flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-semibold text-slate-900">Notifications</h1>
        <p class="text-xs uppercase tracking-wide text-slate-500">
          {{ role }} view
        </p>
      </div>
      <span
        v-if="unreadCount"
        class="rounded-full bg-indigo-100 px-3 py-1 text-sm text-indigo-700"
      >
        {{ unreadCount }} unread
      </span>
    </header>

    <!-- Row 1: All / Unread toggle -->
    <div class="mb-3 flex items-center gap-2">
      <button
        v-for="f in (['all', 'unread'] as const)"
        :key="f"
        class="rounded px-3 py-1.5 text-sm capitalize transition"
        :class="filter === f
          ? 'bg-slate-900 text-white'
          : 'bg-slate-100 text-slate-700 hover:bg-slate-200'"
        @click="filter = f"
      >
        {{ f }}
      </button>

      <button
        class="ml-auto rounded border px-3 py-1.5 text-sm transition hover:bg-slate-50 disabled:opacity-40"
        :disabled="!unreadCount"
        @click="onMarkAllRead"
      >
        Mark all read
      </button>
    </div>

    <!-- Row 2: Role-specific category tabs -->
    <div
      v-if="visibleCategories.length > 1"
      class="mb-4 flex flex-wrap gap-1.5"
    >
      <button
        class="rounded-full border px-3 py-1 text-xs transition"
        :class="category === 'all'
          ? 'border-indigo-600 bg-indigo-50 text-indigo-700'
          : 'border-slate-200 text-slate-600 hover:bg-slate-50'"
        @click="category = 'all'"
      >
        All
      </button>
      <button
        v-for="c in visibleCategories"
        :key="c"
        class="rounded-full border px-3 py-1 text-xs transition"
        :class="category === c
          ? 'border-indigo-600 bg-indigo-50 text-indigo-700'
          : 'border-slate-200 text-slate-600 hover:bg-slate-50'"
        @click="category = c"
      >
        {{ labels[c] }}
      </button>
    </div>

    <div
      v-if="error"
      class="mb-4 rounded border border-rose-200 bg-rose-50 p-3 text-sm text-rose-700"
    >
      {{ error }}
    </div>

    <div
      v-if="loading && !items.length"
      class="py-10 text-center text-slate-500"
    >
      Loading…
    </div>

    <div
      v-else-if="!filtered.length"
      class="py-10 text-center text-slate-500"
    >
      {{ emptyMessage }}
    </div>

    <ul v-else class="divide-y overflow-hidden rounded-lg border bg-white">
      <li
        v-for="n in filtered"
        :key="n.id"
        class="flex items-start gap-3 p-4 transition"
        :class="{ 'bg-indigo-50/40': !n.read }"
      >
        <div class="flex-1">
          <div class="flex items-center gap-2">
            <span class="text-xs font-medium text-indigo-600">
              {{ n.eventType }}
            </span>
            <span class="text-xs text-slate-400">
              {{ timeAgo(n.createdAt) }}
            </span>
          </div>
          <p class="mt-0.5 font-medium text-slate-800">
            {{ n.subject ?? 'Notification' }}
          </p>
          <p class="text-sm text-slate-600">{{ n.body }}</p>
        </div>
        <div class="flex flex-col gap-1">
          <button
            v-if="!n.read"
            class="text-xs text-indigo-600 hover:underline"
            @click="onMarkRead(n.id)"
          >
            Mark read
          </button>
          <button
            class="text-xs text-rose-600 hover:underline"
            @click="onRemove(n.id)"
          >
            Delete
          </button>
        </div>
      </li>
    </ul>

    <button
      v-if="hasMore"
      class="mt-4 w-full rounded border py-2 text-sm transition hover:bg-slate-50"
      @click="onLoadMore"
    >
      Load more
    </button>
  </div>
</template>