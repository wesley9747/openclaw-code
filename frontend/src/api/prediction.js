import axios from '@/utils/axios'

export default {
  async generate(config) {
    return axios.post('/api/prediction/generate', config)
  },
  async save(prediction) {
    return axios.post('/api/prediction/save', prediction)
  }
}
