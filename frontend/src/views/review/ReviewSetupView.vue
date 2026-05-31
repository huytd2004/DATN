<template>
  <div class="w-full">
    <!-- Welcome/Alert Header -->
    <section class="mb-12">
      <div class="bg-primary-container/30 border border-primary-container p-6 rounded-3xl flex items-center gap-6">
        <div class="w-16 h-16 rounded-2xl bg-primary-container flex items-center justify-center shrink-0">
          <span class="material-symbols-outlined text-primary text-3xl" data-icon="psychology_alt" style="font-variation-settings: 'FILL' 1;">psychology_alt</span>
        </div>
        <div>
          <h3 class="font-headline font-bold text-on-primary-fixed text-xl mb-1">Đề xuất tối ưu cho bạn</h3>
          <p class="text-on-primary-fixed-variant opacity-80 leading-relaxed text-sm">Hệ thống đã tự động thêm <span class="font-bold underline">15 từ</span> bạn hay làm sai vào danh sách ôn tập hôm nay để tăng cường ghi nhớ.</p>
        </div>
      </div>
    </section>

    <!-- Step 1: Deck Selection (Bento Grid) -->
    <section class="mb-16">
      <div class="flex items-baseline justify-between mb-8">
        <div class="flex flex-col">
          <span class="text-xs font-bold text-primary tracking-[0.2em] uppercase mb-1">Bước 01</span>
          <h2 class="text-3xl font-headline font-extrabold text-on-surface">Chọn bộ thẻ (Deck Selection)</h2>
        </div>
        <a class="text-sm font-semibold text-primary flex items-center gap-1 hover:underline cursor-pointer">Xem tất cả <span class="material-symbols-outlined text-sm" data-icon="arrow_forward">arrow_forward</span></a>
      </div>
      
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <!-- Recommended Deck -->
        <div class="md:col-span-2 group relative overflow-hidden rounded-[2rem] bg-surface-container-lowest p-8 transition-all duration-500 hover:shadow-xl hover:shadow-primary/10 cursor-pointer border border-outline-variant/10">
          <div class="relative z-10 flex flex-col h-full">
            <div class="flex justify-between items-start mb-6">
              <span class="px-4 py-1.5 rounded-full bg-secondary-container text-on-secondary-container text-xs font-bold">KHUYÊN DÙNG</span>
              <span class="text-sm text-on-surface-variant font-medium">42 thẻ • 15 từ khó</span>
            </div>
            <h3 class="text-4xl font-headline font-bold text-on-surface mb-2">N3 Kanji Master</h3>
            <p class="text-on-surface-variant max-w-sm mb-8 leading-relaxed">Tập trung vào các bộ thủ (radical) và cách đọc Onyomi của các từ vựng bạn đã học trong 7 ngày qua.</p>
            <div class="mt-auto flex items-center gap-4">
              <div class="flex -space-x-2">
                <div class="w-8 h-8 rounded-full border-2 border-surface-container-lowest bg-surface-container"></div>
                <div class="w-8 h-8 rounded-full border-2 border-surface-container-lowest bg-primary-container"></div>
                <div class="w-8 h-8 rounded-full border-2 border-surface-container-lowest bg-secondary-container"></div>
              </div>
              <span class="text-xs font-semibold text-on-surface-variant">Đã học 85%</span>
            </div>
          </div>
          <!-- Abstract Background Decoration -->
          <div class="absolute top-0 right-0 w-1/2 h-full bg-gradient-to-l from-primary-container/20 to-transparent pointer-events-none"></div>
          <div class="absolute bottom-[-20%] right-[-10%] w-64 h-64 bg-primary opacity-[0.03] rounded-full blur-3xl group-hover:scale-150 transition-transform duration-700"></div>
        </div>

        <!-- Regular Deck 1 -->
        <div class="rounded-[2rem] bg-surface-container-low p-8 border border-transparent hover:border-primary-container hover:bg-surface-container-lowest transition-all group cursor-pointer flex flex-col">
          <div class="w-12 h-12 rounded-2xl bg-surface-container-highest flex items-center justify-center mb-6 group-hover:bg-primary-container transition-colors shrink-0">
            <span class="material-symbols-outlined text-primary" data-icon="translate">translate</span>
          </div>
          <h4 class="text-xl font-headline font-bold text-on-surface mb-2">N3 Grammar</h4>
          <p class="text-sm text-on-surface-variant mb-6 flex-1">Cấu trúc 「～はずだ」 và các mẫu câu phỏng đoán thường gặp.</p>
          <span class="text-xs font-bold text-on-surface-variant opacity-60">28 thẻ • Hôm nay</span>
        </div>

        <!-- Regular Deck 2 -->
        <div class="rounded-[2rem] bg-surface-container-low p-8 border border-transparent hover:border-primary-container hover:bg-surface-container-lowest transition-all group cursor-pointer flex flex-col">
          <div class="w-12 h-12 rounded-2xl bg-surface-container-highest flex items-center justify-center mb-6 group-hover:bg-primary-container transition-colors shrink-0">
            <span class="material-symbols-outlined text-primary" data-icon="forum">forum</span>
          </div>
          <h4 class="text-xl font-headline font-bold text-on-surface mb-2">N3 Listening</h4>
          <p class="text-sm text-on-surface-variant mb-6 flex-1">Luyện nghe hội thoại hàng ngày tại bưu điện và ngân hàng.</p>
          <span class="text-xs font-bold text-on-surface-variant opacity-60">15 thẻ • 2 ngày trước</span>
        </div>

        <!-- Regular Deck 3 -->
        <div class="rounded-[2rem] bg-surface-container-low p-8 border border-transparent hover:border-primary-container hover:bg-surface-container-lowest transition-all group cursor-pointer flex flex-col">
          <div class="w-12 h-12 rounded-2xl bg-surface-container-highest flex items-center justify-center mb-6 group-hover:bg-primary-container transition-colors shrink-0">
            <span class="material-symbols-outlined text-primary" data-icon="menu_book">menu_book</span>
          </div>
          <h4 class="text-xl font-headline font-bold text-on-surface mb-2">N3 Reading</h4>
          <p class="text-sm text-on-surface-variant mb-6 flex-1">Các bài đọc ngắn về chủ đề văn hóa Nhật Bản.</p>
          <span class="text-xs font-bold text-on-surface-variant opacity-60">10 thẻ • Mới</span>
        </div>

        <!-- Regular Deck 4 -->
        <div class="rounded-[2rem] bg-surface-container-low p-8 border border-transparent hover:border-primary-container hover:bg-surface-container-lowest transition-all group cursor-pointer flex flex-col">
          <div class="w-12 h-12 rounded-2xl bg-surface-container-highest flex items-center justify-center mb-6 group-hover:bg-primary-container transition-colors shrink-0">
            <span class="material-symbols-outlined text-primary" data-icon="star" style="font-variation-settings: 'FILL' 1;">star</span>
          </div>
          <h4 class="text-xl font-headline font-bold text-on-surface mb-2">Từ vựng yêu thích</h4>
          <p class="text-sm text-on-surface-variant mb-6 flex-1">Các từ vựng bạn đã đánh dấu sao thủ công.</p>
          <span class="text-xs font-bold text-on-surface-variant opacity-60">52 thẻ • Tùy chỉnh</span>
        </div>
      </div>
    </section>

    <!-- Step 2: Review Mode -->
    <section>
      <div class="flex flex-col mb-8">
        <span class="text-xs font-bold text-primary tracking-[0.2em] uppercase mb-1">Bước 02</span>
        <h2 class="text-3xl font-headline font-extrabold text-on-surface">Chọn chế độ ôn tập</h2>
      </div>
      
      <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
        <!-- Mode: Quiz -->
        <RouterLink to="/review/quiz" class="group cursor-pointer block">
          <div class="relative overflow-hidden rounded-[2.5rem] bg-surface-container-lowest p-1 border-2 border-transparent group-hover:border-primary transition-all duration-500 shadow-sm hover:shadow-xl hover:shadow-primary/10">
            <div class="p-8 flex flex-col h-full bg-surface-container-lowest rounded-[2.25rem]">
              <div class="flex items-center gap-4 mb-6">
                <div class="w-14 h-14 rounded-2xl bg-primary/10 flex items-center justify-center shrink-0">
                  <span class="material-symbols-outlined text-primary text-2xl" data-icon="edit_note">edit_note</span>
                </div>
                <div>
                  <h3 class="text-2xl font-headline font-bold text-on-surface">Quiz / Fill-in-the-blank</h3>
                  <p class="text-sm text-on-surface-variant font-medium mt-1">Luyện tập trí nhớ chủ động</p>
                </div>
              </div>
              
              <div class="space-y-4 mb-10">
                <div class="flex items-center gap-3 text-on-surface-variant">
                  <span class="material-symbols-outlined text-lg text-primary/70" data-icon="check_circle">check_circle</span>
                  <span class="text-sm font-medium">Trắc nghiệm 4 đáp án chọn Kanji đúng</span>
                </div>
                <div class="flex items-center gap-3 text-on-surface-variant">
                  <span class="material-symbols-outlined text-lg text-primary/70" data-icon="check_circle">check_circle</span>
                  <span class="text-sm font-medium">Điền từ vào chỗ trống theo ngữ cảnh</span>
                </div>
                <div class="flex items-center gap-3 text-on-surface-variant">
                  <span class="material-symbols-outlined text-lg text-primary/70" data-icon="check_circle">check_circle</span>
                  <span class="text-sm font-medium">Gõ Hiragana cho Kanji hiển thị</span>
                </div>
              </div>
              
              <div class="mt-auto">
                <div class="h-40 w-full rounded-2xl bg-surface-container overflow-hidden">
                  <img class="w-full h-full object-cover opacity-80 group-hover:opacity-100 group-hover:scale-110 transition-all duration-700" src="https://lh3.googleusercontent.com/aida-public/AB6AXuB5MUHQ4nliKt8TejltzFIBCVgaLhX_9z6Mu4I0Wdi15c7eejeykYlAONxev3oBwXwDh4p6t9ATebh_fT7dnR9ROGwrJjSKl0VN0fx18SrMBA8UKDTtednimiYpakBqys3f3dcwAHY3rfVrWi55fRrwtb_T-iWK1nBap3aSw1B_ONThk0Iyi99iBmUVJTkQJ0htuuB39vR-_7QKAeHvCYbZbc2IkRwthuw20U7hoDvNsFCEmlcMlubAc6oyGptyXo3Zo4ph1lyUodI"/>
                </div>
              </div>
            </div>
          </div>
        </RouterLink>

        <!-- Mode: Interactive Story -->
        <RouterLink to="/review/story" class="group cursor-pointer block">
          <div class="relative overflow-hidden rounded-[2.5rem] bg-surface-container-lowest p-1 border-2 border-transparent group-hover:border-primary transition-all duration-500 shadow-sm hover:shadow-xl hover:shadow-primary/10">
            <div class="p-8 flex flex-col h-full bg-surface-container-lowest rounded-[2.25rem]">
              <div class="flex items-center gap-4 mb-6">
                <div class="w-14 h-14 rounded-2xl bg-secondary/10 flex items-center justify-center shrink-0">
                  <span class="material-symbols-outlined text-secondary text-2xl" data-icon="auto_stories" style="font-variation-settings: 'FILL' 1;">auto_stories</span>
                </div>
                <div>
                  <h3 class="text-2xl font-headline font-bold text-on-surface">Interactive Story</h3>
                  <p class="text-sm text-on-surface-variant font-medium mt-1">Học qua bối cảnh câu chuyện</p>
                </div>
              </div>
              
              <div class="space-y-4 mb-10">
                <div class="flex items-center gap-3 text-on-surface-variant">
                  <span class="material-symbols-outlined text-lg text-secondary/70" data-icon="check_circle">check_circle</span>
                  <span class="text-sm font-medium">Nhập vai nhân vật trong tình huống giả định</span>
                </div>
                <div class="flex items-center gap-3 text-on-surface-variant">
                  <span class="material-symbols-outlined text-lg text-secondary/70" data-icon="check_circle">check_circle</span>
                  <span class="text-sm font-medium">Học cách sử dụng từ vựng tự nhiên nhất</span>
                </div>
                <div class="flex items-center gap-3 text-on-surface-variant">
                  <span class="material-symbols-outlined text-lg text-secondary/70" data-icon="check_circle">check_circle</span>
                  <span class="text-sm font-medium">Kết nối các từ đã học thành một mạch truyện</span>
                </div>
              </div>
              
              <div class="mt-auto">
                <div class="h-40 w-full rounded-2xl bg-surface-container overflow-hidden">
                  <img class="w-full h-full object-cover opacity-80 group-hover:opacity-100 group-hover:scale-110 transition-all duration-700" src="https://lh3.googleusercontent.com/aida-public/AB6AXuC7cpmspiSTuIpqj_Es8s4R0kAlS7WsuNhtKY9XWO0jobLqkKvwgZyDuV-wABsXvkUUTmN_5_tsrunNR-KDTVQgLfuORSTxWfzjhCM954YdWjDM3iHeDyyFr97hcEfYE9Gt9AlcT-bpRIsHJ2HjdPIJygLR_6z0GPBrSxpa-yEiB72CQMHvsFpYpYliq5Tz7SEeNqpSimR1KVXGhAkQU-sa1Or9FNbePWDoyBroF3WlMueIqpjDPgBZTLN48ZZW3Xo8U05KxO4YwD8"/>
                </div>
              </div>
            </div>
          </div>
        </RouterLink>
      </div>

      <!-- Footer Action -->
      <div class="mt-16 flex flex-col items-center">
        <p class="text-on-surface-variant text-sm mb-6 max-w-md text-center">Tùy chọn ôn tập này sẽ mất khoảng 15-20 phút. Hãy chuẩn bị một không gian yên tĩnh.</p>
        <button class="bg-gradient-to-r from-primary to-primary-dim text-white px-12 py-5 rounded-full font-headline font-bold text-lg shadow-xl shadow-primary/30 transition-all active:scale-95 flex items-center gap-4 hover:opacity-90">
          Bắt đầu phiên ôn tập ngẫu nhiên
          <span class="material-symbols-outlined" data-icon="rocket_launch">rocket_launch</span>
        </button>
      </div>
    </section>

    <!-- Floating Action Button -->
    <div class="fixed bottom-10 right-10 z-50">
      <button class="w-16 h-16 rounded-full bg-secondary text-on-secondary shadow-2xl flex items-center justify-center hover:scale-110 active:scale-90 transition-transform hover:shadow-secondary/30">
        <span class="material-symbols-outlined text-3xl" data-icon="history_edu">history_edu</span>
      </button>
    </div>
  </div>
</template>

<script setup>
</script>
