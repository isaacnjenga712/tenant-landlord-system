<template>
  <AppLayout>
    <div class="space-y-6">
      <div class="flex items-center justify-between">
        <div>
          <p class="text-sm uppercase tracking-[0.2em] text-slate-400">Maintenance</p>
          <h2 class="mt-2 text-3xl font-bold text-slate-900">My tickets</h2>
        </div>
        <button
          class="rounded bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700"
          @click="showForm = !showForm"
        >
          {{ showForm ? 'Cancel' : 'New ticket' }}
        </button>
      </div>

      <!-- Create form -->
      <div v-if="showForm" class="card space-y-4 p-5">
        <h3 class="text-lg font-semibold text-slate-900">Report an issue</h3>

        <div class="grid gap-4 md:grid-cols-2">
          <div>
            <label class="block text-sm text-slate-600">Unit</label>
            <select
              v-model="form.unitId"
              class="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm"
            >
              <option value="">Select a unit…</option>
              <option v-for="u in units" :key="u.id" :value="u.id">
                {{ u.label }}
              </option>
            </select>
          </div>

          <div>
            <label class="block text-sm text-slate-600">Priority</label>
            <select
              v-model="form.priority"
              class="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm"
            >
              <option value="LOW">Low</option>
              <option value="MEDIUM">Medium</option>
              <option value="HIGH">High</option>
              <option value="URGENT">Urgent</option>
            </select>
          </div>
        </div>

        <div>
          <label class="block text-sm text-slate-600">Title</label>
          <input
            v-model="form.title"
            class="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm"
            placeholder="e.g. Leaking tap in kitchen"
          />
        </div>

        <div>
          <label class="block text-sm text-slate-600">Description</label>
          <textarea
            v-model="form.description"
            rows="3"
            class="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm"
            placeholder="Describe the issue…"
          />
        </div>

        <div v-if="error" class="text-sm text-red-600">{{ error }}</div>

        <button
          class="rounded bg-slate-900 px-4 py-2 text-sm font-medium text-white hover:bg-slate-800 disabled:opacity-50"
          :disabled="submitting"
          @click="submit"
        >
          {{ submitting ? 'Creating…' : 'Create ticket' }}
        </button>
      </div>

      <!-- Ticket list -->
      <div v-if="loading" class="text-slate-500">Loading…</div>
      <div v-else-if="!tickets.length" class="card p-6 text-slate-500">
        No tickets yet.
      </div>
      <div v-else class="space-y-3">
        <div v-for="t in tickets" :key="t.id" class="card p-5">
          <div class="flex items-start justify-between">
            <div>
              <h3 class="text-lg font-semibold text-slate-900">{{ t.title }}</h3>
              <p v-if="t.description" class="mt-1 text-sm text-slate-600">
                {{ t.description }}
              </p>
              <p class="mt-2 text-xs text-slate-500">
                {{ new Date(t.createdAt).toLocaleString() }}
              </p>
            </div>
            <div class="flex flex-col items-end gap-2">
              <span :class="statusClass(t.status)">{{ t.status }}</span>
              <span :class="priorityClass(t.priority)">{{ t.priority }}</span>
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
import { ticketApi, type Ticket, type TicketPriority } from '../../api/ticket.api'
import { leaseApi } from '../../api/lease.api'
import { unitApi } from '../../api/unit.api'
import { useAuthStore } from '../../stores/auth'
import { useNotificationStore } from '../../stores/notification'

const auth = useAuthStore()
const notification = useNotificationStore()

const tickets = ref<Ticket[]>([])
const units = ref<{ id: string; label: string }[]>([])
const loading = ref(true)
const submitting = ref(false)
const showForm = ref(false)
const error = ref('')

const form = ref({
  unitId: '',
  title: '',
  description: '',
  priority: 'MEDIUM' as TicketPriority,
})

async function load() {
  loading.value = true
  try {
    const { data } = await ticketApi.list()
    tickets.value = data
  } catch (err: any) {
    notification.addToast('Failed to load tickets', 'error')
  } finally {
    loading.value = false
  }
}

async function loadUnits() {
  try {
    // Get tenant's active lease → property → units
    const { data } = await leaseApi.list()
    const lease = data.leases.find(l => l.status === 'ACTIVE') ?? data.leases[0]
    if (!lease) return

    const { data: unitList } = await unitApi.byProperty(lease.propertyId)
    units.value = unitList.map((u: any) => ({
      id: u.id,
      label: `Unit ${u.id.slice(0, 8)} · ${u.bedrooms ?? '?'} bed`,
    }))

    if (units.value.length) {
      form.value.unitId = units.value[0].id
    }
  } catch (err) {
    // silent — form will show "Select a unit…" with no options
  }
}

async function submit() {
  error.value = ''
  if (!form.value.unitId || !form.value.title) {
    error.value = 'Unit and title are required'
    return
  }

  submitting.value = true
  try {
    // We need landlordId from the active lease's property
    const { data } = await leaseApi.list()
    const lease = data.leases.find(l => l.status === 'ACTIVE') ?? data.leases[0]
    if (!lease) {
      error.value = 'No active lease found — cannot determine landlord'
      return
    }

    // Fetch the property to get landlordId
    const { propertyApi } = await import('../../api/property.api')
    const { data: prop } = await propertyApi.get(lease.propertyId)

    await ticketApi.create({
      unitId: form.value.unitId,
      title: form.value.title,
      description: form.value.description || undefined,
      priority: form.value.priority,
      landlordId: prop.landlordId,
    })

    notification.addToast('Ticket created', 'success')
    showForm.value = false
    form.value = { unitId: units.value[0]?.id || '', title: '', description: '', priority: 'MEDIUM' }
    await load()
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Failed to create ticket'
    notification.addToast(error.value, 'error')
  } finally {
    submitting.value = false
  }
}

function statusClass(status: string) {
  const base = 'rounded px-2 py-0.5 text-xs font-medium'
  if (status === 'OPEN') return `${base} bg-amber-100 text-amber-700`
  if (status === 'IN_PROGRESS') return `${base} bg-blue-100 text-blue-700`
  if (status === 'RESOLVED') return `${base} bg-emerald-100 text-emerald-700`
  if (status === 'CLOSED') return `${base} bg-slate-100 text-slate-700`
  return `${base} bg-slate-100 text-slate-700`
}

function priorityClass(priority: string) {
  const base = 'rounded px-2 py-0.5 text-xs font-medium'
  if (priority === 'URGENT') return `${base} bg-rose-100 text-rose-700`
  if (priority === 'HIGH') return `${base} bg-orange-100 text-orange-700`
  if (priority === 'MEDIUM') return `${base} bg-amber-100 text-amber-700`
  return `${base} bg-slate-100 text-slate-700`
}

onMounted(async () => {
  await loadUnits()
  await load()
})
</script>