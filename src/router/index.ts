import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import { useCurrentUserStore } from '@/stores/currentUser'
const whiteList = ['/xxx']
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      redirect:'/alarm',
      component: HomeView,
      children:[
        {
          path: '/warehouse',
          name: 'warehouse',
          component: () => import('@/views/warehouse/index.vue')
        },
        {
          path: '/house',
          name: 'house',
          component: () => import('@/views/house/index.vue')
        },
        {
          path: '/alarm',
          name: 'alarm',
          component: () => import('@/views/alarm/index.vue')
        },
        {
          path: '/alarmarg',
          name: 'alarmarg',
          component: () => import('@/views/alarm/arg/index.vue')
        },
        {
          path: '/data',
          name: 'data',
          component: () => import('@/views/data/index.vue')
        },
        {
          path: '/data/detail',
          name: 'data_detail',
          component: () => import('@/views/data/detail.vue')
        },
        {
          path: '/user',
          name: 'user',
          component: () => import('@/views/user/index.vue')
        },
        {
          path: '/entry',
          name: 'entry',
          component: () => import('@/views/entry/index.vue')
        },
        {
          path: '/show',
          name: 'show',
          component: () => import('@/views/show/index.vue')
        },
      ]
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue')
    }
  ]
})
const adminRoutes = ['/warehouse','/user']
router.beforeEach((to, from, next) => {
  const currentUser = useCurrentUserStore()
  if (whiteList.includes(to.path)) {
    next()
  } else {
    if (to.name !== 'login' && !currentUser.isLogin()) {
      next({ name: 'login' })
    } else if (to.name == 'login' && currentUser.isLogin()) {
      next({ name: 'home' })
    } else {
      if(currentUser.getUserDetail()?.username != 'admin' &&  adminRoutes.includes(to.path)){
        next({ name: 'home' })
      }
      next()
    }
  }

})
export default router
