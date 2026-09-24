import apiClient from './client'

export const maintenanceApi = {
  list: () => apiClient.get('/maintenance/tickets'),
  create: (payload: { title: string; description: string; propertyId: string }) =>
    apiClient.post('/maintenance/tickets', payload),
  updateStatus: (id: string, status: string) => apiClient.patch(`/maintenance/tickets/${id}/status`, { status }),
}
