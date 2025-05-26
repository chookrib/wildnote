<script setup>
import { RouterLink, RouterView } from 'vue-router'
import zhCN from 'ant-design-vue/es/locale/zh_CN'
import { showConfirm } from '@/utils/confirmUtil'
import { removeLocalAccessToken } from '@/utils/localStorageUtil'

console.log(import.meta.env)

const logout = function() {
  showConfirm('确定要注销吗？', function() {
    removeLocalAccessToken()
    window.location.href = '/login.html'
  })
}
</script>

<template>
  <a-config-provider :locale="zhCN">
    <a-layout style="height: 100vh;">
      <a-layout-header>
        <img
          src="/img/logo192.png"
          alt=""
          style="height: 20px; filter: brightness(0) invert(1);"
        />
        <RouterLink to="/">首页</RouterLink>
        <RouterLink to="/explore">浏览</RouterLink>
        <RouterLink to="/search">搜索</RouterLink>
        <RouterLink to="/cron">提醒</RouterLink>
        <div style="flex-grow: 1"></div>
        <a href="javascript:void(0)" @click="logout">注销</a>
      </a-layout-header>
      <a-layout-content style="margin-top: 40px; height: 100%;">
        <RouterView :key="$route.fullPath" />
      </a-layout-content>
    </a-layout>
  </a-config-provider>
</template>

<style scoped>
.ant-layout-header {
  padding: 10px;
  height: 40px;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  gap: 10px;
}

.ant-layout-header a {
  color: #ffffff;

  &:hover {
    color: #1677ff;
  }
}
</style>
