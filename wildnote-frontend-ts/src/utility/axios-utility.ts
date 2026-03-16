import axios from 'axios';
import { message } from 'ant-design-vue';
import * as localStorageUtility from '@/utility/local-storage-utility';
import router from '@/router';

const instance = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  //baseURL: "/",
});

instance.interceptors.request.use((config) => {
  config.headers['Access-Token'] = localStorageUtility.getAccessToken();
  return config;
});

instance.interceptors.response.use(
  (response) => {
    // console.log(response);

    // try {
    //   JSON.parse(response.data);
    // } catch (e) {
    //   console.log(e);
    //   router.push('/login');
    //   throw new Error(e);
    // }

    if (response.data.success) {
      return response;
    }

    if (response.data.code === -1) {
      // window.location.href = '/login.html';
      router.push({ path: '/login', query: { nlr: 'true' } });
      return Promise.reject(new Error(response.data.message));
    } else {
      // alert(response.data.message)
      message.error(response.data.message);
      return Promise.reject(new Error(response.data.message));
    }
  },
  (error) => {
    // axios全局错误处理
    console.log(error);
    message.error(error.message);
    return Promise.reject(new Error(error.message));
  },
);

export default instance;
