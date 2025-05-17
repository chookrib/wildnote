<script setup>
import { onMounted } from 'vue'
import axios from '../utils/axios'
import router from '../router'
import { ref, computed } from 'vue'
import { ReloadOutlined, SearchOutlined } from '@ant-design/icons-vue'
import { showDateTime } from '@/utils/dateTime'

onMounted(() => {
  
})

</script>

<template>
  <a-layout>
    <Header>
      <span style=" padding: 10px; font-weight: bold;">主页</span>
    </Header>
    <a-layout-content>

      <router-link to="/explore">目录</router-link>
      <router-link to="/search">搜索</router-link>
      <router-link to="/cron">提醒</router-link>

    </a-layout-content>
  </a-layout>
</template>

<style scoped>
.ant-layout {
  height: 100%;
}
</style>
