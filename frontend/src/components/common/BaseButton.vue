<template>
  <button
    :class="[
      'px-4 py-2 rounded font-medium transition',
      variant === 'primary' && 'bg-blue-600 text-white hover:bg-blue-700',
      variant === 'secondary' && 'bg-gray-200 text-gray-800 hover:bg-gray-300',
      variant === 'danger' && 'bg-red-600 text-white hover:bg-red-700',
      variant === 'ghost' && 'bg-gray-100 text-gray-700 hover:bg-gray-200',
      disabled && 'opacity-50 cursor-not-allowed',
    ]"
    :disabled="disabled"
    @click="$emit('click')"
  >
    <slot />
  </button>
</template>

<script setup lang="ts">
defineProps<{
  variant?: 'primary' | 'secondary' | 'danger' | 'ghost'
  disabled?: boolean
}>()
defineEmits(['click'])
</script>
