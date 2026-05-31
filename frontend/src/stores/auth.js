import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/services/api'

export const useAuthStore = defineStore('auth', () => {
  // ─── State ──────────────────────────────────────────────────────────────
  const user = ref(JSON.parse(localStorage.getItem('user')) || null)
  const accessToken = ref(localStorage.getItem('accessToken') || null)
  const refreshToken = ref(localStorage.getItem('refreshToken') || null)

  // ─── Getters ─────────────────────────────────────────────────────────────
  const isAuthenticated = computed(() => !!accessToken.value)

  // ─── Helpers ──────────────────────────────────────────────────────────────
  function _persist(data) {
    accessToken.value = data.accessToken
    refreshToken.value = data.refreshToken
    localStorage.setItem('accessToken', data.accessToken)
    localStorage.setItem('refreshToken', data.refreshToken)
  }

  function _clear() {
    user.value = null
    accessToken.value = null
    refreshToken.value = null
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('user')
  }

  // ─── Actions ──────────────────────────────────────────────────────────────

  /**
   * Đăng nhập
   * @param {string} email
   * @param {string} password
   * @returns {Promise<void>}  throws nếu lỗi
   */
  async function login(email, password) {
    // api.js unwraps envelope → trả về { status, code, message, data }
    const res = await api.post('/auth/login', { email, password })
    if (res.status !== 'success') {
      throw new Error(res.message || 'Đăng nhập thất bại')
    }
    _persist(res.data) // { accessToken, refreshToken, tokenType }

    // Lấy thêm thông tin user
    await fetchMe()
  }

  /**
   * Đăng ký
   * @param {{ username, email, password, targetLevel }} payload
   * @returns {Promise<void>}  throws nếu lỗi
   */
  async function register(payload) {
    const res = await api.post('/auth/register', payload)
    if (res.status !== 'success') {
      throw new Error(res.message || 'Đăng ký thất bại')
    }
    // Sau khi đăng ký thành công → auto login
    await login(payload.email, payload.password)
  }

  /**
   * Lấy thông tin user hiện tại từ /users/me
   */
  async function fetchMe() {
    try {
      const res = await api.get('/users/me')
      if (res.status === 'success') {
        user.value = res.data
        localStorage.setItem('user', JSON.stringify(res.data))
      }
    } catch {
      // Bỏ qua nếu lỗi network
    }
  }

  /**
   * Làm mới access token thủ công (dùng khi cần)
   */
  async function refresh() {
    const token = refreshToken.value
    if (!token) throw new Error('No refresh token')
    const res = await api.post('/auth/refresh', { refreshToken: token })
    if (res.status !== 'success') throw new Error(res.message || 'Refresh thất bại')
    _persist({ ...res.data, refreshToken: token })
  }

  /**
   * Đăng xuất
   */
  async function logout() {
    try {
      if (refreshToken.value) {
        await api.post('/auth/logout', { refreshToken: refreshToken.value })
      }
    } catch {
      // Kể cả lỗi server vẫn clear local state
    } finally {
      _clear()
    }
  }

  return {
    user,
    accessToken,
    refreshToken,
    isAuthenticated,
    login,
    register,
    logout,
    refresh,
    fetchMe,
  }
})
