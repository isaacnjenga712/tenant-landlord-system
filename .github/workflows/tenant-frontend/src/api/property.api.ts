import apiClient from './client'
import type { Property, PropertyFilter, Unit } from '../types/property'

interface BackendProperty {
  id: string
  propertyId: string
  landlordId: string
  addressLine1: string
  addressLine2?: string | null
  city: string
  state?: string | null
  zipCode?: string | null
  country?: string | null
  status: 'AVAILABLE' | 'OCCUPIED' | 'MAINTENANCE'
  unitIds: string[]
}

function toView(p: BackendProperty, firstUnit?: Unit): Property {
  return {
    id: p.id,
    propertyId: p.propertyId,
    landlordId: p.landlordId,
    addressLine1: p.addressLine1,
    addressLine2: p.addressLine2 ?? undefined,
    city: p.city,
    state: p.state ?? undefined,
    zipCode: p.zipCode ?? undefined,
    country: p.country ?? undefined,
    status: p.status,
    unitIds: p.unitIds ?? [],

    title: p.addressLine1,
    location: `${p.addressLine1}${p.city ? ', ' + p.city : ''}`,
    rent: firstUnit?.monthlyRent ?? 0,
    bedrooms: firstUnit?.bedrooms ?? 0,
    bathrooms: firstUnit?.bathrooms ?? 0,
    image: undefined,
    images: undefined,
  }
}

async function unitsFor(propertyId: string): Promise<Unit[]> {
  try {
    const { data } = await apiClient.get<Unit[]>(`/units/property/${propertyId}`)
    return data
  } catch {
    return []
  }
}

export const propertyApi = {
  list: async (filter?: PropertyFilter) => {
    const { data: raw } = await apiClient.get<BackendProperty[]>('/properties', {
      params: filter,
    })
    const enriched = await Promise.all(
      raw.map(async p => toView(p, (await unitsFor(p.id))[0])),
    )
    return { data: enriched }
  },

  get: async (id: string) => {
    const { data: raw } = await apiClient.get<BackendProperty>(`/properties/${id}`)
    const units = await unitsFor(id)
    return { data: toView(raw, units[0]) }
  },

  create: (payload: Partial<BackendProperty>) =>
    apiClient.post<BackendProperty>('/properties', payload),

  update: (id: string, payload: Partial<BackendProperty>) =>
    apiClient.put<BackendProperty>(`/properties/${id}`, payload),

  remove: (id: string) => apiClient.delete(`/properties/${id}`),
}