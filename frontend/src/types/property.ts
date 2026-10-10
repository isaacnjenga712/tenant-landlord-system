export type PropertyStatus = 'AVAILABLE' | 'OCCUPIED' | 'MAINTENANCE'

export interface PropertyFilter {
  city?: string
  minRent?: number
  maxRent?: number
  search?: string
  status?: 'ALL' | PropertyStatus
}

export interface Property {
  // Backend fields
  id: string
  propertyId: string
  landlordId: string
  addressLine1: string
  addressLine2?: string
  city: string
  state?: string
  zipCode?: string
  country?: string
  status: PropertyStatus
  unitIds: string[]

  // Enriched / UI fields
  title: string
  location: string
  rent: number
  bedrooms: number
  bathrooms: number
  image?: string
  images?: string[]
}

export interface Unit {
  id: string
  unitNumber: string
  propertyId: string
  bedrooms?: number
  bathrooms?: number
  squareFeet?: number
  monthlyRent?: number
  securityDeposit?: number
  status: PropertyStatus
  currentTenantId?: string | null
}