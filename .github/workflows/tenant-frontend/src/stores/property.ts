import { defineStore } from 'pinia'
import type { Property, PropertyFilter } from '../types/property'

const sampleProperties: Property[] = [
  {
    id: '1',
    title: 'Sunset Apartments',
    location: 'Nairobi West',
    city: 'Nairobi',
    bedrooms: 2,
    bathrooms: 2,
    rent: 35000,
    status: 'AVAILABLE',
    landlordId: 'landlord-1',
    image: '/placeholder.jpg',
    images: ['/placeholder.jpg'],
  },
  {
    id: '2',
    title: 'Maple Residences',
    location: 'Kilimani',
    city: 'Nairobi',
    bedrooms: 3,
    bathrooms: 2,
    rent: 42000,
    status: 'AVAILABLE',
    landlordId: 'landlord-2',
    image: '/placeholder.jpg',
    images: ['/placeholder.jpg'],
  },
]

interface PropertyStoreFilter {
  search: string
  status: 'ALL' | Property['status']
}

export const usePropertyStore = defineStore('property', {
  state: () => ({
    items: [] as Property[],
    filters: {
      search: '',
      status: 'ALL',
    } as PropertyStoreFilter,
    loading: false,
  }),
  getters: {
    properties: (state) => state.items,
    filteredItems: (state) => {
      return state.items.filter((property) => {
        const matchesSearch = property.title.toLowerCase().includes(state.filters.search.toLowerCase())
        const matchesStatus = state.filters.status === 'ALL' || property.status === state.filters.status
        return matchesSearch && matchesStatus
      })
    },
  },
  actions: {
    setProperties(properties: Property[]) {
      this.items = properties
    },
    setFilters(next: Partial<PropertyStoreFilter>) {
      this.filters = { ...this.filters, ...next }
    },
    fetchProperties(filter?: Partial<PropertyFilter | PropertyStoreFilter>) {
      if (filter) {
        this.setFilters({
          search: 'search' in filter ? String(filter.search || '') : this.filters.search,
          status: 'status' in filter ? (filter.status as PropertyStoreFilter['status']) : this.filters.status,
        })
      }

      const list = this.items.length ? this.items : sampleProperties
      const query = this.filters.search.toLowerCase()
      const filtered = list.filter((property) => {
        const matchesSearch = !query || property.title.toLowerCase().includes(query) || property.city?.toLowerCase().includes(query)
        const matchesStatus = this.filters.status === 'ALL' || property.status === this.filters.status
        return matchesSearch && matchesStatus
      })

      this.items = filtered
      return filtered
    },
    addProperty(property: Property) {
      this.items.unshift(property)
    },
  },
})