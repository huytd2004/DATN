<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const name = ref('')
const email = ref('')
const password = ref('')
const goal = ref('JLPT N3+')

const handleRegister = () => {
  // TODO: Add auth logic
  router.push('/profile')
}

const goals = ['JLPT N5', 'JLPT N4', 'JLPT N3+']
</script>

<template>
  <div class="bg-surface font-body text-on-surface antialiased min-h-screen flex flex-col md:flex-row overflow-hidden selection:bg-primary-container selection:text-on-primary-container">
    <!-- Left Visual Pane: The Meditative Space -->
    <div class="hidden md:flex w-full md:w-5/12 lg:w-1/2 relative bg-surface-container flex-col justify-between p-12 overflow-hidden">
      <div class="absolute inset-0 z-0">
        <img class="w-full h-full object-cover opacity-80 mix-blend-multiply" data-alt="soft sunlight filtering through lush green bamboo forest canopy, creating dappled komorebi patterns on the ground, tranquil cinematic lighting, shallow depth of field" src="https://lh3.googleusercontent.com/aida-public/AB6AXuB4Pn1pPiLTa1QfP5FSzBo8MPtFlCkhOPgLlxJnbN20d0Z9twXI3pm-GigMa-5xx8oD43WUAtLI51GpSPiMCtpd0nKnLGFxgfyoquswje1MycrIcVknj6Q3IJMVmxnIH7ihYYimyZbzGWzqoaeQhKJv0mDZvkez3npy6-Ts4tORkAQJESQ4tB7Hrmxx1mIqMSTm2q4L7oI8qnJ2a_ONXxDVTa6kLkhF_WVosJ-lC8RsjJUfTz8W4rxZfgElkHMdqQAkMEhhNKn7BIg"/>
      </div>
      <div class="absolute inset-0 bg-gradient-to-t from-surface-container/90 via-surface-container/20 to-transparent z-10"></div>
      <div class="absolute inset-0 bg-primary/5 mix-blend-overlay z-10"></div>
      
      <div class="z-20 relative">
        <h1 class="font-headline text-2xl font-bold text-on-primary-fixed tracking-tight">ZenPulse AI</h1>
      </div>
      
      <div class="z-20 relative max-w-md">
        <p class="font-headline text-4xl text-on-primary-fixed font-light leading-tight mb-4">
          Master Japanese with focused intent.
        </p>
        <p class="font-body text-on-surface-variant text-base leading-relaxed">
          Step into a quiet space designed for deep learning. Adaptive AI meets traditional serenity.
        </p>
      </div>
    </div>
    
    <!-- Right Interaction Pane: The Canvas -->
    <div class="w-full md:w-7/12 lg:w-1/2 flex items-center justify-center p-6 md:p-12 lg:p-24 relative bg-surface">
      <!-- Ambient Background Depth -->
      <div class="absolute top-0 right-0 w-96 h-96 bg-primary-fixed/20 rounded-full blur-[80px] pointer-events-none -translate-y-1/2 translate-x-1/3"></div>
      <div class="absolute bottom-0 left-0 w-[500px] h-[500px] bg-secondary-fixed/10 rounded-full blur-[100px] pointer-events-none translate-y-1/3 -translate-x-1/4"></div>
      
      <!-- The Floating Card -->
      <div class="w-full max-w-[440px] bg-surface-container-lowest rounded-xl p-8 md:p-10 shadow-[0_24px_60px_rgba(45,52,53,0.04)] relative z-10">
        <div class="mb-10 text-center md:text-left">
          <h2 class="font-headline text-3xl font-light text-on-surface mb-2">Begin Your Journey</h2>
          <p class="font-body text-sm text-on-surface-variant">Create your personalized dojo.</p>
        </div>
        
        <form @submit.prevent="handleRegister" class="space-y-6">
          <!-- Input: Full Name -->
          <div class="flex flex-col gap-1.5">
            <label class="font-body text-sm font-medium text-on-surface-variant" for="name">Full Name</label>
            <input v-model="name" class="w-full rounded-lg bg-surface-container-high px-4 py-3.5 text-on-surface placeholder:text-outline-variant focus:bg-surface-container-lowest focus:outline-none focus:ring-1 focus:ring-primary/20 transition-colors border-none" id="name" name="name" placeholder="Enter your name" type="text" required />
          </div>
          
          <!-- Input: Email -->
          <div class="flex flex-col gap-1.5">
            <label class="font-body text-sm font-medium text-on-surface-variant" for="email">Email Address</label>
            <input v-model="email" class="w-full rounded-lg bg-surface-container-high px-4 py-3.5 text-on-surface placeholder:text-outline-variant focus:bg-surface-container-lowest focus:outline-none focus:ring-1 focus:ring-primary/20 transition-colors border-none" id="email" name="email" placeholder="you@example.com" type="email" required />
          </div>
          
          <!-- Input: Password -->
          <div class="flex flex-col gap-1.5">
            <label class="font-body text-sm font-medium text-on-surface-variant" for="password">Password</label>
            <div class="relative">
              <input v-model="password" class="w-full rounded-lg bg-surface-container-high px-4 py-3.5 pr-12 text-on-surface placeholder:text-outline-variant focus:bg-surface-container-lowest focus:outline-none focus:ring-1 focus:ring-primary/20 transition-colors border-none" id="password" name="password" placeholder="Min. 8 characters" type="password" required />
              <button class="absolute right-3 top-1/2 -translate-y-1/2 text-outline-variant hover:text-on-surface transition-colors p-1 flex items-center justify-center" type="button">
                <span class="material-symbols-outlined text-[20px]">visibility_off</span>
              </button>
            </div>
          </div>
          
          <!-- Selection Chips: Learning Goal -->
          <div class="flex flex-col gap-2.5 pt-2">
            <label class="font-body text-sm font-medium text-on-surface-variant">Current Goal</label>
            <div class="flex gap-3">
              <div 
                v-for="g in goals" 
                :key="g"
                @click="goal = g"
                :class="[
                  'flex-1 py-3 px-2 text-center cursor-pointer transition-all border rounded-lg',
                  goal === g 
                    ? 'bg-primary-container text-on-primary-container border-transparent' 
                    : 'bg-surface-container-highest text-on-surface hover:bg-surface-variant border-transparent'
                ]"
              >
                <span class="font-body text-sm font-medium">{{ g }}</span>
              </div>
            </div>
          </div>
          
          <!-- Actions -->
          <div class="pt-6">
            <button type="submit" class="w-full rounded-full bg-primary text-on-primary py-4 font-body text-base font-medium hover:bg-gradient-to-br hover:from-primary hover:to-primary-container transition-all shadow-[0_8px_24px_rgba(45,52,53,0.06)] flex items-center justify-center gap-2">
              <span>Enter the Dojo</span>
              <span class="material-symbols-outlined text-[18px]">arrow_forward</span>
            </button>
          </div>
        </form>
        
        <div class="mt-8 text-center">
          <p class="font-body text-sm text-on-surface-variant">
            Already walking the path? 
            <RouterLink to="/login" class="text-primary font-medium hover:text-primary-dim transition-colors ml-1">Sign in</RouterLink>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>
