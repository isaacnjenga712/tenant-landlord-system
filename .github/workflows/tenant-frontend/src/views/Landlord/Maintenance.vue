<template>
  <AppLayout>
    <div class="space-y-6">
      <div>
        <p class="text-sm uppercase tracking-[0.2em] text-slate-400">Maintenance</p>
        <h2 class="mt-2 text-3xl font-bold text-slate-900">Tenant tickets</h2>
      </div>

      <div v-if="loading" class="text-slate-500">Loading…</div>
      <div v-else-if="!tickets.length" class="card p-6 text-slate-500">
        No tickets from tenants yet.
      </div>
      <div v-else class="space-y-3">
        <div v-for="t in tickets" :key="t.id" class="card p-5">
          <div class="flex items-start justify-between">
            <div class="flex-1">
              <h3 class="text-lg font-semibold text-slate-900">{{ t.title }}</h3>
              <p v-if="t.description" class="mt-1 text-sm text-slate-600">
                {{ t.description }}
              </p>
              <p class="mt-2 text-xs text-slate-500">
                Tenant {{ t.tenantId.slice(0, 8) }}… · {{ new Date(t.createdAt).toLocaleString() }}
              </p>
            </div>
            <div class="ml-4 flex flex-col items-end gap-2">
              <span :class="statusClass(t.status)">{{ t.status }}</span>
              <span :class="priorityClass(t.priority)">{{ t.priority }}</span>

              <select
                v-if="t.status !== 'CLOSED'"
                class="mt-2 rounded border border-slate-300 px-2 py-1 text-xs"
                :disabled="busyId === t.id"
                :value="t.status"
                @change="changeStatus(t.id, ($event.target as HTMLSelectElement).value)"
              >
                <option value="OPEN">Open</option>
                <option value="IN_PROGRESS">In progress</option>
                <option value="RESOLVED">Resolved</option>
                <option value="CLOSED">Closed</option>
              </select>
            </div>
          </div>
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import AppLayout from '../../components/layout/AppLayout.vue'
import { ticketApi, type Ticket, type TicketStatus } from '../../api/ticket.api'
import { useNotificationStore } from '../../stores/notification'

const tickets = ref<Ticket[]>([])
const loading = ref(true)
const busyId = ref<string | null>(null)
const notification = useNotificationStore()

async function load() {
  loading.value = true
  try {
    const { data } = await ticketApi.list()
    tickets.value = data
  } catch {
    notification.addToast('Failed to load tickets', 'error')
  } finally {
    loading.value = false
  }
}

async function changeStatus(id: string, status: string) {
  busyId.value = id
  try {
    await ticketApi.updateStatus(id, status as TicketStatus)
    notification.addToast('Status updated', 'success')
    await load()
  } catch (err: any) {
    notification.addToast(err.response?.data?.message || 'Update failed', 'error')
  } finally {
    busyId.value = null
  }
}

function statusClass(status: string) {
  const base = 'rounded px-2 py-0.5 text-xs font-medium'
  if (status === 'OPEN') return `${base} bg-amber-100 text-amber-700`
  if (status === 'IN_PROGRESS') return `${base} bg-blue-100 text-blue-700`
  if (status === 'RESOLVED') return `${base} bg-emerald-100 text-emerald-700`
  return `${base} bg-slate-100 text-slate-700`
}

function priorityClass(priority: string) {
  const base = 'rounded px-2 py-0.5 text-xs font-medium'
  if (priority === 'URGENT') return `${base} bg-rose-100 text-rose-700`
  if (priority === 'HIGH') return `${base} bg-orange-100 text-orange-700`
  if (priority === 'MEDIUM') return `${base} bg-amber-100 text-amber-700`
  return `${base} bg-slate-100 text-slate-700`
}

onMounted(load)
</script>