<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { message } from 'ant-design-vue';
import axios from '@/utility/axios-utility';

import * as localStorageUtility from '@/utility/local-storage-utility';
import { showConfirm } from '@/utility/confirm-utility';
import router from '@/router';

const windowLocationOrigin = window.location.origin;
const appEnv = ref('');
const settingContent = ref('');

onMounted(() => {
  loadSetting();
});

const loadSetting = () => {
  axios.get('/api/system/setting').then((response) => {
    appEnv.value = response.data.data.env;
    settingContent.value = response.data.data.content;
  });
};

const reloadAllNote = () => {
  axios.get('/api/explore/reload').then((response) => {
    message.success('重新加载所有笔记成功');
  });
};

const testRemindNotifyPanelVisible = ref(false);
const testRemindNotifyMessage = ref('');
const openTestRemindNotifyPanel = () => {
  testRemindNotifyPanelVisible.value = true;
};

const testRemindNotify = () => {
  axios.get('/api/system/test/remind-notify?message=' + testRemindNotifyMessage.value).then((response) => {
    testRemindNotifyPanelVisible.value = false;
    message.success('测试提醒通知成功');
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
      message.success('测试短信验证码成功');
    });
};

const logout = function () {
  showConfirm({
    title: '注销',
    content: '确定要注销吗？',
    onOk: () => {
      localStorageUtility.deleteAccessToken();
      // window.location.href = '/login.html';
      router.push({ path: '/login', query: { nlr: 'true' } });
    },
  });
};
</script>

<template>
  <a-card>
    <template #title>注销</template>
    <template #extra> </template>
    <a-button type="primary" :danger="true" @click="logout">注销</a-button>
  </a-card>

  <a-card>
    <template #title>当前网址</template>
    <template #extra> </template>
    {{ windowLocationOrigin }}
  </a-card>

  <a-card>
    <template #title>当前运行环境</template>
    <template #extra> </template>
    <a-tag>{{ appEnv }}</a-tag>
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
      <a-button type="primary" @click="openTestRemindNotifyPanel">测试提醒通知</a-button>
      <a-button type="primary" @click="openTestSmsCodePanel">测试短信验证码</a-button>
    </a-space>
  </a-card>

  <a-modal v-model:open="testRemindNotifyPanelVisible" title="测试提醒通知" @ok="testRemindNotify">
    <a-form :label-col="{ span: 5 }" :wrapper-col="{ span: 19 }">
      <a-form-item label="提醒内容">
        <a-input v-model:value="testRemindNotifyMessage"></a-input>
      </a-form-item>
    </a-form>
  </a-modal>

  <a-modal v-model:open="testSmsCodePanelVisible" title="测试短信验证码" @ok="testSmsCode">
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
