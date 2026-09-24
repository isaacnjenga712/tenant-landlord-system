<template>
  <AppLayout>
    <div class="space-y-6">
      <div>
        <p class="text-sm uppercase tracking-[0.2em] text-slate-400">Payments</p>
        <h2 class="mt-2 text-3xl font-bold text-slate-900">Rent payment history</h2>
      </div>

      <PaymentHistory :payments="payments" />
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import type { Payment } from '@/api/payment.api'
import AppLayout from '../../components/layout/AppLayout.vue'
import PaymentHistory from '../../components/payment/PaymentHistory.vue'

const payments: Payment[] = [
  { id: 'pay-1', dueDate: 'Sep 2026', amount: 35000, method: 'MPESA', status: 'PAID', createdAt: '2026-09-01T00:00:00.000Z' },
  { id: 'pay-2', dueDate: 'Oct 2026', amount: 35000, method: 'MPESA', status: 'PENDING', createdAt: '2026-10-01T00:00:00.000Z' },
  { id: 'pay-3', dueDate: 'Nov 2026', amount: 35000, method: 'BANK', status: 'PAID', createdAt: '2026-11-01T00:00:00.000Z' },
]
</script>
