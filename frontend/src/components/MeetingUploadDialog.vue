<template>
  <el-dialog
    title="Upload Meeting Document"
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    width="60%"
    class="meeting-upload-dialog"
  >
    <div class="upload-container">
      <div class="meeting-info">
        <h3>Meeting {{ meetingNo }} - Week {{ meetingNo }}</h3>
        <p class="task-title">{{ taskTitle }}</p>
      </div>

      <div class="validation-section" v-if="validationResult">
        <el-alert
          :title="validationResult.title"
          :type="validationResult.type"
          :description="validationResult.message"
          show-icon
          :closable="false"
        />
      </div>

      <div class="upload-section" v-if="canUpload">
        <el-upload
          ref="uploadRef"
          class="upload-dragger"
          :action="uploadAction"
          :headers="uploadHeaders"
          :data="uploadData"
          :before-upload="beforeUpload"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
          :file-list="fileList"
          :limit="1"
          drag
          accept=".pdf,.doc,.docx,.txt,.md"
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">
            Drop meeting document here or <em>click to upload</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              Supported formats: PDF, DOC, DOCX, TXT, MD (max 10MB)
            </div>
          </template>
        </el-upload>
      </div>

      <div class="goals-status-section" v-if="goalsStatus">
        <h4>Weekly Goals Status Check</h4>
        <div v-if="goalsStatus.allProcessing" class="status-success">
          <el-icon><check /></el-icon>
          <span
            >All team members have set their weekly goals to PROCESSING</span
          >
        </div>
        <div v-else class="status-warning">
          <el-icon><warning /></el-icon>
          <span>{{ goalsStatus.message }}</span>
          <div
            class="not-processing-members"
            v-if="goalsStatus.notProcessingMembers"
          >
            <h5>Members not ready:</h5>
            <ul>
              <li
                v-for="member in goalsStatus.notProcessingMembers"
                :key="member.id"
              >
                Student ID: {{ member.studentId }} - Status: {{ member.status }}
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="$emit('update:modelValue', false)">Cancel</el-button>
        <el-button
          type="primary"
          @click="handleSubmit"
          :disabled="!canSubmit"
          :loading="uploading"
        >
          Upload Meeting Document
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script>
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled, Check, Warning } from '@element-plus/icons-vue'
import studentAPI from '@/api/student'

export default {
  name: 'MeetingUploadDialog',
  components: {
    UploadFilled,
    Check,
    Warning
  },
  props: {
    modelValue: {
      type: Boolean,
      default: false
    },
    taskId: {
      type: Number,
      required: true
    },
    meetingNo: {
      type: Number,
      required: true
    },
    taskTitle: {
      type: String,
      default: ''
    }
  },
  emits: ['update:modelValue', 'upload-success'],
  setup(props, { emit }) {
    const uploadRef = ref()
    const fileList = ref([])
    const uploading = ref(false)
    const uploadedFileUrl = ref('')
    const validationResult = ref(null)
    const goalsStatus = ref(null)

    const uploadAction = '/api/upload'
    const uploadHeaders = {
      Authorization: `Bearer ${localStorage.getItem('authToken')}`
    }
    const uploadData = {}

    const canUpload = computed(() => {
      return validationResult.value && validationResult.value.type === 'success'
    })

    const canSubmit = computed(() => {
      return (
        canUpload.value &&
        uploadedFileUrl.value &&
        goalsStatus.value &&
        goalsStatus.value.allProcessing
      )
    })

    // Watch dialog open/close
    watch(
      () => props.modelValue,
      async visible => {
        if (visible) {
          await checkUploadValidation()
          await checkGoalsStatus()
        } else {
          resetDialog()
        }
      }
    )

    const checkUploadValidation = async () => {
      try {
        const response = await studentAPI.checkCanUploadMeeting(
          props.taskId,
          props.meetingNo
        )
        if (response.data && response.data.code === 1) {
          const result = response.data.data
          if (result.canUpload) {
            validationResult.value = {
              type: 'success',
              title: 'Ready to Upload',
              message: result.message
            }
          } else {
            validationResult.value = {
              type: 'error',
              title: 'Cannot Upload',
              message: result.message
            }
          }
        }
      } catch (error) {
        console.error('Error checking upload validation:', error)
        validationResult.value = {
          type: 'error',
          title: 'Validation Error',
          message: 'Failed to check upload permissions'
        }
      }
    }

    const checkGoalsStatus = async () => {
      try {
        const response = await studentAPI.checkGroupMembersWeeklyGoalsStatus(
          props.taskId,
          props.meetingNo
        )
        if (response.data && response.data.code === 1) {
          goalsStatus.value = response.data.data
        }
      } catch (error) {
        console.error('Error checking goals status:', error)
        goalsStatus.value = {
          allProcessing: false,
          message: 'Failed to check weekly goals status'
        }
      }
    }

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
        ElMessage.error('Please upload PDF, DOC, DOCX, TXT, or MD files only')
        return false
      }
      if (!isLt10M) {
        ElMessage.error('File size cannot exceed 10MB')
        return false
      }

      uploading.value = true
      return true
    }

    const handleUploadSuccess = response => {
      uploading.value = false
      if (response.code === 1) {
        uploadedFileUrl.value = response.data
        ElMessage.success('File uploaded successfully')
      } else {
        ElMessage.error('Upload failed: ' + response.msg)
      }
    }

    const handleUploadError = error => {
      uploading.value = false
      console.error('Upload error:', error)
      ElMessage.error('Upload failed')
    }

    const handleSubmit = async () => {
      if (!canSubmit.value) {
        ElMessage.warning(
          'Please ensure all requirements are met before submitting'
        )
        return
      }

      try {
        uploading.value = true
        const response = await studentAPI.uploadMeetingDocument({
          taskId: props.taskId,
          meetingNo: props.meetingNo,
          documentUrl: uploadedFileUrl.value
        })

        if (response.data && response.data.code === 1) {
          ElMessage.success('Meeting document uploaded successfully')
          emit('upload-success', response.data.data)
          emit('update:modelValue', false)
        } else {
          ElMessage.error(
            'Upload failed: ' + (response.data?.msg || 'Unknown error')
          )
        }
      } catch (error) {
        console.error('Error uploading meeting document:', error)
        ElMessage.error('Upload failed: ' + error.message)
      } finally {
        uploading.value = false
      }
    }

    const resetDialog = () => {
      fileList.value = []
      uploadedFileUrl.value = ''
      validationResult.value = null
      goalsStatus.value = null
      uploading.value = false
    }

    return {
      uploadRef,
      fileList,
      uploading,
      uploadAction,
      uploadHeaders,
      uploadData,
      validationResult,
      goalsStatus,
      canUpload,
      canSubmit,
      beforeUpload,
      handleUploadSuccess,
      handleUploadError,
      handleSubmit
    }
  }
}
</script>

<style scoped>
.meeting-upload-dialog {
  .el-dialog__body {
    padding: 20px 24px;
  }
}

.upload-container {
  padding: 0;
}

.meeting-info {
  margin-bottom: 20px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.meeting-info h3 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 18px;
}

.task-title {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.validation-section {
  margin-bottom: 20px;
}

.upload-section {
  margin-bottom: 20px;
}

.goals-status-section {
  margin-top: 20px;
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
}

.goals-status-section h4 {
  margin: 0 0 12px 0;
  color: #303133;
}

.status-success {
  display: flex;
  align-items: center;
  color: #67c23a;
  font-size: 14px;
}

.status-success .el-icon {
  margin-right: 8px;
}

.status-warning {
  color: #e6a23c;
  font-size: 14px;
}

.status-warning .el-icon {
  margin-right: 8px;
}

.not-processing-members {
  margin-top: 12px;
  padding: 12px;
  background: #fdf6ec;
  border-radius: 4px;
}

.not-processing-members h5 {
  margin: 0 0 8px 0;
  color: #e6a23c;
  font-size: 13px;
}

.not-processing-members ul {
  margin: 0;
  padding-left: 20px;
}

.not-processing-members li {
  margin-bottom: 4px;
  color: #909399;
  font-size: 12px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
