import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    username: '',
    role: '',
    token: ''
  }),
  actions: {
    setUserInfo({ username, role, token }) {
      this.username = username
      this.role = role
      this.token = token
    },
    logout() {
      this.username = ''
      this.role = ''
      this.token = ''
    }
  }
})
