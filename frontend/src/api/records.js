import axios from '@/utils/axios'

export default {
  async create(data) {
    return axios.post('/api/records', data)
  },
  async list() {
    return axios.get('/api/records')
  },
  async get(id) {
    return axios.get(`/api/records/${id}`)
  },
  async remove(id) {
    return axios.delete(`/api/records/${id}`)
  }
}
