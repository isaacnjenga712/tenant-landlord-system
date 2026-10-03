<template>
  <AppLayout>
    <div class="space-y-6">
      <div>
        <p class="text-sm uppercase tracking-[0.2em] text-slate-400">Payments</p>
        <h2 class="mt-2 text-3xl font-bold text-slate-900">Rent history</h2>
      </div>

      <div class="flex gap-3">
        <button
          class="rounded bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-50"
          :disabled="paying"
          @click="openPayDialog"
        >
          {{ paying ? 'Sending…' : 'Pay Rent via M‑Pesa' }}
        </button>
      </div>

      <div v-if="loading" class="text-slate-500">Loading…</div>
      <div v-else-if="error" class="text-red-600">{{ error }}</div>
      <div v-else-if="!payments.length" class="text-slate-500">No payments yet.</div>

      <div v-else class="card overflow-hidden rounded border border-slate-200">
        <table class="min-w-full text-left text-sm">
          <thead class="bg-slate-50 text-slate-700">
            <tr>
              <th class="px-5 py-3 font-medium">Date</th>
              <th class="px-5 py-3 font-medium">Amount</th>
              <th class="px-5 py-3 font-medium">Method</th>
              <th class="px-5 py-3 font-medium">Status</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="p in payments" :key="p.id" class="border-t border-slate-200">
              <td class="px-5 py-3">{{ new Date(p.createdAt).toLocaleDateString() }}</td>
              <td class="px-5 py-3">KSh {{ formatMoney(p.amount) }}</td>
              <td class="px-5 py-3">{{ p.method }}</td>
              <td class="px-5 py-3">
                <span :class="statusClass(p.status)">
                  {{ p.status }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import AppLayout from '../../components/layout/AppLayout.vue'
import { paymentApi, type Payment } from '../../api/payment.api'
import { mpesaApi } from '../../api/mpesa.api'
import { leaseApi } from '../../api/lease.api'
import { useAuthStore } from '../../stores/auth'
import { useNotificationStore } from '../../stores/notification'

const payments = ref<Payment[]>([])
const loading = ref(true)
const error = ref('')
const paying = ref(false)
const notification = useNotificationStore()
const auth = useAuthStore()

function formatMoney(n: number | string | undefined) {
  return Number(n || 0).toLocaleString()
}

function statusClass(status: string) {
  const base = 'rounded px-2 py-0.5 text-xs font-medium'
  if (status === 'PAID' || status === 'COMPLETED' || status === 'SUCCESS') {
    return `${base} bg-emerald-100 text-emerald-700`
  }
  if (status === 'PENDING') return `${base} bg-amber-100 text-amber-700`
  return `${base} bg-rose-100 text-rose-700`
}

async function loadPayments() {
  loading.value = true
  error.value = ''
  try {
    payments.value = await paymentApi.listAll()
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Failed to load payments'
  } finally {
    loading.value = false
  }
}

async function openPayDialog() {
  if (paying.value) return

  try {
    // Find the tenant's active lease
    const { data } = await leaseApi.list()
    const activeLease =
      data.leases.find(l => l.status === 'ACTIVE') ??
      data.leases.find(l => l.status === 'DRAFT') ??
      null

    if (!activeLease) {
      notification.addToast('No active lease found', 'error')
      return
    }

    const phone = prompt('Enter M-Pesa phone (e.g. 2547XXXXXXXX)')
    if (!phone) return

    const tenantId = auth.user?.id
    if (!tenantId) {
      notification.addToast('You must be logged in', 'error')
      return
    }

    const amount = Number(activeLease.rentAmount ?? activeLease.monthlyRent ?? 0)
    if (!amount) {
      notification.addToast('Lease has no rent amount set', 'error')
      return
    }

    paying.value = true

    await mpesaApi.stkPush({
      phone,
      amount,
      leaseId: activeLease.id,
      tenantId,
      accountReference: `LEASE-${activeLease.id.slice(0, 8)}`,
      transactionDesc: 'Rent payment',
    })

    notification.addToast('M-Pesa prompt sent to your phone', 'success')

    // STK push is async — reload after a delay to catch the callback
    setTimeout(loadPayments, 5000)
  } catch (err: any) {
    notification.addToast(
      err.response?.data?.message || err.response?.data?.error || 'Payment failed',
      'error',
    )
  } finally {
    paying.value = false
  }
}

onMounted(loadPayments)
</script>