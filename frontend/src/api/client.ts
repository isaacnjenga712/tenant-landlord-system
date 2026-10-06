import axios from 'axios'
import { useAuthStore } from '../stores/auth'

const TENANT_ID = import.meta.env.VITE_TENANT_ID || 'default'

const apiClient = axios.create({
  baseURL: (import.meta.env.VITE_API_URL || 'http://localhost:8080') + '/api/v1',
  timeout: 15000,
  headers: {
    'X-Tenant-ID': TENANT_ID,
  },
})

apiClient.interceptors.request.use((config) => {
  const authStore = useAuthStore()
  if (authStore.token) {
    config.headers.Authorization = `Bearer ${authStore.token}`
  }
  return config
})

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      useAuthStore().logout()
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  },
)

export default apiClient