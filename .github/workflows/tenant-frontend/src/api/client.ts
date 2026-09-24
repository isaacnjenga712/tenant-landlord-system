import axios from 'axios'
import { useAuthStore } from '../stores/auth'

const apiClient = axios.create({
  baseURL: (import.meta.env.VITE_API_URL || 'http://localhost:8080') + '/api/v1',
  timeout: 15000,
})

// Attach JWT to every request
apiClient.interceptors.request.use((config) => {
  const authStore = useAuthStore()

  if (authStore.token) {
    config.headers.Authorization = `Bearer ${authStore.token}`
  }

  return config
})

// Handle 401 → logout (or refresh, if you add refresh logic later)
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      useAuthStore().logout()
      // Redirect to login if not already there
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  },
)

export default apiClient