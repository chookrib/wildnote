<script setup>

import router from "../router";
import axios from '../utils/axios'
import { reactive } from "vue";
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'

const loginForm = reactive({ username: '', password: '' })

function login() {
  axios.post('/api/login', {
    username: loginForm.username,
    password: loginForm.password
  }).then(response => {
    localStorage.setItem('accessToken', response.data.data)
    router.push('/')
  })
}
</script>

<template>
  <div id="login-container">
    <div id="login-box">
      <div id="logo-container">
        <div id="logo-box">
          <img src="/logo.png" alt="">
        </div>
      </div>
      <a-form id="login-form" autocomplete="off">
        <a-form-item>
          <a-input v-model:value="loginForm.username">
            <template #prefix>
              <user-outlined />
            </template>
          </a-input>
        </a-form-item>
        <a-form-item>
          <a-input-password v-model:value="loginForm.password">
            <template #prefix>
              <lock-outlined />
            </template>
          </a-input-password>
        </a-form-item>
        <a-form-item style="text-align: center;">
          <a-button id="login-button" type="primary" shape="round" @click=login>登录</a-button>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>

<style scoped>
#login-container {
  /* background-color: #3A6A74; */
  background-color: #ddd;
  height: 100%;

  /* background-image: linear-gradient(45deg, #2c3e50, #27ae60, #2980b9, #e74c3c, #8e44ad); */
  background-image: linear-gradient(45deg, #3A6A74, #46537F, #458E56);
  background-size: 400%;
  animation: bganimation 15s infinite;
}

@keyframes bganimation {
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

#login-box {
  width: 450px;
  height: 270px;
  background-color: #fff;
  border-radius: 3px;
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  opacity: 1;
}

#logo-container {
  width: 130px;
  height: 130px;
  border: 1px solid #eee;
  border-radius: 50%;
  padding: 10px;
  box-shadow: 0 0 10px #ddd;
  position: absolute;
  left: 50%;
  transform: translate(-50%, -50%);
  background-color: #fff;
}

#logo-box {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background-color: #eee;
  padding: 15px;
}

#logo-box img {
  width: 100%;
  height: 100%;
}

#login-form {
  position: absolute;
  bottom: 0;
  width: 100%;
  padding: 20px;
}

#login-button {
  background-color: #3A6A74;
  color: #fff;

  &:hover {
    background-color: #1677FF !important;

  }
}
</style>
