<template>
  <AppLayout>
    <div class="space-y-6">
      <div>
        <p class="text-sm uppercase tracking-[0.2em] text-slate-400">Approvals</p>
        <h2 class="mt-2 text-3xl font-bold text-slate-900">Tenant applications</h2>
      </div>

      <div class="card overflow-hidden">
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
            <tr class="border-t border-slate-200">
              <td class="px-5 py-3">Mary Kimani</td>
              <td class="px-5 py-3">Sunset Apartments</td>
              <td class="px-5 py-3"><span class="badge bg-amber-100 text-amber-700">Pending</span></td>
              <td class="px-5 py-3"><BaseButton variant="secondary">Approve</BaseButton></td>
            </tr>
            <tr class="border-t border-slate-200">
              <td class="px-5 py-3">David Otieno</td>
              <td class="px-5 py-3">Maple Residences</td>
              <td class="px-5 py-3"><span class="badge bg-emerald-100 text-emerald-700">Approved</span></td>
              <td class="px-5 py-3"><BaseButton variant="ghost">Details</BaseButton></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import AppLayout from '../../components/layout/AppLayout.vue'
import BaseButton from '../../components/common/BaseButton.vue'
</script>
