<template>
  <AppLayout>
    <div class="space-y-6">
      <div class="flex items-center justify-between">
        <div>
          <p class="text-sm uppercase tracking-[0.2em] text-slate-400">Billing</p>
          <h2 class="mt-2 text-3xl font-bold text-slate-900">Payment allocations</h2>
          <p class="mt-1 text-sm text-slate-500">
            Allocate successful M-Pesa payments to invoices.
          </p>
        </div>
        <button
          class="rounded bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700"
          @click="showForm = !showForm"
        >
          {{ showForm ? 'Cancel' : 'New allocation' }}
        </button>
      </div>

      <!-- Create form -->
      <div v-if="showForm" class="card space-y-4 p-5">
        <h3 class="text-lg font-semibold text-slate-900">Allocate a payment</h3>

        <div v-if="!successfulTransactions.length" class="text-sm text-slate-500 py-2">
          No successful M-Pesa payments found on your leases yet.
        </div>

        <div v-else class="grid gap-4 md:grid-cols-3">
          <div>
            <label class="block text-sm text-slate-600">Payment</label>
            <select v-model="form.paymentId" class="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm">
              <option value="">Select payment…</option>
              <option v-for="p in successfulTransactions" :key="p.id" :value="p.id">
                {{ shortTxn(p) }} — KSh {{ formatMoney(p.amount) }}
              </option>
            </select>
          </div>

          <div>
            <label class="block text-sm text-slate-600">Invoice</label>
            <select v-model="form.invoiceId" class="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm">
              <option value="">Select invoice…</option>
              <option v-for="i in invoices" :key="i.id" :value="i.id">
                {{ i.invoiceNumber }} — KSh {{ formatMoney(i.totalAmount) }}
              </option>
            </select>
          </div>

          <div>
            <label class="block text-sm text-slate-600">Allocated amount (KSh)</label>
            <input v-model.number="form.allocatedAmount" type="number" min="0" step="0.01"
                   class="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm" />
          </div>
        </div>

        <!-- Auto-fill amount when payment is selected -->
        <p v-if="form.paymentId" class="text-xs text-slate-500">
          Tip: allocation defaults to the payment amount —
          <button
            type="button"
            class="text-emerald-600 hover:underline"
            @click="fillFromPayment"
          >
            set to {{ formatMoney(selectedPaymentAmount) }} KSh
          </button>
        </p>

        <div v-if="error" class="text-sm text-red-600">{{ error }}</div>

        <button
          class="rounded bg-slate-900 px-4 py-2 text-sm font-medium text-white hover:bg-slate-800 disabled:opacity-50"
          :disabled="submitting"
          @click="submit"
        >
          {{ submitting ? 'Creating…' : 'Create allocation' }}
        </button>
      </div>

      <!-- List -->
      <div v-if="loading" class="text-slate-500">Loading…</div>
      <div v-else-if="!splits.length" class="card p-6 text-slate-500">
        No allocations yet.
      </div>
      <div v-else class="card overflow-hidden">
        <table class="min-w-full text-left text-sm">
          <thead class="bg-slate-50 text-slate-700">
            <tr>
              <th class="px-5 py-3 font-medium">Payment</th>
              <th class="px-5 py-3 font-medium">Invoice</th>
              <th class="px-5 py-3 font-medium">Allocated</th>
              <th class="px-5 py-3 font-medium">Created</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="s in splits" :key="s.id" class="border-t border-slate-200">
              <td class="px-5 py-3 font-mono text-xs">{{ s.paymentId.slice(0, 8) }}…</td>
              <td class="px-5 py-3 font-mono text-xs">
                {{ invoiceLabel(s.invoiceId) }}
              </td>
              <td class="px-5 py-3">KSh {{ formatMoney(s.allocatedAmount) }}</td>
              <td class="px-5 py-3 text-xs text-slate-500">
                {{ new Date(s.createdAt).toLocaleString() }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import AppLayout from '../../components/layout/AppLayout.vue'
import { paymentSplitApi, type PaymentSplit } from '../../api/payment-split.api'
import { mpesaApi, type MpesaTransaction } from '../../api/mpesa.api'
import { leaseApi } from '../../api/lease.api'
import { invoiceApi, type Invoice } from '../../api/invoice.api'
import { useNotificationStore } from '../../stores/notification'

const notification = useNotificationStore()

const splits = ref<PaymentSplit[]>([])
const transactions = ref<MpesaTransaction[]>([])
const invoices = ref<Invoice[]>([])
const loading = ref(true)
const submitting = ref(false)
const showForm = ref(false)
const error = ref('')

const form = ref({
  paymentId: '',
  invoiceId: '',
  allocatedAmount: 0,
})

const successfulTransactions = computed(() =>
  transactions.value.filter(t => t.status === 'SUCCESS'),
)

const selectedPaymentAmount = computed(() => {
  const t = successfulTransactions.value.find(t => t.id === form.value.paymentId)
  return t?.amount ?? 0
})

function formatMoney(n: number | undefined) { return Number(n || 0).toLocaleString() }

function shortTxn(t: MpesaTransaction): string {
  const ref = t.transactionId || t.id.slice(0, 8)
  return ref.length > 16 ? ref.slice(0, 16) + '…' : ref
}

function invoiceLabel(invoiceId: string): string {
  const inv = invoices.value.find(i => i.id === invoiceId)
  return inv ? inv.invoiceNumber : invoiceId.slice(0, 8) + '…'
}

function fillFromPayment() {
  form.value.allocatedAmount = selectedPaymentAmount.value
}

async function load() {
  loading.value = true
  try {
    splits.value = await paymentSplitApi.listAll()
  } catch {
    notification.addToast('Failed to load splits', 'error')
  } finally {
    loading.value = false
  }
}

async function loadRefs() {
  try {
    // 1. Get landlord's leases (auto-scoped by X-User-Id from JWT)
    const { data: leaseRes } = await leaseApi.list(0, 100)
    const leases = leaseRes.leases ?? []

    // 2. For each lease, fetch its M-Pesa transactions
    const txnArrays = await Promise.all(
      leases.map(l =>
        mpesaApi.byLease(l.id).then(r => r.data).catch(() => [] as MpesaTransaction[]),
      ),
    )
    transactions.value = txnArrays.flat()

    // 3. Also load invoices for the landlord to allocate against
    const { data: invoicePage } = await invoiceApi.list({ size: 100 })
    invoices.value = invoicePage.content
  } catch (err) {
    console.error('refs load failed', err)
  }
}

async function submit() {
  error.value = ''
  if (!form.value.paymentId || !form.value.invoiceId || !form.value.allocatedAmount) {
    error.value = 'All fields are required'
    return
  }
  submitting.value = true
  try {
    await paymentSplitApi.create({
      paymentId: form.value.paymentId,
      invoiceId: form.value.invoiceId,
      allocatedAmount: form.value.allocatedAmount,
    })
    notification.addToast('Allocation created', 'success')
    showForm.value = false
    form.value = { paymentId: '', invoiceId: '', allocatedAmount: 0 }
    await load()
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Failed to create allocation'
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await loadRefs()
  await load()
})
</script>