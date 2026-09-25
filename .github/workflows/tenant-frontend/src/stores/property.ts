import { defineStore } from 'pinia'
import { propertyApi } from '../api/property.api'
import type { Property, PropertyFilter, PropertyStatus } from '../types/property'

interface PropertyStoreFilter {
  search: string
  status: 'ALL' | PropertyStatus
}

export const usePropertyStore = defineStore('property', {
  state: () => ({
    items: [] as Property[],
    filters: {
      search: '',
      status: 'ALL',
    } as PropertyStoreFilter,
    loading: false,
    error: '' as string,
  }),

  getters: {
    properties: (state) => state.items,

    filteredItems: (state) => {
      const search = (state.filters?.search ?? '').toLowerCase()
      const status = state.filters?.status ?? 'ALL'

      return state.items.filter(property => {
        if (search) {
          const title = (property.title ?? '').toLowerCase()
          const city = (property.city ?? '').toLowerCase()
          if (!title.includes(search) && !city.includes(search)) return false
        }
        if (status !== 'ALL' && property.status !== status) return false
        return true
      })
    },
  },

  actions: {
    setFilters(next: Partial<PropertyStoreFilter>) {
      this.filters = { ...this.filters, ...next }
    },

    async fetchProperties(filter?: PropertyFilter) {
      this.loading = true
      this.error = ''
      try {
        const { data } = await propertyApi.list(filter)
        this.items = data
      } catch (err: any) {
        this.error = err.response?.data?.message || 'Failed to load properties'
        console.error('fetchProperties failed', err)
      } finally {
        this.loading = false
      }
    },

    addProperty(property: Property) {
      this.items.unshift(property)
    },
  },
})