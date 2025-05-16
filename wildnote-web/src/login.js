import './assets/main.css'

import { createApp } from 'vue'
import Login from './Login.vue'
//import router from './router'

const app = createApp(Login)

// app.use(router)

app.mount('#app')
