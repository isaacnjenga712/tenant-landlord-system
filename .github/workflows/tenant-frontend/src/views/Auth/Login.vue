<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-100">
    <div class="bg-white p-8 rounded-lg shadow w-full max-w-md">
      <h2 class="text-2xl font-bold mb-6 text-center">Login to RentHub</h2>
      <form @submit.prevent="handleLogin" class="space-y-4">
        <BaseInput v-model="email" label="Email" type="email" />
        <BaseInput v-model="password" label="Password" type="password" />
        <BaseButton type="submit" class="w-full">Login</BaseButton>
      </form>
      <p class="mt-4 text-center text-sm">
        Don't have an account?
        <router-link to="/register" class="text-blue-600">Register</router-link>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth'
import BaseInput from '@/components/common/BaseInput.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import { useNotificationStore } from '@/stores/notification'

const email = ref('')
const password = ref('')
const router = useRouter()
const { login } = useAuth()
const notification = useNotificationStore()

async function handleLogin() {
  try {
    await login({ email: email.value, password: password.value })
    router.push('/dashboard')
  } catch (err: any) {
    notification.addToast(err.response?.data?.message || 'Login failed', 'error')
  }
}
</script>
