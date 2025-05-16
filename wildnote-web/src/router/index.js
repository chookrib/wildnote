import { createRouter, createWebHistory, createWebHashHistory } from 'vue-router'
// import IndexView from '../views/IndexView.vue'

const router = createRouter({
  //history: createWebHistory(import.meta.env.BASE_URL),
  history: createWebHashHistory(),
  routes: [
    {
      path: '/',
      name: 'index',
      component: () => import('../views/IndexView.vue')
    },
    {
      path: '/explore',
      name: 'explore',
      // component: IndexView
      component: () => import('../views/ExploreView.vue')
    },
    {
      path: '/login',
      name: 'login',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('../views/LoginView.vue')
    },
    {
      path: '/note',
      name: 'note',
      component: () => import('../views/NoteView.vue')
    }
  ]
})

export default router
