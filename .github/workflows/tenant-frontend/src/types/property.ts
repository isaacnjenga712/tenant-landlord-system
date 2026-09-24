export type PropertyStatus = 'AVAILABLE' | 'OCCUPIED' | 'MAINTENANCE'

export interface PropertyFilter {
  city: string
  minRent: number
  maxRent: number
  search?: string
  status?: 'ALL' | PropertyStatus
}

export interface Property {
  id: string
  title: string
  location: string
  city?: string
  bedrooms: number
  bathrooms: number
  rent: number
  status: PropertyStatus
  landlordId: string
  image?: string
  images?: string[]
}
