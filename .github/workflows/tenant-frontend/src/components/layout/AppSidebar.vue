<template>
  <aside class="w-64 bg-gray-800 text-white min-h-screen p-4">
    <nav class="space-y-2">
      <router-link
        v-for="item in menuItems"
        :key="item.path"
        :to="item.path"
        class="flex items-center justify-between px-4 py-2 rounded hover:bg-gray-700"
        active-class="bg-gray-700"
      >
        <span>{{ item.label }}</span>
        <span
          v-if="item.path.endsWith('/notifications') && unreadCount > 0"
          class="rounded-full bg-rose-500 px-2 py-0.5 text-xs font-medium"
        >
          {{ unreadCount > 99 ? '99+' : unreadCount }}
        </span>
      </router-link>
    </nav>
  </aside>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useAuth } from '@/composables/useAuth'
import { useNotificationStore } from '@/stores/notifications'

const { role } = useAuth()
const notificationStore = useNotificationStore()
const { unreadCount } = storeToRefs(notificationStore)

onMounted(() => {
  void notificationStore.fetchUnreadCount()
})

const menuItems = computed(() => {
  if (role.value === 'TENANT') {
    return [
      { path: '/tenant/dashboard', label: 'Dashboard' },
      { path: '/tenant/properties', label: 'Browse Properties' },
      { path: '/tenant/my-lease', label: 'My Lease' },
      { path: '/tenant/payments', label: 'Payments' },
      { path: '/tenant/notifications', label: 'Notifications' },
    ]
  }
  if (role.value === 'LANDLORD') {
    return [
      { path: '/landlord/dashboard', label: 'Dashboard' },
      { path: '/landlord/properties', label: 'My Properties' },
      { path: '/landlord/applications', label: 'Applications' },
      { path: '/landlord/tenants', label: 'Tenants' },
      { path: '/landlord/notifications', label: 'Notifications' },
    ]
  }
  if (role.value === 'ADMIN') {
    return [
      { path: '/admin/users', label: 'Users' },
      { path: '/admin/analytics', label: 'Analytics' },
      { path: '/admin/notifications', label: 'Notifications' },
    ]
  }
  return [{ path: '/dashboard', label: 'Dashboard' }]
})
</script>