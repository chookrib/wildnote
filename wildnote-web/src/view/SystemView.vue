<script setup>
import { onMounted, ref } from 'vue'
import axios from '@/utility/axios-utility.js'

const remindMessage = ref('')
const smsMobile = ref('')
const smsMessage = ref('')

const remindLog = ref('')
const smsLog = ref('')

onMounted(() => {
  loadRemindLog()
  loadSmsLog()
})

const loadRemindLog = () => {
  axios.get('/api/system/remind/recent-log').then(response => {
    remindLog.value = response.data.data.replace(
      /(\s\\[^|]+)/g,
      (match, p1) =>
        `<a href="#/note?path=${encodeURIComponent(p1.trim())}">${p1}</a>`,
    )
  })
}

const loadSmsLog = () => {
  axios.get('/api/system/sms/recent-log').then(response => {
    smsLog.value = response.data.data
  })
}

const modalRemindOpen = ref(false)
const openRemindModal = () => {
  modalRemindOpen.value = true
}

const testRemind = () => {
  axios.get('/api/system/remind/test?message=' + remindMessage.value)
    .then(response => {
      modalRemindOpen.value = false
      loadRemindLog()
    })
}

const modalSmsOpen = ref(false)
const openSmsModal = () => {
  modalSmsOpen.value = true
}

const testSms = () => {
  axios.get(
    '/api/system/sms/test?mobile=' +
      smsMobile.value +
      '&message=' +
      smsMessage.value,
  ).then(response => {
    modalSmsOpen.value = false
    loadSmsLog()
  })
}
</script>

<template>
  <a-card>
    <template #title> 最新提醒日志</template>
    <template #extra>
      <a-button type="primary" @click="openRemindModal">测试</a-button>
    </template>
    <div class="log" v-html="remindLog"></div>
  </a-card>

  <a-modal
    v-model:open="modalRemindOpen"
    title="测试发送提醒消息"
    @ok="testRemind"
  >
    <a-form :label-col="{ span: 5 }" :wrapper-col="{ span: 19 }">
      <a-form-item label="提醒内容">
        <a-input v-model:value="remindMessage"></a-input>
      </a-form-item>
    </a-form>
  </a-modal>

  <a-card>
    <template #title> 最新短信日志</template>
    <template #extra>
      <a-button type="primary" @click="openSmsModal">测试</a-button>
    </template>
    <div class="log" v-html="smsLog"></div>
  </a-card>

  <a-modal v-model:open="modalSmsOpen" title="测试发送短信消息" @ok="testSms">
    <a-form :label-col="{ span: 5 }" :wrapper-col="{ span: 19 }">
      <a-form-item label="手机">
        <a-input v-model:value="smsMobile"></a-input>
      </a-form-item>
      <a-form-item label="短信内容">
        <a-input v-model:value="smsMessage"></a-input>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<style scoped>
.log {
  white-space: pre-wrap;
  word-wrap: anywhere;
  font-size: 12px;
}
</style>
