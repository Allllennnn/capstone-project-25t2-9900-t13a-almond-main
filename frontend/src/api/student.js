import api from './auth.js'

// Student API methods
export const studentAPI = {
  // Get student info
  getStudentInfo: () => {
    return api.get('/api/student/info')
  },

  // Update student info
  updateStudentInfo: data => {
    return api.put('/api/student/info', data)
  },

  // Get student tasks
  getStudentTasks: (params = {}) => {
    return api.get('/api/student/tasks', { params })
  },

  // Join group
  joinGroup: groupId => {
    return api.post(`/api/student/groups/${groupId}/join`)
  },

  // Leave group
  leaveGroup: groupId => {
    return api.post(`/api/student/groups/${groupId}/leave`)
  },

  // Get all groups that student belongs to
  getStudentGroups: () => {
    return api.get('/api/student/groups')
  },

  // Get tasks for a specific group
  getGroupTasks: groupId => {
    return api.get(`/api/student/groups/${groupId}/tasks`)
  },

  // Get members of a specific group
  getGroupMembers: groupId => {
    return api.get(`/api/student/groups/${groupId}/members`)
  },

  // Submit task assignment
  submitTaskAssignment: data => {
    return api.post('/api/student/task-assignments', data)
  },

  // 分析每周进度
  analyzeWeeklyProgress: (taskId, weekNo, meetingDocumentUrl) => {
    return api.post('/api/student/agent/analyze-weekly-progress', {
      taskId,
      weekNo,
      meetingDocumentUrl
    })
  },

  // Get task assignment status
  getTaskAssignmentStatus: taskId => {
    return api.get(`/api/student/task-assignments/${taskId}/status`)
  },

  // Update weekly goal
  updateWeeklyGoal: data => {
    return api.put(`/api/student/weekly-goals/${data.id}`, data)
  },

  // Generate weekly goal using AI
  generateWeeklyGoal: (taskId, weekNo) => {
    return api.post(
      `/api/student/tasks/${taskId}/weekly-goals/generate?weekNo=${weekNo}`
    )
  },

  // Get weekly goals for a task
  getWeeklyGoals: taskId => {
    return api.get(`/api/student/tasks/${taskId}/weekly-goals`)
  },

  // Get task meetings
  getTaskMeetings: taskId => {
    return api.get(`/api/student/tasks/${taskId}/meetings`)
  },

  // Get student weekly goals
  getStudentWeeklyGoals: taskId => {
    return api.get(`/api/student/tasks/${taskId}/weekly-goals`)
  },

  // Get conversation history for a task
  getConversationHistory: taskId => {
    return api.get(`/api/student/tasks/${taskId}/conversations`)
  },

  // Get agent advice
  getAgentAdvice: taskId => {
    return api.post(`/api/student/agent/advice`, { taskId })
  },

  // Get finalized assignments
  getFinalizedAssignments: taskId => {
    return api.get(`/api/student/tasks/${taskId}/finalized-assignments`)
  },

  // Update task assignment
  updateTaskAssignment: data => {
    return api.put('/api/student/task-assignments', data)
  },

  // Finalize task assignment
  finalizeTaskAssignment: taskId => {
    return api.post(`/api/student/task-assignments/${taskId}/finalize`)
  },

  // Confirm task assignment with cycle
  confirmTaskAssignment: (taskId, cycle) => {
    return api.post(`/api/student/task-assignments/${taskId}/confirm`, {
      cycle
    })
  },

  // Send message to agent conversation
  sendMessage: (taskId, content) => {
    return api.post('/api/student/conversations/messages', {
      taskId,
      content
    })
  },

  // Check meeting upload requirements
  checkMeetingUploadRequirements: (taskId, meetingNo) => {
    return api.post('/api/student/meetings/check-upload-requirements', {
      taskId,
      meetingNo
    })
  },

  // Meeting related APIs
  getMeetings: taskId => {
    return api.get(`/api/meeting/task/${taskId}`)
  },

  getMeetingUploadValidation: taskId => {
    return api.get(`/api/meeting/upload-validation/${taskId}`)
  },

  getMeetingGoalsStatus: taskId => {
    return api.get(`/api/meeting/goals-status/${taskId}`)
  },

  uploadMeeting: data => {
    return api.post('/api/meeting/upload', data)
  },

  uploadMeetingDocument: data => {
    return api.post('/api/meeting/upload', data)
  },

  completeMeeting: meetingId => {
    return api.post(`/api/meeting/complete/${meetingId}`)
  }
}

export default studentAPI
