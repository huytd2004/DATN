<template>
  <div class="fixed top-0 bottom-0 right-0 left-0 md:left-64 z-[45] bg-surface flex overflow-hidden font-body text-on-surface antialiased">
    <!-- Main Content Area -->
    <main class="flex-1 flex flex-col h-full bg-surface relative">
      <div class="absolute inset-0 opacity-10 pointer-events-none" style="background-size: 40px 40px; background-image: radial-gradient(circle, #adb3b4 1px, transparent 1px);"></div>

      <!-- TopAppBar -->
      <header class="flex items-center justify-between px-8 w-full h-16 shrink-0 z-40 bg-white/80 dark:bg-[#121212]/80 backdrop-blur-md shadow-sm border-b border-outline-variant/10">
        <div class="flex items-center gap-4">
          <RouterLink to="/tutor" class="p-2 -ml-2 rounded-full hover:bg-surface-container transition-colors flex items-center justify-center">
            <span class="material-symbols-outlined text-on-surface-variant">arrow_back</span>
          </RouterLink>
          <h2 class="text-xl font-black text-[#45617d] dark:text-[#cfe5ff] font-headline">AI Tutor</h2>
          <span class="px-3 py-1 bg-primary-container text-on-primary-container rounded-full text-[10px] font-bold uppercase tracking-wider hidden sm:inline-block">Hội thoại thực tế</span>
        </div>
        <div class="flex items-center gap-6">
          <div class="hidden lg:flex items-center bg-surface-container-high rounded-full px-4 py-1.5 w-64 border border-outline-variant/10">
            <span class="material-symbols-outlined text-on-surface-variant text-lg">search</span>
            <input class="bg-transparent border-none focus:ring-0 text-sm w-full placeholder:text-on-surface-variant/60 outline-none pl-2" placeholder="Tìm kiếm bài học..." type="text"/>
          </div>
          <div class="flex items-center gap-2">
            <button class="w-10 h-10 flex items-center justify-center rounded-full hover:bg-surface-container transition-colors">
              <span class="material-symbols-outlined text-on-surface-variant" style="font-variation-settings: 'FILL' 1;">volume_up</span>
            </button>
            <button class="w-10 h-10 flex items-center justify-center rounded-full hover:bg-surface-container transition-colors">
              <span class="material-symbols-outlined text-on-surface-variant" data-icon="settings">settings</span>
            </button>
          </div>
        </div>
      </header>

      <!-- Chat Canvas -->
      <section ref="chatScroll" class="flex-1 overflow-y-auto px-4 md:px-8 py-6 space-y-8 scroll-smooth z-10 relative">
        <!-- Date Separator -->
        <div class="flex justify-center">
          <span class="px-4 py-1 bg-surface-container-low text-on-surface-variant text-[11px] font-semibold rounded-full uppercase tracking-widest border border-outline-variant/10">Hôm nay</span>
        </div>

        <div class="flex flex-col gap-6">
          <template v-for="(m, index) in store.messages" :key="m.id">
            <div class="max-w-2xl" :class="m.role === 'user' ? 'self-end' : 'self-start'">
              <MessageBubble :message="m" />
            </div>

            <!-- Grammar corrections: shown below user bubble, taken from next assistant message -->
            <template v-if="m.role === 'user'">
              <div
                v-for="(c, ci) in (store.messages[index + 1]?.corrections || [])"
                :key="ci"
                class="self-end max-w-md w-full"
              >
                <CorrectionCard :correction="c" />
              </div>
            </template>
          </template>

          <!-- AI Thinking Indicator -->
          <div v-if="store.isLoading" class="max-w-2xl self-start">
            <div class="flex items-center gap-3 px-4 py-3 bg-surface-container-lowest rounded-2xl border border-outline-variant/10 w-fit">
              <div class="flex gap-1 items-center">
                <span class="w-2 h-2 rounded-full bg-primary animate-bounce" style="animation-delay: 0ms;"></span>
                <span class="w-2 h-2 rounded-full bg-primary animate-bounce" style="animation-delay: 150ms;"></span>
                <span class="w-2 h-2 rounded-full bg-primary animate-bounce" style="animation-delay: 300ms;"></span>
              </div>
              <span class="text-xs text-on-surface-variant">AI đang suy nghĩ...</span>
            </div>
          </div>
        </div>
      </section>

      <!-- Microphone fallback banner -->
      <div v-if="micPermissionDenied || !canUseVoiceCapture" class="mx-4 md:mx-8 mb-2 px-4 py-2.5 bg-error-container text-on-error-container text-xs rounded-xl flex items-center gap-2 border border-error/20">
        <span class="material-symbols-outlined text-base">mic_off</span>
        Không dùng được microphone — bạn vẫn có thể nhập text bên dưới.
      </div>

      <!-- Bottom Input Dock -->
      <footer class="p-4 md:p-6 bg-white/80 dark:bg-slate-900/80 backdrop-blur-xl border-t border-outline-variant/10 shrink-0 z-40">
        <div class="max-w-4xl mx-auto flex items-end gap-2 md:gap-4">
          <div class="flex-1 bg-surface-container-lowest rounded-[28px] shadow-sm border border-outline-variant/20 focus-within:border-primary/30 transition-all p-2 flex items-end gap-2">
            <textarea
              v-model="text"
              class="flex-1 bg-transparent border-none focus:ring-0 text-sm py-2.5 px-2 md:px-3 resize-none max-h-32 placeholder:text-on-surface-variant/40 font-body outline-none"
              placeholder="Nhập tin nhắn..."
              rows="1"
              @keydown.enter.exact.prevent="sendText"
            ></textarea>
          </div>
          <div class="flex gap-2 shrink-0 items-center">
            <!-- Voice button -->
            <button
              class="w-12 h-12 flex items-center justify-center rounded-full transition-all relative"
              :class="recordButtonClass"
              @click="toggleVoiceCapture"
              :disabled="!canUseVoiceCapture || recordingState === 'requesting' || recordingState === 'transcribing' || recordingState === 'sending'"
              :aria-pressed="recordingState === 'recording'"
              :title="recordButtonTitle"
            >
              <span class="material-symbols-outlined" :class="recordingState === 'recording' ? 'animate-pulse' : ''">
                {{ micIcon }}
              </span>
            </button>
            <!-- Send text -->
            <button
              class="w-12 h-12 flex items-center justify-center rounded-full bg-gradient-to-br from-primary to-primary-dim text-white shadow-lg shadow-primary/20 active:scale-95 transition-transform disabled:opacity-50"
              @click="sendText"
              :disabled="!text.trim() || store.isLoading"
            >
              <span class="material-symbols-outlined">send</span>
            </button>
            <!-- Finish session -->
            <button
              class="w-12 h-12 flex items-center justify-center rounded-full bg-surface-container-high text-on-surface hover:bg-error-container hover:text-on-error-container transition-colors"
              @click="handleFinishSession"
              :disabled="isFinishing"
              title="Kết thúc phiên"
            >
              <span class="material-symbols-outlined text-base">{{ isFinishing ? 'hourglass_empty' : 'stop_circle' }}</span>
            </button>
          </div>
        </div>

        <!-- Recording status label -->
        <div class="mt-2 max-w-4xl mx-auto h-5">
          <p v-if="recordingState === 'recording'" class="text-xs text-red-500 font-semibold flex items-center gap-1">
            <span class="w-2 h-2 rounded-full bg-red-500 animate-pulse inline-block"></span>
            Đang ghi âm...
          </p>
          <p v-else-if="recordingState === 'transcribing'" class="text-xs text-on-surface-variant">
            Đang xử lý giọng nói...
          </p>
          <p v-else-if="recordingState === 'sending'" class="text-xs text-on-surface-variant">
            Đang gửi...
          </p>
          <p v-else-if="recordingState === 'error'" class="text-xs text-error">
            Lỗi ghi âm — hãy thử lại hoặc nhập text.
          </p>
        </div>

        <div class="mt-2 max-w-4xl mx-auto">
          <!-- Quick replies -->
          <div class="flex flex-wrap gap-2">
            <button
              v-for="s in suggestions"
              :key="s"
              class="px-4 py-2 bg-white border border-primary/10 hover:border-primary/40 text-primary text-xs font-semibold rounded-full transition-all hover:bg-primary-container/20"
              @click="applyQuickReply(s)"
            >{{ s }}</button>
          </div>
          <div class="mt-4 text-center">
            <p class="text-[10px] text-on-surface-variant/50 font-medium tracking-wide uppercase">AI Tutor có thể mắc lỗi. Hãy luôn đối chiếu với giáo trình chính thức.</p>
          </div>
        </div>
      </footer>
    </main>

    <!-- Right Insights Panel (Bento-style) -->
    <aside class="hidden xl:flex flex-col w-80 bg-surface-container-low p-6 gap-6 h-full overflow-y-auto shrink-0 border-l border-outline-variant/10 z-20">
      <h3 class="text-xs font-extrabold text-on-surface-variant uppercase tracking-[0.2em] mb-2">Thống kê buổi học</h3>

      <!-- Bento Card 1: Session Goal -->
      <div class="bg-surface-container-lowest p-5 rounded-3xl shadow-sm border border-outline-variant/10">
        <div class="flex items-center gap-3 mb-4">
          <div class="w-8 h-8 rounded-full bg-primary-container flex items-center justify-center">
            <span class="material-symbols-outlined text-on-primary-container text-sm">target</span>
          </div>
          <span class="text-sm font-bold">Mục tiêu hôm nay</span>
        </div>
        <div class="relative pt-1">
          <div class="flex mb-2 items-center justify-between">
            <div>
              <span class="text-xs font-semibold inline-block text-primary">Tiến độ hội thoại</span>
            </div>
            <div class="text-right">
              <span class="text-xs font-bold inline-block text-primary">{{ sessionProgressPercent }}%</span>
            </div>
          </div>
          <div class="overflow-hidden h-2 mb-4 text-xs flex rounded-full bg-surface-container-high border border-outline-variant/10">
            <div
              class="shadow-none flex flex-col text-center whitespace-nowrap text-white justify-center bg-gradient-to-r from-primary to-primary-dim transition-all duration-500"
              :style="{ width: sessionProgressPercent + '%' }"
            ></div>
          </div>
          <p class="text-[11px] text-on-surface-variant">{{ elapsedTimeLabel }} &bull; {{ userMessageCount }} lượt nói</p>
        </div>
      </div>

      <!-- Bento Card 2: Vocabulary Highlights (bound to real newVocabulary) -->
      <div class="bg-surface-container-lowest p-5 rounded-3xl shadow-sm border border-outline-variant/10 flex-1">
        <h4 class="text-sm font-bold mb-4">Từ vựng mới</h4>
        <div v-if="allNewVocabulary.length === 0" class="text-[11px] text-on-surface-variant text-center py-4">
          Chưa có từ vựng mới trong phiên này.
        </div>
        <div v-else class="space-y-3 overflow-y-auto max-h-60">
          <div
            v-for="vocab in allNewVocabulary"
            :key="vocab.surface"
            class="flex items-center justify-between group cursor-pointer hover:bg-surface-container/50 p-2 -mx-2 rounded-xl transition-colors"
          >
            <div class="flex flex-col">
              <span class="text-sm font-bold text-on-surface">{{ vocab.surface }} <span class="font-normal text-on-surface-variant text-xs">({{ vocab.reading }})</span></span>
              <span class="text-[10px] text-on-surface-variant">{{ vocab.meaning }}</span>
            </div>
            <button class="opacity-0 group-hover:opacity-100 transition-opacity" title="Lưu từ vựng">
              <span class="material-symbols-outlined text-primary text-sm">bookmark</span>
            </button>
          </div>
        </div>
      </div>

      <!-- Bento Card 3: Zen Mode Toggle -->
      <div class="mt-auto bg-primary text-on-primary p-6 rounded-3xl shadow-xl shadow-primary/20 relative overflow-hidden">
        <div class="absolute top-0 right-0 p-4 opacity-10">
          <span class="material-symbols-outlined text-8xl" style="font-variation-settings: 'opsz' 48;">spa</span>
        </div>
        <div class="relative z-10">
          <h4 class="text-lg font-bold mb-2">Chế độ Tập trung</h4>
          <p class="text-xs text-primary-container/80 mb-6 leading-relaxed">Ẩn mọi thông báo và chỉ tập trung vào cuộc hội thoại.</p>
          <button class="w-full py-3 bg-white/10 hover:bg-white/20 backdrop-blur-md rounded-xl text-xs font-bold transition-all border border-white/20">
            Kích hoạt ngay
          </button>
        </div>
      </div>
    </aside>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import MessageBubble from '@/components/tutor/MessageBubble.vue'
import CorrectionCard from '@/components/tutor/CorrectionCard.vue'
import { useTutorStore } from '@/stores/tutor'

const route = useRoute()
const router = useRouter()
const store = useTutorStore()

const text = ref('')
const voiceTranscript = ref('')
const recordingState = ref('idle')
const micPermissionDenied = ref(false)
const isFinishing = ref(false)
const chatScroll = ref(null)

let recordSeconds = 0
let recordTimerId = null
let mediaRecorder = null
let recordedChunks = []
let audioStream = null
const pendingVoiceBlob = ref(null)   // holds recorded blob until user presses Send
const elapsedSeconds = ref(0)
let sessionTimerId = null

// Web Speech API
const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
const supportsSpeech = !!SpeechRecognition
const canUseVoiceCapture = typeof navigator !== 'undefined'
  && !!navigator.mediaDevices?.getUserMedia
  && typeof window.MediaRecorder !== 'undefined'
let recognition = null

// ── Computed ──────────────────────────────────────────────────────────────────

const suggestions = computed(() => {
  const arr = (store.messages || []).slice().reverse()
  const lastAssistant = arr.find(m => m.role === 'assistant')
  return (lastAssistant && lastAssistant.suggestions && lastAssistant.suggestions.length)
    ? lastAssistant.suggestions
    : []
})

/** Tích lũy toàn bộ newVocabulary từ tất cả assistant messages, loại trùng theo surface */
const allNewVocabulary = computed(() => {
  const seen = new Set()
  const result = []
  for (const m of store.messages) {
    if (m.role === 'assistant' && Array.isArray(m.newVocabulary)) {
      for (const v of m.newVocabulary) {
        if (v.surface && !seen.has(v.surface)) {
          seen.add(v.surface)
          result.push(v)
        }
      }
    }
  }
  return result
})

const userMessageCount = computed(() =>
  store.messages.filter(m => m.role === 'user').length
)

/** Tiến độ tính dựa trên thời gian thực đã sử dụng / durationMinutes */
const sessionProgressPercent = computed(() => {
  const totalSeconds = (store.durationMinutes || 10) * 60
  return Math.min(Math.round((elapsedSeconds.value / totalSeconds) * 100), 100)
})

/** Hiển thị mm:ss / mm:00 */
const elapsedTimeLabel = computed(() => {
  const m = Math.floor(elapsedSeconds.value / 60)
  const s = elapsedSeconds.value % 60
  const total = store.durationMinutes || 10
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')} / ${String(total).padStart(2, '0')}:00`
})

const micIcon = computed(() => {
  if (recordingState.value === 'recording') return 'stop'
  if (recordingState.value === 'requesting') return 'sync'
  if (recordingState.value === 'transcribing') return 'graphic_eq'
  if (recordingState.value === 'sending') return 'sync'
  if (recordingState.value === 'error') return 'mic_off'
  return 'keyboard_voice'
})

const recordButtonTitle = computed(() => {
  if (recordingState.value === 'recording') return 'Dừng ghi âm'
  if (recordingState.value === 'requesting') return 'Đang xin quyền...'
  if (recordingState.value === 'transcribing') return 'Đang xử lý giọng nói...'
  if (recordingState.value === 'sending') return 'Đang gửi...'
  if (recordingState.value === 'error') return 'Lỗi ghi âm'
  return 'Nhấn để ghi âm'
})

const recordButtonClass = computed(() => {
  if (recordingState.value === 'recording') {
    return 'bg-red-500 text-white shadow-lg shadow-red-500/25 hover:bg-red-600'
  }
  if (['requesting', 'transcribing', 'sending'].includes(recordingState.value)) {
    return 'bg-surface-container-high text-on-surface opacity-70 cursor-wait'
  }
  if (recordingState.value === 'error') {
    return 'bg-error-container text-on-error-container'
  }
  return 'bg-surface-container-high text-on-surface hover:bg-surface-container'
})

// ── Voice capture ─────────────────────────────────────────────────────────────

function initRecognition() {
  if (!supportsSpeech) return
  recognition = new SpeechRecognition()
  recognition.lang = 'ja-JP'
  recognition.interimResults = true
  recognition.maxAlternatives = 1
  recognition.onresult = (ev) => {
    const transcript = Array.from(ev.results)
      .map((result) => result[0].transcript)
      .join('')
      .trim()
    voiceTranscript.value = transcript
    text.value = transcript   // show in textarea for review
  }
  recognition.onend = () => {}
  recognition.onerror = () => {}
}

function startTimer() {
  recordSeconds = 0
  stopTimer()
  recordTimerId = window.setInterval(() => { recordSeconds += 1 }, 1000)
}

function stopTimer() {
  if (recordTimerId) {
    window.clearInterval(recordTimerId)
    recordTimerId = null
  }
}

// ── Session timer ─────────────────────────────────────────────────────

function startSessionTimer() {
  stopSessionTimer()
  elapsedSeconds.value = 0
  sessionTimerId = window.setInterval(() => { elapsedSeconds.value += 1 }, 1000)
}

function stopSessionTimer() {
  if (sessionTimerId) {
    window.clearInterval(sessionTimerId)
    sessionTimerId = null
  }
}

// ── TTS (speechSynthesis) ──────────────────────────────────────────────

function speakText(text) {
  if (!text || !window.speechSynthesis) return
  window.speechSynthesis.cancel()
  const utterance = new SpeechSynthesisUtterance(text)
  utterance.lang = 'ja-JP'
  utterance.rate = 0.9
  utterance.pitch = 1.0
  window.speechSynthesis.speak(utterance)
}

function cleanupRecordingStream() {
  if (audioStream) {
    audioStream.getTracks().forEach(track => track.stop())
    audioStream = null
  }
}

async function sendRecordedVoice(blob) {
  recordingState.value = 'sending'
  store.recordingState = 'sending'
  try {
    await store.sendVoiceMessage({
      transcript: voiceTranscript.value.trim(),
      audioBlob: blob,
      audioMetadata: {
        durationMs: recordSeconds * 1000,
        mimeType: blob.type || 'audio/webm',
      },
    })
    text.value = ''
    voiceTranscript.value = ''
    pendingVoiceBlob.value = null
  } catch (e) {
    console.error(e)
  } finally {
    recordingState.value = 'idle'
    store.recordingState = 'idle'
  }
}

async function startVoiceCapture() {
  if (!canUseVoiceCapture) return
  recordingState.value = 'requesting'
  store.recordingState = 'requesting'
  micPermissionDenied.value = false
  try {
    audioStream = await navigator.mediaDevices.getUserMedia({ audio: true })
    mediaRecorder = new MediaRecorder(audioStream)
    recordedChunks = []
    voiceTranscript.value = ''
    text.value = ''

    mediaRecorder.ondataavailable = (event) => {
      if (event.data && event.data.size) recordedChunks.push(event.data)
    }

    mediaRecorder.onstop = async () => {
      const blob = new Blob(recordedChunks, { type: mediaRecorder?.mimeType || 'audio/webm' })
      cleanupRecordingStream()
      stopTimer()
      if (blob.size > 0) {
        // Store blob and show transcript in textarea — user reviews then presses Send
        pendingVoiceBlob.value = blob
        recordingState.value = 'idle'
        store.recordingState = 'idle'
        // Focus textarea so user can edit/confirm
        await nextTick()
      } else {
        recordingState.value = 'idle'
        store.recordingState = 'idle'
      }
    }

    mediaRecorder.start()
    if (supportsSpeech) {
      if (!recognition) initRecognition()
      try { recognition.start() } catch (e) { console.warn('recognition start failed', e) }
    }

    startTimer()
    recordingState.value = 'recording'
    store.recordingState = 'recording'
  } catch (e) {
    cleanupRecordingStream()
    stopTimer()
    if (e && (e.name === 'NotAllowedError' || e.name === 'PermissionDeniedError')) {
      micPermissionDenied.value = true
    }
    recordingState.value = 'error'
    store.recordingState = 'error'
    console.error('record error', e)
  }
}

function stopVoiceCapture() {
  if (recognition) {
    try { recognition.stop() } catch (e) { console.warn('recognition stop failed', e) }
  }
  if (mediaRecorder && mediaRecorder.state !== 'inactive') {
    try { mediaRecorder.stop() } catch (e) { console.warn('mediaRecorder stop failed', e) }
  } else {
    cleanupRecordingStream()
    stopTimer()
    recordingState.value = 'idle'
    store.recordingState = 'idle'
  }
}

function toggleVoiceCapture() {
  if (recordingState.value === 'recording' || recordingState.value === 'requesting') {
    stopVoiceCapture()
    return
  }
  startVoiceCapture()
}

// ── Text send ─────────────────────────────────────────────────────────────────

async function sendText() {
  const content = text.value.trim()
  if (!content) return
  try {
    text.value = ''
    if (pendingVoiceBlob.value) {
      // Send as voice message (includes audio blob + transcript)
      const blob = pendingVoiceBlob.value
      pendingVoiceBlob.value = null
      await store.sendVoiceMessage({
        transcript: content,
        audioBlob: blob,
        audioMetadata: { durationMs: recordSeconds * 1000, mimeType: blob.type || 'audio/webm' },
      })
      voiceTranscript.value = ''
    } else {
      await store.sendTextMessage(content)
    }
  } catch (e) {
    console.error(e)
  }
}

// ── Quick reply ───────────────────────────────────────────────────────────────

function applyQuickReply(s) {
  text.value = s
  sendText()
}

// ── Finish session ────────────────────────────────────────────────────────────

async function handleFinishSession() {
  if (isFinishing.value) return
  isFinishing.value = true
  try {
    await store.finishSession()
  } catch (e) {
    console.warn('finishSession failed (non-critical)', e)
  } finally {
    isFinishing.value = false
  }
  router.push(`/tutor/result?sessionId=${store.sessionId}`)
}

// ── Auto-scroll ───────────────────────────────────────────────────────────────

watch(() => store.messages.length, async (newLen, oldLen) => {
  // Auto-speak new assistant messages via speechSynthesis (Japanese only)
  if (newLen > (oldLen ?? 0)) {
    const lastMsg = store.messages[newLen - 1]
    if (lastMsg?.role === 'assistant') {
      const jaText = lastMsg.contentJa || lastMsg.content || ''
      if (jaText) speakText(jaText)
    }
  }
  await nextTick()
  if (chatScroll.value) {
    chatScroll.value.scrollTop = chatScroll.value.scrollHeight
  }
})

watch(() => store.isLoading, async (val) => {
  if (val) {
    await nextTick()
    if (chatScroll.value) chatScroll.value.scrollTop = chatScroll.value.scrollHeight
  }
})

// ── Lifecycle ─────────────────────────────────────────────────────────────────

onBeforeUnmount(() => {
  if (recognition) try { recognition.stop() } catch (e) {}
  cleanupRecordingStream()
  stopTimer()
  stopSessionTimer()
  if (window.speechSynthesis) window.speechSynthesis.cancel()
})

onMounted(async () => {
  await store.hydrate()
  const sid = route.query.sessionId || store.sessionId
  if (!sid) router.push('/tutor')
  // Start real-time session timer
  startSessionTimer()
  // Speak the initial assistant message (Japanese only)
  const initMsg = store.messages.find(m => m.role === 'assistant')
  if (initMsg) {
    const jaText = initMsg.contentJa || initMsg.content || ''
    if (jaText) speakText(jaText)
  }
  await nextTick()
  if (chatScroll.value) chatScroll.value.scrollTop = chatScroll.value.scrollHeight
})
</script>
