import axios from '@/utils/axios'

export default {
  async getSummary(year = new Date().getFullYear()) {
    const res = await axios.get('/api/stats/summary')
    return res
  },
  async export() {
    return axios.get('/api/stats/export')
  }
}
