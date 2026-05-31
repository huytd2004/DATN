<template>
  <div class="w-full max-w-screen-2xl mx-auto pt-6 px-4 md:px-8 pb-12">
    <!-- Breadcrumbs -->
    <nav class="flex items-center gap-2 text-on-surface-variant text-sm mb-6">
      <RouterLink to="/flashcards" class="hover:text-primary cursor-pointer transition-colors">Kho thẻ</RouterLink>
      <span class="material-symbols-outlined text-xs">chevron_right</span>
      <span class="text-on-surface font-medium">{{ deck?.name ?? 'Đang tải...' }}</span>
    </nav>

    <!-- Deck Header Bento Grid -->
    <div class="grid grid-cols-1 lg:grid-cols-12 gap-6 mb-12">
      <!-- Main Info Card -->
      <div class="lg:col-span-8 bg-surface-container-lowest rounded-3xl p-8 relative overflow-hidden shadow-sm">
        <div class="relative z-10">
          <div class="flex flex-col md:flex-row justify-between items-start md:items-center gap-6 mb-8">
            <div>
              <h2 class="text-3xl md:text-4xl font-extrabold text-on-primary-fixed mb-2 leading-tight">
                {{ deck?.name ?? 'Đang tải...' }}
              </h2>
              <p class="text-on-surface-variant max-w-md leading-relaxed">
                {{ deck?.description ?? '' }}
              </p>
            </div>
            <div class="w-full md:w-auto flex flex-col gap-3 shrink-0">
              <label class="block text-xs font-semibold uppercase tracking-widest text-on-surface-variant">
                Học tối đa từ mới mỗi ngày
              </label>
              <div class="flex items-center gap-3">
                <input
                  v-model.number="newWordsPerDay"
                  type="number"
                  min="1"
                  max="100"
                  class="w-28 bg-surface-container-high border border-outline-variant rounded-xl px-4 py-3 text-on-surface font-semibold focus:outline-none focus:border-primary transition"
                />
                <RouterLink
                  :to="studyLink"
                  class="bg-primary hover:bg-primary-dim text-on-primary px-8 py-4 rounded-full font-bold shadow-lg shadow-primary/20 transition-all hover:scale-105 active:scale-95 flex items-center gap-3 shrink-0"
                >
                  <span class="material-symbols-outlined" style="font-variation-settings: 'FILL' 1;">play_arrow</span>
                  Bắt đầu học
                </RouterLink>
              </div>
            </div>
          </div>
          
          <div class="flex flex-wrap gap-8 md:gap-12">
            <div class="flex flex-col">
              <span class="text-on-surface-variant text-xs font-semibold mb-1 uppercase tracking-widest">Tổng số thẻ</span>
              <span class="text-3xl font-headline font-bold text-on-surface">{{ stats?.total ?? 0 }}</span>
            </div>
          </div>
        </div>
        <!-- Decorative Background Image -->
        <div class="absolute right-0 bottom-0 w-64 h-64 opacity-5 pointer-events-none hidden sm:block">
          <img alt="Zen Circle" class="w-full h-full object-contain" src="https://lh3.googleusercontent.com/aida-public/AB6AXuDqjHxrRrh3kjDZMiz_KMKap9zYqSV4BMhE6GTeGNwizdXLjWIP-1V9RzQHebRSBLB8VSN4eY6pM6cx-A_SYeGKSAAiBD_0bNoF84lEjyjs2t35VYkNzjUz3kAQTK5dg63m_TFaS7NJIw9h46jsWaQLzE6MkAvkGcZFA7uz-5VkVLPpUW7eeB0o8GdxUi1xOaZbeS93hhpxs7PmCPNfPweuivOSWO-SUoSVRsQ-HxwHYThX-pJBnP_2eYGWUemoY5GC8GwtmbmNIMQ"/>
        </div>
      </div>

      <!-- Progress Stats Card -->
      <div class="lg:col-span-4 bg-surface-container-low rounded-3xl p-8 flex flex-col justify-between border border-outline-variant/10 shadow-sm">
        <h3 class="text-on-primary-fixed font-bold text-lg mb-6">Tiến độ bộ thẻ</h3>
        <div class="space-y-4">
          <div class="flex justify-between items-end">
            <span class="text-sm font-medium text-on-surface-variant">Thành thạo</span>
            <span class="text-2xl font-bold text-primary">{{ stats?.masteredPercent ?? 0 }}%</span>
          </div>
          <div class="w-full h-3 bg-surface-container-highest rounded-full overflow-hidden">
            <div class="h-full bg-primary rounded-full transition-all duration-500" :style="{ width: `${stats?.masteredPercent ?? 0}%` }"></div>
          </div>
          <div class="grid grid-cols-3 gap-2 pt-4">
            <div class="text-center">
              <div class="text-xs text-on-surface-variant mb-1">Mới</div>
              <div class="text-lg font-bold text-on-surface">{{ stats?.newCount ?? 0 }}</div>
            </div>
            <div class="text-center">
              <div class="text-xs text-on-surface-variant mb-1">Đang học</div>
              <div class="text-lg font-bold text-primary">{{ stats?.learningCount ?? 0 }}</div>
            </div>
            <div class="text-center">
              <div class="text-xs text-on-surface-variant mb-1">Cần ôn</div>
              <div class="text-lg font-bold text-secondary">{{ stats?.reviewCount ?? 0 }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- List Controls -->
    <div class="flex flex-col sm:flex-row items-start sm:items-center justify-between mb-8 gap-4">
      <h3 class="text-2xl font-bold text-on-primary-fixed">Danh sách Flashcard</h3>
      <div class="flex gap-3">
        <button class="flex items-center gap-2 px-4 py-2 rounded-xl bg-surface-container-high text-on-surface-variant text-sm font-medium hover:bg-surface-container-highest transition-colors">
          <span class="material-symbols-outlined text-lg">filter_list</span>
          Lọc theo trạng thái
        </button>
        <button class="flex items-center gap-2 px-4 py-2 rounded-xl bg-surface-container-high text-on-surface-variant text-sm font-medium hover:bg-surface-container-highest transition-colors">
          <span class="material-symbols-outlined text-lg">sort</span>
          Sắp xếp
        </button>
      </div>
    </div>

    <!-- Flashcard Grid -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <!-- Loading State -->
      <div v-if="loading" class="col-span-full text-center py-12">
        <div class="text-on-surface-variant">Đang tải flashcards...</div>
      </div>

      <!-- Empty State -->
      <div v-else-if="!flashcards || flashcards.length === 0" class="col-span-full text-center py-12">
        <div class="text-on-surface-variant">Chưa có flashcard nào trong bộ thẻ này</div>
      </div>

      <!-- Flashcard Items -->
      <div v-for="card in flashcards" :key="card.id" class="group bg-surface-container-lowest rounded-2xl p-6 transition-all duration-300 hover:shadow-xl hover:shadow-slate-200/40 shadow-sm border border-transparent hover:border-outline-variant/10">
        <div class="flex gap-6 items-start">
          <div class="w-24 h-24 bg-surface-container-low rounded-xl flex items-center justify-center text-2xl font-headline font-bold text-on-primary-fixed leading-none shrink-0 text-center line-clamp-2">
            {{ card.frontText }}
          </div>
          <div class="flex-1">
            <div class="flex flex-wrap justify-between items-start gap-2 mb-2">
              <span class="text-xs font-medium text-primary uppercase tracking-widest">{{ card.frontReading }}</span>
              <span :class="['px-2 py-0.5 rounded text-[10px] font-bold uppercase', statusBadgeClass(card.status)]">
                {{ statusLabel(card.status) }}
              </span>
            </div>
            <h4 class="text-lg font-bold text-on-surface mb-1">{{ card.backText }}</h4>
            <p v-if="card.backNotes" class="text-sm text-on-surface-variant leading-relaxed italic">{{ card.backNotes }}</p>
          </div>
        </div>
        <div class="mt-4 flex justify-end gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
          <button class="p-2 text-on-surface-variant hover:text-primary transition-colors"><span class="material-symbols-outlined text-xl">edit</span></button>
          <button class="p-2 text-on-surface-variant hover:text-primary transition-colors"><span class="material-symbols-outlined text-xl">volume_up</span></button>
        </div>
      </div>
    </div>

    <!-- Empty State / Load More (Contextual) -->
    <div v-if="flashcards && flashcards.length > 0" class="mt-12 text-center">
      <button v-if="flashcards.length < stats?.total" class="px-6 py-3 rounded-full border-2 border-primary/20 text-primary font-semibold hover:bg-primary-container/20 transition-all">
        Xem thêm flashcard ({{ (stats?.total ?? 0) - flashcards.length }})
      </button>
    </div>

    <!-- Floating Action Button -->
    <div class="fixed bottom-10 right-10 z-50">
      <button
        @click="openCreateModal"
        class="w-14 h-14 bg-primary text-on-primary rounded-full shadow-2xl shadow-primary/40 flex items-center justify-center transition-transform hover:scale-110 active:scale-90"
        title="Tạo flashcard mới"
      >
        <span class="material-symbols-outlined text-2xl">add</span>
      </button>
    </div>

    <!-- ── Create Flashcard Modal ──────────────────────────────────────── -->
    <Teleport to="body">
      <Transition name="modal">
        <div
          v-if="showModal"
          class="fixed inset-0 z-[100] flex items-center justify-center p-4"
          @click.self="closeModal"
        >
          <!-- Backdrop -->
          <div class="absolute inset-0 bg-black/40 backdrop-blur-sm"></div>

          <!-- Panel -->
          <div class="relative bg-surface rounded-[2rem] p-8 w-full max-w-md shadow-2xl">
            <h3 class="text-xl font-bold text-on-surface font-headline mb-6">
              Tạo flashcard mới
            </h3>

            <form @submit.prevent="submitCreate" class="space-y-5">
              <!-- Front Text (Chữ/Từ) -->
              <div>
                <label class="block text-sm font-semibold text-on-surface-variant mb-2">Chữ / Từ (Mặt trước) *</label>
                <input
                  v-model="form.frontText"
                  type="text"
                  required
                  placeholder="VD: 学ぶ"
                  class="w-full bg-surface-container-low border border-outline-variant rounded-xl px-4 py-3 text-on-surface placeholder:text-on-surface-variant/50 focus:outline-none focus:border-primary transition"
                />
              </div>

              <!-- Front Reading (Hiragana) -->
              <div>
                <label class="block text-sm font-semibold text-on-surface-variant mb-2">Cách đọc (Hiragana)</label>
                <input
                  v-model="form.frontReading"
                  type="text"
                  placeholder="VD: まなぶ"
                  class="w-full bg-surface-container-low border border-outline-variant rounded-xl px-4 py-3 text-on-surface placeholder:text-on-surface-variant/50 focus:outline-none focus:border-primary transition"
                />
              </div>

              <!-- Back Text (Nghĩa) -->
              <div>
                <label class="block text-sm font-semibold text-on-surface-variant mb-2">Nghĩa (Mặt sau) *</label>
                <input
                  v-model="form.backText"
                  type="text"
                  required
                  placeholder="VD: Học"
                  class="w-full bg-surface-container-low border border-outline-variant rounded-xl px-4 py-3 text-on-surface placeholder:text-on-surface-variant/50 focus:outline-none focus:border-primary transition"
                />
              </div>

              <!-- Back Notes -->
              <div>
                <label class="block text-sm font-semibold text-on-surface-variant mb-2">Ghi chú thêm</label>
                <textarea
                  v-model="form.backNotes"
                  rows="3"
                  placeholder="Thêm ví dụ câu hoặc ghi chú giúp nhớ"
                  class="w-full bg-surface-container-low border border-outline-variant rounded-xl px-4 py-3 text-on-surface placeholder:text-on-surface-variant/50 focus:outline-none focus:border-primary transition resize-none"
                ></textarea>
              </div>

              <!-- Buttons -->
              <div class="flex gap-3 pt-2">
                <button
                  type="button"
                  @click="closeModal"
                  class="flex-1 py-3 rounded-xl border border-outline-variant text-on-surface font-semibold hover:bg-surface-container-low transition"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  :disabled="submitting"
                  class="flex-1 py-3 rounded-xl bg-primary text-on-primary font-semibold hover:brightness-110 transition disabled:opacity-50 flex items-center justify-center gap-2"
                >
                  <svg v-if="submitting" class="animate-spin w-4 h-4" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z"/>
                  </svg>
                  {{ submitting ? 'Đang lưu...' : 'Tạo flashcard' }}
                </button>
              </div>
            </form>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/services/api'

const route = useRoute()
const deckId = computed(() => route.params.id)

const deck = ref(null)
const stats = ref(null)
const flashcards = ref([])
const loading = ref(true)
const newWordsPerDay = ref(20)

// Modal state
const showModal = ref(false)
const submitting = ref(false)
const form = ref({ frontText: '', frontReading: '', backText: '', backNotes: '' })

const studyLink = computed(() => ({
  path: `/flashcards/${deckId.value}/study`,
  query: { newWordsPerDay: String(newWordsPerDay.value || 20) }
}))

const statusBadgeClass = (status) => {
  switch (status) {
    case 'new':
      return 'bg-surface-container-high text-on-surface-variant'
    case 'learning':
      return 'bg-primary-container/30 text-on-primary-container'
    case 'review':
      return 'bg-primary-container text-on-primary-container'
    default:
      return 'bg-surface-container-high text-on-surface-variant'
  }
}

const statusLabel = (status) => {
  switch (status) {
    case 'new':
      return 'Mới'
    case 'learning':
      return 'Đang học'
    case 'review':
      return 'Cần ôn'
    default:
      return status
  }
}

// Modal helpers
function openCreateModal() {
  form.value = { frontText: '', frontReading: '', backText: '', backNotes: '' }
  showModal.value = true
}

function closeModal() {
  showModal.value = false
}

// Create flashcard
async function submitCreate() {
  if (!form.value.frontText.trim() || !form.value.backText.trim()) {
    alert('Vui lòng nhập chữ/từ và nghĩa')
    return
  }

  submitting.value = true
  try {
    await api.post('/api/v1/flashcards', {
      deckId: deckId.value,
      frontText: form.value.frontText.trim(),
      frontReading: form.value.frontReading.trim(),
      backText: form.value.backText.trim(),
      backNotes: form.value.backNotes.trim()
    })
    closeModal()
    // Reload flashcards
    const cardsResponse = await api.get('/api/v1/flashcards', {
      params: { deckId: deckId.value }
    })
    flashcards.value = cardsResponse.data || []
  } catch (error) {
    console.error('Error creating flashcard:', error)
    alert('Không thể tạo flashcard. Vui lòng thử lại.')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  try {
    loading.value = true
    
    // Fetch deck detail with stats
    const deckResponse = await api.get(`/api/v1/decks/${deckId.value}`)
    deck.value = deckResponse.data
    stats.value = deckResponse.data.cardStats
    
    // Fetch flashcards in this deck
    const cardsResponse = await api.get(`/api/v1/flashcards`, {
      params: { deckId: deckId.value }
    })
    flashcards.value = cardsResponse.data || []
  } catch (error) {
    console.error('Error loading deck:', error)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.2s ease;
}
.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}
.modal-enter-active .relative,
.modal-leave-active .relative {
  transition: transform 0.2s ease;
}
.modal-enter-from .relative,
.modal-leave-to .relative {
  transform: scale(0.95);
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
.animate-spin {
  animation: spin 0.8s linear infinite;
}
</style>
