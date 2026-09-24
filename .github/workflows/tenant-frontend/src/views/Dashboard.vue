<template>
  <AppLayout>
    <div class="space-y-6">
      <div class="flex items-center justify-between">
        <div>
          <p class="text-sm uppercase tracking-[0.2em] text-slate-400">Overview</p>
          <h2 class="mt-2 text-3xl font-bold text-slate-900">{{ auth.user?.role ? `${auth.user.role.toLowerCase()} workspace` : 'Dashboard' }}</h2>
        </div>
      </div>

      <div class="grid gap-4 md:grid-cols-3">
        <div class="card p-5">
          <p class="text-sm text-slate-500">Properties</p>
          <p class="mt-3 text-3xl font-bold text-slate-900">{{ totalProperties }}</p>
        </div>
        <div class="card p-5">
          <p class="text-sm text-slate-500">Occupancy</p>
          <p class="mt-3 text-3xl font-bold text-slate-900">86%</p>
        </div>
        <div class="card p-5">
          <p class="text-sm text-slate-500">Revenue</p>
          <p class="mt-3 text-3xl font-bold text-slate-900">KSh 1.2M</p>
        </div>
      </div>

      <div class="grid gap-6 lg:grid-cols-2">
        <div class="card p-5">
          <h3 class="text-lg font-semibold text-slate-900">Upcoming tasks</h3>
          <ul class="mt-4 space-y-3 text-sm text-slate-600">
            <li>• Tenant verification for 3 pending applications</li>
            <li>• Water bill reconciliation for NAI 10</li>
            <li>• Maintenance resolution for Flat 4C</li>
          </ul>
        </div>

        <div class="card p-5">
          <h3 class="text-lg font-semibold text-slate-900">System status</h3>
          <ul class="mt-4 space-y-3 text-sm text-slate-600">
            <li>• Gateway service healthy</li>
            <li>• M-Pesa integration online</li>
            <li>• Eureka registry connected</li>
          </ul>
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import AppLayout from '../components/layout/AppLayout.vue'
import { useAuthStore } from '../stores/auth'
import { useProperties } from '../composables/useProperties'

const auth = useAuthStore()
const { totalProperties } = useProperties()
</script>
