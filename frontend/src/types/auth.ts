export type UserRole = 'TENANT' | 'LANDLORD' | 'ADMIN'

export interface UserProfile {
  id: string
  name: string
  email: string
  role: UserRole
  phone?: string
  avatar?: string
}

export interface LoginPayload {
  email: string
  password: string
}

/** What the UI collects — friendly `name` field */
export interface RegisterPayload extends LoginPayload {
  name: string
  role: UserRole
  phone?: string
}

/** What the backend's RegisterRequest DTO expects */
export interface RegisterRequest extends LoginPayload {
  fullName: string
  role: UserRole
  phone?: string
}
