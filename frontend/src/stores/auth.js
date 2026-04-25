import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/services/api'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(JSON.parse(localStorage.getItem('user')) || null)
  const token = ref(localStorage.getItem('token') || null)

  const isAuthenticated = computed(() => !!token.value)

  async function login(email, password) {
    try {
      const response = await api.post('/auth/login', { email, password })
      if (response.code === 0) {
        token.value = response.data.accessToken
        user.value = response.data.user
        localStorage.setItem('token', token.value)
        localStorage.setItem('user', JSON.stringify(user.value))
        return true
      }
      return false
    } catch (error) {
      console.error('Login failed:', error)
      throw error
    }
  }

  function logout() {
    token.value = null
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return {
    user,
    token,
    isAuthenticated,
    login,
    logout
  }
})
