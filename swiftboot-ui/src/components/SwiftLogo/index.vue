<template>
  <svg width="40" height="40" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <linearGradient id="logo-gradient" x1="0" y1="0" x2="40" y2="40" gradientUnits="userSpaceOnUse">
        <stop offset="0%" stop-color="#409EFF" />
        <stop offset="100%" stop-color="#79bbff" />
      </linearGradient>
      <filter id="glow" x="-50%" y="-50%" width="200%" height="200%">
        <feGaussianBlur stdDeviation="2" result="blur" />
        <feComposite in="SourceGraphic" in2="blur" operator="over" />
      </filter>
    </defs>
    <path 
      d="M20 4L24 14H36L22 22L26 36L10 20H4L18 12L20 4Z" 
      fill="url(#logo-gradient)" 
      stroke="white" 
      stroke-width="1.5" 
      stroke-linejoin="round"
      filter="url(#glow)"
    />
  </svg>
</template>

<script setup lang="ts">
// Simple, lightweight, geometric "Bolt" logo representing speed (Swift)
</script>

<style scoped>
svg {
  filter: drop-shadow(0 4px 6px rgba(64, 158, 255, 0.3));
}
</style>
