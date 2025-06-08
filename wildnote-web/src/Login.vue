<script setup>

import axios from '@/utility/axios-utility.js'
import { reactive } from 'vue'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'
import { setLocalAccessToken } from '@/utility/local-storage-utility.js'

const loginForm = reactive({ username: '', password: '' })

const login = function() {
  axios.post('/api/login', {
    username: loginForm.username,
    password: loginForm.password
  }).then(response => {
    setLocalAccessToken(response.data.data)
    //router.push('/')
    window.location.href = '/'
  })
}
</script>

<template>
  <a-card :hoverable="true" style="width: 300px; padding-top: 50px;">
    <div class="logo-box-outside">
      <div class="logo-box-inside">
        <img src="/img/logo192.png" alt="">
      </div>
    </div>
    <a-form autocomplete="off">
      <a-form-item>
        <a-input v-model:value="loginForm.username">
          <template #prefix>
            <UserOutlined />
          </template>
        </a-input>
      </a-form-item>
      <a-form-item>
        <a-input-password v-model:value="loginForm.password" @keyup.enter="login">
          <template #prefix>
            <LockOutlined />
          </template>
        </a-input-password>
      </a-form-item>
      <div style="text-align: center">
        <a-button type="primary" @click="login">登录</a-button>
      </div>
    </a-form>
  </a-card>
</template>

<style scoped>
body {
  /*background-image: linear-gradient(45deg, #3A6A74, #46537F, #458E56);
  background-size: 400%;
  animation: bg-animation 15s infinite;*/
}

:global(#app) {
  /*min-height: 100vh;*/
  display: flex;
  justify-content: center;
  align-items: center;
}

.logo-box-outside {
  width: 100px;
  height: 100px;
  border: 1px solid #eee;
  border-radius: 50%;
  padding: 10px;
  box-shadow: 0 0 10px #ddd;
  position: absolute;
  left: 50%;
  top: 0;
  transform: translate(-50%, -50%);
  background-color: #fff;
}

.logo-box-inside {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background-color: #eee;
  padding: 15px;
}

.logo-box-inside img {
  width: 100%;
  height: 100%;
}

@keyframes bg-animation {
  0% {
    background-position: 0% 50%;
  }

  50% {
    background-position: 100% 50%;
  }

  100% {
    background-position: 0% 50%;
  }
}
</style>
