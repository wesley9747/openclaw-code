<template>
  <div class="page home-page">
    <header class="header">
      <h1>🎱 彩票智查</h1>
      <van-icon name="user-circle-o" size="24" @click="goToSettings" />
    </header>

    <div class="stats-cards">
      <div class="stat-card">
        <div class="value">{{ stats.total_win || 0 }}</div>
        <div class="label">累计中奖(元)</div>
      </div>
      <div class="stat-card">
        <div class="value">{{ stats.total_bet || 0 }}</div>
        <div class="label">累计投注(元)</div>
      </div>
      <div class="stat-card">
        <div class="value" :class="stats.roi < 0 ? 'negative' : ''">{{ stats.roi?.toFixed(1) || 0 }}%</div>
        <div class="label">ROI</div>
      </div>
    </div>

    <div class="quick-actions">
      <van-grid :column-num="3" :border="false">
        <van-grid-item icon="photograph" text="拍照识别" to="/camera" />
        <van-grid-item icon="gem-o" text="智能预测" to="/prediction" />
        <van-grid-item icon="chart-trending-o" text="年度统计" to="/stats" />
      </van-grid>
    </div>

    <div class="recent-records">
      <div class="section-header">
        <h3>最近记录</h3>
        <router-link to="/records">查看全部</router-link>
      </div>
      <van-list v-if="records.length">
        <van-cell v-for="rec in records.slice(0, 5)" :key="rec.id" :title="rec.period" :label="formatDate(rec.created_at)">
          <template #icon>
            <div class="mini-balls">
              <span v-for="b in rec.red_balls.slice(0, 3)" :key="b" class="ball red">{{ b }}</span>
              <span class="ball blue">{{ rec.blue_ball }}</span>
            </div>
          </template>
          <template #value>
            <span :class="rec.is_win ? 'win' : 'lose'">
              {{ rec.is_win ? `+¥${rec.prize_amount}` : '未中奖' }}
            </span>
          </template>
        </van-cell>
      </van-list>
      <van-empty v-else description="暂无记录" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import api from '@/api/records'
import { showToast } from 'vant'

const router = useRouter()
const authStore = useAuthStore()

const stats = ref({})
const records = ref([])

const goToSettings = () => router.push('/settings')

const formatDate = (str) => new Date(str).toLocaleDateString()

onMounted(async () => {
  try {
    const [s, r] = await Promise.all([
      api.getStats(),
      api.list()
    ])
    stats.value = s
    records.value = r
  } catch (e) {
    showToast('加载失败')
  }
})
</script>

<style scoped>
.page { padding: 16px; padding-bottom: 60px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.header h1 { font-size: 24px; }
.stats-cards { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin-bottom: 16px; }
.stat-card { background: white; border-radius: 12px; padding: 16px; text-align: center; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.stat-card .value { font-size: 20px; font-weight: 700; color: #3370ff; }
.stat-card .value.negative { color: #f25340; }
.stat-card .label { font-size: 12px; color: #646666; margin-top: 4px; }
.quick-actions { margin-bottom: 16px; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.section-header a { font-size: 14px; color: #3370ff; }
.mini-balls { display: flex; gap: 4px; margin-right: 8px; }
.ball { width: 24px; height: 24px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 10px; color: white; }
.ball.red { background: linear-gradient(135deg, #ff6b6b, #ee5253); }
.ball.blue { background: linear-gradient(135deg, #3370ff, #2b5eb3); }
.win { color: #00b968; }
.lose { color: #646666; }
</style>
