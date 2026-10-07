<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

interface NavItem {
  path: string
  label: string
  icon: string
}

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const items: NavItem[] = [
  { path: '/admin/dashboard', label: 'Dashboard', icon: '📊' },
  { path: '/admin/users', label: 'Users', icon: '👥' },
  { path: '/admin/payments', label: 'Payments', icon: '💳' },
  { path: '/admin/maintenance', label: 'Maintenance', icon: '🔧' },
  { path: '/admin/notifications', label: 'Notifications', icon: '🔔' },
  { path: '/admin/audit-logs', label: 'Audit Logs', icon: '📜' },
  { path: '/admin/system-health', label: 'System Health', icon: '❤️' },
]

const activePath = computed(() => route.path)

function logout(): void {
  auth.logout()
  void router.push('/login')
}
</script>

<template>
  <div class="flex min-h-screen bg-slate-50">
    <aside class="w-64 bg-slate-900 text-white flex flex-col">
      <div class="p-6 border-b border-slate-800">
        <h1 class="text-lg font-semibold">RentFlow</h1>
        <p class="text-xs uppercase tracking-wide text-slate-400 mt-1">
          Admin Console
        </p>
      </div>

      <nav class="flex-1 p-3 space-y-0.5 overflow-y-auto">
        <RouterLink
          v-for="item in items"
          :key="item.path"
          :to="item.path"
          class="flex items-center gap-3 px-3 py-2 rounded text-sm transition"
          :class="activePath === item.path
            ? 'bg-slate-800 text-white'
            : 'text-slate-300 hover:bg-slate-800 hover:text-white'"
        >
          <span class="text-base leading-none">{{ item.icon }}</span>
          <span>{{ item.label }}</span>
        </RouterLink>
      </nav>

      <div class="p-4 border-t border-slate-800">
        <p class="text-xs text-slate-400 truncate" :title="auth.user?.email">
          {{ auth.user?.email }}
        </p>
        <p class="text-[10px] uppercase tracking-wider text-indigo-400 mt-0.5">
          {{ auth.user?.role }}
        </p>
        <button
          class="mt-3 text-xs text-rose-400 hover:text-rose-300 transition"
          @click="logout"
        >
          Sign out
        </button>
      </div>
    </aside>

    <main class="flex-1 overflow-y-auto">
      <RouterView />
    </main>
  </div>
</template>