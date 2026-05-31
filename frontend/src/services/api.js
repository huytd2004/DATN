import axios from 'axios'

const api = axios.create({
  baseURL: '', // Proxied via Vite: /api → :8080, /auth → :8080
})

// ─── Request interceptor: gắn Bearer token ────────────────────────────────
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// ─── Response interceptor: unwrap envelope + auto-refresh 401 ─────────────
let isRefreshing = false
let failedQueue = []

function processQueue(error, token = null) {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error)
    } else {
      prom.resolve(token)
    }
  })
  failedQueue = []
}

api.interceptors.response.use(
  // Unwrap chuẩn response envelope { status, code, message, data, timestamp }
  (response) => response.data,

  async (error) => {
    const originalRequest = error.config

    // Nếu 401 và chưa retry lần nào
    if (error.response?.status === 401 && !originalRequest._retry) {
      const refreshToken = localStorage.getItem('refreshToken')

      // Không có refresh token → redirect login
      if (!refreshToken) {
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('user')
        window.location.href = '/login'
        return Promise.reject(error)
      }

      if (isRefreshing) {
        // Đang refresh → xếp vào queue, đợi token mới
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        })
          .then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`
            return api(originalRequest)
          })
          .catch((err) => Promise.reject(err))
      }

      originalRequest._retry = true
      isRefreshing = true

      try {
        // Gọi thẳng axios để tránh vòng lặp interceptor
        const res = await axios.post('/auth/refresh', { refreshToken })
        const newAccessToken = res.data?.data?.accessToken

        if (!newAccessToken) throw new Error('No access token in refresh response')

        localStorage.setItem('accessToken', newAccessToken)
        api.defaults.headers.common.Authorization = `Bearer ${newAccessToken}`

        // Resolve queue TRƯỚC khi return để các request chờ được retry ngay
        isRefreshing = false
        processQueue(null, newAccessToken)

        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`
        return api(originalRequest)
      } catch (refreshError) {
        isRefreshing = false
        processQueue(refreshError, null)
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('user')
        window.location.href = '/login'
        return Promise.reject(refreshError)
      }
    }

    return Promise.reject(error)
  }
)

export default api
