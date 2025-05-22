<script setup>
import { RouterLink, RouterView } from 'vue-router'
import zhCN from 'ant-design-vue/es/locale/zh_CN'
import { confirm } from './utils/confirm'

console.log(import.meta.env)

const logout = function() {
  confirm('确定要注销吗？', function() {
    localStorage.removeItem('accessToken')
    window.location.href = '/login.html'
  })
}
</script>

<template>
  <a-config-provider :locale="zhCN">
    <a-layout>
      <a-layout-header>
        <img
          src="/img/logo.png"
          alt=""
          style="height: 20px; filter: brightness(100)"
        />
        <RouterLink to="/">首页</RouterLink>
        <RouterLink to="/explore">浏览</RouterLink>
        <RouterLink to="/search">搜索</RouterLink>
        <RouterLink to="/cron">提醒</RouterLink>
        <div style="flex-grow: 1"></div>
        <a href="#" @click="logout">注销</a>
      </a-layout-header>
      <a-layout-content style="margin-top: 40px">
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
