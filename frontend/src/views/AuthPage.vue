<template>
  <div class="auth-container">
    <!-- Role selection tabs -->
    <div class="role-tabs">
      <div
        :class="['role-tab', { active: currentRole === 'admin' }]"
        @click="switchRole('admin')"
      >
        <i class="el-icon-user-solid"></i>
        Admin
      </div>
      <div
        :class="['role-tab', { active: currentRole === 'teacher' }]"
        @click="switchRole('teacher')"
      >
        <i class="el-icon-user"></i>
        Instructor
      </div>
      <div
        :class="['role-tab', { active: currentRole === 'student' }]"
        @click="switchRole('student')"
      >
        <i class="el-icon-user"></i>
        Student
      </div>
    </div>

    <div class="auth-card">
      <!-- Login/Register tab switch (hidden for admin) -->
      <div v-if="currentRole !== 'admin'" class="auth-tabs">
        <span
          :class="['auth-tab', { active: isLoginMode }]"
          @click="switchMode('login')"
        >
          Login
        </span>
        <span class="divider">|</span>
        <span
          :class="['auth-tab', { active: !isLoginMode }]"
          @click="switchMode('register')"
        >
          Register
        </span>
      </div>

      <!-- Admin login form (admin can only login) -->
      <div v-if="currentRole === 'admin'" class="admin-login">
        <h2>Admin Login</h2>
        <el-form :model="adminLoginForm" class="auth-form">
          <el-form-item>
            <el-input
              v-model="adminLoginForm.username"
              placeholder="Admin Username"
              prefix-icon="el-icon-user"
            />
          </el-form-item>
          <el-form-item>
            <el-input
              v-model="adminLoginForm.password"
              type="password"
              placeholder="Password"
              prefix-icon="el-icon-lock"
              show-password
            />
          </el-form-item>
          <div class="form-row">
            <el-checkbox v-model="adminLoginForm.remember"
              >Remember me</el-checkbox
            >
            <a class="forgot-link" href="#">Forgot password?</a>
          </div>
          <el-form-item>
            <el-button
              type="primary"
              class="submit-btn"
              @click="handleAdminLogin"
              :loading="loading"
            >
              Login as Admin
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- Student/Teacher login form -->
      <el-form v-else-if="isLoginMode" :model="loginForm" class="auth-form">
        <h2>
          {{ currentRole === 'student' ? 'Student' : 'Instructor' }} Login
        </h2>
        <el-form-item>
          <el-input
            v-model="loginForm.username"
            placeholder="Username"
            prefix-icon="el-icon-user"
          />
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="Password"
            prefix-icon="el-icon-lock"
            show-password
          />
        </el-form-item>
        <div class="form-row">
          <el-checkbox v-model="loginForm.remember">Remember me</el-checkbox>
          <a class="forgot-link" href="#">Forgot password?</a>
        </div>
        <el-form-item>
          <el-button
            type="primary"
            class="submit-btn"
            @click="handleLogin"
            :loading="loading"
          >
            Login
          </el-button>
        </el-form-item>
      </el-form>

      <!-- Student/Teacher registration form -->
      <el-form v-else :model="registerForm" class="auth-form">
        <h2>
          {{ currentRole === 'student' ? 'Student' : 'Instructor' }}
          Registration
        </h2>
        <el-form-item>
          <el-input
            v-model="registerForm.name"
            placeholder="Full Name"
            prefix-icon="el-icon-user"
          />
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="registerForm.username"
            placeholder="Username"
            prefix-icon="el-icon-user"
          />
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="registerForm.email"
            placeholder="Email"
            prefix-icon="el-icon-message"
          />
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="registerForm.phone"
            placeholder="Phone Number"
            prefix-icon="el-icon-phone"
          />
        </el-form-item>

        <!-- Student-specific field -->
        <el-form-item v-if="currentRole === 'student'">
          <el-input
            v-model="registerForm.studentNo"
            placeholder="Student Number"
            prefix-icon="el-icon-document"
          />
        </el-form-item>

        <el-form-item>
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="Password"
            prefix-icon="el-icon-lock"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="Confirm Password"
            prefix-icon="el-icon-lock"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            class="submit-btn"
            @click="handleRegister"
            :loading="loading"
          >
            Register
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/store/auth'
import { useRouter } from 'vue-router'

export default {
  name: 'AuthPage',
  setup() {
    const authStore = useAuthStore()
    const router = useRouter()

    // Current selected role (admin, teacher, student)
    const currentRole = ref('student')

    // Current mode (login or register)
    const isLoginMode = ref(true)

    // Loading state for form submission
    const loading = ref(false)

    // Admin login form
    const adminLoginForm = reactive({
      username: '',
      password: '',
      remember: false
    })

    // Student/Teacher login form
    const loginForm = reactive({
      username: '',
      password: '',
      remember: false
    })

    // Student/Teacher registration form
    const registerForm = reactive({
      name: '',
      username: '',
      email: '',
      phone: '',
      studentNo: '', // Student-specific
      password: '',
      confirmPassword: ''
    })

    // Switch between roles
    const switchRole = role => {
      currentRole.value = role
      // Reset forms when switching roles
      resetForms()
      // Admin can only login, so set to login mode
      if (role === 'admin') {
        isLoginMode.value = true
      }
    }

    // Switch between login and register modes
    const switchMode = mode => {
      isLoginMode.value = mode === 'login'
      resetForms()
    }

    // Reset all forms
    const resetForms = () => {
      Object.keys(adminLoginForm).forEach(key => {
        adminLoginForm[key] = key === 'remember' ? false : ''
      })
      Object.keys(loginForm).forEach(key => {
        loginForm[key] = key === 'remember' ? false : ''
      })
      Object.keys(registerForm).forEach(key => {
        registerForm[key] = ''
      })
    }

    // Handle admin login
    const handleAdminLogin = async () => {
      if (!adminLoginForm.username || !adminLoginForm.password) {
        ElMessage.error('Please fill in all required fields')
        return
      }

      try {
        await authStore.adminLogin({
          username: adminLoginForm.username,
          password: adminLoginForm.password
        })

        // Handle Remember Me
        handleRememberMe(
          'admin',
          adminLoginForm.username,
          adminLoginForm.remember
        )

        ElMessage.success('Admin login successful')
        // Navigate to admin dashboard
        router.push('/admin')
      } catch (error) {
        const message = error.response?.data?.message || 'Login failed'
        ElMessage.error(message)
      }
    }

    // Handle student/teacher login
    const handleLogin = async () => {
      if (!loginForm.username || !loginForm.password) {
        ElMessage.error('Please fill in all required fields')
        return
      }

      try {
        if (currentRole.value === 'student') {
          await authStore.studentLogin({
            username: loginForm.username,
            password: loginForm.password
          })
        } else {
          await authStore.teacherLogin({
            username: loginForm.username,
            password: loginForm.password
          })
        }

        // Handle Remember Me
        handleRememberMe(
          currentRole.value,
          loginForm.username,
          loginForm.remember
        )

        ElMessage.success(`${currentRole.value} login successful`)
        // Navigate to respective dashboard
        if (currentRole.value === 'student') {
          router.push('/student')
        } else {
          router.push('/teacher')
        }
      } catch (error) {
        const message = error.response?.data?.message || 'Login failed'
        ElMessage.error(message)
      }
    }

    // Handle student/teacher registration
    const handleRegister = async () => {
      // Validate required fields
      const requiredFields = [
        'name',
        'username',
        'email',
        'phone',
        'password',
        'confirmPassword'
      ]
      if (currentRole.value === 'student') {
        requiredFields.push('studentNo')
      }

      for (const field of requiredFields) {
        if (!registerForm[field]) {
          ElMessage.error(
            `Please fill in ${field.replace(/([A-Z])/g, ' $1').toLowerCase()}`
          )
          return
        }
      }

      // Validate password confirmation
      if (registerForm.password !== registerForm.confirmPassword) {
        ElMessage.error('Passwords do not match')
        return
      }

      // Validate password strength
      if (registerForm.password.length < 6) {
        ElMessage.error('Password must be at least 6 characters long')
        return
      }

      try {
        const userData = {
          name: registerForm.name,
          username: registerForm.username,
          email: registerForm.email,
          phone: registerForm.phone,
          password: registerForm.password
        }

        if (currentRole.value === 'student') {
          userData.studentNo = registerForm.studentNo
          await authStore.studentRegister(userData)
        } else {
          await authStore.teacherRegister(userData)
        }

        ElMessage.success(`${currentRole.value} registration successful`)
        // Switch to login mode after successful registration
        isLoginMode.value = true
        resetForms()
      } catch (error) {
        const message = error.response?.data?.message || 'Registration failed'
        ElMessage.error(message)
      }
    }

    // Initialize auth state on component mount
    onMounted(() => {
      authStore.initAuth()
      loadRememberedCredentials()
    })

    // Load remembered credentials from localStorage
    const loadRememberedCredentials = () => {
      const rememberedRole = localStorage.getItem('rememberedRole')
      const rememberedUsername = localStorage.getItem('rememberedUsername')
      const rememberedRemember = localStorage.getItem('rememberedRemember')

      if (
        rememberedRole &&
        rememberedUsername &&
        rememberedRemember === 'true'
      ) {
        // Set the remembered role
        currentRole.value = rememberedRole

        // Set the remembered username and remember checkbox
        if (rememberedRole === 'admin') {
          adminLoginForm.username = rememberedUsername
          adminLoginForm.remember = true
        } else {
          loginForm.username = rememberedUsername
          loginForm.remember = true
        }
      }
    }

    // Save or clear remembered credentials
    const handleRememberMe = (role, username, remember) => {
      if (remember) {
        localStorage.setItem('rememberedRole', role)
        localStorage.setItem('rememberedUsername', username)
        localStorage.setItem('rememberedRemember', 'true')
      } else {
        localStorage.removeItem('rememberedRole')
        localStorage.removeItem('rememberedUsername')
        localStorage.removeItem('rememberedRemember')
      }
    }

    return {
      currentRole,
      isLoginMode,
      loading,
      authStore,
      adminLoginForm,
      loginForm,
      registerForm,
      switchRole,
      switchMode,
      handleAdminLogin,
      handleLogin,
      handleRegister,
      handleRememberMe
    }
  }
}
</script>

<style scoped>
.auth-container {
  width: 450px;
  margin: 50px auto;
  border: 2px solid #e4e7ed;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.role-tabs {
  display: flex;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.role-tab {
  flex: 1;
  text-align: center;
  padding: 16px 0;
  font-size: 16px;
  cursor: pointer;
  border-right: 1px solid #e4e7ed;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.role-tab:last-child {
  border-right: none;
}

.role-tab:hover {
  background: #ecf5ff;
  color: #409eff;
}

.role-tab.active {
  background: #409eff;
  color: white;
  font-weight: 600;
}

.auth-card {
  padding: 32px;
}

.auth-tabs {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  font-size: 20px;
}

.auth-tab {
  cursor: pointer;
  font-weight: normal;
  margin: 0 8px;
  padding: 8px 16px;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.auth-tab:hover {
  background: #f5f7fa;
}

.auth-tab.active {
  font-weight: bold;
  color: #409eff;
  background: #ecf5ff;
}

.divider {
  margin: 0 12px;
  font-size: 20px;
  color: #c0c4cc;
}

.admin-login h2,
.auth-form h2 {
  text-align: center;
  margin-bottom: 24px;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.auth-form {
  margin-top: 12px;
}

.form-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.forgot-link {
  color: #409eff;
  font-size: 14px;
  text-decoration: none;
  transition: color 0.3s ease;
}

.forgot-link:hover {
  color: #66b1ff;
}

.submit-btn {
  width: 100%;
  font-size: 16px;
  font-weight: 600;
  padding: 12px 0;
  background: #409eff;
  border: none;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.submit-btn:hover {
  background: #66b1ff;
  transform: translateY(-1px);
}

.submit-btn:active {
  transform: translateY(0);
}

/* Responsive design */
@media (max-width: 480px) {
  .auth-container {
    width: 95%;
    margin: 20px auto;
  }

  .auth-card {
    padding: 20px;
  }

  .role-tab {
    font-size: 14px;
    padding: 12px 0;
  }
}
</style>
