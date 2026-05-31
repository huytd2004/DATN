<template>
  <header class="mb-12 text-center md:text-left">
    <h1 class="text-4xl font-extrabold text-on-surface tracking-tight mb-4">Tra cứu&nbsp;</h1>
    <div class="flex justify-center md:justify-start mb-8">
      <div class="bg-surface-container-high p-1 rounded-2xl flex gap-1">
        <RouterLink to="/dictionary" active-class="bg-surface-container-lowest text-primary shadow-sm" class="px-6 py-2.5 rounded-xl text-sm font-bold text-on-surface-variant hover:text-on-surface transition-colors">Từ vựng</RouterLink>
        <RouterLink to="/kanji" active-class="bg-surface-container-lowest text-primary shadow-sm" class="px-6 py-2.5 rounded-xl text-sm font-bold text-on-surface-variant hover:text-on-surface transition-colors">Kanji</RouterLink>
        <RouterLink to="/grammar" active-class="bg-surface-container-lowest text-primary shadow-sm" class="px-6 py-2.5 rounded-xl text-sm font-bold text-on-surface-variant hover:text-on-surface transition-colors">Ngữ pháp</RouterLink>
      </div>
    </div>
    
    <div class="relative max-w-2xl">
      <div class="absolute inset-y-0 left-6 flex items-center pointer-events-none">
        <span class="material-symbols-outlined text-outline">search</span>
      </div>
      <input 
        class="w-full bg-surface-container-high border-none rounded-2xl py-5 px-6 pl-14 focus:ring-2 focus:ring-primary/20 focus:bg-surface-container-lowest transition-all text-lg placeholder:text-outline" 
        :placeholder="placeholder" 
        type="text" 
        v-model="searchQuery" 
        @keyup.enter="handleSearch"
      >
      
      <!-- Extra tools for Kanji (draw, category) -->
      <div class="absolute right-4 top-1/2 -translate-y-1/2 flex gap-2" v-if="showExtraTools">
        <button class="p-2 hover:bg-surface-container-highest rounded-lg transition-colors">
          <span class="material-symbols-outlined text-sm">draw</span>
        </button>
        <button class="p-2 hover:bg-surface-container-highest rounded-lg transition-colors">
          <span class="material-symbols-outlined text-sm">category</span>
        </button>
      </div>
      
      <!-- Search button for others -->
      <div class="absolute inset-y-2 right-2" v-else>
        <button class="h-full px-6 bg-primary text-white rounded-xl font-bold text-sm hover:opacity-90 transition-opacity" @click="handleSearch">Tìm kiếm</button>
      </div>
    </div>
    
    <div class="flex flex-wrap gap-2 mt-4 justify-center md:justify-start" v-if="suggestions.length > 0">
      <span class="text-xs text-on-surface-variant uppercase tracking-widest mr-2 py-1">Gợi ý:</span>
      <button 
        v-for="suggestion in suggestions" 
        :key="suggestion.label" 
        class="px-3 py-1 bg-surface-container-highest rounded-md text-xs font-medium text-on-tertiary-container hover:bg-primary-container transition-colors" 
        @click="selectSuggestion(suggestion.value)"
      >
        {{ suggestion.label }}
      </button>
    </div>
  </header>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  placeholder: {
    type: String,
    default: 'Nhập từ khóa tìm kiếm...'
  },
  suggestions: {
    type: Array,
    default: () => []
  },
  showExtraTools: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['search'])

const searchQuery = ref('')

const handleSearch = () => {
  if (searchQuery.value.trim()) {
    emit('search', searchQuery.value)
  }
}

const selectSuggestion = (value) => {
  searchQuery.value = value
  handleSearch()
}
</script>
