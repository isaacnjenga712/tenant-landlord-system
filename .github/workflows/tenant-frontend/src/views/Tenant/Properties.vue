<template>
  <AppLayout>
    <div class="space-y-6">
      <div>
        <p class="text-sm uppercase tracking-[0.2em] text-slate-400">Browse</p>
        <h2 class="mt-2 text-3xl font-bold text-slate-900">Available homes</h2>
      </div>

      <div class="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
        <PropertyCard v-for="property in propertyStore.filteredItems" :key="property.id" :property="property" />
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import AppLayout from '../../components/layout/AppLayout.vue'
import PropertyCard from '../../components/property/PropertyCard.vue'
import { useProperties } from '../../composables/useProperties'

const { propertyStore } = useProperties()
</script>
