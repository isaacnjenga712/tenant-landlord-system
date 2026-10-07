<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { AdminApi } from '../../api/admin.api'
import type { AuditLogDto, PageResponse } from '../../types/admin'

const items = ref<AuditLogDto[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

const page = ref(0)
const size = 30
const total = ref(0)
const hasMore = ref(false)

const filterActor = ref('')
const filterAction = ref('')

let debounce: ReturnType<typeof setTimeout> | undefined
watch([filterActor, filterAction], () => {
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
    const res: PageResponse<AuditLogDto> = await AdminApi.auditLogs({
      page: page.value,
      size,
      actor: filterActor.value || undefined,
      action: filterAction.value || undefined,
    })
    items.value = res.items
    total.value = res.total
    hasMore.value = res.hasMore
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to load audit logs'
  } finally {
    loading.value = false
  }
}

onMounted(fetch)

const pageCount = computed(() => Math.ceil(total.value / size) || 1)

function prevPage(): void {
  if (page.value > 0) { page.value--; void fetch() }
}
function nextPage(): void {
  if (hasMore.value) { page.value++; void fetch() }
}

function fmtTime(iso: string): string {
  const d = new Date(iso)
  return d.toLocaleString('en-GB', {
    day: '2-digit', month: 'short', year: 'numeric',
    hour: '2-digit', minute: '2-digit', second: '2-digit',
  })
}

function actionBadge(action: string): string {
  const a = action.toLowerCase()
  if (a.includes('delete') || a.includes('remove') || a.includes('deactivate'))
    return 'bg-rose-100 text-rose-700'
  if (a.includes('create') || a.includes('register') || a.includes('activate'))
    return 'bg-emerald-100 text-emerald-700'
  if (a.includes('update') || a.includes('change') || a.includes('role'))
    return 'bg-amber-100 text-amber-700'
  if (a.includes('login') || a.includes('auth'))
    return 'bg-sky-100 text-sky-700'
  return 'bg-slate-100 text-slate-600'
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
        <h1 class="text-2xl font-semibold text-slate-900">Audit Logs</h1>
        <p class="text-sm text-slate-500 mt-1">
          {{ total }} {{ total === 1 ? 'event' : 'events' }} recorded
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

    <!-- Filters -->
    <div class="mb-4 flex flex-wrap gap-2 items-center">
      <input
        v-model="filterActor"
        type="text"
        placeholder="Filter by actor email…"
        class="w-64 rounded border border-slate-200 bg-white px-3 py-1.5 text-sm focus:border-indigo-500 focus:outline-none"
      />
      <input
        v-model="filterAction"
        type="text"
        placeholder="Filter by action…"
        class="w-64 rounded border border-slate-200 bg-white px-3 py-1.5 text-sm focus:border-indigo-500 focus:outline-none"
      />
    </div>

    <div
      v-if="error"
      class="mb-4 rounded border border-rose-200 bg-rose-50 p-3 text-sm text-rose-700"
    >
      {{ error }}
    </div>

    <div v-if="loading && items.length === 0" class="py-16 text-center text-slate-500">
      Loading audit logs…
    </div>

    <div v-else-if="items.length === 0" class="py-16 text-center text-slate-500">
      No audit events recorded.
    </div>

    <div v-else class="overflow-hidden rounded-lg border border-slate-200 bg-white">
      <table class="w-full text-sm">
        <thead class="bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
          <tr>
            <th class="px-4 py-3 font-medium">Timestamp</th>
            <th class="px-4 py-3 font-medium">Actor</th>
            <th class="px-4 py-3 font-medium">Action</th>
            <th class="px-4 py-3 font-medium">Resource</th>
            <th class="px-4 py-3 font-medium">IP</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-100">
          <tr v-for="log in items" :key="log.id" class="hover:bg-slate-50">
            <td class="px-4 py-3 whitespace-nowrap">
              <p class="text-slate-800">{{ fmtTime(log.createdAt) }}</p>
              <p class="text-xs text-slate-400">{{ timeAgo(log.createdAt) }}</p>
            </td>
            <td class="px-4 py-3">
              <p class="text-slate-800 truncate max-w-[200px]" :title="log.actorEmail">
                {{ log.actorEmail || '—' }}
              </p>
            </td>
            <td class="px-4 py-3">
              <span
                class="inline-block rounded-full px-2 py-0.5 text-xs font-medium"
                :class="actionBadge(log.action)"
              >
                {{ log.action }}
              </span>
            </td>
            <td class="px-4 py-3 text-slate-600">
              <span class="text-slate-500 text-xs">{{ log.resource }}</span>
              <span v-if="log.resourceId" class="ml-1 text-xs text-slate-400 font-mono">
                {{ log.resourceId.slice(0, 8) }}…
              </span>
            </td>
            <td class="px-4 py-3 text-xs text-slate-500 font-mono">
              {{ log.ipAddress || '—' }}
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