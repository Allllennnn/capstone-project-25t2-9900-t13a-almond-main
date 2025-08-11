import api from './auth.js'

/**
 * Teacher-related API
 */
export const teacherAPI = {
  // Get teacher dashboard statistics
  getDashboardStats() {
    return api.get('/api/teacher/dashboard/stats')
  },

  // Get teacher's recent activities
  getRecentActivities(params) {
    return api.get('/api/teacher/recent-activities', { params })
  },

  // Get teacher tasks
  getTasks(params) {
    return api.get('/api/teacher/tasks', { params })
  },

  // Complete a task
  completeTask(taskId) {
    return api.post(`/api/teacher/tasks/${taskId}/complete`)
  },

  // Get teacher's student list
  getStudents(params) {
    return api.get('/api/teacher/students', { params })
  },

  // Create a student
  createStudent(studentData) {
    return api.post('/api/teacher/students', studentData)
  },

  // Update a student
  updateStudent(studentId, studentData) {
    return api.put(`/api/teacher/students/${studentId}`, studentData)
  },

  // Get teacher's group list
  getGroups(params) {
    return api.get('/api/teacher/groups', { params })
  },

  // Create a group
  createGroup(data) {
    return api.post('/api/teacher/groups', data)
  },

  // Remote search for a student
  searchStudent(name) {
    return api.get('/api/teacher/searchStudent', { params: { name } })
  },

  // Batch import students
  batchImportStudents: file => {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/api/teacher/students/batch-import', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // Batch import groups
  batchImportGroups: file => {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/api/teacher/groups/batch-import', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // Batch import tasks
  batchImportTasks: file => {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/api/teacher/tasks/batch-import', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
}
