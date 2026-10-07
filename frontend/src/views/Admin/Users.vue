<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { AdminApi } from '../../api/admin.api'
import type { AdminUserDto, PageResponse } from '../../types/admin'

// ---- State ----
const items = ref<AdminUserDto[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

const page = ref(0)
const size = 20
const total = ref(0)
const hasMore = ref(false)

const filterRole = ref('')
const filterStatus = ref('')
const filterSearch = ref('')

// Detail drawer
const selected = ref<AdminUserDto | null>(null)
const actionError = ref<string | null>(null)
const actionLoading = ref(false)

// ---- Debounce search ----
let debounce: ReturnType<typeof setTimeout> | undefined
watch(filterSearch, () => {
  clearTimeout(debounce)
  debounce = setTimeout(() => {
    page.value = 0
    void fetch()
  }, 300)
})

watch([filterRole, filterStatus], () => {
  page.value = 0
  void fetch()
})

// ---- Fetch ----
async function fetch(): Promise<void> {
  loading.value = true
  error.value = null
  try {
    const res: PageResponse<AdminUserDto> = await AdminApi.listUsers({
      page: page.value,
      size,
      role: filterRole.value || undefined,
      status: filterStatus.value || undefined,
      search: filterSearch.value || undefined,
    })
    items.value = res.items
    total.value = res.total
    hasMore.value = res.hasMore
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to load users'
  } finally {
    loading.value = false
  }
}

onMounted(fetch)

// ---- Pagination ----
const pageCount = computed(() => Math.ceil(total.value / size) || 1)

function prevPage(): void {
  if (page.value > 0) {
    page.value--
    void fetch()
  }
}

function nextPage(): void {
  if (hasMore.value) {
    page.value++
    void fetch()
  }
}

// ---- Detail panel ----
function openDetail(u: AdminUserDto): void {
  selected.value = u
  actionError.value = null
}

function closeDetail(): void {
  selected.value = null
  actionError.value = null
}

// ---- Actions ----
async function changeRole(u: AdminUserDto, newRole: string): Promise<void> {
  if (newRole === u.role) return
  actionLoading.value = true
  actionError.value = null
  try {
    await AdminApi.updateRole(u.id, newRole)
    // Update local state
    u.role = newRole as AdminUserDto['role']
    if (selected.value?.id === u.id) selected.value.role = u.role
  } catch (e: unknown) {
    actionError.value = e instanceof Error ? e.message : 'Failed to update role'
  } finally {
    actionLoading.value = false
  }
}

async function toggleStatus(u: AdminUserDto): Promise<void> {
  actionLoading.value = true
  actionError.value = null
  const target = !u.active
  try {
    await AdminApi.updateStatus(u.id, target)
    u.active = target
    if (selected.value?.id === u.id) selected.value.active = target
  } catch (e: unknown) {
    actionError.value = e instanceof Error ? e.message : 'Failed to update status'
  } finally {
    actionLoading.value = false
  }
}

async function resetPassword(u: AdminUserDto): Promise<void> {
  if (!confirm(`Send password reset email to ${u.email}?`)) return
  actionLoading.value = true
  actionError.value = null
  try {
    await AdminApi.resetPassword(u.id)
    alert('Password reset email sent.')
  } catch (e: unknown) {
    actionError.value = e instanceof Error ? e.message : 'Failed to send reset'
  } finally {
    actionLoading.value = false
  }
}

// ---- Helpers ----
function roleBadge(role: string): string {
  switch (role) {
    case 'ADMIN':    return 'bg-indigo-100 text-indigo-700'
    case 'LANDLORD': return 'bg-emerald-100 text-emerald-700'
    case 'TENANT':   return 'bg-sky-100 text-sky-700'
    default:         return 'bg-slate-100 text-slate-600'
  }
}

function fmtDate(iso: string | null): string {
  if (!iso) return '—'
  return new Date(iso).toLocaleDateString('en-GB', {
    day: '2-digit', month: 'short', year: 'numeric',
  })
}
</script>

<template>
  <div class="p-8 max-w-7xl mx-auto">
    <header class="mb-6 flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-semibold text-slate-900">Users</h1>
        <p class="text-sm text-slate-500 mt-1">
          {{ total }} {{ total === 1 ? 'user' : 'users' }} total
        </p>
      </div>
      <button
        class="rounded border border-slate-200 bg-white px-3 py-1.5 text-sm text-slate-700 transition hover:bg-slate-50 disabled:opacity-50"
        :disabled="loading"
        @click="fetch"
      >
        {{ loading ? 'Refreshing…' : 'Refresh' }}
      </button>
    </header>

    <!-- Filters -->
    <div class="mb-4 flex flex-wrap gap-2 items-center">
      <input
        v-model="filterSearch"
        type="text"
        placeholder="Search email or name…"
        class="w-64 rounded border border-slate-200 bg-white px-3 py-1.5 text-sm focus:border-indigo-500 focus:outline-none"
      />
      <select
        v-model="filterRole"
        class="rounded border border-slate-200 bg-white px-3 py-1.5 text-sm focus:border-indigo-500 focus:outline-none"
      >
        <option value="">All roles</option>
        <option value="ADMIN">Admin</option>
        <option value="LANDLORD">Landlord</option>
        <option value="TENANT">Tenant</option>
      </select>
      <select
        v-model="filterStatus"
        class="rounded border border-slate-200 bg-white px-3 py-1.5 text-sm focus:border-indigo-500 focus:outline-none"
      >
        <option value="">All statuses</option>
        <option value="active">Active</option>
        <option value="inactive">Inactive</option>
      </select>
    </div>

    <div
      v-if="error"
      class="mb-4 rounded border border-rose-200 bg-rose-50 p-3 text-sm text-rose-700"
    >
      {{ error }}
    </div>

    <!-- Loading -->
    <div v-if="loading && items.length === 0" class="py-16 text-center text-slate-500">
      Loading users…
    </div>

    <!-- Empty -->
    <div v-else-if="items.length === 0" class="py-16 text-center text-slate-500">
      No users found.
    </div>

    <!-- Table -->
    <div v-else class="overflow-hidden rounded-lg border border-slate-200 bg-white">
      <table class="w-full text-sm">
        <thead class="bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
          <tr>
            <th class="px-4 py-3 font-medium">User</th>
            <th class="px-4 py-3 font-medium">Role</th>
            <th class="px-4 py-3 font-medium">Status</th>
            <th class="px-4 py-3 font-medium">Created</th>
            <th class="px-4 py-3 font-medium text-right">Actions</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-100">
          <tr
            v-for="u in items"
            :key="u.id"
            class="transition hover:bg-slate-50"
          >
            <td class="px-4 py-3">
              <button
                class="text-left hover:underline"
                @click="openDetail(u)"
              >
                <p class="font-medium text-slate-900">{{ u.fullName || '—' }}</p>
                <p class="text-xs text-slate-500">{{ u.email }}</p>
              </button>
            </td>
            <td class="px-4 py-3">
              <span
                class="inline-block rounded-full px-2 py-0.5 text-xs font-medium"
                :class="roleBadge(u.role)"
              >
                {{ u.role }}
              </span>
            </td>
            <td class="px-4 py-3">
              <span
                class="inline-flex items-center gap-1.5 text-xs"
                :class="u.active ? 'text-emerald-700' : 'text-slate-500'"
              >
                <span
                  class="h-1.5 w-1.5 rounded-full"
                  :class="u.active ? 'bg-emerald-500' : 'bg-slate-400'"
                ></span>
                {{ u.active ? 'Active' : 'Inactive' }}
              </span>
            </td>
            <td class="px-4 py-3 text-slate-600">{{ fmtDate(u.createdAt) }}</td>
            <td class="px-4 py-3 text-right">
              <button
                class="text-xs text-indigo-600 hover:underline"
                @click="openDetail(u)"
              >
                Manage
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Pagination -->
    <div
      v-if="items.length > 0"
      class="mt-4 flex items-center justify-between text-sm text-slate-600"
    >
      <span>Page {{ page + 1 }} of {{ pageCount }}</span>
      <div class="flex gap-2">
        <button
          class="rounded border border-slate-200 bg-white px-3 py-1.5 transition hover:bg-slate-50 disabled:opacity-40"
          :disabled="page === 0 || loading"
          @click="prevPage"
        >
          Previous
        </button>
        <button
          class="rounded border border-slate-200 bg-white px-3 py-1.5 transition hover:bg-slate-50 disabled:opacity-40"
          :disabled="!hasMore || loading"
          @click="nextPage"
        >
          Next
        </button>
      </div>
    </div>

    <!-- Detail drawer -->
    <div
      v-if="selected"
      class="fixed inset-0 z-40 flex items-stretch justify-end bg-slate-900/40"
      @click.self="closeDetail"
    >
      <aside class="w-full max-w-md bg-white shadow-xl flex flex-col">
        <header class="p-5 border-b border-slate-200 flex items-start justify-between">
          <div>
            <h2 class="text-lg font-semibold text-slate-900">
              {{ selected.fullName || 'User' }}
            </h2>
            <p class="text-sm text-slate-500 mt-0.5">{{ selected.email }}</p>
          </div>
          <button
            class="rounded p-1 text-slate-400 hover:bg-slate-100 hover:text-slate-700"
            @click="closeDetail"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M18 6 6 18M6 6l12 12" />
            </svg>
          </button>
        </header>

        <div class="flex-1 p-5 space-y-5 overflow-y-auto">
          <div
            v-if="actionError"
            class="rounded border border-rose-200 bg-rose-50 p-3 text-sm text-rose-700"
          >
            {{ actionError }}
          </div>

          <dl class="text-sm space-y-3">
            <div class="flex justify-between">
              <dt class="text-slate-500">Public ID</dt>
              <dd class="text-slate-800 font-mono text-xs">{{ selected.id }}</dd>
            </div>
            <div class="flex justify-between">
              <dt class="text-slate-500">Phone</dt>
              <dd class="text-slate-800">{{ selected.phone || '—' }}</dd>
            </div>
            <div class="flex justify-between">
              <dt class="text-slate-500">Created</dt>
              <dd class="text-slate-800">{{ fmtDate(selected.createdAt) }}</dd>
            </div>
            <div class="flex justify-between">
              <dt class="text-slate-500">Last login</dt>
              <dd class="text-slate-800">{{ fmtDate(selected.lastLoginAt) }}</dd>
            </div>
          </dl>

          <!-- Role -->
          <div>
            <label class="block text-xs font-medium uppercase tracking-wide text-slate-500 mb-2">
              Role
            </label>
            <div class="flex gap-2">
              <button
                v-for="r in ['TENANT', 'LANDLORD', 'ADMIN']"
                :key="r"
                class="flex-1 rounded border px-3 py-1.5 text-xs font-medium transition disabled:opacity-50"
                :class="selected.role === r
                  ? 'border-indigo-500 bg-indigo-50 text-indigo-700'
                  : 'border-slate-200 bg-white text-slate-600 hover:bg-slate-50'"
                :disabled="actionLoading || selected.role === r"
                @click="changeRole(selected, r)"
              >
                {{ r }}
              </button>
            </div>
          </div>

          <!-- Status -->
          <div>
            <label class="block text-xs font-medium uppercase tracking-wide text-slate-500 mb-2">
              Status
            </label>
            <button
              class="w-full rounded border px-3 py-2 text-sm font-medium transition disabled:opacity-50"
              :class="selected.active
                ? 'border-rose-200 text-rose-700 hover:bg-rose-50'
                : 'border-emerald-200 text-emerald-700 hover:bg-emerald-50'"
              :disabled="actionLoading"
              @click="toggleStatus(selected)"
            >
              {{ selected.active ? 'Deactivate account' : 'Activate account' }}
            </button>
          </div>

          <!-- Password reset -->
          <div>
            <label class="block text-xs font-medium uppercase tracking-wide text-slate-500 mb-2">
              Password
            </label>
            <button
              class="w-full rounded border border-slate-200 bg-white px-3 py-2 text-sm text-slate-700 transition hover:bg-slate-50 disabled:opacity-50"
              :disabled="actionLoading"
              @click="resetPassword(selected)"
            >
              Send password reset email
            </button>
          </div>
        </div>

        <footer class="p-5 border-t border-slate-200 flex justify-end">
          <button
            class="rounded border border-slate-200 bg-white px-4 py-2 text-sm text-slate-700 transition hover:bg-slate-50"
            @click="closeDetail"
          >
            Close
          </button>
        </footer>
      </aside>
    </div>
  </div>
</template>