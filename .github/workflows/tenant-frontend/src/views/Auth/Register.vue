<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-100">
    <div class="bg-white p-8 rounded-lg shadow w-full max-w-md">
      <h2 class="text-2xl font-bold mb-6 text-center">Register</h2>
      <form @submit.prevent="handleRegister" class="space-y-4">
        <BaseInput v-model="name" label="Full Name" />
        <BaseInput v-model="email" label="Email" type="email" />
        <BaseInput v-model="password" label="Password" type="password" />
        <div>
          <label class="block text-sm font-medium text-gray-700">Role</label>
          <select v-model="role" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm">
            <option value="TENANT">Tenant</option>
            <option value="LANDLORD">Landlord</option>
          </select>
        </div>
        <BaseButton type="submit" class="w-full">Register</BaseButton>
      </form>
      <p class="mt-4 text-center text-sm">
        Already have an account?
        <router-link to="/login" class="text-blue-600">Login</router-link>
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

const name = ref('')
const email = ref('')
const password = ref('')
const role = ref<'TENANT' | 'LANDLORD'>('TENANT')
const router = useRouter()
const { register } = useAuth()
const notification = useNotificationStore()

async function handleRegister() {
  try {
    await register({ name: name.value, email: email.value, password: password.value, role: role.value })
    router.push('/dashboard')
  } catch (err: any) {
    notification.addToast(err.response?.data?.message || 'Registration failed', 'error')
  }
}
</script>
