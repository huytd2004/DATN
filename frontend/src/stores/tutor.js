import { defineStore } from 'pinia'
import { ref, reactive } from 'vue'
import { tutorService } from '@/services/tutor'

export const useTutorStore = defineStore('tutor', () => {
  const sessionId = ref(null)
  const selectedDeck = ref(null)
  const scenario = ref(null)
  const level = ref('N5')
  const durationMinutes = ref(10)
  const targetWords = ref([])
  const messages = ref([])
  const feedback = ref(null)
  const result = ref(null)
  const recordingState = ref('idle')
  const isLoading = ref(false)
  const lastError = ref(null)

  function persist() {
    const payload = {
      sessionId: sessionId.value,
      selectedDeck: selectedDeck.value,
      scenario: scenario.value,
      level: level.value,
      durationMinutes: durationMinutes.value,
      targetWords: targetWords.value,
      messages: messages.value,
    }
    try {
      sessionStorage.setItem('aiTutorSession', JSON.stringify(payload))
    } catch (e) {
      console.warn('persist failed', e)
    }
  }

  function hydrate() {
    try {
      const raw = sessionStorage.getItem('aiTutorSession')
      if (raw) {
        const p = JSON.parse(raw)
        sessionId.value = p.sessionId || null
        selectedDeck.value = p.selectedDeck || null
        scenario.value = p.scenario || null
        level.value = p.level || 'N5'
        durationMinutes.value = p.durationMinutes || 10
        targetWords.value = p.targetWords || []
        messages.value = p.messages || []
      }
    } catch (e) {
      console.warn('hydrate failed', e)
    }
  }

  async function startSession(payload) {
    try {
      const resp = await tutorService.createSession(payload)
      // Backend wraps in ApiResponse: { status, code, message, data: TutorSessionResponse }
      const data = resp.data?.data || resp.data || resp
      sessionId.value = data.sessionId || data.id || null
      scenario.value = data.scenarioName || payload.scenarioName
      level.value = data.level || payload.level
      durationMinutes.value = data.durationMinutes || payload.durationMinutes
      targetWords.value = data.targetWords || payload.targetWords || []
      messages.value = []
      if (data.initialMessage) messages.value.push(data.initialMessage)
      persist()
      return data
    } catch (e) {
      lastError.value = e
      throw e
    }
  }

  function appendMessage(message) {
    // Normalize audioUrl to absolute when backend returns a leading slash
    if (message && message.audioUrl && typeof message.audioUrl === 'string' && message.audioUrl.startsWith('/')) {
      try {
        // If API is served from same origin, prefix with origin to make a full URL
        message.audioUrl = window.location.origin + message.audioUrl
      } catch (e) {}
    }
    messages.value.push(message)
    persist()
  }

  function buildAssistantMessage(data) {
    // data is already MessageResponse (id, role, content, corrections, suggestions, newVocabulary)
    return {
      id: data.id || Date.now().toString(),
      role: data.role || 'assistant',
      content: data.content || data.contentJa || '',
      contentJa: data.contentJa || data.content || '',
      contentVn: data.contentVn || '',
      audioUrl: data.audioUrl || null,
      corrections: data.corrections || [],
      suggestions: data.suggestions || [],
      newVocabulary: data.newVocabulary || [],
    }
  }

  async function sendTextMessage(content) {
    if (!sessionId.value) throw new Error('no session')
    const userMsg = { id: Date.now().toString(), role: 'user', content }
    appendMessage(userMsg)
    isLoading.value = true
    try {
      const form = new FormData()
      form.append('metadata', JSON.stringify({ content, inputMode: 'text' }))
      const resp = await tutorService.sendMessage(sessionId.value, form)
      // ApiResponse wrapper: resp.data = { status, code, message, data: MessageResponse }
      const payload = resp.data?.data || resp.data
      if (payload) appendMessage(buildAssistantMessage(payload))
      return resp
    } catch (e) {
      lastError.value = e
      throw e
    } finally {
      isLoading.value = false
    }
  }

  async function sendVoiceMessage({ transcript, audioBlob, audioMetadata }) {
    if (!sessionId.value) throw new Error('no session')
    const userMsg = { id: Date.now().toString(), role: 'user', content: transcript, audioMetadata }
    appendMessage(userMsg)
    isLoading.value = true
    try {
      const form = new FormData()
      form.append('metadata', JSON.stringify({ content: transcript, inputMode: 'voice', audioMetadata }))
      if (audioBlob) {
        form.append('audio', audioBlob, 'recording.webm')
      }
      const resp = await tutorService.sendMessage(sessionId.value, form)
      // ApiResponse wrapper: resp.data = { status, code, message, data: MessageResponse }
      const payload = resp?.data?.data || resp?.data
      if (payload) appendMessage(buildAssistantMessage(payload))
      return resp
    } catch (e) {
      lastError.value = e
      throw e
    } finally {
      isLoading.value = false
    }
  }

  async function finishSession() {
    if (!sessionId.value) throw new Error('no session')
    const resp = await tutorService.finishSession(sessionId.value)
    persist()
    return resp
  }

  async function loadResult(id) {
    const sid = id || sessionId.value
    if (!sid) throw new Error('no session')
    const resp = await tutorService.getResult(sid)
    // ApiResponse wrapper: resp.data = { status, code, message, data: TutorResultResponse }
    result.value = resp.data?.data || resp.data || resp
    return result.value
  }

  function resetSession() {
    sessionId.value = null
    selectedDeck.value = null
    scenario.value = null
    level.value = 'N5'
    durationMinutes.value = 10
    targetWords.value = []
    messages.value = []
    feedback.value = null
    result.value = null
    recordingState.value = 'idle'
    lastError.value = null
    try {
      sessionStorage.removeItem('aiTutorSession')
    } catch (e) {}
  }

  return {
    sessionId,
    selectedDeck,
    scenario,
    level,
    durationMinutes,
    targetWords,
    messages,
    feedback,
    result,
    recordingState,
    isLoading,
    lastError,
    persist,
    hydrate,
    startSession,
    appendMessage,
    sendTextMessage,
    sendVoiceMessage,
    finishSession,
    loadResult,
    resetSession,
  }
})

export default useTutorStore
