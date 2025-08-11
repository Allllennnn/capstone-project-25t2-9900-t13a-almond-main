<template>
  <el-dialog
    :title="isEdit ? 'Edit Group' : 'Add Group'"
    :model-value="visible"
    @update:model-value="$emit('update:visible', $event)"
    width="500px"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      @submit.prevent="handleSubmit"
    >
      <el-form-item label="Group Name" prop="name">
        <el-input v-model="form.name" placeholder="Enter group name" />
      </el-form-item>

      <el-form-item label="Description" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="Enter group description"
        />
      </el-form-item>

      <el-form-item label="Teacher" prop="teacherId">
        <el-select
          v-model="form.teacherId"
          placeholder="Select teacher"
          style="width: 100%"
          filterable
          clearable
        >
          <el-option
            v-for="teacher in teachers"
            :key="teacher.id"
            :label="teacher.name"
            :value="teacher.id"
          />
        </el-select>
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="$emit('update:visible', false)">Cancel</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="loading">
          {{ isEdit ? 'Update' : 'Create' }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { adminAPI } from '@/api/admin'

export default {
  name: 'GroupEditDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    isEdit: {
      type: Boolean,
      default: false
    },
    groupData: {
      type: Object,
      default: () => ({})
    }
  },
  emits: ['update:visible', 'success'],
  setup(props, { emit }) {
    const formRef = ref()
    const loading = ref(false)
    const teachers = ref([])

    const form = reactive({
      name: '',
      description: '',
      teacherId: null
    })

    const rules = {
      name: [
        { required: true, message: 'Please enter group name', trigger: 'blur' },
        {
          min: 2,
          max: 50,
          message: 'Length should be 2 to 50 characters',
          trigger: 'blur'
        }
      ],
      description: [
        {
          required: true,
          message: 'Please enter group description',
          trigger: 'blur'
        }
      ],
      teacherId: [
        {
          required: true,
          message: 'Please select a teacher',
          trigger: 'change'
        }
      ]
    }

    // Fetch teachers for dropdown
    const fetchTeachers = async () => {
      try {
        const response = await adminAPI.getTeachers({ pageSize: 1000 })
        if (response.data.code === 1) {
          teachers.value = response.data.data.rows || []
        }
      } catch (error) {
        console.error('Failed to fetch teachers:', error)
        ElMessage.error('Failed to load teachers')
      }
    }

    // Watch for groupData changes to populate form
    watch(
      () => props.groupData,
      newData => {
        if (newData && Object.keys(newData).length > 0) {
          Object.assign(form, {
            name: newData.name || '',
            description: newData.description || '',
            teacherId: newData.teacherId || null
          })
        }
      },
      { immediate: true }
    )

    // Reset form when dialog opens/closes
    watch(
      () => props.visible,
      visible => {
        if (!visible) {
          formRef.value?.resetFields()
          Object.assign(form, {
            name: '',
            description: '',
            teacherId: null
          })
        } else {
          // Load teachers when dialog opens
          fetchTeachers()
        }
      }
    )

    const handleSubmit = async () => {
      if (!formRef.value) return

      try {
        await formRef.value.validate()
        loading.value = true

        const submitData = { ...form }

        let response
        if (props.isEdit) {
          response = await adminAPI.updateGroup(submitData)
        } else {
          response = await adminAPI.createGroup(submitData)
        }

        if (response.data.code === 1) {
          ElMessage.success(
            props.isEdit
              ? 'Group updated successfully'
              : 'Group created successfully'
          )
          emit('success', response.data.data || submitData)
          emit('update:visible', false)
        } else {
          ElMessage.error(response.data.msg || 'Operation failed')
        }
      } catch (error) {
        console.error('API call failed:', error)
        ElMessage.error(error.response?.data?.msg || 'Operation failed')
      } finally {
        loading.value = false
      }
    }

    return {
      formRef,
      form,
      rules,
      loading,
      teachers,
      handleSubmit
    }
  }
}
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
