import { defineStore } from 'pinia'

export interface ToastState {
  visible: boolean
  message: string
  type: 'success' | 'error' | 'info'
}

export interface ToastItem {
  id: string
  text: string
  type: ToastState['type']
}

export const useNotificationStore = defineStore('notification', {
  state: () => ({
    toasts: [] as ToastItem[],
    toast: {
      visible: false,
      message: '',
      type: 'success',
    } as ToastState,
  }),
  actions: {
    addToast(message: string, type: ToastState['type'] = 'success') {
      const id = `${Date.now()}-${Math.random().toString(16).slice(2, 8)}`
      this.toasts = [...this.toasts, { id, text: message, type }].slice(-3)
      this.toast = { visible: true, message, type }
    },
    show(message: string, type: ToastState['type'] = 'success') {
      this.addToast(message, type)
    },
    hide() {
      this.toast.visible = false
    },
  },
})
