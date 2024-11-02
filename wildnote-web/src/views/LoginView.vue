<script setup>

import router from "../router";
import axios from '../axios'
import { reactive } from "vue";
import { User, Lock } from '@element-plus/icons-vue'

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
      <el-form id="login-form" label-width="auto">
        <el-form-item>
          <el-input v-model="loginForm.username" :prefix-icon="User"></el-input>
        </el-form-item>
        <el-form-item>
          <el-input v-model="loginForm.password" :prefix-icon="Lock" type="password"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button id="login-button" type="primary" @click="login">登录</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
#login-container {
  background-color: #3A6A74;
  height: 100%;
}

#login-box {
  width: 450px;
  height: 270px;
  background-color: #fff;
  border-radius: 3px;
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%)
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

  img {
    width: 100%;
    height: 100%;
  }
}

#login-form {
  position: absolute;
  bottom: 0;
  width: 100%;
  padding: 20px;
}

#login-button {
  margin: auto;
}
</style>
