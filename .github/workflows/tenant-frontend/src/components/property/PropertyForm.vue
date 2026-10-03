<template>
  <form @submit.prevent="submit" class="card space-y-4 p-5">
    <h3 class="text-lg font-semibold text-slate-900">Add property</h3>

    <div class="grid gap-4 md:grid-cols-2">
      <BaseInput v-model="form.addressLine1" label="Address line 1" required />
      <BaseInput v-model="form.addressLine2" label="Address line 2" />
      <BaseInput v-model="form.city" label="City" required />
      <BaseInput v-model="form.state" label="State" />
      <BaseInput v-model="form.zipCode" label="Zip / Postal code" />
      <BaseInput v-model="form.country" label="Country" />
    </div>

    <hr class="border-slate-200" />

    <h4 class="text-sm font-medium text-slate-700">First unit</h4>
    <div class="grid gap-4 md:grid-cols-3">
      <BaseInput v-model.number="form.bedrooms" label="Bedrooms" type="number" min="0" />
      <BaseInput v-model.number="form.bathrooms" label="Bathrooms" type="number" min="0" />
      <BaseInput v-model.number="form.monthlyRent" label="Monthly rent (KSh)" type="number" min="0" />
    </div>

    <div v-if="error" class="text-sm text-red-600">{{ error }}</div>

    <div class="flex gap-2">
      <BaseButton type="submit" :disabled="submitting">
        {{ submitting ? 'Saving…' : 'Save property' }}
      </BaseButton>
      <BaseButton type="button" @click="reset">Clear</BaseButton>
    </div>
  </form>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import BaseInput from '@/components/common/BaseInput.vue'
import BaseButton from '@/components/common/BaseButton.vue'

export interface PropertyFormPayload {
  property: {
    addressLine1: string
    addressLine2?: string | null
    city: string
    state?: string | null
    zipCode?: string | null
    country?: string | null
    status: 'AVAILABLE'
  }
  unit: {
    bedrooms: number
    bathrooms: number
    monthlyRent: number
    status: 'AVAILABLE'
  }
}

const emit = defineEmits<{ (e: 'submit', payload: PropertyFormPayload): void }>()

const submitting = ref(false)
const error = ref('')

const initial = () => ({
  addressLine1: '',
  addressLine2: '',
  city: '',
  state: '',
  zipCode: '',
  country: '',
  bedrooms: 1,
  bathrooms: 1,
  monthlyRent: 0,
})

const form = reactive(initial())

function reset() {
  Object.assign(form, initial())
  error.value = ''
}

function submit() {
  error.value = ''
  if (!form.addressLine1 || !form.city) {
    error.value = 'Address line 1 and city are required.'
    return
  }
  submitting.value = true
  emit('submit', {
    property: {
      addressLine1: form.addressLine1,
      addressLine2: form.addressLine2 || null,
      city: form.city,
      state: form.state || null,
      zipCode: form.zipCode || null,
      country: form.country || null,
      status: 'AVAILABLE',
    },
    unit: {
      bedrooms: form.bedrooms,
      bathrooms: form.bathrooms,
      monthlyRent: form.monthlyRent,
      status: 'AVAILABLE',
    },
  })
}

defineExpose({
  done() {
    submitting.value = false
    reset()
  },
  fail(msg: string) {
    submitting.value = false
    error.value = msg
  },
})
</script>