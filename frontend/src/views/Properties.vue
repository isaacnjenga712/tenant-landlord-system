<template>
  <div>
    <h1 class="text-2xl font-bold mb-4">All Properties</h1>
    <PropertyFilter @filter="handleFilter" />
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mt-6">
      <PropertyCard
        v-for="property in propertyStore.properties"
        :key="property.id"
        :property="property"
        @apply="applyForProperty"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { usePropertyStore } from '@/stores/property'
import { useLeaseStore } from '@/stores/lease'
import PropertyCard from '@/components/property/PropertyCard.vue'
import PropertyFilter from '@/components/property/PropertyFilter.vue'
import { useNotificationStore } from '@/stores/notification'
import type { PropertyFilter as FilterType } from '@/types/property'

const propertyStore = usePropertyStore()
const leaseStore = useLeaseStore()
const notification = useNotificationStore()

onMounted(() => propertyStore.fetchProperties())

function handleFilter(filter: FilterType) {
  propertyStore.fetchProperties(filter)
}

async function applyForProperty(propertyId: string | number) {
  try {
    await leaseStore.applyForLease({
      propertyId: String(propertyId),
      startDate: new Date().toISOString().split('T')[0],
      endDate: new Date(Date.now() + 365 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
    })
    notification.addToast('Application submitted!', 'success')
  } catch (err: any) {
    notification.addToast(err.response?.data?.message || 'Application failed', 'error')
  }
}
</script>
