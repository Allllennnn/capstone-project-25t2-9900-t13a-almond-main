<template>
  <div class="api-test">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>API Connection Test</span>
        </div>
      </template>

      <el-row :gutter="20">
        <el-col :span="12">
          <h3>Authentication Test</h3>
          <el-form :model="authForm" label-width="120px">
            <el-form-item label="Username">
              <el-input
                v-model="authForm.username"
                placeholder="Enter username"
              />
            </el-form-item>
            <el-form-item label="Password">
              <el-input
                v-model="authForm.password"
                type="password"
                placeholder="Enter password"
              />
            </el-form-item>
            <el-form-item label="Role">
              <el-select v-model="authForm.role" placeholder="Select role">
                <el-option label="Admin" value="ADMIN" />
                <el-option label="Teacher" value="TEACHER" />
                <el-option label="Student" value="STUDENT" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                @click="testLogin"
                :loading="loading.login"
              >
                Test Login
              </el-button>
            </el-form-item>
          </el-form>

          <div v-if="loginResult" class="result-box">
            <h4>Login Result:</h4>
            <pre>{{ JSON.stringify(loginResult, null, 2) }}</pre>
          </div>
        </el-col>

        <el-col :span="12">
          <h3>API Endpoints Test</h3>
          <div class="endpoint-tests">
            <el-button
              v-for="test in endpointTests"
              :key="test.name"
              @click="test.endpoint"
              :loading="test.loading"
              :type="test.result ? 'success' : 'primary'"
              style="margin: 5px"
            >
              {{ test.name }}
            </el-button>
          </div>

          <div v-if="endpointResults.length > 0" class="result-box">
            <h4>Endpoint Results:</h4>
            <div
              v-for="result in endpointResults"
              :key="result.name"
              class="result-item"
            >
              <h5>{{ result.name }}:</h5>
              <pre>{{ JSON.stringify(result.data, null, 2) }}</pre>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { authAPI } from '@/api/auth'
import { adminAPI } from '@/api/admin'

export default {
  name: 'ApiTest',
  setup() {
    const loading = ref({
      login: false
    })

    const authForm = ref({
      username: 'admin',
      password: '123456',
      role: 'ADMIN'
    })

    const loginResult = ref(null)

    const endpointTests = ref([
      {
        name: 'Get Pending Teachers',
        loading: false,
        result: false,
        endpoint: async () => {
          endpointTests.value[0].loading = true
          try {
            const response = await adminAPI.getPendingTeachers()
            endpointResults.value.push({
              name: 'Get Pending Teachers',
              data: response.data
            })
            endpointTests.value[0].result = true
            ElMessage.success('Get Pending Teachers: Success')
          } catch (error) {
            ElMessage.error('Get Pending Teachers: Failed')
            console.error(error)
          } finally {
            endpointTests.value[0].loading = false
          }
        }
      },
      {
        name: 'Get Students',
        loading: false,
        result: false,
        endpoint: async () => {
          endpointTests.value[1].loading = true
          try {
            const response = await adminAPI.getStudents()
            endpointResults.value.push({
              name: 'Get Students',
              data: response.data
            })
            endpointTests.value[1].result = true
            ElMessage.success('Get Students: Success')
          } catch (error) {
            ElMessage.error('Get Students: Failed')
            console.error(error)
          } finally {
            endpointTests.value[1].loading = false
          }
        }
      },
      {
        name: 'Get Teachers',
        loading: false,
        result: false,
        endpoint: async () => {
          endpointTests.value[2].loading = true
          try {
            const response = await adminAPI.getTeachers()
            endpointResults.value.push({
              name: 'Get Teachers',
              data: response.data
            })
            endpointTests.value[2].result = true
            ElMessage.success('Get Teachers: Success')
          } catch (error) {
            ElMessage.error('Get Teachers: Failed')
            console.error(error)
          } finally {
            endpointTests.value[2].loading = false
          }
        }
      },
      {
        name: 'Get Groups',
        loading: false,
        result: false,
        endpoint: async () => {
          endpointTests.value[3].loading = true
          try {
            const response = await adminAPI.getGroups()
            endpointResults.value.push({
              name: 'Get Groups',
              data: response.data
            })
            endpointTests.value[3].result = true
            ElMessage.success('Get Groups: Success')
          } catch (error) {
            ElMessage.error('Get Groups: Failed')
            console.error(error)
          } finally {
            endpointTests.value[3].loading = false
          }
        }
      }
    ])

    const endpointResults = ref([])

    const testLogin = async () => {
      loading.value.login = true
      try {
        let response
        if (authForm.value.role === 'ADMIN') {
          response = await authAPI.adminLogin(authForm.value)
        } else if (authForm.value.role === 'TEACHER') {
          response = await authAPI.teacherLogin(authForm.value)
        } else {
          response = await authAPI.studentLogin(authForm.value)
        }

        loginResult.value = response.data
        ElMessage.success('Login test successful')

        // 如果登录成功，保存token
        if (response.data.code === 1) {
          localStorage.setItem('authToken', response.data.data)
        }
      } catch (error) {
        loginResult.value = {
          error: true,
          message: error.message,
          response: error.response?.data,
          status: error.response?.status
        }
        ElMessage.error('Login test failed')
        console.error('Login error:', error)
      } finally {
        loading.value.login = false
      }
    }

    return {
      loading,
      authForm,
      loginResult,
      endpointTests,
      endpointResults,
      testLogin
    }
  }
}
</script>

<style scoped>
.api-test {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.result-box {
  margin-top: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
}

.result-box h4 {
  margin: 0 0 10px 0;
  color: #303133;
}

.result-box pre {
  background-color: #fff;
  padding: 10px;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
  overflow-x: auto;
  font-size: 12px;
  line-height: 1.4;
}

.endpoint-tests {
  margin-bottom: 20px;
}

.result-item {
  margin-bottom: 15px;
}

.result-item h5 {
  margin: 0 0 5px 0;
  color: #303133;
}
</style>
