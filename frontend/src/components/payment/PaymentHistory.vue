<template>
  <div class="overflow-x-auto">
    <table class="min-w-full bg-white rounded-lg shadow">
      <thead>
        <tr class="bg-gray-100 text-left">
          <th class="p-3">Date</th>
          <th class="p-3">Amount</th>
          <th class="p-3">Method</th>
          <th class="p-3">Status</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="payment in payments" :key="payment.id" class="border-t">
          <td class="p-3">{{ new Date(payment.createdAt).toLocaleDateString() }}</td>
          <td class="p-3">${{ payment.amount }}</td>
          <td class="p-3">{{ payment.method }}</td>
          <td class="p-3">
            <span
              :class="[
                'px-2 py-1 rounded text-xs',
                (payment.status === 'COMPLETED' || payment.status === 'PAID') && 'bg-green-100 text-green-800',
                payment.status === 'PENDING' && 'bg-yellow-100 text-yellow-800',
                payment.status === 'FAILED' && 'bg-red-100 text-red-800',
              ]"
            >
              {{ payment.status }}
            </span>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
import type { Payment } from '@/api/payment.api'
defineProps<{ payments: Payment[] }>()
</script>