<script setup>
import { onMounted, ref } from 'vue'
import axios from '@/utility/axios-utility.js'

const logTypes = ref('')
const logType = ref('NOTE')
const logContent = ref('')

onMounted(() => {
  loadLogTypes()
  loadLogContent()
})

const loadLogTypes = () => {
  axios.get('/api/log/type').then(response => {
    logTypes.value = response.data.data.list
  })
}

const loadLogContent = () => {
  axios.get('/api/log/get?type=' + logType.value).then(response => {
    // logContent.value = response.data.data.replace(
    //   /(\s\\[^|]+)/g,
    //   (match, p1) =>
    //     `<a href="#/note?path=${encodeURIComponent(p1.trim())}">${p1}</a>`,
    // )
    logContent.value = logContent.value + response.data.data.result.log
  })
}

const handleTabChange = (e) => {
  // console.log(e)
  logType.value = e;
  logContent.value = ''
  loadLogContent()
}

</script>

<template>
  <a-card>
    <template #title></template>
    <template #extra></template>
    <a-tabs v-model:activeKey="logType" @change="handleTabChange">
      <a-tab-pane v-for="item in logTypes" :key="item" :tab="item">
        <div class="log" v-html="logContent"></div>
      </a-tab-pane>
    </a-tabs>
  </a-card>

</template>

<style scoped>
.log {
  white-space: pre-wrap;
  word-wrap: anywhere;
  font-size: 12px;
}
.json {
  white-space: pre-wrap;
  word-wrap: anywhere;
  font-size: 12px;
}
</style>
