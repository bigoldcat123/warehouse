import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import { useCurrentUserStore } from '@/stores/currentUser'
const whiteList = ['/xxx', '/external-login']
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      redirect:'/house',
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
          path: '/warehouseSettings',
          name: 'warehouseSettings',
          component: () => import('@/views/warehouseSettings/index.vue')
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
          path:'/wind',
          name:'wind',
          component: () => import('@/views/wind/index.vue')
        },
        {
          path:'/tempLine',
          name:'tempLine',
          component: () => import('@/views/tempLine/index.vue')
        },
      ]
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue')
    },
    {
      path: '/external-login',
      name: 'external-login',
      component: () => import('@/views/ExternalLogin.vue')
    },
    {
      path:'/display',
      name:"dispaly",
      component: () => import("@/views/display/index.vue")
    },{
      path: '/show',
      name: 'show',
      component: () => import('@/views/show/index.vue')
    },
    {
      path: '/showCarousel',
      name: 'showCarousel',
      component: () => import('@/views/showCarousel/index.vue')
    },
    {
      path: '/yuntu',
      name: 'yuntu',
      component: () => import('@/views/yuntu/index.vue')
    },
    {
      path: '/tongfeng',
      name: 'tongfeng',
      component: () => import('@/views/tongfeng/index.vue')
    },
    {
      path:'/panel',
      name:'panel',
      component:() => import('@/views/panel/index.vue')
    },
    {
      path:'/wsss',
      name:'ws',
      component:() => import('@/views/ws/index.vue')
    },{
      path:"/nhl",
      name:"nhl",
      component:() => import('@/views/nhl/index.vue')
    },{
      path:"/gfkt",
      name:"gfkt",
      component:() => import('@/views/gf_kt/index.vue')
    },{
      path:'/outter',
      name:'outter',
      component:() => import('@/views/outter/index.vue')
    },{
      path:'/granary3d',
      name:'granary3d',
      component:() => import('@/views/granary3d/index.vue')
    },{
      path:'/humidity3d',
      name:'humidity3d',
      component:() => import('@/views/humidity3d/index.vue')
    },{
      path:'/gas3d',
      name:'gas3d',
      component:() => import('@/views/gas3d/index.vue')
    },{
      path:'/heatmap',
      name:'heatmap',
      component:() => import('@/views/heatmap/index.vue')
    },{
      path:'/humidityHeatmap',
      name:'humidityHeatmap',
      component:() => import('@/views/humidityHeatmap/index.vue')
    },{
      path:'/gasHeatmap',
      name:'gasHeatmap',
      component:() => import('@/views/gasHeatmap/index.vue')
    }
  ],
  scrollBehavior(to,from,savedPosition) {
    if (savedPosition) {
      return savedPosition
    }
    const cachedRoutes = ['house']
    if (cachedRoutes.includes(to.name as string)) {
      return false as any
    }
    return {top:0}
  }
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
      next({ name: 'house' })
    } else {
      if(currentUser.getUserDetail()?.username != 'admin' &&  adminRoutes.includes(to.path)){
        next({ name: 'house' })
      }
      next()
    }
  }

})
export default router
