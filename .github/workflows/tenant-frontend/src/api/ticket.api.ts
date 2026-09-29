import apiClient from './client'

export const maintenanceApi = {
  list: () => apiClient.get('/maintenance/tickets'),
  create: (payload: { title: string; description: string; propertyId: string }) =>
    apiClient.post('/maintenance/tickets', payload),
  updateStatus: (id: string, status: string) => apiClient.patch(`/maintenance/tickets/${id}/status`, { status }),
}
import apiClient from './client'

export type TicketPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT'
export type TicketStatus = 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED'

export interface Ticket {
  id: string
  unitId: string
  tenantId: string
  landlordId: string
  title: string
  description?: string
  priority: TicketPriority
  status: TicketStatus
  createdAt: string
  updatedAt: string
}

export interface TicketCreatePayload {
  unitId: string
  title: string
  description?: string
  priority: TicketPriority
  landlordId?: string   // required when caller is TENANT
  tenantId?: string     // required when caller is LANDLORD
}

export interface TicketUpdatePayload {
  title?: string
  description?: string
  priority?: TicketPriority
  status?: TicketStatus
}

export const ticketApi = {
  list: () => apiClient.get<Ticket[]>('/tickets'),

  get: (id: string) => apiClient.get<Ticket>(`/tickets/${id}`),

  create: (payload: TicketCreatePayload) =>
    apiClient.post<Ticket>('/tickets', payload),

  update: (id: string, payload: TicketUpdatePayload) =>
    apiClient.put<Ticket>(`/tickets/${id}`, payload),

  updateStatus: (id: string, status: TicketStatus) =>
    apiClient.patch<Ticket>(`/tickets/${id}/status`, null, {
      params: { status },
    }),

  remove: (id: string) => apiClient.delete(`/tickets/${id}`),

  byUnit: (unitId: string) => apiClient.get<Ticket[]>(`/tickets/unit/${unitId}`),

  byTenant: (tenantId: string) =>
    apiClient.get<Ticket[]>(`/tickets/tenant/${tenantId}`),

  byLandlord: (landlordId: string) =>
    apiClient.get<Ticket[]>(`/tickets/landlord/${landlordId}`),
}