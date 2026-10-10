<template>
  <router-view />
  <Toast />
</template>

<script setup lang="ts">
import Toast from '@/components/common/Toast.vue'
import { useNotificationSocket } from '@/composables/useNotificationSocket'

// Connects STOMP once for the whole app lifetime.
// The composable no-ops when there's no token/publicId, so this is safe
// for login/register pages too — it just won't connect until the user logs in.
useNotificationSocket()
</script>