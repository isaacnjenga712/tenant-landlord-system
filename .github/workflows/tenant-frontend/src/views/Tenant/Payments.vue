<template>
  <AppLayout>
    <div class="space-y-6">
      <div>
        <p class="text-sm uppercase tracking-[0.2em] text-slate-400">Payments</p>
        <h2 class="mt-2 text-3xl font-bold text-slate-900">Rent history</h2>
      </div>

      <div class="flex gap-3">
        <button
          class="rounded bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700"
          @click="openPayDialog"
        >
          Pay Rent via M‑Pesa
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
              <td class="px-5 py-3">KSh {{ p.amount.toLocaleString() }}</td>
              <td class="px-5 py-3">{{ p.method }}</td>
              <td class="px-5 py-3">
                <span :class="[
                  'rounded px-2 py-0.5 text-xs font-medium',
                  p.status === 'PAID' || p.status === 'COMPLETED' ? 'bg-emerald-100 text-emerald-700' :
                  p.status === 'PENDING' ? 'bg-amber-100 text-amber-700' :
                  'bg-rose-100 text-rose-700',
                ]">
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
import { useNotificationStore } from '../../stores/notification'

const payments = ref<Payment[]>([])
const loading = ref(true)
const error = ref('')
const notification = useNotificationStore()

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
  // Find the tenant's active lease
  try {
    const { data } = await leaseApi.list()
    const activeLease = data.leases.find(l => l.status === 'ACTIVE')
    if (!activeLease) {
      notification.addToast('No active lease found', 'error')
      return
    }

    const phone = prompt('Enter M-Pesa phone (e.g. 2547XXXXXXXX)')
    if (!phone) return

    const amount = activeLease.rentAmount ?? activeLease.monthlyRent ?? 0

    await mpesaApi.stkPush({ phone, amount, leaseId: activeLease.id })
    notification.addToast('M-Pesa prompt sent to your phone', 'success')

    // Reload after a short delay (STK push is async)
    setTimeout(loadPayments, 3000)
  } catch (err: any) {
    notification.addToast(err.response?.data?.message || 'Payment failed', 'error')
  }
}

onMounted(loadPayments)
</script>
