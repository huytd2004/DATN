<template>
  <div class="fixed inset-0 z-[100] bg-background font-body text-on-surface antialiased overflow-hidden flex flex-col">
    <header class="h-16 bg-white/80 backdrop-blur-xl z-50 flex items-center justify-between px-4 md:px-8 shrink-0 shadow-[0_4px_20px_-10px_rgba(0,0,0,0.05)]">
      <div class="flex items-center gap-4">
        <RouterLink :to="backLink" class="p-2 hover:bg-surface-container-low rounded-full transition-colors flex items-center justify-center">
          <span class="material-symbols-outlined text-on-surface-variant">close</span>
        </RouterLink>
        <div class="flex flex-col">
          <span class="font-headline font-bold text-primary tracking-tight">ZenCards</span>
          <span class="text-[10px] uppercase tracking-widest text-on-surface-variant/70 font-semibold">Study Session</span>
        </div>
      </div>

      <div class="flex-1 max-w-md mx-4 md:mx-12 hidden sm:block">
        <div class="flex justify-between items-center mb-1.5 px-1">
          <span class="text-[11px] font-bold text-on-primary-fixed-variant">Đã học {{ completedCards }} thẻ / {{ targetCards }} thẻ cần học</span>
          <span class="text-[11px] font-bold text-primary italic">{{ progressPercent }}% complete</span>
        </div>
        <div class="h-1.5 w-full bg-surface-container-high rounded-full overflow-hidden">
          <div class="h-full bg-gradient-to-r from-primary to-primary-container rounded-full transition-all duration-500" :style="{ width: `${progressPercent}%` }"></div>
        </div>
      </div>

      <div class="flex items-center gap-3">
        <span class="material-symbols-outlined text-secondary" style="font-variation-settings: 'FILL' 1;">local_fire_department</span>
        <span class="font-headline font-extrabold text-on-surface">{{ fixedStreakDays }}</span>
      </div>
    </header>

    <main class="flex-1 w-full flex flex-col items-center justify-center p-4 md:p-6 relative">
      <div v-if="loading" class="text-center text-on-surface-variant">Đang tải phiên học...</div>

      <div v-else-if="!targetCards" class="w-full max-w-2xl bg-surface-container-lowest rounded-[2rem] p-8 md:p-10 shadow-xl text-center">
        <h1 class="text-3xl font-bold text-on-surface mb-3">Chưa có thẻ để học</h1>
        <p class="text-on-surface-variant mb-6">Bộ thẻ này hiện chưa có flashcard phù hợp với phiên học.</p>
        <RouterLink :to="backLink" class="inline-flex items-center justify-center px-6 py-3 rounded-full bg-primary text-on-primary font-semibold hover:brightness-110 transition">
          Quay lại bộ thẻ
        </RouterLink>
      </div>

      <div v-else-if="sessionFinished" class="w-full max-w-2xl bg-surface-container-lowest rounded-[2rem] p-8 md:p-10 shadow-xl text-center">
        <h1 class="text-3xl font-bold text-on-surface mb-3">Hoàn thành phiên học</h1>
        <p class="text-on-surface-variant mb-6">Bạn đã học {{ completedCards }} thẻ trong phiên này.</p>
        <RouterLink :to="backLink" class="inline-flex items-center justify-center px-6 py-3 rounded-full bg-primary text-on-primary font-semibold hover:brightness-110 transition">
          Quay lại bộ thẻ
        </RouterLink>
      </div>

      <template v-else>
        <div class="absolute inset-0 -z-10 pointer-events-none opacity-40 overflow-hidden">
          <div class="absolute top-1/4 -left-20 w-96 h-96 bg-primary-container rounded-full blur-[100px]"></div>
          <div class="absolute bottom-1/4 -right-20 w-96 h-96 bg-secondary-container rounded-full blur-[100px]"></div>
        </div>

        <div class="relative w-full max-w-2xl" style="perspective: 1000px;" @click="toggleFlip">
          <div class="absolute -inset-4 bg-on-surface/5 blur-3xl rounded-[2.5rem] pointer-events-none transition-all duration-700"></div>

          <div class="relative w-full aspect-[4/3] sm:aspect-[1.6/1] cursor-pointer transition-transform duration-700" style="transform-style: preserve-3d;" :style="{ transform: isFlipped ? 'rotateY(180deg)' : 'rotateY(0deg)' }">
            <div class="absolute inset-0 bg-surface-container-lowest rounded-[2rem] shadow-xl shadow-on-surface/5 border border-outline-variant/10 flex flex-col items-center justify-center p-12" style="backface-visibility: hidden;">
              <div class="absolute top-8 left-10 text-on-surface-variant/30 text-xs font-bold tracking-[0.2em] uppercase">{{ deck?.name ?? 'Study Session' }}</div>
              <div class="text-center">
                <h1 class="font-headline text-[5rem] md:text-[9rem] font-medium leading-none text-on-primary-fixed mb-4">{{ currentCard?.frontText }}</h1>
                <p class="text-on-surface-variant font-medium tracking-wide flex items-center justify-center gap-2">
                  {{ currentCard?.frontReading || '' }}
                  <span class="material-symbols-outlined text-sm">volume_up</span>
                </p>
              </div>
              <div class="absolute bottom-8 text-on-surface-variant/40 text-[11px] font-medium animate-pulse">Chạm thẻ để xem đáp án</div>
            </div>

            <div class="absolute inset-0 bg-white/90 backdrop-blur-md rounded-[2rem] shadow-xl shadow-on-surface/5 border border-outline-variant/10 flex flex-col items-center justify-center p-8 md:p-12 pointer-events-none" style="backface-visibility: hidden; transform: rotateY(180deg);">
              <div class="w-full text-left space-y-8">
                <div>
                  <span class="text-secondary font-bold text-xs uppercase tracking-widest block mb-1">Nghĩa</span>
                  <h2 class="font-headline text-3xl font-bold text-on-surface">{{ currentCard?.backText }}</h2>
                </div>
                <div>
                  <span class="text-on-surface-variant/60 font-bold text-[10px] uppercase tracking-widest block mb-2">Ghi chú</span>
                  <p class="text-sm leading-relaxed text-on-surface-variant">{{ currentCard?.backNotes || 'Không có ghi chú.' }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="mt-8 md:mt-16 w-full max-w-2xl transition-opacity duration-300" :class="isFlipped ? 'opacity-100 pointer-events-auto' : 'opacity-30 pointer-events-none'">
          <div class="grid grid-cols-2 sm:grid-cols-4 gap-4">
            <button @click.stop="reviewCard('again')" :disabled="reviewing" class="group flex flex-col items-center gap-1 p-4 bg-surface-container rounded-2xl transition-all duration-300 hover:bg-error-container/20 active:scale-95 disabled:opacity-60">
              <span class="text-xs font-bold text-on-surface-variant group-hover:text-error transition-colors">Lại</span>
              <span class="text-[10px] text-on-surface-variant/60">{{ formatInterval(nextIntervalAgain) }}</span>
            </button>
            <button @click.stop="reviewCard('hard')" :disabled="reviewing" class="group flex flex-col items-center gap-1 p-4 bg-surface-container rounded-2xl transition-all duration-300 hover:bg-secondary-container/30 active:scale-95 disabled:opacity-60">
              <span class="text-xs font-bold text-on-surface-variant group-hover:text-secondary transition-colors">Khó</span>
              <span class="text-[10px] text-on-surface-variant/60">{{ formatInterval(nextIntervalHard) }}</span>
            </button>
            <button @click.stop="reviewCard('good')" :disabled="reviewing" class="group flex flex-col items-center gap-1 p-4 bg-primary rounded-2xl transition-all duration-300 shadow-lg shadow-primary/10 hover:shadow-primary/20 hover:scale-105 active:scale-95 disabled:opacity-60">
              <span class="text-xs font-bold text-on-primary">Được</span>
              <span class="text-[10px] text-on-primary/70">{{ formatInterval(nextIntervalGood) }}</span>
            </button>
            <button @click.stop="reviewCard('easy')" :disabled="reviewing" class="group flex flex-col items-center gap-1 p-4 bg-surface-container rounded-2xl transition-all duration-300 hover:bg-primary-container/30 active:scale-95 disabled:opacity-60">
              <span class="text-xs font-bold text-on-surface-variant group-hover:text-primary transition-colors">Dễ</span>
              <span class="text-[10px] text-on-surface-variant/60">{{ formatInterval(nextIntervalEasy) }}</span>
            </button>
          </div>

          <div class="flex justify-center mt-6 md:mt-8 gap-4 md:gap-8">
            <button class="flex items-center gap-2 text-[11px] font-bold text-on-surface-variant/40 hover:text-on-surface transition-colors">
              <span class="material-symbols-outlined text-lg">edit</span> <span class="hidden sm:inline">Chỉnh sửa thẻ</span>
            </button>
            <button class="flex items-center gap-2 text-[11px] font-bold text-on-surface-variant/40 hover:text-on-surface transition-colors">
              <span class="material-symbols-outlined text-lg">flag</span> <span class="hidden sm:inline">Gắn cờ</span>
            </button>
            <button class="flex items-center gap-2 text-[11px] font-bold text-on-surface-variant/40 hover:text-on-surface transition-colors">
              <span class="material-symbols-outlined text-lg">keyboard</span> <span class="hidden sm:inline">Phím tắt</span>
            </button>
          </div>
        </div>
      </template>
    </main>

    <div class="fixed bottom-12 left-12 w-48 h-48 opacity-10 -rotate-12 pointer-events-none hidden lg:block">
      <img class="w-full h-full object-cover rounded-3xl" src="https://lh3.googleusercontent.com/aida-public/AB6AXuCnpTj5CS2o9QcViaUOWsPGYbKBJzj4m-EyVzSrxxjgtHMaFFIX-GtH5qK0lA-D4nXtD7gmMp6V-T6aSxOEEp1hXdVNX3aMJ-bWVDxml3gdjr_54cabSpeh8WF9bq9mKCFHZ_Fx3nCPN_ggSIUIGlKp1eXSz6nh7GxZNfE-vBxN9tqpwXlptVtksiEBOU3t8YKT5Zj8Y_iWssx6Zj7oS54cevG_KlsmtmfnUxl0OqTb9ckKIVMa7Xg8gcNBwhJKQfLnl5E21B_ZItc"/>
    </div>
    <div class="fixed top-24 right-12 w-32 h-32 opacity-10 rotate-6 pointer-events-none hidden lg:block">
      <img class="w-full h-full object-cover rounded-3xl" src="https://lh3.googleusercontent.com/aida-public/AB6AXuB6s90zERtfaRoM3CC66raRt25kGkeEMD4oWF6voDNfP3SkkrfGlxHEXZiyLaskUP35K19imRnHkcQLvfBTeMs7Si_gqcSv95O6IdUwKdtOSkPuZxU8zPf-kriTvUFUPf3cCht_SW1e6Vb7mF57wbxzvEZ4wMyopKt6-JeQDC_JtPZNr-T_WICsRnPTH-idiaEfTZf3oSyXJ9iPC5plA65IIK4QdtDGJ6U1tLEAkdFeAFjHTTZX2rArk3kDHcP2MIHP8SgZLK3y5v8"/>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/services/api'

const route = useRoute()
const deckId = computed(() => route.params.id)
const isFlipped = ref(false)
const loading = ref(true)
const reviewing = ref(false)
const deck = ref(null)
const sessionQueue = ref([])
const currentIndex = ref(0)
const completedCards = ref(0)
const fixedStreakDays = 14

const backLink = computed(() => `/flashcards/${deckId.value}`)

const newWordsPerDay = computed(() => {
  const value = Number(route.query.newWordsPerDay)
  return Number.isFinite(value) && value > 0 ? value : 20
})

const targetCards = computed(() => sessionQueue.value.length)
const progressPercent = computed(() => {
  if (targetCards.value === 0) return 0
  return Math.round((completedCards.value * 100) / targetCards.value)
})
const currentCard = computed(() => sessionQueue.value[currentIndex.value] ?? null)
const sessionFinished = computed(() => !loading.value && targetCards.value > 0 && currentIndex.value >= targetCards.value)

// SM2 interval calculation
function calculateSM2Interval(quality) {
  if (!currentCard.value?.srsInfo) return 1
  const srs = currentCard.value.srsInfo
  const ef = srs.easeFactor ?? 2.5
  const n = srs.repetitions ?? 0
  const interval = srs.intervalDays ?? 0

  if (quality < 3) {
    // Reset: failed answer
    return 1
  } else {
    // Success: calculate next interval
    if (n === 0) return 1
    if (n === 1) return 6
    return Math.round(interval * ef)
  }
}

const nextIntervalAgain = computed(() => calculateSM2Interval(0)) // q = 0 (reset)
const nextIntervalHard = computed(() => calculateSM2Interval(2)) // q = 2 (reset)
const nextIntervalGood = computed(() => calculateSM2Interval(3)) // q = 3 (success, smallest)
const nextIntervalEasy = computed(() => calculateSM2Interval(5)) // q = 5 (success, largest)

function formatInterval(days) {
  if (days < 1) return '< 1 ngày'
  if (days === 1) return '1 ngày'
  if (days <= 7) return `${days} ngày`
  if (days <= 30) return `${Math.round(days / 7)} tuần`
  return `${Math.round(days / 30)} tháng`
}

function toggleFlip() {
  if (!currentCard.value) return
  isFlipped.value = !isFlipped.value
}

async function reviewCard(rating) {
  if (!currentCard.value || reviewing.value) return

  reviewing.value = true
  try {
    await api.patch(`/api/v1/flashcards/${currentCard.value.id}/review`, { rating })
    completedCards.value += 1
    currentIndex.value += 1
    isFlipped.value = false
  } catch (error) {
    console.error('Error reviewing flashcard:', error)
  } finally {
    reviewing.value = false
  }
}

onMounted(async () => {
  try {
    loading.value = true

    const deckResponse = await api.get(`/api/v1/decks/${deckId.value}`)
    deck.value = deckResponse.data

    const dueResponse = await api.get(`/api/v1/decks/${deckId.value}/due`, {
      params: { maxNew: newWordsPerDay.value }
    })

    sessionQueue.value = dueResponse.data || []
  } catch (error) {
    console.error('Error loading study session:', error)
  } finally {
    loading.value = false
  }
})
</script>
