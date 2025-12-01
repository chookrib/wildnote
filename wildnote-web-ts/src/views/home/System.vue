<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { message } from 'ant-design-vue';
import axios from '@/utility/axios-utility';

const windowLocationOrigin = window.location.origin;
const settingContent = ref('');

onMounted(() => {
  loadSetting();
});

const loadSetting = () => {
  axios.get('/api/system/setting').then((response) => {
    settingContent.value = response.data.data.content;
  });
};

const reloadAllNote = () => {
  axios.get('/api/explore/reload').then((response) => {
    message.success('重新加载所有笔记成功');
  });
};

const testRemindPanelVisible = ref(false);
const testRemindMessage = ref('');
const openTestRemindPanel = () => {
  testRemindPanelVisible.value = true;
};

const testRemind = () => {
  axios.get('/api/system/test/remind?message=' + testRemindMessage.value).then((response) => {
    testRemindPanelVisible.value = false;
    message.success('测试提醒消息发送成功');
  });
};

const testSmsCodePanelVisible = ref(false);
const testSmsCodeMobile = ref('');
const testSmsCodeValue = ref('');
const openTestSmsCodePanel = () => {
  testSmsCodePanelVisible.value = true;
};

const testSmsCode = () => {
  axios
    .get('/api/system/test/sms-code?mobile=' + testSmsCodeMobile.value + '&code=' + testSmsCodeValue.value)
    .then((response) => {
      testSmsCodePanelVisible.value = false;
      message.success('测试短信验证码发送成功');
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
      <a-button type="primary" @click="reloadAllNote">重新加载所有笔记</a-button>
      <a-button type="primary" @click="openTestRemindPanel">测试发送提醒消息</a-button>
      <a-button type="primary" @click="openTestSmsCodePanel">测试发送短信验证码</a-button>
    </a-space>
  </a-card>

  <a-modal v-model:open="testRemindPanelVisible" title="测试发送提醒消息" @ok="testRemind">
    <a-form :label-col="{ span: 5 }" :wrapper-col="{ span: 19 }">
      <a-form-item label="提醒内容">
        <a-input v-model:value="testRemindMessage"></a-input>
      </a-form-item>
    </a-form>
  </a-modal>

  <a-modal v-model:open="testSmsCodePanelVisible" title="测试发送短信验证码" @ok="testSmsCode">
    <a-form :label-col="{ span: 5 }" :wrapper-col="{ span: 19 }">
      <a-form-item label="手机">
        <a-input v-model:value="testSmsCodeMobile"></a-input>
      </a-form-item>
      <a-form-item label="验证码">
        <a-input v-model:value="testSmsCodeValue"></a-input>
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
