<script setup>
import { onMounted } from 'vue'
import axios from '../axios'
import router from '../router'
import 'cherry-markdown/dist/cherry-markdown.css'
import Cherry from 'cherry-markdown';

const notes = defineModel('notes')
const cherryInstance = defineModel('cherryInstance')

onMounted(() => {
  loadNoteIndex()
  cherryInstance.value = new Cherry({
    id: 'markdown-container',
    value: '# welcome to cherry-markdown'
  })
})

function loadNoteIndex() {
  axios.get('/api/note/index').then(response => {
    console.log(response)
    notes.value = response.data.data
    console.log(notes.value)
  }, error => {
    console.log(error)
  })
}

function logout() {
  localStorage.removeItem('accessToken')
  router.push('/login')
}

</script>

<template>
  <nav>
    <a href="javascript:void(0)" @click="logout">Logout</a>
  </nav>
  <div id="node-index">
    <a href="javascript:void(0)" @click="loadNoteIndex">Reload</a>
    <div v-for="note in notes" :key="note.path">
      {{ note.name }}
    </div>
  </div>
  <div id="markdown-container">
  </div>
</template>

<style scoped>
#markdown-container {
  margin-top: 20px;
}
</style>
