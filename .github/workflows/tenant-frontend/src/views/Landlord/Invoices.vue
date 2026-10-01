<template>
  <AppLayout>
    <div class="space-y-6">
      <div class="flex items-center justify-between">
        <div>
          <p class="text-sm uppercase tracking-[0.2em] text-slate-400">Billing</p>
          <h2 class="mt-2 text-3xl font-bold text-slate-900">Invoices</h2>
        </div>
        <button
          class="rounded bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700"
          @click="toggleForm"
        >
          {{ showForm ? 'Cancel' : 'New invoice' }}
        </button>
      </div>

      <div v-if="showForm" class="card space-y-4 p-5">
        <h3 class="text-lg font-semibold text-slate-900">Generate invoice</h3>

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
            <label class="block text-sm text-slate-600">Period start</label>
            <input v-model="form.periodStart" type="date"
                   class="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm" />
          </div>

          <div>
            <label class="block text-sm text-slate-600">Period end</label>
            <input v-model="form.periodEnd" type="date"
                   class="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm" />
          </div>

          <div>
            <label class="block text-sm text-slate-600">Due date</label>
            <input v-model="form.dueDate" type="date"
                   class="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm" />
          </div>
        </div>

        <!-- Line Items -->
        <div class="border-t border-slate-200 pt-4">
          <div class="flex items-center justify-between mb-3">
            <h4 class="text-sm font-medium text-slate-700">Line items</h4>
            <button
              type="button"
              class="text-xs rounded border border-slate-300 px-2 py-1 hover:bg-slate-50"
              @click="addLineItem"
            >
              + Add line
            </button>
          </div>

          <div v-if="!lineItems.length" class="text-sm text-slate-500 py-2">
            No line items. Click "+ Add line" to itemize.
          </div>

          <div v-else class="space-y-2">
            <div
              v-for="(li, idx) in lineItems"
              :key="idx"
              class="grid grid-cols-12 gap-2 items-center"
            >
              <input
                v-model="li.description"
                placeholder="Description"
                class="col-span-5 rounded border border-slate-300 px-2 py-1.5 text-sm"
              />
              <select
                v-model="li.category"
                class="col-span-2 rounded border border-slate-300 px-2 py-1.5 text-sm"
              >
                <option value="rent">Rent</option>
                <option value="utility">Utility</option>
                <option value="fee">Fee</option>
                <option value="deposit">Deposit</option>
              </select>
              <input
                v-model.number="li.quantity"
                type="number"
                min="1"
                placeholder="Qty"
                class="col-span-1 rounded border border-slate-300 px-2 py-1.5 text-sm"
              />
              <input
                v-model.number="li.unitPrice"
                type="number"
                min="0"
                step="0.01"
                placeholder="Unit price"
                class="col-span-2 rounded border border-slate-300 px-2 py-1.5 text-sm"
              />
              <div class="col-span-1 text-sm text-slate-500 text-right">
                {{ lineTotal(li).toLocaleString() }}
              </div>
              <button
                type="button"
                class="col-span-1 text-rose-600 hover:text-rose-700 text-sm"
                @click="removeLineItem(idx)"
              >
                ✕
              </button>
            </div>

            <div class="flex justify-end text-sm pt-2 border-t border-slate-200">
              <span class="text-slate-500 mr-2">Total:</span>
              <span class="font-semibold text-slate-900">
                KSh {{ lineItemsTotal.toLocaleString() }}
              </span>
            </div>
          </div>
        </div>

        <!-- Total -->
        <div>
          <label class="block text-sm text-slate-600">
            Total amount (KSh)
            <span v-if="lineItems.length" class="text-xs text-slate-400 ml-2">
              — auto-filled from line items
            </span>
          </label>
          <input
            v-model.number="form.totalAmount"
            type="number"
            min="0"
            class="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm"
          />
        </div>

        <div v-if="error" class="text-sm text-red-600">{{ error }}</div>

        <button
          class="rounded bg-slate-900 px-4 py-2 text-sm font-medium text-white hover:bg-slate-800 disabled:opacity-50"
          :disabled="submitting"
          @click="submit"
        >
          {{ submitting ? 'Creating…' : 'Create invoice' }}
        </button>
      </div>

      <!-- Filter tabs -->
      <div class="flex gap-2 flex-wrap">
        <button
          v-for="tab in filterTabs"
          :key="tab.value"
          :class="filterButtonClass(filterStatus === tab.value)"
          @click="filterStatus = tab.value"
        >
          {{ tab.label }}
          <span v-if="tab.count > 0" class="ml-1 text-xs opacity-70">({{ tab.count }})</span>
        </button>
      </div>

      <div v-if="loading" class="text-slate-500">Loading…</div>
      <div v-else-if="!filteredInvoices.length" class="card p-6 text-slate-500">
        No invoices match this filter.
      </div>
      <div v-else class="card overflow-hidden">
        <table class="min-w-full text-left text-sm">
          <thead class="bg-slate-50 text-slate-700">
            <tr>
              <th class="w-8"></th>
              <th class="px-5 py-3 font-medium">Number</th>
              <th class="px-5 py-3 font-medium">Period</th>
              <th class="px-5 py-3 font-medium">Due</th>
              <th class="px-5 py-3 font-medium">Progress</th>
              <th class="px-5 py-3 font-medium">Status</th>
            </tr>
          </thead>
          <tbody>
            <template v-for="inv in filteredInvoices" :key="inv.id">
              <tr
                class="border-t border-slate-200 cursor-pointer hover:bg-slate-50"
                @click="toggleExpand(inv.id)"
              >
                <td class="px-2 py-3 text-slate-400 text-xs">
                  {{ expandedId === inv.id ? '▼' : '▶' }}
                </td>
                <td class="px-5 py-3 font-mono text-xs">{{ inv.invoiceNumber }}</td>
                <td class="px-5 py-3 text-xs">{{ inv.periodStart }} → {{ inv.periodEnd }}</td>
                <td class="px-5 py-3 text-xs">{{ inv.dueDate }}</td>
                <td class="px-5 py-3">
                  <div class="flex items-center gap-2 min-w-[180px]">
                    <div class="flex-1 h-1.5 rounded-full bg-slate-200 overflow-hidden">
                      <div
                        :class="progressBarClass(inv.status)"
                        :style="{ width: progressPct(inv) + '%' }"
                      ></div>
                    </div>
                    <span class="text-xs text-slate-600 whitespace-nowrap">
                      {{ formatMoney(inv.paidAmount) }} / {{ formatMoney(inv.totalAmount) }}
                    </span>
                  </div>
                </td>
                <td class="px-5 py-3">
                  <span :class="statusClass(inv.status)">{{ inv.status.toUpperCase() }}</span>
                </td>
              </tr>
              <tr v-if="expandedId === inv.id" class="bg-slate-50 border-t border-slate-200">
                <td colspan="6" class="px-8 py-4">
                  <div v-if="itemsLoading" class="text-sm text-slate-500">Loading items…</div>
                  <div v-else-if="!itemsByInvoice[inv.id]?.length" class="text-sm text-slate-500">
                    No line items for this invoice.
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
                </td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import AppLayout from '../../components/layout/AppLayout.vue'
import { invoiceApi, generateInvoiceNumber, type Invoice } from '../../api/invoice.api'
import { invoiceLineItemApi, type InvoiceLineItem, type LineItemCategory } from '../../api/invoice-line-item.api'
import { leaseApi } from '../../api/lease.api'
import { propertyApi } from '../../api/property.api'
import { useNotificationStore } from '../../stores/notification'

const notification = useNotificationStore()

interface DraftLine {
  description: string
  category: LineItemCategory
  quantity: number
  unitPrice: number
}

const invoices = ref<Invoice[]>([])
const leases = ref<{ id: string; propertyTitle: string; tenantShort: string }[]>([])
const loading = ref(true)
const submitting = ref(false)
const showForm = ref(false)
const error = ref('')

const lineItems = ref<DraftLine[]>([])
const expandedId = ref<string | null>(null)
const itemsByInvoice = ref<Record<string, InvoiceLineItem[]>>({})
const itemsLoading = ref(false)

const filterStatus = ref<'all' | 'pending' | 'partial' | 'overdue' | 'paid'>('all')

const filterTabs = computed(() => {
  const counts = { pending: 0, partial: 0, overdue: 0, paid: 0 }
  invoices.value.forEach(i => {
    if (i.status in counts) counts[i.status as keyof typeof counts]++
  })
  return [
    { value: 'all' as const, label: 'All', count: invoices.value.length },
    { value: 'pending' as const, label: 'Pending', count: counts.pending },
    { value: 'partial' as const, label: 'Partial', count: counts.partial },
    { value: 'overdue' as const, label: 'Overdue', count: counts.overdue },
    { value: 'paid' as const, label: 'Paid', count: counts.paid },
  ]
})

const filteredInvoices = computed(() =>
  filterStatus.value === 'all'
    ? invoices.value
    : invoices.value.filter(i => i.status === filterStatus.value),
)

const today = new Date()
const monthStart = new Date(today.getFullYear(), today.getMonth(), 1).toISOString().split('T')[0]
const monthEnd = new Date(today.getFullYear(), today.getMonth() + 1, 0).toISOString().split('T')[0]
const monthDue = new Date(today.getFullYear(), today.getMonth() + 1, 5).toISOString().split('T')[0]

const form = ref({
  leaseId: '',
  totalAmount: 0,
  periodStart: monthStart,
  periodEnd: monthEnd,
  dueDate: monthDue,
})

function formatMoney(n: number | undefined) { return Number(n || 0).toLocaleString() }

function progressPct(inv: Invoice): number {
  if (!inv.totalAmount) return 0
  return Math.min(100, Math.round(((inv.paidAmount || 0) / inv.totalAmount) * 100))
}

function progressBarClass(status: string) {
  if (status === 'paid') return 'h-full bg-emerald-500 transition-all'
  if (status === 'overdue') return 'h-full bg-rose-500 transition-all'
  if (status === 'partial') return 'h-full bg-blue-500 transition-all'
  return 'h-full bg-amber-500 transition-all'
}

function filterButtonClass(active: boolean) {
  const base = 'rounded border px-3 py-1.5 text-xs font-medium transition-colors'
  return active
    ? `${base} bg-slate-900 text-white border-slate-900`
    : `${base} bg-white text-slate-700 border-slate-300 hover:bg-slate-50`
}

function statusClass(status: string) {
  const base = 'rounded px-2 py-0.5 text-xs font-medium'
  if (status === 'paid') return `${base} bg-emerald-100 text-emerald-700`
  if (status === 'partial') return `${base} bg-blue-100 text-blue-700`
  if (status === 'overdue') return `${base} bg-rose-100 text-rose-700`
  if (status === 'voided') return `${base} bg-slate-200 text-slate-600`
  return `${base} bg-amber-100 text-amber-700`
}

function lineTotal(li: DraftLine): number {
  return (Number(li.quantity) || 0) * (Number(li.unitPrice) || 0)
}

const lineItemsTotal = computed(() =>
  lineItems.value.reduce((sum, li) => sum + lineTotal(li), 0),
)

function addLineItem() {
  lineItems.value.push({ description: '', category: 'rent', quantity: 1, unitPrice: 0 })
}

function removeLineItem(idx: number) {
  lineItems.value.splice(idx, 1)
  syncTotalFromLines()
}

function syncTotalFromLines() {
  if (lineItems.value.length) {
    form.value.totalAmount = lineItemsTotal.value
  }
}

function toggleForm() {
  showForm.value = !showForm.value
  if (!showForm.value) {
    lineItems.value = []
  }
}

async function load() {
  loading.value = true
  try {
    const { data } = await invoiceApi.list({ size: 100 })
    invoices.value = data.content
  } catch {
    notification.addToast('Failed to load invoices', 'error')
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

async function submit() {
  error.value = ''
  if (!form.value.leaseId || !form.value.totalAmount) {
    error.value = 'Lease and amount are required'
    return
  }
  const invalid = lineItems.value.find(li => !li.description.trim() || li.unitPrice <= 0)
  if (lineItems.value.length && invalid) {
    error.value = 'Each line item needs a description and a unit price > 0'
    return
  }

  submitting.value = true
  try {
    if (lineItems.value.length) {
      form.value.totalAmount = lineItemsTotal.value
    }

    const { data: inv } = await invoiceApi.create({
      leaseId: form.value.leaseId,
      invoiceNumber: generateInvoiceNumber(),
      periodStart: form.value.periodStart,
      periodEnd: form.value.periodEnd,
      dueDate: form.value.dueDate,
      totalAmount: form.value.totalAmount,
      paidAmount: 0,
    })

    for (const li of lineItems.value) {
      await invoiceLineItemApi.create({
        invoiceId: inv.id,
        description: li.description,
        category: li.category,
        quantity: li.quantity,
        unitPrice: li.unitPrice,
        taxRate: 0,
      })
    }

    notification.addToast('Invoice created', 'success')
    showForm.value = false
    lineItems.value = []
    await load()
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Failed to create invoice'
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await loadLeases()
  await load()
})
</script>