<template>
  <AppLayout>
    <div class="space-y-6">
      <div>
        <p class="text-sm uppercase tracking-[0.2em] text-slate-400">Portfolio</p>
        <h2 class="mt-2 text-3xl font-bold text-slate-900">Current tenants</h2>
        <p class="mt-1 text-sm text-slate-500">
          Active leases and the tenants on them.
        </p>
      </div>

      <div v-if="loading" class="text-slate-500">Loading…</div>
      <div v-else-if="!rows.length" class="card p-6 text-slate-500">
        No active tenants yet.
      </div>
      <div v-else class="card overflow-hidden">
        <table class="min-w-full text-left text-sm">
          <thead class="bg-slate-50 text-slate-700">
            <tr>
              <th class="px-5 py-3 font-medium">Tenant</th>
              <th class="px-5 py-3 font-medium">Property</th>
              <th class="px-5 py-3 font-medium">Unit</th>
              <th class="px-5 py-3 font-medium">Lease period</th>
              <th class="px-5 py-3 font-medium">Rent</th>
              <th class="px-5 py-3 font-medium">Status</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in rows" :key="r.leaseId" class="border-t border-slate-200">
              <td class="px-5 py-3">
                <div class="flex items-center gap-2">
                  <div class="w-8 h-8 rounded-full bg-slate-200 flex items-center justify-center text-xs font-semibold text-slate-600">
                    {{ initials(r.tenantId) }}
                  </div>
                  <span class="font-mono text-xs text-slate-600">
                    {{ r.tenantId.slice(0, 8) }}…
                  </span>
                </div>
              </td>
              <td class="px-5 py-3">{{ r.propertyTitle }}</td>
              <td class="px-5 py-3">{{ r.unitLabel }}</td>
              <td class="px-5 py-3 text-xs text-slate-500">
                {{ r.startDate }} → {{ r.endDate }}
              </td>
              <td class="px-5 py-3 font-medium text-slate-900">
                KSh {{ formatMoney(r.rentAmount) }}
              </td>
              <td class="px-5 py-3">
                <span class="rounded px-2 py-0.5 text-xs font-medium bg-emerald-100 text-emerald-700">
                  {{ r.status }}
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
import { leaseApi } from '../../api/lease.api'
import type { Lease } from '../../types/lease'
import { propertyApi } from '../../api/property.api'
import { unitApi } from '../../api/unit.api'
import { useNotificationStore } from '../../stores/notification.ts'

interface TenantRow {
  leaseId: string
  tenantId: string
  propertyTitle: string
  unitLabel: string
  startDate: string
  endDate: string
  rentAmount: number
  status: string
}

const notification = useNotificationStore()
const rows = ref<TenantRow[]>([])
const loading = ref(true)

function formatMoney(n: number | undefined) { return Number(n || 0).toLocaleString() }

function initials(id: string): string {
  // Simple visual placeholder since we don't have tenant names yet
  return id.slice(0, 2).toUpperCase()
}

async function load() {
  loading.value = true
  try {
    // 1. Landlord's leases (auto-scoped by X-User-Id from JWT)
    const { data: leaseRes } = await leaseApi.list(0, 100)
    const leases = (leaseRes.leases ?? []).filter(l => l.status === 'ACTIVE')

    if (!leases.length) {
      rows.value = []
      return
    }

    // 2. Landlord's properties (for title lookup)
    const { data: properties } = await propertyApi.list()

    // 3. Units per property (for unit label)
    const unitMap: Record<string, string> = {}
    await Promise.all(
      properties.map(async p => {
        try {
          const { data: units } = await unitApi.byProperty(p.id)
          units.forEach(u => {
            unitMap[u.id] = (u as any).unitNumber
              ? `Unit ${(u as any).unitNumber}`
              : `Unit ${u.id.slice(0, 6)}`
          })
        } catch {
          // no units
        }
      }),
    )

    // 4. Build rows
    rows.value = leases.map((l: Lease) => {
      const p = properties.find(p => p.id === l.propertyId)
      return {
        leaseId: l.id,
        tenantId: l.tenantId,
        propertyTitle: p ? `${p.addressLine1}, ${p.city}` : 'Unknown property',
        unitLabel: '—', // we don't know which unit yet (lease isn't tied to a unit)
        startDate: l.startDate,
        endDate: l.endDate,
        rentAmount: (l as any).rentAmount ?? (l as any).monthlyRent ?? 0,
        status: l.status,
      }
    })
  } catch (err) {
    console.error('tenants load failed', err)
    notification.addToast('Failed to load tenants', 'error')
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>