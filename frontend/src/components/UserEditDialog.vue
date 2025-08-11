<template>
  <el-dialog
    :title="isEdit ? 'Edit User' : 'Add User'"
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
      <el-form-item label="Username" prop="username">
        <el-input v-model="form.username" placeholder="Enter username" />
      </el-form-item>

      <el-form-item label="Password" prop="password" v-if="!isEdit">
        <el-input
          v-model="form.password"
          type="password"
          placeholder="Enter password"
          show-password
        />
      </el-form-item>

      <el-form-item label="Name" prop="name">
        <el-input v-model="form.name" placeholder="Enter full name" />
      </el-form-item>

      <el-form-item label="Email" prop="email">
        <el-input v-model="form.email" placeholder="Enter email" />
      </el-form-item>

      <el-form-item label="Phone" prop="phone">
        <el-input v-model="form.phone" placeholder="Enter phone number" />
      </el-form-item>

      <el-form-item label="Role" prop="role">
        <el-select
          v-model="form.role"
          placeholder="Select role"
          style="width: 100%"
        >
          <el-option label="Admin" value="ADMIN" />
          <el-option label="Teacher" value="TEACHER" />
          <el-option label="Student" value="STUDENT" />
        </el-select>
      </el-form-item>

      <el-form-item label="Status" prop="status">
        <el-select
          v-model="form.status"
          placeholder="Select status"
          style="width: 100%"
        >
          <el-option label="Active" value="ACTIVE" />
          <el-option label="Inactive" value="INACTIVE" />
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
  name: 'UserEditDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    isEdit: {
      type: Boolean,
      default: false
    },
    userData: {
      type: Object,
      default: () => ({})
    }
  },
  emits: ['update:visible', 'success'],
  setup(props, { emit }) {
    const formRef = ref()
    const loading = ref(false)

    const form = reactive({
      username: '',
      password: '',
      name: '',
      email: '',
      phone: '',
      role: 'STUDENT',
      status: 'ACTIVE'
    })

    const rules = {
      username: [
        { required: true, message: 'Please enter username', trigger: 'blur' },
        {
          min: 3,
          max: 20,
          message: 'Length should be 3 to 20 characters',
          trigger: 'blur'
        }
      ],
      password: [
        { required: true, message: 'Please enter password', trigger: 'blur' },
        {
          min: 6,
          message: 'Password should be at least 6 characters',
          trigger: 'blur'
        }
      ],
      name: [{ required: true, message: 'Please enter name', trigger: 'blur' }],
      email: [
        { required: true, message: 'Please enter email', trigger: 'blur' },
        { type: 'email', message: 'Please enter valid email', trigger: 'blur' }
      ],
      phone: [
        {
          required: true,
          message: 'Please enter phone number',
          trigger: 'blur'
        }
      ],
      role: [
        { required: true, message: 'Please select role', trigger: 'change' }
      ],
      status: [
        { required: true, message: 'Please select status', trigger: 'change' }
      ]
    }

    // Watch for userData changes to populate form
    watch(
      () => props.userData,
      newData => {
        if (newData && Object.keys(newData).length > 0) {
          Object.assign(form, {
            username: newData.username || '',
            password: '', // Don't populate password for edit
            name: newData.name || '',
            email: newData.email || '',
            phone: newData.phone || '',
            role: newData.role || 'STUDENT',
            status: newData.status || 'ACTIVE'
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
            username: '',
            password: '',
            name: '',
            email: '',
            phone: '',
            role: 'STUDENT',
            status: 'ACTIVE'
          })
        }
      }
    )

    const handleSubmit = async () => {
      if (!formRef.value) return

      try {
        await formRef.value.validate()
        loading.value = true

        const submitData = { ...form }

        // Remove password if it's empty during edit
        if (props.isEdit && !submitData.password) {
          delete submitData.password
        }

        let response
        if (props.isEdit) {
          response = await adminAPI.updateUser(submitData)
        } else {
          response = await adminAPI.createUser(submitData)
        }

        if (response.data.code === 1) {
          ElMessage.success(
            props.isEdit
              ? 'User updated successfully'
              : 'User created successfully'
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
