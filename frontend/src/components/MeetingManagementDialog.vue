<template>
  <el-dialog
    title="Meeting Management"
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    width="70%"
    class="meeting-management-dialog"
  >
    <div class="meeting-management-container">
      <div class="meeting-header">
        <h3>{{ meetingData?.title || 'Meeting ' + meetingData?.meetingNo }}</h3>
        <el-tag :type="getStatusType(meetingData?.status)" class="status-tag">
          {{ meetingData?.status }}
        </el-tag>
      </div>

      <!-- 验证状态检查 -->
      <div class="validation-section">
        <h4>Upload Requirements:</h4>

        <!-- 综合验证状态 -->
        <div class="validation-item">
          <div v-if="canUpload" class="status-success">
            <el-icon><Check /></el-icon>
            <span>Meeting can be uploaded</span>
          </div>
          <div v-else class="status-error">
            <el-icon><Close /></el-icon>
            <span>{{ uploadValidationMessage }}</span>
          </div>
        </div>

        <!-- 详细验证结果 -->
        <div v-if="validationDetails" class="validation-details">
          <!-- 周目标状态检查 -->
          <div class="validation-item">
            <div
              v-if="
                validationDetails.goalsCheck &&
                validationDetails.goalsCheck.allReady
              "
              class="status-success"
            >
              <el-icon><Check /></el-icon>
              <span>{{ validationDetails.goalsCheck.message }}</span>
            </div>
            <div v-else class="status-warning">
              <el-icon><Warning /></el-icon>
              <span>{{
                validationDetails.goalsCheck?.message ||
                'Checking weekly goals status...'
              }}</span>
            </div>
          </div>

          <!-- 前一个会议状态检查 -->
          <div
            class="validation-item"
            v-if="meetingData?.meetingNo && meetingData?.meetingNo > 1"
          >
            <div
              v-if="
                validationDetails.previousMeetingCheck &&
                validationDetails.previousMeetingCheck.previousCompleted
              "
              class="status-success"
            >
              <el-icon><Check /></el-icon>
              <span>{{ validationDetails.previousMeetingCheck.message }}</span>
            </div>
            <div v-else class="status-warning">
              <el-icon><Warning /></el-icon>
              <span>{{
                validationDetails.previousMeetingCheck?.message ||
                'Checking previous meeting status...'
              }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 文件上传区域 -->
      <div class="file-upload-section">
        <h4>Meeting Document:</h4>

        <!-- 当前文档显示 -->
        <div v-if="meetingData?.documentUrl" class="current-document">
          <div class="document-info">
            <el-icon class="document-icon"><Document /></el-icon>
            <span>Current document uploaded</span>
            <el-button
              type="primary"
              size="small"
              @click="previewCurrentDocument"
            >
              <el-icon><View /></el-icon>
              Preview
            </el-button>
          </div>
        </div>

        <!-- 文件选择 -->
        <div class="file-selector">
          <el-upload
            class="upload-demo"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleFileChange"
            :before-upload="beforeUpload"
            accept=".pdf,.doc,.docx,.txt,.md"
          >
            <el-button type="primary" :disabled="!canUpload">
              <el-icon><Upload /></el-icon>
              Choose Meeting Document
            </el-button>
          </el-upload>

          <!-- 选择的文件预览 -->
          <div v-if="selectedFile" class="selected-file-preview">
            <div class="file-info">
              <el-icon class="file-icon"><Document /></el-icon>
              <div class="file-details">
                <span class="file-name">{{ selectedFile.name }}</span>
                <span class="file-size"
                  >({{ formatFileSize(selectedFile.size) }})</span
                >
              </div>
              <el-button type="danger" size="small" @click="removeSelectedFile">
                <el-icon><Delete /></el-icon>
                Remove
              </el-button>
            </div>

            <!-- 文件预览 -->
            <div class="file-preview-container">
              <div class="preview-header">
                <h5>File Preview:</h5>
                <el-button-group>
                  <el-button size="small" @click="previewSelectedFile">
                    <el-icon><View /></el-icon>
                    Preview
                  </el-button>
                </el-button-group>
              </div>
            </div>
          </div>
        </div>

        <!-- 上传状态 -->
        <div v-if="uploading" class="upload-status">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>Uploading file to cloud storage...</span>
        </div>

        <div v-if="uploadedFileUrl" class="upload-success">
          <el-icon><Check /></el-icon>
          <span>File uploaded successfully!</span>
        </div>
      </div>

      <!-- 会议状态和信息 -->
      <div class="meeting-status-section">
        <h4>Meeting Status:</h4>
        <div class="status-info">
          <p>
            <strong>Scheduled Date:</strong>
            {{
              formatDate(
                meetingData?.meetingDate ||
                  new Date().toISOString().split('T')[0]
              )
            }}
          </p>
          <p>
            <strong>Status:</strong>
            <el-tag :type="getStatusType(localMeetingStatus)" size="small">
              {{ localMeetingStatus || 'UNFINISHED' }}
            </el-tag>
          </p>
          <p v-if="meetingData?.documentUrl">
            <strong>Document:</strong> Uploaded
          </p>
          <p v-else><strong>Document:</strong> Not uploaded</p>
        </div>
      </div>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="$emit('update:modelValue', false)">Cancel</el-button>

        <!-- Upload/Update Meeting Button -->
        <el-button
          v-if="!meetingData?.documentUrl || selectedFile"
          type="primary"
          @click="handleSubmit"
          :disabled="!canSubmit"
          :loading="submitting"
        >
          {{ meetingData?.documentUrl ? 'Update Meeting' : 'Upload Meeting' }}
        </el-button>

        <!-- Complete Meeting Button -->
        <el-button
          v-if="meetingData?.documentUrl && localMeetingStatus === 'UNFINISHED'"
          type="success"
          @click="handleCompleteMeeting"
          :loading="completing"
        >
          Complete Meeting
        </el-button>

        <!-- Get Agent Advice Button -->
        <el-button
          v-if="meetingData?.documentUrl && localMeetingStatus === 'COMPLETED'"
          type="warning"
          @click="handleGetAgentAdvice"
          :loading="gettingAdvice"
        >
          Get Agent Advice
        </el-button>
      </span>
    </template>

    <!-- 文件预览弹框 -->
    <el-dialog
      v-model="filePreviewVisible"
      :title="'File Preview: ' + previewFileName"
      width="80%"
      class="file-preview-dialog"
    >
      <div class="file-preview-content" style="height: 500px">
        <!-- PDF预览 -->
        <div v-if="previewFileType === 'pdf'" style="height: 100%">
          <div style="margin-bottom: 10px; text-align: center">
            <el-button-group>
              <el-button type="primary" @click="openPreviewInNewTab">
                <el-icon><View /></el-icon>
                Browser Preview
              </el-button>
              <el-button @click="useGoogleViewer">
                <el-icon><Document /></el-icon>
                Google Viewer
              </el-button>
            </el-button-group>
          </div>
          <iframe
            :src="previewUrl"
            style="width: 100%; height: calc(100% - 50px); border: none"
          ></iframe>
        </div>

        <!-- Office文档预览 -->
        <div v-else-if="previewFileType === 'office'" style="height: 100%">
          <div style="margin-bottom: 10px; text-align: center">
            <el-button-group>
              <el-button type="primary" @click="openPreviewInNewTab">
                <el-icon><View /></el-icon>
                Browser Preview
              </el-button>
              <el-button @click="useMicrosoftViewer">
                <el-icon><OfficeBuilding /></el-icon>
                Office Online
              </el-button>
            </el-button-group>
          </div>
          <iframe
            :src="previewUrl"
            style="width: 100%; height: calc(100% - 50px); border: none"
          ></iframe>
        </div>

        <!-- 文本文件预览 -->
        <div
          v-else-if="previewFileType === 'text'"
          style="height: 100%; overflow-y: auto"
        >
          <pre class="text-preview">{{ previewContent }}</pre>
        </div>

        <!-- 其他文件类型 -->
        <div v-else class="unsupported-preview">
          <el-icon size="64"><Document /></el-icon>
          <p>This file type cannot be previewed directly</p>
          <el-button type="primary" @click="openPreviewInNewTab">
            <el-icon><View /></el-icon>
            Open in Browser
          </el-button>
        </div>
      </div>
      <template #footer>
        <el-button @click="filePreviewVisible = false">Close</el-button>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<script>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Check,
  Close,
  Warning,
  Document,
  View,
  Upload,
  Delete,
  Loading,
  OfficeBuilding
} from '@element-plus/icons-vue'
import { studentAPI } from '@/api/student'
import axios from 'axios'

export default {
  name: 'MeetingManagementDialog',
  components: {
    Check,
    Close,
    Warning,
    Document,
    View,
    Upload,
    Delete,
    Loading,
    OfficeBuilding
  },
  props: {
    modelValue: {
      type: Boolean,
      default: false
    },
    meetingData: {
      type: Object,
      default: () => ({})
    },
    taskId: {
      type: Number,
      required: true
    },
    addMessageToChat: {
      type: Function,
      default: null
    }
  },
  emits: ['update:modelValue', 'meeting-updated'],
  setup(props, { emit }) {
    const canUpload = ref(false)
    const uploadValidationMessage = ref('')
    const validationDetails = ref(null)
    const goalsStatus = ref(null)
    const selectedFile = ref(null)
    const uploadedFileUrl = ref('')
    const uploading = ref(false)
    const submitting = ref(false)
    const completing = ref(false)
    const gettingAdvice = ref(false)
    const localMeetingStatus = ref('')

    // 文件预览相关
    const filePreviewVisible = ref(false)
    const previewFileName = ref('')
    const previewFileType = ref('')
    const previewUrl = ref('')
    const previewContent = ref('')

    // 计算属性
    const canSubmit = computed(() => {
      return (
        canUpload.value &&
        (selectedFile.value ||
          uploadedFileUrl.value ||
          props.meetingData?.documentUrl)
      )
    })

    const getStatusType = status => {
      switch (status) {
        case 'COMPLETED':
          return 'success'
        case 'UNFINISHED':
          return 'warning'
        default:
          return 'info'
      }
    }

    // 监听弹框打开
    watch(
      () => props.modelValue,
      async visible => {
        if (visible) {
          console.log('=== MeetingManagementDialog 打开 ===')
          console.log('taskId:', props.taskId)
          console.log('meetingData:', props.meetingData)
          console.log('meetingData.meetingNo:', props.meetingData?.meetingNo)
          console.log(
            'meetingData keys:',
            props.meetingData ? Object.keys(props.meetingData) : []
          )

          await checkUploadRequirements()
          localMeetingStatus.value = props.meetingData?.status || 'UNFINISHED'
        } else {
          resetDialog()
        }
      }
    )

    // 检查上传要求
    const checkUploadRequirements = async () => {
      console.log('检查上传要求开始')
      console.log('taskId:', props.taskId)
      console.log('meetingNo:', props.meetingData?.meetingNo)
      console.log('meetingData:', props.meetingData)

      // 验证必要的数据是否存在
      if (!props.taskId) {
        console.error('taskId is undefined or null')
        canUpload.value = false
        uploadValidationMessage.value = 'Task ID is not defined'
        return
      }

      // 验证meetingNo是否存在
      if (!props.meetingData?.meetingNo) {
        console.error('meetingNo is undefined or null')
        canUpload.value = false
        uploadValidationMessage.value = 'Meeting number is not defined'
        return
      }

      try {
        // 调用后端API检查上传条件
        const response = await studentAPI.checkMeetingUploadRequirements(
          props.taskId,
          props.meetingData?.meetingNo
        )

        if (response.data && response.data.code === 1) {
          const result = response.data.data
          canUpload.value = result.canUpload
          uploadValidationMessage.value = result.message
          validationDetails.value = result // 存储整个验证结果对象

          // 设置goalsStatus，将后端的goalsCheck映射为前端期望的格式
          if (result.goalsCheck) {
            goalsStatus.value = {
              allProcessing: result.goalsCheck.allReady,
              ...result.goalsCheck
            }
          }

          console.log('上传条件检查结果:', result)
        } else {
          canUpload.value = false
          uploadValidationMessage.value = 'Failed to check upload requirements'
        }
      } catch (error) {
        console.error('检查上传条件时发生错误:', error)
        canUpload.value = false
        uploadValidationMessage.value =
          'Error checking upload requirements: ' + error.message
      }
    }

    // 文件处理
    const beforeUpload = file => {
      const isValidType = [
        'application/pdf',
        'application/msword',
        'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
        'text/plain',
        'text/markdown'
      ].includes(file.type)
      const isLt10M = file.size / 1024 / 1024 < 10

      if (!isValidType) {
        ElMessage.error(
          'Only PDF, Word, TXT, and Markdown files are supported!'
        )
        return false
      }
      if (!isLt10M) {
        ElMessage.error('File size must be less than 10MB!')
        return false
      }
      return true
    }

    const handleFileChange = file => {
      if (beforeUpload(file.raw)) {
        selectedFile.value = file.raw
        uploadedFileUrl.value = ''
      }
    }

    const removeSelectedFile = () => {
      selectedFile.value = null
      uploadedFileUrl.value = ''
    }

    // 文件预览
    const previewSelectedFile = () => {
      if (!selectedFile.value) return

      previewFileName.value = selectedFile.value.name
      const extension = selectedFile.value.name.split('.').pop().toLowerCase()

      if (['pdf'].includes(extension)) {
        previewFileType.value = 'pdf'
        previewUrl.value = URL.createObjectURL(selectedFile.value)
      } else if (['doc', 'docx'].includes(extension)) {
        previewFileType.value = 'office'
        previewUrl.value = URL.createObjectURL(selectedFile.value)
      } else if (['txt', 'md'].includes(extension)) {
        previewFileType.value = 'text'
        const reader = new FileReader()
        reader.onload = e => {
          previewContent.value = e.target.result
        }
        reader.readAsText(selectedFile.value)
      } else {
        previewFileType.value = 'other'
      }

      filePreviewVisible.value = true
    }

    const previewCurrentDocument = async () => {
      if (!props.meetingData?.documentUrl) return

      try {
        // 获取签名URL用于预览
        const response = await axios.post(
          '/api/file/preview-url',
          props.meetingData?.documentUrl,
          {
            headers: {
              'Content-Type': 'text/plain',
              Authorization: `Bearer ${localStorage.getItem('authToken')}`
            }
          }
        )

        if (response.data.code === 1) {
          const signedUrl = response.data.data
          previewFileName.value = props.meetingData?.title || 'Meeting Document'

          const extension = props.meetingData?.documentUrl
            .split('.')
            .pop()
            .toLowerCase()
          if (['pdf'].includes(extension)) {
            previewFileType.value = 'pdf'
            previewUrl.value = signedUrl
          } else if (['doc', 'docx'].includes(extension)) {
            previewFileType.value = 'office'
            previewUrl.value = `https://view.officeapps.live.com/op/embed.aspx?src=${encodeURIComponent(
              signedUrl
            )}`
          } else if (['txt', 'md'].includes(extension)) {
            previewFileType.value = 'text'
            try {
              const textResponse = await axios.get(signedUrl, {
                responseType: 'text'
              })
              previewContent.value = textResponse.data
            } catch (e) {
              previewContent.value = 'Failed to load file content'
            }
          } else {
            previewFileType.value = 'other'
          }

          filePreviewVisible.value = true
        }
      } catch (error) {
        console.error('Error previewing document:', error)
        ElMessage.error('Failed to preview document')
      }
    }

    // 预览工具函数
    const openPreviewInNewTab = () => {
      window.open(previewUrl.value, '_blank')
    }

    const useGoogleViewer = () => {
      previewUrl.value = `https://docs.google.com/gview?url=${encodeURIComponent(
        previewUrl.value
      )}&embedded=true`
    }

    const useMicrosoftViewer = () => {
      previewUrl.value = `https://view.officeapps.live.com/op/embed.aspx?src=${encodeURIComponent(
        previewUrl.value
      )}`
    }

    // 上传文件到OSS
    const uploadFileToOSS = async file => {
      const formData = new FormData()
      formData.append('file', file)

      const token = localStorage.getItem('authToken')
      const headers = {
        Authorization: `Bearer ${token}`
      }

      try {
        const response = await axios.post('/api/upload', formData, {
          headers: {
            ...headers,
            'Content-Type': 'multipart/form-data'
          }
        })

        if (response.data.code === 1) {
          return response.data.data
        } else {
          throw new Error(response.data.msg || 'Upload failed')
        }
      } catch (error) {
        console.error('Upload error:', error)
        throw new Error(
          'File upload failed: ' + (error.response?.data?.msg || error.message)
        )
      }
    }

    // 提交处理
    const handleSubmit = async () => {
      if (!canSubmit.value) {
        ElMessage.warning(
          'Please ensure all requirements are met before submitting'
        )
        return
      }

      submitting.value = true
      try {
        let fileUrl = uploadedFileUrl.value || props.meetingData?.documentUrl

        // 如果有选择的文件但还没上传，先上传
        if (selectedFile.value && !uploadedFileUrl.value) {
          uploading.value = true
          fileUrl = await uploadFileToOSS(selectedFile.value)
          uploadedFileUrl.value = fileUrl
          uploading.value = false
        }

        // 提交会议数据
        const meetingData = {
          taskId: props.taskId,
          meetingNo: props.meetingData?.meetingNo,
          documentUrl: fileUrl
        }

        const response = await studentAPI.uploadMeetingDocument(meetingData)
        if (response.data && response.data.code === 1) {
          ElMessage.success('Meeting uploaded successfully!')
          emit('meeting-updated', response.data.data)
          emit('update:modelValue', false)
        } else {
          ElMessage.error(
            'Upload failed: ' + (response.data?.msg || 'Unknown error')
          )
        }
      } catch (error) {
        console.error('Error submitting meeting:', error)
        ElMessage.error('Submit failed: ' + error.message)
      } finally {
        submitting.value = false
        uploading.value = false
      }
    }

    // 重置弹框
    const resetDialog = () => {
      selectedFile.value = null
      uploadedFileUrl.value = ''
      uploading.value = false
      submitting.value = false
      localMeetingStatus.value = ''
      filePreviewVisible.value = false
      previewFileName.value = ''
      previewFileType.value = ''
      previewUrl.value = ''
      previewContent.value = ''
      canUpload.value = false
      goalsStatus.value = null
      validationDetails.value = null
      uploadValidationMessage.value = ''
    }

    // 格式化文件大小
    const formatFileSize = bytes => {
      if (bytes === 0) return '0 Bytes'
      const k = 1024
      const sizes = ['Bytes', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    }

    // 格式化日期
    const formatDate = dateString => {
      const date = new Date(dateString)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    }

    // 完成会议
    const handleCompleteMeeting = async () => {
      completing.value = true
      try {
        const response = await studentAPI.completeMeeting(props.meetingData?.id)
        if (response.data && response.data.code === 1) {
          ElMessage.success('Meeting completed successfully!')
          // 立即更新本地状态，使按钮立即更新
          localMeetingStatus.value = 'COMPLETED'
          // 通过emit通知父组件更新状态，不直接修改props
          const updatedMeeting = {
            ...(props.meetingData || {}),
            status: 'COMPLETED'
          }
          emit('meeting-updated', updatedMeeting)
          // 自动关闭弹窗
          emit('update:modelValue', false)
        } else {
          ElMessage.error(
            'Failed to complete meeting: ' +
              (response.data?.msg || 'Unknown error')
          )
        }
      } catch (error) {
        console.error('Error completing meeting:', error)
        ElMessage.error('Failed to complete meeting')
      } finally {
        completing.value = false
      }
    }

    // 获取Agent建议
    const handleGetAgentAdvice = async () => {
      gettingAdvice.value = true
      const typingMessageId = 'agent-typing-' + Date.now() // 生成唯一的typing消息ID

      try {
        // 1. 关闭弹窗
        emit('update:modelValue', false)

        // 2. 添加学生请求消息到对话框
        if (props.addMessageToChat) {
          const userData = JSON.parse(localStorage.getItem('userData') || '{}')
          const userName = userData.name || userData.username || 'Unknown User'
          const weekNo = props.meetingData?.meetingNo || 1
          const requestMessage = `${userName} requested the Agent's suggestions for week ${weekNo}`

          props.addMessageToChat({
            sender: 'user',
            content: requestMessage,
            timestamp: new Date().toISOString(),
            senderName: userName
          })

          // 3. 添加Agent正在输入的提示
          props.addMessageToChat({
            id: typingMessageId,
            sender: 'ai',
            content: 'Agent is typing...',
            timestamp: new Date().toISOString(),
            senderName: 'AI Assistant',
            isTyping: true
          })
        }

        const response = await studentAPI.analyzeWeeklyProgress(
          props.taskId,
          props.meetingData?.meetingNo,
          props.meetingData?.documentUrl
        )

        if (response.data && response.data.code === 1) {
          // 4. 移除typing消息并添加实际回复
          if (props.addMessageToChat) {
            // 先移除typing消息
            props.addMessageToChat({
              id: typingMessageId,
              remove: true
            })

            // 添加实际回复
            props.addMessageToChat({
              sender: 'ai',
              content: response.data.data,
              timestamp: new Date().toISOString(),
              senderName: 'AI Assistant'
            })
          }
        } else {
          throw new Error(response.data?.msg || 'Failed to get agent advice')
        }
      } catch (error) {
        console.error('获取Agent建议失败:', error)
        // 移除typing消息并显示错误信息
        if (props.addMessageToChat) {
          // 先移除typing消息
          props.addMessageToChat({
            id: typingMessageId,
            remove: true
          })

          if (error.response?.status === 500) {
            props.addMessageToChat({
              sender: 'ai',
              content:
                'Sorry, I encountered an error while analyzing the progress. Please try again.',
              timestamp: new Date().toISOString(),
              senderName: 'AI Assistant'
            })
          } else {
            props.addMessageToChat({
              sender: 'ai',
              content:
                'Sorry, there was a network error. Please check your connection and try again.',
              timestamp: new Date().toISOString(),
              senderName: 'AI Assistant'
            })
          }
        }
      } finally {
        gettingAdvice.value = false
      }
    }

    return {
      canUpload,
      uploadValidationMessage,
      validationDetails,
      goalsStatus,
      selectedFile,
      uploadedFileUrl,
      uploading,
      submitting,
      completing,
      gettingAdvice,
      localMeetingStatus,
      canSubmit,
      filePreviewVisible,
      previewFileName,
      previewFileType,
      previewUrl,
      previewContent,
      getStatusType,
      beforeUpload,
      handleFileChange,
      removeSelectedFile,
      previewSelectedFile,
      previewCurrentDocument,
      openPreviewInNewTab,
      useGoogleViewer,
      useMicrosoftViewer,
      handleSubmit,
      formatFileSize,
      formatDate,
      handleCompleteMeeting,
      handleGetAgentAdvice
    }
  }
}
</script>

<style scoped>
.meeting-management-dialog {
  .el-dialog__body {
    padding: 20px 24px;
  }
}

.meeting-management-container {
  padding: 0;
}

.meeting-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.meeting-header h3 {
  margin: 0;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}

.status-tag {
  font-size: 12px;
}

.validation-section {
  margin-bottom: 24px;
}

.validation-section h4 {
  margin: 0 0 16px 0;
  color: #606266;
  font-size: 14px;
  font-weight: 500;
}

.validation-item {
  margin-bottom: 12px;
  padding: 12px;
  border-radius: 6px;
  background-color: #f5f7fa;
}

.status-success {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #67c23a;
}

.status-error {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #f56c6c;
}

.status-warning {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  color: #e6a23c;
}

.not-processing-members {
  margin-top: 8px;
  padding-left: 24px;
}

.not-processing-members h5 {
  margin: 4px 0;
  font-size: 12px;
  color: #909399;
}

.not-processing-members ul {
  margin: 4px 0;
  padding-left: 16px;
}

.not-processing-members li {
  font-size: 12px;
  color: #909399;
  margin-bottom: 2px;
}

.file-upload-section {
  margin-bottom: 24px;
}

.file-upload-section h4 {
  margin: 0 0 16px 0;
  color: #606266;
  font-size: 14px;
  font-weight: 500;
}

.current-document {
  margin-bottom: 16px;
  padding: 12px;
  background-color: #f0f9ff;
  border: 1px solid #b3d8ff;
  border-radius: 6px;
}

.document-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.document-icon {
  color: #409eff;
  font-size: 16px;
}

.file-selector {
  margin-bottom: 16px;
}

.selected-file-preview {
  margin-top: 16px;
  padding: 16px;
  background-color: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.file-icon {
  color: #409eff;
  font-size: 16px;
}

.file-details {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.file-name {
  font-weight: 500;
  color: #303133;
}

.file-size {
  font-size: 12px;
  color: #909399;
}

.file-preview-container {
  margin-top: 12px;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.preview-header h5 {
  margin: 0;
  color: #606266;
  font-size: 14px;
  font-weight: 500;
}

.upload-status {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #409eff;
  margin-top: 12px;
}

.upload-success {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #67c23a;
  margin-top: 12px;
}

.meeting-status-section {
  margin-bottom: 24px;
  padding: 16px;
  background-color: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
}

.meeting-status-section h4 {
  margin: 0 0 16px 0;
  color: #606266;
  font-size: 14px;
  font-weight: 500;
}

.status-info p {
  margin: 4px 0;
  color: #909399;
  font-size: 13px;
}

.status-info strong {
  color: #303133;
  font-weight: 500;
}

.meeting-content-section {
  margin-bottom: 24px;
}

.meeting-content-section h4 {
  margin: 0 0 16px 0;
  color: #606266;
  font-size: 14px;
  font-weight: 500;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.file-preview-dialog .text-preview {
  margin: 0;
  padding: 16px;
  background-color: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.6;
  color: #303133;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.unsupported-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
  text-align: center;
}

.unsupported-preview .el-icon {
  margin-bottom: 16px;
}

.unsupported-preview p {
  margin: 0 0 16px 0;
  font-size: 14px;
}
</style>
