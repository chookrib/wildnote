<script setup>
import { createVNode } from 'vue'
import { Modal } from 'ant-design-vue'
import { LogoutOutlined, ExclamationCircleOutlined } from '@ant-design/icons-vue'
import router from '@/router'
import * as localStorageUtility from '@/utility/local-storage-utility.js'

function logout() {
  Modal.confirm({
    title: '注销',
    icon: createVNode(ExclamationCircleOutlined),
    content: '确定要注销吗？',
    okText: '确认',
    cancelText: '取消',
    onOk: function() {
      localStorageUtility.dropAccessToken()
      router.push('/login')
    },
    onCancel: function() {
    }
  })
}
</script>

<template>
  <a-layout-header>
    <div style="display: flex; align-items: center;">
      <img src="/img/logo192c.png" alt="" style="height: 20px; filter: brightness(100);">
    </div>
    <div style="flex-grow: 1; display: flex; align-items: center; white-space: nowrap; gap: 10px;">
      <slot></slot>
    </div>
    <div>
      <a-button @click=logout>
        <template #icon>
          <LogoutOutlined />
        </template>
        注销
      </a-button>
    </div>
  </a-layout-header>
</template>

<style scoped>
.ant-layout-header {
  padding: 0px;
  height: 40px;
  color: #fff;
  background-color: #3c3c3c;
  display: flex;
  gap: 10px;
  padding-left: 10px;
  padding-right: 10px;
  justify-content: space-between;
  align-items: center;
}
</style>
