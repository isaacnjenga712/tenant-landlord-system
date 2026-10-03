<template>
  <aside class="w-64 bg-gray-800 text-white min-h-screen p-4">
    <nav class="space-y-2">
      <router-link
        v-for="item in menuItems"
        :key="item.path"
        :to="item.path"
        class="block px-4 py-2 rounded hover:bg-gray-700"
        active-class="bg-gray-700"
      >
        {{ item.label }}
      </router-link>
    </nav>
  </aside>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAuth } from '@/composables/useAuth'

const { role } = useAuth()

const menuItems = computed(() => {
  if (role.value === 'TENANT') {
    return [
      { path: '/tenant/dashboard', label: 'Dashboard' },
      { path: '/tenant/properties', label: 'Browse Properties' },
      { path: '/tenant/my-lease', label: 'My Lease' },
      { path: '/tenant/payments', label: 'Payments' },
    ]
  }
  if (role.value === 'LANDLORD') {
    return [
      { path: '/landlord/dashboard', label: 'Dashboard' },
      { path: '/landlord/properties', label: 'My Properties' },
      { path: '/landlord/applications', label: 'Applications' },
      { path: '/landlord/tenants', label: 'Tenants' },
    ]
  }
  if (role.value === 'ADMIN') {
    return [
      { path: '/admin/users', label: 'Users' },
      { path: '/admin/analytics', label: 'Analytics' },
    ]
  }
  return [{ path: '/dashboard', label: 'Dashboard' }]
})
</script>
