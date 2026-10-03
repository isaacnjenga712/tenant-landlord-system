<template>
  <div class="fixed bottom-4 right-4 z-50 space-y-2">
    <div
      v-for="toast in notificationStore.toasts"
      :key="toast.id"
      :class="[
        'px-4 py-2 rounded shadow-lg text-white',
        toast.type === 'success' && 'bg-green-500',
        toast.type === 'error' && 'bg-red-500',
        toast.type === 'info' && 'bg-blue-500',
      ]"
    >
      {{ toast.text }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { useNotificationStore } from '@/stores/notification'
const notificationStore = useNotificationStore()
</script>
