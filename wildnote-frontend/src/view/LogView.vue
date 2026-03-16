<script setup>
import { onMounted, ref } from 'vue'
import axios from '@/utility/axios-utility.js'
import { message } from 'ant-design-vue'

const logTypes = ref('')
const logType = ref('NOTE')
const logContent = ref('')
const logHasMore = ref(true) // 是否还有更多日志可以加载
const logOffset = ref(0)    // 开始读取日志的负偏移位置

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
  axios.get('/api/log/get?type=' + logType.value + '&offset=' + logOffset.value).then(response => {
    // logContent.value = response.data.data.replace(
    //   /(\s\\[^|]+)/g,
    //   (match, p1) =>
    //     `<a href="#/note?path=${encodeURIComponent(p1.trim())}">${p1}</a>`,
    // )
    let content = response.data.data.result.log
      .split('\n')
      .map(line =>
        /(失败|异常)/.test(line)
          ? `<span style="color: red;">${line}</span>`
          : line
      )
      .join('<br>');
    logContent.value = logContent.value + content + '<hr style="margin: 10px 0;">'
    logHasMore.value = response.data.data.result.hasMore
    logOffset.value = response.data.data.result.offset
  })
}

const handleTabChange = (e) => {
  // console.log(e)
  logType.value = e
  logContent.value = ''
  logHasMore.value = true
  logOffset.value = 0
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
        <div style="text-align: center;">
          <a-button type="primary" @click="loadLogContent" :disabled="!logHasMore">加载更多</a-button>
        </div>
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
