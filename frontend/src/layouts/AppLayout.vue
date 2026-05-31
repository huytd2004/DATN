<template>
  <div class="bg-background text-on-surface min-h-screen flex flex-col">
    <!-- TopNavBar -->
    <header class="fixed top-0 right-0 w-[calc(100%-16rem)] h-16 z-40 bg-white/80 dark:bg-slate-950/80 backdrop-blur-xl flex justify-end items-center px-8 gap-4 shadow-[0_4px_20px_-10px_rgba(0,0,0,0.05)] font-['Plus_Jakarta_Sans'] text-sm tracking-wide hidden md:flex">
      <div class="flex items-center gap-4">
        <button class="hover:bg-slate-100 dark:hover:bg-slate-800 rounded-full p-2 transition-all opacity-80 hover:opacity-100">
          <span class="material-symbols-outlined text-slate-700 dark:text-slate-300">notifications</span>
        </button>
        <button class="hover:bg-slate-100 dark:hover:bg-slate-800 rounded-full p-2 transition-all opacity-80 hover:opacity-100">
          <span class="material-symbols-outlined text-slate-700 dark:text-slate-300">settings</span>
        </button>
        <button
          @click="handleLogout"
          title="Đăng xuất"
          class="hover:bg-red-50 dark:hover:bg-red-900/20 rounded-full p-2 transition-all opacity-70 hover:opacity-100 group"
        >
          <span class="material-symbols-outlined text-slate-500 group-hover:text-red-500 transition-colors">logout</span>
        </button>
        <div class="h-8 w-px bg-outline-variant/20 mx-2"></div>
        <RouterLink to="/profile" class="flex items-center gap-3 cursor-pointer group">
          <div class="text-right">
            <p class="font-semibold text-on-surface leading-tight">{{ authStore.user?.username || authStore.user?.email || 'Người dùng' }}</p>
            <p class="text-[10px] text-on-surface-variant uppercase tracking-widest">{{ authStore.user?.targetLevel ? 'JLPT ' + authStore.user.targetLevel : 'Learner' }}</p>
          </div>
          <div class="w-10 h-10 rounded-full overflow-hidden border-2 border-primary-container group-hover:border-primary transition-colors">
            <img alt="User Profile" class="w-full h-full object-cover" src="https://lh3.googleusercontent.com/aida-public/AB6AXuCnKL9LPHGHjrylfSG3-ynVSdRt-_67Y32PYOlIDoD3El9_sW35UlVUExYwFUWGSxk9zYzfGEYd9O5uEbpDZQIyPKKwA_PYRk4I6Sq_OMP-1CH_IqlgmWKyq0mS5m9UP8gtd4E9A3MKhPlRX6lOE0M_-TUrdm8zxt2_Lgvjg4DduxBULd-DcJg49RbLOkZ0FZbwN9YUnqd_4waWT9sraiV3IFM2REjbpI6qA2pmLNW3LdnOqleYtYzQ0gQ-kwN4JYS8pXk1__wvY6g">
          </div>
        </RouterLink>
      </div>
    </header>

    <!-- SideNavBar -->
    <aside class="h-screen w-64 flex-col fixed left-0 top-0 bg-slate-50 dark:bg-slate-900 font-['Plus_Jakarta_Sans'] antialiased py-6 space-y-2 z-50 hidden md:flex">
      <div class="px-6 mb-10">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-primary to-primary-dim flex items-center justify-center text-on-primary shadow-sm">
            <span class="material-symbols-outlined" style="font-variation-settings: 'FILL' 1;">spa</span>
          </div>
          <div>
            <h1 class="text-xl font-bold tracking-tight text-slate-800 dark:text-slate-100">EasyNihongo</h1>
            <p class="text-xs text-slate-500 font-medium">Study Zen</p>
          </div>
        </div>
      </div>
      <nav class="flex flex-col h-full">
        <!-- Links -->
        <RouterLink to="/dashboard" active-class="bg-gradient-to-br from-blue-100 to-blue-50 dark:from-blue-900/40 dark:to-blue-800/20 text-blue-900 dark:text-blue-200" class="text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-100 font-semibold rounded-xl mx-2 shadow-sm px-4 py-3 flex items-center gap-3 transition-all duration-200 ease-out">
          <span class="material-symbols-outlined">dashboard</span>
          <span>Dashboard</span>
        </RouterLink>
        <RouterLink to="/dictionary" active-class="bg-gradient-to-br from-blue-100 to-blue-50 dark:from-blue-900/40 dark:to-blue-800/20 text-blue-900 dark:text-blue-200" class="text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-100 px-4 py-3 mx-2 transition-colors hover:bg-slate-200/50 dark:hover:bg-slate-800 rounded-xl flex items-center gap-3">
          <span class="material-symbols-outlined">search</span>
          <span>Tra cứu</span>
        </RouterLink>
        <RouterLink to="/translate" active-class="bg-gradient-to-br from-blue-100 to-blue-50 dark:from-blue-900/40 dark:to-blue-800/20 text-blue-900 dark:text-blue-200" class="text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-100 px-4 py-3 mx-2 transition-colors hover:bg-slate-200/50 dark:hover:bg-slate-800 rounded-xl flex items-center gap-3">
          <span class="material-symbols-outlined">translate</span>
          <span>Dịch thuật</span>
        </RouterLink>
        <RouterLink to="/flashcards" active-class="bg-gradient-to-br from-blue-100 to-blue-50 dark:from-blue-900/40 dark:to-blue-800/20 text-blue-900 dark:text-blue-200" class="text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-100 px-4 py-3 mx-2 transition-colors hover:bg-slate-200/50 dark:hover:bg-slate-800 rounded-xl flex items-center gap-3">
          <span class="material-symbols-outlined">style</span>
          <span>Flashcards</span>
        </RouterLink>
        <RouterLink to="/tutor" active-class="bg-gradient-to-br from-blue-100 to-blue-50 dark:from-blue-900/40 dark:to-blue-800/20 text-blue-900 dark:text-blue-200" class="text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-100 px-4 py-3 mx-2 transition-colors hover:bg-slate-200/50 dark:hover:bg-slate-800 rounded-xl flex items-center gap-3">
          <span class="material-symbols-outlined" style="font-variation-settings: 'FILL' 1;">psychology</span>
          <span>AI Tutor</span>
        </RouterLink>
        <RouterLink to="/review" active-class="bg-gradient-to-br from-blue-100 to-blue-50 dark:from-blue-900/40 dark:to-blue-800/20 text-blue-900 dark:text-blue-200" class="text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-100 px-4 py-3 mx-2 transition-colors hover:bg-slate-200/50 dark:hover:bg-slate-800 rounded-xl flex items-center gap-3">
          <span class="material-symbols-outlined">rebase_edit</span>
          <span>Ôn tập</span>
        </RouterLink>
        
        <div class="mt-auto px-4 pb-4">
          <div class="p-4 rounded-2xl bg-surface-container-low border border-outline-variant/10">
            <p class="text-xs font-medium text-on-surface-variant mb-2">PRO PLAN</p>
            <p class="text-sm text-on-surface mb-3">Mở khóa 2000+ Kanji nâng cao.</p>
            <button class="w-full py-2 bg-primary text-on-primary rounded-full text-xs font-bold shadow-sm">Nâng cấp ngay</button>
          </div>
        </div>
      </nav>
    </aside>


    <main class="flex-1 md:ml-64 pt-20 pb-12 px-4 md:px-8">
      <RouterView />
    </main>

    <!-- Footer -->
<footer class="md:ml-64 bg-slate-50 dark:bg-slate-900 border-t border-slate-200/10 dark:border-slate-800/10 py-12">
<div class="flex flex-col items-center justify-center gap-6 w-full max-w-screen-xl mx-auto px-8">
<div class="flex gap-8 font-['Plus_Jakarta_Sans'] text-xs uppercase tracking-widest font-semibold">
<a class="text-slate-400 hover:text-slate-800 transition-colors" href="#">Privacy</a>
<a class="text-slate-400 hover:text-slate-800 transition-colors" href="#">Terms</a>
<a class="text-slate-400 hover:text-slate-800 transition-colors" href="#">Support</a>
<a class="text-slate-400 hover:text-slate-800 transition-colors" href="#">Contact</a>
</div>
<p class="text-slate-400 dark:text-slate-500 font-['Plus_Jakarta_Sans'] text-xs uppercase tracking-widest">© 2024 THE MEDITATIVE CANVAS. ALL RIGHTS RESERVED.</p>
</div>
</footer>

    <!-- Mobile Navigation (BottomNavBar) -->
<nav class="md:hidden fixed bottom-0 left-0 right-0 glass-panel border-t border-outline-variant/10 px-6 py-3 flex justify-between items-center z-50">
<button class="flex flex-col items-center gap-1 text-outline">
<span class="material-symbols-outlined">menu_book</span>
<span class="text-[10px] uppercase font-bold">Học</span>
</button>
<button class="flex flex-col items-center gap-1 text-primary">
<span class="material-symbols-outlined" style="font-variation-settings: 'FILL' 1;">search</span>
<span class="text-[10px] uppercase font-bold">Tra cứu</span>
</button>
<button class="w-12 h-12 sakura-gradient rounded-full flex items-center justify-center -translate-y-6 shadow-lg shadow-primary/30">
<span class="material-symbols-outlined text-white">draw</span>
</button>
<button class="flex flex-col items-center gap-1 text-outline">
<span class="material-symbols-outlined">layers</span>
<span class="text-[10px] uppercase font-bold">Thẻ</span>
</button>
        <RouterLink to="/profile" active-class="text-primary" class="flex flex-col items-center gap-1 text-outline">
          <span class="material-symbols-outlined" style="font-variation-settings: 'FILL' 1;">person</span>
          <span class="text-[10px] uppercase font-bold">Tôi</span>
        </RouterLink>
</nav>

  </div>
</template>

<script setup>
import { RouterView, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const router = useRouter()

async function handleLogout() {
  await authStore.logout()
  router.push('/login')
}
</script>
