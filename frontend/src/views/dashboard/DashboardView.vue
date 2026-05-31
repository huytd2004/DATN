<template>
<section class="pt-6 space-y-10 w-full max-w-screen-2xl">

<!-- User Profile & Hero Stats -->
<div class="flex flex-col md:flex-row md:items-end justify-between gap-6">
<div>
<h3 class="text-display-sm text-on-primary-fixed font-bold tracking-tight mb-2">Chào buổi sáng, Minh! 👋</h3>
<div class="flex items-center gap-3">
<span class="bg-primary-container text-on-primary-container px-4 py-1 rounded-full text-xs font-bold">TRÌNH ĐỘ N3</span>
<span class="text-on-surface-variant text-sm font-medium">Hành trình đến JLPT còn 142 ngày</span>
</div>
</div>
<div class="flex gap-4">
<div class="text-right">
<p class="text-xs text-on-surface-variant uppercase font-bold tracking-widest">Chuỗi ngày</p>
<p class="text-2xl font-black text-secondary">15 Ngày</p>
</div>
</div>
</div>
<!-- Bento Grid Layout -->
<div class="grid grid-cols-1 md:grid-cols-12 gap-6">
<!-- Daily Goals: Circles -->
<div class="md:col-span-4 bg-surface-container-lowest rounded-xl p-8 flex flex-col items-center justify-center space-y-6">
<h4 class="text-sm font-bold text-on-surface-variant uppercase tracking-widest w-full text-left">Mục tiêu hàng ngày</h4>
<div class="relative flex items-center justify-center">
<!-- Outer Circle -->
<svg class="w-48 h-48 transform -rotate-90">
<circle class="text-surface-container" cx="96" cy="96" fill="transparent" r="88" stroke="currentColor" stroke-width="12"></circle>
<circle class="text-primary rounded-full" cx="96" cy="96" fill="transparent" r="88" stroke="currentColor" stroke-dasharray="552.92" stroke-dashoffset="138" stroke-width="12"></circle>
</svg>
<div class="absolute flex flex-col items-center">
<span class="text-4xl font-black text-on-primary-fixed">75%</span>
<span class="text-xs text-on-surface-variant">Hoàn thành</span>
</div>
</div>
<div class="grid grid-cols-2 gap-4 w-full">
<div class="bg-surface-container-low p-4 rounded-xl text-center">
<p class="text-xl font-bold text-primary">45</p>
<p class="text-[10px] text-on-surface-variant uppercase font-bold">Phút học</p>
</div>
<div class="bg-surface-container-low p-4 rounded-xl text-center">
<p class="text-xl font-bold text-secondary">12</p>
<p class="text-[10px] text-on-surface-variant uppercase font-bold">Từ mới</p>
</div>
</div>
</div>
<!-- Learning Logs Chart -->
<div class="md:col-span-8 bg-surface-container-lowest rounded-xl p-8 flex flex-col">
<div class="flex items-center justify-between mb-8">
<h4 class="text-sm font-bold text-on-surface-variant uppercase tracking-widest">Tiến trình học tập</h4>
<div class="flex gap-2">
<span class="flex items-center gap-1 text-xs text-on-surface-variant">
<span class="w-2 h-2 rounded-full bg-primary"></span> Tuần này
                            </span>
</div>
</div>
<div class="flex-1 flex items-end justify-between gap-2 min-h-[200px]">
<!-- Bar Chart -->
<div class="flex flex-col items-center gap-3 w-full">
<div class="w-full bg-surface-container-low rounded-t-lg relative group h-32">
<div class="absolute bottom-0 w-full bg-primary-container rounded-t-lg transition-all h-24 group-hover:bg-primary"></div>
</div>
<span class="text-[10px] font-bold text-on-surface-variant">T2</span>
</div>
<div class="flex flex-col items-center gap-3 w-full">
<div class="w-full bg-surface-container-low rounded-t-lg relative group h-32">
<div class="absolute bottom-0 w-full bg-primary-container rounded-t-lg transition-all h-28 group-hover:bg-primary"></div>
</div>
<span class="text-[10px] font-bold text-on-surface-variant">T3</span>
</div>
<div class="flex flex-col items-center gap-3 w-full">
<div class="w-full bg-surface-container-low rounded-t-lg relative group h-32">
<div class="absolute bottom-0 w-full bg-primary-container rounded-t-lg transition-all h-16 group-hover:bg-primary"></div>
</div>
<span class="text-[10px] font-bold text-on-surface-variant">T4</span>
</div>
<div class="flex flex-col items-center gap-3 w-full">
<div class="w-full bg-surface-container-low rounded-t-lg relative group h-32">
<div class="absolute bottom-0 w-full bg-primary-container rounded-t-lg transition-all h-32 group-hover:bg-primary"></div>
</div>
<span class="text-[10px] font-bold text-on-surface-variant">T5</span>
</div>
<div class="flex flex-col items-center gap-3 w-full">
<div class="w-full bg-surface-container-low rounded-t-lg relative group h-32">
<div class="absolute bottom-0 w-full bg-primary-container rounded-t-lg transition-all h-20 group-hover:bg-primary"></div>
</div>
<span class="text-[10px] font-bold text-on-surface-variant">T6</span>
</div>
<div class="flex flex-col items-center gap-3 w-full">
<div class="w-full bg-surface-container-low rounded-t-lg relative group h-32">
<div class="absolute bottom-0 w-full bg-primary-container rounded-t-lg transition-all h-24 group-hover:bg-primary"></div>
</div>
<span class="text-[10px] font-bold text-on-surface-variant">T7</span>
</div>
<div class="flex flex-col items-center gap-3 w-full">
<div class="w-full bg-surface-container-low rounded-t-lg relative group h-32">
<div class="absolute bottom-0 w-full bg-primary-container rounded-t-lg transition-all h-10 group-hover:bg-primary border-2 border-primary"></div>
</div>
<span class="text-[10px] font-bold text-primary">CN</span>
</div>
</div>
</div>
<!-- Words to Review (AI Powered) -->
<div class="md:col-span-12 bg-surface-container-lowest rounded-xl p-8 border border-primary/5">
<div class="flex items-center justify-between mb-8">
<div>
<h4 class="text-lg font-bold text-on-primary-fixed">Từ vựng cần ôn tập</h4>
<p class="text-sm text-on-surface-variant">Gợi ý từ AI Tutor dựa trên các lỗi sai gần đây</p>
</div>
<button class="px-6 py-2 bg-primary-container text-on-primary-container rounded-full text-sm font-bold hover:bg-primary hover:text-on-primary transition-all">
                            Ôn tập tất cả
                        </button>
</div>
<div class="grid grid-cols-1 md:grid-cols-3 gap-6">
<!-- Word Card 1 -->
<div class="bg-surface-container-low p-6 rounded-xl hover:bg-surface-container-lowest transition-all hover:shadow-sm">
<div class="flex justify-between items-start mb-4">
<span class="text-3xl font-bold text-on-primary-fixed">把握</span>
<span class="bg-secondary-container text-on-secondary-container text-[10px] px-2 py-0.5 rounded font-bold">KHÓ</span>
</div>
<p class="text-xs text-on-surface-variant mb-1">はあく • Haaku</p>
<p class="text-sm font-medium">Nắm bắt, hiểu rõ</p>
<div class="mt-4 pt-4 border-t border-outline-variant/10">
<p class="text-[10px] text-on-surface-variant italic">AI: "Bạn thường nhầm với 把握 và 掌握"</p>
</div>
</div>
<!-- Word Card 2 -->
<div class="bg-surface-container-low p-6 rounded-xl hover:bg-surface-container-lowest transition-all hover:shadow-sm">
<div class="flex justify-between items-start mb-4">
<span class="text-3xl font-bold text-on-primary-fixed">微妙</span>
<span class="bg-tertiary-container text-on-tertiary-container text-[10px] px-2 py-0.5 rounded font-bold">TRUNG BÌNH</span>
</div>
<p class="text-xs text-on-surface-variant mb-1">びみょう • Bimyou</p>
<p class="text-sm font-medium">Tinh tế, khó nói, nhạy cảm</p>
<div class="mt-4 pt-4 border-t border-outline-variant/10">
<p class="text-[10px] text-on-surface-variant italic">AI: "Sắp đến hạn ôn tập định kỳ (SRS)"</p>
</div>
</div>
<!-- Word Card 3 -->
<div class="bg-surface-container-low p-6 rounded-xl hover:bg-surface-container-lowest transition-all hover:shadow-sm">
<div class="flex justify-between items-start mb-4">
<span class="text-3xl font-bold text-on-primary-fixed">具体的</span>
<span class="bg-primary-container text-on-primary-container text-[10px] px-2 py-0.5 rounded font-bold">DỄ</span>
</div>
<p class="text-xs text-on-surface-variant mb-1">ぐたいてき • Gutaiteki</p>
<p class="text-sm font-medium">Cụ thể, hữu hình</p>
<div class="mt-4 pt-4 border-t border-outline-variant/10">
<p class="text-[10px] text-on-surface-variant italic">AI: "Bạn đã trả lời đúng 4 lần liên tiếp"</p>
</div>
</div>
</div>
</div>
<!-- AI Tutor Floating Insight Card -->
<div class="md:col-span-12 glass-panel p-8 rounded-xl flex flex-col md:flex-row items-center gap-8 border border-white">
<div class="w-20 h-20 bg-primary rounded-2xl flex items-center justify-center shrink-0">
<span class="material-symbols-outlined text-4xl text-on-primary">psychology</span>
</div>
<div class="flex-1 text-center md:text-left">
<h4 class="text-lg font-bold text-on-primary-fixed">Phân tích từ AI Tutor</h4>
<p class="text-on-surface-variant leading-relaxed mt-2">
                            Dựa trên thói quen của bạn, thời gian học hiệu quả nhất là từ 21:00 đến 22:00. 
                            Kỹ năng <strong>Nghe hiểu</strong> đang tiến bộ nhanh hơn so với <strong>Ngữ pháp</strong>. 
                            Hãy thử dành thêm 10 phút hôm nay để ôn lại các cấu trúc <i>~はずだ</i>.
                        </p>
</div>
<button class="px-8 py-4 bg-on-primary-fixed text-on-primary rounded-full font-bold whitespace-nowrap shadow-lg shadow-primary/20 hover:scale-[1.02] transition-transform">
                        Chat với AI Tutor
                    </button>
</div>
</div>

</section>
</template>
<script setup>
</script>
