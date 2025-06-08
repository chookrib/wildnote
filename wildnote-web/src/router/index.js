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
      path: '/cron',
      name: 'cron',
      component: () => import('@/view/CronView.vue')
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
      path: '/cm',
      name: 'cm',
      component: () => import('@/view/CodeMirrorView.vue')
    }

  ]
})

export default router
