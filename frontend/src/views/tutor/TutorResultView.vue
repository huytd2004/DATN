<template>
  <div class="w-full">
    <!-- Loading State -->
    <div v-if="loading" class="flex flex-col items-center justify-center py-32 gap-4">
      <div class="w-12 h-12 rounded-full border-4 border-primary/20 border-t-primary animate-spin"></div>
      <p class="text-on-surface-variant text-sm">Đang tải kết quả phiên học...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="flex flex-col items-center justify-center py-32 gap-4 text-center">
      <span class="material-symbols-outlined text-error text-5xl">error_outline</span>
      <p class="text-on-surface-variant">{{ error }}</p>
      <RouterLink to="/tutor" class="mt-4 px-6 py-3 bg-primary text-on-primary rounded-full text-sm font-semibold">
        Quay lại
      </RouterLink>
    </div>

    <template v-else-if="result">
      <!-- ── Hero: Header + Stats grid ───────────────────────────────────── -->
      <section class="mb-16 flex flex-col md:flex-row items-center gap-12">
        <div class="w-full md:w-1/2">
          <h1 class="text-display-lg font-bold text-on-primary-fixed mb-2 tracking-tight leading-tight" style="font-size:3.5rem;">完了</h1>
          <h2 class="text-3xl font-headline font-semibold text-primary mb-4">Phiên học hoàn tất</h2>
          <p class="text-on-surface-variant text-base leading-relaxed max-w-md">
            Bạn đã hoàn thành phiên luyện tập hội thoại tiếng Nhật.
            Dưới đây là phân tích chi tiết về phiên học của bạn.
          </p>

          <div class="mt-8 flex gap-4 flex-wrap">
            <RouterLink to="/tutor"
              class="bg-gradient-to-r from-primary to-primary-dim text-on-primary px-8 py-4 rounded-full font-semibold hover:opacity-90 transition-all flex items-center gap-2 shadow-xl shadow-primary/10">
              Học tiếp
              <span class="material-symbols-outlined">arrow_forward</span>
            </RouterLink>
            <RouterLink to="/dashboard"
              class="text-on-surface border border-outline-variant/30 px-8 py-4 rounded-full font-medium hover:bg-surface-container-low transition-all">
              Dashboard
            </RouterLink>
          </div>
        </div>

        <!-- Bento Stats -->
        <div class="w-full md:w-1/2 grid grid-cols-2 gap-4">
          <!-- Duration -->
          <div class="bg-surface-container-lowest p-8 rounded-[2rem] flex flex-col items-center justify-center text-center shadow-sm border border-outline-variant/10">
            <span class="material-symbols-outlined text-primary text-3xl mb-2" style="font-variation-settings:'FILL' 1;">schedule</span>
            <span class="text-4xl font-headline font-bold text-primary mb-1">{{ result.durationMinutes ?? '—' }}</span>
            <span class="text-xs uppercase tracking-widest text-on-surface-variant font-medium">Phút luyện tập</span>
          </div>

          <!-- Turns -->
          <div class="bg-primary-container p-8 rounded-[2rem] flex flex-col items-center justify-center text-center shadow-sm">
            <span class="material-symbols-outlined text-on-primary-container text-3xl mb-2" style="font-variation-settings:'FILL' 1;">forum</span>
            <span class="text-4xl font-headline font-bold text-on-primary-container mb-1">{{ result.userTurns ?? '—' }}</span>
            <span class="text-xs uppercase tracking-widest text-on-primary-container/70 font-medium">Lượt phản hồi</span>
          </div>

          <!-- Stats row: real metrics only -->
          <div class="bg-surface-container-low col-span-2 p-8 rounded-[2rem] flex items-center justify-around border border-outline-variant/10">
            <div class="text-center">
              <div class="text-2xl font-headline font-bold mb-1"
                :class="corrections.length > 0 ? 'text-error' : 'text-primary'">
                {{ corrections.length }}
              </div>
              <div class="text-[10px] uppercase tracking-[0.2em] text-on-surface-variant">Lỗi sai</div>
            </div>
            <div class="w-px h-12 bg-outline-variant/20"></div>
            <div class="text-center">
              <div class="text-2xl font-headline font-bold text-secondary mb-1">
                {{ newVocabulary.length }}
              </div>
              <div class="text-[10px] uppercase tracking-[0.2em] text-on-surface-variant">Từ mới</div>
            </div>
            <div class="w-px h-12 bg-outline-variant/20"></div>
            <div class="text-center">
              <div class="text-2xl font-headline font-bold text-on-surface mb-1">
                {{ result.assistantTurns ?? '—' }}
              </div>
              <div class="text-[10px] uppercase tracking-[0.2em] text-on-surface-variant">Lượt AI</div>
            </div>
          </div>
        </div>
      </section>

      <!-- ── Insights Grid ───────────────────────────────────────────────── -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <!-- Left: Corrections + Vocabulary -->
        <div class="lg:col-span-2 space-y-10">

          <!-- Grammar Corrections -->
          <div>
            <h3 class="text-xl font-headline font-semibold px-2 flex items-center gap-3 mb-6">
              <span class="material-symbols-outlined text-error" style="font-variation-settings:'FILL' 1;">auto_fix_high</span>
              Phân tích lỗi sai ({{ corrections.length }} lỗi)
            </h3>

            <div v-if="corrections.length === 0"
              class="bg-surface-container-lowest p-8 rounded-[1.5rem] flex items-center gap-4 border border-outline-variant/10">
              <span class="material-symbols-outlined text-primary text-4xl" style="font-variation-settings:'FILL' 1;">check_circle</span>
              <div>
                <p class="font-semibold text-on-surface">Xuất sắc! Không có lỗi nào được ghi nhận.</p>
                <p class="text-sm text-on-surface-variant mt-1">Bạn đã sử dụng tiếng Nhật rất chính xác trong phiên này.</p>
              </div>
            </div>

            <div v-for="(c, i) in corrections" :key="i"
              class="bg-surface-container-lowest p-6 rounded-[1.5rem] border-l-4 border-error/40 shadow-sm mb-4 hover:bg-white transition-all">
              <div class="flex items-center gap-2 mb-4">
                <span class="text-xs font-bold uppercase tracking-widest bg-error-container text-on-error-container px-3 py-1 rounded-full">
                  Lỗi {{ i + 1 }}
                </span>
              </div>
              <div class="space-y-3">
                <div class="flex gap-3 items-start">
                  <span class="material-symbols-outlined text-error mt-0.5 flex-shrink-0">close</span>
                  <p class="text-on-surface-variant italic">「{{ c.original }}」</p>
                </div>
                <div class="flex gap-3 items-start">
                  <span class="material-symbols-outlined text-primary mt-0.5 flex-shrink-0">check_circle</span>
                  <p class="text-on-surface font-semibold">「{{ c.corrected }}」</p>
                </div>
              </div>
              <div v-if="c.note || c.explanation" class="mt-4 pt-4 border-t border-outline-variant/10">
                <p class="text-sm text-on-surface-variant leading-relaxed">
                  <strong class="text-on-surface">Giải thích: </strong>{{ c.note || c.explanation }}
                </p>
              </div>
            </div>
          </div>

          <!-- New Vocabulary -->
          <div>
            <h3 class="text-xl font-headline font-semibold px-2 flex items-center gap-3 mb-6">
              <span class="material-symbols-outlined text-secondary" style="font-variation-settings:'FILL' 1;">auto_stories</span>
              Từ vựng mới trong phiên ({{ newVocabulary.length }} từ)
            </h3>

            <div v-if="newVocabulary.length === 0"
              class="bg-surface-container-lowest p-6 rounded-[1.5rem] border border-outline-variant/10 text-on-surface-variant text-sm">
              Không có từ vựng mới nào được ghi nhận trong phiên này.
            </div>

            <div v-else class="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div v-for="(v, i) in newVocabulary" :key="i"
                class="bg-surface-container-lowest p-5 rounded-2xl border border-outline-variant/10 hover:border-secondary/30 transition-all shadow-sm">
                <div class="flex items-baseline gap-2 mb-1">
                  <span class="text-2xl font-bold text-on-surface font-headline">{{ v.surface }}</span>
                  <span class="text-xs text-on-surface-variant">{{ v.reading }}</span>
                </div>
                <p class="text-sm text-secondary font-medium">{{ v.meaning }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Right: Sidebar -->
        <div class="space-y-6">
          <!-- Motivational card -->
          <div class="relative overflow-hidden rounded-[2rem] bg-on-primary-fixed p-8 text-on-primary min-h-[220px] flex flex-col justify-end shadow-lg shadow-primary-fixed/20">
            <div class="relative z-10">
              <span class="material-symbols-outlined text-primary-fixed mb-3 text-4xl" style="font-variation-settings:'FILL' 1;">self_improvement</span>
              <h4 class="text-2xl font-headline font-bold mb-2">Hành trình ngàn dặm</h4>
              <p class="text-primary-fixed/80 text-sm leading-relaxed">
                Bắt đầu từ một bước chân duy nhất. Hôm nay bạn đã tiến xa hơn hôm qua một bước.
              </p>
            </div>
          </div>

          <!-- Session summary -->
          <div class="bg-surface-container-high p-6 rounded-[2rem] border border-outline-variant/10">
            <h4 class="text-on-surface font-semibold mb-5 flex items-center gap-2">
              <span class="material-symbols-outlined text-primary">summarize</span>
              Tóm tắt phiên học
            </h4>
            <div class="space-y-4 text-sm">
              <div class="flex justify-between items-center">
                <span class="text-on-surface-variant">Thời lượng</span>
                <span class="font-semibold text-on-surface">{{ result.durationMinutes ?? 0 }} phút</span>
              </div>
              <div class="flex justify-between items-center">
                <span class="text-on-surface-variant">Lượt bạn nói</span>
                <span class="font-semibold text-on-surface">{{ result.userTurns ?? 0 }} lượt</span>
              </div>
              <div class="flex justify-between items-center">
                <span class="text-on-surface-variant">Lượt AI phản hồi</span>
                <span class="font-semibold text-on-surface">{{ result.assistantTurns ?? 0 }} lượt</span>
              </div>
              <div class="flex justify-between items-center">
                <span class="text-on-surface-variant">Số lỗi sai</span>
                <span class="font-semibold" :class="corrections.length > 0 ? 'text-error' : 'text-primary'">
                  {{ corrections.length }} lỗi
                </span>
              </div>
              <div class="flex justify-between items-center">
                <span class="text-on-surface-variant">Từ vựng mới</span>
                <span class="font-semibold text-secondary">{{ newVocabulary.length }} từ</span>
              </div>
            </div>
          </div>

          <!-- Summary text if available -->
          <div v-if="result.summary" class="bg-surface-container-lowest p-6 rounded-[2rem] border border-outline-variant/10">
            <h4 class="text-on-surface font-semibold mb-3 flex items-center gap-2 text-sm">
              <span class="material-symbols-outlined text-tertiary text-base">notes</span>
              Nhận xét
            </h4>
            <p class="text-sm text-on-surface-variant leading-relaxed">{{ result.summary }}</p>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import { useTutorStore } from '@/stores/tutor'

const route = useRoute()
const store = useTutorStore()

const loading = ref(true)
const error = ref(null)

const result = computed(() => store.result)

const corrections = computed(() => {
  const raw = result.value?.corrections
  if (!Array.isArray(raw)) return []
  return raw.filter(c => c && (c.original || c.corrected))
})

const newVocabulary = computed(() => {
  const raw = result.value?.newVocabulary
  if (!Array.isArray(raw)) return []
  return raw.filter(v => v && v.surface)
})

onMounted(async () => {
  try {
    const sessionId = route.query.sessionId || store.sessionId
    if (!sessionId) {
      error.value = 'Không tìm thấy thông tin phiên học.'
      return
    }
    await store.loadResult(sessionId)
    if (!store.result) {
      error.value = 'Không có kết quả cho phiên học này.'
    }
  } catch (e) {
    console.error(e)
    error.value = 'Không thể tải kết quả. Vui lòng thử lại.'
  } finally {
    loading.value = false
  }
})
</script>
