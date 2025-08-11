import axios from 'axios'

// Create axios instance with base configuration
const api = axios.create({
  baseURL: process.env.VUE_APP_API_URL || '',
  timeout: 60000, // 增加到60秒以适应AI处理时间
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request interceptor to add auth token
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('authToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// Response interceptor to handle errors
api.interceptors.response.use(
  response => {
    return response
  },
  error => {
    if (error.response?.status === 401) {
      // Unauthorized - clear token and redirect to login
      localStorage.removeItem('authToken')
      window.location.href = '/'
    }
    return Promise.reject(error)
  }
)

// Authentication API methods
export const authAPI = {
  // Admin login
  adminLogin: credentials => {
    return api.post('/api/login', { ...credentials, role: 'ADMIN' })
  },

  // Student login
  studentLogin: credentials => {
    return api.post('/api/login', { ...credentials, role: 'STUDENT' })
  },

  // Teacher login
  teacherLogin: credentials => {
    return api.post('/api/login', { ...credentials, role: 'TEACHER' })
  },

  // Student registration
  studentRegister: userData => {
    return api.post('/api/register/student', userData)
  },

  // Teacher registration
  teacherRegister: userData => {
    return api.post('/api/register/teacher', userData)
  },

  // Get current user info
  getCurrentUser: () => {
    return api.get('/api/user/current')
  }
}

export default api
