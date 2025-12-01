<script setup lang="ts">
import { reactive } from 'vue';
import { LockOutlined, UserOutlined } from '@ant-design/icons-vue';
import axios from '@/utility/axios-utility';
import * as localStorageUtility from '@/utility/local-storage-utility';
import { useRoute } from 'vue-router';
import router from '@/router';

const route = useRoute();
if (route.query.nlr !== 'true') router.push({ path: '/index' }); // 尝试进入系统

const loginForm = reactive({ username: '', password: '' });

const login = () => {
  axios
    .post('/api/login', {
      username: loginForm.username,
      password: loginForm.password,
    })
    .then((response) => {
      localStorageUtility.setAccessToken(response.data.data.accessToken);
      // window.location.href = '/index';
      router.push({ path: '/index' });
    });
};
</script>

<template>
  <div class="login-page">
    <a-card :hoverable="true" class="login-box">
      <div class="logo-box-outside">
        <div class="logo-box-inside">
          <img src="/img/logo192.png" alt="" />
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
  </div>
</template>

<style scoped>
.login-page {
  width: 100vw;
  height: 100vh;
  background-image: linear-gradient(45deg, #3a6a74, #46537f, #458e56);
  background-size: 400%;
  animation: bg-animation 15s infinite;
  display: flex;
  align-items: center;
  justify-content: center;
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

.login-box {
  width: 300px;
  padding-top: 50px;
}

.logo-box-outside {
  width: 100px;
  height: 100px;
  border: 1px solid #eeeeee;
  border-radius: 50%;
  padding: 10px;
  box-shadow: 0 0 10px #dddddd;
  position: absolute;
  left: 50%;
  top: 0;
  transform: translate(-50%, -50%);
  background-color: #ffffff;
}

.logo-box-inside {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background-color: #eeeeee;
  padding: 15px;
}

.logo-box-inside img {
  width: 100%;
  height: 100%;
}
</style>
