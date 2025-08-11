<template>
  <div class="teacher-dashboard">
    <el-container>
      <!-- Sidebar -->
      <el-aside width="250px" class="sidebar">
        <div class="sidebar-header">
          <h2>Instructor Panel</h2>
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
          <el-menu-item index="student">
            <el-icon><Avatar /></el-icon>
            <span>Student</span>
          </el-menu-item>
          <el-menu-item index="groups">
            <el-icon><Collection /></el-icon>
            <span>Groups</span>
          </el-menu-item>
          <el-menu-item index="create-groups">
            <el-icon><Plus /></el-icon>
            <span>Create Groups</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- Main content -->
      <el-container>
        <!-- Header -->
        <el-header class="main-header">
          <div class="header-left">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item>Instructor</el-breadcrumb-item>
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
                  <el-dropdown-item command="profile">
                    Profile
                  </el-dropdown-item>
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
          <div v-if="activeMenu === 'dashboard'" class="dashboard-overview">
            <!-- Statistics Cards -->
            <el-row :gutter="20" class="stats-row">
              <el-col :span="6" v-for="stat in statistics" :key="stat.title">
                <el-card class="stat-card" shadow="hover">
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
                  </div>
                </el-card>
              </el-col>
            </el-row>

            <!-- Recent Activities & Pending Tasks -->
            <el-row :gutter="20" class="charts-row">
              <el-col :span="12">
                <el-card class="chart-card">
                  <template #header>
                    <div class="card-header">
                      <span>Recent Activities</span>
                      <el-button
                        type="primary"
                        size="small"
                        @click="fetchRecentActivities"
                        :loading="loading.activities"
                      >
                        Refresh
                      </el-button>
                    </div>
                  </template>
                  <div class="activities-container">
                    <el-timeline v-loading="loading.activities">
                      <el-timeline-item
                        v-for="(activity, index) in recentActivities"
                        :key="activity.id || index"
                        :timestamp="activity.timestamp || activity.time"
                        :type="getActivityType(activity.type)"
                      >
                        {{ activity.content }}
                      </el-timeline-item>
                    </el-timeline>
                    <div class="load-more-container">
                      <el-button
                        type="text"
                        @click="loadMoreActivities"
                        :loading="loading.activities"
                        v-if="recentActivities.length > 0"
                      >
                        Load More
                      </el-button>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="12">
                <el-card class="chart-card">
                  <template #header>
                    <div class="card-header">
                      <span>Pending Tasks</span>
                      <div class="header-buttons">
                        <el-button
                          type="primary"
                          size="small"
                          @click="fetchTasks"
                          :loading="loading.tasks"
                        >
                          Refresh
                        </el-button>
                      </div>
                    </div>
                  </template>
                  <div class="tasks-list" v-loading="loading.tasks">
                    <div
                      v-for="task in pendingTasks"
                      :key="task.id"
                      class="task-item"
                    >
                      <div class="task-header">
                        <h4>{{ task.title }}</h4>
                        <span class="priority" :class="task.priority">
                          {{ task.priority }}
                        </span>
                      </div>
                      <p class="task-course">{{ task.course }}</p>
                      <p class="task-description">{{ task.description }}</p>
                      <div class="task-actions">
                        <el-button
                          type="primary"
                          size="small"
                          @click="completeTask(task)"
                          :loading="loading.tasks"
                        >
                          Complete
                        </el-button>
                        <el-button
                          type="info"
                          size="small"
                          @click="viewDetails(task)"
                        >
                          Details
                        </el-button>
                      </div>
                    </div>
                    <div v-if="pendingTasks.length === 0" class="empty-state">
                      <p>No pending tasks</p>
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>
          <div v-else-if="activeMenu === 'student'" class="student-management">
            <el-card>
              <template #header>
                <div class="card-header">
                  <span>Student List</span>
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
                :data="students"
                style="width: 100%"
                v-loading="loading.students"
              >
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="name" label="Name" />
                <el-table-column prop="email" label="Email" />
                <el-table-column prop="phone" label="Phone" />
                <el-table-column prop="status" label="Status">
                  <template #default="scope">
                    <el-tag
                      :type="
                        scope.row.status === 'ACTIVE' ? 'success' : 'warning'
                      "
                    >
                      {{ scope.row.status }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="Actions" width="120">
                  <template #default="scope">
                    <el-button
                      size="small"
                      @click="showEditStudentDialog(scope.row)"
                    >
                      Edit
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
          <div v-else-if="activeMenu === 'groups'" class="groups-management">
            <el-card>
              <template #header>
                <div class="card-header">
                  <span>Groups</span>
                  <div class="header-buttons">
                    <el-button type="success" @click="showImportGroupsDialog">
                      <el-icon><UploadFilled /></el-icon>
                      Import Groups
                    </el-button>
                    <el-button type="success" @click="showImportTasksDialog">
                      <el-icon><UploadFilled /></el-icon>
                      Import Tasks
                    </el-button>
                  </div>
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
                <el-table-column
                  prop="teacherId"
                  label="Teacher ID"
                  width="100"
                />
                <el-table-column
                  prop="createdAt"
                  label="Created At"
                  width="180"
                >
                  <template #default="scope">
                    {{ new Date(scope.row.createdAt).toLocaleString() }}
                  </template>
                </el-table-column>
                <el-table-column label="Details" width="180">
                  <template #default="scope">
                    <el-dropdown
                      trigger="click"
                      @command="
                        cmd => handleGroupDetailsCommand(cmd, scope.row)
                      "
                    >
                      <el-button size="mini" type="info">
                        View Details<el-icon><ArrowDown /></el-icon>
                      </el-button>
                      <template #dropdown>
                        <el-dropdown-menu>
                          <el-dropdown-item command="students"
                            >Students</el-dropdown-item
                          >
                          <el-dropdown-item command="tasks"
                            >Tasks</el-dropdown-item
                          >
                        </el-dropdown-menu>
                      </template>
                    </el-dropdown>
                  </template>
                </el-table-column>
                <el-table-column label="Actions" width="120">
                  <template #default="scope">
                    <el-button
                      size="mini"
                      type="primary"
                      @click="openCreateTaskDialog(scope.row)"
                      >Create Task</el-button
                    >
                  </template>
                </el-table-column>
              </el-table>

              <!-- Pagination -->
              <div class="pagination-wrapper">
                <el-pagination
                  v-model:current-page="pagination.groups.page"
                  v-model:page-size="pagination.groups.pageSize"
                  :page-sizes="[10, 20, 50, 100]"
                  :total="pagination.groups.total"
                  layout="total, sizes, prev, pager, next, jumper"
                  @size-change="fetchGroups"
                  @current-change="fetchGroups"
                />
              </div>
            </el-card>

            <!-- Create Task Dialog Form -->
            <el-dialog v-model="createTaskDialogVisible" title="Create Task">
              <el-form
                :model="createTaskForm"
                :rules="createTaskRules"
                ref="createTaskFormRef"
                label-width="100px"
              >
                <el-form-item label="Title" prop="title">
                  <el-input v-model="createTaskForm.title" />
                </el-form-item>
                <el-form-item label="Description" prop="description">
                  <el-input
                    type="textarea"
                    v-model="createTaskForm.description"
                  />
                </el-form-item>
                <el-form-item label="Cycle (weeks)" prop="cycle">
                  <el-input-number v-model="createTaskForm.cycle" :min="1" />
                </el-form-item>
                <el-form-item label="Due Date" prop="dueDate">
                  <el-date-picker
                    v-model="createTaskForm.dueDate"
                    type="date"
                  />
                </el-form-item>
                <el-form-item label="Attachment">
                  <el-upload
                    class="upload-demo"
                    :auto-upload="false"
                    :show-file-list="false"
                    :on-change="handleFileChange"
                    :before-remove="handleFileRemove"
                    accept=".pdf,.doc,.docx,.txt,.jpg,.jpeg,.png,.zip,.rar"
                  >
                    <el-button type="primary">Choose File</el-button>
                  </el-upload>
                  <div
                    v-if="createTaskForm.selectedFile"
                    style="margin-top: 10px"
                  >
                    <span style="color: #409eff">
                      <el-icon><Document /></el-icon>
                      {{ createTaskForm.selectedFile.name }}
                      ({{ formatFileSize(createTaskForm.selectedFile.size) }})
                    </span>
                    <el-button
                      type="danger"
                      size="small"
                      @click="removeSelectedFile"
                      style="margin-left: 10px"
                    >
                      Remove
                    </el-button>
                  </div>
                  <span
                    v-if="createTaskForm.fileUrl"
                    style="margin-top: 10px; display: block"
                  >
                    <span style="color: #67c23a">
                      <el-icon><Check /></el-icon>
                      File uploaded successfully
                    </span>
                  </span>
                </el-form-item>
              </el-form>
              <template #footer>
                <el-button @click="createTaskDialogVisible = false"
                  >Cancel</el-button
                >
                <el-button type="primary" @click="submitCreateTask"
                  >Submit</el-button
                >
              </template>
            </el-dialog>

            <!-- Group Students Dialog -->
            <el-dialog
              v-model="groupStudentsDialogVisible"
              title="Group Students"
              width="600px"
            >
              <el-table :data="currentGroupStudents" style="width: 100%">
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="name" label="Name" />
                <el-table-column prop="email" label="Email" />
                <el-table-column prop="phone" label="Phone" />
                <el-table-column prop="status" label="Status">
                  <template #default="scope">
                    <el-tag
                      :type="
                        scope.row.status === 'ACTIVE' ? 'success' : 'warning'
                      "
                    >
                      {{ scope.row.status }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
              <template #footer>
                <el-button @click="groupStudentsDialogVisible = false"
                  >Close</el-button
                >
              </template>
            </el-dialog>

            <!-- Group Tasks Dialog -->
            <el-dialog
              v-model="groupTasksDialogVisible"
              title="Group Tasks"
              width="900px"
            >
              <el-table :data="currentGroupTasks" style="width: 100%">
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="title" label="Title" width="120" />
                <el-table-column prop="description" label="Description" />
                <el-table-column prop="cycle" label="Cycle" width="80" />
                <el-table-column prop="dueDate" label="Due Date" width="140">
                  <template #default="scope">
                    {{
                      scope.row.dueDate
                        ? new Date(scope.row.dueDate).toLocaleDateString()
                        : ''
                    }}
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="Status" width="100">
                  <template #default="scope">
                    <el-tag
                      :type="
                        scope.row.status === 'PENDING'
                          ? 'info'
                          : scope.row.status === 'COMPLETED'
                          ? 'success'
                          : 'warning'
                      "
                    >
                      {{ scope.row.status }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="File" width="120">
                  <template #default="scope">
                    <el-button
                      v-if="scope.row.fileUrl"
                      type="primary"
                      size="small"
                      @click="previewFile(scope.row.fileUrl, scope.row.title)"
                    >
                      <el-icon><Document /></el-icon>
                      Preview
                    </el-button>
                    <span v-else style="color: #909399">No file</span>
                  </template>
                </el-table-column>
              </el-table>
              <template #footer>
                <el-button @click="groupTasksDialogVisible = false"
                  >Close</el-button
                >
              </template>
            </el-dialog>

            <!-- File Preview Dialog -->
            <el-dialog
              v-model="filePreviewDialogVisible"
              :title="'File Preview: ' + previewFileTitle"
              width="80%"
              :before-close="closeFilePreview"
            >
              <div class="file-preview-container" style="height: 500px">
                <!-- PDF预览 -->
                <div v-if="previewFileType === 'pdf'" style="height: 100%">
                  <!-- 多种PDF预览选项 -->
                  <div style="margin-bottom: 10px; text-align: center">
                    <el-button-group>
                      <el-button type="primary" @click="openInNewTab">
                        <el-icon><View /></el-icon>
                        Browser Preview
                      </el-button>
                      <el-button @click="useGoogleViewer">
                        <el-icon><Document /></el-icon>
                        Google Viewer
                      </el-button>
                      <el-button @click="useMicrosoftViewer">
                        <el-icon><OfficeBuilding /></el-icon>
                        Office Viewer
                      </el-button>
                      <el-button @click="useDirectPreview">
                        <el-icon><Refresh /></el-icon>
                        Direct Preview
                      </el-button>
                    </el-button-group>
                  </div>
                  <iframe
                    :src="pdfViewerUrl"
                    style="width: 100%; height: calc(100% - 50px); border: none"
                  ></iframe>
                </div>

                <!-- Office文档预览 -->
                <div
                  v-else-if="previewFileType === 'office'"
                  style="height: 100%"
                >
                  <div style="margin-bottom: 10px; text-align: center">
                    <el-button-group>
                      <el-button type="primary" @click="openInNewTab">
                        <el-icon><View /></el-icon>
                        Browser Preview
                      </el-button>
                      <el-button @click="useMicrosoftViewer">
                        <el-icon><OfficeBuilding /></el-icon>
                        Office Online
                      </el-button>
                      <el-button @click="useGoogleViewer">
                        <el-icon><Document /></el-icon>
                        Google Docs Viewer
                      </el-button>
                    </el-button-group>
                  </div>
                  <iframe
                    :src="officeViewerUrl"
                    style="width: 100%; height: calc(100% - 50px); border: none"
                  ></iframe>
                </div>

                <!-- 图片预览 -->
                <div
                  v-else-if="previewFileType === 'image'"
                  style="
                    text-align: center;
                    height: 100%;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                  "
                >
                  <img
                    :src="previewFileUrl"
                    style="
                      max-width: 100%;
                      max-height: 100%;
                      object-fit: contain;
                    "
                    @error="handleImageError"
                  />
                </div>

                <!-- 文本文件预览 -->
                <div
                  v-else-if="previewFileType === 'text'"
                  style="
                    height: 100%;
                    overflow-y: auto;
                    white-space: pre-wrap;
                    font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
                    padding: 20px;
                    background: #f8f9fa;
                    border: 1px solid #e9ecef;
                    border-radius: 4px;
                  "
                >
                  <pre style="margin: 0; font-family: inherit">{{
                    previewFileContent
                  }}</pre>
                </div>

                <!-- 其他文件类型 -->
                <div
                  v-else
                  style="
                    text-align: center;
                    height: 100%;
                    display: flex;
                    flex-direction: column;
                    align-items: center;
                    justify-content: center;
                  "
                >
                  <el-icon
                    size="64"
                    style="color: #909399; margin-bottom: 20px"
                  >
                    <Document />
                  </el-icon>
                  <p style="color: #909399; margin-bottom: 20px">
                    This file type cannot be previewed directly
                  </p>
                  <div
                    style="
                      display: flex;
                      gap: 10px;
                      flex-wrap: wrap;
                      justify-content: center;
                    "
                  >
                    <el-button type="primary" @click="openInNewTab">
                      <el-icon><View /></el-icon>
                      Open in Browser
                    </el-button>
                    <el-button @click="downloadFile">
                      <el-icon><Download /></el-icon>
                      Download File
                    </el-button>
                  </div>
                </div>
              </div>
              <template #footer>
                <el-button @click="downloadFile">
                  <el-icon><Download /></el-icon>
                  Download
                </el-button>
                <el-button @click="closeFilePreview">Close</el-button>
              </template>
            </el-dialog>
          </div>
          <div
            v-else-if="activeMenu === 'create-groups'"
            class="create-groups-section"
          >
            <el-card>
              <template #header>
                <div class="card-header">
                  <span>Create New Group</span>
                  <div class="header-buttons">
                    <el-button type="success" @click="showImportGroupsDialog">
                      <el-icon><UploadFilled /></el-icon>
                      Import Excel
                    </el-button>
                  </div>
                </div>
              </template>
              <el-form
                :model="newGroup"
                label-width="120px"
                class="create-group-form"
                :rules="groupRules"
                ref="groupFormRef"
              >
                <el-form-item label="Group Name" prop="name">
                  <el-input
                    v-model="newGroup.name"
                    placeholder="Enter group name"
                    maxlength="50"
                    show-word-limit
                  />
                </el-form-item>
                <el-form-item label="Description" prop="description">
                  <el-input
                    v-model="newGroup.description"
                    type="textarea"
                    placeholder="Enter group description"
                    :rows="3"
                    maxlength="200"
                    show-word-limit
                  />
                </el-form-item>
                <el-form-item label="Members" prop="members">
                  <el-select
                    v-model="newGroup.members"
                    multiple
                    placeholder="Select members"
                    style="width: 100%"
                    :filterable="false"
                    :remote="false"
                    @visible-change="handleSelectDropdown"
                  >
                    <template #header>
                      <el-input
                        v-model="studentSearchKeyword"
                        placeholder="Search by name"
                        size="small"
                        @input="handleStudentSearch"
                        clearable
                        style="margin: 4px"
                      />
                    </template>
                    <el-option
                      v-for="student in searchStudentList"
                      :key="student.id"
                      :label="`${student.name} (${student.studentNo})`"
                      :value="student.id"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button
                    type="primary"
                    @click="createGroup"
                    :loading="loading.groups"
                  >
                    Create Group
                  </el-button>
                  <el-button @click="resetGroupForm">Reset</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </div>
          <div v-else class="section-placeholder">
            <el-card>
              <template #header>
                <div class="card-header">
                  <span>{{ currentPageTitle }}</span>
                </div>
              </template>
              <div class="placeholder-content">
                <el-icon :size="64">
                  <component :is="currentPageIcon" />
                </el-icon>
                <h3>{{ currentPageTitle }} Management</h3>
                <p>This section is under development. Coming soon...</p>
              </div>
            </el-card>
          </div>
        </el-main>
      </el-container>

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
            :limit="1"
            accept=".xlsx"
            :on-change="handleStudentFileChange"
            :file-list="
              selectedStudentFile ? [{ name: selectedStudentFile.name }] : []
            "
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">
              Drop file here or <em>click to upload</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                Only .xlsx files are supported. The file should contain columns:
                username, password, name, email, phone, studentNo
              </div>
            </template>
          </el-upload>

          <div v-if="selectedStudentFile" class="selected-file">
            <span>Selected file: {{ selectedStudentFile.name }}</span>
          </div>
        </div>

        <template #footer>
          <span class="dialog-footer">
            <el-button @click="closeStudentDialog">Cancel</el-button>
            <el-button
              type="primary"
              @click="importStudents"
              :loading="importing.students"
            >
              Import
            </el-button>
          </span>
        </template>
      </el-dialog>

      <!-- 批量导入小组对话框 -->
      <el-dialog
        v-model="importGroupsDialogVisible"
        title="Import Groups from Excel"
        width="500px"
        @closed="resetGroupUpload"
      >
        <div class="import-dialog-content">
          <el-upload
            ref="groupUploadRef"
            class="upload-demo"
            drag
            action="#"
            :auto-upload="false"
            :limit="1"
            accept=".xlsx"
            :on-change="handleGroupFileChange"
            :file-list="
              selectedGroupFile ? [{ name: selectedGroupFile.name }] : []
            "
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">
              Drop file here or <em>click to upload</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                <p>
                  Only .xlsx files are supported. The file should contain
                  columns:
                </p>
                <ul>
                  <li>
                    <strong>Name</strong> (required): Group name (must be
                    unique)
                  </li>
                  <li><strong>Description</strong>: Group description</li>
                  <li>
                    <strong>Members</strong>: Comma-separated student IDs (e.g.,
                    "1,2,3")
                  </li>
                </ul>
                <p>
                  Note: Student IDs will be validated. Non-existent students
                  will be skipped and reported.
                </p>
              </div>
            </template>
          </el-upload>

          <div v-if="selectedGroupFile" class="selected-file">
            <span>Selected file: {{ selectedGroupFile.name }}</span>
          </div>
        </div>

        <template #footer>
          <span class="dialog-footer">
            <el-button @click="closeGroupDialog">Cancel</el-button>
            <el-button
              type="primary"
              @click="importGroups"
              :loading="importingGroups.groups"
            >
              Import
            </el-button>
          </span>
        </template>
      </el-dialog>

      <!-- 添加/编辑学生对话框 -->
      <el-dialog
        v-model="studentDialogVisible"
        :title="studentDialogIsEdit ? 'Edit Student' : 'Add Student'"
        width="500px"
      >
        <el-form
          ref="studentFormRef"
          :model="studentForm"
          :rules="studentRules"
          label-width="120px"
        >
          <el-form-item label="Username" prop="username">
            <el-input v-model="studentForm.username" />
          </el-form-item>
          <el-form-item
            label="Password"
            prop="password"
            v-if="!studentDialogIsEdit"
          >
            <el-input v-model="studentForm.password" type="password" />
          </el-form-item>
          <el-form-item label="Name" prop="name">
            <el-input v-model="studentForm.name" />
          </el-form-item>
          <el-form-item label="Email" prop="email">
            <el-input v-model="studentForm.email" />
          </el-form-item>
          <el-form-item label="Phone" prop="phone">
            <el-input v-model="studentForm.phone" />
          </el-form-item>
          <el-form-item label="Student Number" prop="studentNo">
            <el-input v-model="studentForm.studentNo" />
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="studentDialogVisible = false">Cancel</el-button>
            <el-button
              type="primary"
              @click="submitStudentForm"
              :loading="loading.submitStudent"
            >
              {{ studentDialogIsEdit ? 'Update' : 'Create' }}
            </el-button>
          </span>
        </template>
      </el-dialog>

      <!-- 批量导入任务对话框 -->
      <el-dialog
        v-model="importTasksDialogVisible"
        title="Import Tasks from Excel"
        width="500px"
        @closed="resetTaskUpload"
      >
        <div class="import-dialog-content">
          <el-upload
            ref="taskUploadRef"
            class="upload-demo"
            drag
            action="#"
            :auto-upload="false"
            :limit="1"
            accept=".xlsx"
            :on-change="handleTaskFileChange"
            :file-list="
              selectedTaskFile ? [{ name: selectedTaskFile.name }] : []
            "
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">
              Drop file here or <em>click to upload</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                <p>
                  Only .xlsx files are supported. The file should contain
                  columns:
                </p>
                <ul>
                  <li><strong>Title</strong> (required): Task title</li>
                  <li><strong>Description</strong>: Task description</li>
                  <li>
                    <strong>Group ID</strong> (required): Group ID (must belong
                    to current teacher)
                  </li>
                  <li>
                    <strong>Due Date</strong>: Due date (YYYY-MM-DD format)
                  </li>
                  <li><strong>Cycle</strong>: Task cycle (default: 1)</li>
                  <li><strong>File URL</strong>: Task file URL</li>
                </ul>
                <p>
                  Note: Group IDs will be validated. Non-existent groups or
                  groups not belonging to current teacher will be skipped and
                  reported.
                </p>
              </div>
            </template>
          </el-upload>

          <div v-if="selectedTaskFile" class="selected-file">
            <span>Selected file: {{ selectedTaskFile.name }}</span>
          </div>
        </div>

        <template #footer>
          <span class="dialog-footer">
            <el-button @click="closeTaskDialog">Cancel</el-button>
            <el-button
              type="primary"
              @click="importTasks"
              :loading="importingTasks.tasks"
            >
              Import
            </el-button>
          </span>
        </template>
      </el-dialog>
    </el-container>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/store/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  DataBoard,
  Avatar,
  Collection,
  Plus,
  ArrowDown,
  Search,
  View,
  OfficeBuilding,
  Refresh,
  UploadFilled
} from '@element-plus/icons-vue'
import { teacherAPI } from '@/api/teacher'
import { createTask, getGroupMembers, getGroupTasks } from '../store/Teacher'
import axios from 'axios'

export default {
  name: 'TeacherDashboard',
  components: {
    DataBoard,
    Avatar,
    Collection,
    Plus,
    ArrowDown,
    Search,
    View,
    OfficeBuilding,
    Refresh,
    UploadFilled
  },
  setup() {
    const authStore = useAuthStore()
    const activeMenu = ref('dashboard')
    const groupFormRef = ref(null)

    // Loading states
    const loading = ref({
      students: false,
      groups: false,
      activities: false,
      tasks: false,
      stats: false,
      searchStudent: false,
      submitStudent: false
    })

    // Data refs
    const students = ref([])
    const groups = ref([])
    const recentActivities = ref([])
    const pendingTasks = ref([])
    const searchStudentList = ref([])
    const studentSearchKeyword = ref('')
    const searchStudentTimeout = ref(null)

    // Pagination
    const pagination = ref({
      students: { page: 1, pageSize: 10, total: 0 },
      groups: { page: 1, pageSize: 10, total: 0 },
      activities: { page: 1, pageSize: 10, total: 0 },
      tasks: { page: 1, pageSize: 10, total: 0 }
    })

    // Search filters
    const searchFilters = ref({
      students: { name: '', id: null },
      groups: { name: '' }
    })

    // Statistics data
    const statistics = ref([
      {
        title: 'Total Students',
        value: 0,
        color: '#409eff',
        icon: 'Avatar'
      },
      {
        title: 'My Groups',
        value: 0,
        color: '#67c23a',
        icon: 'Collection'
      },
      {
        title: 'Active Tasks',
        value: 0,
        color: '#e6a23c',
        icon: 'Document'
      },
      {
        title: 'Completed Tasks',
        value: 0,
        color: '#f56c6c',
        icon: 'Check'
      }
    ])

    // New group form
    const newGroup = ref({
      name: '',
      description: '',
      members: []
    })

    // Group rules
    const groupRules = {
      name: [
        { required: true, message: 'Please enter group name', trigger: 'blur' },
        {
          max: 50,
          message: 'Group name cannot exceed 50 characters',
          trigger: 'blur'
        }
      ],
      description: [
        {
          max: 200,
          message: 'Group description cannot exceed 200 characters',
          trigger: 'blur'
        }
      ],
      members: [
        {
          required: true,
          message: 'Please select at least one member',
          trigger: 'change'
        }
      ]
    }

    // New task dialog
    const createTaskDialogVisible = ref(false)
    const createTaskForm = ref({
      title: '',
      description: '',
      cycle: 1,
      dueDate: '',
      groupId: null,
      fileUrl: ''
    })
    const createTaskRules = {
      title: [
        { required: true, message: 'Please enter task title', trigger: 'blur' }
      ],
      cycle: [
        {
          required: true,
          message: 'Please enter cycle number',
          trigger: 'blur'
        }
      ]
    }
    const createTaskFormRef = ref()

    // Group members
    const groupMembers = ref({})

    // Group dialogs
    const groupStudentsDialogVisible = ref(false)
    const groupTasksDialogVisible = ref(false)
    const currentGroupStudents = ref([])
    const currentGroupTasks = ref([])

    // File preview
    const filePreviewDialogVisible = ref(false)
    const previewFileUrl = ref('')
    const previewFileTitle = ref('')
    const previewFileType = ref('')
    const previewFileContent = ref('')
    const pdfViewerUrl = ref('')
    const officeViewerUrl = ref('')

    // API methods
    const fetchDashboardStats = async () => {
      loading.value.stats = true
      try {
        const response = await teacherAPI.getDashboardStats()
        if (response.data.code === 1) {
          const stats = response.data.data
          statistics.value[0].value = stats.totalStudents || 0
          statistics.value[1].value = stats.totalGroups || 0
          statistics.value[2].value = stats.activeTasks || 0
          statistics.value[3].value = stats.completedTasks || 0
        }
      } catch (error) {
        ElMessage.error('Failed to get statistics')
        console.error('Error fetching dashboard stats:', error)
      } finally {
        loading.value.stats = false
      }
    }

    const fetchRecentActivities = async (isLoadMore = false) => {
      if (loading.value.activities) return
      loading.value.activities = true
      try {
        const params = {
          page: pagination.value.activities.page,
          pageSize: pagination.value.activities.pageSize
        }
        const response = await teacherAPI.getRecentActivities(params)
        if (response.data.code === 1) {
          const activities = response.data.data
          if (isLoadMore) {
            recentActivities.value.push(...activities)
          } else {
            recentActivities.value = activities
          }
        }
      } catch (error) {
        ElMessage.error('Failed to get recent activities')
        console.error('Error fetching recent activities:', error)
      } finally {
        loading.value.activities = false
      }
    }

    const loadMoreActivities = async () => {
      pagination.value.activities.page += 1
      await fetchRecentActivities(true)
    }

    const fetchTasks = async () => {
      loading.value.tasks = true
      try {
        const params = {
          page: pagination.value.tasks.page,
          pageSize: pagination.value.tasks.pageSize,
          status: 'PENDING'
        }
        const response = await teacherAPI.getTasks(params)
        if (response.data.code === 1) {
          pendingTasks.value = response.data.data.rows || []
        }
      } catch (error) {
        ElMessage.error('Failed to get tasks')
        console.error('Error fetching tasks:', error)
      } finally {
        loading.value.tasks = false
      }
    }

    const fetchStudents = async () => {
      loading.value.students = true
      try {
        const params = {
          page: pagination.value.students.page,
          pageSize: pagination.value.students.pageSize,
          ...searchFilters.value.students
        }
        const response = await teacherAPI.getStudents(params)
        if (response.data.code === 1) {
          students.value = response.data.data.rows
          pagination.value.students.total = response.data.data.total
        }
      } catch (error) {
        ElMessage.error('Failed to get student list')
        console.error('Error fetching students:', error)
      } finally {
        loading.value.students = false
      }
    }

    const fetchGroups = async () => {
      loading.value.groups = true
      try {
        const params = {
          page: pagination.value.groups.page,
          pageSize: pagination.value.groups.pageSize,
          ...searchFilters.value.groups
        }
        const response = await teacherAPI.getGroups(params)
        if (response.data.code === 1) {
          groups.value = response.data.data.rows
          pagination.value.groups.total = response.data.data.total
        }
      } catch (error) {
        ElMessage.error('Failed to get group list')
        console.error('Error fetching groups:', error)
      } finally {
        loading.value.groups = false
      }
    }

    const createGroup = async () => {
      if (!newGroup.value.name.trim()) {
        ElMessage.warning('Please enter group name')
        return
      }

      try {
        const groupData = {
          ...newGroup.value,
          teacherId: authStore.userId
        }
        const response = await teacherAPI.createGroup(groupData)
        if (response.data.code === 1) {
          ElMessage.success('Group created successfully')
          resetGroupForm()
          // 刷新小组列表
          await fetchGroups()
          // 刷新统计数据
          await fetchDashboardStats()
        }
      } catch (error) {
        ElMessage.error('Failed to create group')
        console.error('Error creating group:', error)
      }
    }

    const resetGroupForm = () => {
      newGroup.value = { name: '', description: '', members: [] }
      if (groupFormRef.value) {
        groupFormRef.value.resetFields()
      }
    }

    const getActivityType = type => {
      const typeMap = {
        student_joined: 'primary',
        assignment_submitted: 'success',
        group_created: 'warning',
        task_completed: 'info'
      }
      return typeMap[type] || 'primary'
    }

    // Computed properties
    const currentPageTitle = computed(() => {
      const titles = {
        dashboard: 'Dashboard',
        student: 'Student Management',
        groups: 'Groups',
        'create-groups': 'Create Groups'
      }
      return titles[activeMenu.value] || 'Dashboard'
    })

    // Event handlers
    const handleMenuSelect = index => {
      activeMenu.value = index
      // Load data based on selected menu
      if (index === 'student') {
        fetchStudents()
      } else if (index === 'groups') {
        fetchGroups()
      } else if (index === 'dashboard') {
        fetchDashboardStats()
        fetchRecentActivities()
        fetchTasks()
      }
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
        await ElMessageBox.confirm(
          'Are you sure you want to logout?',
          'Confirm Logout',
          {
            confirmButtonText: 'Yes',
            cancelButtonText: 'No',
            type: 'warning'
          }
        )

        await authStore.logout()
        ElMessage.success('Logged out successfully')
        window.location.href = '/'
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('Logout failed')
        }
      }
    }

    // Task handlers
    const completeTask = async task => {
      try {
        const response = await teacherAPI.completeTask(task.id)
        if (response.data.code === 1) {
          ElMessage.success(`Task "${task.title}" completed`)
          // 从列表中移除已完成的任务
          const index = pendingTasks.value.findIndex(t => t.id === task.id)
          if (index > -1) {
            pendingTasks.value.splice(index, 1)
          }
          // 刷新统计数据
          await fetchDashboardStats()
        }
      } catch (error) {
        ElMessage.error('Failed to complete task')
        console.error('Error completing task:', error)
      }
    }

    const viewDetails = task => {
      ElMessage.info(`View task details: ${task.title}`)
    }

    // New task handlers
    function openCreateTaskDialog(group) {
      createTaskForm.value = {
        title: '',
        description: '',
        cycle: 1,
        dueDate: '',
        groupId: group.id,
        fileUrl: '',
        selectedFile: null
      }
      createTaskDialogVisible.value = true
    }

    async function submitCreateTask() {
      createTaskFormRef.value.validate(async valid => {
        if (valid) {
          try {
            let fileUrl = createTaskForm.value.fileUrl

            // 如果有选择的文件但还没上传，先上传文件
            if (createTaskForm.value.selectedFile && !fileUrl) {
              ElMessage.info('Uploading file...')
              fileUrl = await uploadFileToOSS(createTaskForm.value.selectedFile)
              createTaskForm.value.fileUrl = fileUrl
            }

            await createTask({
              title: createTaskForm.value.title,
              description: createTaskForm.value.description,
              groupId: createTaskForm.value.groupId,
              dueDate: createTaskForm.value.dueDate,
              cycle: createTaskForm.value.cycle,
              fileUrl: fileUrl
            })
            ElMessage.success('Task created successfully')
            createTaskDialogVisible.value = false
            // 刷新任务列表
            await fetchTasks()
          } catch (e) {
            console.error('Task creation error:', e)
            ElMessage.error(
              'Task creation failed: ' + (e.message || 'Unknown error')
            )
          }
        }
      })
    }

    // 文件选择处理
    const handleFileChange = file => {
      console.log('File selected:', file)

      // 验证文件大小 (10MB限制)
      if (file.size > 10 * 1024 * 1024) {
        ElMessage.error('File size cannot exceed 10MB')
        return false
      }

      createTaskForm.value.selectedFile = file.raw
      createTaskForm.value.fileUrl = '' // 清除之前的上传URL
    }

    // 移除选择的文件
    const removeSelectedFile = () => {
      createTaskForm.value.selectedFile = null
      createTaskForm.value.fileUrl = ''
    }

    // 文件移除处理
    const handleFileRemove = () => {
      removeSelectedFile()
      return true
    }

    // 格式化文件大小
    const formatFileSize = bytes => {
      if (bytes === 0) return '0 Bytes'
      const k = 1024
      const sizes = ['Bytes', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
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

    // 预览文件
    const previewFile = async (fileUrl, fileName) => {
      try {
        previewFileUrl.value = fileUrl
        previewFileTitle.value = fileName
        previewFileContent.value = ''

        // 首先获取签名URL
        let signedUrl = fileUrl
        try {
          const response = await axios.post('/api/file/preview-url', fileUrl, {
            headers: {
              'Content-Type': 'text/plain',
              Authorization: `Bearer ${localStorage.getItem('authToken')}`
            }
          })
          if (response.data.code === 1) {
            signedUrl = response.data.data
            console.info('获取到签名URL:', signedUrl)
          }
        } catch (e) {
          console.warn('获取签名URL失败，使用原始URL:', e.message)
        }

        // 根据文件扩展名确定文件类型
        const extension = fileUrl.split('.').pop().toLowerCase()

        if (['pdf'].includes(extension)) {
          previewFileType.value = 'pdf'
          // 默认使用浏览器原生预览
          pdfViewerUrl.value = signedUrl
        } else if (['doc', 'docx'].includes(extension)) {
          previewFileType.value = 'office'
          // 默认使用Microsoft Office在线预览
          officeViewerUrl.value = `https://view.officeapps.live.com/op/embed.aspx?src=${encodeURIComponent(
            signedUrl
          )}`
        } else if (['xls', 'xlsx'].includes(extension)) {
          previewFileType.value = 'office'
          // Excel文档使用Office在线预览
          officeViewerUrl.value = `https://view.officeapps.live.com/op/embed.aspx?src=${encodeURIComponent(
            signedUrl
          )}`
        } else if (['ppt', 'pptx'].includes(extension)) {
          previewFileType.value = 'office'
          // PowerPoint文档使用Office在线预览
          officeViewerUrl.value = `https://view.officeapps.live.com/op/embed.aspx?src=${encodeURIComponent(
            signedUrl
          )}`
        } else if (
          ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'].includes(extension)
        ) {
          previewFileType.value = 'image'
        } else if (
          [
            'txt',
            'log',
            'md',
            'json',
            'xml',
            'yml',
            'yaml',
            'js',
            'ts',
            'css',
            'html'
          ].includes(extension)
        ) {
          previewFileType.value = 'text'
          // 对于文本文件，尝试获取内容
          try {
            const response = await axios.get(signedUrl, {
              responseType: 'text',
              timeout: 10000,
              headers: {
                'Cache-Control': 'no-cache'
              }
            })
            previewFileContent.value = response.data
          } catch (e) {
            console.warn('Failed to load text content:', e)
            previewFileContent.value =
              'Failed to load file content. Please download to view.'
          }
        } else {
          previewFileType.value = 'other'
        }

        filePreviewDialogVisible.value = true
      } catch (error) {
        console.error('Preview file error:', error)
        ElMessage.error('Failed to preview file')
      }
    }

    // 下载文件
    const downloadFile = () => {
      if (previewFileUrl.value) {
        // 创建一个临时的a标签来触发下载
        const link = document.createElement('a')
        link.href = previewFileUrl.value
        link.download = previewFileTitle.value
        link.target = '_blank'
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
      }
    }

    // 在新标签页中打开文件
    const openInNewTab = () => {
      // 使用代理URL以确保能够访问
      const proxyUrl = `/api/file/proxy?url=${encodeURIComponent(
        previewFileUrl.value
      )}`
      window.open(proxyUrl, '_blank')
    }

    // 使用Google文档查看器
    const useGoogleViewer = () => {
      const googleViewerUrl = `https://docs.google.com/gview?url=${encodeURIComponent(
        previewFileUrl.value
      )}&embedded=true`
      if (previewFileType.value === 'pdf') {
        pdfViewerUrl.value = googleViewerUrl
      } else {
        officeViewerUrl.value = googleViewerUrl
      }
    }

    // 使用Microsoft Office在线查看器
    const useMicrosoftViewer = () => {
      const msViewerUrl = `https://view.officeapps.live.com/op/embed.aspx?src=${encodeURIComponent(
        previewFileUrl.value
      )}`
      if (previewFileType.value === 'pdf') {
        pdfViewerUrl.value = msViewerUrl
      } else {
        officeViewerUrl.value = msViewerUrl
      }
    }

    // 使用直接预览（代理URL）
    const useDirectPreview = () => {
      const proxyUrl = `/api/file/proxy?url=${encodeURIComponent(
        previewFileUrl.value
      )}`
      if (previewFileType.value === 'pdf') {
        pdfViewerUrl.value = proxyUrl
      } else {
        officeViewerUrl.value = proxyUrl
      }
    }

    // 处理图片加载错误
    const handleImageError = event => {
      console.error('Image load error:', event)
      ElMessage.error('Failed to load image, please try downloading to view')
    }

    // 关闭文件预览
    const closeFilePreview = () => {
      filePreviewDialogVisible.value = false
      previewFileUrl.value = ''
      previewFileTitle.value = ''
      previewFileType.value = ''
      previewFileContent.value = ''
      pdfViewerUrl.value = ''
      officeViewerUrl.value = ''
    }

    // Check authentication and load initial data on mount
    onMounted(async () => {
      if (!authStore.isAuthenticated || !authStore.isTeacher) {
        ElMessage.error('Access denied. Teacher privileges required.')
        window.location.href = '/'
        return
      }

      // Load initial data
      await Promise.all([
        fetchDashboardStats(),
        fetchRecentActivities(),
        fetchTasks(),
        fetchStudents(),
        fetchGroups()
      ])
    })

    // Group members
    async function fetchGroupMembers(groupId) {
      if (!groupMembers.value[groupId]) {
        groupMembers.value[groupId] = null // 标记为加载中
        try {
          const members = await getGroupMembers(groupId)
          groupMembers.value[groupId] = members
        } catch (e) {
          groupMembers.value[groupId] = []
          ElMessage.error('Failed to get members')
        }
      }
    }

    // 在下拉展开时加载成员
    function handleGroupDropdownVisibleChange(visible, group) {
      if (visible) fetchGroupMembers(group.id)
    }

    // Group details command
    async function handleGroupDetailsCommand(cmd, group) {
      if (cmd === 'students') {
        try {
          currentGroupStudents.value = await getGroupMembers(group.id)
          groupStudentsDialogVisible.value = true
        } catch (e) {
          ElMessage.error('Failed to get members')
        }
      } else if (cmd === 'tasks') {
        try {
          currentGroupTasks.value = await getGroupTasks(group.id)
          groupTasksDialogVisible.value = true
        } catch (e) {
          ElMessage.error('Failed to get tasks')
        }
      }
    }

    // 远程搜索学生
    const handleSelectDropdown = visible => {
      if (visible) {
        fetchStudentList('')
      }
    }

    const handleStudentSearch = val => {
      if (searchStudentTimeout.value) clearTimeout(searchStudentTimeout.value)
      searchStudentTimeout.value = setTimeout(() => {
        fetchStudentList(val)
      }, 300)
    }

    const fetchStudentList = async name => {
      loading.value.searchStudent = true
      try {
        const resp = await teacherAPI.searchStudent(name)
        if (resp.data.code === 1) {
          searchStudentList.value = resp.data.data || []
        } else {
          searchStudentList.value = []
        }
      } catch (e) {
        searchStudentList.value = []
      } finally {
        loading.value.searchStudent = false
      }
    }

    // 批量导入学生
    const importStudentsDialogVisible = ref(false)
    const studentUploadRef = ref(null)
    const selectedStudentFile = ref(null)
    const importing = ref({ students: false })

    const showImportStudentsDialog = () => {
      selectedStudentFile.value = null
      if (studentUploadRef.value) {
        studentUploadRef.value.clearFiles()
      }
      importStudentsDialogVisible.value = true
    }

    const handleStudentFileChange = file => {
      selectedStudentFile.value = file.raw
    }

    const resetStudentUpload = () => {
      selectedStudentFile.value = null
      if (studentUploadRef.value) {
        studentUploadRef.value.clearFiles()
      }
    }

    const closeStudentDialog = () => {
      resetStudentUpload()
      importStudentsDialogVisible.value = false
    }

    const importStudents = async () => {
      if (!selectedStudentFile.value) {
        ElMessage.warning('Please select a file to import.')
        return
      }

      importing.value.students = true
      try {
        const response = await teacherAPI.batchImportStudents(
          selectedStudentFile.value
        )

        if (response.data && response.data.code === 1) {
          const result = response.data.data
          ElMessage.success(
            `Successfully imported ${result.success} out of ${
              result.success + result.errors.length
            } students`
          )

          if (result.errors.length > 0) {
            // 显示错误详情
            ElMessageBox.alert(
              `<div style="max-height: 300px; overflow-y: auto;">
                <p>Failed to import ${
                  result.errors.length
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

    // 批量导入小组
    const importGroupsDialogVisible = ref(false)
    const groupUploadRef = ref(null)
    const selectedGroupFile = ref(null)
    const importingGroups = ref({ groups: false })

    const showImportGroupsDialog = () => {
      selectedGroupFile.value = null
      if (groupUploadRef.value) {
        groupUploadRef.value.clearFiles()
      }
      importGroupsDialogVisible.value = true
    }

    const handleGroupFileChange = file => {
      selectedGroupFile.value = file.raw
    }

    const resetGroupUpload = () => {
      selectedGroupFile.value = null
      if (groupUploadRef.value) {
        groupUploadRef.value.clearFiles()
      }
    }

    const closeGroupDialog = () => {
      resetGroupUpload()
      importGroupsDialogVisible.value = false
    }

    const importGroups = async () => {
      if (!selectedGroupFile.value) {
        ElMessage.warning('Please select a file to import.')
        return
      }

      importingGroups.value.groups = true
      try {
        const response = await teacherAPI.batchImportGroups(
          selectedGroupFile.value
        )

        if (response.data && response.data.code === 1) {
          const result = response.data.data
          ElMessage.success(`Successfully imported ${result.success} group(s)`)

          if (result.errors && result.errors.length > 0) {
            // 显示错误详情
            ElMessageBox.alert(
              `<div style="max-height: 300px; overflow-y: auto;">
                <p>The following issues were encountered during import:</p>
                <ul>${result.errors.map(err => `<li>${err}</li>`).join('')}</ul>
              </div>`,
              'Import Results',
              {
                dangerouslyUseHTMLString: true,
                confirmButtonText: 'OK'
              }
            )
          }

          // 刷新小组列表
          fetchGroups()
          importGroupsDialogVisible.value = false
          resetGroupUpload()
        } else {
          ElMessage.error(response.data?.msg || 'Failed to import groups')
        }
      } catch (error) {
        console.error('Error importing groups:', error)
        ElMessage.error('Failed to import groups: ' + error.message)
      } finally {
        importingGroups.value.groups = false
      }
    }

    // 批量导入任务
    const importTasksDialogVisible = ref(false)
    const taskUploadRef = ref(null)
    const selectedTaskFile = ref(null)
    const importingTasks = ref({ tasks: false })

    const showImportTasksDialog = () => {
      selectedTaskFile.value = null
      if (taskUploadRef.value) {
        taskUploadRef.value.clearFiles()
      }
      importTasksDialogVisible.value = true
    }

    const handleTaskFileChange = file => {
      selectedTaskFile.value = file.raw
    }

    const resetTaskUpload = () => {
      selectedTaskFile.value = null
      if (taskUploadRef.value) {
        taskUploadRef.value.clearFiles()
      }
    }

    const closeTaskDialog = () => {
      resetTaskUpload()
      importTasksDialogVisible.value = false
    }

    const importTasks = async () => {
      if (!selectedTaskFile.value) {
        ElMessage.warning('Please select a file to import.')
        return
      }

      importingTasks.value.tasks = true
      try {
        const response = await teacherAPI.batchImportTasks(
          selectedTaskFile.value
        )

        if (response.data && response.data.code === 1) {
          const result = response.data.data
          ElMessage.success(`Successfully imported ${result.success} task(s)`)

          if (result.errors && result.errors.length > 0) {
            // 显示错误详情
            ElMessageBox.alert(
              `<div style="max-height: 300px; overflow-y: auto;">
                <p>The following issues were encountered during import:</p>
                <ul>${result.errors.map(err => `<li>${err}</li>`).join('')}</ul>
              </div>`,
              'Import Results',
              {
                dangerouslyUseHTMLString: true,
                confirmButtonText: 'OK'
              }
            )
          }

          // 刷新任务列表
          fetchTasks()
          importTasksDialogVisible.value = false
          resetTaskUpload()
        } else {
          ElMessage.error(response.data?.msg || 'Failed to import tasks')
        }
      } catch (error) {
        console.error('Error importing tasks:', error)
        ElMessage.error('Failed to import tasks: ' + error.message)
      } finally {
        importingTasks.value.tasks = false
      }
    }

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

    // 添加/编辑学生对话框
    const studentDialogVisible = ref(false)
    const studentDialogIsEdit = ref(false)
    const studentForm = ref({
      id: null,
      username: '',
      password: '',
      name: '',
      email: '',
      phone: '',
      studentNo: '',
      status: 'ACTIVE'
    })
    const studentFormRef = ref()
    const studentRules = {
      username: [
        { required: true, message: 'Please enter username', trigger: 'blur' }
      ],
      password: [
        { required: true, message: 'Please enter password', trigger: 'blur' }
      ],
      name: [{ required: true, message: 'Please enter name', trigger: 'blur' }],
      email: [
        {
          type: 'email',
          message: 'Please enter a valid email address',
          trigger: 'blur'
        }
      ],
      phone: [
        {
          pattern: /^1[3-9]\d{9}$/,
          message: 'Please enter a valid phone number',
          trigger: 'blur'
        }
      ],
      studentNo: [
        {
          required: true,
          message: 'Please enter student number',
          trigger: 'blur'
        }
      ]
    }

    const showAddStudentDialog = () => {
      studentDialogIsEdit.value = false
      studentForm.value = {
        id: null,
        username: '',
        password: '',
        name: '',
        email: '',
        phone: '',
        studentNo: '',
        status: 'ACTIVE'
      }
      studentDialogVisible.value = true
    }

    const showEditStudentDialog = async student => {
      studentDialogIsEdit.value = true
      studentForm.value = { ...student }
      studentDialogVisible.value = true
    }

    const submitStudentForm = async () => {
      studentFormRef.value.validate(async valid => {
        if (valid) {
          try {
            loading.value.submitStudent = true
            let response
            if (studentForm.value.id) {
              // 编辑学生
              response = await teacherAPI.updateStudent(
                studentForm.value.id,
                studentForm.value
              )
            } else {
              // 添加学生
              response = await teacherAPI.createStudent(studentForm.value)
            }

            if (response.data.code === 1) {
              ElMessage.success(
                `Student ${
                  studentForm.value.id ? 'updated' : 'created'
                } successfully`
              )
              studentDialogVisible.value = false
              fetchStudents() // 刷新学生列表
            } else {
              ElMessage.error(response.data?.msg || 'Failed to save student')
            }
          } catch (e) {
            console.error('Error submitting student form:', e)
            ElMessage.error(
              'Failed to save student: ' + (e.message || 'Unknown error')
            )
          } finally {
            loading.value.submitStudent = false
          }
        }
      })
    }

    return {
      authStore,
      activeMenu,
      loading,
      students,
      groups,
      recentActivities,
      pendingTasks,
      pagination,
      searchFilters,
      statistics,
      newGroup,
      currentPageTitle,
      handleMenuSelect,
      handleUserCommand,
      handleLogout,
      completeTask,
      viewDetails,
      createGroup,
      loadMoreActivities,
      groupRules,
      resetGroupForm,
      getActivityType,
      groupFormRef,
      createTaskDialogVisible,
      createTaskForm,
      createTaskRules,
      createTaskFormRef,
      openCreateTaskDialog,
      submitCreateTask,
      groupMembers,
      handleGroupDropdownVisibleChange,
      groupStudentsDialogVisible,
      groupTasksDialogVisible,
      currentGroupStudents,
      currentGroupTasks,
      handleGroupDetailsCommand,
      searchStudentList,
      studentSearchKeyword,
      handleStudentSearch,
      handleSelectDropdown,
      // File handling
      handleFileChange,
      removeSelectedFile,
      handleFileRemove,
      formatFileSize,
      uploadFileToOSS,
      // File preview
      filePreviewDialogVisible,
      previewFileUrl,
      previewFileTitle,
      previewFileType,
      previewFileContent,
      pdfViewerUrl,
      officeViewerUrl,
      previewFile,
      openInNewTab,
      useGoogleViewer,
      useMicrosoftViewer,
      useDirectPreview,
      handleImageError,
      downloadFile,
      closeFilePreview,
      // 批量导入学生
      importStudentsDialogVisible,
      showImportStudentsDialog,
      studentUploadRef,
      selectedStudentFile,
      importing,
      importStudents,
      handleStudentFileChange,
      resetStudentUpload,
      closeStudentDialog,
      // 批量导入小组
      importGroupsDialogVisible,
      showImportGroupsDialog,
      groupUploadRef,
      selectedGroupFile,
      importingGroups,
      importGroups,
      resetGroupUpload,
      handleGroupFileChange,
      closeGroupDialog,
      // 批量导入任务
      importTasksDialogVisible,
      showImportTasksDialog,
      taskUploadRef,
      selectedTaskFile,
      importingTasks,
      importTasks,
      resetTaskUpload,
      closeTaskDialog,
      handleTaskFileChange,
      // 处理学生分页变化
      handleStudentPageChange,
      // 处理学生每页数量变化
      handleStudentSizeChange,
      // 添加/编辑学生对话框
      studentDialogVisible,
      studentDialogIsEdit,
      studentForm,
      studentFormRef,
      studentRules,
      showAddStudentDialog,
      showEditStudentDialog,
      submitStudentForm
    }
  }
}
</script>

<style scoped>
.teacher-dashboard {
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
  margin-bottom: 20px;
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
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.charts-row {
  margin-bottom: 20px;
}

.chart-card {
  height: 400px;
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

.activities-container {
  height: 300px;
  overflow-y: auto;
}

.load-more-container {
  text-align: right;
  padding: 16px;
}

.tasks-list {
  height: 300px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.task-item {
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #fafafa;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.task-header h4 {
  margin: 0;
  color: #303133;
  font-size: 16px;
}

.priority {
  font-size: 12px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 4px;
  text-transform: uppercase;
}

.priority.high {
  background: #fef0f0;
  color: #f56c6c;
}

.priority.medium {
  background: #fdf6ec;
  color: #e6a23c;
}

.priority.low {
  background: #f0f9ff;
  color: #409eff;
}

.task-course {
  color: #409eff;
  font-size: 14px;
  margin: 4px 0;
}

.task-description {
  color: #606266;
  font-size: 14px;
  margin: 8px 0;
  line-height: 1.4;
}

.task-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
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

.create-group-form {
  max-width: 500px;
  margin: 0 auto;
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

.search-filters {
  margin-bottom: 20px;
}

.pagination-wrapper {
  margin-top: 20px;
  text-align: right;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #909399;
}

.empty-state p {
  margin: 0;
  font-size: 14px;
}

.import-dialog-content {
  padding: 20px;
}

.selected-file {
  margin-top: 15px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.el-upload__tip {
  line-height: 1.5;
  margin-top: 10px;
  text-align: left;
}

.el-upload__tip p {
  margin: 5px 0;
}

.el-upload__tip ul {
  margin: 5px 0;
  padding-left: 20px;
}

.el-upload__tip li {
  margin: 3px 0;
}

.el-upload-dragger {
  width: 100%;
}

.import-errors {
  max-height: 200px;
  overflow-y: auto;
  margin-top: 10px;
  padding: 10px;
  background-color: #fff1f0;
  border: 1px solid #ffccc7;
  border-radius: 4px;
}
</style>
