<template>
  <div class="page">
    <header class="header">
      <h1>📊 年度统计</h1>
      <van-dropdown-menu>
        <van-dropdown-item v-model="year" :options="yearOptions" @change="loadStats" />
      </van-dropdown-menu>
    </header>

    <div v-if="stats" class="content">
      <div class="summary-cards">
        <div class="card">
          <div class="value" :class="stats.total_win - stats.total_bet < 0 ? 'negative' : ''">
            ¥{{ (stats.total_win - stats.total_bet).toFixed(0) }}
          </div>
          <div class="label">净收益</div>
        </div>
        <div class="card">
          <div class="value">{{ stats.roi.toFixed(1) }}%</div>
          <div class="label">ROI</div>
        </div>
        <div class="card">
          <div class="value">{{ stats.win_count }}/{{ stats.record_count }}</div>
          <div class="label">中奖/总次数</div>
        </div>
      </div>

      <van-cell-group title="月度趋势">
        <div class="chart-container">
          <div class="bar-chart">
            <div v-for="(m, idx) in monthlyData" :key="idx" class="bar-item">
              <div class="bar" :style="{ height: m.height + '%' }">
                <span class="value">¥{{ m.bet }}</span>
              </div>
              <div class="label">{{ m.month }}</div>
            </div>
          </div>
        </div>
      </van-cell-group>

      <van-cell-group title="中奖分布">
        <van-cell v-for="prize in prizeDistribution" :key="prize.level" :title="prize.level" :value="`¥${prize.amount} (${prize.count}次)`" />
      </van-cell-group>

      <div class="export-section">
        <van-button block type="primary" @click="exportData">📤 导出数据</van-button>
      </div>
    </div>

    <van-empty v-else description="暂无统计数据" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import api from '@/api/stats'
import { showToast } from 'vant'

const authStore = useAuthStore()
const year = ref(new Date().getFullYear())
const yearOptions = Array.from({ length: 5 }, (_, i) => ({
  text: `${year.value - i}年`,
  value: year.value - i
}))
const stats = ref(null)

const monthlyData = computed(() => {
  if (!stats.value?.monthly) return []
  const max = Math.max(...stats.value.monthly.map(m => m.bet))
  return stats.value.monthly.map(m => ({
    ...m,
    height: (m.bet / max) * 100
  }))
})

const prizeDistribution = computed(() => {
  if (!stats.value?.prizes) return []
  return stats.value.prizes
})

const loadStats = async () => {
  try {
    stats.value = await api.getSummary(year.value)
  } catch (e) {
    showToast('加载失败')
  }
}

const exportData = async () => {
  try {
    const data = await api.export()
    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `lottery-stats-${year.value}.json`
    a.click()
    URL.revokeObjectURL(url)
    showToast('导出成功')
  } catch (e) {
    showToast('导出失败')
  }
}

onMounted(loadStats)
</script>

<style scoped>
.page { padding-bottom: 60px; }
.header { display: flex; justify-content: space-between; align-items: center; padding: 16px; }
.header h1 { font-size: 24px; }
.summary-cards { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin: 16px; }
.card { background: white; border-radius: 12px; padding: 16px; text-align: center; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.card .value { font-size: 20px; font-weight: 700; color: #3370ff; }
.card .value.negative { color: #f25340; }
.card .label { font-size: 12px; color: #646666; margin-top: 4px; }
.chart-container { padding: 16px; }
.bar-chart { display: flex; align-items: flex-end; height: 120px; gap: 8px; margin-top: 12px; }
.bar-item { flex: 1; display: flex; flex-direction: column; align-items: center; }
.bar { width: 100%; background: linear-gradient(to top, #3370ff, #5b8cff); border-radius: 4px 4px 0 0; transition: height 0.3s; position: relative; }
.bar .value { position: absolute; top: -24px; left: 50%; transform: translateX(-50%); font-size: 10px; color: #646666; white-space: nowrap; }
.bar-item .label { font-size: 10px; color: #646666; margin-top: 4px; }
.export-section { padding: 16px; }
</style>
