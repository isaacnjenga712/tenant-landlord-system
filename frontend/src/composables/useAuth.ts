import { computed } from 'vue'
import { useAuthStore } from '../stores/auth'

export function useAuth() {
  const auth = useAuthStore()

  const isTenant = computed(() => auth.user?.role === 'TENANT')
  const isLandlord = computed(() => auth.user?.role === 'LANDLORD')
  const isAdmin = computed(() => auth.user?.role === 'ADMIN')
  const user = computed(() => auth.user)
  const role = computed(() => auth.user?.role)

  return {
    auth,
    user,
    role,
    isTenant,
    isLandlord,
    isAdmin,
    login: auth.login,
    register: auth.register,
    logout: auth.logout,
  }
}
