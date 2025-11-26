<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { message } from 'ant-design-vue';
import axios from '@/utility/axios-utility';

const windowLocationOrigin = window.location.origin;

const remindMessage = ref('');
const smsMobile = ref('');
const smsCode = ref('');

const settingContent = ref('');

onMounted(() => {
  loadSetting();
});

const loadSetting = () => {
  axios.get('/api/system/setting').then((response) => {
    settingContent.value = response.data.data.content;
  });
};

const remindPanelVisible = ref(false);
const openRemindPanel = () => {
  remindPanelVisible.value = true;
};

const testRemind = () => {
  axios.get('/api/system/remind/test?message=' + remindMessage.value).then((response) => {
    remindPanelVisible.value = false;
    message.success('测试提醒消息发送成功');
  });
};

const smsPanelVisible = ref(false);
const openSmsPanel = () => {
  smsPanelVisible.value = true;
};

const testSms = () => {
  axios.get('/api/system/sms/test?mobile=' + smsMobile.value + '&code=' + smsCode.value).then((response) => {
    smsPanelVisible.value = false;
    message.success('测试短信验证码发送成功');
  });
};

const reloadNote = () => {
  axios.get('/api/note/reload').then((response) => {
    message.success('重新加载所有笔记成功');
  });
};
</script>

<template>
  <a-card>
    <template #title>当前网址</template>
    <template #extra> </template>
    {{ windowLocationOrigin }}
  </a-card>

  <a-card>
    <template #title>系统配置</template>
    <template #extra> </template>
    <div class="json">{{ settingContent }}</div>
  </a-card>

  <a-card>
    <template #title>分享</template>
    <template #extra>
      <a-button type="primary" @click="message.info('待开发')">添加</a-button>
    </template>
  </a-card>

  <a-card>
    <template #title>测试</template>
    <a-space>
      <a-button type="primary" @click="reloadNote">重新加载所有笔记</a-button>
      <a-button type="primary" @click="openRemindPanel">测试发送提醒消息</a-button>
      <a-button type="primary" @click="openSmsPanel">测试发送短信验证码</a-button>
    </a-space>
  </a-card>

  <a-modal v-model:open="remindPanelVisible" title="测试发送提醒消息" @ok="testRemind">
    <a-form :label-col="{ span: 5 }" :wrapper-col="{ span: 19 }">
      <a-form-item label="提醒内容">
        <a-input v-model:value="remindMessage"></a-input>
      </a-form-item>
    </a-form>
  </a-modal>

  <a-modal v-model:open="smsPanelVisible" title="测试发送短信验证码" @ok="testSms">
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
.json {
  white-space: pre-wrap;
  word-wrap: anywhere;
  font-size: 12px;
}
</style>
