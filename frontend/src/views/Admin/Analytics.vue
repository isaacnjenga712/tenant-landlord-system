<template>
  <AppLayout>
    <div class="space-y-6">
      <div>
        <p class="text-sm uppercase tracking-[0.2em] text-slate-400">Monitoring</p>
        <h2 class="mt-2 text-3xl font-bold text-slate-900">System analytics</h2>
      </div>

      <div class="card overflow-hidden">
        <iframe
          class="h-[72vh] w-full bg-white"
          src="http://localhost:3000/d/tenant-overview/tenant-overview?orgId=1&kiosk"
          title="Grafana analytics"
        />
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import AppLayout from '../../components/layout/AppLayout.vue'
</script>
