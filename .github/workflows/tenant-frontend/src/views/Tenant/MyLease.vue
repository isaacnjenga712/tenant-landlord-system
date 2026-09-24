<template>
  <AppLayout>
    <div class="space-y-6">
      <div>
        <p class="text-sm uppercase tracking-[0.2em] text-slate-400">Lease</p>
        <h2 class="mt-2 text-3xl font-bold text-slate-900">My rental agreement</h2>
      </div>

      <LeaseCard :lease="lease" />
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
