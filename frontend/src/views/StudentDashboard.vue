<template>
  <div class="student-dashboard">
    <el-container>
      <!-- Sidebar -->
      <el-aside width="220px" class="sidebar">
        <div class="sidebar-header">My Groups</div>
        <el-menu
          :default-openeds="openedGroups"
          class="project-menu"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409eff"
          :unique-opened="false"
          @select="handleMenuSelect"
        >
          <!-- Dashboard Overview Menu Item -->
          <el-menu-item
            index="dashboard-overview"
            @click="showDashboardOverview"
          >
            <el-icon><Menu /></el-icon>
            <span>Student Dashboard</span>
          </el-menu-item>

          <!-- Existing Groups Menu -->
          <el-sub-menu
            v-for="group in studentGroups"
            :key="`group-${group.id}`"
            :index="`group-${group.id}`"
            @click="loadGroupTasks(group.id)"
          >
            <template #title>
              <span>{{ group.name || `Group ${group.id}` }}</span>
            </template>
            <el-menu-item
              v-for="task in groupTasks[group.id] || []"
              :key="`task-${task.id}`"
              :index="`task-${task.id}`"
              @click="selectTask(task)"
            >
              {{ task.title || task.name || `Task ${task.id}` }}
            </el-menu-item>
            <el-menu-item
              v-if="!groupTasks[group.id] || groupTasks[group.id].length === 0"
              :index="`no-tasks-${group.id}`"
              disabled
            >
              No tasks available
            </el-menu-item>
          </el-sub-menu>
          <div v-if="studentGroups.length === 0" class="no-groups-message">
            No groups joined yet
          </div>
        </el-menu>
      </el-aside>

      <el-container class="content-area">
        <!-- Header -->
        <el-header class="main-header">
          <div class="header-content">
            <!-- Top Components - 与搜索框同一行 -->
            <div
              v-if="selectedTask && !showingDashboard"
              class="header-components"
            >
              <!-- Task Description -->
              <div class="header-component">
                <div
                  class="header-component-title"
                  @click="openTaskDescriptionModal"
                >
                  <span>Task Description</span>
                  <el-icon class="action-icon">
                    <View />
                  </el-icon>
                </div>
              </div>

              <!-- Group Assignment -->
              <div class="header-component">
                <div
                  class="header-component-title"
                  @click="openAssignmentModal"
                >
                  <span>Group Member Assignment</span>
                  <el-icon class="action-icon">
                    <Edit />
                  </el-icon>
                </div>
              </div>

              <!-- Task Preview -->
              <div class="header-component">
                <div
                  class="header-component-title"
                  @click="openTaskPreviewModal"
                >
                  <span>Task Preview</span>
                  <el-icon class="action-icon">
                    <Document />
                  </el-icon>
                </div>
              </div>
            </div>

            <div class="header-right">
              <el-input
                v-model="search"
                placeholder="Search"
                prefix-icon="el-icon-search"
                class="search-input"
                clearable
              />
              <el-dropdown @command="handleUserCommand">
                <span class="user-dropdown">
                  <el-avatar :size="32">
                    {{ studentInfo.name.charAt(0) }}
                  </el-avatar>
                  <span class="username">{{ studentInfo.name }}</span>
                  <el-icon><ArrowDown /></el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="profile"
                      >Profile</el-dropdown-item
                    >
                    <el-dropdown-item command="settings"
                      >Settings</el-dropdown-item
                    >
                    <el-dropdown-item divided command="logout"
                      >Logout</el-dropdown-item
                    >
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </el-header>

        <!-- Main Content -->
        <el-main class="main-content">
          <div v-if="showingDashboard">
            <StudentDashboardOverview :studentGroups="studentGroups" />
          </div>
          <div v-else class="content-row">
            <!-- Existing AI Chat Section -->
            <div class="chat-section">
              <div class="chat-header">
                <h3>
                  {{ chatTitle.split(' - ').slice(0, -1).join(' - ') }}
                  <el-tag
                    v-if="chatTitle.includes(' - IN_PROGRESS')"
                    type="success"
                    size="small"
                  >
                    IN_PROGRESS
                  </el-tag>
                  <el-tag
                    v-else-if="chatTitle.includes(' - INITIALIZING')"
                    type="warning"
                    size="small"
                  >
                    INITIALIZING
                  </el-tag>
                </h3>
              </div>
              <div class="chat-messages" ref="chatMessagesRef">
                <div
                  v-for="(msg, idx) in chatMessages"
                  :key="idx"
                  class="message-wrapper"
                >
                  <div
                    :class="[
                      'message',
                      msg.sender === 'user' ? 'user-message' : 'ai-message',
                      msg.isTyping ? 'typing-message' : ''
                    ]"
                  >
                    <div class="message-sender">
                      {{
                        msg.senderName ||
                        (msg.sender === 'user' ? studentInfo.name : 'AI')
                      }}
                    </div>
                    <div
                      class="message-content"
                      :class="{ 'typing-content': msg.isTyping }"
                    >
                      <div v-if="msg.isTyping" class="typing-indicator">
                        <span class="typing-dot"></span>
                        <span class="typing-dot"></span>
                        <span class="typing-dot"></span>
                        <span class="typing-text">{{ msg.content }}</span>
                      </div>
                      <div v-else v-html="renderMarkdown(msg.content)"></div>
                    </div>
                    <div class="message-time">
                      {{ formatTime(msg.timestamp) }}
                    </div>
                  </div>
                </div>
                <div v-if="chatMessages.length === 0" class="empty-chat">
                  Select a task to start chatting with AI
                </div>
              </div>
              <div class="chat-input">
                <el-input
                  v-model="chatInput"
                  placeholder="Type a message..."
                  :disabled="!selectedTask"
                  @keyup.enter="sendMessage"
                >
                  <template #append>
                    <el-button @click="sendMessage" :disabled="!selectedTask"
                      >Send</el-button
                    >
                  </template>
                </el-input>
              </div>
            </div>

            <!-- Right Panel -->
            <div class="right-panel">
              <!-- Meeting Management -->
              <div class="meeting-section">
                <div
                  class="meeting-header"
                  @click="openMeetingManagementDialog"
                >
                  <h3>Meeting Management</h3>
                  <div class="header-actions">
                    <el-button
                      v-if="selectedTask"
                      type="primary"
                      size="small"
                      @click.stop="openNewMeetingDialog"
                      circle
                    >
                      <el-icon><Plus /></el-icon>
                    </el-button>
                    <el-icon class="toggle-icon">
                      <component
                        :is="isMeetingExpanded ? 'ArrowUp' : 'ArrowDown'"
                      />
                    </el-icon>
                  </div>
                </div>
                <div class="meeting-content">
                  <div class="meeting-items">
                    <div
                      v-for="(meeting, idx) in meetingNotes"
                      :key="idx"
                      class="meeting-item"
                      @click="openMeetingDialog(meeting)"
                    >
                      <div class="meeting-item-header">
                        <h4>{{ meeting.title }}</h4>
                        <el-tag
                          :type="
                            meeting.status === 'COMPLETED'
                              ? 'success'
                              : 'warning'
                          "
                          size="small"
                        >
                          {{ meeting.status }}
                        </el-tag>
                      </div>
                      <p class="meeting-content-text">
                        {{
                          meeting.documentUrl
                            ? 'Document uploaded'
                            : 'No document uploaded'
                        }}
                      </p>
                    </div>
                    <div
                      v-if="meetingNotes.length === 0"
                      class="empty-meetings"
                    >
                      <p>
                        No meetings available. Complete task assignment
                        confirmation to generate meeting schedule.
                      </p>
                    </div>
                  </div>
                </div>
              </div>

              <!-- To-Do List -->
              <div class="todo-section-right">
                <div class="todo-header" @click="openWeeklyGoalDialog">
                  <h3>Weekly Goals</h3>
                  <el-icon class="toggle-icon">
                    <component :is="isTodoExpanded ? 'ArrowUp' : 'ArrowDown'" />
                  </el-icon>
                </div>
                <div class="todo-content">
                  <div class="todo-items">
                    <div
                      v-for="(todo, idx) in todos"
                      :key="idx"
                      class="todo-item"
                      @click="editWeeklyGoal(todo)"
                    >
                      <div class="todo-content-inner">
                        <h4>{{ todo.title }}</h4>
                        <el-tag
                          :type="getStatusType(todo.status)"
                          size="small"
                          class="status-tag"
                        >
                          {{ getStatusText(todo.status) }}
                        </el-tag>
                      </div>
                    </div>
                    <div v-if="todos.length === 0" class="empty-todos">
                      <p>
                        No weekly goals available. Complete task assignment
                        confirmation to generate goals.
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-main>
      </el-container>
    </el-container>

    <!-- Task Description Modal -->
    <el-dialog
      v-model="taskDescriptionModalVisible"
      title="Task Description"
      width="60%"
      class="task-modal"
    >
      <div class="modal-content">
        <div class="task-description-content">
          <pre class="task-description-text">{{
            selectedTask?.description || 'No description available'
          }}</pre>
          <div class="task-meta">
            <el-tag v-if="selectedTask?.status" type="info">{{
              selectedTask.status
            }}</el-tag>
            <span v-if="selectedTask?.dueDate" class="due-date">
              Due: {{ formatDate(selectedTask.dueDate) }}
            </span>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- Group Assignment Modal -->
    <el-dialog
      v-model="assignmentModalVisible"
      title="Group Member Assignment"
      width="70%"
      class="assignment-modal"
    >
      <div class="modal-content assignment-modal-content">
        <div v-if="currentGroupMembers.length === 0" class="empty-state">
          No group members loaded. Please select a task first.
        </div>
        <div v-else class="assignment-form">
          <div
            v-for="member in currentGroupMembers"
            :key="member.id"
            class="member-assignment-row"
          >
            <div class="member-info">
              <el-avatar :size="32">{{ member.name.charAt(0) }}</el-avatar>
              <span class="member-name">{{ member.name }}</span>
            </div>
            <div class="member-task">
              <el-input
                v-model="assignmentForm.assignments[member.id].description"
                placeholder="Enter task description for this member"
                type="textarea"
                :autosize="{ minRows: 5, maxRows: 20 }"
                resize="none"
                class="assignment-textarea"
              />
            </div>
          </div>

          <!-- 周期数输入 -->
          <div v-if="showCycleInput" class="cycle-input-section">
            <el-divider content-position="left">Task Cycle Settings</el-divider>
            <div class="cycle-input-row">
              <label class="cycle-label">Task Cycle (Weeks):</label>
              <el-input-number
                v-model="taskCycle"
                :min="1"
                :max="20"
                :step="1"
                size="default"
                style="width: 120px"
              />
              <span class="cycle-hint"
                >(Set the task execution cycle in weeks. The system will
                automatically generate corresponding weekly goals for each
                member)</span
              >
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="modal-footer">
          <el-button @click="assignmentModalVisible = false">Cancel</el-button>
          <el-button
            v-if="!isAssignmentSubmitted"
            type="primary"
            @click="submitTaskAssignment"
          >
            Submit Assignment
          </el-button>
          <el-button
            v-if="isAssignmentSubmitted && !isAssignmentFinalized"
            type="warning"
            @click="updateAssignment"
          >
            Update Assignment
          </el-button>
          <el-button
            v-if="isAssignmentSubmitted && !isAssignmentFinalized"
            type="success"
            @click="getAgentAdvice"
          >
            Get Agent Advice
          </el-button>
          <el-button
            v-if="
              isAssignmentSubmitted && !isAssignmentFinalized && !showCycleInput
            "
            type="danger"
            @click="showCycleInput = true"
          >
            Confirm Assignment
          </el-button>
          <el-button
            v-if="
              isAssignmentSubmitted && !isAssignmentFinalized && showCycleInput
            "
            type="primary"
            @click="confirmTaskAssignment"
            :loading="confirmLoading"
          >
            Confirm with {{ taskCycle }} Weeks
          </el-button>
          <el-button v-if="showCycleInput" @click="showCycleInput = false">
            Cancel Confirm
          </el-button>
          <el-button @click="resetAssignment">Reset</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Task Preview Modal -->
    <el-dialog
      v-model="taskPreviewModalVisible"
      title="Task Preview"
      width="60%"
      class="preview-modal"
    >
      <div class="modal-content">
        <div v-if="selectedTask?.fileUrl" class="file-preview-section">
          <div class="file-info">
            <el-icon class="file-icon"><Document /></el-icon>
            <div class="file-details">
              <span class="file-name">{{
                getFileName(selectedTask.fileUrl)
              }}</span>
              <span class="file-size">{{
                formatFileSize(previewFileSize)
              }}</span>
            </div>
          </div>
          <div class="file-actions">
            <el-button
              type="primary"
              @click="previewFile(selectedTask.fileUrl)"
            >
              <el-icon><View /></el-icon>
              Preview
            </el-button>
            <el-button @click="downloadFile(selectedTask.fileUrl)">
              <el-icon><Download /></el-icon>
              Download
            </el-button>
          </div>
        </div>
        <div v-else class="empty-state">
          <el-icon class="empty-icon"><Document /></el-icon>
          <p>No file attached to this task</p>
        </div>
      </div>
    </el-dialog>

    <!-- File Preview Modal -->
    <el-dialog
      v-model="filePreviewVisible"
      :title="previewFileName"
      width="80%"
      class="file-preview-modal"
    >
      <div class="file-preview-content">
        <div v-if="previewLoading" class="loading-state">
          <el-icon class="is-loading"><Loading /></el-icon>
          <p>Loading preview...</p>
        </div>
        <div v-else-if="previewError" class="error-state">
          <p>{{ previewError }}</p>
        </div>
        <div v-else-if="previewType === 'pdf'" class="pdf-preview">
          <iframe :src="previewUrl" width="100%" height="600px"></iframe>
        </div>
        <div v-else-if="previewType === 'image'" class="image-preview">
          <img :src="previewUrl" alt="Preview" />
        </div>
        <div v-else-if="previewType === 'office'" class="office-preview">
          <iframe :src="previewUrl" width="100%" height="600px"></iframe>
        </div>
        <div v-else-if="previewType === 'text'" class="text-preview">
          <pre>{{ previewContent }}</pre>
        </div>
        <div v-else class="unsupported-preview">
          <p>Preview not supported for this file type</p>
          <el-button @click="downloadFile(previewFileUrl)"
            >Download File</el-button
          >
        </div>
      </div>
    </el-dialog>

    <!-- Weekly Goal Edit Dialog -->
    <WeeklyGoalEditDialog
      v-model="showWeeklyGoalDialog"
      :goal-data="selectedGoal"
      :task-id="selectedTask?.id"
      @goal-updated="handleGoalUpdated"
    />

    <!-- Meeting Management Dialog -->
    <MeetingManagementDialog
      v-model="showMeetingManagementDialog"
      :meeting-data="currentMeetingData"
      :task-id="selectedTask?.id"
      :add-message-to-chat="addMessageToChat"
      @meeting-updated="handleMeetingUpdated"
      @conversation-updated="handleConversationUpdated"
    />
  </div>
</template>

<script>
import { ref, nextTick, onMounted } from 'vue'
import {
  ArrowDown,
  ArrowUp,
  Close,
  Document,
  Loading,
  View,
  Edit,
  Download,
  Plus,
  Menu
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import studentAPI from '@/api/student'
import { useAuthStore } from '@/store/auth'
import { useRouter } from 'vue-router'
import MarkdownIt from 'markdown-it'
import WeeklyGoalEditDialog from '@/components/WeeklyGoalEditDialog.vue'
import MeetingManagementDialog from '@/components/MeetingManagementDialog.vue'
import StudentDashboardOverview from '@/components/StudentDashboardOverview.vue'

export default {
  name: 'StudentDashboard',
  components: {
    ArrowDown,
    ArrowUp,
    Close,
    Document,
    Loading,
    View,
    Edit,
    Download,
    WeeklyGoalEditDialog,
    MeetingManagementDialog,
    StudentDashboardOverview,
    Plus,
    Menu
  },
  setup() {
    const router = useRouter()
    const authStore = useAuthStore()

    const search = ref('')
    const studentInfo = ref({
      name: authStore.displayName || authStore.user?.name || 'Student'
    })
    const chatInput = ref('')
    const chatMessages = ref([])
    const chatTitle = ref('AI-Chat - Please select a task')
    const chatMessagesRef = ref(null)
    const meetingNotes = ref([])
    const todos = ref([])

    // UI state
    const isTaskDescriptionExpanded = ref(false) // 默认收起
    const isTodoExpanded = ref(true)
    const isTopPanelExpanded = ref(true) // 新增：控制顶部面板的展开/收起
    const isAssignmentExpanded = ref(false) // 默认收起
    const isAssignmentSubmitted = ref(false)
    const isAssignmentFinalized = ref(false)
    const isTaskPreviewExpanded = ref(false) // 新增：控制任务预览的展开/收起

    // 新增的响应式数据
    const studentGroups = ref([])
    const groupTasks = ref({})
    const openedGroups = ref([])
    const selectedTask = ref(null)
    const loading = ref(false)
    const lastApiResponse = ref(null) // 添加调试用的API响应追踪

    // 任务分工相关数据
    const currentGroupMembers = ref([])
    const assignmentForm = ref({
      assignments: {}
    })
    const assignmentLoading = ref(false)
    const agentAdviceLoading = ref(false)
    const updateLoading = ref(false)
    const finalizeLoading = ref(false)
    const confirmLoading = ref(false)
    const taskCycle = ref(5) // 默认5周
    const showCycleInput = ref(false)

    // 已完成分工的任务列表
    const finalizedTasks = ref([])
    const selectedFinalizedTask = ref(null)

    // 文件预览相关
    const filePreviewVisible = ref(false)
    const previewFileUrl = ref('')
    const previewFileName = ref('')
    const previewType = ref('')
    const previewUrl = ref('')
    const previewContent = ref('')
    const previewLoading = ref(false)
    const previewError = ref('')
    const previewFileSize = ref(0) // 新增：文件大小

    // 弹窗状态
    const taskDescriptionModalVisible = ref(false)
    const assignmentModalVisible = ref(false)
    const taskPreviewModalVisible = ref(false)
    const showWeeklyGoalDialog = ref(false)
    const selectedGoal = ref(null)

    // Meeting Management
    const showMeetingManagementDialog = ref(false)
    const currentMeetingData = ref({})
    const isMeetingExpanded = ref(true)

    // 右侧面板宽度计算
    // 移除mainContentWrapper引用，因为不再需要动态调整

    // Initialize markdown parser
    const md = new MarkdownIt({
      html: true,
      linkify: true,
      typographer: true
    })

    // Markdown rendering
    const renderMarkdown = content => {
      return md.render(content || '')
    }

    // Toggle functions
    const toggleTaskDescription = () => {
      isTaskDescriptionExpanded.value = !isTaskDescriptionExpanded.value
      updateTopPanelState()
    }

    const toggleTodo = () => {
      isTodoExpanded.value = !isTodoExpanded.value
    }

    const toggleAssignmentForm = () => {
      isAssignmentExpanded.value = !isAssignmentExpanded.value
      updateTopPanelState()
    }

    const toggleTaskPreview = () => {
      isTaskPreviewExpanded.value = !isTaskPreviewExpanded.value
      updateTopPanelState()
    }

    // 更新顶部面板状态
    const updateTopPanelState = () => {
      isTopPanelExpanded.value =
        isTaskDescriptionExpanded.value ||
        isAssignmentExpanded.value ||
        isTaskPreviewExpanded.value
    }

    // Chat scroll to bottom
    const scrollToBottom = () => {
      nextTick(() => {
        if (chatMessagesRef.value) {
          chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
        }
      })
    }

    // 文件预览相关函数
    const previewFile = async url => {
      if (!url) return

      previewFileUrl.value = url
      previewFileName.value = getFileName(url)
      filePreviewVisible.value = true
      previewLoading.value = true
      previewError.value = ''
      previewContent.value = ''
      previewUrl.value = ''
      previewFileSize.value = 0 // 重置文件大小

      try {
        const fileType = getFileType(url)
        previewType.value = fileType

        switch (fileType) {
          case 'pdf':
            previewUrl.value = url
            previewLoading.value = false
            break
          case 'image':
            previewUrl.value = url
            previewLoading.value = false
            break
          case 'office':
            // 使用 Microsoft Office Online 预览
            previewUrl.value = `https://view.officeapps.live.com/op/embed.aspx?src=${encodeURIComponent(
              url
            )}`
            previewLoading.value = false
            break
          case 'text':
            try {
              const response = await fetch(url)
              if (!response.ok) throw new Error('Failed to fetch file')
              previewContent.value = await response.text()
              previewLoading.value = false
            } catch (error) {
              previewError.value = 'Failed to load text file: ' + error.message
              previewLoading.value = false
            }
            break
          default:
            previewError.value = 'Preview not supported for this file type'
            previewLoading.value = false
        }
      } catch (error) {
        previewError.value = 'Failed to preview file: ' + error.message
        previewLoading.value = false
      }
    }

    const getFileName = url => {
      if (!url) return ''
      const parts = url.split('/')
      return parts[parts.length - 1] || 'Unknown file'
    }

    const getFileType = url => {
      if (!url) return 'unknown'
      const extension = url.split('.').pop()?.toLowerCase()

      if (['pdf'].includes(extension)) return 'pdf'
      if (['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'].includes(extension))
        return 'image'
      if (['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx'].includes(extension))
        return 'office'
      if (['txt', 'md', 'json', 'xml', 'csv', 'log'].includes(extension))
        return 'text'

      return 'unknown'
    }

    const openInNewTab = url => {
      if (url) {
        window.open(url, '_blank')
      }
    }

    const downloadFile = url => {
      if (!url) return

      const a = document.createElement('a')
      a.href = url
      a.download = getFileName(url)
      a.target = '_blank'
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
    }

    // Time formatting
    const formatTime = timestamp => {
      if (!timestamp) return ''
      const date = new Date(timestamp)
      return date.toLocaleTimeString('en-US', {
        hour: '2-digit',
        minute: '2-digit'
      })
    }

    // Format date
    const formatDate = dateString => {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
      })
    }

    // 格式化文件大小
    const formatFileSize = bytes => {
      if (bytes === 0) return '0 Bytes'
      const k = 1024
      const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    }

    // 处理预览错误
    const handlePreviewError = () => {
      previewError.value = 'Failed to load preview.'
      previewLoading.value = false
    }

    // 获取 assignment state for current task
    const getAssignmentState = async () => {
      if (!selectedTask.value) return

      try {
        // Load assignment state from backend
        // This will check if assignment exists and its status
        const taskId = selectedTask.value.id
        console.log(`Getting assignment state for task ${taskId}`)

        const response = await studentAPI.getTaskAssignmentStatus(taskId)
        console.log('Assignment state response:', response)

        if (response.data && response.data.code === 1) {
          const statusData = response.data.data
          console.log('Status data:', statusData)

          // 如果存在分工记录，就认为已提交（不管状态是否为DRAFT）
          const hasAssignments =
            statusData.assignments && statusData.assignments.length > 0
          const currentStatus = statusData.status

          console.log(
            `Has assignments: ${hasAssignments}, Status: ${currentStatus}`
          )

          // 强制更新状态
          isAssignmentSubmitted.value = hasAssignments
          isAssignmentFinalized.value = currentStatus === 'FINALIZED'

          console.log(
            `Updated state - Submitted: ${isAssignmentSubmitted.value}, Finalized: ${isAssignmentFinalized.value}`
          )

          // Load existing assignments
          if (hasAssignments) {
            Object.keys(assignmentForm.value.assignments).forEach(memberId => {
              const existingAssignment = statusData.assignments.find(
                a => a.userId == memberId
              )
              if (existingAssignment) {
                assignmentForm.value.assignments[memberId].description =
                  existingAssignment.description
              }
            })
          }

          // 强制触发UI重新渲染
          nextTick(() => {
            console.log(
              'UI state after nextTick - Submitted:',
              isAssignmentSubmitted.value,
              'Finalized:',
              isAssignmentFinalized.value
            )
          })
        } else {
          console.log('API returned error or no data')
          isAssignmentSubmitted.value = false
          isAssignmentFinalized.value = false
        }
      } catch (error) {
        console.error('Error loading assignment state:', error)
        isAssignmentSubmitted.value = false
        isAssignmentFinalized.value = false
      }
    }

    // 获取学生的所有小组
    const loadStudentGroups = async () => {
      try {
        loading.value = true
        const response = await studentAPI.getStudentGroups()
        if (response.data.code === 1) {
          studentGroups.value = response.data.data || []
          // 自动展开第一个小组
          if (studentGroups.value.length > 0) {
            openedGroups.value = [`group-${studentGroups.value[0].id}`]
            loadGroupTasks(studentGroups.value[0].id)
          }
        } else {
          ElMessage.error('获取小组信息失败')
        }
      } catch (error) {
        console.error('Load student groups error:', error)
        ElMessage.error('加载小组信息失败')
      } finally {
        loading.value = false
      }
    }

    // 加载指定小组的任务
    const loadGroupTasks = async groupId => {
      selectedGroupId.value = groupId
      try {
        const response = await studentAPI.getGroupTasks(groupId)
        if (response.data.code === 1) {
          groupTasks.value[groupId] = response.data.data || []
        } else {
          ElMessage.error('获取任务失败')
        }
      } catch (error) {
        console.error('Load group tasks error:', error)
        ElMessage.error('加载任务失败')
        groupTasks.value[groupId] = []
      }
    }

    // 加载小组成员
    const loadGroupMembers = async groupId => {
      try {
        const response = await studentAPI.getGroupMembers(groupId)

        if (response.data && response.data.code === 1) {
          currentGroupMembers.value = response.data.data

          // 初始化分工表单
          const assignments = {}
          currentGroupMembers.value.forEach(member => {
            assignments[member.id] = {
              userId: member.id,
              userName: member.name,
              description: ''
            }
          })
          assignmentForm.value.assignments = assignments
        } else {
          console.error('API returned error:', response.data)
          ElMessage.error(
            'Failed to load group members: ' +
              (response.data?.msg || 'Unknown error')
          )
          currentGroupMembers.value = []
        }
      } catch (error) {
        console.error('Error loading group members:', error)
        ElMessage.error('Failed to load group members: ' + error.message)
        currentGroupMembers.value = []
      }
    }

    // 选择任务时加载周目标
    const selectTask = async task => {
      showingDashboard.value = false
      selectedTask.value = task

      // 先设置一个基本的标题
      chatTitle.value = `AI-Chat - ${getSelectedGroup()?.name || 'Group'} - ${
        task.title || task.name || `Task ${task.id}`
      }`

      // 加载小组成员
      await loadGroupMembers(task.groupId || task.group_id)

      // 重置分工表单
      resetAssignment()

      // 获取任务状态
      await getAssignmentState()

      // 更新标题以包含任务状态
      const taskStatus = isAssignmentFinalized.value
        ? 'IN_PROGRESS'
        : 'INITIALIZING'
      chatTitle.value = `AI-Chat - ${getSelectedGroup()?.name || 'Group'} - ${
        task.title || task.name || `Task ${task.id}`
      } - ${taskStatus}`

      // 加载会议记录和周期目标
      await loadTaskMeetings()
      await loadStudentWeeklyGoals()

      // 加载对话历史
      await loadConversationHistory()
    }

    // 加载任务的会议记录
    const loadTaskMeetings = async () => {
      if (!selectedTask.value) return

      try {
        const response = await studentAPI.getTaskMeetings(selectedTask.value.id)
        if (response.data && response.data.code === 1) {
          const meetings = response.data.data || []
          meetingNotes.value = meetings.map(meeting => ({
            id: meeting.id,
            meetingNo: meeting.meetingNo,
            title: `Meeting ${meeting.meetingNo} - Week ${meeting.meetingNo}`,
            status: meeting.status,
            meetingDate: meeting.meetingDate,
            documentUrl: meeting.documentUrl
          }))
        } else {
          meetingNotes.value = []
        }
      } catch (error) {
        console.error('Error loading task meetings:', error)
        meetingNotes.value = []
      }
    }

    // 加载学生的周期目标
    const loadStudentWeeklyGoals = async () => {
      if (!selectedTask.value) return

      try {
        const response = await studentAPI.getStudentWeeklyGoals(
          selectedTask.value.id
        )
        if (response.data && response.data.code === 1) {
          const goals = response.data.data || []
          todos.value = goals.map(goal => ({
            id: goal.id,
            title: `Week ${goal.weekNo} Goal`,
            desc: goal.goal,
            status: goal.status,
            weekNo: goal.weekNo
          }))
        } else {
          todos.value = []
        }
      } catch (error) {
        console.error('Error loading student weekly goals:', error)
        todos.value = []
      }
    }

    // 处理菜单选择
    const handleMenuSelect = index => {
      console.log('Menu selected:', index)
    }

    // 关闭任务分工界面
    const closeTaskAssignment = () => {
      selectedTask.value = null
      currentGroupMembers.value = []
      assignmentForm.value = {
        assignments: {}
      }
      isAssignmentExpanded.value = false
      isAssignmentSubmitted.value = false
    }

    // 重置分工
    const resetAssignment = () => {
      const assignments = {}
      currentGroupMembers.value.forEach(member => {
        assignments[member.id] = {
          userId: member.id,
          userName: member.name,
          description: ''
        }
      })
      assignmentForm.value.assignments = assignments
      isAssignmentSubmitted.value = false
      isAssignmentFinalized.value = false
      ElMessage.info('Assignment content reset')
    }

    // 提交任务分工
    const submitTaskAssignment = async () => {
      try {
        assignmentLoading.value = true

        // 验证表单
        const hasValidAssignments = Object.values(
          assignmentForm.value.assignments
        ).some(
          assignment => assignment.description && assignment.description.trim()
        )

        if (!hasValidAssignments) {
          ElMessage.warning(
            'Please assign task description for at least one member'
          )
          return
        }

        // 准备提交数据
        const submitData = {
          taskId: selectedTask.value.id,
          assignments: []
        }

        Object.keys(assignmentForm.value.assignments).forEach(memberId => {
          const assignment = assignmentForm.value.assignments[memberId]
          if (assignment.description && assignment.description.trim()) {
            submitData.assignments.push({
              userId: parseInt(memberId),
              role: assignment.userName,
              description: assignment.description
            })
          }
        })

        console.log('Submitting assignment:', submitData)
        const response = await studentAPI.submitTaskAssignment(submitData)
        console.log('Submit response:', response)

        if (response.data && response.data.code === 1) {
          ElMessage.success('Task assignment submitted successfully')
          isAssignmentSubmitted.value = true
          isAssignmentExpanded.value = false

          // 等待一小段时间让后端处理完成
          await new Promise(resolve => setTimeout(resolve, 500))

          // 重新获取任务状态来确保前端状态同步
          console.log('Refreshing assignment state after submit...')
          await getAssignmentState()
          console.log('Assignment state refreshed')
        } else {
          console.log('Submit failed. Response structure:', response)
          ElMessage.error(response.data?.msg || 'Submission failed')
        }
      } catch (error) {
        console.error('Submit task assignment error:', error)
        ElMessage.error('Failed to submit task assignment')
      } finally {
        assignmentLoading.value = false
      }
    }

    // 更新任务分工
    const updateAssignment = async () => {
      try {
        updateLoading.value = true

        // 准备更新数据
        const updateData = {
          taskId: selectedTask.value.id,
          assignments: []
        }

        Object.keys(assignmentForm.value.assignments).forEach(memberId => {
          const assignment = assignmentForm.value.assignments[memberId]
          if (assignment.description && assignment.description.trim()) {
            updateData.assignments.push({
              userId: parseInt(memberId),
              role: assignment.userName,
              description: assignment.description
            })
          }
        })

        console.log('Updating assignment:', updateData)
        const response = await studentAPI.updateTaskAssignment(updateData)
        console.log('Update response:', response)

        if (response.data && response.data.code === 1) {
          ElMessage.success('Task assignment updated successfully')

          // 等待一小段时间让后端处理完成
          await new Promise(resolve => setTimeout(resolve, 500))

          // 重新获取任务状态来确保前端状态同步
          console.log('Refreshing assignment state after update...')
          await getAssignmentState()
          console.log('Assignment state refreshed')
        } else {
          ElMessage.error(response.data?.msg || 'Update failed')
        }
      } catch (error) {
        console.error('Update task assignment error:', error)
        ElMessage.error('Failed to update task assignment')
      } finally {
        updateLoading.value = false
      }
    }

    // 确定任务分工
    const finalizeAssignment = async () => {
      try {
        finalizeLoading.value = true

        console.log('Finalizing assignment for task:', selectedTask.value.id)
        const response = await studentAPI.finalizeTaskAssignment(
          selectedTask.value.id
        )
        console.log('Finalize response:', response)

        if (response.data && response.data.code === 1) {
          ElMessage.success('Task assignment finalized successfully')
          isAssignmentFinalized.value = true
          // 更新已确定任务列表
          await loadFinalizedTasks()

          // 等待一小段时间让后端处理完成
          await new Promise(resolve => setTimeout(resolve, 500))

          // 重新获取任务状态来确保前端状态同步
          console.log('Refreshing assignment state after finalize...')
          await getAssignmentState()
          console.log('Assignment state refreshed')

          // 更新标题以显示新的任务状态
          chatTitle.value = `AI-Chat - ${
            getSelectedGroup()?.name || 'Group'
          } - ${
            selectedTask.value.title ||
            selectedTask.value.name ||
            `Task ${selectedTask.value.id}`
          } - IN_PROGRESS`
        } else {
          console.log('Finalize failed. Response structure:', response)
          ElMessage.error(response.data?.msg || 'Finalize failed')
        }
      } catch (error) {
        console.error('Finalize task assignment error:', error)
        ElMessage.error('Failed to finalize task assignment')
      } finally {
        finalizeLoading.value = false
      }
    }

    // 确认任务分工和周期数
    const confirmTaskAssignment = async () => {
      try {
        confirmLoading.value = true

        console.log(
          'Confirming assignment for task:',
          selectedTask.value.id,
          'with cycle:',
          taskCycle.value
        )
        const response = await studentAPI.confirmTaskAssignment(
          selectedTask.value.id,
          taskCycle.value
        )
        console.log('Confirm response:', response)

        if (response.data && response.data.code === 1) {
          ElMessage.success(
            `Task assignment confirmed successfully! Set to ${taskCycle.value} weeks cycle. The system has automatically generated corresponding weekly goals and meeting records.`
          )
          isAssignmentFinalized.value = true
          showCycleInput.value = false

          // 更新已确定任务列表
          await loadFinalizedTasks()

          // 等待一小段时间让后端处理完成
          await new Promise(resolve => setTimeout(resolve, 500))

          // 重新获取任务状态来确保前端状态同步
          console.log('Refreshing assignment state after confirm...')
          await getAssignmentState()
          console.log('Assignment state refreshed')

          // 更新标题以显示新的任务状态
          chatTitle.value = `AI-Chat - ${
            getSelectedGroup()?.name || 'Group'
          } - ${
            selectedTask.value.title ||
            selectedTask.value.name ||
            `Task ${selectedTask.value.id}`
          } - IN_PROGRESS`

          // 重新加载会议记录和周期目标
          await loadTaskMeetings()
          await loadStudentWeeklyGoals()
        } else {
          console.log('Confirm failed. Response structure:', response)
          ElMessage.error(response.data?.msg || 'Confirm failed')
        }
      } catch (error) {
        console.error('Confirm task assignment error:', error)
        ElMessage.error('Failed to confirm task assignment')
      } finally {
        confirmLoading.value = false
      }
    }

    // 加载已确定的任务列表
    const loadFinalizedTasks = async () => {
      try {
        const tasks = []
        for (const group of studentGroups.value) {
          const groupTasksList = groupTasks.value[group.id] || []
          for (const task of groupTasksList) {
            try {
              const response = await studentAPI.getFinalizedAssignments(task.id)
              if (
                response.data &&
                response.data.code === 1 &&
                response.data.data.length > 0
              ) {
                tasks.push({
                  id: task.id,
                  groupName: group.name || `Group ${group.id}`,
                  taskName: task.title || task.name || `Task ${task.id}`,
                  assignments: response.data.data
                })
              }
            } catch (error) {
              console.log(`No finalized assignments for task ${task.id}`)
            }
          }
        }
        finalizedTasks.value = tasks
      } catch (error) {
        console.error('Load finalized tasks error:', error)
      }
    }

    // 查看已确定的分工
    const viewFinalizedAssignment = taskId => {
      if (!taskId) return

      const task = finalizedTasks.value.find(t => t.id === taskId)
      if (task) {
        ElMessage.info(
          `Viewing finalized assignments for ${task.groupName} - ${task.taskName}`
        )
        // 这里可以显示详细的分工信息，比如在弹窗中
        console.log('Finalized assignments:', task.assignments)
      }
    }

    // 加载对话历史
    const loadConversationHistory = async () => {
      if (!selectedTask.value) return

      try {
        const response = await studentAPI.getConversationHistory(
          selectedTask.value.id
        )
        if (response.data && response.data.code === 1) {
          const conversations = response.data.data || []
          chatMessages.value = conversations.map(conv => ({
            content: conv.content,
            sender: conv.sender_type === 'USER' ? 'user' : 'ai',
            senderName:
              conv.sender_name ||
              (conv.sender_type === 'USER' ? 'Student' : 'AI Assistant'),
            timestamp: conv.created_at,
            isTyping: false
          }))
          // 滚动到底部
          nextTick(() => {
            scrollToBottom()
          })
        } else {
          chatMessages.value = []
        }
      } catch (error) {
        console.error('Error loading conversation history:', error)
        chatMessages.value = []
      }
    }

    // 发送消息
    const sendMessage = async () => {
      if (!chatInput.value.trim() || !selectedTask.value) return

      const messageContent = chatInput.value.trim()
      chatInput.value = ''

      // 立即添加用户消息到界面
      const userMessage = {
        sender: 'user',
        content: messageContent,
        timestamp: new Date().toISOString(),
        senderName: studentInfo.value.name,
        isTyping: false
      }
      chatMessages.value.push(userMessage)

      // 添加正在输入指示器
      const typingIndicator = {
        sender: 'ai',
        content: 'AI is typing...',
        timestamp: new Date().toISOString(),
        senderName: 'AI Assistant',
        isTyping: true
      }
      chatMessages.value.push(typingIndicator)

      scrollToBottom()

      try {
        const response = await studentAPI.sendMessage(
          selectedTask.value.id,
          messageContent
        )

        // 移除正在输入指示器
        chatMessages.value = chatMessages.value.filter(msg => !msg.isTyping)

        if (response.data && response.data.code === 1) {
          // 添加AI回复
          const aiMessage = {
            sender: 'ai',
            content: response.data.data.response,
            timestamp: new Date().toISOString(),
            senderName: 'AI Assistant',
            isTyping: false
          }
          chatMessages.value.push(aiMessage)

          // 可选：同时重新加载对话历史以确保同步
          setTimeout(() => {
            loadConversationHistory()
          }, 1000)
        } else {
          // 移除正在输入指示器并显示错误消息
          const errorMessage = {
            sender: 'ai',
            content: 'Sorry, there was an error processing your message.',
            timestamp: new Date().toISOString(),
            senderName: 'AI Assistant',
            isTyping: false
          }
          chatMessages.value.push(errorMessage)
        }
      } catch (error) {
        console.error('Error sending message:', error)
        // 移除正在输入指示器
        chatMessages.value = chatMessages.value.filter(msg => !msg.isTyping)

        const errorMessage = {
          sender: 'ai',
          content:
            'Sorry, there was an error processing your message. Please try again.',
          timestamp: new Date().toISOString(),
          senderName: 'AI Assistant',
          isTyping: false
        }
        chatMessages.value.push(errorMessage)
      }

      nextTick(() => {
        scrollToBottom()
      })
    }

    // 获取Agent建议
    const getAgentAdvice = async () => {
      if (!selectedTask.value?.id) {
        ElMessage.error('请先选择任务')
        return
      }

      agentAdviceLoading.value = true

      try {
        // 1. 关闭弹窗
        assignmentModalVisible.value = false

        // 2. 添加学生请求消息到对话框
        const userMessage = {
          sender: 'user',
          content: 'Submit initial division of labor',
          timestamp: new Date().toISOString(),
          senderName: studentInfo.value.name
        }
        chatMessages.value.push(userMessage)

        // 3. 添加Agent正在输入的提示
        const typingIndicator = {
          sender: 'ai',
          content: 'Agent is typing...',
          timestamp: new Date().toISOString(),
          senderName: 'AI Assistant',
          isTyping: true
        }
        chatMessages.value.push(typingIndicator)

        // 4. 滚动到底部
        scrollToBottom()

        console.log('Getting agent advice for task:', selectedTask.value.id)
        const response = await studentAPI.getAgentAdvice(selectedTask.value.id)
        console.log('Agent advice response:', response)

        // 5. 移除正在输入指示器
        chatMessages.value = chatMessages.value.filter(msg => !msg.isTyping)

        if (response.data && response.data.code === 1) {
          // 6. 添加Agent回复消息
          const aiMessage = {
            sender: 'ai',
            content: response.data.data.advice || response.data.data,
            timestamp: new Date().toISOString(),
            senderName: 'AI Assistant',
            isTyping: false
          }
          chatMessages.value.push(aiMessage)

          ElMessage.success('Agent建议已生成')

          // 7. 重新加载对话历史以确保数据同步
          setTimeout(async () => {
            await loadConversationHistory()
            await getAssignmentState()
          }, 1000)
        } else {
          // 添加错误消息到对话框
          const errorMessage = {
            sender: 'ai',
            content:
              'Sorry, I encountered an error while generating advice. Please try again.',
            timestamp: new Date().toISOString(),
            senderName: 'AI Assistant',
            isTyping: false
          }
          chatMessages.value.push(errorMessage)

          ElMessage.error(
            'Failed to get agent advice: ' +
              (response.data?.msg || 'Unknown error')
          )
        }
      } catch (error) {
        console.error('Error getting agent advice:', error)

        // 移除正在输入指示器
        chatMessages.value = chatMessages.value.filter(msg => !msg.isTyping)

        // 添加错误消息到对话框
        const errorMessage = {
          sender: 'ai',
          content:
            'Sorry, there was a network error. Please check your connection and try again.',
          timestamp: new Date().toISOString(),
          senderName: 'AI Assistant',
          isTyping: false
        }
        chatMessages.value.push(errorMessage)

        // 如果是超时错误，尝试重新加载对话历史
        if (
          error.code === 'ECONNABORTED' ||
          error.message.includes('timeout')
        ) {
          ElMessage.warning(
            'Request timed out, but the agent may still be processing. Checking for new messages...'
          )
          setTimeout(async () => {
            await loadConversationHistory()
            await getAssignmentState()
          }, 2000)
        } else {
          ElMessage.error('获取Agent建议失败: ' + error.message)
        }
      } finally {
        agentAdviceLoading.value = false
        // 最后滚动到底部
        scrollToBottom()
      }
    }

    // 处理用户命令
    const handleUserCommand = async command => {
      if (command === 'logout') {
        // 使用 auth store 的 logout 方法
        await authStore.logout()

        // 使用路由跳转而不是 window.location.href
        router.push('/')
      } else if (command === 'profile') {
        ElMessage.info('Profile page not implemented yet')
      } else if (command === 'settings') {
        ElMessage.info('Settings page not implemented yet')
      }
    }

    // 打开任务描述弹窗
    const openTaskDescriptionModal = () => {
      taskDescriptionModalVisible.value = true
    }

    // 打开任务分工弹窗
    const openAssignmentModal = () => {
      assignmentModalVisible.value = true
    }

    // 打开任务预览弹窗
    const openTaskPreviewModal = () => {
      taskPreviewModalVisible.value = true
    }

    // 在Weekly Goals标题点击时打开弹窗
    const openWeeklyGoalDialog = () => {
      showWeeklyGoalDialog.value = true
    }

    // 查看会议文档
    const viewMeetingDocument = async documentUrl => {
      if (!documentUrl) {
        ElMessage.warning('No document URL available for this meeting.')
        return
      }
      try {
        previewFileUrl.value = documentUrl
        previewFileName.value = getFileName(documentUrl)
        filePreviewVisible.value = true
        previewLoading.value = true
        previewError.value = ''
        previewContent.value = ''
        previewUrl.value = ''
        previewFileSize.value = 0

        const fileType = getFileType(documentUrl)
        previewType.value = fileType

        switch (fileType) {
          case 'pdf':
            previewUrl.value = documentUrl
            previewLoading.value = false
            break
          case 'image':
            previewUrl.value = documentUrl
            previewLoading.value = false
            break
          case 'office':
            previewUrl.value = `https://view.officeapps.live.com/op/embed.aspx?src=${encodeURIComponent(
              documentUrl
            )}`
            previewLoading.value = false
            break
          case 'text':
            try {
              const response = await fetch(documentUrl)
              if (!response.ok) throw new Error('Failed to fetch document')
              previewContent.value = await response.text()
              previewLoading.value = false
            } catch (error) {
              previewError.value = 'Failed to load document: ' + error.message
              previewLoading.value = false
            }
            break
          default:
            previewError.value = 'Preview not supported for this document type'
            previewLoading.value = false
        }
      } catch (error) {
        previewError.value = 'Failed to preview document: ' + error.message
        previewLoading.value = false
      }
    }

    // Helper to get the current group name for chat title
    const getSelectedGroup = () => {
      if (!selectedTask.value) return null
      return studentGroups.value.find(g => g.id === selectedTask.value.groupId)
    }

    // 处理周目标更新
    const handleGoalsUpdated = async newGoals => {
      if (newGoals) {
        todos.value = newGoals
      }
      await loadStudentWeeklyGoals()
    }

    // 处理单个目标更新
    const handleGoalUpdated = updatedGoal => {
      const index = todos.value.findIndex(todo => todo.id === updatedGoal.id)
      if (index !== -1) {
        todos.value[index] = updatedGoal
      }
    }

    // 获取状态类型
    const getStatusType = status => {
      switch (status) {
        case 'NOTUPLOADED':
          return 'info'
        case 'PROCESSING':
          return 'warning'
        case 'COMPLETED':
          return 'success'
        default:
          return 'info'
      }
    }

    // 获取状态文本
    const getStatusText = status => {
      switch (status) {
        case 'NOTUPLOADED':
        case 'NOT_STARTED':
          return 'Not Started'
        case 'PROCESSING':
        case 'IN_PROGRESS':
          return 'In Progress'
        case 'COMPLETED':
        case 'FINISHED':
          return 'Completed'
        default:
          console.log('Unknown status:', status) // 调试用
          return 'Unknown'
      }
    }

    // 编辑周目标
    const editWeeklyGoal = goal => {
      selectedGoal.value = goal
      showWeeklyGoalDialog.value = true
    }

    // 页面加载时获取数据
    onMounted(() => {
      // 从 localStorage 恢复用户信息
      const userData = localStorage.getItem('userData')
      if (userData) {
        try {
          const user = JSON.parse(userData)
          studentInfo.value.name = user.name || user.username || 'Student'
        } catch (error) {
          console.error('Error parsing user data:', error)
        }
      }

      loadStudentGroups()
      loadFinalizedTasks()
      // Set initial padding
      // 移除mainContentWrapper引用，因为不再需要动态调整
    })

    // 处理会议上传成功
    const handleMeetingUploadSuccess = async () => {
      ElMessage.success('Meeting document uploaded successfully!')
      // 重新加载会议记录
      await loadTaskMeetings()
      // 可以在这里触发其他需要的更新
    }

    // 处理会议更新成功
    const handleMeetingUpdated = async () => {
      ElMessage.success('Meeting updated successfully!')
      // 重新加载会议记录
      await loadTaskMeetings()
      // 重新加载weekly goals以显示最新状态
      await loadStudentWeeklyGoals()
      // 可以在这里触发其他需要的更新
    }

    // 添加消息到对话框的辅助函数
    const addMessageToChat = message => {
      // 如果是移除消息的请求
      if (message.remove) {
        if (message.id) {
          // 通过ID移除特定消息
          chatMessages.value = chatMessages.value.filter(
            msg => msg.id !== message.id
          )
        } else if (message.isTyping) {
          // 移除所有typing消息
          chatMessages.value = chatMessages.value.filter(msg => !msg.isTyping)
        }
        return
      }

      // 添加消息到对话框
      chatMessages.value.push(message)

      // 滚动到底部
      nextTick(() => {
        scrollToBottom()
      })
    }

    // 处理对话更新成功（Agent建议生成后）
    const handleConversationUpdated = async () => {
      // 重新加载对话历史以显示最新的Agent建议
      await loadConversationHistory()
    }

    // Meeting Management Functions
    const openMeetingManagementDialog = () => {
      isMeetingExpanded.value = !isMeetingExpanded.value
    }

    const openMeetingDialog = meeting => {
      currentMeetingData.value = meeting
      showMeetingManagementDialog.value = true
    }

    const openNewMeetingDialog = () => {
      console.log('开始创建新会议')
      console.log('当前会议记录:', meetingNotes.value)

      // 简化逻辑：如果没有会议记录，从1开始；否则找到最大编号+1
      let nextMeetingNumber = 1
      if (meetingNotes.value && meetingNotes.value.length > 0) {
        console.log('现有会议数量:', meetingNotes.value.length)
        const maxMeetingNo = meetingNotes.value.reduce((max, meeting) => {
          // 使用meeting.meetingNo如果存在，否则从title解析
          const meetingNo =
            meeting.meetingNo ||
            parseInt(meeting.title.match(/Meeting (\d+)/)?.[1] || 0)
          console.log(`会议 ${meeting.title} 的编号: ${meetingNo}`)
          return Math.max(max, meetingNo)
        }, 0)
        nextMeetingNumber = maxMeetingNo + 1
      }

      console.log('计算出的下一个会议编号:', nextMeetingNumber)

      currentMeetingData.value = {
        meetingNo: nextMeetingNumber,
        title: `Meeting ${nextMeetingNumber} - Week ${nextMeetingNumber}`,
        status: 'UNFINISHED',
        content: '',
        documentUrl: ''
      }
      console.log('创建的会议数据:', currentMeetingData.value)
      showMeetingManagementDialog.value = true
    }

    // Add new refs for dashboard
    const showingDashboard = ref(true) // Default to showing dashboard
    const selectedGroupId = ref(null)

    // Add new method for showing dashboard
    const showDashboardOverview = () => {
      showingDashboard.value = true
      selectedTask.value = null
    }

    return {
      search,
      studentInfo,
      chatInput,
      chatMessages,
      chatTitle,
      chatMessagesRef,
      meetingNotes,
      todos,
      loading,
      studentGroups,
      groupTasks,
      openedGroups,
      selectedTask,
      currentGroupMembers,
      assignmentForm,
      isAssignmentExpanded,
      isAssignmentSubmitted,
      isAssignmentFinalized,
      assignmentLoading,
      agentAdviceLoading,
      updateLoading,
      finalizeLoading,
      finalizedTasks,
      selectedFinalizedTask,
      // UI state
      isTaskDescriptionExpanded,
      isTodoExpanded,
      isTopPanelExpanded,
      isTaskPreviewExpanded,
      toggleTaskDescription,
      toggleTodo,
      toggleAssignmentForm,
      toggleTaskPreview,
      formatTime,
      formatDate,
      getAssignmentState,
      loadStudentGroups,
      loadGroupTasks,
      loadGroupMembers,
      selectTask,
      handleMenuSelect,
      closeTaskAssignment,
      resetAssignment,
      submitTaskAssignment,
      updateAssignment,
      finalizeAssignment,
      confirmTaskAssignment,
      confirmLoading,
      taskCycle,
      showCycleInput,
      loadFinalizedTasks,
      viewFinalizedAssignment,
      loadConversationHistory,
      loadTaskMeetings,
      loadStudentWeeklyGoals,
      sendMessage,
      getAgentAdvice,
      handleUserCommand,
      scrollToBottom,
      renderMarkdown,
      lastApiResponse,
      // 文件预览相关
      filePreviewVisible,
      previewFileUrl,
      previewFileName,
      previewType,
      previewUrl,
      previewContent,
      previewLoading,
      previewError,
      previewFile,
      getFileName,
      getFileType,
      openInNewTab,
      downloadFile,
      // 弹窗状态
      taskDescriptionModalVisible,
      assignmentModalVisible,
      taskPreviewModalVisible,
      openTaskDescriptionModal,
      openAssignmentModal,
      openTaskPreviewModal,
      previewFileSize,
      formatFileSize,
      handlePreviewError,
      showWeeklyGoalDialog,
      openWeeklyGoalDialog,
      handleGoalsUpdated,
      // 编辑周目标
      editWeeklyGoal,
      // 处理单个目标更新
      handleGoalUpdated,
      // 获取状态类型
      getStatusType,
      // 获取状态文本
      getStatusText,
      // 选中的目标
      selectedGoal,
      // 会议管理 (Complete Meeting功能已移至MeetingManagementDialog)
      viewMeetingDocument,
      handleMeetingUploadSuccess,
      // Meeting Management Dialog
      showMeetingManagementDialog,
      currentMeetingData,
      handleMeetingUpdated,
      handleConversationUpdated,
      openMeetingManagementDialog,
      openMeetingDialog,
      openNewMeetingDialog,
      isMeetingExpanded,
      addMessageToChat,
      showingDashboard,
      selectedGroupId,
      showDashboardOverview
    }
  }
}
</script>

<style scoped>
.student-dashboard {
  display: flex;
  height: 100vh;
  background-color: #f5f7fa;
}

.sidebar {
  background-color: #304156;
  color: white;
  border-right: 1px solid #e4e7ed;
}

.sidebar-header {
  padding: 20px;
  font-size: 18px;
  font-weight: bold;
  border-bottom: 1px solid #434a5a;
  text-align: center;
}

.project-menu {
  border: none;
  width: 100%;
}

.no-groups-message {
  padding: 20px;
  text-align: center;
  color: #bfcbd9;
  font-style: italic;
}

.content-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.main-header {
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 24px;
  height: 60px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}

.header-components {
  display: flex;
  gap: 16px;
  flex: 1;
}

.header-component {
  flex: 1;
  min-width: 0;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background: white;
  padding: 8px 12px;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.2s ease;
}

.header-component:hover {
  border-color: #409eff;
  box-shadow: 0 2px 4px 0 rgba(64, 158, 255, 0.12);
}

.header-component-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  padding: 0;
  height: 32px;
}

.header-component-title span {
  color: #2c3e50;
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.header-component-title .toggle-icon {
  font-size: 14px;
  color: #909399;
  transition: transform 0.3s ease;
  flex-shrink: 0;
}

.header-component-title .action-icon {
  font-size: 14px;
  color: #909399;
  transition: transform 0.3s ease;
  flex-shrink: 0;
}

.page-title {
  margin: 0;
  font-size: 20px;
  color: #2c3e50;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.search-input {
  width: 200px;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 6px;
  transition: background-color 0.3s;
}

.user-dropdown:hover {
  background-color: #f5f7fa;
}

.username {
  font-size: 14px;
  color: #2c3e50;
}

/* 下拉菜单样式 */
.el-dropdown-menu {
  text-align: right;
}

.el-dropdown-menu__item {
  text-align: right;
  justify-content: flex-end;
}

/* Top Components Panel */
.top-components-panel {
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 8px 24px; /* 减少padding */
}

.top-components-row {
  display: flex;
  gap: 16px;
}

.top-component-card {
  flex: 1;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: white;
  overflow: hidden;
}

.component-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f8f9fa;
  border-bottom: 1px solid #e4e7ed;
  cursor: pointer;
  transition: background-color 0.3s;
}

.component-header:hover {
  background: #e9ecef;
}

.component-header span {
  font-weight: 500;
  color: #2c3e50;
}

.toggle-icon {
  transition: transform 0.3s ease;
}

.component-content {
  padding: 16px;
}

.task-meta {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-top: 12px;
}

.due-date {
  font-size: 14px;
  color: #909399;
}

.empty-state {
  text-align: center;
  color: #909399;
  padding: 40px 20px;
  font-style: italic;
}

/* 弹窗中的空状态消息左对齐 */
.task-modal .empty-state,
.assignment-modal .empty-state {
  text-align: left;
}

.assignment-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px;
  text-align: left;
}

.member-assignment-row {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  text-align: left;
  margin-bottom: 16px;
}

.member-info {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 150px;
  flex-shrink: 0;
  text-align: left;
}

.member-name {
  font-weight: 500;
  color: #2c3e50;
  font-size: 15px;
  text-align: left;
}

.member-task {
  flex: 1;
}

.member-task .el-textarea {
  width: 100%;
}

.action-buttons {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  justify-content: flex-end;
}

.file-preview {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.file-actions {
  display: flex;
  gap: 8px;
}

/* Main Content */
.main-content {
  background: #f5f7fa;
  padding: 8px 24px; /* 减少padding */
  flex: 1;
  overflow: hidden;
}

.content-row {
  display: flex;
  gap: 16px;
  height: 100%;
}

.chat-section {
  flex: 1;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  margin-bottom: 16px; /* 底部留点空隙 */
}

.chat-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 16px 20px;
  border-radius: 8px 8px 0 0;
  text-align: center;
}

.chat-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.chat-header .el-tag {
  margin-left: 8px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  background: #fafbfc;
}

.message-wrapper {
  margin-bottom: 16px;
  display: flex;
}

.message {
  padding: 12px 16px;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  max-width: 70%;
  word-wrap: break-word;
}

.user-message {
  margin-left: auto;
  background: linear-gradient(135deg, #409eff 0%, #36a3f7 100%);
  color: white;
  border-bottom-right-radius: 4px;
}

.ai-message {
  margin-right: auto;
  background: white;
  color: #2c3e50;
  border: 1px solid #e4e7ed;
  border-bottom-left-radius: 4px;
}

/* 修复AI消息markdown内容左对齐 */
.ai-message .message-content {
  text-align: left !important;
}

.ai-message .message-content * {
  text-align: left !important;
}

.ai-message .message-content h1,
.ai-message .message-content h2,
.ai-message .message-content h3,
.ai-message .message-content h4,
.ai-message .message-content h5,
.ai-message .message-content h6 {
  text-align: left !important;
  margin: 12px 0 8px 0;
}

.ai-message .message-content p {
  text-align: left !important;
  margin: 8px 0;
}

.ai-message .message-content ul,
.ai-message .message-content ol {
  text-align: left !important;
  margin: 8px 0;
  padding-left: 20px;
}

.ai-message .message-content li {
  text-align: left !important;
  margin: 4px 0;
}

.ai-message .message-content pre {
  text-align: left !important;
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  overflow-x: auto;
  margin: 8px 0;
}

.ai-message .message-content code {
  text-align: left !important;
  background: #f5f7fa;
  padding: 2px 4px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
}

.ai-message .message-content blockquote {
  text-align: left !important;
  border-left: 4px solid #409eff;
  padding-left: 12px;
  margin: 8px 0;
  color: #6b7280;
  font-style: italic;
}

.ai-message .message-content table {
  text-align: left !important;
  border-collapse: collapse;
  width: 100%;
  margin: 8px 0;
}

.ai-message .message-content th,
.ai-message .message-content td {
  text-align: left !important;
  border: 1px solid #e5e7eb;
  padding: 8px;
}

.ai-message .message-content th {
  background-color: #f9fafb;
  font-weight: 600;
}

.message-sender {
  font-weight: 500;
  margin-bottom: 4px;
  font-size: 12px;
}

.user-message .message-sender {
  color: rgba(255, 255, 255, 0.8);
}

.ai-message .message-sender {
  color: #409eff;
}

.message-content {
  line-height: 1.5;
  margin-bottom: 4px;
}

.message-time {
  font-size: 10px;
  opacity: 0.7;
  text-align: right;
}

.user-message .message-time {
  color: rgba(255, 255, 255, 0.6);
}

.ai-message .message-time {
  color: #909399;
}

.empty-chat {
  text-align: center;
  color: #909399;
  padding: 40px 20px;
  font-style: italic;
}

.chat-input {
  border-top: 1px solid #e4e7ed;
  padding: 16px 20px;
  background: white;
  border-radius: 0 0 8px 8px;
}

/* Right Panel - Meeting Notes and To-Do List */
.right-panel {
  width: 300px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.notes-section {
  flex: 1;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 0;
}

.notes-header {
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
  color: #2c3e50;
  padding: 16px 20px;
  border-radius: 8px 8px 0 0;
  flex-shrink: 0;
}

.notes-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
}

.notes-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  background: #fafbfc;
}

.meeting-item {
  background: white;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;
  border-left: 4px solid #409eff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.meeting-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.meeting-header h4 {
  margin: 0;
  color: #2c3e50;
  font-size: 14px;
}

.meeting-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.meeting-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  background: #fafbfc;
}

.meeting-actions-bar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e4e7ed;
}

.meeting-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.meeting-meta {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

.no-document {
  color: #f56c6c;
  font-style: italic;
}

.empty-notes {
  text-align: center;
  padding: 20px;
  color: #999;
  font-style: italic;
}

/* To-Do Section in Right Panel */
.todo-section-right {
  flex: 1;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 0;
}

.todo-header {
  background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
  color: #2c3e50;
  padding: 16px 20px;
  border-radius: 8px 8px 0 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  flex-shrink: 0;
}

.todo-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
}

.todo-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  background: #fafbfc;
}

.todo-columns {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.todo-column {
  background: white;
  border-radius: 6px;
  padding: 12px;
  border-left: 3px solid #409eff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.todo-column h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #2c3e50;
}

.todo-column p {
  margin: 0;
  font-size: 12px;
  color: #606266;
  line-height: 1.4;
}

/* 隐藏原来的底部To-Do List */
.todo-section {
  display: none;
}

/* Modal Styles */
.task-modal .el-dialog__body,
.assignment-modal .el-dialog__body,
.preview-modal .el-dialog__body {
  padding: 0;
}

.modal-content {
  max-height: 70vh;
  overflow-y: auto;
}

/* 添加专门的样式用于分工弹窗 */
.assignment-modal-content {
  max-height: 70vh;
  overflow-y: auto;
  padding: 20px;
}

.task-description-content {
  padding: 16px;
  text-align: left;
}

.task-description-text {
  white-space: pre-wrap; /* Preserve line breaks */
  font-family: 'Arial', 'Microsoft YaHei', sans-serif;
  font-size: 14px;
  line-height: 1.6;
  color: #333;
  background-color: #f8f9fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 10px;
  margin-bottom: 15px;
  word-break: break-word; /* Break long words */
  overflow-wrap: break-word; /* Break long words */
  text-align: left; /* 确保文字左对齐 */
}

/* 确保弹窗内容左对齐 */
.task-modal .modal-content,
.assignment-modal .modal-content {
  text-align: left;
}

/* 确保弹窗标题左对齐 */
.task-modal .el-dialog__header,
.assignment-modal .el-dialog__header,
.preview-modal .el-dialog__header {
  text-align: left;
}

/* 确保弹窗内所有文本元素左对齐 */
.task-modal .el-dialog__body *,
.assignment-modal .el-dialog__body *,
.preview-modal .el-dialog__body * {
  text-align: left;
}

/* 确保成员信息左对齐 */
.member-info {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 150px;
  flex-shrink: 0;
  text-align: left;
}

.member-name {
  font-weight: 500;
  color: #2c3e50;
  font-size: 15px;
  text-align: left;
}

.file-preview-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f8f9fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}

.file-icon {
  font-size: 24px;
  color: #409eff;
}

.file-details {
  display: flex;
  flex-direction: column;
  margin-left: 10px;
}

.file-name {
  font-weight: 500;
  color: #2c3e50;
  font-size: 15px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-size {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* File Preview Modal */
.file-preview-modal .el-dialog__body {
  padding: 0;
}

.file-preview-content {
  max-height: 70vh;
  overflow-y: auto;
}

.loading-state,
.error-state {
  text-align: center;
  padding: 40px;
  color: #909399;
}

.loading-state .el-icon {
  font-size: 32px;
  margin-bottom: 16px;
}

.pdf-preview iframe,
.office-preview iframe {
  width: 100%;
  height: 600px;
  border: none;
}

.image-preview img {
  max-width: 100%;
  height: auto;
  display: block;
  margin: 0 auto;
}

.text-preview pre {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 4px;
  overflow-x: auto;
  font-family: 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.4;
  margin: 0;
}

.unsupported-preview {
  text-align: center;
  padding: 40px;
  color: #909399;
}

.empty-icon {
  font-size: 48px;
  color: #c0c4cc;
  margin-bottom: 16px;
}

/* Responsive Design */
@media (max-width: 1200px) {
  .meeting-section {
    width: 250px;
  }

  .main-content {
    padding: 16px;
  }

  .top-components-panel {
    padding: 16px;
  }
}

@media (max-width: 768px) {
  .content-row {
    flex-direction: column;
  }

  .notes-section {
    width: 100%;
    max-height: 200px;
  }

  .top-components-row {
    flex-direction: column;
  }

  .main-content {
    padding: 12px;
  }

  .top-components-panel {
    padding: 12px;
  }
}

/* Cycle Input Styles */
.cycle-input-section {
  margin-top: 20px;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.cycle-input-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.cycle-label {
  font-weight: 500;
  color: #333;
  min-width: 120px;
}

.cycle-hint {
  color: #666;
  font-size: 12px;
  flex: 1;
  min-width: 200px;
}

/* Todo Status Styles */
.todo-status {
  margin-top: 8px;
}

.todo-column.status-finished {
  opacity: 0.7;
  background-color: #f0f9ff;
}

.todo-column.status-processing {
  border-left: 3px solid #f39c12;
}

.todo-column.status-notuploaded {
  border-left: 3px solid #3498db;
}

.empty-todos {
  text-align: center;
  padding: 20px;
  color: #999;
  font-style: italic;
}

.empty-notes {
  text-align: center;
  padding: 20px;
  color: #999;
  font-style: italic;
}

.todo-item {
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  margin-bottom: 12px;
  background-color: #ffffff;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
}

.todo-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.todo-content-inner {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.todo-content-inner h4 {
  margin: 0;
  font-size: 14px;
  color: #303133;
}

.status-tag {
  margin-left: 8px;
}

/* Meeting Management Styles */
.notes-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.notes-header h3 {
  margin: 0;
  color: #2c3e50;
  font-size: 16px;
}

.meeting-item {
  background: white;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s ease;
}

.meeting-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.meeting-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.meeting-header h4 {
  margin: 0;
  color: #2c3e50;
  font-size: 14px;
  font-weight: 600;
}

.meeting-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.meeting-content {
  margin: 8px 0;
  color: #606266;
  font-size: 12px;
  line-height: 1.4;
}

.meeting-meta {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
  display: flex;
  gap: 16px;
}

.no-document {
  color: #f56c6c;
  font-style: italic;
}

.empty-notes {
  text-align: center;
  padding: 20px;
  color: #999;
  font-style: italic;
}

.meeting-section {
  flex: 1;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 0;
}

.meeting-header {
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
  color: #2c3e50;
  padding: 16px 20px;
  border-radius: 8px 8px 0 0;
  flex-shrink: 0;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  user-select: none;
}

.meeting-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
}

.meeting-header .header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.meeting-header .toggle-icon {
  font-size: 14px;
  transition: transform 0.3s ease;
}

.meeting-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  background: #fafbfc;
}

.meeting-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.meeting-item {
  background: white;
  border-radius: 8px;
  padding: 12px;
  border-left: 4px solid #409eff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s ease;
}

.meeting-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  transform: translateY(-1px);
}

.meeting-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  flex-wrap: nowrap;
}

.meeting-item-header h4 {
  margin: 0;
  color: #2c3e50;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
  margin-right: 8px;
}

.meeting-item-header .el-tag {
  font-size: 10px;
  flex-shrink: 0;
}

.meeting-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.meeting-content-text {
  color: #606266;
  font-size: 13px;
  line-height: 1.4;
  margin: 8px 0;
}

.meeting-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.meeting-meta .no-document {
  color: #f56c6c;
  font-style: italic;
}

.empty-meetings {
  text-align: center;
  padding: 40px 20px;
  color: #909399;
}

.empty-meetings p {
  margin: 0;
  font-size: 14px;
  line-height: 1.5;
}

.typing-message {
  animation: blink 0.7s step-end infinite;
}

@keyframes blink {
  50% {
    background-color: rgba(255, 255, 255, 0.5);
  }
}

.typing-content {
  position: relative;
}

.typing-indicator {
  display: flex;
  align-items: center;
  padding: 8px;
  min-height: 30px;
}

.typing-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #409eff;
  margin: 0 2px;
  animation: typing-bounce 1.4s infinite;
}

.typing-dot:nth-child(1) {
  animation-delay: 0s;
}

.typing-dot:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-dot:nth-child(3) {
  animation-delay: 0.4s;
}

.typing-text {
  margin-left: 8px;
  color: #409eff;
  font-style: italic;
}

@keyframes typing-bounce {
  0%,
  60%,
  100% {
    transform: translateY(0);
    opacity: 0.3;
  }
  30% {
    transform: translateY(-10px);
    opacity: 1;
  }
}

/* Add new styles for dashboard menu item */
.el-menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.el-menu-item .el-icon {
  margin-right: 5px;
}

/* 修改分工文本框样式 */
.assignment-textarea :deep(.el-textarea__inner) {
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
  overflow-wrap: break-word;
  overflow-y: hidden !important; /* 移除textarea的滚动条 */
  border-color: #dcdfe6;
  transition: border-color 0.2s;
}

.assignment-textarea :deep(.el-textarea__inner):focus {
  border-color: #409eff;
}

.assignment-textarea :deep(.el-textarea__inner):hover {
  border-color: #c0c4cc;
}

.member-assignment-row {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  text-align: left;
  margin-bottom: 16px;
}

.member-task {
  flex: 1;
  min-width: 0;
}

/* 确保弹窗内容正确显示 */
.assignment-modal :deep(.el-dialog__body) {
  padding: 0;
  max-height: calc(90vh - 120px); /* 减去标题和底部按钮的高度 */
  overflow: visible;
}

.assignment-modal-content {
  max-height: calc(90vh - 120px);
  overflow-y: auto;
  padding: 20px;
}
</style>
