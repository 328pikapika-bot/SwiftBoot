import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const sidebar = ref({
    opened: true,
    withoutAnimation: false
  })

  function toggleSidebar() {
    sidebar.value.opened = !sidebar.value.opened
    sidebar.value.withoutAnimation = false
  }

  function closeSidebar(withoutAnimation: boolean) {
    sidebar.value.opened = false
    sidebar.value.withoutAnimation = withoutAnimation
  }

  return {
    sidebar,
    toggleSidebar,
    closeSidebar
  }
})
