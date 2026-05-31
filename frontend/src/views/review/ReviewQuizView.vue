<template>
  <div class="fixed top-0 bottom-0 right-0 left-0 md:left-64 z-[45] bg-surface flex flex-col font-body text-on-surface antialiased overflow-hidden">
    <!-- TopAppBar -->
    <header class="h-20 shrink-0 z-40 bg-[#f9f9f9]/80 dark:bg-slate-950/80 backdrop-blur-xl border-b border-outline-variant/10 font-headline font-medium px-8 md:px-12 flex items-center justify-between">
      <div class="flex items-center gap-4">
        <RouterLink to="/review" class="p-2 -ml-2 rounded-full hover:bg-surface-container transition-colors flex items-center justify-center">
          <span class="material-symbols-outlined text-on-surface-variant">arrow_back</span>
        </RouterLink>
        <h2 class="text-lg font-bold text-[#45617d] dark:text-sky-200">Review Center</h2>
        <div class="hidden sm:block h-1 w-32 md:w-48 bg-surface-container-highest rounded-full overflow-hidden ml-4 md:ml-8">
          <div class="h-full bg-primary w-3/5 rounded-full"></div>
        </div>
        <span class="hidden sm:inline-block text-xs font-bold text-on-surface-variant tracking-wider uppercase ml-2">Progress: 12/20</span>
      </div>
      <div class="flex items-center gap-4 md:gap-6">
        <button class="flex items-center gap-2 text-secondary font-medium px-3 py-1.5 rounded-full hover:bg-secondary-container transition-colors">
          <span class="material-symbols-outlined text-xl" style="font-variation-settings: 'FILL' 1;">local_fire_department</span>
          <span class="hidden sm:inline">Streak: 5</span>
        </button>
        <div class="flex gap-4 hidden sm:flex">
          <span class="material-symbols-outlined text-[#45617d] cursor-pointer hover:opacity-70">notifications</span>
        </div>
      </div>
    </header>

    <!-- Quiz Content Section -->
    <section class="flex-grow flex items-center justify-center p-6 md:p-12 overflow-y-auto">
      <div class="max-w-4xl w-full flex flex-col gap-8 md:gap-12">
        <!-- The Question Display -->
        <div class="text-center">
          <span class="inline-block px-4 py-1.5 rounded-full bg-primary-container text-on-primary-container text-xs font-bold mb-8 tracking-widest uppercase">Ngữ pháp N3</span>
          <div class="text-3xl md:text-5xl font-headline font-semibold text-on-surface leading-relaxed" style="line-height: 1.8; letter-spacing: 0.05em;">
            雨が降って<span class="mx-2 md:mx-4 px-6 md:px-8 py-1 border-b-4 border-primary text-primary/30 inline-block align-middle bg-surface-container-low rounded-t-xl transition-all">___</span>から、傘を持っていったほうがいいですよ。
          </div>
          <p class="mt-8 text-on-surface-variant text-base md:text-lg italic opacity-70">"Vì trời đang mưa, nên mang theo ô thì hơn đấy."</p>
        </div>

        <!-- Answer Grid -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4 md:gap-6">
          <!-- Option 1 -->
          <button class="group flex items-center p-4 md:p-6 bg-surface-container-lowest rounded-2xl transition-all duration-300 hover:bg-primary-container border-2 border-transparent hover:border-primary/20 active:scale-[0.98] shadow-sm hover:shadow-md">
            <div class="w-10 h-10 shrink-0 rounded-full bg-surface-container flex items-center justify-center font-bold text-on-surface-variant group-hover:bg-primary group-hover:text-on-primary transition-colors">A</div>
            <span class="ml-4 md:ml-6 text-lg md:text-xl font-medium text-on-surface" style="line-height: 1.8; letter-spacing: 0.05em;">いる</span>
          </button>
          
          <!-- Option 2 -->
          <button class="group flex items-center p-4 md:p-6 bg-surface-container-lowest rounded-2xl transition-all duration-300 hover:bg-primary-container border-2 border-transparent hover:border-primary/20 active:scale-[0.98] shadow-sm hover:shadow-md">
            <div class="w-10 h-10 shrink-0 rounded-full bg-surface-container flex items-center justify-center font-bold text-on-surface-variant group-hover:bg-primary group-hover:text-on-primary transition-colors">B</div>
            <span class="ml-4 md:ml-6 text-lg md:text-xl font-medium text-on-surface" style="line-height: 1.8; letter-spacing: 0.05em;">いた</span>
          </button>
          
          <!-- Option 3 (Selected State Simulation) -->
          <button class="group flex items-center p-4 md:p-6 bg-primary-container rounded-2xl transition-all duration-300 ring-2 ring-primary border-2 border-transparent active:scale-[0.98] shadow-md shadow-primary/20">
            <div class="w-10 h-10 shrink-0 rounded-full bg-primary flex items-center justify-center font-bold text-on-primary">C</div>
            <span class="ml-4 md:ml-6 text-lg md:text-xl font-semibold text-on-primary-container" style="line-height: 1.8; letter-spacing: 0.05em;">きた</span>
          </button>
          
          <!-- Option 4 -->
          <button class="group flex items-center p-4 md:p-6 bg-surface-container-lowest rounded-2xl transition-all duration-300 hover:bg-primary-container border-2 border-transparent hover:border-primary/20 active:scale-[0.98] shadow-sm hover:shadow-md">
            <div class="w-10 h-10 shrink-0 rounded-full bg-surface-container flex items-center justify-center font-bold text-on-surface-variant group-hover:bg-primary group-hover:text-on-primary transition-colors">D</div>
            <span class="ml-4 md:ml-6 text-lg md:text-xl font-medium text-on-surface" style="line-height: 1.8; letter-spacing: 0.05em;">おわる</span>
          </button>
        </div>

        <!-- Footer Actions -->
        <div class="flex flex-col-reverse sm:flex-row items-center justify-between mt-4 md:mt-8 gap-6">
          <!-- AI Hint Trigger -->
          <button class="flex items-center gap-3 px-6 py-4 bg-surface-container-high rounded-full text-primary font-semibold hover:bg-surface-container-highest transition-all group shadow-sm">
            <div class="w-8 h-8 rounded-full bg-primary/10 flex items-center justify-center group-hover:bg-primary group-hover:text-on-primary transition-all">
              <span class="material-symbols-outlined text-lg" style="font-variation-settings: 'FILL' 1;">auto_awesome</span>
            </div>
            <span>Gợi ý từ AI Komorebi</span>
          </button>
          
          <div class="flex gap-4 w-full sm:w-auto">
            <button class="flex-1 sm:flex-none px-6 md:px-8 py-4 rounded-full font-bold text-on-surface-variant hover:bg-surface-container transition-all text-center">Bỏ qua</button>
            <button class="flex-1 sm:flex-none px-8 md:px-12 py-4 bg-primary text-on-primary rounded-full font-bold text-lg shadow-xl shadow-primary/20 hover:scale-105 transition-all text-center">Kiểm tra</button>
          </div>
        </div>
      </div>
    </section>

    <!-- Floating Decorative Element (Komorebi Vibe) -->
    <div class="absolute bottom-10 right-10 pointer-events-none opacity-20 hidden lg:block">
      <img alt="Japanese ink painting" class="w-64 h-auto" src="https://lh3.googleusercontent.com/aida-public/AB6AXuCdQ9h1fUsf_eXEPWn_3GVcuqswDeRA5vpBq_S515lSZZZGuH4zNzokFXGBpfNKzF-EbSPMbjhjiVNMhYXYrHOU3zYf2WzTX0cd0be_XjgDBObPtO6svzyWhAdcvxxSnZTh2OZoMOsay3SSVp-108xdQznaUNshxb0AEVaeSIe8r7FND8lZCMgeDwHPF4lejLiqbTPqBmyuY8JgbzHSyVfkfacSHk2XQpY3X2zVbo1lVzRarIfALX8ZJcuSJmVA4ryV2tnsEqOGboA"/>
    </div>

    <!-- AI Hint Overlay Example (Visible for demonstration) -->
    <div class="fixed bottom-24 left-1/2 -translate-x-1/2 w-full max-w-[90%] md:max-w-lg p-0 z-50 animate-in fade-in slide-in-from-bottom-8 duration-500">
      <div class="bg-white/80 dark:bg-slate-900/80 backdrop-blur-xl p-6 rounded-[2rem] shadow-2xl border border-white/40 dark:border-white/10 flex gap-5 items-start">
        <div class="w-12 h-12 rounded-2xl bg-gradient-to-br from-primary to-primary-dim flex items-center justify-center flex-shrink-0 shadow-lg shadow-primary/20">
          <span class="material-symbols-outlined text-white" style="font-variation-settings: 'FILL' 1;">psychology</span>
        </div>
        <div>
          <h4 class="font-headline font-bold text-on-primary-container dark:text-sky-200 mb-1">Gợi ý ngữ pháp</h4>
          <p class="text-sm text-on-surface-variant leading-relaxed">Trong cấu trúc <span class="font-bold text-primary dark:text-sky-300">V-て + くる/きた</span>, nó diễn tả một hành động hoặc trạng thái bắt đầu và tiếp diễn cho đến hiện tại. Ở đây dùng để chỉ cơn mưa đang bắt đầu kéo tới.</p>
        </div>
        <button class="text-on-surface-variant hover:text-error transition-colors p-1 -mt-1 -mr-1">
          <span class="material-symbols-outlined">close</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
</script>
