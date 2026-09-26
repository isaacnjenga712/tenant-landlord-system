<template>
  <AppLayout>
    <div class="space-y-6">
      <div>
        <p class="text-sm uppercase tracking-[0.2em] text-slate-400">Portfolio</p>
        <h2 class="mt-2 text-3xl font-bold text-slate-900">Current tenants</h2>
      </div>

      <div class="card overflow-hidden">
        <table class="min-w-full text-left text-sm text-slate-600">
          <thead class="bg-slate-50 text-slate-700">
            <tr>
              <th class="px-5 py-3 font-medium">Tenant</th>
              <th class="px-5 py-3 font-medium">Unit</th>
              <th class="px-5 py-3 font-medium">Rent</th>
              <th class="px-5 py-3 font-medium">Status</th>
            </tr>
          </thead>
          <tbody>
            <tr class="border-t border-slate-200">
              <td class="px-5 py-3">Mary Kimani</td>
              <td class="px-5 py-3">Unit 4A</td>
              <td class="px-5 py-3">KSh 35,000</td>
              <td class="px-5 py-3"><span class="badge bg-emerald-100 text-emerald-700">Active</span></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import AppLayout from '../../components/layout/AppLayout.vue'
</script>
