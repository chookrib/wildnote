<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import Cherry from 'cherry-markdown'
import 'cherry-markdown/dist/cherry-markdown.css'
import { message } from 'ant-design-vue'
import router from '@/router'
import axios from '@/utility/axios-utility.js'
import { showDateTime } from '@/utility/datetime-utility.js'

const route = useRoute()
const notePath = route.query.path

const lastSaveTime = ref(null)

const cherryInstance = defineModel('cherryInstance')

onMounted(() => {
  axios.post('/api/note/get', {
    path: notePath
  }).then(response => {
    cherryInstance.value = new Cherry({
      id: 'markdown-container',
      value: response.data.data.content,
      themeSettings: {
        mainTheme: 'light'
      }
    })
  })
})

function saveNote() {
  axios.post('/api/note/save', {
    path: notePath,
    content: cherryInstance.value.getValue()
  }).then(response => {
    lastSaveTime.value = new Date()
    message.success('保存成功')
  })
}

function noteIndex() {
  router.push('/index')
}
</script>

<template>
  <a-layout>
    <Header>
      <a-button @click=noteIndex>返回目录</a-button>
      <span>{{ notePath }}</span>
      <a-button type="primary" @click=saveNote>保存</a-button>
      <span v-if="lastSaveTime">最后保存于 {{ showDateTime(lastSaveTime) }}</span>
    </Header>
    <a-layout-content>
      <div id="markdown-container"></div>
    </a-layout-content>
  </a-layout>
</template>

<style scoped>
.ant-layout {
  height: 100%;
}

#markdown-container {
}
</style>
