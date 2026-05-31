<script setup>
import { ref, computed } from 'vue'
import { useDebounceFn } from '@vueuse/core'
import SearchHeader from '@/components/dictionary/SearchHeader.vue'
import CommentSection from '@/components/dictionary/CommentSection.vue'
import { dictionaryService } from '@/services/dictionary'
import api from '@/services/api'

// ── State ─────────────────────────────────────────────────────────────────
const searchQuery = ref('')
const selectedEntry = ref(null)
const loading = ref(false)
const detailLoading = ref(false)
const error = ref('')
const commentInput = ref('')

// ── Add to Flashcard Modal ────────────────────────────────────────────────
const showAddModal = ref(false)
const decks = ref([])
const selectedDeckId = ref(null)
const loadingDecks = ref(false)
const addingFlashcard = ref(false)
const addSuccess = ref(false)

// ── Create New Deck ───────────────────────────────────────────────────────
const showCreateDeckForm = ref(false)
const newDeckName = ref('')
const newDeckDescription = ref('')
const creatingDeck = ref(false)

// ── Search ────────────────────────────────────────────────────────────────
async function doSearch() {
  loading.value = true
  error.value = ''
  selectedEntry.value = null
  try {
    const res = await dictionaryService.search({
      q: searchQuery.value,
      type: 'word',
      page: 0,
      size: 1,
    })
    const items = res.data ?? []
    if (items.length > 0) {
      await selectEntry(items[0])
    } else {
      error.value = 'Không tìm thấy từ nào phù hợp.'
    }
  } catch {
    error.value = 'Không thể tải dữ liệu. Vui lòng thử lại.'
  } finally {
    loading.value = false
  }
}

const debouncedSearch = useDebounceFn(() => doSearch(), 350)

function handleSearch(q) {
  searchQuery.value = q
  debouncedSearch()
}

// ── Detail ────────────────────────────────────────────────────────────────
async function selectEntry(entry) {
  if (selectedEntry.value?.id === entry.id) return
  detailLoading.value = true
  selectedEntry.value = entry
  try {
    const res = await dictionaryService.getById(entry.id)
    selectedEntry.value = res.data
  } catch {
    // keep basic info
  } finally {
    detailLoading.value = false
  }
}

// ── Computed ──────────────────────────────────────────────────────────────
// Kanji thành phần — chữ kanji & bộ thủ → hiển thị trong side panel
const kanjiComponents = computed(() => {
  if (!selectedEntry.value?.relations) return []
  return selectedEntry.value.relations
    .filter(r => r.relationType === 'kanji' || r.relationType === 'radical')
    .slice(0, 5)
})

// Từ liên quan — compound + synonym → hiển thị trong section riêng
const relatedWords = computed(() => {
  if (!selectedEntry.value?.relations) return []
  return selectedEntry.value.relations
    .filter(r => r.relationType === 'compound' || r.relationType === 'synonym')
    .slice(0, 8)
})

// ── Helpers ───────────────────────────────────────────────────────────────
const jlptColors = {
  N5: 'bg-green-100 text-green-800',
  N4: 'bg-blue-100 text-blue-800',
  N3: 'bg-yellow-100 text-yellow-800',
  N2: 'bg-orange-100 text-orange-800',
  N1: 'bg-red-100 text-red-800',
}

// ── Add to Flashcard Functions ────────────────────────────────────────────
async function loadDecks() {
  if (decks.value.length > 0) {
    showAddModal.value = true
    return
  }

  loadingDecks.value = true
  try {
    const res = await api.get('/api/v1/decks')
    decks.value = res.data || []
    if (decks.value.length > 0) {
      selectedDeckId.value = decks.value[0].id
    }
  } catch (err) {
    console.error('Error loading decks:', err)
    error.value = 'Không thể tải danh sách bộ thẻ'
  } finally {
    loadingDecks.value = false
    showAddModal.value = true
  }
}

async function addToFlashcard() {
  if (!selectedDeckId.value || !selectedEntry.value) return

  addingFlashcard.value = true
  try {
    await api.post('/api/v1/flashcards', {
      deckId: selectedDeckId.value,
      frontText: selectedEntry.value.text,
      frontReading: selectedEntry.value.reading || '',
      backText: selectedEntry.value.meaningVn || '',
      backNotes: selectedEntry.value.explanationShort || '',
    })
    addSuccess.value = true
    setTimeout(() => {
      showAddModal.value = false
      addSuccess.value = false
    }, 2000)
  } catch (err) {
    console.error('Error adding flashcard:', err)
    error.value = 'Không thể thêm vào flashcard. Vui lòng thử lại.'
  } finally {
    addingFlashcard.value = false
  }
}

function closeModal() {
  showAddModal.value = false
  addSuccess.value = false
  showCreateDeckForm.value = false
  newDeckName.value = ''
  newDeckDescription.value = ''
}

async function createNewDeck() {
  if (!newDeckName.value.trim()) {
    error.value = 'Vui lòng nhập tên bộ thẻ'
    return
  }

  creatingDeck.value = true
  try {
    const res = await api.post('/api/v1/decks', {
      name: newDeckName.value.trim(),
      description: newDeckDescription.value.trim(),
      isPublic: false,
    })
    const newDeck = res.data
    decks.value.push(newDeck)
    selectedDeckId.value = newDeck.id
    showCreateDeckForm.value = false
    newDeckName.value = ''
    newDeckDescription.value = ''
  } catch (err) {
    console.error('Error creating deck:', err)
    error.value = 'Không thể tạo bộ thẻ. Vui lòng thử lại.'
  } finally {
    creatingDeck.value = false
  }
}

// Chỉ search khi user nhập — không auto-search khi mount

</script>

<template>
  <div class="w-full max-w-5xl mx-auto">

    <!-- ── Search Hero ─────────────────────────────────────────────── -->
    <SearchHeader
      placeholder="Tìm kiếm Kanji, Kana hoặc Romaji..."
      :suggestions="[
        { label: 'Komorebi', value: '木漏れ日' },
        { label: 'Ikigai', value: '生き甲斐' },
        { label: 'Yūgen', value: '幽玄' },
      ]"
      @search="handleSearch"
    />

    <!-- Error -->
    <div v-if="error" class="mb-6 p-4 rounded-xl bg-error-container/60 text-on-error-container text-sm flex items-center gap-2">
      <span class="material-symbols-outlined text-error">error</span>
      {{ error }}
    </div>

    <!-- Loading full-page skeleton -->
    <div v-if="loading && !selectedEntry" class="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start mt-8">
      <div class="lg:col-span-8 h-96 rounded-[2rem] bg-surface-container-low animate-pulse"></div>
      <div class="lg:col-span-4 space-y-6">
        <div class="h-64 rounded-[2rem] bg-surface-container-low animate-pulse"></div>
        <div class="aspect-square rounded-[2rem] bg-surface-container-low animate-pulse"></div>
      </div>
    </div>

    <!-- ── Entry Display ──────────────────────────────────────────── -->
    <template v-if="selectedEntry">

      <!-- Bento Grid: Main Card + Side Panel -->
      <div class="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">

        <!-- ── Main Word Card (8 cols) ────────────────────────────── -->
        <div class="lg:col-span-8 bg-surface-container-lowest rounded-[2rem] p-10 komorebi-shadow relative overflow-hidden">
          <div class="absolute -right-16 -top-16 w-64 h-64 bg-primary-container/20 rounded-full blur-3xl pointer-events-none"></div>

          <div class="relative z-10">
            <!-- Word heading + Add button -->
            <div class="flex justify-between items-start mb-12">
              <div class="space-y-4">
                <div class="flex items-end gap-6 flex-wrap">
                  <span class="font-display text-8xl text-on-primary-fixed leading-none">{{ selectedEntry.text }}</span>
                  <span v-if="selectedEntry.reading" class="text-on-surface-variant font-light text-2xl mb-2">{{ selectedEntry.reading }}</span>
                </div>

                <div class="flex items-center gap-3 flex-wrap">
                  <span
                    v-if="selectedEntry.jlptLevel"
                    :class="['text-xs font-bold px-3 py-1 rounded-full', jlptColors[selectedEntry.jlptLevel] ?? 'bg-surface-container text-on-surface']"
                  >
                    JLPT {{ selectedEntry.jlptLevel }}
                  </span>
                  <div v-if="selectedEntry.reading" class="inline-flex items-center gap-2 bg-surface-container text-on-surface-variant px-4 py-1.5 rounded-full text-sm font-medium">
                    <span class="material-symbols-outlined text-sm">record_voice_over</span>
                    {{ selectedEntry.reading }}
                  </div>
                </div>
              </div>

              <!-- Add to Flashcard button -->
              <button
                @click="loadDecks"
                class="p-4 bg-primary rounded-full text-on-primary shadow-lg shadow-primary/20 hover:scale-105 transition-transform group relative shrink-0"
                title="Thêm vào Flashcard"
              >
                <span class="material-symbols-outlined" style="font-variation-settings: 'FILL' 1;">add</span>
                <span class="absolute right-full mr-4 top-1/2 -translate-y-1/2 opacity-0 group-hover:opacity-100 whitespace-nowrap bg-on-surface text-white text-xs px-3 py-1.5 rounded-lg transition-opacity pointer-events-none z-10">
                  Thêm vào Flashcard
                </span>
              </button>
            </div>

            <!-- Content sections -->
            <div class="space-y-10">
              <!-- Ý nghĩa -->
              <div>
                <h3 class="text-xs font-['Inter'] text-outline uppercase tracking-widest mb-4">Ý nghĩa</h3>
                <p class="text-2xl font-display text-on-surface leading-snug">{{ selectedEntry.meaningVn }}</p>
              </div>

              <!-- Giải thích -->
              <div v-if="selectedEntry.explanationShort">
                <h3 class="text-xs font-['Inter'] text-outline uppercase tracking-widest mb-3">Giải thích</h3>
                <p class="text-base text-on-surface-variant leading-relaxed">{{ selectedEntry.explanationShort }}</p>
              </div>

              <!-- Ví dụ câu -->
              <div v-if="selectedEntry.examples?.length">
                <h3 class="text-xs font-['Inter'] text-outline uppercase tracking-widest mb-6">Ví dụ câu</h3>
                <div class="space-y-6">
                  <div
                    v-for="(ex, idx) in selectedEntry.examples"
                    :key="ex.id ?? idx"
                    class="p-6 bg-surface-container-low rounded-2xl border-l-4 border-primary/30"
                  >
                    <p class="text-xl font-medium text-on-surface mb-2">{{ ex.japaneseSentence }}</p>
                    <p class="text-on-surface-variant italic">{{ ex.vietnameseSentence }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Loading spinner overlay -->
          <div v-if="detailLoading" class="absolute top-6 right-6">
            <span class="material-symbols-outlined animate-spin text-primary text-xl">progress_activity</span>
          </div>
        </div>

        <!-- ── Side Info Panel (4 cols) ──────────────────────────── -->
        <div class="lg:col-span-4 space-y-6">

          <!-- Phân tích Kanji -->
          <div v-if="kanjiComponents.length" class="bg-surface-container-low rounded-[2rem] p-8">
            <h3 class="text-xs font-['Inter'] text-outline uppercase tracking-widest mb-6">Phân tích Kanji</h3>
            <div class="space-y-6">
              <div
                v-for="comp in kanjiComponents"
                :key="comp.id"
                class="flex items-center gap-4 cursor-pointer group"
                @click="selectEntry({ id: comp.id, text: comp.text, reading: comp.reading, meaningVn: comp.meaningVn })"
              >
                <div class="w-14 h-14 bg-surface-container-lowest rounded-xl flex items-center justify-center text-3xl font-display text-on-primary-fixed border border-outline-variant/10 group-hover:border-primary/30 transition-colors shrink-0">
                  {{ comp.text }}
                </div>
                <div>
                  <p class="font-bold text-on-surface">{{ comp.reading }}</p>
                  <p class="text-sm text-on-surface-variant">{{ comp.meaningVn }}</p>
                </div>
              </div>
            </div>
          </div>

          <!-- Visual Context Card (ảnh cố định từ stitch.html) -->
          <div class="rounded-[2rem] overflow-hidden komorebi-shadow aspect-square relative group">
            <img
              src="https://lh3.googleusercontent.com/aida-public/AB6AXuDMxaCoGrkfqBby3Spxl1ijp12JuRc3H3JuhxGySUB8FESiSZ8T_jxfJbPnF9u5Z-oOGaCBIi_n7dDNsWC7ePr9f77qkjdGwl9yWtzWnr482dXgg_UVuAFV7conMZ97-hDa_kLg8peEuQp3AmMRZ3doG6kUSXmOBIFkTtqfMTwhDzoMmNacjjg9fIALzrVSmdAG8gnE5gpUFQO8BroqbNeLDTYI_2HqEXGxxK7Hdcu2fJ6u-TC3L1WWjVO1V4NJrJief7EGbDagSUc"
              alt="Ánh nắng xuyên tán lá"
              class="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110"
            >
            <div class="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent"></div>
            <div class="absolute bottom-6 left-6 right-6 text-white">
              <p class="text-xs uppercase tracking-widest opacity-80 mb-1">Cảm hứng</p>
              <p class="font-display font-medium text-lg italic leading-snug">"Với ngôn ngữ, bạn sẽ cảm thấy ở đâu cũng là nhà."</p>
            </div>
          </div>

        </div>
      </div>

      <!-- ── Từ vựng liên quan ──────────────────────────────────────── -->
      <section v-if="relatedWords.length" class="mt-20">
        <h3 class="text-xs font-['Inter'] text-outline uppercase tracking-widest mb-8 text-center">Từ vựng liên quan</h3>
        <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-4">
          <div
            v-for="rel in relatedWords"
            :key="rel.id"
            class="bg-surface-container-low p-6 rounded-2xl hover:bg-surface-container-lowest transition-all cursor-pointer border border-transparent hover:border-outline-variant/10 hover:komorebi-shadow"
            @click="selectEntry({ id: rel.id, text: rel.text, reading: rel.reading, meaningVn: rel.meaningVn })"
          >
            <span class="text-2xl font-display block mb-2 text-on-surface">{{ rel.text }}</span>
            <span class="text-xs text-on-surface-variant block mb-1">{{ rel.reading }}</span>
            <p class="font-medium text-sm text-on-surface line-clamp-2">{{ rel.meaningVn }}</p>
          </div>
        </div>
      </section>

      <!-- ── Bình luận cộng đồng ────────────────────────────────────── -->
      <CommentSection :entry-id="selectedEntry?.id?.toString()" />

    </template>

    <!-- No entry yet (initial / no results) -->
    <div v-else-if="!loading && !error" class="mt-8 relative overflow-hidden rounded-[2.5rem] bg-surface-container-lowest komorebi-shadow">
      <!-- Ambient gradient blobs -->
      <div class="absolute -top-24 -left-24 w-96 h-96 bg-primary-container/30 rounded-full blur-3xl pointer-events-none"></div>
      <div class="absolute -bottom-24 -right-24 w-80 h-80 bg-tertiary-container/20 rounded-full blur-3xl pointer-events-none"></div>

      <div class="relative z-10 px-12 py-20 flex flex-col items-center text-center">
        <!-- Giant decorative kanji -->
        <div class="font-display text-[10rem] leading-none text-primary/10 select-none mb-8 transition-all duration-700">
          語
        </div>

        <!-- Editorial headline -->
        <h2 class="font-display text-3xl md:text-4xl font-extrabold text-on-primary-fixed leading-tight mb-4">
          Bắt đầu hành trình học.
        </h2>
        <p class="text-base text-on-surface-variant max-w-sm leading-relaxed mb-10">
          Nhập một <span class="text-primary font-semibold">từ vựng</span> để khám phá ý nghĩa,
          ví dụ câu và phân tích Kanji.
        </p>

        <!-- Suggestion chips -->
        <div class="flex flex-wrap gap-3 justify-center">
          <button
            v-for="s in [{ label: '丁寧', hint: 'Lịch sự' }, { label: '木漏れ日', hint: 'Ánh sáng qua lá' }, { label: '生き甲斐', hint: 'Lý do sống' }]"
            :key="s.label"
            class="flex items-center gap-2 px-5 py-2.5 bg-surface-container-low rounded-full text-sm font-medium text-on-surface hover:bg-primary-container hover:text-on-primary-container transition-all cursor-pointer"
            @click="handleSearch(s.label)"
          >
            <span class="font-display text-base">{{ s.label }}</span>
            <span class="text-on-surface-variant text-xs">{{ s.hint }}</span>
          </button>
        </div>
      </div>
    </div>

  </div>

  <!-- ── Add to Flashcard Modal ──────────────────────────────────────── -->
  <div v-if="showAddModal" class="fixed inset-0 bg-black/40 z-50 flex items-center justify-center p-4">
    <div class="bg-surface-container-lowest rounded-[2.5rem] max-w-md w-full p-10 animate-in" style="box-shadow: 0 20px 40px rgba(45, 52, 53, 0.08), 0 8px 16px rgba(45, 52, 53, 0.04); backdrop-filter: blur(4px);">
      <!-- Header -->
      <div class="flex items-center justify-between mb-8">
        <h2 class="text-xl font-display font-bold text-on-surface">Thêm vào Flashcard</h2>
        <button
          @click="closeModal"
          class="p-2.5 rounded-full hover:bg-surface-container/50 transition-colors text-on-surface-variant hover:text-on-surface"
        >
          <span class="material-symbols-outlined text-lg">close</span>
        </button>
      </div>

      <!-- Success state -->
      <div v-if="addSuccess" class="text-center py-8">
        <div class="inline-flex items-center justify-center w-16 h-16 bg-primary/15 rounded-full mb-6">
          <span class="material-symbols-outlined text-3xl text-primary">check_circle</span>
        </div>
        <p class="text-lg font-semibold text-on-surface mb-2">Thêm thành công!</p>
        <p class="text-sm text-on-surface-variant">Từ vựng đã được thêm vào bộ thẻ.</p>
      </div>

      <!-- Loading state -->
      <div v-else-if="loadingDecks" class="space-y-4">
        <div class="h-12 bg-surface-container-low rounded-[1.5rem] animate-pulse"></div>
        <div class="h-40 bg-surface-container-low rounded-[1.5rem] animate-pulse"></div>
      </div>

      <!-- Deck selection -->
      <div v-else class="space-y-6">
        <!-- Word preview -->
        <div class="p-6 bg-surface-container-low rounded-[1.75rem]">
          <p class="text-xs font-bold text-on-surface-variant uppercase tracking-widest mb-3">Từ vựng</p>
          <p class="text-3xl font-display font-bold text-on-surface mb-1">{{ selectedEntry?.text }}</p>
          <p class="text-sm text-on-surface-variant mb-3">{{ selectedEntry?.reading }}</p>
          <p class="text-sm text-on-surface leading-relaxed">{{ selectedEntry?.meaningVn }}</p>
        </div>

        <!-- Deck selection -->
        <div v-if="!showCreateDeckForm">
          <p class="text-xs font-bold text-on-surface-variant uppercase tracking-widest mb-4">Chọn bộ thẻ</p>
          <div v-if="decks.length === 0" class="p-8 text-center rounded-[1.75rem] bg-surface-container-low">
            <span class="material-symbols-outlined text-5xl text-on-surface-variant/30 mb-3 block">folder_open</span>
            <p class="text-on-surface-variant mb-6 font-medium">Bạn chưa có bộ thẻ nào</p>
            <button
              @click="showCreateDeckForm = true"
              class="px-6 py-2.5 bg-gradient-to-br from-primary to-primary-container text-on-primary rounded-full font-semibold text-sm transition-all hover:shadow-lg"
            >
              Tạo bộ thẻ mới
            </button>
          </div>
          <div v-else class="space-y-2 max-h-60 overflow-y-auto">
            <label
              v-for="deck in decks"
              :key="deck.id"
              class="flex items-start p-4 rounded-[1.25rem] cursor-pointer hover:bg-surface-container-low transition-all"
              style="border: 1px solid rgba(173, 179, 180, 0.15);"
            >
              <input
                type="radio"
                :value="deck.id"
                v-model="selectedDeckId"
                class="mt-1.5 cursor-pointer accent-primary"
              />
              <div class="ml-4 flex-1">
                <p class="font-semibold text-on-surface text-sm">{{ deck.name }}</p>
                <p class="text-xs text-on-surface-variant mt-1">{{ deck.description }}</p>
                <p class="text-xs text-on-surface-variant mt-2">
                  {{ deck.cardStats?.total || 0 }} từ
                </p>
              </div>
            </label>

            <!-- Create new deck button -->
            <button
              @click="showCreateDeckForm = true"
              class="w-full p-4 rounded-[1.25rem] font-semibold text-sm text-on-surface-variant hover:text-primary transition-all mt-3 flex items-center justify-center gap-2"
              style="border: 1px solid rgba(173, 179, 180, 0.2); background: rgba(199, 206, 207, 0.05);"
            >
              <span class="material-symbols-outlined text-lg">add</span>
              <span>Tạo bộ thẻ mới</span>
            </button>
          </div>
        </div>

        <!-- Create Deck Form -->
        <div v-else>
          <p class="text-xs font-bold text-on-surface-variant uppercase tracking-widest mb-6">Tạo bộ thẻ mới</p>
          <div class="space-y-5">
            <div>
              <label class="block text-sm font-semibold text-on-surface mb-2.5">Tên bộ thẻ</label>
              <input
                v-model="newDeckName"
                type="text"
                placeholder="Nhập tên bộ thẻ..."
                class="w-full px-4 py-3 rounded-[1.25rem] bg-surface-container-high text-on-surface placeholder:text-on-surface-variant/50 transition-all"
                style="border: 1px solid rgba(173, 179, 180, 0.15);"
                @focus="$event.target.style.borderColor = 'rgba(69, 97, 125, 0.2)'"
                @blur="$event.target.style.borderColor = 'rgba(173, 179, 180, 0.15)'"
              />
            </div>
            <div>
              <label class="block text-sm font-semibold text-on-surface mb-2.5">Mô tả (tùy chọn)</label>
              <textarea
                v-model="newDeckDescription"
                placeholder="Nhập mô tả bộ thẻ..."
                class="w-full px-4 py-3 rounded-[1.25rem] bg-surface-container-high text-on-surface placeholder:text-on-surface-variant/50 resize-none transition-all"
                style="border: 1px solid rgba(173, 179, 180, 0.15);"
                rows="3"
                              @focus="$event.target.style.borderColor = 'rgba(69, 97, 125, 0.2)'"
                              @blur="$event.target.style.borderColor = 'rgba(173, 179, 180, 0.15)'"
              ></textarea>
            </div>
          </div>
        </div>

        <!-- Action buttons -->
        <div class="flex gap-3 pt-6">
          <button
            @click="closeModal"
            class="flex-1 py-3 px-4 rounded-full font-semibold text-sm text-on-surface-variant hover:text-on-surface transition-all"
            style="border: 1px solid rgba(173, 179, 180, 0.2); background: rgba(199, 206, 207, 0.03);"
          >
            {{ showCreateDeckForm ? 'Quay lại' : 'Hủy' }}
          </button>
          <button
            v-if="showCreateDeckForm"
            @click="createNewDeck"
            :disabled="!newDeckName.trim() || creatingDeck"
            class="flex-1 py-3 px-4 rounded-full font-semibold text-sm bg-gradient-to-br from-primary to-primary-container text-on-primary transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 hover:shadow-lg"
          >
            <span v-if="creatingDeck" class="material-symbols-outlined animate-spin text-lg">progress_activity</span>
            <span>{{ creatingDeck ? 'Đang tạo...' : 'Tạo' }}</span>
          </button>
          <button
            v-else
            @click="addToFlashcard"
            :disabled="!selectedDeckId || addingFlashcard || decks.length === 0"
            class="flex-1 py-3 px-4 rounded-full font-semibold text-sm bg-gradient-to-br from-primary to-primary-container text-on-primary transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 hover:shadow-lg"
          >
            <span v-if="addingFlashcard" class="material-symbols-outlined animate-spin text-lg">progress_activity</span>
            <span>{{ addingFlashcard ? 'Đang thêm...' : 'Thêm' }}</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
@keyframes spin { to { transform: rotate(360deg); } }
.animate-spin { animation: spin 0.8s linear infinite; }

@keyframes pulse { 0%,100% { opacity:1; } 50% { opacity:.5; } }
.animate-pulse { animation: pulse 1.5s ease-in-out infinite; }

@keyframes slideUp { from { transform: translateY(1rem); opacity: 0; } to { transform: translateY(0); opacity: 1; } }
.animate-in { animation: slideUp 0.3s ease-out; }

.line-clamp-2 { display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }

.komorebi-shadow { box-shadow: 0 20px 40px rgba(45, 52, 53, 0.04); }
.hover\:komorebi-shadow:hover { box-shadow: 0 20px 40px rgba(45, 52, 53, 0.08); }
</style>
