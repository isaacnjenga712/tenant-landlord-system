<template>
  <form @submit.prevent="submit" class="space-y-4">
    <BaseInput v-model="form.title" label="Title" />
    <BaseInput v-model="form.address" label="Address" />
    <BaseInput v-model="form.city" label="City" />
    <BaseInput v-model.number="form.rent" label="Rent" type="number" />
    <BaseInput v-model.number="form.bedrooms" label="Bedrooms" type="number" />
    <BaseInput v-model.number="form.bathrooms" label="Bathrooms" type="number" />
    <BaseButton type="submit">Save</BaseButton>
  </form>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import BaseInput from '@/components/common/BaseInput.vue'
import BaseButton from '@/components/common/BaseButton.vue'

const emit = defineEmits(['submit'])
const form = reactive({
  title: '',
  address: '',
  city: '',
  rent: 0,
  bedrooms: 1,
  bathrooms: 1,
})

function submit() {
  emit('submit', { ...form })
}
</script>