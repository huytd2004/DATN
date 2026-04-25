import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/auth/LoginView.vue')
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/auth/RegisterView.vue')
    },
    {
      path: '/profile',
      name: 'Profile',
      component: () => import('@/views/profile/ProfileView.vue')
    },
    {
      path: '/',
      redirect: '/login'
    }
  ]
})

export default router
