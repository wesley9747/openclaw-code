<template>
  <div class="page">
    <header class="header">
      <h1>🔮 智能预测</h1>
    </header>

    <div class="config-section">
      <van-cell-group title="预测设置">
        <van-field v-model="config.lotteryType" label="彩票类型" readonly @click="showTypePicker = true" />
        <van-field v-model="config.modelProvider" label="大模型" readonly @click="showProviderPicker = true" />
        <van-field v-model="config.modelId" label="模型 ID" placeholder="如 qwen-max" />
        <van-field v-model="config.apiKey" type="password" label="API Key" placeholder="输入您的 API Key" />
        <van-field name="slider" label="推荐组数" :value="config.groups" @input="config.groups = $event">
          <template #input>
            <van-slider v-model="config.groups" :min="1" :max="10" :step="1" button-size="16" />
          </template>
          <template #right>{{ config.groups }}组</template>
        </van-field>
      </van-cell-group>

      <van-button type="primary" block @click="generate" :loading="generating" style="margin-top: 16px;">
        🔮 生成推荐
      </van-button>
    </div>

    <div v-if="results.length" class="results-section">
      <h3>推荐结果</h3>
      <van-card v-for="(res, idx) in results" :key="idx" :title="`方案 ${idx + 1}`" :desc="res.reason">
        <template #tags>
          <van-tag plain type="primary">置信度 {{ res.confidence }}%</van-tag>
        </template>
        <template #footer>
          <div class="prediction-balls">
            <span v-for="b in res.red_balls" :key="b" class="ball red">{{ b }}</span>
            <span class="ball blue">{{ res.blue_ball }}</span>
          </div>
          <div class="action-btns">
            <van-button size="mini" @click="savePrediction(res)">保存</van-button>
            <van-button size="mini" type="primary" @click="sharePrediction(res)">分享</van-button>
          </div>
        </template>
      </van-card>
    </div>
  </div>

  <!-- 选择弹窗 -->
  <van-popup v-model:show="showTypePicker" position="bottom">
    <van-picker :columns="['双色球', '大乐透']" @confirm="onTypeConfirm" @cancel="showTypePicker = false" />
  </van-popup>
  <van-popup v-model:show="showProviderPicker" position="bottom">
    <van-picker :columns="providers" @confirm="onProviderConfirm" @cancel="showProviderPicker = false" />
  </van-popup>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useAuthStore } from '@/stores/auth'
import api from '@/api/prediction'
import { showToast } from 'vant'

const authStore = useAuthStore()

const config = reactive({
  lotteryType: '双色球',
  modelProvider: '通义千问',
  modelId: 'qwen-max',
  apiKey: '',
  groups: 5
})

const providers = ['通义千问', 'ChatGPT', 'DeepSeek', 'Claude']
const showTypePicker = ref(false)
const showProviderPicker = ref(false)
const generating = ref(false)
const results = ref([])

const onTypeConfirm = ({ selectedOptions }) => {
  config.lotteryType = selectedOptions[0].text
  showTypePicker.value = false
}
const onProviderConfirm = ({ selectedOptions }) => {
  config.modelProvider = selectedOptions[0].text
  showProviderPicker.value = false
}

const generate = async () => {
  if (!config.apiKey) {
    showToast('请填写 API Key')
    return
  }
  generating.value = true
  try {
    const res = await api.generate({
      ...config,
      user_id: authStore.user?.id
    })
    results.value = res.results
    showToast('生成成功')
  } catch (e) {
    showToast('生成失败：' + e.message)
  } finally {
    generating.value = false
  }
}

const savePrediction = async (pred) => {
  try {
    await api.save({ ...pred, lottery_type: config.lotteryType })
    showToast('已保存')
  } catch (e) {
    showToast('保存失败')
  }
}

const sharePrediction = (pred) => {
  const text = `【{{pred.lottery_type}}预测】${pred.red_balls.join(' ')} + ${pred.blue_ball}\n理由：${pred.reason}`
  navigator.clipboard.writeText(text)
  showToast('已复制到剪贴板')
}
</script>

<style scoped>
.page { padding-bottom: 60px; }
.header { padding: 16px; }
.header h1 { font-size: 24px; }
.config-section { padding: 16px; }
.prediction-balls { display: flex; gap: 8px; margin: 12px 0; }
.ball { width: 36px; height: 36px; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: white; font-weight: 700; }
.ball.red { background: linear-gradient(135deg, #ff6b6b, #ee5253); }
.ball.blue { background: linear-gradient(135deg, #3370ff, #2b5eb3); }
.action-btns { display: flex; gap: 8px; }
.results-section { padding: 16px; }
</style>
