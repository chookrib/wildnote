<script setup>
import { onMounted } from 'vue'
import axios from '../utils/axios'
import router from '../router'
import { ref, computed } from 'vue'

const crons = ref([])

onMounted(() => {
  axios.get('/api/note/cron').then(response => {
    crons.value = response.data.data
  })
})

function editNote(note) {
  router.push({ path: '/note', query: { path: note.relPath } })
}
</script>

<template>
  <div v-for="(cron, index) in crons" :key="index">
    <div>
      <span>{{ cron }}</span>
    </div>
  </div>
</template>

<style scoped>
</style>
