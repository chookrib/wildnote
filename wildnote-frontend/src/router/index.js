import { createRouter, createWebHistory, createWebHashHistory } from 'vue-router'
// import IndexView from '@/views/IndexView.vue'

const router = createRouter({
  //history: createWebHistory(import.meta.env.BASE_URL),
  history: createWebHashHistory(),
  routes: [
    {
      path: '/',
      name: 'index',
      // component: IndexView
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('@/view/IndexView.vue')
    },
    {
      path: '/explore',
      name: 'explore',
      component: () => import('@/view/ExploreView.vue')
    },
    {
      path: '/search',
      name: 'search',
      component: () => import('@/view/SearchView.vue')
    },
    {
      path: '/remind',
      name: 'remind',
      component: () => import('@/view/RemindView.vue')
    },
    {
      path: '/note',
      name: 'note',
      component: () => import('@/view/NoteView.vue')
    },
    {
      path: '/system',
      name: 'system',
      component: () => import('@/view/SystemView.vue')
    },
    {
      path: '/log',
      name: 'log',
      component: () => import('@/view/LogView.vue')
    },
    {
      path: '/code-mirror-demo',
      name: 'code-mirror-demo',
      component: () => import('@/view/CodeMirrorDemoView.vue')
    }

  ]
})

export default router
