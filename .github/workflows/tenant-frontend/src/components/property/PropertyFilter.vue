<template>
  <div class="flex gap-4 items-end">
    <BaseInput v-model="filters.city" label="City" placeholder="e.g. Nairobi" />
    <BaseInput v-model.number="filters.minRent" label="Min Rent" type="number" />
    <BaseInput v-model.number="filters.maxRent" label="Max Rent" type="number" />
    <BaseButton @click="$emit('filter', filters)">Filter</BaseButton>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import BaseInput from '@/components/common/BaseInput.vue'
import BaseButton from '@/components/common/BaseButton.vue'

const filters = reactive({ city: '', minRent: 0, maxRent: 0 })
defineEmits(['filter'])
</script>