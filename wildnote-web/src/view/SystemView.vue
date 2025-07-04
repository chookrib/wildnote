<script setup>
import { onMounted, ref } from 'vue'
import axios from '@/utility/axios-utility.js'
import { message } from 'ant-design-vue'

const remindMessage = ref('')
const smsMobile = ref('')
const smsCode = ref('')

const setting = ref('')
const remindLog = ref('')
const smsLog = ref('')

onMounted(() => {
  loadSetting()
  loadRemindLog()
  loadSmsLog()
})

const loadSetting = () => {
  axios.get('/api/system/setting').then(response => {
    setting.value = response.data.data
  })
}

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
      '&code=' +
      smsCode.value,
  ).then(response => {
    modalSmsOpen.value = false
    loadSmsLog()
  })
}
</script>

<template>
  <a-card>
    <template #title>系统设置</template>
    <template #extra>
    </template>
    <div class="json">{{setting}}</div>
  </a-card>

  <a-card>
    <template #title>分享</template>
    <template #extra>
      <a-button type="primary" @click="message.info('待开发')">添加</a-button>
    </template>
  </a-card>

  <a-card>
    <template #title>最新提醒日志</template>
    <template #extra>
      <a-space>
        <a-button type="primary" @click="openRemindModal">测试</a-button>
      </a-space>
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
    <template #title>最新短信日志</template>
    <template #extra>
      <a-button type="primary" @click="openSmsModal">测试</a-button>
    </template>
    <div class="log">{{smsLog}}</div>
  </a-card>

  <a-modal v-model:open="modalSmsOpen" title="测试发送短信验证码" @ok="testSms">
    <a-form :label-col="{ span: 5 }" :wrapper-col="{ span: 19 }">
      <a-form-item label="手机">
        <a-input v-model:value="smsMobile"></a-input>
      </a-form-item>
      <a-form-item label="验证码">
        <a-input v-model:value="smsCode"></a-input>
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
.json {
  white-space: pre-wrap;
  word-wrap: anywhere;
  font-size: 12px;
}
</style>
