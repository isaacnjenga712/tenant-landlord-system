<template>
  <AppLayout>
    <div class="space-y-6">
      <div class="flex items-center justify-between">
        <div>
          <p class="text-sm uppercase tracking-[0.2em] text-slate-400">Portfolio</p>
          <h2 class="mt-2 text-3xl font-bold text-slate-900">My properties</h2>
        </div>
      </div>

      <PropertyForm ref="formRef" @submit="handleSubmit" />

      <div v-if="propertyStore.loading" class="text-slate-500">Loading…</div>
      <div v-else-if="!propertyStore.items.length" class="text-slate-500">
        No properties yet. Add one above.
      </div>
      <div v-else class="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
        <PropertyCard
          v-for="property in propertyStore.items"
          :key="property.id"
          :property="property"
        />
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import AppLayout from '../../components/layout/AppLayout.vue'
import PropertyCard from '../../components/property/PropertyCard.vue'
import PropertyForm, { type PropertyFormPayload } from '../../components/property/PropertyForm.vue'
import { useProperties } from '../../composables/useProperties'
import { useNotificationStore } from '../../stores/notification'

const { propertyStore } = useProperties()
const notification = useNotificationStore()
const formRef = ref<InstanceType<typeof PropertyForm> | null>(null)

async function handleSubmit(payload: PropertyFormPayload) {
  try {
    const property = await propertyStore.createProperty(payload.property)
    await propertyStore.createUnit({
      propertyId: property.id,
      ...payload.unit,
    })
    notification.addToast('Property and unit created', 'success')
    formRef.value?.done()
  } catch (err: any) {
    const msg = err?.response?.data?.message || 'Failed to create property'
    notification.addToast(msg, 'error')
    formRef.value?.fail(msg)
  }
}
</script>