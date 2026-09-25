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
              <td class="px-5 py-3">{{ app.tenantId }}</td>
              <td class="px-5 py-3">{{ propertyTitle(app.propertyId) }}</td>
              <td class="px-5 py-3">
                <span
                  :class="[
                    'badge',
                    app.status === 'PENDING' && 'bg-amber-100 text-amber-700',
                    app.status === 'ACTIVE' && 'bg-emerald-100 text-emerald-700',
                    app.status === 'TERMINATED' && 'bg-rose-100 text-rose-700',
                  ]"
                >
                  {{ app.status }}
                </span>
              </td>
              <td class="px-5 py-3">
                <BaseButton
                  v-if="app.status === 'PENDING'"
                  variant="secondary"
                  @click="approve(app.id)"
                >
                  Approve
                </BaseButton>
                <BaseButton v-else variant="ghost">Details</BaseButton>
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
import BaseButton from '../../components/common/BaseButton.vue'
import { leaseApi } from '../../api/lease.api'
import { propertyApi } from '../../api/property.api'
import { useNotificationStore } from '../../stores/notification'
import type { Lease } from '../../types/lease'
import type { Property } from '../../types/property'

const applications = ref<Lease[]>([])
const properties = ref<Property[]>([])
const loading = ref(true)
const notification = useNotificationStore()

function propertyTitle(propertyId: string): string {
  const p = properties.value.find(x => x.id === propertyId)
  return p ? `${p.addressLine1}, ${p.city}` : propertyId.slice(0, 8)
}

async function approve(leaseId: string) {
  try {
    // NOTE: backend doesn't yet have an /approve endpoint.
    // Uncomment the line below once you add it:
    // await leaseApi.approve(leaseId)
    notification.addToast('Approval endpoint not yet implemented', 'info')
  } catch (err: any) {
    notification.addToast(err.response?.data?.message || 'Approve failed', 'error')
  }
}

onMounted(async () => {
  try {
    const [leaseRes, propRes] = await Promise.all([
      leaseApi.list(),
      propertyApi.list(),
    ])
    applications.value = leaseRes.data.leases.filter(l => l.status === 'PENDING')
    properties.value = propRes.data
  } catch (err) {
    console.error('Applications load failed', err)
  } finally {
    loading.value = false
  }
})
</script>