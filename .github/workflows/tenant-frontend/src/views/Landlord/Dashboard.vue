<template>
  <AppLayout>
    <div class="space-y-6">
      <div>
        <p class="text-sm uppercase tracking-[0.2em] text-slate-400">Portfolio</p>
        <h2 class="mt-2 text-3xl font-bold text-slate-900">Landlord dashboard</h2>
      </div>

      <div class="grid gap-4 md:grid-cols-4">
        <div class="card p-5">
          <p class="text-sm text-slate-500">Properties</p>
          <p class="mt-3 text-3xl font-bold text-slate-900">46</p>
        </div>
        <div class="card p-5">
          <p class="text-sm text-slate-500">Occupancy</p>
          <p class="mt-3 text-3xl font-bold text-slate-900">92%</p>
        </div>
        <div class="card p-5">
          <p class="text-sm text-slate-500">Revenue</p>
          <p class="mt-3 text-3xl font-bold text-slate-900">KSh 1.8M</p>
        </div>
        <div class="card p-5">
          <p class="text-sm text-slate-500">Applications</p>
          <p class="mt-3 text-3xl font-bold text-slate-900">12</p>
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import AppLayout from '../../components/layout/AppLayout.vue'
</script>
