<template>
  <AppLayout>
    <div class="space-y-6">
      <div>
        <p class="text-sm uppercase tracking-[0.2em] text-slate-400">Browse</p>
        <h2 class="mt-2 text-3xl font-bold text-slate-900">Available homes</h2>
      </div>

      <div v-if="loading" class="text-slate-500">Loading…</div>
      <div v-else-if="error" class="text-red-600">{{ error }}</div>
      <div v-else-if="!properties.length" class="text-slate-500">No properties.</div>

      <div v-else class="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
        <PropertyCard
          v-for="p in properties"
          :key="p.id"
          :property="p"
          @apply="handleApply"
        />
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AppLayout from '../../components/layout/AppLayout.vue'
import PropertyCard from '../../components/property/PropertyCard.vue'
import { propertyApi } from '../../api/property.api'
import { leaseApi } from '../../api/lease.api'
import { useAuthStore } from '../../stores/auth'
import { useNotificationStore } from '../../stores/notification'
import type { Property } from '../../types/property'

const router = useRouter()
const auth = useAuthStore()
const notification = useNotificationStore()

const properties = ref<Property[]>([])
const loading = ref(true)
const error = ref('')
const submitting = ref(false)

function isoDaysFromNow(days: number) {
  const d = new Date()
  d.setDate(d.getDate() + days)
  return d.toISOString().split('T')[0]
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const { data } = await propertyApi.list()
    properties.value = data
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Failed to load properties'
  } finally {
    loading.value = false
  }
}

async function handleApply(propertyId: string) {
  if (submitting.value) return

  const property = properties.value.find(p => p.id === propertyId)
  if (!property) return

  const tenantId = auth.user?.id
  if (!tenantId) {
    notification.addToast('You must be logged in to apply', 'error')
    return
  }
  if (!property.landlordId) {
    notification.addToast('Property has no landlord assigned', 'error')
    return
  }

  submitting.value = true
  try {
    await leaseApi.create({
      propertyId: property.id,
      tenantId,
      landlordId: property.landlordId,
      startDate: isoDaysFromNow(0),
      endDate: isoDaysFromNow(365),
      rentAmount: property.rent ?? 0,
      depositAmount: (property.rent ?? 0) * 2,
    })

    notification.addToast('Application submitted', 'success')
    router.push('/tenant/my-lease')
  } catch (err: any) {
    notification.addToast(
      err.response?.data?.message || 'Failed to apply for lease',
      'error',
    )
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>