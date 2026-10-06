<template>
  <AppLayout>
    <div class="space-y-6">
      <div class="flex items-center justify-between">
        <div>
          <p class="text-sm uppercase tracking-[0.2em] text-slate-400">Billing</p>
          <h2 class="mt-2 text-3xl font-bold text-slate-900">Security deposits</h2>
        </div>
        <button
          class="rounded bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700"
          @click="toggleForm"
        >
          {{ showForm ? 'Cancel' : 'New deposit' }}
        </button>
      </div>

      <!-- Create form -->
      <div v-if="showForm" class="card space-y-4 p-5">
        <h3 class="text-lg font-semibold text-slate-900">Record security deposit</h3>

        <div class="grid gap-4 md:grid-cols-2">
          <div>
            <label class="block text-sm text-slate-600">Lease</label>
            <select v-model="form.leaseId" class="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm">
              <option value="">Select lease…</option>
              <option v-for="l in leases" :key="l.id" :value="l.id">
                {{ l.propertyTitle }} — {{ l.tenantShort }}
              </option>
            </select>
          </div>

          <div>
            <label class="block text-sm text-slate-600">Total deposit (KSh)</label>
            <input v-model.number="form.totalDeposit" type="number" min="0"
                   class="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm" />
          </div>
        </div>

        <div v-if="error" class="text-sm text-red-600">{{ error }}</div>

        <button
          class="rounded bg-slate-900 px-4 py-2 text-sm font-medium text-white hover:bg-slate-800 disabled:opacity-50"
          :disabled="submitting"
          @click="submit"
        >
          {{ submitting ? 'Creating…' : 'Record deposit' }}
        </button>
      </div>

      <!-- Deposit list -->
      <div v-if="loading" class="text-slate-500">Loading…</div>
      <div v-else-if="!deposits.length" class="card p-6 text-slate-500">
        No security deposits recorded yet.
      </div>
      <div v-else class="space-y-3">
        <div v-for="d in deposits" :key="d.id" class="card p-5">
          <div class="flex items-start justify-between">
            <div class="flex-1">
              <p class="text-sm text-slate-500 font-mono">Lease {{ d.leaseId.slice(0, 8) }}…</p>
              <p class="text-xl font-bold text-slate-900 mt-1">
                KSh {{ formatMoney(d.currentBalance) }}
                <span class="text-sm font-normal text-slate-500">
                  of KSh {{ formatMoney(d.totalDeposit) }}
                </span>
              </p>
              <p v-if="d.deductionTotal" class="text-sm text-rose-600 mt-1">
                Deducted: KSh {{ formatMoney(d.deductionTotal) }}
              </p>
              <p v-if="d.returnedDate" class="text-sm text-emerald-600 mt-1">
                Returned {{ formatMoney(d.returnAmount) }} on {{ d.returnedDate }}
              </p>
            </div>
            <div class="flex flex-col items-end gap-2">
              <span :class="statusClass(d.status)">{{ d.status.replace('_', ' ') }}</span>
              <div v-if="d.status !== 'RETURNED'" class="flex gap-2 mt-1">
                <button
                  class="rounded border border-rose-300 px-3 py-1.5 text-xs font-medium text-rose-700 hover:bg-rose-50"
                  @click="openDeduction(d)"
                >
                  Deduct
                </button>
                <button
                  class="rounded bg-slate-900 px-3 py-1.5 text-xs font-medium text-white hover:bg-slate-800"
                  @click="openReturn(d)"
                >
                  Return
                </button>
              </div>
            </div>
          </div>
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
import { propertyApi } from '../../api/property.api'
import { useNotificationStore } from '../../stores/notification.ts'

const notification = useNotificationStore()

const deposits = ref<SecurityDeposit[]>([])
const leases = ref<{ id: string; propertyTitle: string; tenantShort: string }[]>([])
const loading = ref(true)
const submitting = ref(false)
const showForm = ref(false)
const error = ref('')

const form = ref({
  leaseId: '',
  totalDeposit: 0,
})

function formatMoney(n: number | undefined) { return Number(n || 0).toLocaleString() }

function statusClass(status: string) {
  const base = 'rounded px-2 py-0.5 text-xs font-medium'
  if (status === 'ACTIVE') return `${base} bg-emerald-100 text-emerald-700`
  if (status === 'PARTIALLY_DEDUCTED') return `${base} bg-amber-100 text-amber-700`
  if (status === 'RETURNED') return `${base} bg-slate-200 text-slate-700`
  return `${base} bg-slate-100 text-slate-700`
}

async function load() {
  loading.value = true
  try {
    const { data } = await securityDepositApi.list({ size: 100 })
    deposits.value = data.content
  } catch {
    notification.addToast('Failed to load deposits', 'error')
  } finally {
    loading.value = false
  }
}

async function loadLeases() {
  try {
    const [leaseRes, propRes] = await Promise.all([
      leaseApi.list(0, 100),
      propertyApi.list(),
    ])
    const all = leaseRes.data.leases ?? []
    const props = propRes.data
    leases.value = all
      .filter(l => l.status === 'ACTIVE')
      .map(l => {
        const p = props.find(p => p.id === l.propertyId)
        return {
          id: l.id,
          propertyTitle: p ? `${p.addressLine1}, ${p.city}` : 'Unknown property',
          tenantShort: l.tenantId.slice(0, 8),
        }
      })
  } catch (err) {
    console.error('leases load failed', err)
  }
}

function toggleForm() {
  showForm.value = !showForm.value
  if (!showForm.value) error.value = ''
}

async function submit() {
  error.value = ''
  if (!form.value.leaseId || !form.value.totalDeposit) {
    error.value = 'Lease and amount are required'
    return
  }
  submitting.value = true
  try {
    await securityDepositApi.create({
      leaseId: form.value.leaseId,
      totalDeposit: form.value.totalDeposit,
    })
    notification.addToast('Deposit recorded', 'success')
    showForm.value = false
    form.value = { leaseId: '', totalDeposit: 0 }
    await load()
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Failed to record deposit'
  } finally {
    submitting.value = false
  }
}

async function openDeduction(d: SecurityDeposit) {
  const amountStr = prompt(`Deduction amount (max KSh ${d.currentBalance}):`)
  if (!amountStr) return
  const amount = Number(amountStr)
  if (!amount || amount <= 0 || amount > d.currentBalance) {
    notification.addToast('Invalid amount', 'error')
    return
  }

  const description = prompt('Reason for deduction (e.g. broken window):')
  if (!description) return

  try {
    await securityDepositApi.addDeduction(d.id, { amount, description })
    notification.addToast('Deduction applied', 'success')
    await load()
  } catch (err: any) {
    notification.addToast(err.response?.data?.message || 'Failed to apply deduction', 'error')
  }
}

async function openReturn(d: SecurityDeposit) {
  const amountStr = prompt(`Return amount (max KSh ${d.currentBalance}):`, String(d.currentBalance))
  if (!amountStr) return
  const amount = Number(amountStr)
  if (amount < 0 || amount > d.currentBalance) {
    notification.addToast('Invalid amount', 'error')
    return
  }

  try {
    await securityDepositApi.returnDeposit(d.id, { returnAmount: amount })
    notification.addToast('Deposit marked as returned', 'success')
    await load()
  } catch (err: any) {
    notification.addToast(err.response?.data?.message || 'Failed to return deposit', 'error')
  }
}

onMounted(async () => {
  await loadLeases()
  await load()
})
</script>