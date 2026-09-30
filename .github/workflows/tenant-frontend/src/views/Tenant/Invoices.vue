<template>
  <AppLayout>
    <div class="space-y-6">
      <div>
        <p class="text-sm uppercase tracking-[0.2em] text-slate-400">Billing</p>
        <h2 class="mt-2 text-3xl font-bold text-slate-900">My invoices</h2>
      </div>

      <div v-if="loading" class="text-slate-500">Loading…</div>
      <div v-else-if="!invoices.length" class="card p-6 text-slate-500">No invoices yet.</div>
      <div v-else class="space-y-3">
        <div v-for="inv in invoices" :key="inv.id" class="card p-5">
          <div class="flex items-start justify-between">
            <div class="flex-1">
              <div class="flex items-center gap-2">
                <button
                  class="text-slate-400 text-xs hover:text-slate-600"
                  @click="toggleExpand(inv.id)"
                >
                  {{ expandedId === inv.id ? '▼' : '▶' }}
                </button>
                <h3 class="text-lg font-semibold text-slate-900">{{ inv.invoiceNumber }}</h3>
              </div>
              <p class="text-sm text-slate-500 mt-1 ml-5">
                {{ inv.periodStart }} → {{ inv.periodEnd }} · Due {{ inv.dueDate }}
              </p>
              <p class="text-xl font-bold text-slate-900 mt-2 ml-5">
                KSh {{ formatMoney(inv.totalAmount) }}
              </p>
              <p v-if="inv.paidAmount > 0" class="text-sm text-emerald-600 mt-1 ml-5">
                Paid: KSh {{ formatMoney(inv.paidAmount) }}
              </p>

              <!-- Line items -->
              <div v-if="expandedId === inv.id" class="mt-3 ml-5 border-t border-slate-200 pt-3">
                <div v-if="itemsLoading" class="text-sm text-slate-500">Loading items…</div>
                <div v-else-if="!itemsByInvoice[inv.id]?.length" class="text-sm text-slate-500">
                  No line items.
                </div>
                <table v-else class="w-full text-sm">
                  <thead class="text-slate-500">
                    <tr>
                      <th class="text-left py-1">Description</th>
                      <th class="text-left py-1">Category</th>
                      <th class="text-right py-1">Qty</th>
                      <th class="text-right py-1">Unit price</th>
                      <th class="text-right py-1">Total</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="li in itemsByInvoice[inv.id]" :key="li.id" class="border-t border-slate-200">
                      <td class="py-1">{{ li.description }}</td>
                      <td class="py-1 capitalize text-slate-500">{{ li.category }}</td>
                      <td class="py-1 text-right">{{ li.quantity }}</td>
                      <td class="py-1 text-right">KSh {{ formatMoney(li.unitPrice) }}</td>
                      <td class="py-1 text-right font-medium">KSh {{ formatMoney(li.total) }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <div class="flex flex-col items-end gap-3 ml-4">
              <span :class="statusClass(inv.status)">{{ inv.status.toUpperCase() }}</span>
              <button
                v-if="inv.status !== 'paid' && inv.status !== 'voided'"
                class="rounded bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-50"
                :disabled="payingId === inv.id"
                @click="payInvoice(inv)"
              >
                {{ payingId === inv.id ? 'Sending…' : 'Pay via M-Pesa' }}
              </button>
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
import { invoiceApi, type Invoice } from '../../api/invoice.api'
import { invoiceLineItemApi, type InvoiceLineItem } from '../../api/invoice-line-item.api'
import { leaseApi } from '../../api/lease.api'
import { mpesaApi } from '../../api/mpesa.api'
import { useAuthStore } from '../../stores/auth'
import { useNotificationStore } from '../../stores/notification'

const auth = useAuthStore()
const notification = useNotificationStore()

const invoices = ref<Invoice[]>([])
const loading = ref(true)
const payingId = ref<string | null>(null)

const expandedId = ref<string | null>(null)
const itemsByInvoice = ref<Record<string, InvoiceLineItem[]>>({})
const itemsLoading = ref(false)

function formatMoney(n: number | undefined) { return Number(n || 0).toLocaleString() }

function statusClass(status: string) {
  const base = 'rounded px-2 py-0.5 text-xs font-medium'
  if (status === 'paid') return `${base} bg-emerald-100 text-emerald-700`
  if (status === 'partial') return `${base} bg-blue-100 text-blue-700`
  if (status === 'overdue') return `${base} bg-rose-100 text-rose-700`
  if (status === 'voided') return `${base} bg-slate-200 text-slate-600`
  return `${base} bg-amber-100 text-amber-700`
}

function normalizePhone(input: string): string {
  const digits = input.replace(/\D/g, '')
  if (digits.startsWith('254')) return digits
  if (digits.startsWith('0')) return '254' + digits.slice(1)
  if (digits.length === 9) return '254' + digits
  return digits
}

async function load() {
  loading.value = true
  try {
    const { data } = await leaseApi.list(0, 100)
    const leaseIds = (data.leases ?? []).map(l => l.id)

    const all: Invoice[] = []
    for (const leaseId of leaseIds) {
      const { data: page } = await invoiceApi.list({ leaseId, size: 100 })
      all.push(...page.content)
    }
    all.sort((a, b) => (b.createdAt || '').localeCompare(a.createdAt || ''))
    invoices.value = all
  } catch {
    notification.addToast('Failed to load invoices', 'error')
  } finally {
    loading.value = false
  }
}

async function toggleExpand(invoiceId: string) {
  if (expandedId.value === invoiceId) {
    expandedId.value = null
    return
  }
  expandedId.value = invoiceId
  if (!itemsByInvoice.value[invoiceId]) {
    itemsLoading.value = true
    try {
      const { data } = await invoiceLineItemApi.listByInvoice(invoiceId)
      itemsByInvoice.value[invoiceId] = data
    } catch {
      itemsByInvoice.value[invoiceId] = []
    } finally {
      itemsLoading.value = false
    }
  }
}

async function payInvoice(inv: Invoice) {
  const rawPhone = prompt('Enter M-Pesa phone (e.g. 2547XXXXXXXX or 07XXXXXXXX)')
  if (!rawPhone) return

  const phone = normalizePhone(rawPhone)
  if (!/^254[0-9]{9}$/.test(phone)) {
    notification.addToast('Invalid phone. Use 2547XXXXXXXX', 'error')
    return
  }

  const tenantId = auth.user?.id
  if (!tenantId) {
    notification.addToast('Not logged in', 'error')
    return
  }

  payingId.value = inv.id
  try {
    const amount = Math.round(inv.totalAmount - (inv.paidAmount || 0))
    await mpesaApi.stkPush({
      phone,
      amount,
      leaseId: inv.leaseId,
      tenantId,
      accountReference: inv.invoiceNumber,
      transactionDesc: `Invoice ${inv.invoiceNumber}`,
    })
    notification.addToast('M-Pesa prompt sent — confirm on your phone', 'success')

    pollAndMarkPaid(inv)
  } catch (err: any) {
    notification.addToast(err.response?.data?.message || 'Payment failed', 'error')
  } finally {
    payingId.value = null
  }
}

function pollAndMarkPaid(inv: Invoice) {
  let attempts = 0
  const max = 20
  const timer = setInterval(async () => {
    attempts++
    try {
      const { data } = await mpesaApi.byLease(inv.leaseId)
      const latest = data.find(t => t.status === 'SUCCESS')
      if (latest) {
        clearInterval(timer)
        const amount = inv.totalAmount - (inv.paidAmount || 0)
        await invoiceApi.pay(inv.id, amount)
        notification.addToast(`Invoice ${inv.invoiceNumber} marked paid`, 'success')
        await load()
      }
    } catch {
      // ignore
    }
    if (attempts >= max) clearInterval(timer)
  }, 3000)
}

onMounted(load)
</script>