<template>
  <div class="page">
    <header class="header">
      <h1>⚙️ 设置</h1>
    </header>

    <div class="content">
      <van-cell-group title="模型配置">
        <van-cell title="双色球模型" is-link @click="openConfig('双色球')" :value="configs['双色球']?.provider || '未设置'" />
        <van-cell title="大乐透模型" is-link @click="openConfig('大乐透')" :value="configs['大乐透']?.provider || '未设置'" />
      </van-cell-group>

      <van-cell-group title="通知设置">
        <van-cell title="开奖提醒">
          <template #right-icon>
            <van-switch v-model="notifications.draw" />
          </template>
        </van-cell>
        <van-cell title="中奖提醒">
          <template #right-icon>
            <van-switch v-model="notifications.win" />
          </template>
        </van-cell>
      </van-cell-group>

      <van-cell-group title="数据管理">
        <van-cell title="导出数据" is-link @click="exportData" />
        <van-cell title="清除缓存" is-link @click="clearCache" />
      </van-cell-group>

      <van-cell-group title="关于">
        <van-cell title="版本" value="1.0.0" />
        <van-cell title="作者" value="大龙虾 🦞" />
      </van-cell-group>

      <div class="logout-section">
        <van-button block type="danger" @click="logout">退出登录</van-button>
      </div>
    </div>

    <!-- 模型配置弹窗 -->
    <van-dialog v-model:show="showConfigDialog" :title="currentConfigType + ' 模型配置'" show-cancel-button @confirm="saveConfig">
      <div class="config-form">
        <van-field v-model="currentConfig.provider" label="提供商" placeholder="如：通义千问" />
        <van-field v-model="currentConfig.apiUrl" label="API地址" placeholder="https://dashscope..." />
        <van-field v-model="currentConfig.modelId" label="模型ID" placeholder="qwen-max" />
        <van-field v-model="currentConfig.apiKey" type="password" label="API Key" />
      </div>
    </van-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { Dialog, showToast } from 'vant'

const router = useRouter()
const authStore = useAuthStore()

const notifications = reactive({
  draw: true,
  win: true
})

const configs = reactive({
  '双色球': { provider: '通义千问', apiUrl: '', modelId: 'qwen-max', apiKey: '' },
  '大乐透': { provider: '', apiUrl: '', modelId: '', apiKey: '' }
})

const showConfigDialog = ref(false)
const currentConfigType = ref('')
const currentConfig = reactive({ provider: '', apiUrl: '', modelId: '', apiKey: '' })

const openConfig = (type) => {
  currentConfigType.value = type
  Object.assign(currentConfig, configs[type])
  showConfigDialog.value = true
}

const saveConfig = () => {
  configs[currentConfigType.value] = { ...currentConfig }
  showToast('已保存')
}

const exportData = async () => {
  // 调用后端导出所有数据
  showToast('功能开发中')
}

const clearCache = () => {
  Dialog.confirm({ title: '确认', message: '清除本地缓存？' }).then(() => {
    localStorage.clear()
    showToast('已清除')
  }).catch(() => {})
}

const logout = () => {
  authStore.logout()
  router.replace('/login')
}

onMounted(() => {
  const saved = localStorage.getItem('settings')
  if (saved) {
    const data = JSON.parse(saved)
    Object.assign(notifications, data.notifications)
    Object.assign(configs, data.configs)
  }
})

// 监听保存到 localStorage
window.addEventListener('beforeunload', () => {
  localStorage.setItem('settings', JSON.stringify({ notifications, configs }))
})
</script>

<style scoped>
.page { padding-bottom: 60px; }
.header { padding: 16px; }
.header h1 { font-size: 24px; }
.content { padding: 16px; }
.config-form { padding: 16px; }
.logout-section { margin-top: 32px; padding: 0 16px; }
</style>
