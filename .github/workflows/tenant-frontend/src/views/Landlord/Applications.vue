<template>
  <AppLayout>
    <div class="space-y-6">
      <div>
        <p class="text-sm uppercase tracking-[0.2em] text-slate-400">Approvals</p>
        <h2 class="mt-2 text-3xl font-bold text-slate-900">Tenant applications</h2>
      </div>

      <div v-if="loading" class="text-slate-500">Loading…</div>

      <div v-else-if="!applications.length" class="card p-6 text-slate-500">
        No pending applications.
      </div>

      <div v-else class="card overflow-hidden">
        <table class="min-w-full text-left text-sm text-slate-600">
          <thead class="bg-slate-50 text-slate-700">
            <tr>
              <th class="px-5 py-3 font-medium">Applicant</th>
              <th class="px-5 py-3 font-medium">Property</th>
              <th class="px-5 py-3 font-medium">Period</th>
              <th class="px-5 py-3 font-medium">Rent</th>
              <th class="px-5 py-3 font-medium">Status</th>
              <th class="px-5 py-3 font-medium">Action</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="app in applications"
              :key="app.id"
              class="border-t border-slate-200"
            >
              <td class="px-5 py-3 font-mono text-xs">{{ app.tenantId.slice(0, 8) }}…</td>
              <td class="px-5 py-3">{{ propertyTitle(app.propertyId) }}</td>
              <td class="px-5 py-3 text-xs">{{ app.startDate }} → {{ app.endDate }}</td>
              <td class="px-5 py-3">KSh {{ formatMoney(app.rentAmount) }}</td>
              <td class="px-5 py-3">
                <span :class="statusClass(app.status)">
                  {{ app.status }}
                </span>
              </td>
              <td class="px-5 py-3">
                <button
                  v-if="app.status === 'DRAFT'"
                  class="rounded bg-emerald-600 px-3 py-1.5 text-xs font-medium text-white hover:bg-emerald-700 disabled:opacity-50"
                  :disabled="busyId === app.id"
                  @click="approve(app.id)"
                >
                  {{ busyId === app.id ? 'Approving…' : 'Approve' }}
                </button>
                <span v-else class="text-xs text-slate-500">—</span>
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
import { leaseApi } from '../../api/lease.api'
import { propertyApi } from '../../api/property.api'
import { useNotificationStore } from '../../stores/notification'
import type { Lease } from '../../types/lease'
import type { Property } from '../../types/property'

const applications = ref<Lease[]>([])
const properties = ref<Property[]>([])
const loading = ref(true)
const busyId = ref<string | null>(null)
const notification = useNotificationStore()

function propertyTitle(propertyId: string): string {
  const p = properties.value.find(x => x.id === propertyId)
  return p ? `${p.addressLine1}, ${p.city}` : propertyId.slice(0, 8) + '…'
}

function formatMoney(n: number | string | undefined) {
  return Number(n || 0).toLocaleString()
}

function statusClass(status: string) {
  const base = 'inline-block rounded px-2 py-0.5 text-xs font-medium'
  if (status === 'ACTIVE') return `${base} bg-emerald-100 text-emerald-700`
  if (status === 'DRAFT' || status === 'PENDING') return `${base} bg-amber-100 text-amber-700`
  return `${base} bg-rose-100 text-rose-700`
}

async function approve(leaseId: string) {
  busyId.value = leaseId
  try {
    await leaseApi.approve(leaseId)
    notification.addToast('Lease approved', 'success')
    await load()
  } catch (err: any) {
    notification.addToast(
      err.response?.data?.message || 'Approve failed',
      'error',
    )
  } finally {
    busyId.value = null
  }
}

async function load() {
  loading.value = true
  try {
    const [leaseRes, propRes] = await Promise.all([
      leaseApi.list(0, 50),
      propertyApi.list(),
    ])
    const all = leaseRes.data.leases ?? []
    // Backend uses DRAFT for "pending application"
    applications.value = all.filter(l => l.status === 'DRAFT' || l.status === 'PENDING')
    properties.value = propRes.data
  } catch (err) {
    console.error('Applications load failed', err)
    notification.addToast('Failed to load applications', 'error')
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>