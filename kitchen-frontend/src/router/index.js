import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

import Layout from '@/layout'

export const constantRoutes = [
  {
    path: '/login',
    component: () => import('@/views/login/index'),
    hidden: true
  },
  {
    path: '/register',
    component: () => import('@/views/register/index'),
    hidden: true
  },
  {
    path: '/404',
    component: () => import('@/views/404'),
    hidden: true
  },
  {
    path: '/ai',
    component: Layout,
    hidden: true,
    children: [
      {
        path: '',
        name: 'AI',
        component: () => import('@/views/ai/AiPage.vue'),
        meta: { title: 'AI营养助手', icon: 'el-icon-s-operation', roles: ['admin', 'resident'] }
      }
    ]
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index'),
        meta: { title: '首页', icon: 'el-icon-s-home' }
      }
    ]
  }
]

export const asyncRoutes = [
  {
    path: '/mealmanage',
    name: 'MealManage',
    component: Layout,
    redirect: '/mealmanage/dishinfo',
    alwaysShow: true,
    meta: {
      title: '配餐管理',
      icon: 'el-icon-food'
    },
    children: [
      {
        path: 'dishinfo',
        name: 'DishInfo',
        component: () => import('@/views/dish/index'),
        meta: {
          title: '菜品管理',
          icon: 'el-icon-dish',
          roles: ['admin', 'resident'],
          noCache: true
        }
      },
      {
        path: 'dishtype',
        name: 'DishType',
        component: () => import('@/views/dishtype/index'),
        meta: {
          title: '菜品分类管理',
          icon: 'el-icon-collection-tag',
          roles: ['admin'],
          noCache: true
        }
      },
      {
        path: 'order',
        name: 'Order',
        component: () => import('@/views/order/index'),
        meta: {
          title: '订餐预约管理',
          icon: 'el-icon-tickets',
          roles: ['admin', 'resident'],
          noCache: true
        }
      }
    ]
  },
  {
    path: '/other',
    name: 'Other',
    component: Layout,
    redirect: '/other/resident',
    alwaysShow: true,
    meta: {
      title: '系统管理',
      icon: 'el-icon-setting'
    },
    children: [
      {
        path: 'resident',
        name: 'Resident',
        component: () => import('@/views/resident/index'),
        meta: {
          title: '居民管理',
          icon: 'el-icon-user',
          roles: ['admin'],
          noCache: true
        }
      },
      {
        path: 'password',
        name: 'Password',
        component: () => import('@/views/password/index'),
        meta: {
          title: '个人信息',
          icon: 'el-icon-user',
          roles: ['admin', 'resident'],
          noCache: true
        }
      },
      {
        path: 'ai-assistant',
        name: 'AiAssistant',
        component: () => import('@/views/ai/AiPage.vue'),
        meta: {
          title: 'AI营养助手',
          icon: 'el-icon-s-opportunity',
          roles: ['admin', 'resident'],
          noCache: true
        }
      }
    ]
  },
  { path: '*', redirect: '/404', hidden: true }
]

const createRouter = () =>
  new Router({
    scrollBehavior: () => ({ y: 0 }),
    routes: constantRoutes
  })

const router = createRouter()

export function resetRouter() {
  const newRouter = createRouter()
  router.matcher = newRouter.matcher
}

export default router
