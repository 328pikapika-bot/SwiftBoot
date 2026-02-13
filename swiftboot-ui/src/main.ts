import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'virtual:uno.css'

import App from './App.vue'
import router from './router'
import i18n from './locales'
import './styles/index.scss'

import DictTag from '@/components/DictTag/index.vue'
import Pagination from '@/components/Pagination/index.vue'

const app = createApp(App)

// 注册全局组件
app.component('DictTag', DictTag)
app.component('Pagination', Pagination)

// 注册 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(i18n)
app.use(ElementPlus) // Remove static locale here, handle in App.vue

app.mount('#app')
