<template>
  <div class="bubble-row" :class="rowClass">
    <div class="bubble-shell" :class="shellClass">
      <div class="bubble-avatar" :class="avatarClass">
        <span class="material-symbols-outlined bubble-avatar-icon">{{ avatarIcon }}</span>
      </div>

      <div class="bubble-stack">
        <div class="bubble-meta" :class="metaClass">
          <span class="bubble-role">{{ roleLabel }}</span>
          <span class="bubble-dot">&bull;</span>
          <span class="bubble-subrole">{{ roleSubLabel }}</span>
        </div>

        <div class="bubble-card" :class="cardClass">
          <!-- Japanese content (always shown) -->
          <div class="content">
            <template v-for="(line, index) in displayLines" :key="index">
              <span>{{ line }}</span>
              <br v-if="index < displayLines.length - 1" />
            </template>
          </div>

          <!-- Vietnamese translation panel (toggled) -->
          <div v-if="message.role === 'assistant' && showVn && message.contentVn" class="content-vn">
            <span class="content-vn-label">🇻🇳 Ghi chú</span>
            <p>{{ message.contentVn }}</p>
          </div>

          <div v-if="message.role === 'assistant'" class="bubble-actions">
            <!-- Play TTS — Japanese only -->
            <button
              v-if="message.audioUrl || message.ttsText || message.contentJa || message.content"
              class="bubble-action-button"
              @click="playAudio"
            >
              <span class="material-symbols-outlined text-sm">volume_up</span>
              <span>Play</span>
            </button>
            <!-- Toggle Vietnamese translation -->
            <button
              v-if="message.contentVn"
              class="bubble-action-button"
              :class="showVn ? 'vn-active' : ''"
              @click="showVn = !showVn"
              :title="showVn ? 'Ẩn tiếng Việt' : 'Xem tiếng Việt'"
            >
              <span style="font-size:14px;">🇻🇳</span>
              <span>{{ showVn ? 'Ẩn' : 'Dịch' }}</span>
            </button>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'

const props = defineProps({ message: { type: Object, required: true } })
const audio = ref(null)
const showVn = ref(false)

const roleLabel = computed(() => props.message.role === 'assistant' ? 'AI Tutor' : 'Bạn')
const roleSubLabel = computed(() => props.message.role === 'assistant' ? 'Zen Tutor' : 'Learner')
const rowClass = computed(() => props.message.role === 'user' ? 'is-user' : 'is-assistant')
const shellClass = computed(() => props.message.role === 'user' ? 'is-user' : 'is-assistant')
const avatarClass = computed(() => props.message.role === 'user' ? 'avatar-user' : 'avatar-assistant')
const cardClass = computed(() => props.message.role === 'user' ? 'card-user' : 'card-assistant')
const metaClass = computed(() => props.message.role === 'user' ? 'meta-user' : 'meta-assistant')
const avatarIcon = computed(() => props.message.role === 'user' ? 'person' : 'smart_toy')
let speakingUtterance = null

function extractDisplayText(rawText) {
  if (!rawText || typeof rawText !== 'string') return ''

  const trimmed = rawText.trim()
  const fenceMatch = trimmed.match(/^```(?:json)?\s*([\s\S]*?)\s*```$/i)
  const candidate = fenceMatch ? fenceMatch[1].trim() : trimmed

  // Try full JSON parse — support both new format (content_ja) and old format (content)
  try {
    const parsed = JSON.parse(candidate)
    if (parsed && typeof parsed === 'object') {
      const text = parsed.content_ja || parsed.content
      if (typeof text === 'string') return text.trim()
    }
  } catch (e) {}

  // Regex fallback: try content_ja first, then content
  const jaMatch = candidate.match(/"content_ja"\s*:\s*"([\s\S]*?)"(?:,|\n|\r|\s*})/)
  if (jaMatch && jaMatch[1]) {
    try {
      return JSON.parse(`"${jaMatch[1].replace(/\\/g, '\\\\').replace(/"/g, '\\"')}"`).trim()
    } catch (e) {
      return jaMatch[1].trim()
    }
  }

  const contentMatch = candidate.match(/"content"\s*:\s*"([\s\S]*?)"(?:,|\n|\r|\s*})/)
  if (contentMatch && contentMatch[1]) {
    try {
      return JSON.parse(`"${contentMatch[1].replace(/\\/g, '\\\\').replace(/"/g, '\\"')}"`).trim()
    } catch (e) {
      return contentMatch[1].trim()
    }
  }

  return trimmed
    .replace(/^```(?:json)?\s*/i, '')
    .replace(/\s*```$/i, '')
    .trim()
}

/** Strip common markdown markers so they don't appear in display or TTS */
function stripMarkdown(text) {
  if (!text) return ''
  return text
    .replace(/\*\*([^*]+)\*\*/g, '$1')   // **bold**
    .replace(/\*([^*]+)\*/g, '$1')        // *italic*
    .replace(/__([^_]+)__/g, '$1')        // __bold__
    .replace(/_([^_]+)_/g, '$1')          // _italic_
    .replace(/~~([^~]+)~~/g, '$1')        // ~~strikethrough~~
    .replace(/`([^`]+)`/g, '$1')          // `code`
    .replace(/#{1,6}\s+/g, '')            // # headings
    .trim()
}

// Use contentJa for display (Japanese only); strip markdown before rendering
const displayText = computed(() => {
  const ja = props.message.contentJa || props.message.content || ''
  return stripMarkdown(extractDisplayText(ja))
})

const displayLines = computed(() => {
  const text = displayText.value || ''
  return text ? text.split(/\r?\n/) : ['']
})

function playAudio() {
  // TTS uses only the Japanese content, stripped of markdown
  const jaText = props.message.contentJa || props.message.content || props.message.ttsText || ''
  const text = stripMarkdown(extractDisplayText(jaText))
  if (!text) return

  if ('speechSynthesis' in window) {
    try {
      window.speechSynthesis.cancel()
      const utterance = new SpeechSynthesisUtterance(text)
      utterance.lang = 'ja-JP'
      utterance.rate = 0.95
      utterance.pitch = 1

      const voices = window.speechSynthesis.getVoices?.() || []
      const japaneseVoice = voices.find(v => (v.lang || '').toLowerCase().startsWith('ja'))
      if (japaneseVoice) {
        utterance.voice = japaneseVoice
      }

      speakingUtterance = utterance
      window.speechSynthesis.speak(utterance)
      return
    } catch (e) {
      console.warn('browser TTS failed, falling back to audio url', e)
    }
  }

  const src = props.message.audioUrl || null
  if (!src) return
  if (audio.value) { audio.value.pause(); audio.value = null }
  audio.value = new Audio(src)
  audio.value.play()
}
</script>

<style scoped>
.bubble-row {
  display: flex;
  width: 100%;
}

.bubble-row.is-assistant {
  justify-content: flex-start;
}

.bubble-row.is-user {
  justify-content: flex-end;
}

.bubble-shell {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  max-width: min(100%, 44rem);
}

.bubble-shell.is-user {
  flex-direction: row-reverse;
}

.bubble-avatar {
  width: 42px;
  height: 42px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 12px 24px rgba(45, 52, 53, 0.12);
  border: 1px solid rgba(173, 179, 180, 0.18);
}

.avatar-assistant {
  background: linear-gradient(135deg, #45617d 0%, #cfe5ff 100%);
}

.avatar-user {
  background: linear-gradient(135deg, #7c5556 0%, #ffdad9 100%);
}

.bubble-avatar-icon {
  color: #fff;
  font-size: 20px;
}

.bubble-stack {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.bubble-shell.is-user .bubble-stack {
  align-items: flex-end;
}

.bubble-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #5a6061;
  padding: 0 2px;
}

.bubble-shell.is-user .bubble-meta {
  justify-content: flex-end;
}

.bubble-role {
  color: #45617d;
}

.bubble-subrole {
  color: #7a8486;
}

.bubble-dot {
  color: #adb3b4;
}

.bubble-card {
  position: relative;
  padding: 16px 18px;
  border-radius: 22px;
  border: 1px solid rgba(173, 179, 180, 0.14);
  box-shadow: 0 12px 28px rgba(45, 52, 53, 0.08);
  overflow: hidden;
}

.card-assistant {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.94) 0%, rgba(242, 244, 244, 0.95) 100%);
  border-top-left-radius: 8px;
}

.card-user {
  background: linear-gradient(135deg, #45617d 0%, #395571 100%);
  color: #f5f8ff;
  border-top-right-radius: 8px;
}

.content {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.65;
  font-size: 0.98rem;
}

.bubble-actions {
  margin-top: 12px;
  display: flex;
  gap: 8px;
  justify-content: flex-start;
}

.bubble-action-button {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  border-radius: 9999px;
  border: 1px solid rgba(69, 97, 125, 0.14);
  background: rgba(255, 255, 255, 0.88);
  color: #45617d;
  font-size: 12px;
  font-weight: 700;
  transition: transform 0.15s ease, box-shadow 0.15s ease, background 0.15s ease;
}

.bubble-action-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 20px rgba(69, 97, 125, 0.12);
  background: #ffffff;
}

.corrections {
  margin-top: 6px;
}

.content-vn {
  margin-top: 10px;
  padding: 10px 12px;
  background: rgba(207, 229, 255, 0.25);
  border-left: 3px solid rgba(69, 97, 125, 0.35);
  border-radius: 8px;
  font-size: 0.88rem;
  color: #445566;
  line-height: 1.55;
}

.content-vn-label {
  display: block;
  font-size: 10px;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: #45617d;
  margin-bottom: 4px;
}

.content-vn p {
  margin: 0;
}

.vn-active {
  background: rgba(69, 97, 125, 0.12);
  border-color: rgba(69, 97, 125, 0.4);
}
</style>
