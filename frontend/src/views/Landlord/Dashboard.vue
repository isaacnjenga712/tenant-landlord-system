<template>
  <AppLayout>
    <div class="space-y-6">
      <div>
        <p class="text-sm uppercase tracking-[0.2em] text-slate-400">Portfolio</p>
        <h2 class="mt-2 text-3xl font-bold text-slate-900">Landlord dashboard</h2>
      </div>

      <div v-if="loading" class="text-slate-500">Loading…</div>

      <div v-else class="grid gap-4 md:grid-cols-4">
        <div class="card p-5">
          <p class="text-sm text-slate-500">Properties</p>
          <p class="mt-3 text-3xl font-bold text-slate-900">{{ propertyCount }}</p>
        </div>
        <div class="card p-5">
          <p class="text-sm text-slate-500">Occupancy</p>
          <p class="mt-3 text-3xl font-bold text-slate-900">{{ occupancyPct }}%</p>
        </div>
        <div class="card p-5">
          <p class="text-sm text-slate-500">Monthly revenue</p>
          <p class="mt-3 text-3xl font-bold text-slate-900">KSh {{ formatMoney(monthlyRevenue) }}</p>
        </div>
        <div class="card p-5">
          <p class="text-sm text-slate-500">Applications</p>
          <p class="mt-3 text-3xl font-bold text-slate-900">{{ applicationCount }}</p>
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import AppLayout from '../../components/layout/AppLayout.vue'
import { propertyApi } from '../../api/property.api'
import { leaseApi } from '../../api/lease.api'
import { unitApi } from '../../api/unit.api'
import type { Property, Unit } from '../../types/property'
import type { Lease } from '../../types/lease'

const properties = ref<Property[]>([])
const units = ref<Unit[]>([])
const leases = ref<Lease[]>([])
const loading = ref(true)

const propertyCount = computed(() => properties.value.length)

const occupancyPct = computed(() => {
  if (!units.value.length) return 0
  const occupied = units.value.filter(u => u.status === 'OCCUPIED').length
  return Math.round((occupied / units.value.length) * 100)
})

const monthlyRevenue = computed(() =>
  units.value
    .filter(u => u.status === 'OCCUPIED')
    .reduce((sum, u) => sum + (u.monthlyRent ?? 0), 0),
)

const applicationCount = computed(
  () => leases.value.filter(l => l.status === 'PENDING').length,
)

function formatMoney(n: number) {
  if (n >= 1_000_000) return `${(n / 1_000_000).toFixed(1)}M`
  if (n >= 1_000) return `${(n / 1_000).toFixed(1)}K`
  return n.toString()
}

onMounted(async () => {
  try {
    const [propRes, leaseRes] = await Promise.all([
      propertyApi.list(),
      leaseApi.list(),
    ])
    properties.value = propRes.data
    leases.value = leaseRes.data.leases

    // Fetch units per property using the authenticated axios client
    const unitArrays = await Promise.all(
      properties.value.map(p =>
        unitApi.byProperty(p.id).then(r => r.data).catch(() => []),
      ),
    )
    units.value = unitArrays.flat()
  } catch (err) {
    console.error('Dashboard load failed', err)
  } finally {
    loading.value = false
  }
})
</script>