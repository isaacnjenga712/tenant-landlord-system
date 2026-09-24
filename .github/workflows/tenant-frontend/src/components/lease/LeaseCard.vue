<template>
  <div class="bg-white rounded-lg shadow p-4">
    <div class="flex justify-between items-start">
      <div>
        <h3 class="font-semibold">Lease #{{ lease.id }}</h3>
        <p class="text-sm text-gray-600">Property ID: {{ lease.propertyId }}</p>
        <p class="text-sm text-gray-600">{{ lease.startDate }} → {{ lease.endDate }}</p>
        <p class="text-blue-600 font-bold">${{ lease.rentAmount }}/month</p>
      </div>
      <span
        :class="[
          'px-2 py-1 rounded text-xs font-medium',
          lease.status === 'ACTIVE' && 'bg-green-100 text-green-800',
          lease.status === 'PENDING' && 'bg-yellow-100 text-yellow-800',
          lease.status === 'TERMINATED' && 'bg-red-100 text-red-800',
        ]"
      >
        {{ lease.status }}
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Lease } from '@/types/lease'
defineProps<{ lease: Lease }>()
</script>
