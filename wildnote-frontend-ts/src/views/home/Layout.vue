<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router';
import zhCN from 'ant-design-vue/es/locale/zh_CN';
import * as localStorageUtility from '@/utility/local-storage-utility';
import { showConfirm } from '@/utility/confirm-utility';
import router from '@/router';

const title = window.location.hostname;

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
  <a-config-provider :locale="zhCN">
    <div class="layout-header">
      <img src="/img/logo192.png" alt="" style="height: 20px; filter: brightness(0) invert(1)" />
      <div style="flex-grow: 1">
        <RouterLink to="/">{{ title }}</RouterLink>
      </div>
      <RouterLink to="/explore">浏览</RouterLink>
      <RouterLink to="/search">搜索</RouterLink>
      <RouterLink to="/remind">提醒</RouterLink>
      <RouterLink to="/system">系统</RouterLink>
      <RouterLink to="/log">日志</RouterLink>
      <a href="javascript:void(0)" @click="logout">注销</a>
    </div>
    <div class="layout-content">
      <RouterView :key="$route.fullPath" />
    </div>
  </a-config-provider>
</template>

<style scoped>
.layout-header {
  padding: 10px;
  height: 40px;
  position: sticky;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  gap: 10px;
  background-color: #000000;
}

.layout-header > div {
  color: #ffffff;
}

.layout-header a {
  white-space: nowrap;
  color: #ffffff;
  text-decoration: none;

  &:hover {
    color: #1677ff;
  }
}
</style>
