import axios from '@/utils/axios'

export default {
  async recognize(imageBase64) {
    return axios.post('/api/ocr/recognize', { image_base64: imageBase64 })
  },
  async parseTickets(ocrResult) {
    return axios.post('/api/ocr/parse-tickets', { ocr_result: ocrResult })
  }
}
