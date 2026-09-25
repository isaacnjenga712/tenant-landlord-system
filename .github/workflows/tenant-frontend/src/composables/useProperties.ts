import { computed, onMounted } from 'vue'
import { usePropertyStore } from '../stores/property'
import type { PropertyFilter } from '../types/property'

export function useProperties(filter?: PropertyFilter) {
  const propertyStore = usePropertyStore()

  const totalProperties = computed(() => propertyStore.items.length)

  const availableCount = computed(
    () => propertyStore.items.filter(item => item.status === 'AVAILABLE').length,
  )

  onMounted(() => {
    propertyStore.fetchProperties(filter)
  })

  return { propertyStore, totalProperties, availableCount }
}