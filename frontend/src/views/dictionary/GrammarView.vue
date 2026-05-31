<script setup>
import { ref } from 'vue'
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

// ── Search (size:1) ───────────────────────────────────────────────────────
async function doSearch() {
  loading.value = true
  error.value = ''
  selectedEntry.value = null
  try {
    const res = await dictionaryService.search({ q: searchQuery.value, type: 'grammar', page: 0, size: 1 })
    const items = res.data ?? []
    if (items.length > 0) {
      await selectEntry(items[0])
    } else {
      error.value = 'Không tìm thấy mẫu ngữ pháp nào phù hợp.'
    }
  } catch {
    error.value = 'Không thể tải dữ liệu. Vui lòng thử lại.'
  } finally {
    loading.value = false
  }
}

const debouncedSearch = useDebounceFn(() => doSearch(), 350)
function handleSearch(q) { searchQuery.value = q; debouncedSearch() }

// ── Detail ────────────────────────────────────────────────────────────────
async function selectEntry(entry) {
  if (selectedEntry.value?.id === entry.id) return
  detailLoading.value = true
  selectedEntry.value = entry
  try {
    const res = await dictionaryService.getById(entry.id)
    selectedEntry.value = res.data
  } catch { /* keep basic */ } finally { detailLoading.value = false }
}

// ── Helpers ───────────────────────────────────────────────────────────────
function synonyms(relations) {
  return (relations ?? []).filter(r => r.relationType === 'synonym')
}

const jlptColors = {
  N5: 'bg-green-100 text-green-800',
  N4: 'bg-blue-100 text-blue-800',
  N3: 'bg-yellow-100 text-yellow-800',
  N2: 'bg-orange-100 text-orange-800',
  N1: 'bg-red-100 text-red-800',
}
const jlptLabel = { N5: 'Sơ cấp', N4: 'Sơ cấp', N3: 'Trung cấp', N2: 'Trung cao', N1: 'Cao cấp' }

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
      frontReading: selectedEntry.value.text || '',
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

function closeModal() {
  showAddModal.value = false
  addSuccess.value = false
  showCreateDeckForm.value = false
  newDeckName.value = ''
  newDeckDescription.value = ''
}

// Chỉ search khi user nhập — không auto-search khi mount

</script>

<template>
  <div class="w-full max-w-5xl mx-auto">

    <!-- ── Search Header ──────────────────────────────────────────────── -->
    <SearchHeader
      placeholder="Nhập cấu trúc ngữ pháp (vd: ~うちに, ~ばかり...)"
      :suggestions="[
        { label: 'Te form', value: '～て' },
        { label: 'Ba form', value: '～ば' },
        { label: 'Tara form', value: '～たら' },
      ]"
      @search="handleSearch"
    />

    <!-- Error -->
    <div v-if="error" class="mb-6 p-4 rounded-xl bg-error-container/60 text-on-error-container text-sm flex items-center gap-2">
      <span class="material-symbols-outlined text-error">error</span>
      {{ error }}
    </div>

    <!-- Loading skeleton -->
    <div v-if="loading && !selectedEntry" class="space-y-6 mt-8">
      <div class="h-56 rounded-[2rem] bg-surface-container-low animate-pulse"></div>
      <div class="h-72 rounded-[2rem] bg-surface-container-low animate-pulse"></div>
    </div>

    <!-- ── Entry Display ───────────────────────────────────────────────── -->
    <template v-if="selectedEntry">

      <!-- ── Core Info Card ──────────────────────────────────────────── -->
      <div class="bg-surface-container-lowest rounded-[2rem] p-10 relative overflow-hidden komorebi-shadow">
        <!-- Decorative watermark -->
        <div class="absolute top-0 right-0 p-8 select-none pointer-events-none">
          <span class="text-[9rem] font-display font-bold leading-none text-on-surface opacity-[0.03]">文</span>
        </div>

        <div class="relative z-10">
          <!-- Badges row + Flashcard button -->
          <div class="flex items-start justify-between mb-8 gap-4 flex-wrap">
            <div class="flex items-center gap-3 flex-wrap">
              <span
                v-if="selectedEntry.jlptLevel"
                :class="['px-4 py-1.5 rounded-full text-xs font-bold uppercase tracking-widest', jlptColors[selectedEntry.jlptLevel] ?? 'bg-surface-container text-on-surface']"
              >
                JLPT {{ selectedEntry.jlptLevel }}
              </span>
              <span
                v-if="selectedEntry.jlptLevel"
                class="px-4 py-1.5 bg-secondary-container text-on-secondary-container rounded-full text-xs font-bold uppercase tracking-widest"
              >
                {{ jlptLabel[selectedEntry.jlptLevel] ?? 'Ngữ pháp' }}
              </span>
              <span class="inline-flex items-center gap-1.5 bg-surface-container text-on-surface-variant px-3 py-1.5 rounded-full text-sm font-medium">
                <span class="material-symbols-outlined text-sm">auto_stories</span>
                Ngữ pháp
              </span>
              <span v-if="detailLoading" class="material-symbols-outlined animate-spin text-primary text-xl">progress_activity</span>
            </div>

            <!-- Add to Flashcard -->
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

          <!-- Pattern title -->
          <h2 class="font-display text-5xl md:text-6xl font-extrabold text-on-surface mb-5 leading-tight">
            {{ selectedEntry.text }}
          </h2>
          <p class="text-xl text-on-surface-variant font-medium leading-relaxed max-w-2xl">
            {{ selectedEntry.meaningVn }}
          </p>

          <!-- Cấu trúc / Cách dùng -->
          <div v-if="selectedEntry.explanationShort" class="mt-10 p-6 bg-surface-container-low rounded-2xl">
            <h3 class="text-xs font-bold uppercase tracking-widest text-outline mb-3 flex items-center gap-2">
              <span class="material-symbols-outlined text-sm">layers</span>
              Cấu trúc / Cách dùng
            </h3>
            <p class="text-base text-on-surface leading-relaxed">{{ selectedEntry.explanationShort }}</p>
          </div>
        </div>
      </div>

      <!-- ── Ví dụ minh họa ─────────────────────────────────────────── -->
      <div v-if="selectedEntry.examples?.length" class="mt-6 bg-surface-container-lowest rounded-[2rem] p-10 komorebi-shadow">
        <h3 class="font-display text-2xl font-bold flex items-center gap-3 mb-8">
          <span class="material-symbols-outlined text-primary">auto_awesome</span>
          Ví dụ minh họa
        </h3>
        <div class="space-y-6">
          <div
            v-for="(ex, idx) in selectedEntry.examples"
            :key="ex.id ?? idx"
            class="group flex items-start gap-5"
            :class="idx > 0 ? 'border-t border-outline-variant/10 pt-6' : ''"
          >
            <div class="w-8 h-8 rounded-full bg-surface-container-high flex items-center justify-center flex-shrink-0 text-xs font-bold text-outline group-hover:bg-primary group-hover:text-white transition-colors">
              {{ String(idx + 1).padStart(2, '0') }}
            </div>
            <div class="space-y-1.5">
              <p class="text-2xl font-display leading-snug text-on-surface">{{ ex.japaneseSentence }}</p>
              <p class="text-on-surface-variant text-base">{{ ex.vietnameseSentence }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- ── Ngữ pháp tương đương ──────────────────────────────────── -->
      <div v-if="synonyms(selectedEntry.relations).length" class="mt-6 bg-surface-container-lowest rounded-[2rem] p-8 komorebi-shadow">
        <h4 class="text-xs font-bold uppercase tracking-widest text-outline mb-6">Ngữ pháp tương đương</h4>
        <div class="space-y-3">
          <div
            v-for="rel in synonyms(selectedEntry.relations)"
            :key="rel.id"
            class="flex items-center justify-between p-4 bg-surface-container-low rounded-2xl cursor-pointer hover:translate-x-1 transition-transform"
            @click="selectEntry({ id: rel.id, text: rel.text, meaningVn: rel.meaningVn })"
          >
            <div>
              <span class="font-bold text-on-surface">{{ rel.text }}</span>
              <p class="text-xs text-on-surface-variant mt-0.5">{{ rel.meaningVn }}</p>
            </div>
            <span class="material-symbols-outlined text-outline">chevron_right</span>
          </div>
        </div>
      </div>

      <!-- ── Bình luận cộng đồng ────────────────────────────────────── -->
      <CommentSection :entry-id="selectedEntry?.id?.toString()" />

    </template>

    <!-- ── Add to Flashcard Modal ──────────────────────────────────────── -->
    <div v-if="showAddModal" class="fixed inset-0 z-50 flex items-center justify-center p-4" style="background: rgba(45, 52, 53, 0.1); backdrop-filter: blur(4px);">
      <div class="bg-surface-container-lowest rounded-[2.5rem] max-w-md w-full p-10 animate-in" style="box-shadow: 0 20px 40px rgba(45, 52, 53, 0.08), 0 8px 16px rgba(45, 52, 53, 0.04);">
        <div class="flex items-center justify-between mb-8">
          <h2 class="text-xl font-display font-bold text-on-surface">Thêm vào Flashcard</h2>
          <button @click="closeModal" class="p-2.5 rounded-full hover:bg-surface-container/50 transition-colors text-on-surface-variant hover:text-on-surface">
            <span class="material-symbols-outlined text-lg">close</span>
          </button>
        </div>
        <div v-if="addSuccess" class="text-center py-8">
          <div class="inline-flex items-center justify-center w-16 h-16 bg-primary/15 rounded-full mb-6">
            <span class="material-symbols-outlined text-3xl text-primary">check_circle</span>
          </div>
          <p class="text-lg font-semibold text-on-surface mb-2">Thêm thành công!</p>
          <p class="text-sm text-on-surface-variant">Từ vựng đã được thêm vào bộ thẻ.</p>
        </div>
        <div v-else-if="loadingDecks" class="space-y-4">
          <div class="h-12 bg-surface-container-low rounded-[1.5rem] animate-pulse"></div>
          <div class="h-40 bg-surface-container-low rounded-[1.5rem] animate-pulse"></div>
        </div>
        <div v-else class="space-y-6">
          <div class="p-6 bg-surface-container-low rounded-[1.75rem]">
            <p class="text-xs font-bold text-on-surface-variant uppercase tracking-widest mb-3">Mẫu ngữ pháp</p>
            <p class="text-3xl font-display font-bold text-on-surface mb-1">{{ selectedEntry?.text }}</p>
            <p class="text-sm text-on-surface leading-relaxed">{{ selectedEntry?.meaningVn }}</p>
          </div>
          <div v-if="!showCreateDeckForm">
            <p class="text-xs font-bold text-on-surface-variant uppercase tracking-widest mb-4">Chọn bộ thẻ</p>
            <div v-if="decks.length === 0" class="p-8 text-center rounded-[1.75rem] bg-surface-container-low">
              <span class="material-symbols-outlined text-5xl text-on-surface-variant/30 mb-3 block">folder_open</span>
              <p class="text-on-surface-variant mb-6 font-medium">Bạn chưa có bộ thẻ nào</p>
              <button @click="showCreateDeckForm = true" class="px-6 py-2.5 bg-gradient-to-br from-primary to-primary-container text-on-primary rounded-full font-semibold text-sm transition-all hover:shadow-lg">
                Tạo bộ thẻ mới
              </button>
            </div>
            <div v-else class="space-y-2 max-h-60 overflow-y-auto">
              <label v-for="deck in decks" :key="deck.id" class="flex items-start p-4 rounded-[1.25rem] cursor-pointer hover:bg-surface-container-low transition-all" style="border: 1px solid rgba(173, 179, 180, 0.15);">
                <input type="radio" :value="deck.id" v-model="selectedDeckId" class="mt-1.5 cursor-pointer accent-primary" />
                <div class="ml-4 flex-1">
                  <p class="font-semibold text-on-surface text-sm">{{ deck.name }}</p>
                  <p class="text-xs text-on-surface-variant mt-1">{{ deck.description }}</p>
                  <p class="text-xs text-on-surface-variant mt-2">{{ deck.cardStats?.total || 0 }} từ</p>
                </div>
              </label>
              <button @click="showCreateDeckForm = true" class="w-full p-4 rounded-[1.25rem] font-semibold text-sm text-on-surface-variant hover:text-primary transition-all mt-3 flex items-center justify-center gap-2" style="border: 1px solid rgba(173, 179, 180, 0.2); background: rgba(199, 206, 207, 0.05);">
                <span class="material-symbols-outlined text-lg">add</span>
                <span>Tạo bộ thẻ mới</span>
              </button>
            </div>
          </div>
          <div v-else>
            <p class="text-xs font-bold text-on-surface-variant uppercase tracking-widest mb-6">Tạo bộ thẻ mới</p>
            <div class="space-y-5">
              <div>
                <label class="block text-sm font-semibold text-on-surface mb-2.5">Tên bộ thẻ</label>
                <input v-model="newDeckName" type="text" placeholder="Nhập tên bộ thẻ..." class="w-full px-4 py-3 rounded-[1.25rem] bg-surface-container-high text-on-surface placeholder:text-on-surface-variant/50 transition-all" style="border: 1px solid rgba(173, 179, 180, 0.15);" @focus="$event.target.style.borderColor = 'rgba(69, 97, 125, 0.2)'" @blur="$event.target.style.borderColor = 'rgba(173, 179, 180, 0.15)'" />
              </div>
              <div>
                <label class="block text-sm font-semibold text-on-surface mb-2.5">Mô tả (tùy chọn)</label>
                <textarea v-model="newDeckDescription" placeholder="Nhập mô tả bộ thẻ..." class="w-full px-4 py-3 rounded-[1.25rem] bg-surface-container-high text-on-surface placeholder:text-on-surface-variant/50 resize-none transition-all" style="border: 1px solid rgba(173, 179, 180, 0.15);" rows="3" @focus="$event.target.style.borderColor = 'rgba(69, 97, 125, 0.2)'" @blur="$event.target.style.borderColor = 'rgba(173, 179, 180, 0.15)'"></textarea>
              </div>
            </div>
          </div>
          <div class="flex gap-3 pt-6">
            <button @click="closeModal" class="flex-1 py-3 px-4 rounded-full font-semibold text-sm text-on-surface-variant hover:text-on-surface transition-all" style="border: 1px solid rgba(173, 179, 180, 0.2); background: rgba(199, 206, 207, 0.03);">
              {{ showCreateDeckForm ? 'Quay lại' : 'Hủy' }}
            </button>
            <button v-if="showCreateDeckForm" @click="createNewDeck" :disabled="!newDeckName.trim() || creatingDeck" class="flex-1 py-3 px-4 rounded-full font-semibold text-sm bg-gradient-to-br from-primary to-primary-container text-on-primary transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 hover:shadow-lg">
              <span v-if="creatingDeck" class="material-symbols-outlined animate-spin text-lg">progress_activity</span>
              <span>{{ creatingDeck ? 'Đang tạo...' : 'Tạo' }}</span>
            </button>
            <button v-else @click="addToFlashcard" :disabled="!selectedDeckId || addingFlashcard || decks.length === 0" class="flex-1 py-3 px-4 rounded-full font-semibold text-sm bg-gradient-to-br from-primary to-primary-container text-on-primary transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 hover:shadow-lg">
              <span v-if="addingFlashcard" class="material-symbols-outlined animate-spin text-lg">progress_activity</span>
              <span>{{ addingFlashcard ? 'Đang thêm...' : 'Thêm' }}</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- No entry yet -->
    <div v-else-if="!loading && !error" class="mt-8 relative overflow-hidden rounded-[2.5rem] bg-surface-container-lowest komorebi-shadow">
      <!-- Ambient gradient blobs -->
      <div class="absolute -top-24 -left-24 w-96 h-96 bg-primary-container/30 rounded-full blur-3xl pointer-events-none"></div>
      <div class="absolute -bottom-24 -right-24 w-80 h-80 bg-secondary-container/20 rounded-full blur-3xl pointer-events-none"></div>

      <div class="relative z-10 px-12 py-20 flex flex-col items-center text-center">
        <!-- Giant decorative kanji -->
        <div class="font-display text-[10rem] leading-none text-primary/10 select-none mb-8">
          文
        </div>

        <!-- Editorial headline -->
        <h2 class="font-display text-3xl md:text-4xl font-extrabold text-on-primary-fixed leading-tight mb-4">
          Bắt đầu hành trình học.
        </h2>
        <p class="text-base text-on-surface-variant max-w-sm leading-relaxed mb-10">
          Nhập một <span class="text-primary font-semibold">mẫu ngữ pháp</span> để khám phá
          cấu trúc, cách dùng và ví dụ minh họa.
        </p>

        <!-- Suggestion chips -->
        <div class="flex flex-wrap gap-3 justify-center">
          <button
            v-for="s in [{ label: '～てから', hint: 'Sau khi' }, { label: '～ながら', hint: 'Vừa... vừa' }, { label: '～ばかり', hint: 'Vừa mới' }]"
            :key="s.label"
            class="flex items-center gap-2 px-5 py-2.5 bg-surface-container-low rounded-full text-sm font-medium text-on-surface hover:bg-primary-container hover:text-on-primary-container transition-all cursor-pointer"
            @click="handleSearch(s.label)"
          >
            <span class="font-display font-bold">{{ s.label }}</span>
            <span class="text-on-surface-variant text-xs">{{ s.hint }}</span>
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

.komorebi-shadow { box-shadow: 0 20px 40px rgba(45, 52, 53, 0.04); }
</style>
