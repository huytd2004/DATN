<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

// ─── Form state ──────────────────────────────────────────────────────────────
const username = ref('')
const email = ref('')
const password = ref('')
const showPassword = ref(false)
const targetLevel = ref('N3')
const loading = ref(false)
const errorMsg = ref('')

// Map nhãn hiển thị → giá trị enum backend (N5, N4, N3, N2, N1)
const levelOptions = [
  { label: 'JLPT N5', value: 'N5' },
  { label: 'JLPT N4', value: 'N4' },
  { label: 'JLPT N3', value: 'N3' },
  { label: 'JLPT N2', value: 'N2' },
  { label: 'JLPT N1', value: 'N1' },
]

const handleRegister = async () => {
  if (loading.value) return
  errorMsg.value = ''

  if (password.value.length < 8) {
    errorMsg.value = 'Mật khẩu phải có ít nhất 8 ký tự.'
    return
  }

  loading.value = true
  try {
    await authStore.register({
      username: username.value,
      email: email.value,
      password: password.value,
      targetLevel: targetLevel.value,
    })
    router.push('/dashboard')
  } catch (err) {
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
  <div class="bg-surface font-body text-on-surface antialiased min-h-screen flex flex-col md:flex-row overflow-hidden selection:bg-primary-container selection:text-on-primary-container">
    <!-- Left Visual Pane -->
    <div class="hidden md:flex w-full md:w-5/12 lg:w-1/2 relative bg-surface-container flex-col justify-between p-12 overflow-hidden">
      <div class="absolute inset-0 z-0">
        <img
          class="w-full h-full object-cover opacity-80 mix-blend-multiply"
          src="https://thuthuatnhanh.com/wp-content/uploads/2020/09/hinh-nen-co-xanh-cung-cay-co-don.jpg"
          alt="Bamboo forest"
        />
      </div>
      <div class="absolute inset-0 bg-gradient-to-t from-surface-container/90 via-surface-container/20 to-transparent z-10"></div>
      <div class="absolute inset-0 bg-primary/5 mix-blend-overlay z-10"></div>
      
      <div class="z-20 relative">
        <h1 class="font-headline text-2xl font-bold text-on-primary-fixed tracking-tight">EasyNihongo</h1>
      </div>
      
      <div class="z-20 relative max-w-md">
        <p class="font-headline text-4xl text-on-primary-fixed font-light leading-tight mb-4">
          Làm chủ tiếng Nhật với sự tập trung.
        </p>
        <p class="font-body text-on-surface-variant text-base leading-relaxed">
          Bước vào không gian tĩnh lặng được thiết kế cho việc học sâu. Nơi AI thích ứng gặp gỡ sự thanh bình truyền thống.
        </p>
      </div>
    </div>
    
    <!-- Right Interaction Pane -->
    <div class="w-full md:w-7/12 lg:w-1/2 flex items-center justify-center p-6 md:p-12 lg:p-24 relative bg-surface">
      <!-- Ambient Background -->
      <div class="absolute top-0 right-0 w-96 h-96 bg-primary-fixed/20 rounded-full blur-[80px] pointer-events-none -translate-y-1/2 translate-x-1/3"></div>
      <div class="absolute bottom-0 left-0 w-[500px] h-[500px] bg-secondary-fixed/10 rounded-full blur-[100px] pointer-events-none translate-y-1/3 -translate-x-1/4"></div>
      
      <!-- Card -->
      <div class="w-full max-w-[440px] bg-surface-container-lowest rounded-xl p-8 md:p-10 shadow-[0_24px_60px_rgba(45,52,53,0.04)] relative z-10">
        <div class="mb-8 text-center md:text-left">
          <h2 class="font-headline text-3xl font-light text-on-surface mb-2">Bắt đầu hành trình</h2>
          <p class="font-body text-sm text-on-surface-variant">Tạo ra hành trình học tập mang đậm dấu ấn cá nhân của bạn.</p>
        </div>
        
        <!-- Error Alert -->
        <transition name="fade">
          <div v-if="errorMsg" class="mb-5 flex items-start gap-3 rounded-lg bg-error-container/60 border border-error/20 px-4 py-3">
            <span class="material-symbols-outlined text-error text-lg mt-0.5 shrink-0">error</span>
            <p class="text-sm text-on-error-container font-body">{{ errorMsg }}</p>
          </div>
        </transition>

        <form @submit.prevent="handleRegister" class="space-y-5">
          <!-- Username -->
          <div class="flex flex-col gap-1.5">
            <label class="font-body text-sm font-medium text-on-surface-variant" for="username">Tên đăng nhập</label>
            <input
              v-model="username"
              id="username"
              name="username"
              type="text"
              placeholder="vd: tanaka_hana"
              required
              :disabled="loading"
              class="w-full rounded-lg bg-surface-container-high px-4 py-3.5 text-on-surface placeholder:text-outline-variant focus:bg-surface-container-lowest focus:outline-none focus:ring-1 focus:ring-primary/20 transition-colors border-none disabled:opacity-60"
            />
          </div>

          <!-- Email -->
          <div class="flex flex-col gap-1.5">
            <label class="font-body text-sm font-medium text-on-surface-variant" for="email">Địa chỉ Email</label>
            <input
              v-model="email"
              id="email"
              name="email"
              type="email"
              placeholder="ban@example.com"
              required
              :disabled="loading"
              class="w-full rounded-lg bg-surface-container-high px-4 py-3.5 text-on-surface placeholder:text-outline-variant focus:bg-surface-container-lowest focus:outline-none focus:ring-1 focus:ring-primary/20 transition-colors border-none disabled:opacity-60"
            />
          </div>
          
          <!-- Password -->
          <div class="flex flex-col gap-1.5">
            <label class="font-body text-sm font-medium text-on-surface-variant" for="password">Mật khẩu</label>
            <div class="relative">
              <input
                v-model="password"
                id="password"
                name="password"
                :type="showPassword ? 'text' : 'password'"
                placeholder="Tối thiểu 8 ký tự"
                required
                :disabled="loading"
                class="w-full rounded-lg bg-surface-container-high px-4 py-3.5 pr-12 text-on-surface placeholder:text-outline-variant focus:bg-surface-container-lowest focus:outline-none focus:ring-1 focus:ring-primary/20 transition-colors border-none disabled:opacity-60"
              />
              <button
                type="button"
                @click="showPassword = !showPassword"
                tabindex="-1"
                class="absolute right-3 top-1/2 -translate-y-1/2 text-outline-variant hover:text-on-surface transition-colors p-1 flex items-center justify-center"
              >
                <span class="material-symbols-outlined text-[20px]">{{ showPassword ? 'visibility' : 'visibility_off' }}</span>
              </button>
            </div>
          </div>
          
          <!-- Target Level Chips -->
          <div class="flex flex-col gap-2.5 pt-1">
            <label class="font-body text-sm font-medium text-on-surface-variant">Mục tiêu hiện tại</label>
            <div class="flex flex-wrap gap-2">
              <button
                v-for="opt in levelOptions"
                :key="opt.value"
                type="button"
                @click="targetLevel = opt.value"
                :class="[
                  'flex-auto py-3 px-2 text-center cursor-pointer transition-all border rounded-lg font-body text-sm font-medium',
                  targetLevel === opt.value
                    ? 'bg-primary-container text-on-primary-container border-transparent'
                    : 'bg-surface-container-highest text-on-surface hover:bg-surface-variant border-transparent'
                ]"
              >
                {{ opt.label }}
              </button>
            </div>
          </div>
          
          <!-- Submit -->
          <div class="pt-4">
            <button
              type="submit"
              :disabled="loading"
              class="w-full rounded-full bg-primary text-on-primary py-4 font-body text-base font-medium hover:bg-gradient-to-br hover:from-primary hover:to-primary-container transition-all shadow-[0_8px_24px_rgba(45,52,53,0.06)] flex items-center justify-center gap-2 disabled:opacity-70 disabled:cursor-not-allowed"
            >
              <span v-if="loading" class="material-symbols-outlined animate-spin text-xl">progress_activity</span>
              <span v-else>Bắt đầu</span>
              <span v-if="!loading" class="material-symbols-outlined text-[18px]">arrow_forward</span>
            </button>
          </div>
        </form>
        
        <div class="mt-7 text-center">
          <p class="font-body text-sm text-on-surface-variant">
            Bạn đã có tài khoản? 
            <RouterLink to="/login" class="text-primary font-medium hover:text-primary-dim transition-colors ml-1">Đăng nhập</RouterLink>
          </p>
        </div>
      </div>
    </div>
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
