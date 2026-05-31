<template>
  <div class="w-full max-w-screen-2xl mx-auto pt-6 px-4 md:px-8 pb-24">

    <!-- Welcome & Global Stats Section -->
    <section class="mb-12 flex flex-col md:flex-row justify-between items-start md:items-end gap-6">
      <div>
        <h2 class="text-4xl font-extrabold text-on-primary-fixed font-headline tracking-tight mb-2">Quản lý Bộ thẻ</h2>
        <p class="text-on-surface-variant max-w-lg leading-relaxed">Tiếp tục hành trình chinh phục tiếng Nhật của bạn với những bộ thẻ được tối ưu hóa theo phương pháp Spaced Repetition.</p>
      </div>
      <div class="flex gap-4">
        <div class="bg-surface-container-lowest p-6 rounded-xl text-center min-w-[120px] shadow-sm">
          <span class="block text-2xl font-bold text-primary font-headline">
            {{ loading ? '—' : totalCards.toLocaleString('vi-VN') }}
          </span>
          <span class="text-[10px] uppercase tracking-widest text-on-surface-variant font-semibold">Tổng số thẻ</span>
        </div>
        <div class="bg-primary-container p-6 rounded-xl text-center min-w-[120px] shadow-sm">
          <span class="block text-2xl font-bold text-on-primary-container font-headline">
            {{ loading ? '—' : dueToday }}
          </span>
          <span class="text-[10px] uppercase tracking-widest text-on-primary-container font-semibold">Hôm nay</span>
        </div>
      </div>
    </section>

    <!-- Error Banner -->
    <div v-if="error" class="mb-8 p-4 bg-error-container text-on-error-container rounded-2xl flex items-center gap-3">
      <span class="material-symbols-outlined">error</span>
      <span>{{ error }}</span>
      <button @click="loadDecks" class="ml-auto text-sm font-bold underline">Thử lại</button>
    </div>

    <!-- Loading Skeleton -->
    <div v-if="loading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
      <div v-for="i in 3" :key="i" class="bg-surface-container-lowest p-8 rounded-[2rem] animate-pulse h-64">
        <div class="h-12 w-12 rounded-2xl bg-surface-container-high mb-6"></div>
        <div class="h-5 w-2/3 rounded-full bg-surface-container-high mb-3"></div>
        <div class="h-3 w-1/2 rounded-full bg-surface-container-high"></div>
      </div>
    </div>

    <!-- Bento Grid -->
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">

      <!-- Deck Cards -->
      <RouterLink
        v-for="(deck, i) in decks"
        :key="deck.id"
        :to="`/flashcards/${deck.id}`"
        class="group relative bg-surface-container-lowest p-8 rounded-[2rem] hover:shadow-2xl hover:shadow-slate-200/50 transition-all duration-500 flex flex-col h-full border border-transparent hover:border-outline-variant/10 cursor-pointer block"
      >
        <div class="flex justify-between items-start mb-6">
          <!-- Icon -->
          <div :class="`p-3 ${DECK_ICON_BG[i % DECK_ICON_BG.length]} rounded-2xl`">
            <span :class="`material-symbols-outlined ${DECK_ICON_COLOR[i % DECK_ICON_COLOR.length]} text-3xl`">
              {{ DECK_ICONS[i % DECK_ICONS.length] }}
            </span>
          </div>
          <!-- Edit/Delete buttons (hover) -->
          <div class="flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity" @click.prevent>
            <button
              @click.stop="openEditModal(deck)"
              class="p-2 hover:bg-surface-container-high rounded-full text-on-surface-variant transition-colors"
              title="Sửa bộ thẻ"
            >
              <span class="material-symbols-outlined text-lg">edit</span>
            </button>
            <button
              @click.stop="confirmDelete(deck)"
              class="p-2 hover:bg-error-container/20 rounded-full text-error transition-colors"
              title="Xóa bộ thẻ"
            >
              <span class="material-symbols-outlined text-lg">delete</span>
            </button>
          </div>
        </div>

        <!-- Name & Description -->
        <h3 class="text-xl font-bold text-on-surface font-headline mb-2 leading-tight">{{ deck.name }}</h3>
        <p class="text-sm text-on-surface-variant mb-8 line-clamp-2">{{ deck.description || 'Chưa có mô tả.' }}</p>

        <!-- Stats -->
        <div class="mt-auto space-y-4">
          <!-- Mastered count (replaces overall progress) -->
          <div class="flex justify-between items-center text-xs font-medium">
            <span class="text-on-surface-variant">Đã thuộc</span>
            <span class="text-primary">{{ deck.cardStats?.masteredCount ?? 0 }}</span>
          </div>
          <div class="h-1.5 w-full bg-surface-container-high rounded-full overflow-hidden">
            <div
              class="h-full bg-primary rounded-full transition-all duration-700"
              :style="{ width: (deck.cardStats?.masteredPercent ?? 0) + '%' }"
            ></div>
          </div>
          <!-- Status breakdown -->
          <div class="grid grid-cols-3 gap-2 pt-4">
            <div class="text-center">
              <span class="block text-sm font-bold text-tertiary">{{ deck.cardStats?.newCount ?? 0 }}</span>
              <span class="text-[9px] uppercase font-bold text-on-surface-variant">Mới</span>
            </div>
            <div class="text-center">
              <span class="block text-sm font-bold text-primary">{{ deck.cardStats?.learningCount ?? 0 }}</span>
              <span class="text-[9px] uppercase font-bold text-on-surface-variant">Đang học</span>
            </div>
            <div class="text-center">
              <span class="block text-sm font-bold text-secondary">{{ deck.cardStats?.reviewCount ?? 0 }}</span>
              <span class="text-[9px] uppercase font-bold text-on-surface-variant">Cần ôn</span>
            </div>
          </div>
        </div>
      </RouterLink>

      <!-- Add New Deck Card -->
      <button
        @click="openCreateModal"
        class="group relative bg-surface-container-low border-2 border-dashed border-outline-variant/30 p-8 rounded-[2rem] hover:bg-surface-container-high transition-all duration-500 flex flex-col items-center justify-center text-center cursor-pointer w-full"
      >
        <div class="w-16 h-16 rounded-full bg-white flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
          <span class="material-symbols-outlined text-primary text-3xl">add</span>
        </div>
        <h3 class="text-lg font-bold text-on-surface font-headline">Tạo bộ thẻ mới</h3>
        <p class="text-sm text-on-surface-variant mt-2 px-6">Bắt đầu hành trình học tập mới với chủ đề riêng của bạn.</p>
      </button>

      <!-- Learning Streak Card (Hardcoded) -->
      <div class="md:col-span-2 bg-on-primary-fixed text-on-primary rounded-[2rem] p-8 flex items-center gap-12 overflow-hidden relative">
        <div class="relative z-10">
          <h3 class="text-3xl font-bold font-headline mb-4">Chuỗi học tập: {{ STREAK_DAYS }} Ngày 🔥</h3>
          <p class="text-primary-fixed-dim/80 mb-6 max-w-md leading-relaxed">
            Bạn đang làm rất tốt! Hôm nay có <strong>{{ dueToday }}</strong> thẻ cần ôn tập.
          </p>
          <RouterLink
            to="/study"
            class="inline-block bg-white text-on-primary-fixed px-8 py-3 rounded-full font-bold text-sm shadow-lg hover:scale-105 transition-transform"
          >
            Tiếp tục ôn tập
          </RouterLink>
        </div>
        <div class="hidden lg:block absolute -right-20 -top-20 opacity-20">
          <span class="material-symbols-outlined text-[20rem]" style="font-variation-settings: 'FILL' 1;">psychology</span>
        </div>
      </div>

    </div>

    <!-- Featured Section -->
    <section class="mt-16 grid grid-cols-1 lg:grid-cols-2 gap-8">
      <div class="relative h-64 rounded-[2.5rem] overflow-hidden">
        <img
          class="w-full h-full object-cover"
          src="https://lh3.googleusercontent.com/aida-public/AB6AXuDRDNwagZlzpGITC3MekZcnNbASjt4eQUdDzjYPMDC2RsWjMdWhOuJ2-4ZXEaI2dxwabzL8SUk7zqTpCsrQJbcYfHWd59w-O1ZjOzCw5GpON-hqAUvyzkyQEDqXfcw8gzXav9VIq3gfpUbQtFxBZTj3W5P02c-psAzbyO6w5wqw1jdDQbj39b-Ugu9vqIF79NK3rOpOuKtR_7o647Iecfzu_eGJDe6bNZUFEgBgRQHjbBq6-Jcu9eQHxOSuDyrrUCR2mDVMg6H50RA"
          alt="Zen garden"
        />
        <div class="absolute inset-0 bg-gradient-to-r from-black/60 to-transparent flex flex-col justify-center p-10 text-white">
          <span class="text-[10px] uppercase tracking-widest font-bold mb-2">Lời khuyên hôm nay</span>
          <h4 class="text-2xl font-bold font-headline max-w-xs">Học ít nhưng đều đặn là chìa khóa của sự bền bỉ.</h4>
        </div>
      </div>
      <div class="bg-surface-container-low rounded-[2.5rem] p-10 flex flex-col justify-center">
        <div class="flex items-center gap-4 mb-4">
          <div class="h-1 w-12 bg-primary"></div>
          <span class="text-xs font-bold uppercase tracking-widest text-on-surface-variant">Gợi ý từ cộng đồng</span>
        </div>
        <h4 class="text-xl font-bold text-on-surface font-headline mb-4 leading-snug">Bộ thẻ "2000 Hán tự Heisig" vừa được cập nhật với hình ảnh minh họa mới.</h4>
        <a class="text-primary font-bold text-sm flex items-center gap-2 group" href="#">
          Khám phá ngay
          <span class="material-symbols-outlined text-sm group-hover:translate-x-1 transition-transform">arrow_forward</span>
        </a>
      </div>
    </section>

    <!-- FAB -->
    <div class="fixed bottom-8 right-8 z-50">
      <button
        @click="openCreateModal"
        class="w-14 h-14 bg-primary text-on-primary rounded-full shadow-2xl flex items-center justify-center hover:scale-110 active:scale-95 transition-all"
        title="Tạo bộ thẻ mới"
      >
        <span class="material-symbols-outlined text-3xl">add</span>
      </button>
    </div>

    <!-- ── Create / Edit Deck Modal ─────────────────────────────────────────── -->
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
              {{ editingDeck ? 'Sửa bộ thẻ' : 'Tạo bộ thẻ mới' }}
            </h3>

            <form @submit.prevent="submitModal" class="space-y-5">
              <!-- Name -->
              <div>
                <label class="block text-sm font-semibold text-on-surface-variant mb-2">Tên bộ thẻ *</label>
                <input
                  v-model="form.name"
                  type="text"
                  required
                  maxlength="80"
                  placeholder="VD: JLPT N3 Kanji Essentials"
                  class="w-full bg-surface-container-low border border-outline-variant rounded-xl px-4 py-3 text-on-surface placeholder:text-on-surface-variant/50 focus:outline-none focus:border-primary transition"
                />
              </div>

              <!-- Description -->
              <div>
                <label class="block text-sm font-semibold text-on-surface-variant mb-2">Mô tả</label>
                <textarea
                  v-model="form.description"
                  rows="3"
                  maxlength="300"
                  placeholder="Mô tả ngắn về bộ thẻ (không bắt buộc)"
                  class="w-full bg-surface-container-low border border-outline-variant rounded-xl px-4 py-3 text-on-surface placeholder:text-on-surface-variant/50 focus:outline-none focus:border-primary transition resize-none"
                ></textarea>
              </div>

              <!-- Public toggle -->
              <label class="flex items-center gap-3 cursor-pointer select-none">
                <div
                  @click="form.isPublic = !form.isPublic"
                  :class="form.isPublic ? 'bg-primary' : 'bg-surface-container-high'"
                  class="relative w-11 h-6 rounded-full transition-colors"
                >
                  <span
                    :class="form.isPublic ? 'translate-x-5' : 'translate-x-1'"
                    class="absolute top-1 w-4 h-4 bg-white rounded-full shadow transition-transform"
                  ></span>
                </div>
                <span class="text-sm font-medium text-on-surface">Chia sẻ công khai</span>
              </label>

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
                  {{ submitting ? 'Đang lưu...' : (editingDeck ? 'Lưu thay đổi' : 'Tạo bộ thẻ') }}
                </button>
              </div>
            </form>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- ── Delete Confirm Modal ──────────────────────────────────────────────── -->
    <Teleport to="body">
      <Transition name="modal">
        <div
          v-if="deletingDeck"
          class="fixed inset-0 z-[100] flex items-center justify-center p-4"
          @click.self="deletingDeck = null"
        >
          <div class="absolute inset-0 bg-black/40 backdrop-blur-sm"></div>
          <div class="relative bg-surface rounded-[2rem] p-8 w-full max-w-sm shadow-2xl text-center">
            <div class="w-16 h-16 bg-error-container rounded-full flex items-center justify-center mx-auto mb-4">
              <span class="material-symbols-outlined text-error text-3xl">delete_forever</span>
            </div>
            <h3 class="text-lg font-bold text-on-surface mb-2">Xóa bộ thẻ?</h3>
            <p class="text-sm text-on-surface-variant mb-6">
              Bộ thẻ "<strong>{{ deletingDeck.name }}</strong>" và toàn bộ flashcard bên trong sẽ bị xóa vĩnh viễn.
            </p>
            <div class="flex gap-3">
              <button @click="deletingDeck = null" class="flex-1 py-3 rounded-xl border border-outline-variant text-on-surface font-semibold hover:bg-surface-container-low transition">
                Hủy
              </button>
              <button
                @click="executeDelete"
                :disabled="submitting"
                class="flex-1 py-3 rounded-xl bg-error text-on-error font-semibold hover:brightness-110 transition disabled:opacity-50"
              >
                {{ submitting ? 'Đang xóa...' : 'Xóa' }}
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '@/services/api'

// ── Constants ──────────────────────────────────────────────────────────────────

const STREAK_DAYS = 12  // TODO: fetch from user_profiles API when available

const DECK_ICONS    = ['auto_stories', 'translate', 'forum', 'school', 'local_library', 'bookmark']
const DECK_ICON_BG  = ['bg-primary-container/30', 'bg-secondary-container/30', 'bg-tertiary-container/30']
const DECK_ICON_COLOR = ['text-primary', 'text-secondary', 'text-tertiary']

// ── State ──────────────────────────────────────────────────────────────────────

const decks      = ref([])
const loading    = ref(true)
const error      = ref(null)
const submitting = ref(false)

// Modal
const showModal    = ref(false)
const editingDeck  = ref(null)   // null = create mode, object = edit mode
const deletingDeck = ref(null)
const form         = ref({ name: '', description: '', isPublic: false })

// ── Computed ───────────────────────────────────────────────────────────────────

const totalCards = computed(() =>
  decks.value.reduce((sum, d) => sum + (d.cardStats?.total ?? 0), 0)
)

const dueToday = computed(() =>
  decks.value.reduce((sum, d) => sum + (d.cardStats?.dueToday ?? 0), 0)
)

// ── Data fetching ──────────────────────────────────────────────────────────────

async function loadDecks() {
  loading.value = true
  error.value   = null
  try {
    const res  = await api.get('/api/v1/decks')
    decks.value = res.data ?? []
  } catch (e) {
    error.value = 'Không thể tải danh sách bộ thẻ. Vui lòng thử lại.'
    console.error('[FlashcardsView] loadDecks:', e)
  } finally {
    loading.value = false
  }
}

onMounted(loadDecks)

// ── Modal helpers ──────────────────────────────────────────────────────────────

function openCreateModal() {
  editingDeck.value = null
  form.value = { name: '', description: '', isPublic: false }
  showModal.value = true
}

function openEditModal(deck) {
  editingDeck.value = deck
  form.value = { name: deck.name, description: deck.description ?? '', isPublic: deck.public }
  showModal.value = true
}

function closeModal() {
  showModal.value = false
  editingDeck.value = null
}

// ── CRUD ───────────────────────────────────────────────────────────────────────

async function submitModal() {
  submitting.value = true
  try {
    const payload = {
      name:        form.value.name.trim(),
      description: form.value.description.trim(),
      public:      form.value.isPublic,
    }
    if (editingDeck.value) {
      await api.put(`/api/v1/decks/${editingDeck.value.id}`, payload)
    } else {
      await api.post('/api/v1/decks', payload)
    }
    closeModal()
    await loadDecks()
  } catch (e) {
    alert('Có lỗi xảy ra. Vui lòng thử lại.')
    console.error('[FlashcardsView] submitModal:', e)
  } finally {
    submitting.value = false
  }
}

function confirmDelete(deck) {
  deletingDeck.value = deck
}

async function executeDelete() {
  if (!deletingDeck.value) return
  submitting.value = true
  try {
    await api.delete(`/api/v1/decks/${deletingDeck.value.id}`)
    deletingDeck.value = null
    await loadDecks()
  } catch (e) {
    alert('Không thể xóa bộ thẻ. Vui lòng thử lại.')
    console.error('[FlashcardsView] executeDelete:', e)
  } finally {
    submitting.value = false
  }
}
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

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
