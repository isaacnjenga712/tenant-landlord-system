import { defineStore } from 'pinia'
import type { LoginPayload, RegisterPayload, UserProfile, UserRole } from '../types/auth'
import { authApi } from '../api/auth.api'

interface AuthState {
  token: string
  user: UserProfile | null
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: localStorage.getItem('tenant_token') || '',
    user: localStorage.getItem('tenant_user') ? JSON.parse(localStorage.getItem('tenant_user') || '{}') : null,
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token),
    isTenant: (state) => state.user?.role === 'TENANT',
    isLandlord: (state) => state.user?.role === 'LANDLORD',
    isAdmin: (state) => state.user?.role === 'ADMIN',
    role: (state) => state.user?.role as UserRole | undefined,
  },
  actions: {
    setSession(token: string, user: UserProfile) {
      this.token = token
      this.user = user
      localStorage.setItem('tenant_token', token)
      localStorage.setItem('tenant_user', JSON.stringify(user))
    },
    async login(payload: LoginPayload) {
      const { data } = await authApi.login(payload)
      this.setSession(data.token, data.user)
      return data
    },
    async register(payload: RegisterPayload) {
      const { data } = await authApi.register(payload)
      this.setSession(data.token, data.user)
      return data
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('tenant_token')
      localStorage.removeItem('tenant_user')
    },
  },
})