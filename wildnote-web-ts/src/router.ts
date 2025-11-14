// import { createRouter, createWebHistory, createWebHashHistory } from 'vue-router'
import { createRouter, createWebHashHistory } from 'vue-router'
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
            component: () => import('@/views/IndexView.vue')
        },
        {
            path: '/explore',
            name: 'explore',
            component: () => import('@/views/ExploreView.vue')
        },
        {
            path: '/search',
            name: 'search',
            component: () => import('@/views/SearchView.vue')
        },
        {
            path: '/remind',
            name: 'remind',
            component: () => import('@/views/RemindView.vue')
        },
        {
            path: '/note',
            name: 'note',
            component: () => import('@/views/NoteView.vue')
        },
        {
            path: '/system',
            name: 'system',
            component: () => import('@/views/SystemView.vue')
        },
        {
            path: '/log',
            name: 'log',
            component: () => import('@/views/LogView.vue')
        },
        {
            path: '/code-mirror-demo',
            name: 'code-mirror-demo',
            component: () => import('@/views/CodeMirrorDemoView.vue')
        }
    ]
})

export default router
