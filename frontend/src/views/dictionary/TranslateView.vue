<template>
  <div class="w-full pt-6">
    <!-- Header / Title -->
    <div class="flex flex-col mb-8 px-2">
      <h1 class="text-3xl font-headline font-bold text-on-primary-fixed mb-2">Dịch thuật & Phân tích</h1>
      <p class="text-on-surface-variant">Dịch câu tiếng Nhật và phân tích cấu trúc ngữ pháp, từ vựng chi tiết bằng AI.</p>
    </div>

    <div class="space-y-10">
      <!-- Translation Interface -->
      <section class="grid grid-cols-1 lg:grid-cols-2 gap-8 items-stretch">
        <!-- Source Input -->
        <div class="space-y-4 h-full flex flex-col">
          <div class="flex items-center justify-between px-2">
            <label class="text-xs font-bold uppercase tracking-widest text-on-surface-variant">Văn bản nguồn (Tiếng Nhật)</label>
            <div class="flex items-center gap-2 text-primary cursor-pointer">
              <span class="text-xs font-medium">Tự động phát hiện</span>
              <span class="material-symbols-outlined text-sm">expand_more</span>
            </div>
          </div>
          <div class="bg-surface-container-lowest rounded-[2rem] p-8 shadow-sm transition-shadow hover:shadow-md border border-outline-variant/10 flex-1 flex flex-col min-h-[360px]">
            <textarea
              v-model="sourceText"
              class="w-full flex-1 bg-transparent border-none focus:ring-0 text-xl md:text-2xl font-medium leading-relaxed resize-none min-h-0 placeholder:text-outline/50 outline-none"
              placeholder="Nhập tiếng Nhật tại đây..."
            ></textarea>
            <p v-if="quickError" class="text-sm text-error mt-3">{{ quickError }}</p>
            <div class="flex justify-end pt-4 border-t border-outline-variant/10 mt-2">
              <button
                class="bg-primary text-on-primary px-8 py-3 mt-4 rounded-full font-bold flex items-center gap-2 hover:opacity-90 transition-all hover:scale-[1.02] shadow-sm disabled:opacity-60 disabled:cursor-not-allowed"
                :disabled="isTranslating"
                @click="translateNow"
              >
                <span class="material-symbols-outlined">translate</span>
                {{ isTranslating ? 'Đang dịch...' : 'Dịch ngay' }}
              </button>
            </div>
          </div>
        </div>

        <!-- Result Output -->
        <div class="space-y-4 h-full flex flex-col">
          <div class="flex items-center justify-between px-2">
            <label class="text-xs font-bold uppercase tracking-widest text-on-surface-variant">Bản dịch nhanh (Tiếng Việt)</label>
            <button
              class="material-symbols-outlined text-on-surface-variant hover:text-primary transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
              :disabled="!translatedText"
              @click="copyTranslation"
            >content_copy</button>
          </div>
          <div class="bg-surface-container-low rounded-[2rem] p-8 flex-1 min-h-[360px] flex flex-col justify-between border border-transparent">
            <p v-if="translatedText" class="flex-1 text-xl md:text-2xl font-medium leading-relaxed text-on-surface italic">
              {{ translatedText }}
            </p>
            <p v-else class="flex-1 text-lg md:text-xl font-medium leading-relaxed text-on-surface-variant italic">
              Bản dịch sẽ hiển thị ở đây sau khi bạn nhấn Dịch ngay.
            </p>
            <div class="flex items-center justify-between pt-6 mt-4 border-t border-outline-variant/10">
              <div class="flex items-center gap-4 mt-2">
                <span class="material-symbols-outlined p-2 rounded-full bg-surface-container-highest cursor-pointer hover:bg-outline-variant/30 transition-colors">volume_up</span>
                <span class="material-symbols-outlined p-2 rounded-full bg-surface-container-highest cursor-pointer hover:bg-outline-variant/30 transition-colors">share</span>
              </div>
              <button
                class="mt-2 inline-flex items-center gap-2 rounded-full px-4 py-2 text-xs font-bold transition-all disabled:opacity-60 disabled:cursor-not-allowed"
                :class="isDeepAnalysisEnabled ? 'bg-primary text-on-primary' : 'bg-surface-container-highest text-on-surface-variant hover:bg-outline-variant/20'"
                :disabled="isAnalyzing"
                @click="toggleDeepAnalysis"
              >
                <span class="material-symbols-outlined text-sm">auto_awesome</span>
                {{ isAnalyzing ? 'Đang phân tích...' : isDeepAnalysisEnabled ? 'Tắt phân tích chuyên sâu' : 'Phân tích chuyên sâu' }}
              </button>
            </div>
          </div>
        </div>
      </section>

      <!-- Deep Analysis Feature (Active State) -->
      <section class="space-y-8 border-t border-surface-container pt-12">
        <div class="flex flex-col gap-2">
          <h2 class="text-3xl font-bold text-on-primary-fixed leading-tight font-headline">Phân tích chuyên sâu</h2>
          <p class="text-on-surface-variant max-w-2xl">Cấu trúc ngữ pháp và từ vựng chi tiết dựa trên ngữ cảnh thực tế của câu.</p>
          <p v-if="analysisError" class="text-sm text-error">{{ analysisError }}</p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-12 gap-8">
          <!-- Advanced Translation Card -->
          <div class="md:col-span-4 bg-surface-container-lowest rounded-[2rem] p-8 space-y-6 shadow-sm border border-outline-variant/10 h-fit">
            <div class="flex items-center gap-3 text-secondary">
              <span class="material-symbols-outlined">auto_awesome</span>
              <h3 class="font-bold">{{ deepAnalysis ? 'Bản dịch nâng cao' : 'Chờ phân tích' }}</h3>
            </div>
            <p class="text-sm leading-relaxed text-on-surface-variant">
              {{ analysisSummary }}
            </p>
            <div class="bg-secondary-container/20 p-4 rounded-xl">
              <p class="text-xs font-bold text-secondary uppercase tracking-tighter mb-2">Ghi chú ngữ cảnh</p>
              <p class="text-xs text-on-secondary-container">{{ analysisContext }}</p>
            </div>
          </div>

          <!-- Highlighted Breakdown -->
          <div class="md:col-span-8 bg-surface-container-low rounded-[3rem] p-10 relative overflow-hidden">
            <div class="absolute top-0 right-0 w-64 h-64 bg-primary/5 rounded-full blur-3xl -translate-y-1/2 translate-x-1/2"></div>
            <div class="relative space-y-8">
              <div class="flex flex-wrap items-end gap-x-2 gap-y-6 text-3xl md:text-4xl font-light">
                <span v-if="!analysisTokens.length" class="text-on-surface-variant text-2xl md:text-3xl">Bật phân tích chuyên sâu để xem các token được chấm điểm từ graph.</span>
                <template v-else>
                  <div
                    v-for="item in analysisTokens"
                    :key="item.surface"
                    class="group relative px-2 py-1 bg-primary-container/40 rounded-lg cursor-default transition-all hover:bg-primary-container"
                  >
                    <span class="text-on-primary-container font-medium">{{ item.surface }}</span>
                    <div class="absolute -top-10 left-1/2 -translate-x-1/2 bg-on-surface text-surface text-[10px] px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap z-10 pointer-events-none">
                      {{ item.glossVi || '—' }}
                    </div>
                  </div>
                </template>
              </div>

              <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-6">
                <div
                  v-for="item in analysisCards"
                  :key="item.surface"
                  class="bg-surface-container-lowest p-5 rounded-2xl flex items-center justify-between group hover:scale-[1.02] transition-transform shadow-sm border border-transparent hover:border-outline-variant/10"
                >
                  <div class="space-y-1">
                    <div class="flex items-center gap-2">
                      <span class="text-lg font-bold text-on-surface">{{ item.token }}</span>
                      <span class="text-[10px] text-on-surface-variant font-medium bg-surface-container px-2 py-0.5 rounded">{{ item.badge }}</span>
                    </div>
                    <p class="text-xs text-on-surface-variant italic">{{ item.detail }}</p>
                    <p class="text-[10px] text-on-surface-variant">{{ item.extra }}</p>
                  </div>
                  <button class="flex flex-col items-center gap-1 text-primary group-hover:text-primary-dim transition-colors">
                    <span class="material-symbols-outlined" style="font-variation-settings: 'FILL' 1;">style</span>
                    <span class="text-[9px] font-bold uppercase">Tạo thẻ</span>
                  </button>
                </div>

                <div
                  v-if="!analysisCards.length"
                  class="border-2 border-dashed border-outline-variant/30 rounded-2xl flex items-center justify-center p-5 cursor-pointer hover:border-primary/50 hover:bg-surface-container-highest/30 transition-colors sm:col-span-2"
                >
                  <div class="flex items-center gap-3 text-on-surface-variant">
                    <span class="material-symbols-outlined">add_circle</span>
                    <span class="text-sm font-medium">Bật phân tích để nhận gợi ý từ GraphRAG</span>
                  </div>
                </div>
              </div>

              <!-- Notes section -->
              <div v-if="analysisNotes.length" class="pt-4 space-y-2">
                <p class="text-xs font-bold uppercase tracking-widest text-on-surface-variant">Ghi chú dịch thuật</p>
                <div
                  v-for="(note, i) in analysisNotes"
                  :key="i"
                  class="flex items-start gap-2 bg-surface-container-lowest rounded-xl px-4 py-3"
                >
                  <span class="material-symbols-outlined text-sm text-secondary mt-0.5">info</span>
                  <div>
                    <span class="text-xs font-bold text-secondary">{{ note.token }}</span>
                    <span class="text-xs text-on-surface-variant ml-2">{{ note.content }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Learning Context Suggestions -->
      <section class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div class="bg-primary/5 p-8 rounded-[2rem] space-y-4 border border-primary/10 relative overflow-hidden">
          <div class="relative z-10">
            <span class="material-symbols-outlined text-primary text-3xl mb-2 block" style="font-variation-settings: 'FILL' 1;">lightbulb</span>
            <h4 class="font-bold text-on-primary-fixed text-lg">Mẹo ghi nhớ</h4>
            <p class="text-sm text-on-surface-variant leading-relaxed">Hãy liên tưởng Kanji '傘' (Kasa) với hình dáng một chiếc ô có cán bên dưới và mái che phía trên.</p>
          </div>
        </div>

        <div class="bg-surface-container-low p-8 rounded-[2rem] space-y-4">
          <span class="material-symbols-outlined text-on-surface-variant text-3xl mb-2 block">history</span>
          <h4 class="font-bold text-on-surface text-lg">Bản dịch gần đây</h4>
          <div class="space-y-2 mt-4">
            <p class="text-[10px] font-bold text-outline uppercase tracking-wider">Hôm qua</p>
            <p class="text-sm truncate text-on-surface-variant bg-surface-container-highest/50 px-3 py-2 rounded-lg">こんにちは、お元気ですか？</p>
          </div>
        </div>

        <div class="bg-surface-container-highest p-8 rounded-[2rem] flex flex-col justify-between">
          <div class="space-y-2">
            <h4 class="font-bold text-on-surface text-lg">Sẵn sàng học chưa?</h4>
            <p class="text-sm text-on-surface-variant">Bắt đầu bài kiểm tra dựa trên các từ vựng vừa dịch.</p>
          </div>
          <button class="mt-6 bg-on-surface text-surface py-3 rounded-full text-sm font-bold hover:opacity-90 transition-opacity flex items-center justify-center gap-2">
            <span class="material-symbols-outlined text-sm">school</span>
            Bắt đầu Test
          </button>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { translateService } from '@/services/translate'

const sourceText = ref('雨が降っているから、傘を持って行ったほうがいいですよ')
const translatedText = ref('')
const isTranslating = ref(false)
const quickError = ref('')
const isDeepAnalysisEnabled = ref(false)
const isAnalyzing = ref(false)
const analysisError = ref('')
const deepAnalysis = ref(null)

const analysisTokens = computed(() => deepAnalysis.value?.keyVocabulary?.slice(0, 4) ?? [])

const analysisCards = computed(() => {
  const vocab = deepAnalysis.value?.keyVocabulary ?? []
  return vocab.map((item) => ({
    token:    item.surface || 'Unknown',
    badge:    item.domain || item.register || 'Graph',
    detail:   item.glossVi || '—',
    extra:    [
      item.reading ? `読み: ${item.reading}` : null,
      item.jlpt    ? `JLPT N${item.jlpt}` : null,
      item.register,
    ].filter(Boolean).join(' · '),
    surface:  item.surface,
  }))
})

const analysisNotes = computed(() => deepAnalysis.value?.notes ?? [])

const topVocab = computed(() => analysisTokens.value[0] ?? null)

const analysisSummary = computed(() => {
  if (!isDeepAnalysisEnabled.value) {
    return 'Bật công tắc phân tích chuyên sâu để backend gọi langraph_pipeline và lấy evidence từ Neo4j.'
  }
  if (isAnalyzing.value) {
    return 'Đang truy vấn GraphRAG và dựng phân tích từ ngữ cảnh của câu...'
  }
  if (!deepAnalysis.value) {
    return 'Không lấy được evidence từ graph cho câu hiện tại.'
  }
  if (!topVocab.value) {
    return 'Bản dịch đã sẵn sàng nhưng chưa có từ vựng nào được highlight từ graph.'
  }
  const domains = (deepAnalysis.value.detectedDomains ?? []).join(', ') || 'general'
  return `Phát hiện domain: ${domains}. Token "${topVocab.value.surface}" → "${topVocab.value.glossVi || '?'}".`
})

const analysisContext = computed(() => {
  if (!isDeepAnalysisEnabled.value) return 'Chưa bật phân tích chuyên sâu.'
  if (isAnalyzing.value) return 'Đang lấy kết quả từ backend.'
  const kvCount = deepAnalysis.value?.keyVocabulary?.length ?? 0
  const noteCount = deepAnalysis.value?.notes?.length ?? 0
  const domains = (deepAnalysis.value?.detectedDomains ?? []).join(', ') || 'general'
  return `Domain: ${domains} · ${kvCount} từ vựng · ${noteCount} ghi chú`
})

async function translateNow() {
  if (!sourceText.value.trim()) {
    quickError.value = 'Vui lòng nhập câu tiếng Nhật cần dịch.'
    translatedText.value = ''
    return
  }

  isTranslating.value = true
  quickError.value = ''

  try {
    const res = await translateService.quick({
      text: sourceText.value,
      sourceLang: 'ja',
      targetLang: 'vi',
    })

    translatedText.value = res.data?.translatedText ?? ''
    if (!translatedText.value) {
      quickError.value = 'Không nhận được nội dung bản dịch.'
    }

    if (isDeepAnalysisEnabled.value) {
      await fetchDeepAnalysis()
    }
  } catch (error) {
    quickError.value = error?.response?.data?.message || 'Không thể dịch lúc này, vui lòng thử lại.'
    translatedText.value = ''
  } finally {
    isTranslating.value = false
  }
}

async function fetchDeepAnalysis() {
  if (!sourceText.value.trim()) {
    analysisError.value = 'Vui lòng nhập câu tiếng Nhật trước khi phân tích.'
    deepAnalysis.value = null
    return
  }

  isAnalyzing.value = true
  analysisError.value = ''

  try {
    const res = await translateService.deep({
      text: sourceText.value,
      sourceLang: 'ja',
      targetLang: 'vi',
    })

    deepAnalysis.value = res.data ?? null
    if (!deepAnalysis.value) {
      analysisError.value = 'Không nhận được dữ liệu phân tích chuyên sâu.'
    }
  } catch (error) {
    analysisError.value = error?.response?.data?.message || 'Không thể phân tích chuyên sâu lúc này, vui lòng thử lại.'
    deepAnalysis.value = null
  } finally {
    isAnalyzing.value = false
  }
}

async function toggleDeepAnalysis() {
  isDeepAnalysisEnabled.value = !isDeepAnalysisEnabled.value

  if (!isDeepAnalysisEnabled.value) {
    analysisError.value = ''
    deepAnalysis.value = null
    return
  }

  await fetchDeepAnalysis()
}

async function copyTranslation() {
  if (!translatedText.value) return

  try {
    await navigator.clipboard.writeText(translatedText.value)
  } catch {
    quickError.value = 'Không thể sao chép bản dịch.'
  }
}
</script>
