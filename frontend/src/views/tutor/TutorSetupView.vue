<template>
  <div class="w-full">
    <section class="mb-10">
      <span class="text-xs font-bold tracking-[0.2em] text-secondary uppercase mb-2 block">AI Session Setup</span>
      <h2 class="text-4xl md:text-5xl font-display font-bold text-on-primary-fixed tracking-tight mb-4">Gia sư Trí tuệ</h2>
      <p class="text-on-surface-variant max-w-2xl leading-relaxed">Thiết lập môi trường luyện tập của bạn. Chọn deck, chủ đề, cấp độ JLPT và thời lượng trước khi bắt đầu phiên AI Tutor.</p>
    </section>

    <div v-if="errorMessage" class="mb-8 p-4 md:p-5 rounded-2xl bg-error-container text-on-error-container flex flex-col md:flex-row md:items-center md:justify-between gap-4">
      <p class="text-sm font-medium">{{ errorMessage }}</p>
      <button class="px-4 py-2 rounded-full bg-on-error-container text-error-container font-semibold text-sm transition-transform active:scale-95" @click="retryLoad">
        Thử lại
      </button>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-12 gap-8">
      <div class="lg:col-span-7 flex flex-col gap-6">
        <div class="flex items-start justify-between gap-4">
          <div>
            <h3 class="text-xl font-headline font-semibold text-on-surface flex items-center gap-2">
              <span class="w-8 h-8 rounded-full bg-tertiary-container text-on-tertiary-container flex items-center justify-center text-sm">1</span>
              Chọn bộ thẻ từ vựng
            </h3>
            <p class="mt-2 text-sm text-on-surface-variant">AI Tutor sẽ ưu tiên thẻ đến hạn trong deck đã chọn, sau đó bổ sung thẻ mới trong giới hạn `maxNew = 10`.</p>
          </div>
        </div>

        <div v-if="loadingDecks" class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div v-for="i in 4" :key="i" class="p-6 rounded-2xl bg-surface-container-lowest shadow-sm animate-pulse">
            <div class="h-4 w-20 rounded-full bg-surface-container-highest mb-4"></div>
            <div class="h-6 w-3/4 rounded-full bg-surface-container-highest mb-3"></div>
            <div class="h-4 w-1/2 rounded-full bg-surface-container-highest"></div>
          </div>
        </div>

        <div v-else-if="!decks.length" class="p-8 rounded-3xl bg-surface-container-lowest shadow-sm text-center">
          <div class="mx-auto mb-4 w-14 h-14 rounded-full bg-surface-container flex items-center justify-center text-primary">
            <span class="material-symbols-outlined">library_books</span>
          </div>
          <h4 class="text-lg font-semibold text-on-surface mb-2">Chưa có deck nào</h4>
          <p class="text-sm text-on-surface-variant max-w-md mx-auto">Hãy tạo hoặc đồng bộ deck trước, sau đó quay lại để chọn thẻ cho AI Tutor.</p>
        </div>

        <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <button
            v-for="deck in decks"
            :key="deck.id"
            type="button"
            @click="selectDeck(deck)"
            class="p-6 rounded-2xl text-left transition-all duration-200 bg-surface-container-lowest shadow-sm hover:shadow-md active:scale-[0.99]"
            :class="selectedDeckId === deck.id ? 'bg-primary-container text-on-primary-container shadow-lg shadow-primary/10' : ''"
          >
            <div class="flex items-start justify-between gap-4 mb-4">
              <span class="material-symbols-outlined text-3xl" :class="selectedDeckId === deck.id ? 'text-on-primary-container' : 'text-primary'">menu_book</span>
              <span v-if="selectedDeckId === deck.id" class="material-symbols-outlined text-on-primary-container" style="font-variation-settings: 'FILL' 1;">check_circle</span>
            </div>
            <h4 class="font-bold text-lg mb-1">{{ deck.name }}</h4>
            <p class="text-sm mb-4" :class="selectedDeckId === deck.id ? 'text-on-primary-container/80' : 'text-on-surface-variant'">{{ deck.description || 'Chưa có mô tả.' }}</p>
            <div class="flex flex-wrap gap-2 text-xs">
              <span class="px-3 py-1 rounded-full" :class="selectedDeckId === deck.id ? 'bg-on-primary-container/10 text-on-primary-container' : 'bg-surface-container-highest text-on-surface-variant'">Tổng: {{ deck.cardStats?.total ?? 0 }}</span>
              <span class="px-3 py-1 rounded-full" :class="selectedDeckId === deck.id ? 'bg-on-primary-container/10 text-on-primary-container' : 'bg-surface-container-highest text-on-surface-variant'">Đến hạn: {{ deck.cardStats?.dueToday ?? 0 }}</span>
            </div>
          </button>
        </div>
      </div>

      <div class="lg:col-span-5 flex flex-col gap-6">
        <h3 class="text-xl font-headline font-semibold text-on-surface flex items-center gap-2">
          <span class="w-8 h-8 rounded-full bg-tertiary-container text-on-tertiary-container flex items-center justify-center text-sm">2</span>
          Chủ đề hội thoại
        </h3>

        <div class="space-y-3">
          <button
            v-for="scenario in scenarioOptions"
            :key="scenario.value"
            type="button"
            @click="selectedScenarioName = scenario.value"
            class="w-full flex items-center gap-4 p-4 rounded-2xl text-left transition-all duration-200 bg-surface-container-lowest shadow-sm hover:shadow-md active:scale-[0.99]"
            :class="selectedScenarioName === scenario.value ? 'bg-secondary-container text-on-secondary-container shadow-lg shadow-secondary/10' : ''"
          >
            <span class="material-symbols-outlined text-2xl" :class="selectedScenarioName === scenario.value ? 'text-on-secondary-container' : 'text-on-surface-variant'">{{ scenario.icon }}</span>
            <div class="flex-grow">
              <p class="font-semibold">{{ scenario.label }}</p>
              <p class="text-xs" :class="selectedScenarioName === scenario.value ? 'text-on-secondary-container/80' : 'text-on-surface-variant'">{{ scenario.description }}</p>
            </div>
          </button>
        </div>

        <div class="p-6 rounded-3xl bg-gradient-to-br from-tertiary to-tertiary-dim text-on-tertiary overflow-hidden relative shadow-lg shadow-tertiary/15">
          <div class="relative z-10">
            <p class="text-[10px] font-bold uppercase tracking-widest opacity-70 mb-2">AI Persona</p>
            <h4 class="text-lg font-headline font-bold mb-1">Sakura-sensei</h4>
            <p class="text-sm opacity-90 leading-relaxed">"Tôi sẽ giúp bạn sửa lỗi phát âm và cách dùng từ tự nhiên nhất như người bản xứ."</p>
          </div>
          <div class="absolute -right-4 -bottom-4 opacity-10">
            <span class="material-symbols-outlined text-9xl">psychology</span>
          </div>
        </div>

        <div class="p-6 rounded-3xl bg-surface-container-lowest shadow-sm">
          <h4 class="text-sm font-semibold mb-4">Tùy chỉnh phiên</h4>

          <div class="mb-6">
            <p class="text-xs text-on-surface-variant mb-2">Cấp độ JLPT</p>
            <div class="flex flex-wrap gap-2">
              <button
                v-for="lvl in jlptLevels"
                :key="lvl"
                type="button"
                @click="selectedLevel = lvl"
                class="px-4 py-2 rounded-full transition-all"
                :class="selectedLevel === lvl ? 'bg-primary-container text-on-primary-container font-semibold shadow-sm' : 'bg-surface-container-highest text-on-surface-variant hover:bg-surface-container-high'"
              >
                {{ lvl }}
              </button>
            </div>
          </div>

          <div>
            <p class="text-xs text-on-surface-variant mb-2">Thời lượng</p>
            <div class="flex flex-wrap gap-2">
              <button
                v-for="d in durationOptions"
                :key="d"
                type="button"
                @click="durationMinutes = d"
                class="px-4 py-2 rounded-full transition-all"
                :class="durationMinutes === d ? 'bg-primary-container text-on-primary-container font-semibold shadow-sm' : 'bg-surface-container-highest text-on-surface-variant hover:bg-surface-container-high'"
              >
                {{ d }} phút
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <section class="mt-10 p-6 rounded-3xl bg-surface-container-lowest shadow-sm">
      <div class="flex flex-col lg:flex-row lg:items-end lg:justify-between gap-3 mb-5">
        <div>
          <h3 class="text-xl font-headline font-semibold text-on-surface flex items-center gap-2">
            <span class="w-8 h-8 rounded-full bg-tertiary-container text-on-tertiary-container flex items-center justify-center text-sm">3</span>
            Preview target words
          </h3>
          <p class="mt-2 text-sm text-on-surface-variant">Các thẻ đến hạn trong deck sẽ được dùng làm ngữ cảnh cho AI Tutor. Bạn có thể bỏ thẻ không phù hợp trước khi bắt đầu.</p>
        </div>
        <div class="text-sm text-on-surface-variant">
          <span class="font-semibold text-on-surface">{{ previewTargetWords.length }}</span> thẻ sẽ được gửi vào phiên
        </div>
      </div>

      <div v-if="loadingDueCards" class="grid grid-cols-1 md:grid-cols-2 gap-3">
        <div v-for="i in 4" :key="i" class="p-4 rounded-2xl bg-surface-container animate-pulse">
          <div class="h-4 w-24 rounded-full bg-surface-container-highest mb-3"></div>
          <div class="h-5 w-3/4 rounded-full bg-surface-container-highest mb-2"></div>
          <div class="h-4 w-1/2 rounded-full bg-surface-container-highest"></div>
        </div>
      </div>

      <div v-else-if="!selectedDeck" class="p-8 rounded-2xl bg-surface-container text-center text-on-surface-variant">
        Chọn một deck để xem các thẻ đến hạn và tạo target words.
      </div>

      <div v-else-if="!previewTargetWords.length" class="p-8 rounded-2xl bg-surface-container text-center text-on-surface-variant">
        Deck này hiện chưa có thẻ nào phù hợp để đưa vào AI Tutor.
      </div>

      <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-3">
        <article
          v-for="word in previewTargetWords"
          :key="word.id"
          class="p-4 rounded-2xl bg-surface-container-low shadow-sm flex flex-col gap-3"
        >
          <div class="flex items-start justify-between gap-3">
            <div>
              <p class="text-xs font-bold uppercase tracking-[0.2em] text-secondary">{{ word.status || 'target' }}</p>
              <h4 class="text-lg font-semibold text-on-surface">{{ word.surface }}</h4>
              <p v-if="word.reading" class="text-sm text-primary">{{ word.reading }}</p>
            </div>
            <button
              type="button"
              class="w-9 h-9 rounded-full bg-surface-container-highest text-on-surface-variant flex items-center justify-center transition-transform active:scale-95"
              @click="removeTargetWord(word.id)"
              :disabled="!canRemoveTargets"
              title="Bỏ thẻ này khỏi phiên"
            >
              <span class="material-symbols-outlined text-sm">close</span>
            </button>
          </div>
          <p class="text-sm text-on-surface-variant leading-relaxed">{{ word.meaning || 'Chưa có nghĩa.' }}</p>
        </article>
      </div>
    </section>

    <div class="mt-10 flex flex-col md:flex-row items-center justify-between gap-6 p-8 rounded-3xl bg-surface-container-low shadow-sm">
      <div class="space-y-2">
        <p class="text-sm font-semibold text-on-surface">Tóm tắt phiên</p>
        <p class="text-sm text-on-surface-variant">
          Deck: {{ selectedDeck?.name || 'Chưa chọn' }} • Chủ đề: {{ selectedScenarioLabel }} • JLPT: {{ selectedLevel }} • Thời lượng: {{ durationMinutes }} phút
        </p>
        <p class="text-xs text-on-surface-variant">
          {{ previewTargetWords.length }} target words • Ưu tiên LEARNING và REVIEW đến hạn, sau đó bổ sung NEW trong giới hạn `maxNew = 10`.
        </p>
      </div>

      <button
        class="bg-gradient-to-r from-primary to-primary-dim text-on-primary px-10 py-4 rounded-full font-headline font-bold text-lg shadow-[0_12px_32px_rgba(69,97,125,0.3)] hover:scale-105 active:scale-95 transition-all flex items-center gap-3 disabled:opacity-60 disabled:cursor-not-allowed"
        :disabled="!canStartSession"
        @click="startConversation"
      >
        <span v-if="startingSession">Đang tạo phiên...</span>
        <span v-else>Bắt đầu hội thoại</span>
        <span class="material-symbols-outlined">auto_awesome</span>
      </button>
    </div>

    <p v-if="startError" class="mt-4 text-sm text-error">{{ startError }}</p>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useTutorStore } from '@/stores/tutor'
import { tutorService } from '@/services/tutor'

const router = useRouter()
const tutorStore = useTutorStore()

const decks = ref([])
const selectedDeckId = ref(null)
const dueCards = ref([])
const excludedTargetWordIds = ref([])
const loadingDecks = ref(false)
const loadingDueCards = ref(false)
const startError = ref('')
const deckError = ref('')
const startingSession = ref(false)

const scenarioOptions = [
  { value: 'auto', label: 'Để AI chọn chủ đề', description: 'AI sẽ suy luận chủ đề dựa trên các thẻ trong deck', icon: 'smart_toy' },
  { value: 'restaurant', label: 'Tại nhà hàng', description: 'Gọi món và xử lý tình huống', icon: 'skillet' },
  { value: 'shopping', label: 'Mua sắm', description: 'Hỏi giá và mặc cả lịch sự', icon: 'shopping_bag' },
  { value: 'interview', label: 'Phỏng vấn xin việc', description: 'Luyện tập kính ngữ (Keigo)', icon: 'badge' },
  { value: 'travel', label: 'Du lịch', description: 'Đặt phòng, hỏi đường và di chuyển', icon: 'travel_explore' },
]

const jlptLevels = ['N5', 'N4', 'N3', 'N2', 'N1']
const durationOptions = [10, 15, 20]

const selectedScenarioName = ref('restaurant')
const selectedLevel = ref('N3')
const durationMinutes = ref(15)

const selectedDeck = computed(() => decks.value.find(deck => deck.id === selectedDeckId.value) || null)

const selectedScenarioLabel = computed(() => {
  return scenarioOptions.find(option => option.value === selectedScenarioName.value)?.label || 'Chưa chọn'
})

const previewTargetWords = computed(() => {
  return dueCards.value
    .filter(card => !excludedTargetWordIds.value.includes(card.id))
    .map(card => ({
      id: card.id,
      surface: card.frontText || card.front_text || '',
      reading: card.frontReading || card.front_reading || '',
      meaning: card.backText || card.back_text || '',
      status: card.status || card.cardStatus || card.card_status || '',
    }))
    .filter(word => word.surface || word.reading || word.meaning)
})

const canRemoveTargets = computed(() => previewTargetWords.value.length > 0)

const canStartSession = computed(() => {
  return Boolean(
    selectedDeckId.value &&
    selectedScenarioName.value &&
    previewTargetWords.value.length > 0 &&
    !loadingDecks.value &&
    !loadingDueCards.value &&
    !startingSession.value
  )
})

const errorMessage = computed(() => deckError.value)

function unwrapListResponse(response) {
  const payload = response?.data ?? response
  if (Array.isArray(payload)) return payload
  if (Array.isArray(payload?.data)) return payload.data
  return []
}

function normalizeTargetWord(card) {
  return {
    id: card.id,
    surface: card.surface || card.frontText || card.front_text || '',
    reading: card.reading || card.frontReading || card.front_reading || '',
    meaning: card.meaning || card.backText || card.back_text || '',
  }
}

async function loadDecks() {
  loadingDecks.value = true
  deckError.value = ''

  try {
    const response = await tutorService.getDecks()
    decks.value = unwrapListResponse(response)
  } catch (error) {
    console.error('[TutorSetupView] loadDecks failed:', error)
    deckError.value = 'Không thể tải danh sách bộ thẻ. Vui lòng thử lại.'
    decks.value = []
  } finally {
    loadingDecks.value = false
  }
}

async function loadDueCards(deckId) {
  if (!deckId) {
    dueCards.value = []
    excludedTargetWordIds.value = []
    return
  }

  loadingDueCards.value = true
  deckError.value = ''

  try {
    const response = await tutorService.getDeckDue(deckId, 10)
    dueCards.value = unwrapListResponse(response)
    excludedTargetWordIds.value = []
  } catch (error) {
    console.error('[TutorSetupView] loadDueCards failed:', error)
    deckError.value = 'Không thể tải các thẻ đến hạn của deck đã chọn.'
    dueCards.value = []
    excludedTargetWordIds.value = []
  } finally {
    loadingDueCards.value = false
  }
}

async function selectDeck(deck) {
  if (!deck || deck.id === selectedDeckId.value) return
  selectedDeckId.value = deck.id
  tutorStore.selectedDeck = deck
  await loadDueCards(deck.id)
}

function removeTargetWord(cardId) {
  if (excludedTargetWordIds.value.includes(cardId)) return
  excludedTargetWordIds.value = [...excludedTargetWordIds.value, cardId]
}

async function retryLoad() {
  await loadDecks()
  if (selectedDeckId.value) {
    await loadDueCards(selectedDeckId.value)
  }
}

async function startConversation() {
  if (!canStartSession.value) return

  startingSession.value = true
  startError.value = ''

  try {
    const payload = {
      deckId: selectedDeckId.value,
      scenarioName: selectedScenarioName.value === 'auto' ? null : selectedScenarioName.value,
      level: selectedLevel.value,
      durationMinutes: durationMinutes.value,
      targetWords: previewTargetWords.value.map(normalizeTargetWord),
    }

    tutorStore.selectedDeck = selectedDeck.value
    const session = await tutorStore.startSession(payload)
    const sessionId = session?.sessionId || tutorStore.sessionId

    if (!sessionId) {
      throw new Error('Không tạo được phiên luyện hội thoại')
    }

    await router.push({ path: '/tutor/chat', query: { sessionId } })
  } catch (error) {
    console.error('startConversation failed', error)
    startError.value = 'Không thể bắt đầu hội thoại. Vui lòng thử lại.'
  } finally {
    startingSession.value = false
  }
}

onMounted(async () => {
  try {
    tutorStore.hydrate()
  } catch (error) {
    console.warn('[TutorSetupView] hydrate failed:', error)
  }

  selectedScenarioName.value = scenarioOptions.some(option => option.value === tutorStore.scenario)
    ? tutorStore.scenario
    : 'restaurant'
  selectedLevel.value = jlptLevels.includes(tutorStore.level) ? tutorStore.level : 'N3'
  durationMinutes.value = durationOptions.includes(tutorStore.durationMinutes) ? tutorStore.durationMinutes : 15

  await loadDecks()

  if (tutorStore.selectedDeck?.id) {
    selectedDeckId.value = tutorStore.selectedDeck.id
    await loadDueCards(selectedDeckId.value)
    return
  }

  if (decks.value.length === 1) {
    selectedDeckId.value = decks.value[0].id
    tutorStore.selectedDeck = decks.value[0]
    await loadDueCards(selectedDeckId.value)
  }
})
</script>
