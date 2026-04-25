<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const handleLogout = () => {
  // TODO: Add logout logic
  router.push('/login')
}
</script>

<template>
  <div class="bg-surface text-on-surface antialiased min-h-screen flex flex-col pt-16 pb-24 md:pb-0">
    <!-- TopAppBar (Web & Mobile Header Shell) -->
    <header class="fixed top-0 w-full z-50 bg-slate-50/80 dark:bg-slate-900/80 backdrop-blur-xl text-blue-800 dark:text-blue-300 font-['Plus_Jakarta_Sans'] tracking-tight border-b border-slate-100 dark:border-slate-800">
      <div class="flex justify-between items-center px-6 h-16 w-full max-w-7xl mx-auto">
        <div class="text-xl font-bold text-blue-900 dark:text-blue-100">
          ZenPulse AI
        </div>
        <!-- Web Navigation Cluster (Hidden on Mobile) -->
        <nav class="hidden md:flex space-x-8 items-center">
          <a class="text-slate-500 dark:text-slate-400 hover:bg-slate-200/50 dark:hover:bg-slate-800/50 transition-colors px-3 py-2 rounded-lg flex items-center gap-2" href="#">
            <span class="material-symbols-outlined text-lg">menu_book</span>
            <span class="font-medium text-sm">Learn</span>
          </a>
          <a class="text-slate-500 dark:text-slate-400 hover:bg-slate-200/50 dark:hover:bg-slate-800/50 transition-colors px-3 py-2 rounded-lg flex items-center gap-2" href="#">
            <span class="material-symbols-outlined text-lg">emoji_events</span>
            <span class="font-medium text-sm">Goals</span>
          </a>
          <a class="text-slate-500 dark:text-slate-400 hover:bg-slate-200/50 dark:hover:bg-slate-800/50 transition-colors px-3 py-2 rounded-lg flex items-center gap-2" href="#">
            <span class="material-symbols-outlined text-lg">bar_chart</span>
            <span class="font-medium text-sm">Progress</span>
          </a>
          <RouterLink to="/profile" class="text-blue-700 dark:text-blue-400 font-semibold hover:bg-slate-200/50 dark:hover:bg-slate-800/50 transition-colors px-3 py-2 rounded-lg flex items-center gap-2">
            <span class="material-symbols-outlined text-lg" style="font-variation-settings: 'FILL' 1;">person</span>
            <span class="text-sm">Profile</span>
          </RouterLink>
        </nav>
        <div class="flex items-center gap-4">
          <button class="text-slate-500 dark:text-slate-400 hover:bg-slate-200/50 dark:hover:bg-slate-800/50 transition-colors p-2 rounded-full">
            <span class="material-symbols-outlined">settings</span>
          </button>
          <img alt="User profile photo" class="w-8 h-8 rounded-full object-cover ghost-border" src="https://lh3.googleusercontent.com/aida-public/AB6AXuAxkgVzkxE77et6XnzItlINforFfWaqdiwDEt56WEJhOy7bIV3_yrYc6BYKRxM8FJ8XKd212Xx7PhohgoZoo-IFjko_guF1CrlP4JfpedeXb7DwgOocjvpr25cX1a3V9hy3g2ATCvR2R9ioiQdtMO6T-EWcc9FCmfNheJFdBgYCzR2HbF8zHmtFX4UwSVDtQ7qpj29gDRJ9hv1Htg02NqFQqsDpAx6Gw1JRrFuuFtJ-cXvOVxqG3uM_TDek6BdUFawKVxkk6lkqmPs"/>
        </div>
      </div>
    </header>

    <!-- Main Content Canvas -->
    <main class="flex-1 w-full max-w-7xl mx-auto px-6 py-12 md:py-24">
      <!-- Profile Header Section (Asymmetric Layout) -->
      <section class="flex flex-col md:flex-row items-start md:items-center justify-between gap-12 mb-16">
        <div class="flex items-center gap-8">
          <div class="relative">
            <div class="w-32 h-32 md:w-40 md:h-40 rounded-full overflow-hidden ambient-shadow">
              <img alt="Minh Hoàng" class="w-full h-full object-cover" src="https://lh3.googleusercontent.com/aida-public/AB6AXuDjZ8Sf-kUF0qvs6wWDee_n0Pg0LtU6orl0Vlz4iaZ6hUi6MyidRSgfAK7mFvXvFn4XBGeVNnbpYk7Q1AvFL02HLRFNAXAusymE1pBsp-t5Pljm2Ov9PbceB5RK_2bZ5hE-9FIPBzftFl6T-AQQk4GR3Pp_Mb2kOamGrQgFkE7fHm1A7fmQeGTWTrcaVPew_Sum_wIRSo_NsXfNi3OxlTUm9_d-t7Wt17_6mU5AYyTE9qRmUIU_PHVjrLDuVnOSeXqgtHOa-GEuTS8"/>
            </div>
            <div class="absolute bottom-2 right-2 bg-surface-container-lowest rounded-full p-2 ambient-shadow cursor-pointer hover:bg-surface-container transition-colors">
              <span class="material-symbols-outlined text-primary text-xl">edit</span>
            </div>
          </div>
          <div>
            <h1 class="font-headline text-4xl md:text-5xl font-bold text-on-surface tracking-tight mb-2">Minh Hoàng</h1>
            <div class="flex items-center gap-3">
              <span class="px-4 py-1.5 bg-primary-container text-on-primary-container rounded-full text-sm font-medium font-body flex items-center gap-2">
                <span class="material-symbols-outlined text-sm" style="font-variation-settings: 'FILL' 1;">flag</span>
                JLPT N3 Goal
              </span>
            </div>
          </div>
        </div>
        <!-- Quick Actions -->
        <div class="flex gap-4 self-start md:self-center">
          <button class="px-6 py-3 bg-surface-container-low text-on-surface-variant font-medium rounded-full hover:bg-surface-variant transition-colors flex items-center gap-2">
            <span class="material-symbols-outlined text-lg">share</span>
            Share Profile
          </button>
        </div>
      </section>

      <!-- Stats & Target Level Row -->
      <section class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-12">
        <!-- Target Level Card -->
        <div class="bg-surface-container-lowest rounded-xl p-8 ambient-shadow flex flex-col justify-between group overflow-hidden relative">
          <div class="absolute -right-8 -top-8 w-32 h-32 bg-primary/5 rounded-full blur-2xl group-hover:bg-primary/10 transition-colors"></div>
          <div class="relative z-10">
            <div class="flex items-center gap-3 mb-6">
              <div class="p-3 bg-primary-container text-on-primary-container rounded-full">
                <span class="material-symbols-outlined">star_rate</span>
              </div>
              <h3 class="font-headline text-lg font-semibold text-on-surface">Target Level</h3>
            </div>
            <div>
              <span class="font-display text-6xl font-bold text-primary">N3</span>
              <p class="text-sm text-on-surface-variant mt-2">Intermediate Proficiency</p>
            </div>
            <div class="mt-6 flex items-center gap-2">
              <div class="flex-1 h-2 bg-surface-container rounded-full overflow-hidden">
                <div class="h-full bg-primary rounded-full" style="width: 65%;"></div>
              </div>
              <span class="text-xs font-semibold text-primary">65%</span>
            </div>
          </div>
        </div>

        <!-- Streak Card -->
        <div class="bg-surface-container-lowest rounded-xl p-8 ambient-shadow relative overflow-hidden group">
          <div class="absolute top-0 right-0 w-32 h-32 bg-secondary-fixed-dim/20 rounded-full blur-3xl -mr-10 -mt-10 transition-transform group-hover:scale-150 duration-700"></div>
          <div class="flex flex-col h-full relative z-10">
            <div class="flex items-center gap-3 mb-6">
              <div class="p-3 bg-secondary-container text-on-secondary-container rounded-full">
                <span class="material-symbols-outlined" style="font-variation-settings: 'FILL' 1;">local_fire_department</span>
              </div>
              <h3 class="font-headline text-lg font-semibold text-on-surface">Current Streak</h3>
            </div>
            <div class="mt-auto">
              <div class="flex items-baseline gap-2">
                <span class="font-display text-6xl font-bold text-secondary">42</span>
                <span class="text-on-surface-variant font-medium">days</span>
              </div>
              <p class="text-sm text-on-surface-variant mt-2 leading-relaxed">Discipline is your strength.</p>
            </div>
          </div>
        </div>

        <!-- Focus Time Card -->
        <div class="bg-surface-container-lowest rounded-xl p-8 ambient-shadow">
          <div class="flex flex-col h-full">
            <div class="flex items-center justify-between mb-6">
              <div class="flex items-center gap-3">
                <div class="p-3 bg-surface-container-high text-on-surface rounded-full">
                  <span class="material-symbols-outlined">schedule</span>
                </div>
                <h3 class="font-headline text-lg font-semibold text-on-surface">Focus Time</h3>
              </div>
              <span class="text-xs font-medium text-on-surface-variant bg-surface-container px-2 py-1 rounded-full">Week</span>
            </div>
            <div class="mt-auto">
              <div class="flex items-baseline gap-2">
                <span class="font-display text-5xl font-bold text-on-surface">14</span>
                <span class="text-on-surface-variant font-medium">h</span>
                <span class="font-display text-5xl font-bold text-on-surface ml-2">30</span>
                <span class="text-on-surface-variant font-medium">m</span>
              </div>
              <div class="flex items-center gap-2 mt-4 text-sm text-primary font-medium">
                <span class="material-symbols-outlined text-sm">trending_up</span>
                <span>+2.5 hrs vs last week</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Content Sections: Mistakes & History -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-12 mb-20">
        <!-- Common Mistakes Section -->
        <section>
          <div class="flex items-center justify-between mb-8">
            <h2 class="font-headline text-2xl font-bold text-on-surface">Lỗi sai đã gặp</h2>
            <button class="text-primary text-sm font-semibold hover:underline">Xem tất cả</button>
          </div>
          <div class="space-y-4">
            <div class="bg-surface-container-lowest rounded-xl p-6 ambient-shadow border-l-4 border-error/40 group hover:bg-surface-bright transition-colors">
              <div class="flex justify-between items-start mb-2">
                <span class="text-error font-bold font-display text-lg">〜ている vs 〜た</span>
                <span class="px-2 py-1 bg-error-container/20 text-error text-[10px] uppercase font-bold rounded">Grammar</span>
              </div>
              <p class="text-on-surface-variant text-sm leading-relaxed">Nhầm lẫn giữa trạng thái đang tiếp diễn và hành động đã kết thúc trong quá khứ.</p>
            </div>
            <div class="bg-surface-container-lowest rounded-xl p-6 ambient-shadow border-l-4 border-error/40 group hover:bg-surface-bright transition-colors">
              <div class="flex justify-between items-start mb-2">
                <span class="text-error font-bold font-display text-lg">独身 (どくしん)</span>
                <span class="px-2 py-1 bg-error-container/20 text-error text-[10px] uppercase font-bold rounded">Vocabulary</span>
              </div>
              <p class="text-on-surface-variant text-sm leading-relaxed">Hay viết sai bộ Nhân đứng thành bộ Nhân nằm trong chữ Kanji 独.</p>
            </div>
            <div class="bg-surface-container-lowest rounded-xl p-6 ambient-shadow border-l-4 border-error/40 group hover:bg-surface-bright transition-colors">
              <div class="flex justify-between items-start mb-2">
                <span class="text-error font-bold font-display text-lg">〜に従って</span>
                <span class="px-2 py-1 bg-error-container/20 text-error text-[10px] uppercase font-bold rounded">N3 Grammar</span>
              </div>
              <p class="text-on-surface-variant text-sm leading-relaxed">Dùng sai ngữ cảnh với "〜につれて". "に従って" nhấn mạnh hơn vào sự thay đổi đồng thời.</p>
            </div>
          </div>
        </section>

        <!-- Exercise History Section -->
        <section>
          <div class="flex items-center justify-between mb-8">
            <h2 class="font-headline text-2xl font-bold text-on-surface">Lịch sử làm bài tập</h2>
            <button class="text-primary text-sm font-semibold hover:underline">Chi tiết</button>
          </div>
          <div class="bg-surface-container-lowest rounded-xl ambient-shadow overflow-hidden">
            <table class="w-full text-left">
              <thead class="bg-surface-container-low/50">
                <tr>
                  <th class="px-6 py-4 text-xs font-bold text-on-surface-variant uppercase tracking-wider">Bài tập</th>
                  <th class="px-6 py-4 text-xs font-bold text-on-surface-variant uppercase tracking-wider">Điểm số</th>
                  <th class="px-6 py-4 text-xs font-bold text-on-surface-variant uppercase tracking-wider">Ngày</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-outline-variant/10">
                <tr class="hover:bg-surface-bright transition-colors cursor-pointer">
                  <td class="px-6 py-4">
                    <div class="flex flex-col">
                      <span class="font-medium text-on-surface">JLPT N3 Mock #4</span>
                      <span class="text-xs text-on-surface-variant">Phần Đọc hiểu</span>
                    </div>
                  </td>
                  <td class="px-6 py-4">
                    <span class="text-primary font-bold">160/180</span>
                  </td>
                  <td class="px-6 py-4 text-sm text-on-surface-variant">20/10/2023</td>
                </tr>
                <tr class="hover:bg-surface-bright transition-colors cursor-pointer">
                  <td class="px-6 py-4">
                    <div class="flex flex-col">
                      <span class="font-medium text-on-surface">Ngữ pháp N3 (A-Z)</span>
                      <span class="text-xs text-on-surface-variant">Trắc nghiệm</span>
                    </div>
                  </td>
                  <td class="px-6 py-4">
                    <span class="text-primary font-bold">45/50</span>
                  </td>
                  <td class="px-6 py-4 text-sm text-on-surface-variant">18/10/2023</td>
                </tr>
                <tr class="hover:bg-surface-bright transition-colors cursor-pointer">
                  <td class="px-6 py-4">
                    <div class="flex flex-col">
                      <span class="font-medium text-on-surface">Kanji N3 Weekly</span>
                      <span class="text-xs text-on-surface-variant">Kiểm tra từ vựng</span>
                    </div>
                  </td>
                  <td class="px-6 py-4">
                    <span class="text-primary font-bold">92%</span>
                  </td>
                  <td class="px-6 py-4 text-sm text-on-surface-variant">15/10/2023</td>
                </tr>
                <tr class="hover:bg-surface-bright transition-colors cursor-pointer">
                  <td class="px-6 py-4">
                    <div class="flex flex-col">
                      <span class="font-medium text-on-surface">Luyện nghe N3 #12</span>
                      <span class="text-xs text-on-surface-variant">Nghe hiểu</span>
                    </div>
                  </td>
                  <td class="px-6 py-4">
                    <span class="text-primary font-bold">38/60</span>
                  </td>
                  <td class="px-6 py-4 text-sm text-on-surface-variant">12/10/2023</td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
      </div>

      <!-- Preferences Section -->
      <section class="max-w-3xl">
        <h2 class="font-headline text-2xl font-bold text-on-surface mb-8">Cài đặt &amp; Tùy chỉnh</h2>
        <div class="space-y-4">
          <!-- Notifications Toggle -->
          <div class="bg-surface-container-lowest rounded-xl p-6 ambient-shadow flex items-center justify-between group hover:bg-surface-bright transition-colors cursor-pointer">
            <div class="flex items-center gap-6">
              <div class="p-4 bg-surface-container-low rounded-full text-on-surface-variant group-hover:bg-primary-container group-hover:text-on-primary-container transition-colors">
                <span class="material-symbols-outlined">notifications_active</span>
              </div>
              <div>
                <h4 class="font-headline text-lg font-medium text-on-surface mb-1">Nhắc nhở học tập</h4>
                <p class="text-sm text-on-surface-variant leading-relaxed">Nhận thông báo hàng ngày để duy trì chuỗi học.</p>
              </div>
            </div>
            <div class="w-12 h-6 bg-primary rounded-full relative">
              <div class="absolute right-1 top-1 w-4 h-4 bg-on-primary rounded-full"></div>
            </div>
          </div>

          <!-- Theme Preference -->
          <div class="bg-surface-container-lowest rounded-xl p-6 ambient-shadow flex flex-col md:flex-row md:items-center justify-between gap-6 group hover:bg-surface-bright transition-colors cursor-pointer">
            <div class="flex items-center gap-6">
              <div class="p-4 bg-surface-container-low rounded-full text-on-surface-variant group-hover:bg-primary-container group-hover:text-on-primary-container transition-colors">
                <span class="material-symbols-outlined">palette</span>
              </div>
              <div>
                <h4 class="font-headline text-lg font-medium text-on-surface mb-1">Giao diện Dojo</h4>
                <p class="text-sm text-on-surface-variant leading-relaxed">Điều chỉnh môi trường học tập trực quan.</p>
              </div>
            </div>
            <div class="flex bg-surface-container-high p-1 rounded-lg">
              <div class="px-4 py-2 bg-surface-container-lowest text-on-surface rounded-md text-sm font-medium shadow-sm">Sáng</div>
              <div class="px-4 py-2 text-on-surface-variant rounded-md text-sm font-medium hover:text-on-surface transition-colors">Tối</div>
              <div class="px-4 py-2 text-on-surface-variant rounded-md text-sm font-medium hover:text-on-surface transition-colors">Hệ thống</div>
            </div>
          </div>

          <!-- Account Settings Link -->
          <div class="bg-surface-container-lowest rounded-xl p-6 ambient-shadow flex items-center justify-between group hover:bg-surface-bright transition-colors cursor-pointer mt-8">
            <div class="flex items-center gap-6">
              <div class="p-4 bg-surface-container-low rounded-full text-on-surface-variant">
                <span class="material-symbols-outlined">manage_accounts</span>
              </div>
              <div>
                <h4 class="font-headline text-lg font-medium text-on-surface mb-1">Tài khoản &amp; Bảo mật</h4>
                <p class="text-sm text-on-surface-variant leading-relaxed">Quản lý email, mật khẩu và dữ liệu cá nhân.</p>
              </div>
            </div>
            <span class="material-symbols-outlined text-on-surface-variant group-hover:text-on-surface transition-colors">chevron_right</span>
          </div>
        </div>
      </section>

      <!-- Logout Action -->
      <div class="mt-16 mb-12 max-w-3xl flex justify-end">
        <button @click="handleLogout" class="px-6 py-3 text-secondary font-medium rounded-full hover:bg-secondary-container/50 transition-colors flex items-center gap-2">
          <span class="material-symbols-outlined">logout</span>
          Đăng xuất
        </button>
      </div>
    </main>

    <!-- BottomNavBar (Mobile Only) -->
    <nav class="md:hidden fixed bottom-0 left-0 w-full flex justify-around items-center px-4 pb-6 pt-3 bg-slate-50/90 dark:bg-slate-900/90 backdrop-blur-md rounded-t-3xl shadow-[0_-4px_20px_rgba(0,0,0,0.05)] z-50 text-[11px] font-medium font-['Plus_Jakarta_Sans'] text-blue-800 dark:text-blue-300">
      <a class="flex flex-col items-center justify-center text-slate-500 dark:text-slate-400 px-5 py-2 hover:text-blue-700 dark:hover:text-blue-300" href="#">
        <span class="material-symbols-outlined text-2xl mb-1">menu_book</span>
        <span>Learn</span>
      </a>
      <a class="flex flex-col items-center justify-center text-slate-500 dark:text-slate-400 px-5 py-2 hover:text-blue-700 dark:hover:text-blue-300" href="#">
        <span class="material-symbols-outlined text-2xl mb-1">emoji_events</span>
        <span>Goals</span>
      </a>
      <a class="flex flex-col items-center justify-center text-slate-500 dark:text-slate-400 px-5 py-2 hover:text-blue-700 dark:hover:text-blue-300" href="#">
        <span class="material-symbols-outlined text-2xl mb-1">bar_chart</span>
        <span>Progress</span>
      </a>
      <RouterLink to="/profile" class="flex flex-col items-center justify-center bg-blue-100 dark:bg-blue-900/40 text-blue-900 dark:text-blue-100 rounded-2xl px-5 py-2 translate-y-[-2px] transition-transform">
        <span class="material-symbols-outlined text-2xl mb-1" style="font-variation-settings: 'FILL' 1;">person</span>
        <span>Profile</span>
      </RouterLink>
    </nav>
  </div>
</template>
