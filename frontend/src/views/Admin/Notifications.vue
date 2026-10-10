<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { AdminApi } from '../../api/admin.api'
import type { NotificationLogDto, PageResponse } from '../../types/admin'

const items = ref<NotificationLogDto[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

const page = ref(0)
const size = 25
const total = ref(0)
const hasMore = ref(false)

const filterEventType = ref('')
const filterChannel = ref('')
const filterStatus = ref('')

const retrying = ref<string | null>(null)
const actionError = ref<string | null>(null)

let debounce: ReturnType<typeof setTimeout> | undefined
watch([filterEventType, filterChannel, filterStatus], () => {
  clearTimeout(debounce)
  debounce = setTimeout(() => {
    page.value = 0
    void fetch()
  }, 300)
})

async function fetch(): Promise<void> {
  loading.value = true
  error.value = null
  try {
    const res: PageResponse<NotificationLogDto> = await AdminApi.notificationLogs({
      page: page.value,
      size,
      eventType: filterEventType.value || undefined,
      channel: filterChannel.value || undefined,
      status: filterStatus.value || undefined,
    })
    items.value = res.items
    total.value = res.total
    hasMore.value = res.hasMore
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to load logs'
  } finally {
    loading.value = false
  }
}

onMounted(fetch)

const pageCount = computed(() => Math.ceil(total.value / size) || 1)

function prevPage(): void {
  if (page.value > 0) {
    page.value--
    void fetch()
  }
}

function nextPage(): void {
  if (hasMore.value) {
    page.value++
    void fetch()
  }
}

async function retry(log: NotificationLogDto): Promise<void> {
  retrying.value = log.id
  actionError.value = null
  try {
    await AdminApi.retryNotification(log.id)
    await fetch()
  } catch (e: unknown) {
    actionError.value = e instanceof Error ? e.message : 'Retry failed'
  } finally {
    retrying.value = null
  }
}

function statusClass(status: string): string {
  switch (status.toUpperCase()) {
    case 'SENT':    return 'bg-emerald-100 text-emerald-700'
    case 'FAILED':  return 'bg-rose-100 text-rose-700'
    case 'PENDING': return 'bg-amber-100 text-amber-700'
    default:        return 'bg-slate-100 text-slate-600'
  }
}

function channelIcon(channel: string): string {
  switch (channel.toUpperCase()) {
    case 'EMAIL':    return '✉️'
    case 'SMS':      return '📱'
    case 'WHATSAPP': return '💬'
    case 'IN_APP':   return '🔔'
    case 'PUSH':     return '📲'
    default:         return '📤'
  }
}

function timeAgo(iso: string): string {
  const s = Math.floor((Date.now() - new Date(iso).getTime()) / 1000)
  if (s < 60) return `${s}s ago`
  if (s < 3600) return `${Math.floor(s / 60)}m ago`
  if (s < 86400) return `${Math.floor(s / 3600)}h ago`
  return `${Math.floor(s / 86400)}d ago`
}
</script>

<template>
  <div class="p-8 max-w-7xl mx-auto">
    <header class="mb-6 flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-semibold text-slate-900">Notifications</h1>
        <p class="text-sm text-slate-500 mt-1">
          Delivery logs · {{ total }} total
        </p>
      </div>
      <button
        class="rounded border border-slate-200 bg-white px-3 py-1.5 text-sm text-slate-700 transition hover:bg-slate-50 disabled:opacity-50"
        :disabled="loading"
        @click="fetch"
      >
        {{ loading ? 'Refreshing…' : 'Refresh' }}
      </button>
    </header>

    <div class="mb-4 flex flex-wrap gap-2 items-center">
      <input
        v-model="filterEventType"
        type="text"
        placeholder="Event type (e.g. LEASE_CREATED)…"
        class="w-64 rounded border border-slate-200 bg-white px-3 py-1.5 text-sm focus:border-indigo-500 focus:outline-none"
      />
      <select
        v-model="filterChannel"
        class="rounded border border-slate-200 bg-white px-3 py-1.5 text-sm focus:border-indigo-500 focus:outline-none"
      >
        <option value="">All channels</option>
        <option value="EMAIL">Email</option>
        <option value="SMS">SMS</option>
        <option value="WHATSAPP">WhatsApp</option>
        <option value="IN_APP">In-App</option>
        <option value="PUSH">Push</option>
      </select>
      <select
        v-model="filterStatus"
        class="rounded border border-slate-200 bg-white px-3 py-1.5 text-sm focus:border-indigo-500 focus:outline-none"
      >
        <option value="">All statuses</option>
        <option value="SENT">Sent</option>
        <option value="FAILED">Failed</option>
        <option value="PENDING">Pending</option>
      </select>
    </div>

    <div
      v-if="error"
      class="mb-4 rounded border border-rose-200 bg-rose-50 p-3 text-sm text-rose-700"
    >
      {{ error }}
    </div>

    <div
      v-if="actionError"
      class="mb-4 rounded border border-rose-200 bg-rose-50 p-3 text-sm text-rose-700"
    >
      {{ actionError }}
    </div>

    <div v-if="loading && items.length === 0" class="py-16 text-center text-slate-500">
      Loading delivery logs…
    </div>

    <div v-else-if="items.length === 0" class="py-16 text-center text-slate-500">
      No delivery logs found.
    </div>

    <div v-else class="overflow-hidden rounded-lg border border-slate-200 bg-white">
      <table class="w-full text-sm">
        <thead class="bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
          <tr>
            <th class="px-4 py-3 font-medium">Time</th>
            <th class="px-4 py-3 font-medium">Event</th>
            <th class="px-4 py-3 font-medium">Channel</th>
            <th class="px-4 py-3 font-medium">Recipient</th>
            <th class="px-4 py-3 font-medium">Status</th>
            <th class="px-4 py-3 font-medium">Attempts</th>
            <th class="px-4 py-3 font-medium text-right">Actions</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-100">
          <tr v-for="log in items" :key="log.id" class="hover:bg-slate-50">
            <td class="px-4 py-3 whitespace-nowrap">
              <p class="text-slate-800">{{ timeAgo(log.createdAt) }}</p>
            </td>
            <td class="px-4 py-3">
              <p class="text-slate-800 font-medium">{{ log.eventType }}</p>
            </td>
            <td class="px-4 py-3">
              <span class="inline-flex items-center gap-1.5 text-slate-700">
                <span>{{ channelIcon(log.channel) }}</span>
                <span class="text-xs">{{ log.channel }}</span>
              </span>
            </td>
            <td class="px-4 py-3">
              <p
                class="text-xs text-slate-500 font-mono truncate max-w-[180px]"
                :title="log.recipientPublicId ?? undefined"
              >
                {{ log.recipientPublicId || '—' }}
              </p>
            </td>
            <td class="px-4 py-3">
              <span
                class="inline-block rounded-full px-2 py-0.5 text-xs font-medium"
                :class="statusClass(log.status)"
              >
                {{ log.status }}
              </span>
              <p
                v-if="log.errorMessage"
                class="text-[10px] text-rose-600 mt-1 truncate max-w-[200px]"
                :title="log.errorMessage ?? undefined"
              >
                {{ log.errorMessage }}
              </p>
            </td>
            <td class="px-4 py-3 text-slate-600 text-center">{{ log.attempts }}</td>
            <td class="px-4 py-3 text-right">
              <button
                v-if="log.status.toUpperCase() === 'FAILED'"
                class="text-xs text-indigo-600 hover:underline disabled:opacity-50"
                :disabled="retrying === log.id"
                @click="retry(log)"
              >
                {{ retrying === log.id ? 'Retrying…' : 'Retry' }}
              </button>
              <span v-else class="text-xs text-slate-400">—</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div
      v-if="items.length > 0"
      class="mt-4 flex items-center justify-between text-sm text-slate-600"
    >
      <span>Page {{ page + 1 }} of {{ pageCount }}</span>
      <div class="flex gap-2">
        <button
          class="rounded border border-slate-200 bg-white px-3 py-1.5 transition hover:bg-slate-50 disabled:opacity-40"
          :disabled="page === 0 || loading"
          @click="prevPage"
        >
          Previous
        </button>
        <button
          class="rounded border border-slate-200 bg-white px-3 py-1.5 transition hover:bg-slate-50 disabled:opacity-40"
          :disabled="!hasMore || loading"
          @click="nextPage"
        >
          Next
        </button>
      </div>
    </div>
  </div>
</template>