<template>
  <div class="admin-dashboard">
    <el-container>
      <!-- Sidebar -->
      <el-aside width="250px" class="sidebar">
        <div class="sidebar-header">
          <h2>Admin Panel</h2>
          <!-- <h1>dfahf;d</h1> -->
        </div>
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          @select="handleMenuSelect"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409eff"
        >
          <el-menu-item index="dashboard">
            <el-icon><DataBoard /></el-icon>
            <span>Dashboard</span>
          </el-menu-item>
          <el-menu-item index="students">
            <el-icon><Avatar /></el-icon>
            <span>Students</span>
          </el-menu-item>
          <el-menu-item index="teachers">
            <el-icon><UserFilled /></el-icon>
            <span>Instructor</span>
          </el-menu-item>
          <el-menu-item index="groups">
            <el-icon><Collection /></el-icon>
            <span>Groups</span>
          </el-menu-item>
          <!-- <el-menu-item index="tasks">
            <el-icon><Document /></el-icon>
            <span>Tasks</span>
          </el-menu-item>
          <el-menu-item index="reports">
            <el-icon><PieChart /></el-icon>
            <span>Reports</span>
          </el-menu-item>
          <el-menu-item index="settings">
            <el-icon><Setting /></el-icon>
            <span>Settings</span>
          </el-menu-item> -->
        </el-menu>
      </el-aside>

      <!-- Main content -->
      <el-container>
        <!-- Header -->
        <el-header class="main-header">
          <div class="header-left">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item>Admin</el-breadcrumb-item>
              <el-breadcrumb-item>{{ currentPageTitle }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="header-right">
            <el-dropdown @command="handleUserCommand">
              <span class="user-dropdown">
                <el-avatar :size="32" :src="authStore.avatar">
                  {{ authStore.displayName?.charAt(0) }}
                </el-avatar>
                <span class="username">{{ authStore.displayName }}</span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="settings">
                    Settings
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    Logout
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <!-- Main content area -->
        <el-main class="main-content">
          <!-- Dashboard Overview -->
          <div v-if="activeMenu === 'dashboard'" class="dashboard-overview">
            <!-- Statistics Cards -->
            <el-row :gutter="20" class="stats-row">
              <el-col :span="8" v-for="stat in statistics" :key="stat.title">
                <el-card
                  class="stat-card"
                  shadow="hover"
                  @click="handleStatCardClick(stat.menuIndex)"
                >
                  <div class="stat-content">
                    <div
                      class="stat-icon"
                      :style="{ backgroundColor: stat.color }"
                    >
                      <el-icon :size="24">
                        <component :is="stat.icon" />
                      </el-icon>
                    </div>
                    <div class="stat-info">
                      <h3>{{ stat.title }}</h3>
                      <p class="stat-number">{{ stat.value }}</p>
                      <p class="stat-change" :class="stat.trend">
                        <el-icon>
                          <component
                            :is="stat.trend === 'up' ? 'ArrowUp' : 'ArrowDown'"
                          />
                        </el-icon>
                        {{ stat.change }}%
                      </p>
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>

            <!-- Charts and Tables -->
            <el-row :gutter="20" class="charts-row">
              <el-col :span="12">
                <el-card class="chart-card">
                  <template #header>
                    <div class="card-header">
                      <span>User Registration Trend</span>
                    </div>
                  </template>
                  <div
                    ref="registrationTrendChart"
                    style="width: 100%; height: 350px"
                  ></div>
                </el-card>
              </el-col>
              <el-col :span="12">
                <el-card class="chart-card">
                  <template #header>
                    <div class="card-header">
                      <span>Recent Activities</span>
                      <el-button
                        type="primary"
                        size="small"
                        @click="refreshActivities"
                        :loading="activitiesLoading"
                      >
                        <el-icon><Refresh /></el-icon>
                        Refresh
                      </el-button>
                    </div>
                  </template>
                  <div class="activities-container">
                    <div
                      ref="activitiesScrollContainer"
                      class="activities-scroll-container"
                      @scroll="handleActivitiesScroll"
                    >
                      <div v-loading="activitiesLoading">
                        <template v-if="recentActivities.length === 0">
                          <el-empty description="No recent activities" />
                        </template>
                        <template v-else>
                          <el-timeline>
                            <el-timeline-item
                              v-for="(activity, index) in recentActivities"
                              :key="`${activity.time}-${index}`"
                              :timestamp="activity.time"
                              :type="activity.activity_type"
                              :color="activity.color"
                              @click="handleActivityClick(activity.menu_index)"
                              class="activity-item"
                            >
                              <h4>{{ activity.title }}</h4>
                              <p>{{ activity.description }}</p>
                            </el-timeline-item>
                          </el-timeline>
                          <!-- Loading more indicator -->
                          <div v-if="loadingMore" class="loading-more">
                            <el-icon class="is-loading"><Loading /></el-icon>
                            <span>Loading more activities...</span>
                          </div>
                          <!-- No more data indicator -->
                          <div
                            v-if="noMoreData && recentActivities.length > 0"
                            class="no-more-data"
                          >
                            <span>No more activities</span>
                          </div>
                        </template>
                      </div>
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>
            <!-- Teacher Registration Requests -->
            <el-row :gutter="20" class="teacher-requests-row">
              <el-col :span="24">
                <el-card>
                  <template #header>
                    <div class="card-header">
                      <span>Teacher Registration Requests</span>
                    </div>
                  </template>
                  <div class="teacher-requests-list">
                    <template v-if="pendingTeachers.length === 0">
                      <el-empty description="No pending requests" />
                    </template>
                    <template v-else>
                      <el-row :gutter="24">
                        <el-col
                          v-for="teacher in pendingTeachers"
                          :key="teacher.id"
                          :xs="24"
                          :sm="12"
                          :md="8"
                          :lg="6"
                          class="teacher-request-item"
                        >
                          <el-card shadow="hover" class="request-card">
                            <div class="teacher-info">
                              <div class="teacher-name">{{ teacher.name }}</div>
                              <div class="teacher-email">
                                {{ teacher.email }}
                              </div>
                            </div>
                            <div class="request-actions">
                              <el-button
                                type="success"
                                @click="approveTeacher(teacher)"
                                :loading="teacher.loadingApprove"
                                plain
                              >
                                Approve
                              </el-button>
                              <el-button
                                type="danger"
                                @click="rejectTeacher(teacher)"
                                :loading="teacher.loadingReject"
                                plain
                              >
                                Reject
                              </el-button>
                            </div>
                          </el-card>
                        </el-col>
                      </el-row>
                    </template>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>
          <!-- Students Management -->
          <div
            v-else-if="activeMenu === 'students'"
            class="students-management"
          >
            <el-card>
              <template #header>
                <div class="card-header">
                  <span>Students Management</span>
                  <div class="header-buttons">
                    <el-button type="success" @click="showImportStudentsDialog">
                      <el-icon><UploadFilled /></el-icon>
                      Import Excel
                    </el-button>
                    <el-button type="primary" @click="showAddStudentDialog">
                      <el-icon><Plus /></el-icon>
                      Add Student
                    </el-button>
                  </div>
                </div>
              </template>

              <!-- Search filters -->
              <div class="search-filters">
                <el-row :gutter="16">
                  <el-col :span="8">
                    <el-input
                      v-model="searchFilters.students.name"
                      placeholder="Search by name"
                      clearable
                      @keyup.enter="fetchStudents"
                    >
                      <template #append>
                        <el-button @click="fetchStudents">
                          <el-icon><Search /></el-icon>
                        </el-button>
                      </template>
                    </el-input>
                  </el-col>
                  <el-col :span="8">
                    <el-input
                      v-model="searchFilters.students.id"
                      placeholder="Search by ID"
                      clearable
                      @keyup.enter="fetchStudents"
                    />
                  </el-col>
                </el-row>
              </div>

              <el-table
                :data="Array.isArray(students) ? students : []"
                style="width: 100%"
                v-loading="loading.students"
                row-key="id"
              >
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="name" label="Name" />
                <el-table-column prop="email" label="Email" />
                <el-table-column prop="phone" label="Phone" />
                <el-table-column prop="status" label="Status">
                  <template #default="scope">
                    <el-select
                      v-model="scope.row.status"
                      placeholder="Select status"
                      size="small"
                      @change="handleStudentStatusChange(scope.row)"
                      style="width: 100px"
                    >
                      <el-option label="Active" value="ACTIVE" />
                      <el-option label="Inactive" value="INACTIVE" />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="Actions" width="200">
                  <template #default="scope">
                    <el-button size="small" @click="editStudent(scope.row)">
                      Edit
                    </el-button>
                    <el-button
                      size="small"
                      type="danger"
                      @click="deleteStudent(scope.row)"
                    >
                      Delete
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>

              <!-- Pagination -->
              <div class="pagination-wrapper">
                <el-pagination
                  :current-page="pagination.students.page"
                  :page-size="pagination.students.pageSize"
                  :page-sizes="[10, 20, 50, 100]"
                  :total="pagination.students.total"
                  layout="total, sizes, prev, pager, next, jumper"
                  @size-change="handleStudentSizeChange"
                  @current-change="handleStudentPageChange"
                />
              </div>
            </el-card>
          </div>

          <!-- Teachers Management -->
          <div
            v-else-if="activeMenu === 'teachers'"
            class="teachers-management"
          >
            <!-- Active Teachers -->
            <el-card class="teacher-card">
              <template #header>
                <div class="card-header">
                  <span>Active Instructor</span>
                  <div class="header-buttons">
                    <el-button type="success" @click="showImportTeachersDialog">
                      <el-icon><UploadFilled /></el-icon>
                      Import Excel
                    </el-button>
                    <el-button type="primary" @click="showAddTeacherDialog">
                      <el-icon><Plus /></el-icon>
                      Add Instructor
                    </el-button>
                  </div>
                </div>
              </template>

              <!-- Search filters for active teachers -->
              <div class="search-filters">
                <el-row :gutter="16">
                  <el-col :span="8">
                    <el-input
                      v-model="searchFilters.teachers.name"
                      placeholder="Search by name"
                      clearable
                      @keyup.enter="fetchTeachers"
                    >
                      <template #append>
                        <el-button @click="fetchTeachers">
                          <el-icon><Search /></el-icon>
                        </el-button>
                      </template>
                    </el-input>
                  </el-col>
                  <el-col :span="8">
                    <el-input
                      v-model="searchFilters.teachers.id"
                      placeholder="Search by ID"
                      clearable
                      @keyup.enter="fetchTeachers"
                    />
                  </el-col>
                </el-row>
              </div>

              <el-table
                :data="
                  Array.isArray(teachers)
                    ? teachers.filter(t => t.status === 'ACTIVE')
                    : []
                "
                style="width: 100%"
                v-loading="loading.teachers"
              >
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="name" label="Name" />
                <el-table-column prop="email" label="Email" />
                <el-table-column prop="phone" label="Phone" />
                <el-table-column prop="status" label="Status" width="100">
                  <template #default>
                    <el-tag type="success" size="small">Active</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="Actions" width="200">
                  <template #default="scope">
                    <el-button size="small" @click="editTeacher(scope.row)">
                      Edit
                    </el-button>
                    <el-button
                      size="small"
                      type="danger"
                      @click="deleteTeacher(scope.row)"
                    >
                      Delete
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>

              <!-- Pagination for active teachers -->
              <div class="pagination-wrapper">
                <el-pagination
                  :current-page="pagination.teachers.page"
                  :page-size="pagination.teachers.pageSize"
                  :page-sizes="[10, 20, 50, 100]"
                  :total="pagination.teachers.total"
                  layout="total, sizes, prev, pager, next, jumper"
                  @size-change="handleTeacherSizeChange"
                  @current-change="handleTeacherPageChange"
                />
              </div>
            </el-card>

            <!-- Pending Teachers -->
            <el-card class="teacher-card" style="margin-top: 20px">
              <template #header>
                <div class="card-header">
                  <span>Pending Instructor (Awaiting Approval)</span>
                  <el-button type="primary" @click="fetchPendingTeachers">
                    <el-icon><Refresh /></el-icon>
                    Refresh
                  </el-button>
                </div>
              </template>

              <el-table
                :data="Array.isArray(pendingTeachers) ? pendingTeachers : []"
                style="width: 100%"
                v-loading="loading.pendingTeachers"
              >
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="name" label="Name" />
                <el-table-column prop="email" label="Email" />
                <el-table-column prop="phone" label="Phone" />
                <el-table-column prop="status" label="Status" width="100">
                  <template #default>
                    <el-tag type="warning" size="small">Pending</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="Actions" width="200">
                  <template #default="scope">
                    <el-button
                      size="small"
                      type="success"
                      @click="approveTeacher(scope.row)"
                      :loading="scope.row.loadingApprove"
                    >
                      Approve
                    </el-button>
                    <el-button
                      size="small"
                      type="danger"
                      @click="rejectTeacher(scope.row)"
                      :loading="scope.row.loadingReject"
                    >
                      Reject
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>

              <div v-if="pendingTeachers.length === 0" class="empty-state">
                <p>No pending teachers</p>
              </div>
            </el-card>
          </div>

          <!-- Groups Management -->
          <div v-else-if="activeMenu === 'groups'" class="groups-management">
            <el-card>
              <template #header>
                <div class="card-header">
                  <span>Groups Management</span>
                  <el-button type="primary" @click="showAddGroupDialog">
                    <el-icon><Plus /></el-icon>
                    Add Group
                  </el-button>
                </div>
              </template>

              <!-- Search filters -->
              <div class="search-filters">
                <el-row :gutter="16">
                  <el-col :span="8">
                    <el-input
                      v-model="searchFilters.groups.name"
                      placeholder="Search by group name"
                      clearable
                      @keyup.enter="fetchGroups"
                    >
                      <template #append>
                        <el-button @click="fetchGroups">
                          <el-icon><Search /></el-icon>
                        </el-button>
                      </template>
                    </el-input>
                  </el-col>
                </el-row>
              </div>

              <el-table
                :data="groups"
                style="width: 100%"
                v-loading="loading.groups"
              >
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="name" label="Group Name" />
                <el-table-column prop="description" label="Description" />
                <el-table-column prop="teacherId" label="Teacher" width="150">
                  <template #default="scope">
                    <el-select
                      v-model="scope.row.teacherId"
                      placeholder="Select teacher"
                      size="small"
                      @change="handleGroupTeacherChange(scope.row)"
                      style="width: 120px"
                      filterable
                    >
                      <el-option
                        v-for="teacher in teachers"
                        :key="teacher.id"
                        :label="teacher.name"
                        :value="teacher.id"
                      />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column
                  prop="createdAt"
                  label="Created At"
                  width="180"
                >
                  <template #default="scope">
                    {{ new Date(scope.row.createdAt).toLocaleString() }}
                  </template>
                </el-table-column>
                <el-table-column label="Actions" width="200">
                  <template #default="scope">
                    <el-button size="small" @click="editGroup(scope.row)">
                      Edit
                    </el-button>
                    <el-button
                      size="small"
                      type="danger"
                      @click="deleteGroup(scope.row)"
                    >
                      Delete
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>

              <!-- Pagination -->
              <div class="pagination-wrapper">
                <el-pagination
                  :current-page="pagination.groups.page"
                  :page-size="pagination.groups.pageSize"
                  :page-sizes="[10, 20, 50, 100]"
                  :total="pagination.groups.total"
                  layout="total, sizes, prev, pager, next, jumper"
                  @size-change="handleGroupSizeChange"
                  @current-change="handleGroupPageChange"
                />
              </div>
            </el-card>
          </div>
        </el-main>
      </el-container>
    </el-container>

    <!-- Edit Dialogs -->
    <TeacherEditDialog
      :visible="teacherDialogVisible"
      :is-edit="teacherDialogIsEdit"
      :teacher-data="currentTeacherData"
      @update:visible="teacherDialogVisible = $event"
      @success="handleTeacherSuccess"
    />

    <StudentEditDialog
      :visible="studentDialogVisible"
      :is-edit="studentDialogIsEdit"
      :student-data="currentStudentData"
      @update:visible="studentDialogVisible = $event"
      @success="handleStudentSuccess"
    />

    <GroupEditDialog
      :visible="groupDialogVisible"
      :is-edit="groupDialogIsEdit"
      :group-data="currentGroupData"
      @update:visible="groupDialogVisible = $event"
      @success="handleGroupSuccess"
    />

    <!-- 批量导入学生对话框 -->
    <el-dialog
      v-model="importStudentsDialogVisible"
      title="Import Students from Excel"
      width="500px"
      @closed="resetStudentUpload"
    >
      <div class="import-dialog-content">
        <el-upload
          ref="studentUploadRef"
          class="upload-demo"
          drag
          action="#"
          :auto-upload="false"
          :on-change="handleStudentFileChange"
          :limit="1"
          accept=".xlsx,.xls"
        >
          <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
          <div class="el-upload__text">
            Drop Excel file here or <em>click to upload</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              Only .xlsx/.xls files are supported. The first row should be
              headers.
              <br />
              Required columns: Username, Password, Name, Email, Phone, Student
              Number
            </div>
          </template>
        </el-upload>

        <div v-if="selectedStudentFile" class="selected-file">
          <span>Selected file: {{ selectedStudentFile.name }}</span>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button
            @click="
              () => {
                importStudentsDialogVisible = false
                resetStudentUpload()
              }
            "
          >
            Cancel
          </el-button>
          <el-button
            type="primary"
            @click="importStudents"
            :loading="importing.students"
            :disabled="!selectedStudentFile"
          >
            Import
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 批量导入教师对话框 -->
    <el-dialog
      v-model="importTeachersDialogVisible"
      title="Import Teachers from Excel"
      width="500px"
      @closed="resetTeacherUpload"
    >
      <div class="import-dialog-content">
        <el-upload
          ref="teacherUploadRef"
          class="upload-demo"
          drag
          action="#"
          :auto-upload="false"
          :on-change="handleTeacherFileChange"
          :limit="1"
          accept=".xlsx,.xls"
        >
          <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
          <div class="el-upload__text">
            Drop Excel file here or <em>click to upload</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              Only .xlsx/.xls files are supported. The first row should be
              headers.
              <br />
              Required columns: Username, Password, Name, Email, Phone
            </div>
          </template>
        </el-upload>

        <div v-if="selectedTeacherFile" class="selected-file">
          <span>Selected file: {{ selectedTeacherFile.name }}</span>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button
            @click="
              () => {
                importTeachersDialogVisible = false
                resetTeacherUpload()
              }
            "
          >
            Cancel
          </el-button>
          <el-button
            type="primary"
            @click="importTeachers"
            :loading="importing.teachers"
            :disabled="!selectedTeacherFile"
          >
            Import
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useAuthStore } from '@/store/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  DataBoard,
  Avatar,
  UserFilled,
  Collection,
  Document,
  PieChart,
  Setting,
  Plus,
  ArrowDown,
  TrendCharts,
  ArrowUp,
  Search,
  Refresh,
  Loading,
  UploadFilled
} from '@element-plus/icons-vue'
import UserEditDialog from '@/components/UserEditDialog.vue'
import StudentEditDialog from '@/components/StudentEditDialog.vue'
import TeacherEditDialog from '@/components/TeacherEditDialog.vue'
import GroupEditDialog from '@/components/GroupEditDialog.vue'
import { adminAPI } from '@/api/admin'
import * as echarts from 'echarts'

export default {
  name: 'AdminDashboard',
  components: {
    UserEditDialog,
    StudentEditDialog,
    TeacherEditDialog,
    GroupEditDialog,
    DataBoard,
    Avatar,
    UserFilled,
    Collection,
    Document,
    PieChart,
    Setting,
    Plus,
    ArrowDown,
    TrendCharts,
    ArrowUp,
    Search,
    Refresh,
    Loading,
    UploadFilled
  },
  setup() {
    const authStore = useAuthStore()
    const activeMenu = ref('dashboard')

    // Loading states
    const loading = ref({
      pendingTeachers: false,
      students: false,
      teachers: false,
      groups: false
    })

    // 导入状态
    const importing = ref({
      students: false,
      teachers: false
    })

    // 导入对话框状态
    const importStudentsDialogVisible = ref(false)
    const importTeachersDialogVisible = ref(false)
    const selectedStudentFile = ref(null)
    const selectedTeacherFile = ref(null)

    // 上传组件引用
    const studentUploadRef = ref(null)
    const teacherUploadRef = ref(null)

    // 重置上传组件
    const resetStudentUpload = () => {
      selectedStudentFile.value = null
      if (studentUploadRef.value) {
        studentUploadRef.value.clearFiles()
      }
    }

    const resetTeacherUpload = () => {
      selectedTeacherFile.value = null
      if (teacherUploadRef.value) {
        teacherUploadRef.value.clearFiles()
      }
    }

    // 显示导入学生对话框
    const showImportStudentsDialog = () => {
      resetStudentUpload()
      importStudentsDialogVisible.value = true
    }

    // 显示导入教师对话框
    const showImportTeachersDialog = () => {
      resetTeacherUpload()
      importTeachersDialogVisible.value = true
    }

    // 处理学生文件选择
    const handleStudentFileChange = file => {
      selectedStudentFile.value = file.raw
    }

    // 处理教师文件选择
    const handleTeacherFileChange = file => {
      selectedTeacherFile.value = file.raw
    }

    // 导入学生
    const importStudents = async () => {
      if (!selectedStudentFile.value) {
        ElMessage.warning('Please select an Excel file first')
        return
      }

      importing.value.students = true
      try {
        const response = await adminAPI.batchImportStudents(
          selectedStudentFile.value
        )

        if (response.data && response.data.code === 1) {
          const result = response.data.data
          ElMessage.success(
            `Successfully imported ${result.success} out of ${result.total} students`
          )

          if (result.failed > 0) {
            // 显示错误详情
            ElMessageBox.alert(
              `<div style="max-height: 300px; overflow-y: auto;">
                <p>Failed to import ${
                  result.failed
                } students due to the following errors:</p>
                <ul>${result.errors.map(err => `<li>${err}</li>`).join('')}</ul>
              </div>`,
              'Import Errors',
              {
                dangerouslyUseHTMLString: true,
                confirmButtonText: 'OK'
              }
            )
          }

          // 刷新学生列表
          fetchStudents()
          importStudentsDialogVisible.value = false
          resetStudentUpload()
        } else {
          ElMessage.error(response.data?.msg || 'Failed to import students')
        }
      } catch (error) {
        console.error('Error importing students:', error)
        ElMessage.error('Failed to import students: ' + error.message)
      } finally {
        importing.value.students = false
      }
    }

    // 导入教师
    const importTeachers = async () => {
      if (!selectedTeacherFile.value) {
        ElMessage.warning('Please select an Excel file first')
        return
      }

      importing.value.teachers = true
      try {
        const response = await adminAPI.batchImportTeachers(
          selectedTeacherFile.value
        )

        if (response.data && response.data.code === 1) {
          const result = response.data.data
          ElMessage.success(
            `Successfully imported ${result.success} out of ${result.total} teachers`
          )

          if (result.failed > 0) {
            // 显示错误详情
            ElMessageBox.alert(
              `<div style="max-height: 300px; overflow-y: auto;">
                <p>Failed to import ${
                  result.failed
                } teachers due to the following errors:</p>
                <ul>${result.errors.map(err => `<li>${err}</li>`).join('')}</ul>
              </div>`,
              'Import Errors',
              {
                dangerouslyUseHTMLString: true,
                confirmButtonText: 'OK'
              }
            )
          }

          // 刷新教师列表
          fetchTeachers()
          importTeachersDialogVisible.value = false
          resetTeacherUpload()
        } else {
          ElMessage.error(response.data?.msg || 'Failed to import teachers')
        }
      } catch (error) {
        console.error('Error importing teachers:', error)
        ElMessage.error('Failed to import teachers: ' + error.message)
      } finally {
        importing.value.teachers = false
      }
    }

    // Data refs
    const pendingTeachers = ref([])
    const students = ref([])
    const teachers = ref([])
    const groups = ref([])
    const users = ref([])

    // Pagination
    const pagination = ref({
      students: { page: 1, pageSize: 10, total: 0 },
      teachers: { page: 1, pageSize: 10, total: 0 },
      groups: { page: 1, pageSize: 10, total: 0 }
    })

    // Search filters
    const searchFilters = ref({
      students: { name: '', id: null },
      teachers: { name: '', id: null },
      groups: { name: '' }
    })

    // Statistics data
    const statistics = ref([
      {
        title: 'Total Students',
        value: 0,
        change: 12,
        trend: 'up',
        color: '#409eff',
        icon: 'Avatar',
        menuIndex: 'students'
      },
      {
        title: 'Total Teachers',
        value: 0,
        change: 8,
        trend: 'up',
        color: '#67c23a',
        icon: 'UserFilled',
        menuIndex: 'teachers'
      },
      {
        title: 'Active Groups',
        value: 0,
        change: 5,
        trend: 'up',
        color: '#e6a23c',
        icon: 'Collection',
        menuIndex: 'groups'
      }
    ])

    // Recent activities
    const recentActivities = ref([])
    const activitiesPagination = ref({ page: 1, pageSize: 10, total: 0 })
    const activitiesLoading = ref(false)
    const loadingMore = ref(false)
    const noMoreData = ref(false)

    // Dialog states
    const userDialogVisible = ref(false)
    const userDialogIsEdit = ref(false)
    const currentUserData = ref({})

    const studentDialogVisible = ref(false)
    const studentDialogIsEdit = ref(false)
    const currentStudentData = ref({})

    const teacherDialogVisible = ref(false)
    const teacherDialogIsEdit = ref(false)
    const currentTeacherData = ref({})

    const groupDialogVisible = ref(false)
    const groupDialogIsEdit = ref(false)
    const currentGroupData = ref({})

    // ECharts注册趋势图相关
    const registrationTrendChart = ref(null)
    const registrationTrendData = ref({ dates: [], counts: [] })

    // Scroll container
    const activitiesScrollContainer = ref(null)

    // API methods
    const fetchPendingTeachers = async () => {
      loading.value.pendingTeachers = true
      try {
        const response = await adminAPI.getPendingTeachers()
        if (response.data.code === 1) {
          pendingTeachers.value = response.data.data.map(teacher => ({
            ...teacher,
            loadingApprove: false,
            loadingReject: false
          }))
        }
      } catch (error) {
        ElMessage.error('Failed to fetch pending teachers')
        console.error('Error fetching pending teachers:', error)
      } finally {
        loading.value.pendingTeachers = false
      }
    }

    const fetchStudents = async () => {
      loading.value.students = true
      try {
        console.log('开始获取学生数据...')
        const params = {
          page: pagination.value.students.page,
          pageSize: pagination.value.students.pageSize,
          ...searchFilters.value.students
        }
        console.log('请求参数:', params)
        console.log('当前分页状态:', pagination.value.students)
        const response = await adminAPI.getStudents(params)
        console.log('API原始响应：', response.data)
        console.log('请求URL:', response.config?.url)
        console.log('请求参数:', response.config?.params)
        console.log(
          'rows类型：',
          typeof response.data.data?.rows,
          Array.isArray(response.data.data?.rows)
        )
        console.log('赋值前 students:', students.value)
        students.value = Array.isArray(response.data.data?.rows)
          ? response.data.data.rows
          : []
        console.log('赋值后 students:', students.value)
        if (
          response.data.code === 1 &&
          Array.isArray(response.data.data?.rows)
        ) {
          pagination.value.students.total = response.data.data.total
          // Update statistics
          statistics.value[0].value = response.data.data.total
          console.log('学生数据更新成功:', students.value)
        } else if (response.data.code !== 1) {
          console.error('获取学生数据失败:', response.data.msg)
          ElMessage.error(response.data.msg || 'Failed to fetch students')
        }
      } catch (error) {
        students.value = []
        console.error('获取学生数据异常:', error)
        console.error('错误详情:', error.response?.data)
        ElMessage.error('Failed to fetch students')
      } finally {
        loading.value.students = false
      }
    }

    const fetchTeachers = async () => {
      loading.value.teachers = true
      try {
        console.log('开始获取教师数据...')
        const params = {
          page: pagination.value.teachers.page,
          pageSize: pagination.value.teachers.pageSize,
          ...searchFilters.value.teachers
        }
        console.log('请求参数:', params)
        console.log('当前分页状态:', pagination.value.teachers)
        const response = await adminAPI.getTeachers(params)
        console.log('API原始响应：', response.data)
        console.log('请求URL:', response.config?.url)
        console.log('请求参数:', response.config?.params)
        console.log(
          'rows类型：',
          typeof response.data.data?.rows,
          Array.isArray(response.data.data?.rows)
        )
        console.log('赋值前 teachers:', teachers.value)
        teachers.value = Array.isArray(response.data.data?.rows)
          ? response.data.data.rows
          : []
        console.log('赋值后 teachers:', teachers.value)
        if (
          response.data.code === 1 &&
          Array.isArray(response.data.data?.rows)
        ) {
          pagination.value.teachers.total = response.data.data.total
          // Update statistics
          statistics.value[1].value = response.data.data.total
          console.log('教师数据更新成功:', teachers.value)
        } else if (response.data.code !== 1) {
          console.error('获取教师数据失败:', response.data.msg)
          ElMessage.error(response.data.msg || 'Failed to fetch teachers')
        }
      } catch (error) {
        teachers.value = []
        console.error('获取教师数据异常:', error)
        console.error('错误详情:', error.response?.data)
        ElMessage.error('Failed to fetch teachers')
      } finally {
        loading.value.teachers = false
      }
    }

    const fetchGroups = async () => {
      loading.value.groups = true
      try {
        console.log('开始获取小组数据...')
        const params = {
          page: pagination.value.groups.page,
          pageSize: pagination.value.groups.pageSize,
          ...searchFilters.value.groups
        }
        console.log('请求参数:', params)
        const response = await adminAPI.getGroups(params)
        console.log('小组数据响应:', response)
        if (response.data.code === 1) {
          groups.value = response.data.data.rows
          pagination.value.groups.total = response.data.data.total
          // Update statistics
          statistics.value[2].value = response.data.data.total
          console.log('小组数据更新成功:', groups.value)
        } else {
          console.error('获取小组数据失败:', response.data.msg)
          ElMessage.error(response.data.msg || 'Failed to fetch groups')
        }
      } catch (error) {
        console.error('获取小组数据异常:', error)
        console.error('错误详情:', error.response?.data)
        ElMessage.error('Failed to fetch groups')
      } finally {
        loading.value.groups = false
      }
    }

    // 获取注册趋势数据
    const fetchRegistrationTrend = async () => {
      try {
        const response = await adminAPI.getRegistrationTrend()
        console.log('[ECharts] 后端响应：', response.data)
        if (response.data.code === 1 && Array.isArray(response.data.data)) {
          const dates = response.data.data.map(item => item.date)
          const counts = response.data.data.map(item => parseInt(item.count))
          console.log('[ECharts] dates:', dates)
          console.log('[ECharts] counts:', counts)
          registrationTrendData.value = { dates, counts }
          // 渲染ECharts
          nextTick(() => {
            renderRegistrationTrendChart()
          })
        } else {
          registrationTrendData.value = { dates: [], counts: [] }
          console.error('[ECharts] 数据格式异常:', response.data)
          // 渲染空图表
          nextTick(() => {
            renderRegistrationTrendChart()
          })
        }
      } catch (error) {
        registrationTrendData.value = { dates: [], counts: [] }
        console.error('[ECharts] 获取注册趋势数据异常:', error)
        // 渲染空图表
        nextTick(() => {
          renderRegistrationTrendChart()
        })
      }
    }

    // 获取最近活动数据
    const fetchRecentActivities = async (isLoadMore = false) => {
      if (!isLoadMore) {
        activitiesLoading.value = true
        activitiesPagination.value.page = 1
        recentActivities.value = []
        noMoreData.value = false
      } else {
        loadingMore.value = true
      }

      try {
        const params = {
          page: activitiesPagination.value.page,
          pageSize: activitiesPagination.value.pageSize
        }
        const response = await adminAPI.getRecentActivities(params)
        console.log('[Activities] 后端响应：', response.data)
        if (response.data.code === 1 && Array.isArray(response.data.data)) {
          const newActivities = response.data.data.map(activity => ({
            ...activity,
            time: new Date(activity.time).toLocaleString()
          }))

          if (isLoadMore) {
            // 追加数据
            recentActivities.value.push(...newActivities)
          } else {
            // 替换数据
            recentActivities.value = newActivities
          }

          // 检查是否还有更多数据
          if (newActivities.length < activitiesPagination.value.pageSize) {
            noMoreData.value = true
          }

          console.log('[Activities] 活动数据：', recentActivities.value)
        } else {
          if (!isLoadMore) {
            recentActivities.value = []
          }
          console.error('[Activities] 数据格式异常:', response.data)
        }
      } catch (error) {
        if (!isLoadMore) {
          recentActivities.value = []
        }
        console.error('[Activities] 获取最近活动数据异常:', error)
      } finally {
        activitiesLoading.value = false
        loadingMore.value = false
      }
    }

    // 刷新活动数据
    const refreshActivities = () => {
      fetchRecentActivities(false)
    }

    // 渲染ECharts
    const renderRegistrationTrendChart = () => {
      if (!registrationTrendChart.value) {
        console.warn('[ECharts] registrationTrendChart ref 未绑定')
        return
      }

      console.log('[ECharts] 渲染数据：', registrationTrendData.value)

      // 销毁之前的图表实例
      const existingChart = echarts.getInstanceByDom(
        registrationTrendChart.value
      )
      if (existingChart) {
        existingChart.dispose()
      }

      // 创建新的图表实例
      const chart = echarts.init(registrationTrendChart.value)
      const option = {
        title: { text: 'User Registration Trend', left: 'center' },
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'category', data: registrationTrendData.value.dates },
        yAxis: { type: 'value', minInterval: 1 },
        series: [
          {
            data: registrationTrendData.value.counts,
            type: 'line',
            smooth: true,
            areaStyle: {},
            name: 'Registrations',
            color: '#409eff'
          }
        ]
      }
      chart.setOption(option)
      console.log('[ECharts] option:', option)

      // 监听窗口大小变化，重新调整图表大小
      const handleResize = () => {
        chart.resize()
      }
      window.addEventListener('resize', handleResize)

      // 返回清理函数
      return () => {
        window.removeEventListener('resize', handleResize)
        chart.dispose()
      }
    }

    // Computed properties
    const currentPageTitle = computed(() => {
      const titles = {
        dashboard: 'Dashboard',
        users: 'User Management',
        students: 'Students',
        teachers: 'Instructor',
        courses: 'Courses',
        groups: 'Groups',
        tasks: 'Tasks',
        reports: 'Reports',
        settings: 'Settings'
      }
      return titles[activeMenu.value] || 'Dashboard'
    })

    const currentPageIcon = computed(() => {
      const icons = {
        dashboard: 'DataBoard',
        users: 'User',
        students: 'Avatar',
        teachers: 'UserFilled',
        courses: 'Reading',
        groups: 'Collection',
        tasks: 'Document',
        reports: 'PieChart',
        settings: 'Setting'
      }
      return icons[activeMenu.value] || 'DataBoard'
    })

    // Event handlers
    const handleMenuSelect = index => {
      activeMenu.value = index
      // Load data based on selected menu
      if (index === 'students') {
        fetchStudents()
      } else if (index === 'teachers') {
        fetchTeachers()
      } else if (index === 'groups') {
        fetchGroups()
        fetchTeachers() // Also fetch teachers for group teacher selection
      } else if (index === 'dashboard') {
        // 当切换到Dashboard时，重新获取数据并渲染图表
        fetchRegistrationTrend()
        fetchRecentActivities()
      }
    }

    const handleStatCardClick = menuIndex => {
      activeMenu.value = menuIndex
      handleMenuSelect(menuIndex)
    }

    const handleActivityClick = menuIndex => {
      activeMenu.value = menuIndex
      handleMenuSelect(menuIndex)
    }

    const handleUserCommand = command => {
      switch (command) {
        case 'profile':
          ElMessage.info('Profile page will be implemented')
          break
        case 'settings':
          ElMessage.info('Settings page will be implemented')
          break
        case 'logout':
          handleLogout()
          break
      }
    }

    const handleLogout = async () => {
      try {
        console.log('[logout] 点击退出按钮')
        await ElMessageBox.confirm(
          'Are you sure you want to logout?',
          'Confirm Logout',
          {
            confirmButtonText: 'Yes',
            cancelButtonText: 'No',
            type: 'warning'
          }
        )
        console.log('[logout] 用户确认退出')
        await authStore.logout()
        console.log('[logout] localStorage 状态:', {
          authToken: localStorage.getItem('authToken'),
          userRole: localStorage.getItem('userRole'),
          userData: localStorage.getItem('userData')
        })
        console.log('[logout] Pinia 状态:', {
          token: authStore.token,
          isAuthenticated: authStore.isAuthenticated,
          user: authStore.user,
          role: authStore.role
        })
        if (ElMessage && typeof ElMessage.success === 'function') {
          ElMessage.success('Logged out successfully')
        }
        // 推荐用router跳转，如果没有则用location
        if (typeof window.$router !== 'undefined') {
          window.$router.push('/')
        } else {
          window.location.href = '/'
        }
      } catch (error) {
        console.log('[logout] error:', error)
        if (error !== 'cancel') {
          if (ElMessage && typeof ElMessage.error === 'function') {
            ElMessage.error('Logout failed')
          }
        }
      }
    }

    // Action handlers
    const showAddUserDialog = () => {
      userDialogIsEdit.value = false
      currentUserData.value = {}
      userDialogVisible.value = true
    }

    const showAddStudentDialog = () => {
      studentDialogIsEdit.value = false
      currentStudentData.value = {}
      studentDialogVisible.value = true
    }

    const showAddTeacherDialog = () => {
      teacherDialogIsEdit.value = false
      currentTeacherData.value = {}
      teacherDialogVisible.value = true
    }

    const showAddGroupDialog = () => {
      groupDialogIsEdit.value = false
      currentGroupData.value = {}
      groupDialogVisible.value = true
    }

    const showAddCourseDialog = () => {
      ElMessage.info('Add Course dialog will be implemented')
    }

    const generateReport = () => {
      ElMessage.info('Report generation will be implemented')
    }

    const editUser = user => {
      userDialogIsEdit.value = true
      currentUserData.value = { ...user }
      userDialogVisible.value = true
    }

    const deleteUser = async user => {
      try {
        await ElMessageBox.confirm(
          `Are you sure you want to delete user "${user.username}"?`,
          'Confirm Delete',
          {
            confirmButtonText: 'Delete',
            cancelButtonText: 'Cancel',
            type: 'warning'
          }
        )

        // TODO: Implement actual API call
        ElMessage.success('User deleted successfully')
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('Failed to delete user')
        }
      }
    }

    const editStudent = student => {
      studentDialogIsEdit.value = true
      currentStudentData.value = { ...student }
      studentDialogVisible.value = true
    }

    const deleteStudent = async student => {
      try {
        await ElMessageBox.confirm(
          `Are you sure you want to delete student "${student.name}"?`,
          'Confirm Delete',
          {
            confirmButtonText: 'Delete',
            cancelButtonText: 'Cancel',
            type: 'warning'
          }
        )

        await adminAPI.deleteStudent(student.id)
        ElMessage.success('Student deleted successfully')
        // Refresh students list
        fetchStudents()
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('Failed to delete student')
        }
      }
    }

    const editTeacher = teacher => {
      teacherDialogIsEdit.value = true
      currentTeacherData.value = { ...teacher }
      teacherDialogVisible.value = true
    }

    const deleteTeacher = async teacher => {
      try {
        await ElMessageBox.confirm(
          `Are you sure you want to delete teacher "${teacher.name}"?`,
          'Confirm Delete',
          {
            confirmButtonText: 'Delete',
            cancelButtonText: 'Cancel',
            type: 'warning'
          }
        )

        await adminAPI.deleteTeacher(teacher.id)
        ElMessage.success('Teacher deleted successfully')
        // Refresh teachers list
        fetchTeachers()
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('Failed to delete teacher')
        }
      }
    }

    const editGroup = group => {
      groupDialogIsEdit.value = true
      currentGroupData.value = { ...group }
      groupDialogVisible.value = true
    }

    const deleteGroup = async group => {
      try {
        await ElMessageBox.confirm(
          `Are you sure you want to delete group "${group.name}"?`,
          'Confirm Delete',
          {
            confirmButtonText: 'Delete',
            cancelButtonText: 'Cancel',
            type: 'warning'
          }
        )

        await adminAPI.deleteGroup(group.id)
        ElMessage.success('Group deleted successfully')
        // Refresh groups list
        fetchGroups()
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('Failed to delete group')
        }
      }
    }

    const approveTeacher = async teacher => {
      teacher.loadingApprove = true
      try {
        await adminAPI.approveTeacher(teacher.id)
        ElMessage.success(`Approved ${teacher.name}`)
        // Remove from pending list
        const index = pendingTeachers.value.findIndex(t => t.id === teacher.id)
        if (index > -1) {
          pendingTeachers.value.splice(index, 1)
        }
        // Refresh active teachers list
        await fetchTeachers()
      } catch (error) {
        ElMessage.error('Approve failed')
        console.error('Error approving teacher:', error)
      } finally {
        teacher.loadingApprove = false
      }
    }

    const rejectTeacher = async teacher => {
      teacher.loadingReject = true
      try {
        await adminAPI.rejectTeacher(teacher.id)
        ElMessage.success(`Rejected ${teacher.name}`)
        // Remove from pending list
        const index = pendingTeachers.value.findIndex(t => t.id === teacher.id)
        if (index > -1) {
          pendingTeachers.value.splice(index, 1)
        }
      } catch (error) {
        ElMessage.error('Reject failed')
        console.error('Error rejecting teacher:', error)
      } finally {
        teacher.loadingReject = false
      }
    }

    // Success handlers
    const handleUserSuccess = userData => {
      if (userDialogIsEdit.value) {
        // Update existing user
        const index = users.value.findIndex(
          u => u.id === currentUserData.value.id
        )
        if (index > -1) {
          users.value[index] = { ...users.value[index], ...userData }
        }
      } else {
        // Add new user
        const newUser = {
          id: users.value.length + 1,
          ...userData
        }
        users.value.push(newUser)
      }
    }

    const handleStudentSuccess = () => {
      // Refresh the students list to get the latest data
      fetchStudents()
    }

    const handleStudentStatusChange = async student => {
      try {
        await adminAPI.updateStudent(student)
        ElMessage.success(`Student ${student.name} status updated successfully`)
      } catch (error) {
        console.error('Failed to update student status:', error)
        ElMessage.error('Failed to update student status')
        // Revert the change by refreshing the data
        fetchStudents()
      }
    }

    const handleTeacherSuccess = () => {
      // Refresh the teachers list to get the latest data
      fetchTeachers()
    }

    const handleTeacherStatusChange = async teacher => {
      try {
        await adminAPI.updateTeacher(teacher)
        ElMessage.success(`Teacher ${teacher.name} status updated successfully`)
      } catch (error) {
        console.error('Failed to update teacher status:', error)
        ElMessage.error('Failed to update teacher status')
        // Revert the change by refreshing the data
        fetchTeachers()
      }
    }

    const handleGroupSuccess = () => {
      // Refresh the groups list to get the latest data
      fetchGroups()
    }

    const handleGroupTeacherChange = async group => {
      try {
        await adminAPI.updateGroup(group)
        ElMessage.success(`Group ${group.name} teacher updated successfully`)
      } catch (error) {
        console.error('Failed to update group teacher:', error)
        ElMessage.error('Failed to update group teacher')
        // Revert the change by refreshing the data
        fetchGroups()
      }
    }

    // Scroll container
    const handleActivitiesScroll = () => {
      if (activitiesScrollContainer.value) {
        const container = activitiesScrollContainer.value
        const scrollTop = container.scrollTop
        const scrollHeight = container.scrollHeight
        const clientHeight = container.clientHeight
        const scrollThreshold = scrollHeight - clientHeight - 100

        if (scrollTop >= scrollThreshold) {
          if (!loadingMore.value && !noMoreData.value) {
            loadingMore.value = true
            activitiesPagination.value.page++
            fetchRecentActivities(true)
          }
        }
      }
    }

    // Watch for activeMenu changes to re-render charts
    watch(
      () => activeMenu.value,
      newMenu => {
        if (newMenu === 'dashboard') {
          // 延迟渲染，确保DOM已经更新
          nextTick(() => {
            if (registrationTrendData.value.dates.length > 0) {
              renderRegistrationTrendChart()
            }
          })
        }
      }
    )

    // 监听分页状态变化
    watch(
      () => pagination.value.students.page,
      newPage => {
        console.log('学生分页页码变化:', newPage)
      }
    )

    watch(
      () => pagination.value.students.pageSize,
      newSize => {
        console.log('学生分页大小变化:', newSize)
      }
    )

    watch(
      () => pagination.value.teachers.page,
      newPage => {
        console.log('教师分页页码变化:', newPage)
      }
    )

    watch(
      () => pagination.value.teachers.pageSize,
      newSize => {
        console.log('教师分页大小变化:', newSize)
      }
    )

    // 处理学生分页变化
    const handleStudentPageChange = page => {
      console.log('学生分页页码变化处理函数被调用:', page)
      pagination.value.students.page = page
      fetchStudents()
    }

    // 处理学生每页数量变化
    const handleStudentSizeChange = size => {
      console.log('学生分页大小变化处理函数被调用:', size)
      pagination.value.students.pageSize = size
      pagination.value.students.page = 1 // 重置到第一页
      fetchStudents()
    }

    // 处理教师分页变化
    const handleTeacherPageChange = page => {
      console.log('教师分页页码变化处理函数被调用:', page)
      pagination.value.teachers.page = page
      fetchTeachers()
    }

    // 处理教师每页数量变化
    const handleTeacherSizeChange = size => {
      console.log('教师分页大小变化处理函数被调用:', size)
      pagination.value.teachers.pageSize = size
      pagination.value.teachers.page = 1 // 重置到第一页
      fetchTeachers()
    }

    // 处理小组分页变化
    const handleGroupPageChange = page => {
      console.log('小组分页页码变化处理函数被调用:', page)
      pagination.value.groups.page = page
      fetchGroups()
    }

    // 处理小组每页数量变化
    const handleGroupSizeChange = size => {
      console.log('小组分页大小变化处理函数被调用:', size)
      pagination.value.groups.pageSize = size
      pagination.value.groups.page = 1 // 重置到第一页
      fetchGroups()
    }

    // Check authentication and load initial data on mount
    onMounted(async () => {
      console.log('AdminDashboard 组件挂载开始...')
      console.log('认证状态:', authStore.isAuthenticated)
      console.log('管理员状态:', authStore.isAdmin)

      if (!authStore.isAuthenticated || !authStore.isAdmin) {
        console.error('访问被拒绝：需要管理员权限')
        ElMessage.error('Access denied. Admin privileges required.')
        window.location.href = '/'
        return
      }

      console.log('开始加载初始数据...')
      // Load initial data
      try {
        await Promise.all([
          fetchPendingTeachers(),
          fetchStudents(),
          fetchTeachers(),
          fetchGroups(),
          fetchRegistrationTrend(),
          fetchRecentActivities()
        ])
        console.log('初始数据加载完成')
      } catch (error) {
        console.error('初始数据加载失败:', error)
      }
    })

    return {
      authStore,
      activeMenu,
      loading,
      statistics,
      recentActivities,
      pendingTeachers,
      students,
      teachers,
      groups,
      pagination,
      searchFilters,
      currentPageTitle,
      currentPageIcon,
      userDialogVisible,
      userDialogIsEdit,
      currentUserData,
      studentDialogVisible,
      studentDialogIsEdit,
      currentStudentData,
      teacherDialogVisible,
      teacherDialogIsEdit,
      currentTeacherData,
      groupDialogVisible,
      groupDialogIsEdit,
      currentGroupData,
      handleMenuSelect,
      handleStatCardClick,
      handleActivityClick,
      handleUserCommand,
      handleLogout,
      showAddUserDialog,
      showAddStudentDialog,
      showAddTeacherDialog,
      showAddGroupDialog,
      showAddCourseDialog,
      generateReport,
      editUser,
      deleteUser,
      editStudent,
      deleteStudent,
      editTeacher,
      deleteTeacher,
      editGroup,
      deleteGroup,
      handleUserSuccess,
      handleStudentSuccess,
      handleTeacherSuccess,
      handleGroupSuccess,
      approveTeacher,
      rejectTeacher,
      registrationTrendChart,
      fetchRegistrationTrend,
      fetchRecentActivities,
      handleStudentStatusChange,
      handleTeacherStatusChange,
      handleGroupTeacherChange,
      activitiesLoading,
      activitiesPagination,
      loadingMore,
      noMoreData,
      activitiesScrollContainer,
      handleActivitiesScroll,
      refreshActivities,
      showImportStudentsDialog,
      showImportTeachersDialog,
      handleStudentFileChange,
      handleTeacherFileChange,
      importStudents,
      importTeachers,
      importing,
      selectedStudentFile,
      selectedTeacherFile,
      importStudentsDialogVisible,
      importTeachersDialogVisible,
      handleStudentPageChange,
      handleStudentSizeChange,
      handleTeacherPageChange,
      handleTeacherSizeChange,
      handleGroupPageChange,
      handleGroupSizeChange,
      studentUploadRef,
      teacherUploadRef,
      resetStudentUpload,
      resetTeacherUpload
    }
  }
}
</script>

<style scoped>
.admin-dashboard {
  height: 100vh;
  display: flex;
}

.sidebar {
  background-color: #304156;
  color: #bfcbd9;
  height: 100vh;
  overflow-y: auto;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #435266;
}

.sidebar-header h2 {
  margin: 0;
  color: #bfcbd9;
  font-size: 18px;
  font-weight: 600;
}

.sidebar-menu {
  border: none;
}

.main-header {
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-dropdown:hover {
  background-color: #f5f7fa;
}

.username {
  font-size: 14px;
  color: #606266;
}

.main-content {
  background: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
}

.dashboard-overview {
  max-width: 1200px;
  margin: 0 auto;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  transition: transform 0.3s;
  cursor: pointer;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-info h3 {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #909399;
  font-weight: normal;
}

.stat-number {
  margin: 0 0 4px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.stat-change {
  margin: 0;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.stat-change.up {
  color: #67c23a;
}

.stat-change.down {
  color: #f56c6c;
}

.charts-row {
  margin-bottom: 20px;
}

.chart-card {
  height: 400px;
}

.chart-placeholder {
  height: 300px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
}

.chart-placeholder p {
  margin: 16px 0 0 0;
  font-size: 14px;
}

.activity-item {
  cursor: pointer;
  transition: all 0.3s;
}

.activity-item:hover {
  background-color: #f5f7fa;
  border-radius: 4px;
  padding: 8px;
  margin: -8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-buttons {
  display: flex;
  gap: 10px;
}

.card-content {
  margin-top: 20px;
}

.section-placeholder {
  max-width: 800px;
  margin: 0 auto;
}

.placeholder-content {
  text-align: center;
  padding: 60px 20px;
  color: #909399;
}

.placeholder-content h3 {
  margin: 16px 0 8px 0;
  color: #606266;
}

.placeholder-content p {
  margin: 0;
  font-size: 14px;
}

/* Responsive design */
@media (max-width: 768px) {
  .sidebar {
    width: 200px !important;
  }

  .stats-row .el-col {
    margin-bottom: 16px;
  }
}

@media (max-width: 576px) {
  .sidebar {
    width: 100% !important;
    position: fixed;
    z-index: 1000;
    transform: translateX(-100%);
    transition: transform 0.3s;
  }

  .sidebar.open {
    transform: translateX(0);
  }

  .main-header {
    padding: 0 16px;
  }

  .main-content {
    padding: 16px;
  }
}

.teacher-requests-row {
  margin-bottom: 20px;
}
.teacher-requests-list {
  margin-top: 12px;
}
.teacher-request-item {
  margin-bottom: 24px;
}
.request-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.08);
  padding: 20px 16px 16px 16px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}
.teacher-info {
  margin-bottom: 16px;
}
.teacher-name {
  font-size: 18px;
  font-weight: 600;
  color: #222;
}
.teacher-email {
  font-size: 15px;
  color: #b0b0b0;
  margin-top: 2px;
}
.request-actions {
  display: flex;
  gap: 16px;
  width: 100%;
  justify-content: flex-start;
}

.search-filters {
  margin-bottom: 20px;
}

.pagination-wrapper {
  margin-top: 20px;
  text-align: right;
}

.activities-container {
  height: 350px;
  overflow: hidden;
}

.activities-scroll-container {
  height: 100%;
  overflow-y: auto;
  padding-right: 8px;
}

.activities-scroll-container::-webkit-scrollbar {
  width: 6px;
}

.activities-scroll-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.activities-scroll-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.activities-scroll-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.loading-more {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 16px;
  color: #909399;
  font-size: 14px;
}

.loading-more .el-icon {
  margin-right: 8px;
}

.no-more-data {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 16px;
  color: #909399;
  font-size: 14px;
}

.teacher-card {
  margin-bottom: 20px;
}

.teacher-card:last-child {
  margin-bottom: 0;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #909399;
}

.empty-state p {
  margin: 0;
  font-size: 14px;
}

/* 导入对话框样式 */
.import-dialog-content {
  padding: 20px;
}

.selected-file {
  margin-top: 15px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  border-left: 3px solid #409eff;
}

.el-upload__tip {
  line-height: 1.5;
  margin-top: 10px;
  color: #909399;
}

.el-upload-dragger {
  width: 100%;
}

/* 导入错误样式 */
.import-errors {
  max-height: 300px;
  overflow-y: auto;
  margin-top: 15px;
}

.import-errors ul {
  padding-left: 20px;
}

.import-errors li {
  margin-bottom: 5px;
  color: #f56c6c;
}
</style>
