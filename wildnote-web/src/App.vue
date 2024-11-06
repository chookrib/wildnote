<script setup>
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
  <!-- <RouterLink to="/">Home</RouterLink> -->
  <RouterView :key="$route.fullPath" />
</template>

<style scoped></style>
