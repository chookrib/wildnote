<script setup lang="ts">
const hostname = window.location.hostname;
const link = document.createElement('link');
link.rel = 'manifest';
link.href = import.meta.env.VITE_API_URL + 'manifest/manifest-' + hostname + '.json';
document.head.appendChild(link);

const mode = import.meta.env.MODE;
if (mode === 'dev') {
  console.log(window.location);
  console.log(import.meta.env);
}
</script>

<template>
  <router-view />
</template>

<style scoped></style>
