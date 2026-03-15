# 彩票管理应用 - UI设计规范（飞书应用版）

## 设计系统

基于 **Vant 4** 组件库，适配飞书移动端设计语言（飞书 Blue + 圆角卡片）。

---

## 1. 主题配置

```scss
// 飞书风格覆盖
$van-primary-color: #3370ff; // 飞书蓝
$van-success-color: #00b968;
$van-warning-color: #ff991f;
$van-danger-color: #f25340;
$van-text-color: #1d1e20;
$van-text-color-2: #646666;
$van-border-color: #e3e4e6;
$van-background-color: #f5f6f7;
$van-white: #ffffff;

// 彩票专属色
$lottery-red: #ff6b6b;
$lottery-blue: #3370ff;
```

---

## 2. 关键组件设计

### 2.1 彩票球 (LotteryBall)

```vue
<template>
  <div class="lottery-ball" :class="type">
    <span v-if="highlight" class="highlight-ring"></span>
    {{ number.toString().padStart(2, '0') }}
  </div>
</template>

<script setup>
defineProps({
  number: { type: Number, required: true },
  type: { type: String, default: 'red' }, // 'red' | 'blue'
  highlight: { type: Boolean, default: false } // 中奖号码高亮
})
</script>

<style scoped>
.lottery-ball {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 14px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.12);
  position: relative;
}
.lottery-ball.red { background: linear-gradient(135deg, #ff6b6b, #ee5253); }
.lottery-ball.blue { background: linear-gradient(135deg, #3370ff, #2b5eb3); }
.lottery-ball.highlight {
  box-shadow: 0 0 0 3px rgba(255, 215, 0, 0.6), 0 2px 6px rgba(0,0,0,0.12);
}
</style>
```

---

### 2.2 记录卡片 (RecordCard)

```vue
<template>
  <van-card>
    <template #title>
      <div class="period-row">
        <span>第{{ record.period }}期</span>
        <van-tag :type="record.isWin ? 'success' : 'default'">
          {{ record.isWin ? '中奖' : '未中奖' }}
        </van-tag>
      </div>
    </template>
    <template #desc>
      <div class="numbers">
        <LotteryBallRow
          :red="record.redBalls"
          :blue="record.blueBall"
          :disabled="!record.isWin"
        />
      </div>
      <div v-if="record.drawRedBalls" class="draw-result">
        <div class="label">开奖号码</div>
        <LotteryBallRow
          :red="record.drawRedBalls"
          :blue="record.drawBlueBall"
          :disabled="true"
          :highlight="true"
        />
      </div>
      <div v-if="record.isWin" class="prize-tag success">
        🎉 {{ record.prizeLevel }} ¥{{ record.prizeAmount }}
      </div>
    </template>
    <template #footer>
      <div class="footer-info">
        {{ formatDate(record.createdAt) }}
        <span v-if="record.betAmount > 2">(复式)</span>
      </div>
    </template>
  </van-card>
</template>
```

---

### 2.3 统计卡片 (StatsCard)

```vue
<template>
  <div class="stats-card">
    <div class="value" :class="{ negative: value < 0 }">
      {{ formattedValue }}
    </div>
    <div class="title">{{ title }}</div>
    <div v-if="subtitle" class="subtitle">{{ subtitle }}</div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  title: String,
  value: [Number, String],
  subtitle: String
})

const formattedValue = computed(() => {
  if (typeof props.value === 'number') {
    if (props.title.includes('ROI')) return props.value.toFixed(1) + '%'
    return '¥' + props.value.toLocaleString()
  }
  return props.value
})
</script>

<style scoped>
.stats-card {
  background: white;
  border-radius: 12px;
  padding: 16px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
.value {
  font-size: 22px;
  font-weight: 700;
  color: #3370ff;
}
.value.negative { color: #f25340; }
.title {
  font-size: 13px;
  color: #646666;
  margin-top: 8px;
}
.subtitle {
  font-size: 12px;
  color: #00b968;
  margin-top: 4px;
}
</style>
```

---

## 3. 页面设计（Vue组件）

### 3.1 App.vue（根组件）

```vue
<template>
  <div id="app" class="app-container">
    <router-view v-slot="{ Component, route }">
      <transition :name="transitionName" mode="out-in">
        <component :is="Component" :key="route.fullPath" />
      </transition>
    </router-view>

    <!-- 全局底部导航（首页、拍照、记录、预测、统计） -->
    <van-tabbar v-if="showTabbar" v-model="active" route>
      <van-tabbar-item to="/" icon="home-o">首页</van-tabbar-item>
      <van-tabbar-item to="/camera" icon="photograph">拍照</van-tabbar-item>
      <van-tabbar-item to="/records" icon="orders-o">记录</van-tabbar-item>
      <van-tabbar-item to="/prediction" icon="gem-o">预测</van-tabbar-item>
      <van-tabbar-item to="/stats" icon="chart-trending-o">统计</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const active = ref(0)

const showTabbar = computed(() => {
  const hideRoutes = ['/login', '/auth/feishu/callback', '/config']
  return !hideRoutes.includes(route.path)
})

const transitionName = ref('slide-left')
</script>

<style>
/* 全局样式重置 + 飞书字体 */
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

* { box-sizing: border-box; -webkit-tap-highlight-color: transparent; }
body {
  margin: 0;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background: #f5f6f7;
}

/* 页面过渡动画 */
.slide-left-enter-active, .slide-left-leave-active,
.slide-right-enter-active, .slide-right-leave-active {
  transition: all 0.25s ease;
}
.slide-left-enter-from { transform: translateX(100%); opacity: 0.8; }
.slide-left-leave-to { transform: translateX(-30%); opacity: 0.8; }
.slide-right-enter-from { transform: translateX(-100%); opacity: 0.8; }
.slide-right-leave-to { transform: translateX(30%); opacity: 0.8; }
</style>
```

---

### 3.2 登录页（Login.vue）

```vue
<template>
  <div class="login-page">
    <div class="brand">
      <h1>🎱 彩票智查</h1>
      <p>智能彩票管理与预测</p>
    </div>

    <div class="content">
      <p>使用飞书账号快速登录</p>
      <van-button
        type="primary"
        block
        size="large"
        @click="handleFeishuLogin"
      >
        <img src="@/assets/feishu-logo.svg" class="feishu-icon" />
        使用飞书登录
      </van-button>
    </div>

    <div class="footer">
      <p>Powered by 大龙虾 🦞</p>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const handleFeishuLogin = () => {
  // 重定向到飞书 OAuth
  const redirectUri = encodeURIComponent(import.meta.env.VITE_FEISHU_REDIRECT_URI)
  const state = Math.random().toString(36).substring(7)
  localStorage.setItem('oauth_state', state)
  const url = `${import.meta.env.VITE_FEISHU_OAUTH_URL}?redirect_uri=${redirectUri}&state=${state}`
  window.location.href = url
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #3370ff 0%, #5b8cff 100%);
  color: white;
  padding: 32px;
}
.brand h1 { font-size: 36px; margin-bottom: 8px; }
.brand p { font-size: 16px; opacity: 0.9; }
.content {
  width: 100%;
  max-width: 320px;
  margin-top: 48px;
  text-align: center;
  color: white;
}
.feishu-icon {
  width: 20px;
  height: 20px;
  margin-right: 8px;
  vertical-align: middle;
}
.footer {
  position: absolute;
  bottom: 32px;
  font-size: 12px;
  opacity: 0.7;
}
</style>
```

---

### 3.3 首页（Home.vue）同前，保持简洁

---

### 3.4 拍照页（Camera.vue）

重要：在飞书小程序中使用 `upload` 或 `chooseImage` API 代替 `getUserMedia` 以避免权限问题。

```vue
<template>
  <div class="camera-page">
    <van-nav-bar title="拍照识别" left-arrow @click-left="goBack" />

    <!-- 预览区域 -->
    <div class="preview-area">
      <img v-if="imageUrl" :src="imageUrl" class="preview-img" />
      <div v-else class="placeholder">
        <van-icon name="photograph" size="64" color="#c8c9cc" />
        <p>点击拍照或选择相册</p>
      </div>
    </div>

    <div class="actions">
      <!-- 使用飞书 API -->
      <van-button type="primary" block @click="takePhoto">
        拍照
      </van-button>
      <van-button plain block @click="chooseImage">
        相册选择
      </van-button>
    </div>

    <van-dialog
      v-model:show="showConfirm"
      title="识别结果"
      show-cancel-button
      @confirm="confirmAndSave"
    >
      <div class="confirm-content">
        <p>期号：<span>{{ ocrResult.period }}</span></p>
        <p>号码：</p>
        <LotteryBallRow :red="ocrResult.redBalls" :blue="ocrResult.blueBall" />
        <van-field v-model="ocrResult.period" label="期号" />
      </div>
    </van-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useOcrStore } from '@/stores/ocr'
import { useRecordsStore } from '@/stores/records'
import { showToast } from 'vant'

const router = useRouter()
const ocrStore = useOcrStore()
const recordsStore = useRecordsStore()

const imageUrl = ref('')
const showConfirm = ref(false)
const ocrResult = ref({ period: '', redBalls: [], blueBall: null })

const takePhoto = async () => {
  // 调用飞书摄像头 API（小程序）或 navigator.mediaDevices
  // 这里简化：获取 Base64
  const base64 = await capturePhoto()
  imageUrl.value = `data:image/jpeg;base64,${base64}`
  const result = await ocrStore.recognize(base64)
  ocrResult.value = result
  showConfirm.value = true
}

const chooseImage = async () => {
  // 飞书 chooseImage API 或 <input type="file">
}

const confirmAndSave = async () => {
  await recordsStore.createFromOcr(ocrResult.value)
  showToast('保存成功')
  router.back()
}

const goBack = () => router.back()
</script>

<style scoped>
.camera-page { min-height: 100vh; background: #f5f6f7; }
.preview-area {
  height: 60vh;
  background: #000;
  display: flex;
  align-items: center;
  justify-content: center;
}
.preview-img {
  max-width: 100%;
  max-height: 100%;
}
.placeholder { color: #969799; text-align: center; }
.actions { padding: 16px; display: flex; gap: 12px; }
.confirm-content { padding: 16px; }
</style>
```

---

## 4. 配色变量（同 H5 版）

```css
:root {
  --primary: #3370ff;       /* 飞书蓝 */
  --red-ball: #ff6b6b;
  --blue-ball: #3370ff;
  --success: #00b968;
  --warning: #ff991f;
  --error: #f25340;
  --bg: #f5f6f7;
  --surface: #ffffff;
  --text: #1d1e20;
  --text-light: #646666;
}
```

---

## 5. Vant 组件覆盖

```javascript
// theme.js
import { Button, Cell, Dialog, Toast, Tabbar } from 'vant'

// 圆角调整（飞书风格）
Button.defaultProps.round = true
Cell.defaultProps.borderColor = 'var(--border)'
Dialog.defaultProps.messageColor = 'var(--text)'

// 全局导航栏样式
const customNavBarStyle = {
  backgroundColor: '#ffffff',
  boxShadow: '0 1px 4px rgba(0,0,0,0.06)'
}
```

---

## 6. 飞书特定适配

- **状态栏高度**：safe-area-inset-top
- **底部安全区**：safe-area-inset-bottom（Tabbar）
- **字体**：使用系统字体（Inter、PingFang SC）

```css
.safe-area-inset-top { padding-top: env(safe-area-inset-top); }
.safe-area-inset-bottom { padding-bottom: env(safe-area-inset-bottom); }
```

---

**设计版本**: v1.0-feishu-app  
**可用于**: 前端开发
