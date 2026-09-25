<template>
  <div class="card overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
    <div v-if="property.image" class="h-40 w-full bg-slate-100">
      <img :src="property.image" :alt="property.title" class="h-full w-full object-cover" />
    </div>

    <div class="p-5 space-y-2">
      <h3 class="text-lg font-semibold text-slate-900">{{ property.title }}</h3>
      <p class="text-sm text-slate-500">{{ property.location }}</p>

      <div class="flex items-center gap-3 text-sm text-slate-600">
        <span>{{ property.bedrooms }} bed</span>
        <span>·</span>
        <span>{{ property.bathrooms }} bath</span>
      </div>

      <p class="text-base font-semibold text-slate-900">
        KSh {{ property.rent.toLocaleString() }}<span class="text-sm text-slate-500"> / month</span>
      </p>

      <span
        :class="[
          'inline-block rounded px-2 py-0.5 text-xs font-medium',
          property.status === 'AVAILABLE' && 'bg-emerald-100 text-emerald-700',
          property.status === 'OCCUPIED' && 'bg-amber-100 text-amber-700',
          property.status === 'MAINTENANCE' && 'bg-rose-100 text-rose-700',
        ]"
      >
        {{ property.status }}
      </span>

      <button
        class="mt-3 w-full rounded bg-slate-900 py-2 text-sm font-medium text-white hover:bg-slate-800 disabled:opacity-50"
        :disabled="property.status !== 'AVAILABLE'"
        @click="$emit('apply', property.id)"
      >
        Apply
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Property } from '../../types/property'

defineProps<{ property: Property }>()
defineEmits(['apply'])
</script>
