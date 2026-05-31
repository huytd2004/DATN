<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const email = ref('')
const password = ref('')
const showPassword = ref(false)
const loading = ref(false)
const errorMsg = ref('')

const handleLogin = async () => {
  if (loading.value) return
  errorMsg.value = ''
  loading.value = true
  try {
    await authStore.login(email.value, password.value)
    router.push('/dashboard')
  } catch (err) {
    // Lấy message từ response envelope nếu có
    const msg =
      err?.response?.data?.message ||
      err?.message ||
      'Đã có lỗi xảy ra, vui lòng thử lại.'
    errorMsg.value = msg
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="bg-surface text-on-surface font-body antialiased flex flex-col justify-center items-center relative overflow-hidden min-h-screen">
    <!-- Decorative Zen Elements -->
    <div class="absolute top-[-10%] left-[-5%] w-[40%] h-[50%] bg-primary/5 rounded-full blur-3xl -z-10 pointer-events-none"></div>
    <div class="absolute bottom-[-10%] right-[-5%] w-[30%] h-[40%] bg-secondary/5 rounded-full blur-3xl -z-10 pointer-events-none"></div>
    
    <main class="w-full max-w-lg px-6 z-10">
      <!-- Logo / Brand Anchor -->
      <div class="text-center flex flex-col items-center mb-8">
        <div class="w-16 h-16 rounded-xl bg-surface-container-lowest flex items-center justify-center mb-6 shadow-[0_20px_40px_rgba(45,52,53,0.06)]">
          <span class="material-symbols-outlined text-primary text-4xl">spa</span>
        </div>
        <h1 class="font-display text-4xl font-extrabold text-primary tracking-tight">EasyNihongo</h1>
        <p class="font-body text-on-surface-variant mt-3 text-sm tracking-wide">Bước vào không gian học tập tĩnh lặng.</p>
      </div>
      
      <!-- Login Card -->
      <div class="bg-surface-container-lowest rounded-xl p-10 shadow-[0_30px_60px_rgba(45,52,53,0.04)] relative">
        <!-- Error Alert -->
        <transition name="fade">
          <div v-if="errorMsg" class="mb-5 flex items-start gap-3 rounded-lg bg-error-container/60 border border-error/20 px-4 py-3">
            <span class="material-symbols-outlined text-error text-lg mt-0.5 shrink-0">error</span>
            <p class="text-sm text-on-error-container font-body">{{ errorMsg }}</p>
          </div>
        </transition>

        <form @submit.prevent="handleLogin" class="flex flex-col gap-6">
          <!-- Email Field -->
          <div class="flex flex-col gap-2">
            <label class="font-label text-sm text-on-surface font-medium ml-1" for="email">Email</label>
            <div class="relative group">
              <span class="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-outline-variant group-focus-within:text-primary transition-colors">mail</span>
              <input
                v-model="email"
                id="email"
                name="email"
                type="email"
                placeholder="student@gmail.com"
                required
                :disabled="loading"
                class="w-full bg-surface-container-high text-on-surface placeholder:text-outline-variant rounded-lg py-4 pl-12 pr-4 border-0 focus:ring-0 focus:bg-surface-container-lowest focus:shadow-[inset_0_0_0_1px_rgba(69,97,125,0.2)] transition-all outline-none disabled:opacity-60"
              />
            </div>
          </div>
          
          <!-- Password Field -->
          <div class="flex flex-col gap-2">
            <div class="flex justify-between items-center ml-1">
              <label class="font-label text-sm text-on-surface font-medium" for="password">Mật khẩu</label>
              <a class="font-label text-xs text-primary hover:text-primary-dim transition-colors" href="#">Quên mật khẩu?</a>
            </div>
            <div class="relative group">
              <span class="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-outline-variant group-focus-within:text-primary transition-colors">lock</span>
              <input
                v-model="password"
                id="password"
                name="password"
                :type="showPassword ? 'text' : 'password'"
                placeholder="••••••••"
                required
                :disabled="loading"
                class="w-full bg-surface-container-high text-on-surface placeholder:text-outline-variant rounded-lg py-4 pl-12 pr-12 border-0 focus:ring-0 focus:bg-surface-container-lowest focus:shadow-[inset_0_0_0_1px_rgba(69,97,125,0.2)] transition-all outline-none disabled:opacity-60"
              />
              <button
                type="button"
                @click="showPassword = !showPassword"
                class="absolute right-4 top-1/2 -translate-y-1/2 text-outline-variant hover:text-on-surface transition-colors"
                tabindex="-1"
              >
                <span class="material-symbols-outlined text-[20px]">{{ showPassword ? 'visibility' : 'visibility_off' }}</span>
              </button>
            </div>
          </div>
          
          <!-- Submit Button -->
          <div class="mt-4">
            <button
              type="submit"
              :disabled="loading"
              class="w-full bg-primary text-on-primary rounded-full py-4 font-headline font-semibold text-lg shadow-[0_20px_40px_rgba(45,52,53,0.06)] signature-gradient-hover transition-all duration-300 flex items-center justify-center gap-2 disabled:opacity-70 disabled:cursor-not-allowed"
            >
              <span v-if="loading" class="material-symbols-outlined animate-spin text-xl">progress_activity</span>
              <span v-else>Đăng nhập</span>
              <span v-if="!loading" class="material-symbols-outlined text-xl">arrow_forward</span>
            </button>
          </div>
        </form>
      </div>
      
      <!-- Secondary Action -->
      <div class="text-center mt-6">
        <p class="font-body text-sm text-on-surface-variant">
          Bạn chưa có tài khoản? 
          <RouterLink to="/register" class="font-headline font-semibold text-primary hover:text-primary-dim ml-1 transition-colors">Đăng ký</RouterLink>
        </p>
      </div>
    </main>
  </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
.animate-spin {
  animation: spin 0.8s linear infinite;
}
</style>
