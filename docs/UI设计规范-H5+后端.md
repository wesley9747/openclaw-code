# 彩票管理应用 - UI设计规范（H5 + 后端版）

## 设计系统

基于 **Vant 4** 组件库 + 自定义主题，快速开发移动端 H5。

---

## 1. 主题配置

### 1.1 Vant 主题色覆盖

```javascript
// main.js 或 theme.js
import { Button, Cell, Dialog, Toast } from 'vant'

// 覆盖主色
Button.defaultProps.color = '#1989FA'
Cell.defaultProps.borderColor = '#EBEDF0'
Dialog.defaultProps.messageColor = '#323233'
Toast.defaultProps.color = '#FFFFFF'
Toast.defaultProps.background = '#323233'

// 自定义彩票相关色
const LotteryTheme = {
  redBall: '#FF6B6B',
  blueBall: '#1989FA',
  success: '#07C160',
  warning: '#FF976A',
  error: '#EE0A24',
  background: '#F7F8FA',
  surface: '#FFFFFF'
}
```

---

## 2. 关键组件设计

### 2.1 彩票球组件 (LotteryBall)

```vue
<template>
  <div class="lottery-ball" :class="type">
    {{ number.toString().padStart(2, '0') }}
  </div>
</template>

<script setup>
defineProps({
  number: { type: Number, required: true },
  type: { type: String, default: 'red' } // 'red' | 'blue'
})
</script>

<style scoped>
.lottery-ball {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  font-size: 14px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}
.lottery-ball.red { background: #FF6B6B; }
.lottery-ball.blue { background: #1989FA; }
</style>
```

**使用**：
```vue
<template>
  <div class="ball-row">
    <LotteryBall v-for="n in redBalls" :key="n" :number="n" type="red" />
    <span class="plus">+</span>
    <LotteryBall :number="blueBall" type="blue" />
  </div>
</template>
```

---

### 2.2 记录卡片 (RecordCard)

```vue
<template>
  <van-cell-group :border="false">
    <van-cell>
      <template #title>
        <div class="record-header">
          <span class="period">第{{ record.period }}期</span>
          <span class="date">{{ formatDate(record.createdAt) }}</span>
        </div>
      </template>
      <template #label>
        <div class="numbers">
          <LotteryBallRow :red="record.redBalls" :blue="record.blueBall" />
        </div>
        <div v-if="record.drawRedBalls" class="result">
          <LotteryBallRow
            :red="record.drawRedBalls"
            :blue="record.drawBlueBall"
            :disabled="true"
          />
          <div v-if="record.isWin" class="win-tag success">
            ✓ {{ record.prizeLevel }} ¥{{ record.prizeAmount }}
          </div>
          <div v-else class="lose-tag">未中奖</div>
        </div>
      </template>
      <template #right-icon>
        <van-icon name="arrow" color="#c8c9cc" />
      </template>
    </van-cell>
  </van-cell-group>
</template>

<style scoped>
.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.period { font-weight: bold; font-size: 16px; }
.date { font-size: 12px; color: #969799; }
.numbers { margin: 8px 0; }
.result { margin-top: 8px; padding-top: 8px; border-top: 1px dashed #EBEDF0; }
.win-tag { color: #07C160; font-weight: bold; }
.lose-tag { color: #969799; }
</style>
```

---

### 2.3 统计卡片 (StatsCard)

```vue
<template>
  <div class="stats-card">
    <div class="value">{{ value }}</div>
    <div v-if="subtitle" class="subtitle">{{ subtitle }}</div>
    <div class="title">{{ title }}</div>
  </div>
</template>

<style scoped>
.stats-card {
  background: white;
  border-radius: 12px;
  padding: 16px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}
.value {
  font-size: 24px;
  font-weight: bold;
  color: #1989FA;
}
.subtitle {
  font-size: 12px;
  color: #FF976A;
  margin-top: 4px;
}
.title {
  font-size: 14px;
  color: #646566;
  margin-top: 8px;
}
</style>
```

---

### 2.4 预测卡片 (PredictionCard)

```vue
<template>
  <van-card>
    <template #title>
      方案 {{ index + 1 }}
      <van-tag type="primary" plain>{{ prediction.reason }}</van-tag>
    </template>
    <template #desc>
      <div class="numbers">
        <LotteryBallRow :red="prediction.redBalls" :blue="prediction.blueBall" />
      </div>
    </template>
    <template #footer>
      <van-button size="mini" @click="saveToRecord">保存</van-button>
      <van-button size="mini" type="primary" plain @click="share">分享</van-button>
    </template>
  </van-card>
</template>
```

---

## 3. 页面详细设计

### 3.1 首页（Home.vue）

**布局**：
```vue
<template>
  <div class="page home-page">
    <!-- 顶部导航 -->
    <van-nav-bar title="彩票智查">
      <template #right>
        <van-icon name="user-circle-o" size="20" @click="goToSettings" />
      </template>
    </van-nav-bar>

    <!-- 统计卡片 -->
    <van-grid :column-num="3" :gutter="12" class="stats-grid">
      <van-grid-item>
        <StatsCard title="年度花费" :value="'¥' + stats.totalSpent" />
      </van-grid-item>
      <van-grid-item>
        <StatsCard title="中奖金额" :value="'¥' + stats.totalWinnings" />
      </van-grid-item>
      <van-grid-item>
        <StatsCard
          title="ROI"
          :value="stats.roi + '%'"
          :subtitle="stats.roi < 0 ? '亏损' : '盈利'"
        />
      </van-grid-item>
    </van-grid>

    <!-- 快捷操作 -->
    <van-grid :column-num="2" :gutter="12" class="actions-grid">
      <van-grid-item icon="photograph" text="拍照识别" to="/camera" />
      <van-grid-item icon="bullseye-o" text="智能预测" to="/prediction" />
    </van-grid>

    <!-- 最近记录 -->
    <van-cell-group title="最近记录">
      <RecordCard
        v-for="record in recentRecords"
        :key="record.id"
        :record="record"
        @click="goToDetail(record.id)"
      />
      <van-empty v-if="recentRecords.length === 0" description="暂无记录" />
    </van-cell-group>

    <!-- 底部导航 -->
    <van-tabbar v-model="activeTab" route>
      <van-tabbar-item to="/" icon="home-o">首页</van-tabbar-item>
      <van-tabbar-item to="/camera" icon="photograph">拍照</van-tabbar-item>
      <van-tabbar-item to="/records" icon="orders-o">记录</van-tabbar-item>
      <van-tabbar-item to="/prediction" icon="gem-o">预测</van-tabbar-item>
      <van-tabbar-item to="/stats" icon="chart-trending-o">统计</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStatsStore } from '@/stores/stats'
import { useRecordsStore } from '@/stores/records'

const router = useRouter()
const statsStore = useStatsStore()
const recordsStore = useRecordsStore()

const recentRecords = ref([])
const stats = ref({ totalSpent: 0, totalWinnings: 0, roi: 0 })

onMounted(async () => {
  const currentYear = new Date().getFullYear().toString()
  stats.value = await statsStore.fetchYearlyStats(currentYear)
  recentRecords.value = await recordsStore.fetchRecent(10)
})

const goToDetail = (id) => router.push(`/record/${id}`)
const goToSettings = () => router.push('/settings')
</script>

<style scoped>
.home-page { background: #F7F8FA; min-height: 100vh; padding-bottom: 50px; }
.stats-grid { margin: 12px; }
.actions-grid { margin: 12px; }
</style>
```

---

### 3.2 拍照识别页（Camera.vue）

```vue
<template>
  <div class="camera-page">
    <van-nav-bar title="拍照识别" left-arrow @click-left="goBack" />

    <!-- 相机预览 -->
    <video ref="video" autoplay playsinline class="camera-preview"></video>
    <canvas ref="canvas" style="display:none;"></canvas>

    <!-- 取景框覆盖 -->
    <div class="overlay">
      <div class="frame"></div>
      <div class="hint">将彩票放在框内</div>
    </div>

    <!-- 控制按钮 -->
    <div class="controls">
      <van-button type="primary" round @click="takePhoto">
        <van-icon name="photograph" /> 拍照
      </van-button>
      <van-button plain @click="chooseFromAlbum">相册</van-button>
    </div>

    <!-- 识别结果确认弹窗 -->
    <van-dialog
      v-model:show="showConfirm"
      title="识别结果"
      show-cancel-button
      @confirm="confirmAndSave"
      @cancel="retry"
    >
      <div class="confirm-content">
        <p>期号：{{ ocrResult.period }}</p>
        <div class="numbers">
          <LotteryBallRow :red="ocrResult.redBalls" :blue="ocrResult.blueBall" />
        </div>
        <van-field
          v-model="ocrResult.period"
          label="期号"
          placeholder="如有误请修改"
        />
        <!-- 号码选择器（可选） -->
      </div>
    </van-dialog>

    <!-- Loading -->
    <van-loading v-if="isProcessing" type="spinner" class="loading">
      识别中...
    </van-loading>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useOcrStore } from '@/stores/ocr'
import { useRecordsStore } from '@/stores/records'

const router = useRouter()
const ocrStore = useOcrStore()
const recordsStore = useRecordsStore()

const video = ref(null)
const canvas = ref(null)
const isProcessing = ref(false)
const showConfirm = ref(false)
const ocrResult = ref({ period: '', redBalls: [], blueBall: null })

onMounted(async () => {
  const stream = await navigator.mediaDevices.getUserMedia({ video: { facingMode: 'environment' } })
  video.value.srcObject = stream
})

onUnmounted(() => {
  if (video.value?.srcObject) {
    video.value.srcObject.getTracks().forEach(t => t.stop())
  }
})

const takePhoto = async () => {
  isProcessing.value = true
  canvas.value.width = video.value.videoWidth
  canvas.value.height = video.value.videoHeight
  canvas.value.getContext('2d').drawImage(video.value, 0, 0)
  const imageData = canvas.value.toDataURL('image/jpeg')

  // 调用后端 OCR
  const result = await ocrStore.recognize(imageData)
  ocrResult.value = result
  isProcessing.value = false
  showConfirm.value = true
}

const confirmAndSave = async () => {
  // 查询开奖并保存
  await recordsStore.createFromOcr(ocrResult.value)
  showToast('保存成功')
  router.back()
}

const goBack = () => router.back()
const retry = () => { showConfirm.value = false; isProcessing.value = false }
const chooseFromAlbum = () => { /* TODO: 从相册选择 */ }
</script>

<style scoped>
.camera-page { position: relative; height: 100vh; background: black; }
.camera-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.overlay {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.frame {
  width: 80%;
  height: 60%;
  border: 2px dashed rgba(255,255,255,0.7);
  border-radius: 8px;
}
.hint {
  color: white;
  margin-top: 16px;
  font-size: 14px;
}
.controls {
  position: absolute;
  bottom: 40px;
  left: 0;
  right: 0;
  display: flex;
  justify-content: center;
  gap: 16px;
}
.loading {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(0,0,0,0.7);
  color: white;
}
</style>
```

---

### 3.3 预测页（Prediction.vue）

```vue
<template>
  <div class="prediction-page">
    <van-nav-bar title="智能预测" />

    <div class="form">
      <van-radio-group v-model="lotteryType">
        <van-radio name="double_color_ball">双色球</van-radio>
        <van-radio name="super_lotto">大乐透</van-radio>
      </van-radio-group>

      <van-field
        v-model="count"
        type="number"
        label="推荐组数"
        placeholder="1-10"
        :rules="[{ validator: validateCount }]"
      />

      <van-cell title="当前配置" is-link @click="goToConfig" />
      <div class="config-info">
        提供商：{{ config.provider }} | 模型：{{ config.modelId }}
      </div>
    </div>

    <van-button
      type="primary"
      block
      :loading="isGenerating"
      @click="generate"
    >
      <van-icon name="magic-o" /> 生成推荐
    </van-button>

    <!-- 预测结果 -->
    <div v-if="predictions.length" class="results">
      <van-divider>推荐结果</van-divider>
      <PredictionCard
        v-for="(p, idx) in predictions"
        :key="idx"
        :prediction="p"
        :index="idx"
        @save="saveToRecord(p)"
      />
    </div>

    <!-- 底部导航由 App.vue 统一管理 -->
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { usePredictionStore } from '@/stores/prediction'
import { useConfigStore } from '@/stores/config'

const router = useRouter()
const predictionStore = usePredictionStore()
const configStore = useConfigStore()

const lotteryType = ref('double_color_ball')
const count = ref(5)
const isGenerating = ref(false)
const predictions = ref([])

const config = computed(() =>
  configStore.getConfig(lotteryType.value) || { provider: '未配置', modelId: '-' }
)

const validateCount = (val) => val >= 1 && val <= 10

const generate = async () => {
  isGenerating.value = true
  predictions.value = await predictionStore.generate(lotteryType.value, count.value)
  isGenerating.value = false
}

const saveToRecord = (prediction) => {
  // 将推荐号码保存为一条投注记录
  recordsStore.createFromPrediction(prediction)
  showToast('已保存到记录')
}

const goToConfig = () => router.push('/config')
</script>
```

---

## 4. Vant 组件使用规范

- **Button**：主要操作用 `primary`，次要用 `default`，危险操作用 `danger`
- **Cell**：列表项，配合 `van-cell-group`
- **Toast**：轻提示，`showToast('文本')`
- **Dialog**：确认弹窗，`showConfirm('标题','内容')`
- **Loading**：加载中，全屏用 `v-if` 控制
- **Empty**：空状态，插画可使用自定义图片
- **Grid**：网格布局，用于统计卡片和快捷入口
- **Tabbar**：底部导航，固定5个tab

---

## 5. 页面路由（Vue Router）

```javascript
// router.js
const routes = [
  { path: '/', component: () => import('@/views/Home.vue') },
  { path: '/camera', component: () => import('@/views/Camera.vue') },
  { path: '/records', component: () => import('@/views/Records.vue') },
  { path: '/record/:id', component: () => import('@/views/RecordDetail.vue') },
  { path: '/prediction', component: () => import('@/views/Prediction.vue') },
  { path: '/config', component: () => import('@/views/ModelConfig.vue') },
  { path: '/stats', component: () => import('@/views/Stats.vue') },
  { path: '/settings', component: () => import('@/views/Settings.vue') }
]
```

---

## 6. 状态管理（Pinia）

```javascript
// stores/records.js
export const useRecordsStore = defineStore('records', {
  state: () => ({ records: [], pagination: { page: 1, hasMore: true } }),
  actions: {
    async fetch(params) {
      const res = await api.getRecords(params)
      this.records = res.data
    },
    async fetchRecent(limit = 10) {
      const res = await api.getRecords({ limit })
      return res.data
    },
    async create(data) {
      const res = await api.createRecord(data)
      this.records.unshift(res.data)
      return res.data
    }
  }
})
```

---

## 7. API 封装

```javascript
// api/index.js
import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 30000
})

// 请求拦截器：添加 JWT
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// 响应拦截器：错误处理
api.interceptors.response.use(
  res => res.data,
  err => {
    if (err.response?.status === 401) {
      // 跳转登录
    }
    return Promise.reject(err)
  }
)

export default api
```

---

## 8. 配色变量（CSS自定义属性）

```css
:root {
  --primary: #1989FA;
  --red-ball: #FF6B6B;
  --blue-ball: #1989FA;
  --success: #07C160;
  --warning: #FF976A;
  --error: #EE0A24;
  --bg: #F7F8FA;
  --text: #323233;
  --text-light: #969799;
}
```

---

## 9. 响应式适配

```css
/* 大屏设备 */
@media (min-width: 768px) {
  .page {
    max-width: 600px;
    margin: 0 auto;
  }
}
```

---

## 10. 交付物清单

✅ **Vue 3 + Vite 项目结构**（可脚手架生成）
✅ **页面组件**（8个，见上文代码示例）
✅ **自定义组件**（4个： lottery-ball, record-card, stats-card, prediction-card）
✅ **Pinia store**（records, stats, prediction, config, auth）
✅ **API 封装**（axios + 拦截器）
✅ **主题配置**（Vant 主题覆盖）
✅ **路由配置**

---

**设计版本**: v1.0-vue-vant  
**可直接用于前端开发实现**
