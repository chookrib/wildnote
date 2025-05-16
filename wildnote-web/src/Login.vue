<script setup>

//import router from "./router";
import axios from './utils/axios'
import { reactive } from "vue";
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'

const loginForm = reactive({ username: '', password: '' })

function login() {
  axios.post('/api/login', {
    username: loginForm.username,
    password: loginForm.password
  }).then(response => {
    localStorage.setItem('accessToken', response.data.data)
    //router.push('/')
    window.location.href = '/'
  })
}
</script>

<template>
  <a-card title=" " :hoverable="true" style="width: 300px;">
    <div class="logo-box-outside">
    <div class="logo-box-inside">
      <img src="/img/logo.png">
    </div>
    </div>
    <a-form autocomplete="off">
      <a-form-item>
        <a-input v-model:value="loginForm.username">
          <template #prefix>
            <user-outlined />
          </template>
        </a-input>
      </a-form-item>
      <a-form-item>
        <a-input-password v-model:value="loginForm.password" @keyup.enter="login">
          <template #prefix>
            <lock-outlined />
          </template>
        </a-input-password>
      </a-form-item>
      <div style="text-align: center">
        <a-button type="primary" @click="login">登录</a-button>
      </div>
    </a-form>
  </a-card>
</template>

<style>
body {
  background: #ececec;
  /*background-image: linear-gradient(45deg, #3A6A74, #46537F, #458E56);
  background-size: 400%;
  animation: bganimation 15s infinite;*/
}

#app {
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
  top: 0px;
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
