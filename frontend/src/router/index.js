import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import AuthPage from '@/views/AuthPage.vue'
import AdminDashboard from '@/views/AdminDashboard.vue'
import StudentDashboard from '@/views/StudentDashboard.vue'
import TeacherDashboard from '@/views/TeacherDashboard.vue'
import ApiTest from '@/views/ApiTest.vue'

const routes = [
  {
    path: '/',
    name: 'AuthPage',
    component: AuthPage,
    meta: { requiresGuest: true }
  },
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: AdminDashboard,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/student',
    name: 'StudentDashboard',
    component: StudentDashboard,
    meta: { requiresAuth: true, requiresStudent: true }
  },
  {
    path: '/teacher',
    name: 'TeacherDashboard',
    component: TeacherDashboard,
    meta: { requiresAuth: true, requiresTeacher: true }
  },
  {
    path: '/api-test',
    name: 'ApiTest',
    component: ApiTest
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

// Navigation guard
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  // Check if route requires authentication
  if (to.meta.requiresAuth) {
    if (!authStore.isAuthenticated) {
      // Not authenticated, redirect to login
      next('/')
      return
    }

    // Check role-specific requirements
    if (to.meta.requiresAdmin && !authStore.isAdmin) {
      next('/')
      return
    }

    if (to.meta.requiresStudent && !authStore.isStudent) {
      next('/')
      return
    }

    if (to.meta.requiresTeacher && !authStore.isTeacher) {
      next('/')
      return
    }
  }

  // Check if route requires guest (not authenticated)
  if (to.meta.requiresGuest && authStore.isAuthenticated) {
    // Already authenticated, redirect to appropriate dashboard
    if (authStore.isAdmin) {
      next('/admin')
    } else if (authStore.isStudent) {
      next('/student')
    } else if (authStore.isTeacher) {
      next('/teacher')
    } else {
      next('/')
    }
    return
  }

  next()
})

export default router
