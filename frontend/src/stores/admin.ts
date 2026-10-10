import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { AdminApi } from '../api/admin.api'
import type { OverviewDto } from '../types/admin'

export const useAdminStore = defineStore('admin', () => {
  const overview = ref<OverviewDto | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  const hasData = computed(() => overview.value !== null)

  async function fetchOverview(force = false): Promise<void> {
    if (overview.value && !force) return

    loading.value = true
    error.value = null
    try {
      overview.value = await AdminApi.overview()
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to load overview'
      console.error('[admin] fetchOverview failed', e)
    } finally {
      loading.value = false
    }
  }

  function reset(): void {
    overview.value = null
    error.value = null
  }

  return { overview, loading, error, hasData, fetchOverview, reset }
})