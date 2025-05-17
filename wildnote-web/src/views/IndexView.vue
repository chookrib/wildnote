<script setup>
import { onMounted } from 'vue'
import axios from '../utils/axios'
import router from '../router'
import { ref, computed } from 'vue'
import { ReloadOutlined, SearchOutlined } from '@ant-design/icons-vue'
import { showDateTime } from '@/utils/dateTime'

const indexNotes = ref(null);

onMounted(() => {
  localStorage.setItem('indexNotes', JSON.stringify(['1', '2']))
  indexNotes.value = JSON.parse(localStorage.getItem('indexNotes'))
  console.log(indexNotes)
})

</script>

<template>
  <div v-for="(note, index) in indexNotes" :key="index">
    {{note}}
  </div>
</template>

<style scoped>

</style>
