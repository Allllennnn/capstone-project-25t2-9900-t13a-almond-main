<template>
  <el-dialog
    :title="
      goalData?.status === 'FINISHED' ? 'View Weekly Goal' : 'Edit Weekly Goal'
    "
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    width="60%"
    class="goal-edit-dialog"
  >
    <div class="goal-edit-container">
      <div class="goal-header">
        <h3>Week {{ goalData?.weekNo }} Goal</h3>
        <el-tag :type="getStatusType(goalData?.status)" class="status-tag">
          {{ getStatusText(goalData?.status) }}
        </el-tag>
      </div>

      <div class="goal-content-section">
        <h4>Current Goal Content:</h4>
        <div class="current-goal-display">
          <pre class="goal-text">{{
            localGoal.goal || 'No goal content available'
          }}</pre>
        </div>
      </div>

      <el-form :model="localGoal" label-width="100px" class="goal-form">
        <el-form-item label="Edit Goal:">
          <el-input
            v-model="localGoal.goal"
            type="textarea"
            :rows="8"
            :placeholder="'Enter the goal for week ' + (goalData?.weekNo || '')"
            resize="vertical"
            :disabled="goalData?.status === 'FINISHED'"
          />
          <div class="auto-generate-section">
            <el-button
              type="primary"
              plain
              size="small"
              @click="generateGoal"
              :loading="generating"
              :disabled="goalData?.status === 'FINISHED'"
            >
              <el-icon><Magic /></el-icon>
              Auto Generate Goal
            </el-button>
            <span v-if="generating" class="generating-text"
              >AI is generating your goal...</span
            >
          </div>
        </el-form-item>
        <el-form-item label="Status:">
          <el-select
            v-model="localGoal.status"
            style="width: 200px"
            :disabled="goalData?.status === 'FINISHED'"
          >
            <el-option label="Not Started" value="NOTUPLOADED" />
            <el-option label="In Progress" value="PROCESSING" />
          </el-select>
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="$emit('update:modelValue', false)">
          {{ goalData?.status === 'FINISHED' ? 'Close' : 'Cancel' }}
        </el-button>
        <el-button
          v-if="goalData?.status !== 'FINISHED'"
          type="primary"
          @click="handleSave"
        >
          Save
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import studentAPI from '@/api/student'
import { Magic } from '@element-plus/icons-vue'

export default {
  name: 'WeeklyGoalEditDialog',
  components: {
    Magic
  },
  props: {
    modelValue: {
      type: Boolean,
      default: false
    },
    goalData: {
      type: Object,
      default: null
    },
    taskId: {
      type: [Number, String],
      required: true
    }
  },
  emits: ['update:modelValue', 'goal-updated'],
  setup(props, { emit }) {
    const localGoal = ref({
      goal: '',
      status: 'NOTUPLOADED'
    })

    const generating = ref(false)

    // 监听 goalData 变化
    watch(
      () => props.goalData,
      newData => {
        if (newData) {
          localGoal.value = {
            goal: newData.desc || '',
            status: newData.status || 'NOTUPLOADED'
          }
        }
      },
      { immediate: true }
    )

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
          return 'Unknown'
      }
    }

    const handleSave = async () => {
      try {
        const updateData = {
          id: props.goalData?.id,
          goal: localGoal.value.goal,
          status: localGoal.value.status
        }

        const response = await studentAPI.updateWeeklyGoal(updateData)
        if (response.data && response.data.code === 1) {
          ElMessage.success('Weekly goal updated successfully')
          emit('goal-updated', {
            ...props.goalData,
            desc: localGoal.value.goal,
            status: localGoal.value.status
          })
          emit('update:modelValue', false)
        } else {
          ElMessage.error(
            'Update failed: ' + (response.data?.msg || 'Unknown error')
          )
        }
      } catch (error) {
        console.error('Error updating weekly goal:', error)
        ElMessage.error('Update failed: ' + error.message)
      }
    }

    // 自动生成周目标
    const generateGoal = async () => {
      if (!props.taskId || !props.goalData?.weekNo) {
        ElMessage.warning('Missing task ID or week number')
        return
      }

      generating.value = true
      try {
        const response = await studentAPI.generateWeeklyGoal(
          props.taskId,
          props.goalData.weekNo
        )

        if (response.data && response.data.code === 1) {
          const generatedGoal = response.data.data
          localGoal.value.goal = generatedGoal.goal

          ElMessage.success('Weekly goal generated successfully')
        } else {
          ElMessage.error(
            'Generation failed: ' + (response.data?.msg || 'Unknown error')
          )
        }
      } catch (error) {
        console.error('Error generating weekly goal:', error)
        ElMessage.error('Generation failed: ' + error.message)
      } finally {
        generating.value = false
      }
    }

    return {
      localGoal,
      generating,
      getStatusType,
      getStatusText,
      handleSave,
      generateGoal
    }
  }
}
</script>

<style scoped>
.goal-edit-dialog {
  .el-dialog__body {
    padding: 20px 24px;
  }
}

.goal-edit-container {
  padding: 0;
}

.goal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.goal-header h3 {
  margin: 0;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}

.status-tag {
  font-size: 12px;
}

.goal-content-section {
  margin-bottom: 24px;
}

.goal-content-section h4 {
  margin: 0 0 12px 0;
  color: #606266;
  font-size: 14px;
  font-weight: 500;
}

.current-goal-display {
  background-color: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 16px;
  min-height: 120px;
}

.goal-text {
  margin: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen,
    Ubuntu, Cantarell, 'Helvetica Neue', sans-serif;
  font-size: 14px;
  line-height: 1.6;
  color: #303133;
  white-space: pre-wrap;
  word-wrap: break-word;
  text-align: left;
}

.goal-form {
  margin-top: 24px;
}

.goal-form .el-form-item {
  margin-bottom: 20px;
}

.goal-form .el-form-item__label {
  color: #606266;
  font-weight: 500;
}

.goal-form .el-textarea__inner {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen,
    Ubuntu, Cantarell, 'Helvetica Neue', sans-serif;
  line-height: 1.6;
  resize: vertical;
}

.auto-generate-section {
  margin-top: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.generating-text {
  color: #409eff;
  font-size: 14px;
  font-style: italic;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
