<template>
  <div class="voice-recorder">
    <button @click="toggle" :disabled="requesting">{{ state === 'recording' ? 'Stop' : 'Record' }}</button>
    <span v-if="state === 'recording'">● Recording {{ seconds }}s</span>
    <span v-else-if="state === 'requesting'">Requesting permission…</span>
    <audio v-if="playUrl" :src="playUrl" controls></audio>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const emit = defineEmits(['recorded', 'error'])

const state = ref('idle') // idle | requesting | recording | error
const seconds = ref(0)
let intervalId = null
let mediaRecorder = null
let chunks = []
const playUrl = ref('')
const requesting = ref(false)

function startTimer() {
  seconds.value = 0
  intervalId = setInterval(() => seconds.value++, 1000)
}

function stopTimer() {
  if (intervalId) { clearInterval(intervalId); intervalId = null }
}

async function startRecording() {
  try {
    requesting.value = true
    state.value = 'requesting'
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
    mediaRecorder = new MediaRecorder(stream)
    chunks = []
    mediaRecorder.ondataavailable = (e) => { if (e.data && e.data.size) chunks.push(e.data) }
    mediaRecorder.onstop = async () => {
      stopTimer()
      const blob = new Blob(chunks, { type: 'audio/webm' })
      playUrl.value = URL.createObjectURL(blob)
      const audioMetadata = { durationMs: seconds.value * 1000, mimeType: blob.type }
      emit('recorded', { blob, audioMetadata })
      state.value = 'idle'
      requesting.value = false
    }
    mediaRecorder.start()
    startTimer()
    state.value = 'recording'
    requesting.value = false
  } catch (e) {
    state.value = 'error'
    requesting.value = false
    emit('error', e)
  }
}

function stopRecording() {
  try {
    if (mediaRecorder && mediaRecorder.state !== 'inactive') mediaRecorder.stop()
    state.value = 'idle'
    stopTimer()
  } catch (e) {
    emit('error', e)
  }
}

function toggle() {
  if (state.value === 'recording') stopRecording()
  else startRecording()
}
</script>

<style scoped>
.voice-recorder { display:flex; gap:8px; align-items:center }
.voice-recorder button { padding:6px 10px }
</style>
