<template>
  <header class="bg-white shadow px-6 py-4 flex justify-between items-center">
    <h1 class="text-xl font-bold text-gray-800">RentHub</h1>
    <div class="flex items-center space-x-4">
      <span class="text-gray-600">{{ auth.user?.name }}</span>
      <BaseButton variant="secondary" @click="handleLogout">Logout</BaseButton>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth'
import BaseButton from '@/components/common/BaseButton.vue'

const router = useRouter()
const { user: authUser, logout } = useAuth()
const auth = computed(() => ({ user: authUser.value }))

function handleLogout() {
  logout()
  router.push('/login')
}
</script>