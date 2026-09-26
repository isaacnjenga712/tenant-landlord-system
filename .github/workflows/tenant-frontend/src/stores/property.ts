import { defineStore } from 'pinia'
import { propertyApi } from '../api/property.api'
import { unitApi } from '../api/unit.api'
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

    async createProperty(payload: {
      addressLine1: string
      addressLine2?: string | null
      city: string
      state?: string | null
      zipCode?: string | null
      country?: string | null
      status?: PropertyStatus
    }) {
      this.loading = true
      this.error = ''
      try {
        const { data } = await propertyApi.create(payload)
        await this.fetchProperties()
        return data
      } catch (err: any) {
        this.error = err.response?.data?.message || 'Failed to create property'
        throw err
      } finally {
        this.loading = false
      }
    },

    async createUnit(payload: {
      propertyId: string
      bedrooms: number
      bathrooms: number
      monthlyRent: number
      status?: 'AVAILABLE' | 'OCCUPIED' | 'MAINTENANCE'
    }) {
      const { data } = await unitApi.create(payload)
      await this.fetchProperties()
      return data
    },
  },
})