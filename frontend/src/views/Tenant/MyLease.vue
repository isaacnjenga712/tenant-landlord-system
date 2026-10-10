<template>
  <AppLayout>
    <div class="space-y-6">
      <div>
        <p class="text-sm uppercase tracking-[0.2em] text-slate-400">Lease</p>
        <h2 class="mt-2 text-3xl font-bold text-slate-900">My rental agreement</h2>
      </div>

      <div v-if="loading" class="text-slate-500">Loading…</div>
      <div v-else-if="error" class="text-red-600">{{ error }}</div>

      <div v-else-if="!lease" class="card p-5">
        <h3 class="text-lg font-semibold text-slate-900">No active lease</h3>
        <p class="mt-2 text-sm text-slate-600">
          You don't have a lease yet. Apply for a property to get started.
        </p>
      </div>

      <LeaseCard v-else :lease="lease" />
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import type { Lease } from '@/types/lease'
import AppLayout from '../../components/layout/AppLayout.vue'
import LeaseCard from '../../components/lease/LeaseCard.vue'
import { leaseApi } from '../../api/lease.api'

const loading = ref(true)
const error = ref('')
const lease = ref<Lease | null>(null)

async function loadLease() {
  loading.value = true
  error.value = ''
  try {
    const { data } = await leaseApi.list()
    const leases: Lease[] = data?.leases ?? []
    lease.value =
      leases.find(l => l.status === 'ACTIVE') ??
      leases.find(l => l.status === 'PENDING') ??
      leases[0] ??
      null
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Failed to load lease'
  } finally {
    loading.value = false
  }
}

onMounted(loadLease)
</script>