import api from './auth.js'

// Admin API methods
export const adminAPI = {
  // Get pending teachers for approval
  getPendingTeachers: () => {
    return api.get('/api/admin/teachers/pending')
  },

  // Approve teacher registration
  approveTeacher: teacherId => {
    return api.post(`/api/admin/teachers/${teacherId}/approve`)
  },

  // Reject teacher registration
  rejectTeacher: teacherId => {
    return api.post(`/api/admin/teachers/${teacherId}/reject`)
  },

  // Delete teacher
  deleteTeacher: teacherId => {
    return api.delete(`/api/admin/teachers/${teacherId}`)
  },

  // Get groups with pagination and search
  getGroups: (params = {}) => {
    return api.get('/api/admin/groups', { params })
  },

  // Create new group
  createGroup: groupData => {
    return api.post('/api/admin/groups', groupData)
  },

  // Update group
  updateGroup: groupData => {
    return api.put('/api/admin/groups', groupData)
  },

  // Delete group
  deleteGroup: groupId => {
    return api.delete(`/api/admin/groups/${groupId}`)
  },

  // Get students with pagination and search
  getStudents: (params = {}) => {
    return api.get('/api/admin/students', { params })
  },

  // Create new student
  createStudent: studentData => {
    return api.post('/api/admin/students', studentData)
  },

  // Update student
  updateStudent: studentData => {
    return api.put('/api/admin/students', studentData)
  },

  // Delete student
  deleteStudent: studentId => {
    return api.delete(`/api/admin/students/${studentId}`)
  },

  // Get teachers with pagination and search
  getTeachers: (params = {}) => {
    return api.get('/api/admin/teachers', { params })
  },

  // Create new teacher
  createTeacher: teacherData => {
    return api.post('/api/admin/teachers', teacherData)
  },

  // Update teacher
  updateTeacher: teacherData => {
    return api.put('/api/admin/teachers', teacherData)
  },

  // Create user
  createUser: userData => {
    return api.post('/api/admin/users', userData)
  },

  // Update user
  updateUser: userData => {
    return api.put('/api/admin/users', userData)
  },

  // Get user registration trend
  getRegistrationTrend: () => {
    return api.get('/api/admin/registration-trend')
  },

  // Get recent activities
  getRecentActivities: (params = {}) => {
    return api.get('/api/admin/recent-activities', { params })
  },

  // 批量导入学生
  batchImportStudents: file => {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/api/admin/students/batch-import', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 批量导入教师
  batchImportTeachers: file => {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/api/admin/teachers/batch-import', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
}

export default adminAPI
