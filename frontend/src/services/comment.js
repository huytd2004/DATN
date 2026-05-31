import api from './api'

/**
 * Comment service — maps to /api/v1/comments
 *
 * Endpoints:
 *   GET    /api/v1/comments?entryId={id}   → top-level comments
 *   GET    /api/v1/comments/{id}/replies   → replies of a comment
 *   POST   /api/v1/comments                → create comment / reply
 *   DELETE /api/v1/comments/{id}           → delete (author / ADMIN only)
 */
export const commentService = {
  /** Lấy top-level comments của một entry */
  getByEntry(entryId) {
    return api.get('/api/v1/comments', { params: { entryId } })
  },

  /** Lấy replies của một comment */
  getReplies(commentId) {
    return api.get(`/api/v1/comments/${commentId}/replies`)
  },

  /**
   * Tạo comment mới hoặc reply
   * @param {Object} payload - { entryId: UUID, content: string, parentId?: UUID }
   */
  create(payload) {
    return api.post('/api/v1/comments', payload)
  },

  /** Xóa comment (chỉ tác giả hoặc ADMIN) */
  delete(commentId) {
    return api.delete(`/api/v1/comments/${commentId}`)
  },
}
