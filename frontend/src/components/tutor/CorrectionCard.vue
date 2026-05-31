<template>
  <div class="correction-wrap">
    <!-- Icon -->
    <div class="correction-icon">
      <span class="material-symbols-outlined" style="font-variation-settings: 'FILL' 1; font-size: 16px;">auto_fix_high</span>
    </div>

    <!-- Content -->
    <div class="correction-body">
      <p class="correction-label">Gợi ý ngữ pháp:</p>

      <!-- note / explanation -->
      <p class="correction-note" v-if="note">{{ note }}</p>

      <!-- original → corrected -->
      <p class="correction-pair">
        <span class="correction-wrong">「{{ correction.original }}」</span>
        <span class="correction-arrow"> → </span>
        <span class="correction-right">「{{ correction.corrected }}」</span>
      </p>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({ correction: { type: Object, required: true } })

// Support both 'note' and legacy 'explanation' field names
const note = computed(() => props.correction.note || props.correction.explanation || '')
</script>

<style scoped>
.correction-wrap {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  /* Light red — matching stitch.html error/30 tone */
  background: rgba(220, 80, 60, 0.07);
  border: 1px solid rgba(220, 80, 60, 0.15);
  border-left: 3px solid rgba(200, 60, 40, 0.45);
  padding: 12px 14px;
  border-radius: 16px;
  border-top-right-radius: 4px;
  animation: slide-in 0.35s ease;
}

@keyframes slide-in {
  from { opacity: 0; transform: translateX(8px); }
  to   { opacity: 1; transform: translateX(0); }
}

.correction-icon {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  /* Soft red icon background */
  background: rgba(200, 60, 40, 0.75);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 1px;
}

.correction-body {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.correction-label {
  font-size: 10px;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  color: rgba(180, 50, 30, 0.85);
  margin: 0;
}

.correction-note {
  font-size: 13px;
  color: #4a3030;
  line-height: 1.5;
  margin: 0;
}

.correction-pair {
  font-size: 13px;
  margin: 2px 0 0;
  font-style: italic;
}

.correction-wrong {
  color: #b83232;
  font-weight: 600;
  text-decoration: line-through;
  text-decoration-color: rgba(184, 50, 50, 0.5);
}

.correction-arrow {
  color: #aaa;
  margin: 0 2px;
}

.correction-right {
  color: #2e7d4f;
  font-weight: 700;
}
</style>
