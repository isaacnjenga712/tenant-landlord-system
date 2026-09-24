<template>
  <AppLayout>
    <div class="space-y-6">
      <div>
        <p class="text-sm uppercase tracking-[0.2em] text-slate-400">Tenant view</p>
        <h2 class="mt-2 text-3xl font-bold text-slate-900">My home overview</h2>
      </div>

      <div class="grid gap-4 md:grid-cols-3">
        <div class="card p-5">
          <p class="text-sm text-slate-500">Current rent</p>
          <p class="mt-3 text-3xl font-bold text-slate-900">KSh 35,000</p>
        </div>
        <div class="card p-5">
          <p class="text-sm text-slate-500">Due date</p>
          <p class="mt-3 text-3xl font-bold text-slate-900">26 Sep</p>
        </div>
        <div class="card p-5">
          <p class="text-sm text-slate-500">Maintenance tickets</p>
          <p class="mt-3 text-3xl font-bold text-slate-900">2 open</p>
        </div>
      </div>

      <div class="grid gap-6 lg:grid-cols-2">
        <LeaseCard :lease="lease" />
        <div class="card p-5">
          <h3 class="text-lg font-semibold text-slate-900">Service updates</h3>
          <ul class="mt-4 space-y-3 text-sm text-slate-600">
            <li>• Water and security services are active.</li>
            <li>• Your notices are synced with the landlord.</li>
            <li>• M-Pesa payment links are available for prompt payment.</li>
          </ul>
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import type { Lease } from '@/types/lease'
import AppLayout from '../../components/layout/AppLayout.vue'
import LeaseCard from '../../components/lease/LeaseCard.vue'

const lease: Lease = {
  id: 'lease-001',
  propertyId: 'Sunset Apartments · Unit 4A',
  tenantId: 'tenant-01',
  landlordId: 'landlord-01',
  monthlyRent: 35000,
  rentAmount: 35000,
  startDate: '2024-01-01',
  endDate: '2026-12-31',
  status: 'ACTIVE',
}
</script>
