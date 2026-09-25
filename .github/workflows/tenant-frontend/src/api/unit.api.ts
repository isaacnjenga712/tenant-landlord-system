import apiClient from './client'
import type { Unit } from '../types/property'

export const unitApi = {
  list: () => apiClient.get<Unit[]>('/units'),
  get: (id: string) => apiClient.get<Unit>(`/units/${id}`),
  byProperty: (propertyId: string) =>
    apiClient.get<Unit[]>(`/units/property/${propertyId}`),
  create: (payload: Partial<Unit>) => apiClient.post<Unit>('/units', payload),
  update: (id: string, payload: Partial<Unit>) =>
    apiClient.put<Unit>(`/units/${id}`, payload),
  remove: (id: string) => apiClient.delete(`/units/${id}`),
}