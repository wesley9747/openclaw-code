import axios from '@/utils/axios'

export default {
  async getLatest(lottery_type = '双色球', count = 10) {
    return axios.get('/api/draws/latest', { params: { lottery_type, count } })
  }
}
