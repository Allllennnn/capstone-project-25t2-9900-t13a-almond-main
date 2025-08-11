import axios from 'axios'

export async function createTask(payload) {
  // payload: { title, description, groupId, dueDate }
  return axios.post('/api/task/add', payload)
}

export async function getGroupMembers(groupId) {
  // 假设后端有 /api/group/members?groupId=xxx
  const res = await axios.get(`/api/group/members`, { params: { groupId } })
  return res.data.data || []
}

export async function getGroupTasks(groupId) {
  // 修正：加 /api 前缀
  const res = await axios.get(`/api/task/by-group`, { params: { groupId } })
  return res.data.data || []
}
