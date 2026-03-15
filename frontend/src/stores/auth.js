import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token'))
  const user = token.value ? JSON.parse(localStorage.getItem('user')) : null

  const setToken = (t) => {
    token.value = t
    localStorage.setItem('token', t)
  }
  const setUser = (u) => {
    user.value = u
    localStorage.setItem('user', JSON.stringify(u))
  }
  const logout = () => {
    token.value = null
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  const login = async (code) => {
    const res = await api.feishuLogin(code)
    setToken(res.access_token)
    setUser(res.user)
    return res
  }

  return { token, user, setToken, setUser, logout, login }
})
