<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const email = ref('')
const password = ref('')

const handleLogin = () => {
  // TODO: Add auth logic
  router.push('/profile')
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
          <span class="material-symbols-outlined text-primary text-4xl" data-icon="spa">spa</span>
        </div>
        <h1 class="font-display text-4xl font-extrabold text-primary tracking-tight">ZenPulse AI</h1>
        <p class="font-body text-on-surface-variant mt-3 text-sm tracking-wide">Enter your sanctuary of learning.</p>
      </div>
      
      <!-- Login Card - Surface Lowest for subtle pop -->
      <div class="bg-surface-container-lowest rounded-xl p-10 shadow-[0_30px_60px_rgba(45,52,53,0.04)] relative">
        <form @submit.prevent="handleLogin" class="flex flex-col gap-6">
          <!-- Email Field -->
          <div class="flex flex-col gap-2">
            <label class="font-label text-sm text-on-surface font-medium ml-1" for="email">Email</label>
            <div class="relative group">
              <span class="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-outline-variant group-focus-within:text-primary transition-colors" data-icon="mail">mail</span>
              <input v-model="email" class="w-full bg-surface-container-high text-on-surface placeholder:text-outline-variant rounded-lg py-4 pl-12 pr-4 border-0 focus:ring-0 focus:bg-surface-container-lowest focus:shadow-[inset_0_0_0_1px_rgba(69,97,125,0.2)] transition-all outline-none" id="email" name="email" placeholder="student@dojo.com" type="email" required />
            </div>
          </div>
          
          <!-- Password Field -->
          <div class="flex flex-col gap-2">
            <div class="flex justify-between items-center ml-1">
              <label class="font-label text-sm text-on-surface font-medium" for="password">Password</label>
              <a class="font-label text-xs text-primary hover:text-primary-dim transition-colors" href="#">Forgot Password?</a>
            </div>
            <div class="relative group">
              <span class="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-outline-variant group-focus-within:text-primary transition-colors" data-icon="lock">lock</span>
              <input v-model="password" class="w-full bg-surface-container-high text-on-surface placeholder:text-outline-variant rounded-lg py-4 pl-12 pr-4 border-0 focus:ring-0 focus:bg-surface-container-lowest focus:shadow-[inset_0_0_0_1px_rgba(69,97,125,0.2)] transition-all outline-none" id="password" name="password" placeholder="••••••••" type="password" required />
            </div>
          </div>
          
          <!-- Primary Action -->
          <div class="mt-4">
            <button class="w-full bg-primary text-on-primary rounded-full py-4 font-headline font-semibold text-lg shadow-[0_20px_40px_rgba(45,52,53,0.06)] signature-gradient-hover transition-all duration-300 flex items-center justify-center gap-2" type="submit">
              Enter Dojo
              <span class="material-symbols-outlined text-xl" data-icon="arrow_forward">arrow_forward</span>
            </button>
          </div>
        </form>
      </div>
      
      <!-- Secondary Action -->
      <div class="text-center mt-6">
        <p class="font-body text-sm text-on-surface-variant">
          Beginning your journey? 
          <RouterLink to="/register" class="font-headline font-semibold text-primary hover:text-primary-dim ml-1 transition-colors">Sign Up</RouterLink>
        </p>
      </div>
    </main>
  </div>
</template>
