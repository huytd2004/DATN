<template>
  <div class="fixed top-0 bottom-0 right-0 left-0 md:left-64 z-[45] bg-surface flex flex-col font-body text-on-surface antialiased overflow-hidden">
    <!-- TopAppBar -->
    <header class="h-20 shrink-0 z-40 bg-[#f9f9f9]/80 dark:bg-slate-950/80 backdrop-blur-xl border-b border-outline-variant/10 font-headline font-medium px-8 md:px-12 flex items-center justify-between">
      <div class="flex items-center gap-4">
        <RouterLink to="/review" class="p-2 -ml-2 rounded-full hover:bg-surface-container transition-colors flex items-center justify-center">
          <span class="material-symbols-outlined text-on-surface-variant">arrow_back</span>
        </RouterLink>
        <h2 class="text-lg font-bold text-[#45617d] dark:text-sky-200">Review Center</h2>
      </div>
      <div class="flex items-center gap-4 md:gap-8">
        <div class="hidden md:flex relative items-center bg-surface-container-high rounded-full px-4 py-2 focus-within:ring-2 focus-within:ring-[#45617d]/20 transition-all">
          <span class="material-symbols-outlined text-on-surface-variant mr-2" data-icon="search">search</span>
          <input class="bg-transparent border-none focus:ring-0 text-sm w-48 font-body outline-none" placeholder="Tìm kiếm câu chuyện..." type="text"/>
        </div>
        <div class="flex items-center gap-4 text-[#45617d]">
          <span class="material-symbols-outlined hover:text-[#45617d] dark:hover:text-sky-200 cursor-pointer transition-colors" data-icon="local_fire_department">local_fire_department</span>
          <span class="material-symbols-outlined hover:text-[#45617d] dark:hover:text-sky-200 cursor-pointer transition-colors" data-icon="notifications">notifications</span>
        </div>
      </div>
    </header>

    <!-- Main Canvas Content -->
    <main class="flex-grow flex flex-col items-center bg-surface overflow-y-auto">
      <div class="w-full max-w-6xl h-full p-6 md:p-8 grid grid-cols-1 md:grid-cols-12 gap-8 md:gap-12 min-h-min">
        
        <!-- Left: Minimalist Visual Illustration -->
        <div class="md:col-span-12 lg:col-span-7 flex flex-col gap-6 h-full min-h-[400px]">
          <!-- Progress Indicator -->
          <div class="flex items-center gap-4 px-4 shrink-0">
            <div class="flex-1 h-2 bg-surface-container-highest rounded-full overflow-hidden">
              <div class="h-full bg-primary w-2/3 rounded-full"></div>
            </div>
            <span class="text-xs font-bold text-primary tracking-widest uppercase">Chương 3: Bữa tối ở Kyoto</span>
          </div>
          
          <!-- Illustration Card -->
          <div class="flex-1 min-h-0 bg-surface-container-lowest rounded-[2rem] overflow-hidden relative shadow-lg shadow-primary/5 border border-outline-variant/10 group">
            <img class="w-full h-full object-cover opacity-90 transition-transform duration-1000 group-hover:scale-105" src="https://lh3.googleusercontent.com/aida-public/AB6AXuCWp6svrcCQp81pH1ZxO1Pr-AMW3frWh2ZzLStbL5zIicAH6Mo4DMs7bCRNOHhmji8QCG-sU3xnOTlCKXtPK5QS4oOqhD_diQzrMJy9OJa1w85LeD__1hqBYubkGIT5QhUgDmx4cQeDX8q820TOC3ubj3rQnRRuqVCbAdnjdep6WYY9CPsgm9MFoTNOjF8wDU-moBZy3pQPKSQqnOsT38nKtNe1U5kMhpCU3DjMHxnFGddrIdE5wAujb3ZiaC7Wa0H9blAUeipynH8"/>
            <div class="absolute inset-0 bg-gradient-to-t from-black/40 via-transparent to-transparent"></div>
            
            <!-- Glassmorphic Character Popover -->
            <div class="absolute bottom-6 md:bottom-8 left-6 right-6 md:left-8 md:right-8 bg-white/70 dark:bg-slate-900/70 backdrop-blur-xl p-5 md:p-6 rounded-2xl flex items-start gap-4 border border-white/20 dark:border-white/10 shadow-xl">
              <div class="w-12 h-12 rounded-full bg-primary-container flex items-center justify-center shrink-0">
                <span class="material-symbols-outlined text-primary" data-icon="person">person</span>
              </div>
              <div>
                <span class="text-xs font-bold text-primary-dim uppercase tracking-wider block mb-1">Phục vụ</span>
                <p class="text-base md:text-lg font-headline font-semibold text-on-surface leading-tight">
                  いらっしゃいませ！何にしますか？<br/>
                  <span class="text-xs md:text-sm font-body font-normal text-on-surface-variant italic mt-1 inline-block">(Chào mừng quý khách! Quý khách muốn dùng gì ạ?)</span>
                </p>
              </div>
            </div>
          </div>
        </div>
        
        <!-- Right: Dialogue & Choices -->
        <div class="md:col-span-12 lg:col-span-5 flex flex-col justify-center gap-8 py-4">
          <!-- Story Narrative -->
          <div class="px-2 md:px-4">
            <h2 class="text-3xl md:text-4xl font-headline font-extrabold text-on-primary-fixed leading-tight mb-4">Gọi món bằng tiếng Nhật</h2>
            <p class="text-on-surface-variant text-base md:text-lg leading-relaxed font-body">
              Bạn vừa bước vào một quán Ramen nhỏ ấm cúng bên lề phố Kyoto. Mùi thơm của nước dùng bay nghi ngút. Người phục vụ mỉm cười và đợi yêu cầu của bạn.
            </p>
            <div class="mt-8 flex items-center gap-3">
              <span class="w-12 h-px bg-outline-variant/40"></span>
              <span class="text-xs text-outline font-semibold tracking-widest uppercase">Hãy chọn cách trả lời đúng</span>
              <span class="flex-1 h-px bg-outline-variant/40"></span>
            </div>
          </div>
          
          <!-- Choice Cards (Bento Grid Style) -->
          <div class="flex flex-col gap-4">
            <!-- Choice 1 (Correct) -->
            <button class="group flex items-center justify-between p-5 md:p-6 bg-surface-container-lowest hover:bg-primary-container transition-all duration-300 rounded-3xl text-left border border-outline-variant/10 hover:border-primary/30 active:scale-[0.98] shadow-sm hover:shadow-md">
              <div class="flex items-center gap-4 md:gap-5">
                <div class="w-10 h-10 shrink-0 rounded-xl bg-surface-container-highest group-hover:bg-primary flex items-center justify-center transition-colors">
                  <span class="text-sm font-bold group-hover:text-on-primary transition-colors">A</span>
                </div>
                <div>
                  <p class="text-base md:text-lg font-headline font-bold text-on-surface group-hover:text-on-primary-container transition-colors">ラーメンをください。</p>
                  <p class="text-xs md:text-sm text-on-surface-variant group-hover:text-on-primary-container/70 transition-colors mt-1">Ramen o kudasai (Cho tôi Ramen)</p>
                </div>
              </div>
              <span class="material-symbols-outlined text-outline-variant group-hover:text-primary transition-colors" data-icon="chevron_right">chevron_right</span>
            </button>
            
            <!-- Choice 2 -->
            <button class="group flex items-center justify-between p-5 md:p-6 bg-surface-container-lowest hover:bg-surface-container-high transition-all duration-300 rounded-3xl text-left border border-outline-variant/10 active:scale-[0.98] shadow-sm hover:shadow-md">
              <div class="flex items-center gap-4 md:gap-5">
                <div class="w-10 h-10 shrink-0 rounded-xl bg-surface-container-highest flex items-center justify-center">
                  <span class="text-sm font-bold">B</span>
                </div>
                <div>
                  <p class="text-base md:text-lg font-headline font-bold text-on-surface">ラーメンがありますか？</p>
                  <p class="text-xs md:text-sm text-on-surface-variant mt-1">Ramen ga arimasu ka? (Bạn có Ramen không?)</p>
                </div>
              </div>
              <span class="material-symbols-outlined text-outline-variant" data-icon="chevron_right">chevron_right</span>
            </button>
            
            <!-- Choice 3 -->
            <button class="group flex items-center justify-between p-5 md:p-6 bg-surface-container-lowest hover:bg-surface-container-high transition-all duration-300 rounded-3xl text-left border border-outline-variant/10 active:scale-[0.98] shadow-sm hover:shadow-md">
              <div class="flex items-center gap-4 md:gap-5">
                <div class="w-10 h-10 shrink-0 rounded-xl bg-surface-container-highest flex items-center justify-center">
                  <span class="text-sm font-bold">C</span>
                </div>
                <div>
                  <p class="text-base md:text-lg font-headline font-bold text-on-surface">ラーメンを食べた。</p>
                  <p class="text-xs md:text-sm text-on-surface-variant mt-1">Ramen o tabeta (Tôi đã ăn Ramen)</p>
                </div>
              </div>
              <span class="material-symbols-outlined text-outline-variant" data-icon="chevron_right">chevron_right</span>
            </button>
          </div>
          
          <!-- Contextual Hint -->
          <div class="mx-2 md:mx-4 p-5 bg-tertiary-container/30 border border-tertiary/10 rounded-2xl flex items-start gap-4 shadow-sm">
            <span class="material-symbols-outlined text-tertiary mt-1 shrink-0" data-icon="lightbulb">lightbulb</span>
            <p class="text-sm text-on-tertiary-container font-medium leading-relaxed">
              <strong class="block mb-1 text-tertiary">Mẹo nhỏ:</strong>
              Cấu trúc <strong class="bg-white/50 px-1.5 py-0.5 rounded">[Danh từ] + をください (o kudasai)</strong> là cách lịch sự và phổ biến nhất để gọi món hoặc yêu cầu một món đồ tại Nhật Bản.
            </p>
          </div>
        </div>
      </div>
    </main>

    <!-- Floating Actions (Translation & Next) -->
    <div class="fixed bottom-6 right-6 md:bottom-10 md:right-10 flex gap-4 z-50">
      <button class="w-14 h-14 bg-surface-container-lowest text-primary shadow-xl rounded-full flex items-center justify-center hover:scale-110 active:scale-95 transition-all border border-outline-variant/10 hover:shadow-primary/20">
        <span class="material-symbols-outlined" data-icon="translate">translate</span>
      </button>
      <button class="bg-gradient-to-r from-primary to-primary-dim text-white px-8 py-4 rounded-full font-bold shadow-xl hover:opacity-90 active:scale-95 transition-all flex items-center gap-3 hover:shadow-primary/30">
        <span>Tiếp theo</span>
        <span class="material-symbols-outlined" data-icon="arrow_forward">arrow_forward</span>
      </button>
    </div>
  </div>
</template>

<script setup>
</script>
