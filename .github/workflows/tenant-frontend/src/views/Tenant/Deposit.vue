<template>
  <AppLayout>
    <div class="space-y-6">
      <div>
        <p class="text-sm uppercase tracking-[0.2em] text-slate-400">Billing</p>
        <h2 class="mt-2 text-3xl font-bold text-slate-900">Security deposit</h2>
      </div>

      <div v-if="loading" class="text-slate-500">Loading…</div>
      <div v-else-if="!deposit" class="card p-6 text-slate-500">
        No security deposit on file for your lease.
      </div>
      <div v-else class="card p-6 space-y-5">
        <div class="flex items-start justify-between">
          <div>
            <p class="text-sm text-slate-500">Total deposit held</p>
            <p class="text-3xl font-bold text-slate-900 mt-1">
              KSh {{ formatMoney(deposit.totalDeposit) }}
            </p>
          </div>
          <span :class="statusClass(deposit.status)">
            {{ deposit.status.replace('_', ' ') }}
          </span>
        </div>

        <div class="grid gap-4 sm:grid-cols-3 pt-4 border-t border-slate-200">
          <div>
            <p class="text-xs text-slate-500 uppercase tracking-wide">Current balance</p>
            <p class="text-lg font-semibold text-slate-900 mt-1">
              KSh {{ formatMoney(deposit.currentBalance) }}
            </p>
          </div>
          <div>
            <p class="text-xs text-slate-500 uppercase tracking-wide">Deducted</p>
            <p class="text-lg font-semibold text-slate-900 mt-1">
              KSh {{ formatMoney(deposit.deductionTotal || 0) }}
            </p>
          </div>
          <div>
            <p class="text-xs text-slate-500 uppercase tracking-wide">Interest accrued</p>
            <p class="text-lg font-semibold text-slate-900 mt-1">
              KSh {{ formatMoney(deposit.interestAccrued || 0) }}
            </p>
          </div>
        </div>

        <div v-if="deposit.returnedDate" class="pt-4 border-t border-slate-200">
          <p class="text-sm text-slate-500">Returned on {{ deposit.returnedDate }}</p>
          <p class="text-lg font-semibold text-emerald-600 mt-1">
            KSh {{ formatMoney(deposit.returnAmount) }} returned
          </p>
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import AppLayout from '../../components/layout/AppLayout.vue'
import { securityDepositApi, type SecurityDeposit } from '../../api/security-deposit.api'
import { leaseApi } from '../../api/lease.api'
import { useNotificationStore } from '../../stores/notification'

const notification = useNotificationStore()

const deposit = ref<SecurityDeposit | null>(null)
const loading = ref(true)

function formatMoney(n: number | undefined) { return Number(n || 0).toLocaleString() }

function statusClass(status: string) {
  const base = 'rounded px-2 py-1 text-xs font-medium'
  if (status === 'ACTIVE') return `${base} bg-emerald-100 text-emerald-700`
  if (status === 'PARTIALLY_DEDUCTED') return `${base} bg-amber-100 text-amber-700`
  if (status === 'RETURNED') return `${base} bg-slate-200 text-slate-700`
  return `${base} bg-slate-100 text-slate-700`
}

async function load() {
  loading.value = true
  try {
    const { data } = await leaseApi.list(0, 100)
    const leases = data.leases ?? []
    const active = leases.find(l => l.status === 'ACTIVE') ?? leases[0]

    if (!active) {
      deposit.value = null
      return
    }

    try {
      const { data: dep } = await securityDepositApi.byLease(active.id)
      deposit.value = dep
    } catch {
      // 404 — no deposit yet for this lease
      deposit.value = null
    }
  } catch {
    notification.addToast('Failed to load deposit', 'error')
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>