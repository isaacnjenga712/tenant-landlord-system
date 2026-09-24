<template>
  <div class="bg-white rounded-lg shadow overflow-hidden">
    <img :src="property.images?.[0] || property.image || '/placeholder.jpg'" alt="Property" class="h-48 w-full object-cover" />
    <div class="p-4">
      <h3 class="text-lg font-semibold">{{ property.title }}</h3>
      <p class="text-gray-600">{{ property.city }}</p>
      <p class="text-blue-600 font-bold mt-2">${{ property.rent }}/month</p>
      <p class="text-sm text-gray-500">{{ property.bedrooms }} bed · {{ property.bathrooms }} bath</p>
      <BaseButton class="mt-4 w-full" @click="$emit('apply', property.id)">Apply</BaseButton>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Property } from '@/types/property'
import BaseButton from '@/components/common/BaseButton.vue'

defineProps<{ property: Property }>()
defineEmits(['apply'])
</script>
