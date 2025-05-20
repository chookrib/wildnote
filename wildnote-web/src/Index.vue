<script setup>
import { RouterLink, RouterView } from 'vue-router'
import zhCN from 'ant-design-vue/es/locale/zh_CN'

import router from './router'
import { createVNode } from 'vue'
import { Modal } from 'ant-design-vue'
import { ExclamationCircleOutlined } from '@ant-design/icons-vue'

console.log(import.meta.env);

const logout = function () {
  Modal.confirm({
    title: '注销',
    icon: createVNode(ExclamationCircleOutlined),
    content: '确定要注销吗？',
    okText: '确认',
    cancelText: '取消',
    onOk: function() {
      localStorage.removeItem('accessToken')
      //router.push('/login')
      window.location.href = '/login.html'
    },
    onCancel: function() {
    }
  })
}

</script>

<template>
  <a-config-provider :locale="zhCN">
    <a-layout>
      <a-layout-header style="position: fixed; top: 0; left: 0; right: 0; z-index: 1000; display: flex; height: 40px; align-items: center; padding: 10px; gap: 10px;">
        <img src="/img/logo.png" alt="" style="height: 20px; filter: brightness(100);">
        <RouterLink to="/">首页</RouterLink>
        <RouterLink to="/explore">浏览</RouterLink>
        <RouterLink to="/search">搜索</RouterLink>
        <RouterLink to="/cron">提醒</RouterLink>
        <div style="flex-grow: 1;">
        </div>
        <a href="#" @click="logout">注销</a>
      </a-layout-header>
      <a-layout-content style="margin-top: 40px;">
        <RouterView :key="$route.fullPath" />
      </a-layout-content>
    </a-layout>
  </a-config-provider>
</template>

<style scoped>
.ant-layout-header a {
  color: #ffffff;
  &:hover {
    color: #1677ff;
  }
}

</style>
