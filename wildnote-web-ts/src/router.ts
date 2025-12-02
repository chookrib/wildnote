// import { createRouter, createWebHistory, createWebHashHistory } from 'vue-router'
import { createRouter, createWebHashHistory } from 'vue-router';
// import IndexView from '@/views/IndexView.vue'

const router = createRouter({
  //history: createWebHistory(import.meta.env.BASE_URL),
  history: createWebHashHistory(),
  routes: [
    {
      path: '/',
      redirect: '/index',
    },
    {
      path: '/login',
      component: () => import('@/views/Login.vue'),
    },
    {
      path: '/code-mirror-demo',
      component: () => import('@/views/CodeMirrorDemo.vue'),
    },
    {
      path: '/',
      component: () => import('@/views/home/Layout.vue'),
      children: [
        {
          path: '/index',
          component: () => import('@/views/home/Index.vue'),
        },
        {
          path: '/explore',
          component: () => import('@/views/home/Explore.vue'),
        },
        {
          path: '/search',
          component: () => import('@/views/home/Search.vue'),
        },
        {
          path: '/remind',
          component: () => import('@/views/home/Remind.vue'),
        },
        {
          path: '/note',
          component: () => import('@/views/home/Note.vue'),
        },
        {
          path: '/system',
          component: () => import('@/views/home/System.vue'),
        },
        {
          path: '/log',
          component: () => import('@/views/home/Log.vue'),
        },
      ],
    },
  ],
});

export default router;
