<template>
  <AppLayout>
    <div class="space-y-6">
      <div>
        <p class="text-sm uppercase tracking-[0.2em] text-slate-400">Browse</p>
        <h2 class="mt-2 text-3xl font-bold text-slate-900">Available homes</h2>
      </div>

      <p style="background: yellow; padding: 10px; font-family: monospace;">
        loading: {{ loading }} | error: "{{ error }}" | count: {{ properties.length }}
      </p>

      <div v-if="loading">Loading…</div>
      <div v-else-if="error" style="color: red;">{{ error }}</div>
      <div v-else-if="!properties.length">No properties.</div>
      <div v-else>
        <div v-for="p in properties" :key="p.id"
             style="border: 2px solid green; padding: 10px; margin: 10px 0;">
          <div><b>{{ p.addressLine1 }}</b>, {{ p.city }}</div>
          <div>Status: {{ p.status }}</div>
          <div>Property ID: {{ p.propertyId }}</div>
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import AppLayout from '../../components/layout/AppLayout.vue'
import apiClient from '../../api/client'

const properties = ref<any[]>([])
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  console.log('=== DIRECT FETCH START ===')
  try {
    const { data } = await apiClient.get('/properties')
    console.log('=== DIRECT FETCH OK, count:', data.length, data)
    properties.value = data
  } catch (err: any) {
    console.error('=== DIRECT FETCH FAILED', err)
    error.value = err.message || 'fetch failed'
  } finally {
    loading.value = false
    console.log('=== DIRECT FETCH DONE, count:', properties.value.length)
  }
})
</script>