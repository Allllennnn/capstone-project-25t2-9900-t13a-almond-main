import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import { useAuthStore } from '@/store/auth'

import axios from 'axios'

// 全局 axios 拦截器，自动加 token
axios.interceptors.request.use(config => {
  const token = localStorage.getItem('authToken')
  if (token) {
    config.headers.Authorization = 'Bearer ' + token
  }
  return config
})

const app = createApp(App)
const pinia = createPinia()
app.use(pinia)

// 恢复登录状态
useAuthStore().initAuth()

app.use(router)
app.use(ElementPlus)
app.mount('#app')
