import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    // ─── Guest-only routes ──────────────────────────────────────────────────
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/auth/LoginView.vue'),
      meta: { guestOnly: true }
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/auth/RegisterView.vue'),
      meta: { guestOnly: true }
    },

    // ─── Protected routes (cần đăng nhập) ─────────────────────────────────
    {
      path: '/',
      component: () => import('@/layouts/AppLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          redirect: '/dashboard'
        },
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/dashboard/DashboardView.vue')
        },
        {
          path: 'review',
          name: 'ReviewSetup',
          component: () => import('@/views/review/ReviewSetupView.vue')
        },
        {
          path: 'review/quiz',
          name: 'ReviewQuiz',
          component: () => import('@/views/review/ReviewQuizView.vue')
        },
        {
          path: 'review/story',
          name: 'ReviewStory',
          component: () => import('@/views/review/ReviewStoryView.vue')
        },
        {
          path: 'dictionary',
          name: 'Dictionary',
          component: () => import('@/views/dictionary/DictionaryView.vue')
        },
        {
          path: 'kanji',
          name: 'Kanji',
          component: () => import('@/views/dictionary/KanjiView.vue')
        },
        {
          path: 'grammar',
          name: 'Grammar',
          component: () => import('@/views/dictionary/GrammarView.vue')
        },
        {
          path: 'translate',
          name: 'Translate',
          component: () => import('@/views/dictionary/TranslateView.vue')
        },
        {
          path: 'tutor',
          name: 'TutorSetup',
          component: () => import('@/views/tutor/TutorSetupView.vue')
        },
        {
          path: 'tutor/chat',
          name: 'TutorChat',
          component: () => import('@/views/tutor/TutorChatView.vue')
        },
        {
          path: 'tutor/result',
          name: 'TutorResult',
          component: () => import('@/views/tutor/TutorResultView.vue')
        },
        {
          path: 'profile',
          name: 'Profile',
          component: () => import('@/views/profile/ProfileView.vue')
        },
        {
          path: 'flashcards',
          name: 'Flashcards',
          component: () => import('@/views/flashcard/FlashcardsView.vue')
        },
        {
          path: 'flashcards/:id',
          name: 'DeckDetail',
          component: () => import('@/views/flashcard/DeckDetailView.vue')
        },
        {
          path: 'flashcards/:id/study',
          name: 'StudySession',
          component: () => import('@/views/flashcard/StudySessionView.vue')
        },
      ]
    },

    // ─── Catch-all ────────────────────────────────────────────────────────
    {
      path: '/:pathMatch(.*)*',
      redirect: '/dashboard'
    }
  ]
})

// ─── Navigation Guards ────────────────────────────────────────────────────
router.beforeEach((to) => {
  // Lấy store sau khi Pinia đã được mount (lazy)
  const authStore = useAuthStore()

  // Route yêu cầu đăng nhập mà chưa auth → redirect về /login
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return { name: 'Login', query: { redirect: to.fullPath } }
  }

  // Route chỉ dành cho guest mà đã auth → redirect về /dashboard
  if (to.meta.guestOnly && authStore.isAuthenticated) {
    return { name: 'Dashboard' }
  }
})

export default router
