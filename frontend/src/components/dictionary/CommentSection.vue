<script setup>
import { ref, watch } from 'vue'
import { commentService } from '@/services/comment'
import { useAuthStore } from '@/stores/auth'

const props = defineProps({
  /** UUID của dictionary entry */
  entryId: { type: String, default: null },
})

const auth = useAuthStore()

// ── State ─────────────────────────────────────────────────────────────────
const comments    = ref([])
const loading     = ref(false)
const submitting  = ref(false)
const commentText = ref('')
const error       = ref('')

// reply state
const replyingTo  = ref(null)   // comment object đang reply
const replyText   = ref('')
const replies     = ref({})     // { [commentId]: CommentResponse[] }
const loadingReplies = ref({})

// ── Load comments ─────────────────────────────────────────────────────────
async function loadComments() {
  if (!props.entryId) return
  loading.value = true
  error.value = ''
  try {
    const res = await commentService.getByEntry(props.entryId)
    comments.value = res.data ?? []
  } catch {
    error.value = 'Không thể tải bình luận.'
  } finally {
    loading.value = false
  }
}

// Reload khi entryId thay đổi
watch(() => props.entryId, (id) => {
  if (id) {
    comments.value = []
    replies.value = {}
    replyingTo.value = null
    loadComments()
  }
}, { immediate: true })

// ── Submit top-level comment ──────────────────────────────────────────────
async function submitComment() {
  if (!commentText.value.trim() || !props.entryId) return
  submitting.value = true
  try {
    const res = await commentService.create({
      entryId: props.entryId,
      content: commentText.value.trim(),
    })
    comments.value.unshift(res.data)
    commentText.value = ''
  } catch {
    error.value = 'Không thể gửi bình luận. Vui lòng thử lại.'
  } finally {
    submitting.value = false
  }
}

// ── Submit reply ──────────────────────────────────────────────────────────
async function submitReply() {
  if (!replyText.value.trim() || !replyingTo.value) return
  submitting.value = true
  try {
    const res = await commentService.create({
      entryId: props.entryId,
      parentId: replyingTo.value.id,
      content: replyText.value.trim(),
    })
    // Append vào replies list của parent
    if (!replies.value[replyingTo.value.id]) {
      replies.value[replyingTo.value.id] = []
    }
    replies.value[replyingTo.value.id].push(res.data)
    replyText.value = ''
    replyingTo.value = null
  } catch {
    error.value = 'Không thể gửi trả lời. Vui lòng thử lại.'
  } finally {
    submitting.value = false
  }
}

// ── Load replies ──────────────────────────────────────────────────────────
async function toggleReplies(comment) {
  const id = comment.id
  if (replies.value[id] !== undefined) {
    // Toggle: nếu đã load rồi thì ẩn đi
    delete replies.value[id]
    return
  }
  loadingReplies.value[id] = true
  try {
    const res = await commentService.getReplies(id)
    replies.value[id] = res.data ?? []
  } catch {
    /* ignore */
  } finally {
    loadingReplies.value[id] = false
  }
}

// ── Delete ────────────────────────────────────────────────────────────────
async function deleteComment(comment, parentId = null) {
  if (!confirm('Xóa bình luận này?')) return
  try {
    await commentService.delete(comment.id)
    if (parentId) {
      replies.value[parentId] = (replies.value[parentId] ?? []).filter(r => r.id !== comment.id)
    } else {
      comments.value = comments.value.filter(c => c.id !== comment.id)
    }
  } catch {
    error.value = 'Không thể xóa bình luận.'
  }
}

// ── Format time ───────────────────────────────────────────────────────────
function timeAgo(iso) {
  const diff = (Date.now() - new Date(iso).getTime()) / 1000
  if (diff < 60) return 'vừa xong'
  if (diff < 3600) return `${Math.floor(diff / 60)} phút trước`
  if (diff < 86400) return `${Math.floor(diff / 3600)} giờ trước`
  return `${Math.floor(diff / 86400)} ngày trước`
}
</script>

<template>
  <section class="mt-8 pt-16 border-t border-outline-variant/20">
    <h3 class="text-xs font-['Inter'] uppercase tracking-widest text-outline mb-10 text-center font-bold">
      Bình luận cộng đồng
    </h3>

    <!-- Error banner -->
    <div v-if="error" class="max-w-3xl mx-auto mb-6 p-3 rounded-xl bg-error-container/60 text-on-error-container text-sm flex gap-2 items-center">
      <span class="material-symbols-outlined text-sm">error</span>
      {{ error }}
      <button class="ml-auto text-xs underline" @click="error = ''">Đóng</button>
    </div>

    <div class="max-w-3xl mx-auto space-y-6">

      <!-- ── Comment Input ─────────────────────────────────────────── -->
      <div class="flex gap-4">
        <div class="w-10 h-10 rounded-full bg-surface-container-high shrink-0 flex items-center justify-center text-on-surface-variant border border-outline-variant/10">
          <span class="material-symbols-outlined text-base" style="font-variation-settings: 'FILL' 1;">person</span>
        </div>
        <div class="flex-1 relative">
          <textarea
            v-model="commentText"
            class="w-full bg-surface-container-lowest border border-outline-variant/20 rounded-2xl p-4 pb-14 text-sm font-['Inter'] focus:ring-2 focus:ring-primary/20 focus:border-primary resize-none transition-all"
            placeholder="Chia sẻ cách ghi nhớ, ví dụ thực tế hoặc thắc mắc của bạn..."
            rows="3"
            @keydown.ctrl.enter="submitComment"
          ></textarea>
          <div class="absolute bottom-3 right-3 flex items-center gap-2">
            <span class="text-xs text-on-surface-variant opacity-50">Ctrl+Enter</span>
            <button
              class="bg-primary text-white px-5 py-2 rounded-full text-xs font-medium hover:opacity-90 disabled:opacity-40 transition-all shadow-sm"
              :disabled="!commentText.trim() || submitting"
              @click="submitComment"
            >
              <span v-if="submitting" class="material-symbols-outlined animate-spin text-xs mr-1">progress_activity</span>
              Gửi
            </button>
          </div>
        </div>
      </div>

      <!-- ── Loading skeleton ──────────────────────────────────────── -->
      <div v-if="loading" class="space-y-4">
        <div v-for="i in 3" :key="i" class="flex gap-4 animate-pulse">
          <div class="w-10 h-10 rounded-full bg-surface-container-low shrink-0"></div>
          <div class="flex-1 space-y-2">
            <div class="h-3 w-24 bg-surface-container-low rounded-full"></div>
            <div class="h-4 w-full bg-surface-container-low rounded-full"></div>
            <div class="h-4 w-3/4 bg-surface-container-low rounded-full"></div>
          </div>
        </div>
      </div>

      <!-- ── Empty state ───────────────────────────────────────────── -->
      <div v-else-if="!loading && comments.length === 0" class="text-center py-10 text-on-surface-variant">
        <span class="material-symbols-outlined text-4xl opacity-30">chat_bubble_outline</span>
        <p class="text-sm mt-2">Chưa có bình luận nào. Hãy là người đầu tiên!</p>
      </div>

      <!-- ── Comment list ──────────────────────────────────────────── -->
      <div v-else class="space-y-6">
        <div v-for="comment in comments" :key="comment.id">

          <!-- Top-level comment -->
          <div class="flex gap-4">
            <div class="w-10 h-10 rounded-full bg-primary-container shrink-0 flex items-center justify-center text-on-primary-container font-bold text-sm">
              {{ (comment.authorUsername ?? '?')[0].toUpperCase() }}
            </div>
            <div class="flex-1">
              <div class="flex items-baseline gap-2 mb-1">
                <span class="font-semibold text-on-surface text-sm">{{ comment.authorUsername }}</span>
                <span class="text-xs text-on-surface-variant opacity-60">{{ timeAgo(comment.createdAt) }}</span>
              </div>
              <p class="text-sm text-on-surface leading-relaxed">{{ comment.content }}</p>

              <!-- Actions -->
              <div class="flex items-center gap-4 mt-2">
                <button
                  class="text-xs text-on-surface-variant hover:text-primary transition-colors flex items-center gap-1"
                  @click="replyingTo = replyingTo?.id === comment.id ? null : comment; replyText = ''"
                >
                  <span class="material-symbols-outlined text-sm">reply</span>
                  Trả lời
                </button>
                <button
                  v-if="replies[comment.id] === undefined"
                  class="text-xs text-on-surface-variant hover:text-primary transition-colors flex items-center gap-1"
                  @click="toggleReplies(comment)"
                >
                  <span v-if="loadingReplies[comment.id]" class="material-symbols-outlined animate-spin text-sm">progress_activity</span>
                  <span v-else class="material-symbols-outlined text-sm">expand_more</span>
                  Xem trả lời
                </button>
                <button
                  v-else
                  class="text-xs text-on-surface-variant hover:text-primary transition-colors flex items-center gap-1"
                  @click="toggleReplies(comment)"
                >
                  <span class="material-symbols-outlined text-sm">expand_less</span>
                  Ẩn trả lời
                </button>
                <button
                  v-if="auth.user?.id === comment.authorId || auth.user?.role === 'ADMIN'"
                  class="text-xs text-error hover:opacity-80 transition-colors flex items-center gap-1 ml-auto"
                  @click="deleteComment(comment)"
                >
                  <span class="material-symbols-outlined text-sm">delete</span>
                </button>
              </div>

              <!-- Reply input -->
              <div v-if="replyingTo?.id === comment.id" class="mt-4 flex gap-3">
                <div class="w-8 h-8 rounded-full bg-surface-container-high shrink-0 flex items-center justify-center text-on-surface-variant text-xs">
                  <span class="material-symbols-outlined text-sm" style="font-variation-settings: 'FILL' 1;">person</span>
                </div>
                <div class="flex-1 relative">
                  <textarea
                    v-model="replyText"
                    class="w-full bg-surface-container-lowest border border-outline-variant/20 rounded-xl p-3 pb-11 text-sm font-['Inter'] focus:ring-2 focus:ring-primary/20 resize-none"
                    :placeholder="`Trả lời ${comment.authorUsername}...`"
                    rows="2"
                    @keydown.ctrl.enter="submitReply"
                  ></textarea>
                  <div class="absolute bottom-2 right-2 flex gap-2">
                    <button class="text-xs text-on-surface-variant px-3 py-1 rounded-full hover:bg-surface-container-low" @click="replyingTo = null">Hủy</button>
                    <button
                      class="text-xs bg-primary text-white px-4 py-1 rounded-full disabled:opacity-40"
                      :disabled="!replyText.trim() || submitting"
                      @click="submitReply"
                    >Gửi</button>
                  </div>
                </div>
              </div>

              <!-- Replies list -->
              <div v-if="replies[comment.id]" class="mt-4 space-y-4 pl-4 border-l-2 border-outline-variant/15">
                <div v-for="reply in replies[comment.id]" :key="reply.id" class="flex gap-3">
                  <div class="w-8 h-8 rounded-full bg-secondary-container shrink-0 flex items-center justify-center text-on-secondary-container font-bold text-xs">
                    {{ (reply.authorUsername ?? '?')[0].toUpperCase() }}
                  </div>
                  <div class="flex-1">
                    <div class="flex items-baseline gap-2 mb-0.5">
                      <span class="font-semibold text-on-surface text-xs">{{ reply.authorUsername }}</span>
                      <span class="text-xs text-on-surface-variant opacity-60">{{ timeAgo(reply.createdAt) }}</span>
                    </div>
                    <p class="text-sm text-on-surface leading-relaxed">{{ reply.content }}</p>
                    <button
                      v-if="auth.user?.id === reply.authorId || auth.user?.role === 'ADMIN'"
                      class="mt-1 text-xs text-error hover:opacity-80 flex items-center gap-0.5"
                      @click="deleteComment(reply, comment.id)"
                    >
                      <span class="material-symbols-outlined text-xs">delete</span>
                    </button>
                  </div>
                </div>
              </div>

            </div>
          </div>

        </div>
      </div>

    </div>
  </section>
</template>

<style scoped>
@keyframes spin { to { transform: rotate(360deg); } }
.animate-spin { animation: spin 0.8s linear infinite; }
@keyframes pulse { 0%,100% { opacity:1; } 50% { opacity:.5; } }
.animate-pulse { animation: pulse 1.5s ease-in-out infinite; }
</style>
