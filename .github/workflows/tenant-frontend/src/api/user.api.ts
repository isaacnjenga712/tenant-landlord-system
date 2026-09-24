import apiClient from './client'
import type { UserProfile } from '../types/auth'

export const userApi = {
  profile: () => apiClient.get<UserProfile>('/users/me'),
  updateProfile: (payload: Partial<UserProfile>) => apiClient.put<UserProfile>('/users/me', payload),
  list: () => apiClient.get<UserProfile[]>('/users'),
}
