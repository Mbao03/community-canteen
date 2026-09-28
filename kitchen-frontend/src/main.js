import Vue from 'vue'

import 'normalize.css/normalize.css'

import ElementUI from 'element-ui'
import "@/api/initialize"
import 'element-ui/lib/theme-chalk/index.css'

import * as echarts from 'echarts'

import '@/styles/index.scss' // global css

import App from './App'
import store from './store'
import './assets/font/iconfont.css'
import router from './router'


import '@/permission'


Vue.use(ElementUI)
Vue.prototype.$echarts = echarts

Vue.config.productionTip = false

new Vue({
  el: '#app',
  router,
  mounted() {
  },
  store,
  render: h => h(App)
})
