<template>
  <div class="bg-white rounded-lg shadow p-4">
    <div class="flex justify-between items-start">
      <div>
        <h3 class="font-semibold">Lease #{{ lease.id }}</h3>
        <p class="text-sm text-gray-600">Property ID: {{ lease.propertyId }}</p>
        <p class="text-sm text-gray-600">{{ lease.startDate }} → {{ lease.endDate }}</p>
        <p class="text-blue-600 font-bold">
          KSh {{ formatMoney(rentAmount) }}<span class="text-xs text-gray-500"> / month</span>
        </p>
      </div>
      <span :class="statusClass(lease.status)">
        {{ lease.status }}
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { Lease } from '@/types/lease'

const props = defineProps<{ lease: Lease }>()

const rentAmount = computed(
  () => props.lease.rentAmount ?? props.lease.monthlyRent ?? 0,
)

function formatMoney(n: number | string) {
  return Number(n || 0).toLocaleString()
}

function statusClass(status: string) {
  const base = 'px-2 py-1 rounded text-xs font-medium'
  if (status === 'ACTIVE') return `${base} bg-green-100 text-green-800`
  if (status === 'DRAFT') return `${base} bg-amber-100 text-amber-800`
  if (status === 'PENDING') return `${base} bg-yellow-100 text-yellow-800`
  if (status === 'TERMINATED' || status === 'CANCELLED' || status === 'EXPIRED') {
    return `${base} bg-red-100 text-red-800`
  }
  return `${base} bg-slate-100 text-slate-700`
}
</script>