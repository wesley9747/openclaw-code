import axios from 'axios'

const instance = axios.create({
  baseURL: '/api',
  timeout: 30000
})

instance.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

instance.interceptors.response.use(
  res => res.data,
  err => Promise.reject(err)
)

export default instance
