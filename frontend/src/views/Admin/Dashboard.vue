<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useAdminStore } from '../../stores/admin'

const store = useAdminStore()
const { overview, loading, error } = storeToRefs(store)

onMounted(() => {
  void store.fetchOverview()
})

function fmtMoney(n: number | undefined): string {
  if (n == null) return '—'
  return new Intl.NumberFormat('en-KE', {
    style: 'currency',
    currency: 'KES',
    maximumFractionDigits: 0,
  }).format(n)
}

function fmtPct(rate: number | undefined): string {
  if (rate == null) return '—'
  return `${Math.round(rate * 100)}%`
}

interface Card {
  label: string
  value: string | number
  sub: string
  tone?: 'default' | 'success' | 'warning' | 'danger'
}

const cards = computed<Card[]>(() => {
  const o = overview.value
  if (!o) return []
  const ps = o.propertyStats
  const pm = o.paymentSummary

  return [
    {
      label: 'Total Users',
      value: o.totalUsers,
      sub: `${o.totalLandlords} landlords · ${o.totalTenants} tenants`,
    },
    {
      label: 'Active Leases',
      value: o.activeLeases,
      sub: 'Currently running',
    },
    {
      label: 'Open Tickets',
      value: o.openTickets,
      sub: 'Maintenance outstanding',
      tone: o.openTickets > 10 ? 'warning' : 'default',
    },
    {
      label: 'Properties',
      value: ps.totalProperties,
      sub: `${ps.totalUnits} units total`,
    },
    {
      label: 'Occupancy',
      value: fmtPct(ps.occupancyRate),
      sub: `${ps.occupiedUnits} occupied · ${ps.vacantUnits} vacant`,
      tone: ps.occupancyRate >= 0.8
        ? 'success'
        : ps.occupancyRate >= 0.5
        ? 'default'
        : 'warning',
    },
    {
      label: 'Revenue',
      value: fmtMoney(pm.totalCollected),
      sub: `${pm.paymentCount} transactions`,
      tone: 'success',
    },
    {
      label: 'Pending Payments',
      value: fmtMoney(pm.totalPending),
      sub: 'Awaiting settlement',
    },
    {
      label: 'Failed Payments',
      value: pm.failedCount,
      sub: `${fmtMoney(pm.totalFailed)} total`,
      tone: pm.failedCount > 0 ? 'danger' : 'default',
    },
  ]
})

function toneClass(tone: Card['tone']): string {
  switch (tone) {
    case 'success': return 'text-emerald-600'
    case 'warning': return 'text-amber-600'
    case 'danger':  return 'text-rose-600'
    default:        return 'text-slate-900'
  }
}
</script>

<template>
  <div class="p-8 max-w-7xl mx-auto">
    <header class="mb-8 flex items-start justify-between">
      <div>
        <h1 class="text-2xl font-semibold text-slate-900">Dashboard</h1>
        <p class="text-sm text-slate-500 mt-1">Platform overview</p>
      </div>
      <button
        class="rounded border border-slate-200 bg-white px-3 py-1.5 text-sm text-slate-700 transition hover:bg-slate-50 disabled:opacity-50"
        :disabled="loading"
        @click="store.fetchOverview(true)"
      >
        {{ loading ? 'Refreshing…' : 'Refresh' }}
      </button>
    </header>

    <!-- Error -->
    <div
      v-if="error"
      class="mb-6 rounded border border-rose-200 bg-rose-50 p-3 text-sm text-rose-700"
    >
      {{ error }}
    </div>

    <!-- Loading skeleton -->
    <div
      v-if="loading && !overview"
      class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4"
    >
      <div
        v-for="i in 8"
        :key="i"
        class="rounded-lg border border-slate-200 bg-white p-5 animate-pulse"
      >
        <div class="h-3 w-20 bg-slate-200 rounded"></div>
        <div class="h-7 w-16 bg-slate-200 rounded mt-3"></div>
        <div class="h-3 w-24 bg-slate-100 rounded mt-2"></div>
      </div>
    </div>

    <!-- KPI grid -->
    <div
      v-else
      class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4"
    >
      <div
        v-for="c in cards"
        :key="c.label"
        class="rounded-lg border border-slate-200 bg-white p-5"
      >
        <p class="text-xs uppercase tracking-wide text-slate-500">
          {{ c.label }}
        </p>
        <p
          class="mt-2 text-2xl font-semibold"
          :class="toneClass(c.tone)"
        >
          {{ c.value }}
        </p>
        <p class="mt-1 text-xs text-slate-500">{{ c.sub }}</p>
      </div>
    </div>
  </div>
</template>