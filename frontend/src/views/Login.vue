<template>
  <div class="login-page">
    <div class="brand">
      <h1>🎱 彩票智查</h1>
      <p>智能彩票管理与预测</p>
    </div>
    <div class="content">
      <p>使用飞书账号快速登录</p>
      <van-button type="primary" block size="large" @click="handleLogin">
        使用飞书登录
      </van-button>
      <p class="hint">（开发模式：点击任意账户登录）</p>
    </div>
    <div class="footer">Powered by 大龙虾 🦞</div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { showToast } from 'vant'

const router = useRouter()
const authStore = useAuthStore()

const handleLogin = async () => {
  try {
    // 开发模式：直接调用 mock 登录，不需要真实 code
    const res = await authStore.login('mock-code')
    showToast('登录成功')
    router.replace('/home')
  } catch (e) {
    showToast('登录失败')
  }
}
</script>

<style scoped>
.login-page { min-height: 100vh; display: flex; flex-direction: column; align-items: center; justify-content: center; background: linear-gradient(135deg, #3370ff 0%, #5b8cff 100%); color: white; padding: 32px; }
.brand h1 { font-size: 36px; margin-bottom: 8px; }
.brand p { font-size: 16px; opacity: 0.9; }
.content { width: 100%; max-width: 320px; margin-top: 48px; text-align: center; color: white; }
.hint { font-size: 12px; margin-top: 12px; opacity: 0.8; }
.footer { position: absolute; bottom: 32px; font-size: 12px; opacity: 0.7; }
</style>
