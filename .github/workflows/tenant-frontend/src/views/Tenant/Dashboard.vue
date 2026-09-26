<template>
  <AppLayout>
    <div class="space-y-6">
      <div>
        <p class="text-sm uppercase tracking-[0.2em] text-slate-400">Tenant view</p>
        <h2 class="mt-2 text-3xl font-bold text-slate-900">My home overview</h2>
      </div>

      <div v-if="loading" class="text-slate-500">Loading your dashboard…</div>
      <div v-else-if="error" class="text-red-600">{{ error }}</div>

      <template v-else>
        <!-- Stat cards -->
        <div class="grid gap-4 md:grid-cols-3">
          <div class="card p-5">
            <p class="text-sm text-slate-500">Current rent</p>
            <p class="mt-3 text-3xl font-bold text-slate-900">
              {{ lease ? `KSh ${formatMoney(rentAmount)}` : '—' }}
            </p>
          </div>
          <div class="card p-5">
            <p class="text-sm text-slate-500">Next due date</p>
            <p class="mt-3 text-3xl font-bold text-slate-900">
              {{ nextPayment ? formatDate(nextPayment.dueDate || nextPayment.createdAt) : '—' }}
            </p>
          </div>
          <div class="card p-5">
            <p class="text-sm text-slate-500">Payment status</p>
            <p class="mt-3 text-3xl font-bold text-slate-900">
              {{ nextPayment ? nextPayment.status : 'No pending' }}
            </p>
          </div>
        </div>

        <!-- Lease + payments -->
        <div class="grid gap-6 lg:grid-cols-2">
          <LeaseCard v-if="lease" :lease="lease" />
          <div v-else class="card p-5">
            <h3 class="text-lg font-semibold text-slate-900">No active lease</h3>
            <p class="mt-2 text-sm text-slate-600">
              You don't have an active lease yet. Apply for a property to get started.
            </p>
          </div>

          <div class="card p-5">
            <div class="flex items-center justify-between">
              <h3 class="text-lg font-semibold text-slate-900">Recent payments</h3>
              <button
                v-if="lease"
                class="rounded bg-emerald-600 px-3 py-1.5 text-xs font-medium text-white hover:bg-emerald-700 disabled:opacity-50"
                :disabled="paying"
                @click="openPayDialog"
              >
                {{ paying ? 'Sending…' : 'Pay via M‑Pesa' }}
              </button>
            </div>

            <div v-if="!recentPayments.length" class="mt-4 text-sm text-slate-500">
              No payments yet.
            </div>
            <ul v-else class="mt-4 divide-y divide-slate-200 text-sm">
              <li
                v-for="p in recentPayments"
                :key="p.id"
                class="flex items-center justify-between py-3"
              >
                <div>
                  <p class="font-medium text-slate-900">KSh {{ formatMoney(p.amount) }}</p>
                  <p class="text-slate-500">{{ formatDate(p.createdAt) }} · {{ p.method }}</p>
                </div>
                <span :class="statusClass(p.status)">{{ p.status }}</span>
              </li>
            </ul>
          </div>
        </div>
      </template>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import AppLayout from '../../components/layout/AppLayout.vue'
import LeaseCard from '../../components/lease/LeaseCard.vue'
import { leaseApi } from '../../api/lease.api'
import { paymentApi, type Payment } from '../../api/payment.api'
import { mpesaApi } from '../../api/mpesa.api'
import { useAuthStore } from '../../stores/auth'
import { useNotificationStore } from '../../stores/notification'

const loading = ref(true)
const error = ref('')
const lease = ref<any | null>(null)
const payments = ref<Payment[]>([])
const paying = ref(false)
const notification = useNotificationStore()
const auth = useAuthStore()

const rentAmount = computed(() =>
  lease.value?.rentAmount ?? lease.value?.monthlyRent ?? 0,
)

const leasePayments = computed(() =>
  lease.value ? payments.value.filter(p => p.leaseId === lease.value.id) : [],
)

const nextPayment = computed(() =>
  leasePayments.value.find(p => p.status === 'PENDING') ?? null,
)

const recentPayments = computed(() =>
  [...leasePayments.value]
    .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
    .slice(0, 5),
)

function formatMoney(n: number | string | undefined) {
  return Number(n || 0).toLocaleString()
}

function formatDate(d?: string) {
  if (!d) return '—'
  return new Date(d).toLocaleDateString(undefined, { day: 'numeric', month: 'short' })
}

function statusClass(status: string) {
  const base = 'rounded px-2 py-0.5 text-xs font-medium'
  if (status === 'PAID' || status === 'COMPLETED' || status === 'SUCCESS') {
    return `${base} bg-emerald-100 text-emerald-700`
  }
  if (status === 'PENDING') return `${base} bg-amber-100 text-amber-700`
  return `${base} bg-rose-100 text-rose-700`
}

async function loadDashboard() {
  loading.value = true
  error.value = ''
  try {
    const [leaseRes, paymentsList] = await Promise.all([
      leaseApi.list(),
      paymentApi.listAll(),
    ])
    const leases = leaseRes.data?.leases ?? []
    lease.value =
      leases.find((l: any) => l.status === 'ACTIVE') ??
      leases.find((l: any) => l.status === 'DRAFT') ??
      leases[0] ??
      null
    payments.value = paymentsList
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Failed to load dashboard'
  } finally {
    loading.value = false
  }
}

async function openPayDialog() {
  if (!lease.value || paying.value) return

  const phone = prompt('Enter M-Pesa phone (e.g. 2547XXXXXXXX)')
  if (!phone) return

  const tenantId = auth.user?.id
  if (!tenantId) {
    notification.addToast('You must be logged in', 'error')
    return
  }

  const amount = rentAmount.value
  if (!amount) {
    notification.addToast('Lease has no rent amount set', 'error')
    return
  }

  paying.value = true
  try {
    await mpesaApi.stkPush({
      phone,
      amount,
      leaseId: lease.value.id,
      tenantId,
      accountReference: `LEASE-${lease.value.id.slice(0, 8)}`,
      transactionDesc: 'Rent payment',
    })
    notification.addToast('M-Pesa prompt sent to your phone', 'success')

    // STK push is async — give the callback time, then reload
    setTimeout(loadDashboard, 5000)
  } catch (err: any) {
    notification.addToast(
      err.response?.data?.message || err.response?.data?.error || 'Payment failed',
      'error',
    )
  } finally {
    paying.value = false
  }
}

onMounted(loadDashboard)
</script>