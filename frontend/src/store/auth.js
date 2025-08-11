import { defineStore } from 'pinia'
import { authAPI } from '@/api/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    token: localStorage.getItem('authToken') || null,
    role: localStorage.getItem('userRole') || null,
    isAuthenticated: false,
    loading: false
  }),

  getters: {
    // Check if user is admin
    isAdmin: state => state.role === 'admin',

    // Check if user is teacher
    isTeacher: state => state.role === 'teacher',

    // Check if user is student
    isStudent: state => state.role === 'student',

    // Get user ID
    userId: state => state.user?.id || null,

    // Get user display name
    displayName: state => {
      if (!state.user) return ''
      return state.user.name || state.user.username || 'User'
    }
  },

  actions: {
    // Set authentication data
    setAuth(token, user, role) {
      console.log('setAuth called with:', { token, user, role })
      this.token = token
      this.user = user
      this.role = role
      this.isAuthenticated = true

      // Store in localStorage
      localStorage.setItem('authToken', token)
      localStorage.setItem('userRole', role)
      localStorage.setItem('userData', JSON.stringify(user))
      console.log('localStorage after setAuth:', {
        authToken: localStorage.getItem('authToken'),
        userRole: localStorage.getItem('userRole'),
        userData: localStorage.getItem('userData')
      })
    },

    // Clear authentication data
    clearAuth() {
      this.user = null
      this.token = null
      this.role = null
      this.isAuthenticated = false

      // Clear localStorage
      localStorage.removeItem('authToken')
      localStorage.removeItem('userRole')
      localStorage.removeItem('userData')

      // Clear Remember Me data
      localStorage.removeItem('rememberedRole')
      localStorage.removeItem('rememberedUsername')
      localStorage.removeItem('rememberedRemember')
      console.log('localStorage after clearAuth:', {
        authToken: localStorage.getItem('authToken'),
        userRole: localStorage.getItem('userRole'),
        userData: localStorage.getItem('userData')
      })
    },

    // —— Admin login ——
    async adminLogin(credentials) {
      this.loading = true
      try {
        const response = await authAPI.adminLogin(credentials)
        console.log('adminLogin response:', response)

        if (response.data.code === 0) {
          throw new Error(response.data.msg || 'Login failed')
        }

        // 关键：这里 response.data.data 是一个对象，里面才有 token
        const data = response.data.data
        if (!data || !data.token) {
          throw new Error('No token received')
        }

        const token = data.token
        const user = data.user || {
          username: credentials.username,
          role: 'admin'
        }

        this.setAuth(token, user, 'admin')
        return { success: true, user }
      } finally {
        this.loading = false
      }
    },

    // Student login
    async studentLogin(credentials) {
      this.loading = true
      try {
        const response = await authAPI.studentLogin(credentials)

        // 检查后端返回的数据格式
        if (response.data.code === 0) {
          // 登录失败
          throw new Error(response.data.msg || 'Login failed')
        }

        // 登录成功，response.data.data 现在包含 token, userId, user
        const data = response.data.data
        if (!data || !data.token) {
          throw new Error('No token received')
        }

        // 使用后端返回的用户信息
        const user = data.user || {
          id: data.userId,
          username: credentials.username,
          role: 'student'
        }

        this.setAuth(data.token, user, 'student')
        return { success: true, user }
      } catch (error) {
        console.error('Student login error:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    // Teacher login
    async teacherLogin(credentials) {
      this.loading = true
      try {
        const response = await authAPI.teacherLogin(credentials)

        // 检查后端返回的数据格式
        if (response.data.code === 0) {
          // 登录失败
          throw new Error(response.data.msg || 'Login failed')
        }

        // 登录成功，response.data.data 现在包含 token, userId, user
        const data = response.data.data
        if (!data || !data.token) {
          throw new Error('No token received')
        }

        // 使用后端返回的用户信息
        const user = data.user || {
          id: data.userId,
          username: credentials.username,
          role: 'teacher'
        }

        this.setAuth(data.token, user, 'teacher')
        return { success: true, user }
      } catch (error) {
        console.error('Teacher login error:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    // Student registration
    async studentRegister(userData) {
      this.loading = true
      try {
        const response = await authAPI.studentRegister(userData)

        // 检查后端返回的数据格式
        if (response.data.code === 0) {
          throw new Error(response.data.msg || 'Registration failed')
        }

        return { success: true, message: 'Registration successful' }
      } catch (error) {
        console.error('Student registration error:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    // Teacher registration
    async teacherRegister(userData) {
      this.loading = true
      try {
        const response = await authAPI.teacherRegister(userData)

        // 检查后端返回的数据格式
        if (response.data.code === 0) {
          throw new Error(response.data.msg || 'Registration failed')
        }

        return { success: true, message: 'Registration successful' }
      } catch (error) {
        console.error('Teacher registration error:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    // Logout
    async logout() {
      // 直接清除认证状态，不调用后端API
      this.clearAuth()
    },

    // Initialize auth state from localStorage
    initAuth() {
      const token = localStorage.getItem('authToken')
      const role = localStorage.getItem('userRole')
      const userData = localStorage.getItem('userData')

      if (token && role && userData) {
        try {
          const user = JSON.parse(userData)
          this.setAuth(token, user, role)
        } catch (error) {
          console.error('Error parsing user data:', error)
          this.clearAuth()
        }
      }
    },

    // Refresh user data
    async refreshUser() {
      if (!this.token) return

      try {
        const response = await authAPI.getCurrentUser()
        this.user = response.data
        localStorage.setItem('userData', JSON.stringify(response.data))
      } catch (error) {
        console.error('Error refreshing user data:', error)
        this.clearAuth()
      }
    }
  }
})
