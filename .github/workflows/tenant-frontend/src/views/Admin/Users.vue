<template>
  <AppLayout>
    <div class="space-y-6">
      <div>
        <p class="text-sm uppercase tracking-[0.2em] text-slate-400">Control center</p>
        <h2 class="mt-2 text-3xl font-bold text-slate-900">User accounts</h2>
      </div>

      <div class="card overflow-hidden">
        <table class="min-w-full text-left text-sm text-slate-600">
          <thead class="bg-slate-50 text-slate-700">
            <tr>
              <th class="px-5 py-3 font-medium">Name</th>
              <th class="px-5 py-3 font-medium">Role</th>
              <th class="px-5 py-3 font-medium">Email</th>
              <th class="px-5 py-3 font-medium">Status</th>
            </tr>
          </thead>
          <tbody>
            <tr class="border-t border-slate-200">
              <td class="px-5 py-3">Mary Tenant</td>
              <td class="px-5 py-3">TENANT</td>
              <td class="px-5 py-3">mary@example.com</td>
              <td class="px-5 py-3"><span class="badge bg-emerald-100 text-emerald-700">Active</span></td>
            </tr>
            <tr class="border-t border-slate-200">
              <td class="px-5 py-3">Peter Landlord</td>
              <td class="px-5 py-3">LANDLORD</td>
              <td class="px-5 py-3">peter@example.com</td>
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
