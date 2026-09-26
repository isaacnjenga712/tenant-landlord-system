import apiClient from './client'
import type {
  LoginPayload,
  RegisterPayload,
  RegisterRequest,
  UserProfile,
  UserRole,
} from '@/types/auth'

/** Actual shape returned by the backend */
export interface AuthResponse {
  id: string
  accessToken: string
  refreshToken: string
  email: string
  role: UserRole
}

/** Wrap: raw backend response → frontend-friendly shape */
export interface AuthSession {
  token: string
  refreshToken: string
  user: UserProfile
}

function toSession(raw: AuthResponse): AuthSession {
  return {
    token: raw.accessToken,
    refreshToken: raw.refreshToken,
    user: {
      id: raw.id,
      name: raw.email,
      email: raw.email,
      role: raw.role,
    },
  }
}

export const authApi = {
  login: async (payload: LoginPayload): Promise<{ data: AuthSession }> => {
    const res = await apiClient.post<AuthResponse>('/auth/login', payload)
    return { data: toSession(res.data) }
  },

  register: async (payload: RegisterPayload): Promise<{ data: AuthSession }> => {
    const body: RegisterRequest = {
      fullName: payload.name,
      email: payload.email,
      password: payload.password,
      role: payload.role,
      ...(payload.phone ? { phone: payload.phone } : {}),
    }
    const res = await apiClient.post<AuthResponse>('/auth/register', body)
    return { data: toSession(res.data) }
  },

  refresh: (refreshToken: string) =>
    apiClient.post<AuthResponse>('/auth/refresh', { refreshToken }),
}
