import apiClient from './client'
import type { Property } from '../types/property'

export const propertyApi = {
  list: () => apiClient.get<Property[]>('/properties'),
  get: (id: string) => apiClient.get<Property>(`/properties/${id}`),
  create: (payload: Partial<Property>) => apiClient.post<Property>('/properties', payload),
  update: (id: string, payload: Partial<Property>) => apiClient.put<Property>(`/properties/${id}`, payload),
  remove: (id: string) => apiClient.delete(`/properties/${id}`),
}