<template>
  <div class="dashboard-overview">
    <!-- Tasks Overview -->
    <div class="section">
      <div class="section-header">
        <h2>Tasks Overview</h2>
        <p>Track your project progress</p>
      </div>

      <div class="tasks-grid">
        <div v-if="tasks.length === 0" class="empty-state">
          <el-icon class="empty-icon"><Document /></el-icon>
          <p>No tasks available</p>
        </div>

        <div
          v-for="task in tasks"
          :key="task.id"
          class="task-card"
          @click="openTaskDetails(task)"
        >
          <div class="task-header">
            <h3 class="task-title">{{ task.title }}</h3>
            <el-tag
              :type="
                getTaskStatusType(
                  getTaskStatus(task, task.completedMeetings, task.totalWeeks)
                )
              "
              size="small"
            >
              {{
                getTaskStatusText(
                  getTaskStatus(task, task.completedMeetings, task.totalWeeks)
                )
              }}
            </el-tag>
          </div>
          <div class="task-group">{{ task.groupName }}</div>
          <div class="task-progress">
            <div class="progress-info">
              <span
                >Week {{ task.completedMeetings }} of
                {{ task.totalWeeks }}</span
              >
              <span>{{ Math.round(task.progress) }}%</span>
            </div>
            <el-progress
              :percentage="task.progress"
              :status="task.progress === 100 ? 'success' : 'active'"
              :stroke-width="6"
            />
          </div>
          <div class="task-meetings">
            <el-icon><Calendar /></el-icon>
            <span
              >{{ task.completedMeetings }}/{{ task.totalWeeks }} meetings
              completed</span
            >
          </div>
        </div>
      </div>
    </div>

    <!-- Weekly Goals -->
    <div class="section">
      <div class="section-header">
        <h2>Weekly Goals</h2>
        <p>Your current objectives</p>
      </div>

      <el-tabs v-model="activeGoalTab" class="goals-tabs">
        <el-tab-pane label="Not Uploaded" name="not_uploaded">
          <div class="goals-grid">
            <div
              v-if="filteredGoals('NOTUPLOADED').length === 0"
              class="empty-state"
            >
              <el-icon class="empty-icon"><Calendar /></el-icon>
              <p>No goals with this status</p>
            </div>

            <div
              v-for="goal in filteredGoals('NOTUPLOADED')"
              :key="goal.id"
              class="goal-card"
              @click="openGoalDetails(goal)"
            >
              <div class="goal-header">
                <h3 class="goal-title">{{ goal.title || goal.goal }}</h3>
                <el-tag type="info" size="small">Not Uploaded</el-tag>
              </div>
              <div class="goal-project">{{ goal.projectName }}</div>
              <div class="goal-week">Week {{ goal.weekNo }}</div>
              <div class="goal-due">Due: {{ formatDate(goal.dueDate) }}</div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="In Progress" name="in_progress">
          <div class="goals-grid">
            <div
              v-if="filteredGoals('PROCESSING').length === 0"
              class="empty-state"
            >
              <el-icon class="empty-icon"><Calendar /></el-icon>
              <p>No goals with this status</p>
            </div>

            <div
              v-for="goal in filteredGoals('PROCESSING')"
              :key="goal.id"
              class="goal-card"
              @click="openGoalDetails(goal)"
            >
              <div class="goal-header">
                <h3 class="goal-title">{{ goal.title || goal.goal }}</h3>
                <el-tag type="warning" size="small">In Progress</el-tag>
              </div>
              <div class="goal-project">{{ goal.projectName }}</div>
              <div class="goal-week">Week {{ goal.weekNo }}</div>
              <div class="goal-due">Due: {{ formatDate(goal.dueDate) }}</div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="Finished" name="finished">
          <div class="goals-grid">
            <div
              v-if="filteredGoals('FINISHED').length === 0"
              class="empty-state"
            >
              <el-icon class="empty-icon"><Calendar /></el-icon>
              <p>No goals with this status</p>
            </div>

            <div
              v-for="goal in filteredGoals('FINISHED')"
              :key="goal.id"
              class="goal-card"
              @click="openGoalDetails(goal)"
            >
              <div class="goal-header">
                <h3 class="goal-title">{{ goal.title || goal.goal }}</h3>
                <el-tag type="success" size="small">Finished</el-tag>
              </div>
              <div class="goal-project">{{ goal.projectName }}</div>
              <div class="goal-week">Week {{ goal.weekNo }}</div>
              <div class="goal-due">Due: {{ formatDate(goal.dueDate) }}</div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- Meeting Notes -->
    <div class="section">
      <div class="section-header">
        <h2>Meeting Notes</h2>
        <p>Track your team meetings</p>
      </div>

      <el-tabs v-model="activeMeetingTab" class="meetings-tabs">
        <el-tab-pane label="Unfinished" name="unfinished">
          <div class="meetings-grid">
            <div v-if="upcomingMeetings.length === 0" class="empty-state">
              <el-icon class="empty-icon"><Calendar /></el-icon>
              <p>No unfinished meetings</p>
            </div>

            <div
              v-for="meeting in upcomingMeetings"
              :key="meeting.id"
              class="meeting-card upcoming"
              @click="openMeetingDetails(meeting)"
            >
              <div class="meeting-header">
                <h3 class="meeting-title">{{ meeting.title }}</h3>
                <el-tag type="warning" size="small">Unfinished</el-tag>
              </div>
              <div class="meeting-task">{{ meeting.taskTitle }}</div>
              <div class="meeting-week">
                Week {{ meeting.weekNumber }} Meeting
              </div>
              <div class="meeting-time">
                {{ formatDateTime(meeting.scheduledTime) }}
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="Completed" name="completed">
          <div class="meetings-grid">
            <div v-if="completedMeetings.length === 0" class="empty-state">
              <el-icon class="empty-icon"><Calendar /></el-icon>
              <p>No completed meetings</p>
            </div>

            <div
              v-for="meeting in completedMeetings"
              :key="meeting.id"
              class="meeting-card completed"
              @click="openMeetingDetails(meeting)"
            >
              <div class="meeting-header">
                <h3 class="meeting-title">{{ meeting.title }}</h3>
                <el-tag type="success" size="small">Completed</el-tag>
              </div>
              <div class="meeting-task">{{ meeting.taskTitle }}</div>
              <div class="meeting-week">
                Week {{ meeting.weekNumber }} Meeting
              </div>
              <div class="meeting-time">
                {{ formatDateTime(meeting.completedTime) }}
              </div>
              <div v-if="meeting.documentUrl" class="meeting-document">
                <el-button type="text" size="small">
                  <el-icon><Document /></el-icon>
                  Document Available
                </el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- Goal Details Dialog -->
    <el-dialog v-model="goalDialogVisible" title="Goal Details" width="600px">
      <div v-if="selectedGoal" class="goal-details">
        <div class="details-grid">
          <div class="detail-item">
            <span class="detail-label">Project:</span>
            <span class="detail-value">{{ selectedGoal.projectName }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">Week:</span>
            <span class="detail-value">Week {{ selectedGoal.weekNo }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">Status:</span>
            <el-tag :type="getGoalStatusType(selectedGoal.status)" size="small">
              {{ getGoalStatusText(selectedGoal.status) }}
            </el-tag>
          </div>
          <div class="detail-item">
            <span class="detail-label">Due Date:</span>
            <span class="detail-value">{{
              formatDate(selectedGoal.dueDate)
            }}</span>
          </div>
        </div>
        <div class="goal-content">
          <h4>Goal Description:</h4>
          <p class="goal-description">{{ selectedGoal.goal }}</p>
        </div>
      </div>
    </el-dialog>

    <!-- Meeting Details Dialog -->
    <el-dialog
      v-model="meetingDialogVisible"
      title="Meeting Details"
      width="700px"
    >
      <div v-if="selectedMeeting" class="meeting-details">
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">Task:</span>
            <span class="info-value">{{ selectedMeeting.taskTitle }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">Week:</span>
            <span class="info-value"
              >Week {{ selectedMeeting?.weekNumber }}</span
            >
          </div>
          <div class="info-item">
            <span class="info-label">Status:</span>
            <el-tag
              :type="selectedMeeting?.completed ? 'success' : 'warning'"
              size="small"
            >
              {{ selectedMeeting?.completed ? 'Completed' : 'Unfinished' }}
            </el-tag>
          </div>
          <div class="info-item">
            <span class="info-label">Time:</span>
            <span class="info-value">{{
              formatDateTime(
                selectedMeeting?.scheduledTime || selectedMeeting?.completedTime
              )
            }}</span>
          </div>
        </div>

        <div v-if="selectedMeeting.documentUrl" class="document-section">
          <h4>Meeting Document:</h4>
          <div class="document-preview">
            <div class="file-info">
              <el-icon><Document /></el-icon>
              <span class="file-name">{{
                getFileName(selectedMeeting.documentUrl)
              }}</span>
            </div>
            <div class="file-actions">
              <el-button
                type="primary"
                size="small"
                @click="previewFile(selectedMeeting.documentUrl)"
              >
                <el-icon><View /></el-icon>
                Preview
              </el-button>
              <el-button
                size="small"
                @click="downloadFile(selectedMeeting.documentUrl)"
              >
                <el-icon><Download /></el-icon>
                Download
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- File Preview Dialog -->
    <el-dialog
      v-model="previewDialogVisible"
      title="File Preview"
      width="80%"
      top="5vh"
    >
      <div class="file-preview-container">
        <div v-if="!previewUrl" class="loading-state">
          <el-icon class="is-loading"><Loading /></el-icon>
          <p>Loading preview...</p>
        </div>
        <iframe
          v-else
          :src="previewUrl"
          width="100%"
          height="600px"
          frameborder="0"
        ></iframe>
      </div>
      <template #footer>
        <el-button @click="previewDialogVisible = false">Close</el-button>
      </template>
    </el-dialog>

    <!-- Task Details Dialog -->
    <el-dialog v-model="taskDialogVisible" title="Task Details" width="600px">
      <div v-if="selectedTask" class="task-details">
        <div class="details-grid">
          <div class="detail-item">
            <span class="detail-label">Title:</span>
            <span class="detail-value">{{ selectedTask.title }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">Group:</span>
            <span class="detail-value">{{ selectedTask.groupName }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">Status:</span>
            <el-tag
              :type="
                getTaskStatusType(
                  getTaskStatus(
                    selectedTask,
                    selectedTask.completedMeetings,
                    selectedTask.totalWeeks
                  )
                )
              "
              size="small"
            >
              {{
                getTaskStatusText(
                  getTaskStatus(
                    selectedTask,
                    selectedTask.completedMeetings,
                    selectedTask.totalWeeks
                  )
                )
              }}
            </el-tag>
          </div>
          <div class="detail-item">
            <span class="detail-label">Progress:</span>
            <span class="detail-value"
              >{{ Math.round(selectedTask.progress) }}%</span
            >
          </div>
          <div class="detail-item">
            <span class="detail-label">Current Week:</span>
            <span class="detail-value"
              >Week {{ selectedTask.currentWeek }}</span
            >
          </div>
          <div class="detail-item">
            <span class="detail-label">Total Weeks:</span>
            <span class="detail-value">{{ selectedTask.totalWeeks }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">Completed Meetings:</span>
            <span class="detail-value">{{
              selectedTask.completedMeetings
            }}</span>
          </div>
        </div>
        <div class="task-content">
          <h4>Task Description:</h4>
          <p class="task-description">{{ selectedTask.description }}</p>
        </div>

        <div v-if="selectedTask.fileUrl" class="document-section">
          <h4>Task File:</h4>
          <div class="document-preview">
            <div class="file-info">
              <el-icon><Document /></el-icon>
              <span class="file-name">{{
                getFileName(selectedTask.fileUrl)
              }}</span>
            </div>
            <div class="file-actions">
              <el-button
                type="primary"
                size="small"
                @click="previewFile(selectedTask.fileUrl)"
              >
                <el-icon><View /></el-icon>
                Preview
              </el-button>
              <el-button
                size="small"
                @click="downloadFile(selectedTask.fileUrl)"
              >
                <el-icon><Download /></el-icon>
                Download
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import {
  Calendar,
  Document,
  View,
  Download,
  Loading
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import studentAPI from '@/api/student'

// Reactive data
const tasks = ref([])
const weeklyGoals = ref([])
const upcomingMeetings = ref([])
const completedMeetings = ref([])
const loading = ref(false)

// Dialog states
const goalDialogVisible = ref(false)
const meetingDialogVisible = ref(false)
const previewDialogVisible = ref(false)
const taskDialogVisible = ref(false)
const selectedGoal = ref(null)
const selectedMeeting = ref(null)
const selectedTask = ref(null)
const previewUrl = ref('')

// Tab states
const activeGoalTab = ref('in_progress')
const activeMeetingTab = ref('unfinished')

// Computed properties
const filteredGoals = status => {
  return weeklyGoals.value.filter(goal => goal.status === status)
}

const openMeetingDetails = meeting => {
  selectedMeeting.value = meeting
  meetingDialogVisible.value = true
}

const openGoalDetails = goal => {
  selectedGoal.value = goal
  goalDialogVisible.value = true
}

const openTaskDetails = task => {
  selectedTask.value = task
  taskDialogVisible.value = true
}

// Load dashboard data
const loadDashboardData = async () => {
  loading.value = true
  try {
    // Get student groups first
    const groupsResponse = await studentAPI.getStudentGroups()
    if (groupsResponse.data.code !== 1) {
      throw new Error('Failed to load groups')
    }

    const groups = groupsResponse.data.data
    const allTasks = []
    const allGoals = []
    const allMeetings = []

    // Load data for each group
    for (const group of groups) {
      try {
        // Get tasks for this group
        const tasksResponse = await studentAPI.getGroupTasks(group.id)
        if (tasksResponse.data.code === 1) {
          const groupTasks = tasksResponse.data.data

          // Process each task
          for (const task of groupTasks) {
            let completedMeetingsCount = 0
            let totalWeeks = task.cycle || 4
            let currentWeek = 1

            // Load meetings for this task
            try {
              const meetingsResponse = await studentAPI.getMeetings(task.id)
              if (meetingsResponse.data.code === 1) {
                const taskMeetings = meetingsResponse.data.data.map(
                  (meeting, index) => ({
                    ...meeting,
                    taskId: task.id,
                    taskTitle: task.title,
                    title: `${task.title} - Week ${
                      meeting.meetingNo || index + 1
                    }`,
                    weekNumber: meeting.meetingNo || index + 1,
                    completed: meeting.status === 'COMPLETED',
                    scheduledTime: meeting.meetingDate || meeting.createdAt,
                    completedTime: meeting.updatedAt
                  })
                )
                allMeetings.push(...taskMeetings)

                // Calculate progress based on completed meetings
                completedMeetingsCount = taskMeetings.filter(
                  m => m.completed
                ).length
                // currentWeek should show the completed weeks, not the next week
                currentWeek = completedMeetingsCount
              }
            } catch (error) {
              console.error(
                `Error loading meetings for task ${task.id}:`,
                error
              )
            }

            // Load weekly goals for this task
            try {
              const goalsResponse = await studentAPI.getStudentWeeklyGoals(
                task.id
              )
              if (goalsResponse.data.code === 1) {
                const taskGoals = goalsResponse.data.data.map(goal => ({
                  ...goal,
                  taskId: task.id,
                  projectName: task.title,
                  title: `Week ${goal.weekNo} Goal`,
                  dueDate: goal.dueDate || new Date()
                }))
                allGoals.push(...taskGoals)
              }
            } catch (error) {
              console.error(`Error loading goals for task ${task.id}:`, error)
            }

            // Process task with calculated progress
            const progress =
              totalWeeks > 0 ? (completedMeetingsCount / totalWeeks) * 100 : 0
            const processedTask = {
              ...task,
              groupName: group.name,
              progress: Math.min(progress, 100),
              currentWeek: Math.min(currentWeek, totalWeeks),
              totalWeeks,
              completedMeetings: completedMeetingsCount
            }
            allTasks.push(processedTask)
          }
        }
      } catch (error) {
        console.error(`Error loading data for group ${group.id}:`, error)
      }
    }

    // Set the data
    tasks.value = allTasks
    weeklyGoals.value = allGoals
    upcomingMeetings.value = allMeetings.filter(m => !m.completed)
    completedMeetings.value = allMeetings.filter(m => m.completed)

    console.log('Loaded data:', {
      tasks: allTasks,
      goals: allGoals,
      meetings: allMeetings
    })
  } catch (error) {
    console.error('Error loading dashboard data:', error)
    ElMessage.error('Failed to load dashboard data')
  } finally {
    loading.value = false
  }
}

// Helper functions
const getTaskStatus = (task, completedMeetings, totalWeeks) => {
  // 如果所有会议都完成了，状态应该是COMPLETED
  if (completedMeetings >= totalWeeks) return 'COMPLETED'

  // 否则使用后端返回的状态
  return task.status || 'INITIALIZING'
}

const getTaskStatusType = status => {
  switch (status) {
    case 'COMPLETED':
      return 'success'
    case 'IN_PROGRESS':
      return 'warning'
    case 'INITIALIZING':
      return 'info'
    default:
      return 'info'
  }
}

const getTaskStatusText = status => {
  switch (status) {
    case 'COMPLETED':
      return 'Completed'
    case 'IN_PROGRESS':
      return 'In Progress'
    case 'INITIALIZING':
      return 'Initializing'
    default:
      return 'Unknown'
  }
}

const getGoalStatusType = status => {
  switch (status) {
    case 'FINISHED':
      return 'success'
    case 'PROCESSING':
      return 'warning'
    case 'NOTUPLOADED':
      return 'info'
    default:
      return 'info'
  }
}

const getGoalStatusText = status => {
  switch (status) {
    case 'FINISHED':
      return 'Finished'
    case 'PROCESSING':
      return 'In Progress'
    case 'NOTUPLOADED':
      return 'Not Uploaded'
    default:
      return 'Unknown'
  }
}

const formatDate = date => {
  if (!date) return 'N/A'
  return new Date(date).toLocaleDateString()
}

const formatDateTime = dateTime => {
  if (!dateTime) return 'N/A'
  return new Date(dateTime).toLocaleString()
}

const getFileName = url => {
  if (!url) return 'Unknown file'
  return url.split('/').pop() || 'Unknown file'
}

const previewFile = async url => {
  if (!url) return

  try {
    previewUrl.value = ''
    previewDialogVisible.value = true

    // 首先获取签名URL
    let signedUrl = url
    try {
      const response = await fetch('/api/file/preview-url', {
        method: 'POST',
        headers: {
          'Content-Type': 'text/plain',
          Authorization: `Bearer ${localStorage.getItem('authToken')}`
        },
        body: url
      })

      if (response.ok) {
        const result = await response.json()
        if (result.code === 1) {
          signedUrl = result.data
          console.info('获取到签名URL:', signedUrl)
        }
      }
    } catch (e) {
      console.warn('获取签名URL失败，使用原始URL:', e.message)
    }

    // 根据文件扩展名确定预览方式
    const extension = url.split('.').pop()?.toLowerCase()
    let previewUrlToUse = signedUrl

    if (['pdf'].includes(extension)) {
      // PDF文件直接预览
      previewUrlToUse = signedUrl
    } else if (
      ['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx'].includes(extension)
    ) {
      // Office文档使用Microsoft Office在线预览
      previewUrlToUse = `https://view.officeapps.live.com/op/embed.aspx?src=${encodeURIComponent(
        signedUrl
      )}`
    } else if (
      ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'].includes(extension)
    ) {
      // 图片直接预览
      previewUrlToUse = signedUrl
    } else if (['txt', 'md', 'json', 'xml', 'csv', 'log'].includes(extension)) {
      // 文本文件尝试获取内容
      try {
        const textResponse = await fetch(signedUrl)
        if (textResponse.ok) {
          const textContent = await textResponse.text()
          // 对于文本文件，我们可以在这里处理内容
          console.log('Text content loaded:', textContent)
        }
      } catch (e) {
        console.warn('Failed to load text content:', e)
      }
      previewUrlToUse = signedUrl
    } else {
      // 其他文件类型使用Google文档查看器作为备选
      previewUrlToUse = `https://docs.google.com/gview?url=${encodeURIComponent(
        signedUrl
      )}&embedded=true`
    }

    previewUrl.value = previewUrlToUse
  } catch (error) {
    console.error('Preview file error:', error)
    ElMessage.error('文件预览失败')
    previewDialogVisible.value = false
  }
}

const downloadFile = url => {
  if (!url) return
  window.open(url, '_blank')
}

// Lifecycle
onMounted(() => {
  loadDashboardData()
})
</script>

<style scoped>
.dashboard-overview {
  padding: 20px;
  background-color: #f8f9fa;
  min-height: 100vh;
  overflow-y: auto; /* 允许垂直滚动 */
  height: 100vh; /* 设置固定高度 */
  box-sizing: border-box; /* 包含padding在内 */
}

.section {
  margin-bottom: 40px;
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.section:last-child {
  margin-bottom: 80px; /* 最后一个section增加更多底部间距 */
}

.section-header {
  margin-bottom: 24px;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 16px;
}

.section-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.section-header p {
  color: #909399;
  margin: 0;
  font-size: 14px;
}

/* Tasks Grid */
.tasks-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.task-card {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.task-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  border-color: #409eff;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.task-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0;
  flex: 1;
  margin-right: 12px;
}

.task-group {
  color: #909399;
  font-size: 14px;
  margin-bottom: 16px;
}

.task-progress {
  margin-bottom: 16px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
  color: #606266;
}

.task-meetings {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #909399;
  font-size: 14px;
}

/* Goals Grid */
.goals-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.goal-card {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.goal-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: #409eff;
}

.goal-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.goal-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin: 0;
  flex: 1;
  margin-right: 12px;
}

.goal-project,
.goal-week,
.goal-due {
  color: #909399;
  font-size: 13px;
  margin-bottom: 4px;
}

/* Meetings Grid */
.meetings-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.meeting-card {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.meeting-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: #409eff;
}

.meeting-card.upcoming {
  border-left: 4px solid #e6a23c;
}

.meeting-card.completed {
  border-left: 4px solid #67c23a;
}

.meeting-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.meeting-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin: 0;
  flex: 1;
  margin-right: 12px;
}

.meeting-task,
.meeting-week,
.meeting-time {
  color: #909399;
  font-size: 13px;
  margin-bottom: 4px;
}

.meeting-document {
  margin-top: 8px;
}

/* Tabs */
.goals-tabs,
.meetings-tabs {
  margin-top: 16px;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 40px;
  color: #909399;
  grid-column: 1 / -1;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

/* Dialog Styles */
.goal-details,
.meeting-details,
.task-details {
  padding: 16px 0;
}

.details-grid,
.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.detail-item,
.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-label,
.info-label {
  font-size: 12px;
  color: #909399;
  font-weight: 500;
}

.detail-value,
.info-value {
  font-size: 14px;
  color: #303133;
}

.goal-content {
  border-top: 1px solid #e4e7ed;
  padding-top: 16px;
}

.goal-content h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #303133;
}

.goal-content p {
  margin: 0;
  color: #606266;
  line-height: 1.5;
}

.goal-description {
  margin-top: 0; /* Remove default margin-top */
  margin-bottom: 16px; /* Add margin-bottom for spacing */
  color: #606266;
  text-align: left; /* Ensure text is left-aligned */
  white-space: pre-wrap; /* Preserve line breaks and spaces */
  word-break: break-word; /* Break long words */
}

.task-content {
  border-top: 1px solid #e4e7ed;
  padding-top: 16px;
}

.task-content h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #303133;
}

.task-content p {
  margin: 0;
  color: #606266;
  line-height: 1.5;
}

.task-description {
  margin-top: 0; /* Remove default margin-top */
  margin-bottom: 16px; /* Add margin-bottom for spacing */
  color: #606266;
  text-align: left; /* Ensure text is left-aligned */
  white-space: pre-wrap; /* Preserve line breaks and spaces */
  word-break: break-word; /* Break long words */
}

.document-section {
  border-top: 1px solid #e4e7ed;
  padding-top: 16px;
}

.document-section h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #303133;
}

.document-preview {
  background: #f8f9fa;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 16px;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.file-name {
  font-size: 14px;
  color: #303133;
}

.file-actions {
  display: flex;
  gap: 8px;
}

.file-preview-container {
  width: 100%;
  height: 600px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  overflow: hidden;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
}

.loading-state .is-loading {
  font-size: 48px;
  margin-bottom: 16px;
}

.preview-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
}

.preview-error .el-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

/* Responsive Design */
@media (max-width: 768px) {
  .dashboard-overview {
    padding: 16px;
    height: 100vh;
  }

  .section {
    padding: 16px;
    margin-bottom: 24px;
  }

  .section:last-child {
    margin-bottom: 60px;
  }

  .tasks-grid {
    grid-template-columns: 1fr;
  }

  .goals-grid,
  .meetings-grid {
    grid-template-columns: 1fr;
  }

  .details-grid,
  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
