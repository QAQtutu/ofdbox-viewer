import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

const router = new VueRouter({
  routes: [
    // 动态路径参数 以冒号开头
    { path: '/', redirect: { path: '/uploader' } },
    { path: '/viewer', component: () => import('@/views/viewer/index') },
    { path: '/uploader', component: () => import('@/views/uploader/index') },
  ]
})

export default router