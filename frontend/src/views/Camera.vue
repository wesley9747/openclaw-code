<template>
  <div class="page camera-page">
    <van-nav-bar title="拍照识别" left-arrow @click-left="goBack" />

    <div class="preview-area" ref="preview">
      <video v-if="stream" ref="video" autoplay class="video"></video>
      <div v-else class="placeholder">
        <van-icon name="photograph" size="64" color="#c8c9cc" />
        <p>点击下方按钮拍照</p>
      </div>
    </div>

    <div class="actions">
      <van-button type="primary" block @click="startCamera">开启相机</van-button>
      <van-button v-if="stream" type="success" block @click="takePhoto">拍照识别</van-button>
      <van-button v-if="imageData" type="warning" block @click="doOCR">识别号码</van-button>
    </div>

    <van-dialog v-model:show="showResult" title="识别结果" show-cancel-button @confirm="saveRecord">
      <div class="result-dialog">
        <p v-if="result.period">期号：{{ result.period }}</p>
        <p>号码：</p>
        <div class="balls-preview">
          <span v-for="b in result.red_balls" :key="b" class="ball red">{{ b }}</span>
          <span class="ball blue">{{ result.blue_ball }}</span>
        </div>
        <van-field v-model="result.period" label="期号" />
      </div>
    </van-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import api from '@/api/ocr'
import recordsApi from '@/api/records'
import { showToast, showDialog } from 'vant'

const router = useRouter()
const authStore = useAuthStore()

const video = ref(null)
const stream = ref(null)
const imageData = ref(null)
const showResult = ref(false)
const result = ref({ red_balls: [], blue_ball: '' })

const goBack = () => router.back()

const startCamera = async () => {
  try {
    const media = await navigator.mediaDevices.getUserMedia({ video: { facingMode: 'environment' } })
    stream.value = media
    video.value.srcObject = media
  } catch (e) {
    showToast('无法访问相机')
  }
}

const takePhoto = () => {
  const canvas = document.createElement('canvas')
  canvas.width = video.value.videoWidth
  canvas.height = video.value.videoHeight
  canvas.getContext('2d').drawImage(video.value, 0, 0)
  imageData.value = canvas.toDataURL('image/jpeg')
  showToast('拍照成功')
  // 停止相机
  stream.value.getTracks().forEach(t => t.stop())
  stream.value = null
}

const doOCR = async () => {
  try {
    const ocrRes = await api.recognize(imageData.value)
    const tickets = await api.parseTickets(ocrRes)
    if (tickets.length === 0) throw new Error('未识别到彩票号码')
    result.value = tickets[0]
    showResult.value = true
  } catch (e) {
    showToast('识别失败：' + e.message)
  }
}

const saveRecord = async () => {
  try {
    await recordsApi.create({
      period: result.value.period,
      lottery_type: result.value.lottery_type,
      red_balls: result.value.red_balls,
      blue_ball: result.value.blue_ball,
      bet_amount: 2
    })
    showToast('保存成功')
    router.push('/records')
  } catch (e) {
    showToast('保存失败')
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: #000; }
.preview-area { height: 60vh; background: #000; display: flex; align-items: center; justify-content: center; }
.video { width: 100%; height: 100%; object-fit: cover; }
.placeholder { color: #969799; text-align: center; }
.actions { padding: 16px; display: flex; flex-direction: column; gap: 12px; }
.result-dialog { padding: 16px; }
.balls-preview { display: flex; gap: 8px; margin: 8px 0; }
.ball { width: 32px; height: 32px; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: white; font-weight: 600; }
.ball.red { background: linear-gradient(135deg, #ff6b6b, #ee5253); }
.ball.blue { background: linear-gradient(135deg, #3370ff, #2b5eb3); }
</style>
