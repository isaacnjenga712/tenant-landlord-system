
<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { AdminApi } from '../../api/admin.api'
import type { ServiceHealthDto } from '../../types/admin'

const services = ref<ServiceHealthDto[]>([])
const loading = ref(false)
const error = ref<string | null>(null)
const lastRefresh = ref<Date | null>(null)

let pollHandle: ReturnType<typeof setInterval> | undefined

async function fetch(): Promise<void> {
  loading.value = true
  error.value = null
  try {
    services.value = await AdminApi.services()
    lastRefresh.value = new Date()
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to load services'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  void fetch()
  pollHandle = setInterval(fetch, 15_000)  // auto-refresh every 15s
})

onUnmounted(() => {
  if (pollHandle) clearInterval(pollHandle)
})

// ---- Derived ----
const stats = computed(() => {
  const total = services.value.length
  const up = services.value.filter((s) => s.status === 'UP').length
  const down = total - up
  return { total, up, down }
})

const grouped = computed(() => {
  const order = ['UP', 'DOWN', 'UNKNOWN']
  const map: Record<string, ServiceHealthDto[]> = {}
  for (const s of services.value) {
    const k = order.includes(s.status) ? s.status : 'UNKNOWN'
    if (!map[k]) map[k] = []
    map[k].push(s)
  }
  // Sort each bucket alphabetically by service name
  for (const k of Object.keys(map)) {
    map[k].sort((a, b) => a.serviceName.localeCompare(b.serviceName))
  }
  return map
})

const orderedGroups = computed(() =>
  (['UP', 'DOWN', 'UNKNOWN'] as const).filter((k) => grouped.value[k]?.length)
)

function statusColor(status: string): string {
  switch (status) {
    case 'UP':   return 'emerald'
    case 'DOWN': return 'rose'
    default:     return 'slate'
  }
}

function statusClass(status: string): string {
  const c = statusColor(status)
  return `bg-${c}-100 text-${c}-700`
}
</script>

<template>
  <div class="p-8 max-w-7xl mx-auto">
    <header class="mb-6 flex items-start justify-between">
      <div>
        <h1 class="text-2xl font-semibold text-slate-900">System Health</h1>
        <p class="text-sm text-slate-500 mt-1">
          Auto-refreshes every 15 seconds
          <span v-if="lastRefresh">
            · Last check {{ lastRefresh.toLocaleTimeString() }}
          </span>
        </p>
      </div>
      <button
        class="rounded border border-slate-200 bg-white px-3 py-1.5 text-sm text-slate-700 transition hover:bg-slate-50 disabled:opacity-50"
        :disabled="loading"
        @click="fetch"
      >
        {{ loading ? 'Checking…' : 'Refresh now' }}
      </button>
    </header>

    <div
      v-if="error"
      class="mb-6 rounded border border-rose-200 bg-rose-50 p-3 text-sm text-rose-700"
    >
      {{ error }}
    </div>

    <!-- Summary chips -->
    <div class="mb-6 grid grid-cols-3 gap-4 max-w-xl">
      <div class="rounded-lg border border-slate-200 bg-white p-4">
        <p class="text-xs uppercase tracking-wide text-slate-500">Services</p>
        <p class="mt-1 text-2xl font-semibold text-slate-900">{{ stats.total }}</p>
      </div>
      <div class="rounded-lg border border-emerald-200 bg-emerald-50 p-4">
        <p class="text-xs uppercase tracking-wide text-emerald-700">Up</p>
        <p class="mt-1 text-2xl font-semibold text-emerald-700">{{ stats.up }}</p>
      </div>
      <div
        class="rounded-lg border p-4"
        :class="stats.down > 0
          ? 'border-rose-200 bg-rose-50'
          : 'border-slate-200 bg-white'"
      >
        <p
          class="text-xs uppercase tracking-wide"
          :class="stats.down > 0 ? 'text-rose-700' : 'text-slate-500'"
        >
          Down
        </p>
        <p
          class="mt-1 text-2xl font-semibold"
          :class="stats.down > 0 ? 'text-rose-700' : 'text-slate-400'"
        >
          {{ stats.down }}
        </p>
      </div>
    </div>

    <!-- Loading -->
    <div
      v-if="loading && services.length === 0"
      class="py-16 text-center text-slate-500"
    >
      Checking services…
    </div>

    <!-- Empty -->
    <div
      v-else-if="services.length === 0"
      class="py-16 text-center text-slate-500"
    >
      No services reported.
    </div>

    <!-- Groups -->
    <div v-else class="space-y-8">
      <section
        v-for="status in orderedGroups"
        :key="status"
      >
        <h2 class="text-xs font-semibold uppercase tracking-wider text-slate-500 mb-3">
          {{ status }} · {{ grouped[status].length }}
        </h2>
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
          <div
            v-for="s in grouped[status]"
            :key="s.serviceName"
            class="rounded-lg border border-slate-200 bg-white p-4 flex items-start gap-3"
          >
            <!-- Status dot -->
            <span
              class="mt-1 h-2.5 w-2.5 rounded-full flex-shrink-0"
              :class="{
                'bg-emerald-500': s.status === 'UP',
                'bg-rose-500':    s.status === 'DOWN',
                'bg-slate-400':   s.status !== 'UP' && s.status !== 'DOWN',
              }"
            ></span>

            <div class="min-w-0 flex-1">
              <p class="text-sm font-medium text-slate-900 truncate">
                {{ s.serviceName }}
              </p>
              <p class="text-xs text-slate-500 mt-0.5">
                Version: {{ s.version || 'n/a' }}
              </p>
              <p
                v-if="s.instanceId"
                class="text-[10px] text-slate-400 font-mono truncate mt-0.5"
                :title="s.instanceId"
              >
                {{ s.instanceId }}
              </p>
            </div>

            <span
              class="rounded-full px-2 py-0.5 text-[10px] font-semibold uppercase tracking-wide"
              :class="{
                'bg-emerald-100 text-emerald-700': s.status === 'UP',
                'bg-rose-100 text-rose-700':       s.status === 'DOWN',
                'bg-slate-100 text-slate-600':     s.status !== 'UP' && s.status !== 'DOWN',
              }"
            >
              {{ s.status }}
            </span>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>