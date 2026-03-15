import axios from '@/utils/axios'

export default {
  async feishuLogin(code) {
    return axios.post('/api/auth/feishu-login', { code })
  }
}
