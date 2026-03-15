<template>
  <div class="page">
    <header class="header">
      <h1>投注记录</h1>
    </header>

    <div class="filters">
      <van-tabs v-model:active="activeTab" sticky>
        <van-tab title="全部" name="all" />
        <van-tab title="已中奖" name="win" />
        <van-tab title="未中奖" name="lose" />
      </van-tabs>
    </div>

    <van-list v-if="filteredRecords.length" :loading="false" finished>
      <van-cell v-for="rec in filteredRecords" :key="rec.id" :title="rec.period" :label="formatDate(rec.created_at)" is-link @click="goDetail(rec.id)">
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
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import recordsApi from '@/api/records'
import { showToast } from 'vant'

const router = useRouter()
const activeTab = ref('all')
const allRecords = ref([])

const filteredRecords = computed(() => {
  if (activeTab.value === 'win') return allRecords.value.filter(r => r.is_win)
  if (activeTab.value === 'lose') return allRecords.value.filter(r => !r.is_win)
  return allRecords.value
})

const goDetail = (id) => router.push(`/records/${id}`)

const formatDate = (str) => new Date(str).toLocaleDateString()

onMounted(async () => {
  try {
    allRecords.value = await recordsApi.list()
  } catch (e) {
    showToast('加载失败')
  }
})
</script>

<style scoped>
.page { padding: 16px; padding-bottom: 60px; }
.header h1 { font-size: 24px; margin-bottom: 16px; }
.filters { margin-bottom: 16px; }
.mini-balls { display: flex; gap: 4px; margin-right: 8px; }
.ball { width: 24px; height: 24px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 10px; color: white; }
.ball.red { background: linear-gradient(135deg, #ff6b6b, #ee5253); }
.ball.blue { background: linear-gradient(135deg, #3370ff, #2b5eb3); }
.win { color: #00b968; }
.lose { color: #646666; }
</style>
