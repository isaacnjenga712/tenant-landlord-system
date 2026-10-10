<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { AdminApi } from '../../api/admin.api'
import type {
  AdminPaymentDto,
  PageResponse,
  PaymentSummaryDto,
} from '../../types/admin'

const items = ref<AdminPaymentDto[]>([])
const summary = ref<PaymentSummaryDto | null>(null)

const loading = ref(false)
const error = ref<string | null>(null)

const page = ref(0)
const size = 25
const total = ref(0)
const hasMore = ref(false)

const filterStatus = ref('')
const filterMethod = ref('')

let debounce: ReturnType<typeof setTimeout> | undefined
watch([filterStatus, filterMethod], () => {
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
    const [list, sum] = await Promise.all([
      AdminApi.listPayments({
        page: page.value,
        size,
        status: filterStatus.value || undefined,
        method: filterMethod.value || undefined,
      }),
      AdminApi.financeSummary(),
    ])
    items.value = list.items
    total.value = list.total
    hasMore.value = list.hasMore
    summary.value = sum
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to load payments'
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

function fmtMoney(n: number | undefined): string {
  if (n == null) return '—'
  return new Intl.NumberFormat('en-KE', {
    style: 'currency',
    currency: 'KES',
    maximumFractionDigits: 0,
  }).format(n)
}

function fmtTime(iso: string): string {
  return new Date(iso).toLocaleString('en-GB', {
    day: '2-digit', month: 'short',
    hour: '2-digit', minute: '2-digit',
  })
}

function statusClass(status: string): string {
  switch (status.toLowerCase()) {
    case 'success':   return 'bg-emerald-100 text-emerald-700'
    case 'failed':    return 'bg-rose-100 text-rose-700'
    case 'initiated': return 'bg-amber-100 text-amber-700'
    case 'refunded':  return 'bg-slate-100 text-slate-600'
    default:          return 'bg-slate-100 text-slate-600'
  }
}

function methodIcon(method: string): string {
  const m = method.toLowerCase()
  if (m.includes('mpesa') || m.includes('m-pesa')) return '📱'
  if (m.includes('cash')) return '💵'
  if (m.includes('card')) return '💳'
  if (m.includes('bank')) return '🏦'
  return '💰'
}
</script>

<template>
  <div class="p-8 max-w-7xl mx-auto">
    <header class="mb-6 flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-semibold text-slate-900">Payments</h1>
        <p class="text-sm text-slate-500 mt-1">
          {{ total }} transactions
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

    <!-- Summary cards -->
    <div
      v-if="summary"
      class="mb-6 grid grid-cols-1 sm:grid-cols-3 gap-4"
    >
      <div class="rounded-lg border border-emerald-200 bg-emerald-50 p-4">
        <p class="text-xs uppercase tracking-wide text-emerald-700">Collected</p>
        <p class="mt-1 text-2xl font-semibold text-emerald-700">
          {{ fmtMoney(summary.totalCollected) }}
        </p>
        <p class="text-xs text-emerald-600 mt-0.5">
          {{ summary.paymentCount }} transactions
        </p>
      </div>
      <div class="rounded-lg border border-amber-200 bg-amber-50 p-4">
        <p class="text-xs uppercase tracking-wide text-amber-700">Pending</p>
        <p class="mt-1 text-2xl font-semibold text-amber-700">
          {{ fmtMoney(summary.totalPending) }}
        </p>
        <p class="text-xs text-amber-600 mt-0.5">Awaiting settlement</p>
      </div>
      <div
        class="rounded-lg border p-4"
        :class="summary.failedCount > 0
          ? 'border-rose-200 bg-rose-50'
          : 'border-slate-200 bg-white'"
      >
        <p
          class="text-xs uppercase tracking-wide"
          :class="summary.failedCount > 0 ? 'text-rose-700' : 'text-slate-500'"
        >
          Failed
        </p>
        <p
          class="mt-1 text-2xl font-semibold"
          :class="summary.failedCount > 0 ? 'text-rose-700' : 'text-slate-400'"
        >
          {{ summary.failedCount }}
        </p>
        <p
          class="text-xs mt-0.5"
          :class="summary.failedCount > 0 ? 'text-rose-600' : 'text-slate-400'"
        >
          {{ fmtMoney(summary.totalFailed) }}
        </p>
      </div>
    </div>

    <!-- Filters -->
    <div class="mb-4 flex flex-wrap gap-2 items-center">
      <select
        v-model="filterStatus"
        class="rounded border border-slate-200 bg-white px-3 py-1.5 text-sm focus:border-indigo-500 focus:outline-none"
      >
        <option value="">All statuses</option>
        <option value="success">Success</option>
        <option value="initiated">Initiated</option>
        <option value="failed">Failed</option>
        <option value="refunded">Refunded</option>
      </select>
      <select
        v-model="filterMethod"
        class="rounded border border-slate-200 bg-white px-3 py-1.5 text-sm focus:border-indigo-500 focus:outline-none"
      >
        <option value="">All methods</option>
        <option value="MPESA">M-Pesa</option>
        <option value="CARD">Card</option>
        <option value="CASH">Cash</option>
        <option value="BANK_TRANSFER">Bank Transfer</option>
      </select>
    </div>

    <div
      v-if="error"
      class="mb-4 rounded border border-rose-200 bg-rose-50 p-3 text-sm text-rose-700"
    >
      {{ error }}
    </div>

    <div v-if="loading && items.length === 0" class="py-16 text-center text-slate-500">
      Loading payments…
    </div>

    <div v-else-if="items.length === 0" class="py-16 text-center text-slate-500">
      No payments found.
    </div>

    <div v-else class="overflow-hidden rounded-lg border border-slate-200 bg-white">
      <table class="w-full text-sm">
        <thead class="bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
          <tr>
            <th class="px-4 py-3 font-medium">Date</th>
            <th class="px-4 py-3 font-medium">Reference</th>
            <th class="px-4 py-3 font-medium">Tenant</th>
            <th class="px-4 py-3 font-medium">Method</th>
            <th class="px-4 py-3 font-medium text-right">Amount</th>
            <th class="px-4 py-3 font-medium">Status</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-100">
          <tr v-for="p in items" :key="p.id" class="hover:bg-slate-50">
            <td class="px-4 py-3 whitespace-nowrap text-slate-600">
              {{ fmtTime(p.createdAt) }}
            </td>
            <td class="px-4 py-3">
              <p class="text-slate-800 font-mono text-xs">
                {{ p.gatewayTransactionId || p.id.slice(0, 12) }}
              </p>
            </td>
            <td class="px-4 py-3">
              <p class="text-xs text-slate-500 font-mono truncate max-w-[180px]" :title="p.tenantId">
                {{ p.tenantId }}
              </p>
            </td>
            <td class="px-4 py-3">
              <span class="inline-flex items-center gap-1.5 text-slate-700">
                <span>{{ methodIcon(p.method) }}</span>
                <span class="text-xs">{{ p.method }}</span>
              </span>
            </td>
            <td class="px-4 py-3 text-right font-medium text-slate-800">
              {{ fmtMoney(p.amount) }}
            </td>
            <td class="px-4 py-3">
              <span
                class="inline-block rounded-full px-2 py-0.5 text-xs font-medium"
                :class="statusClass(p.status)"
              >
                {{ p.status }}
              </span>
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

