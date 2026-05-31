import api from '@/services/api'

/**
 * Dictionary API service — tập trung các lời gọi API cho từ điển.
 */
export const dictionaryService = {
  /**
   * Tìm kiếm từ điển.
   * @param {Object} params - { q, type, page, size }
   * @returns {Promise}  unwrapped ApiResponse (response.data = [...])
   */
  search({ q = '', type = null, page = 0, size = 20 } = {}) {
    const params = { page, size }
    if (q && q.trim()) params.q = q.trim()
    if (type) params.type = type
    return api.get('/api/v1/dictionary', { params })
  },

  /**
   * Lấy chi tiết 1 entry (có examples + relations).
   * @param {string} id - UUID
   */
  getById(id) {
    return api.get(`/api/v1/dictionary/${id}`)
  },
}
