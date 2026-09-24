import { computed, onMounted } from 'vue'
import { usePropertyStore } from '../stores/property'
import type { Property } from '../types/property'

const mockProperties: Property[] = [
  {
    id: 'p-101',
    title: 'Sunset Apartments',
    location: 'Nairobi West',
    bedrooms: 2,
    bathrooms: 2,
    rent: 35000,
    status: 'AVAILABLE',
    landlordId: 'l-1',
    image: 'https://images.unsplash.com/photo-1494526585095-c41746248156?auto=format&fit=crop&w=900&q=80',
  },
  {
    id: 'p-102',
    title: 'Maple Residences',
    location: 'Kilimani',
    bedrooms: 3,
    bathrooms: 2,
    rent: 52000,
    status: 'OCCUPIED',
    landlordId: 'l-2',
    image: 'https://images.unsplash.com/photo-1484154218962-a197022b5858?auto=format&fit=crop&w=900&q=80',
  },
]

export function useProperties() {
  const propertyStore = usePropertyStore()

  const totalProperties = computed(() => propertyStore.items.length)
  const availableCount = computed(() => propertyStore.items.filter((item) => item.status === 'AVAILABLE').length)

  onMounted(() => {
    propertyStore.setProperties(mockProperties)
  })

  return {
    propertyStore,
    totalProperties,
    availableCount,
  }
}
