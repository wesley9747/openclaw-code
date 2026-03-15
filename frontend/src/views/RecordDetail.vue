<template>
  <div class="page">
    <van-nav-bar title="记录详情" left-arrow @click-left="goBack" />

    <div v-if="record" class="content">
      <van-cell-group title="投注号码">
        <div class="balls-large">
          <span v-for="b in record.red_balls" :key="b" class="ball red">{{ b }}</span>
          <span class="ball blue">{{ record.blue_ball }}</span>
        </div>
      </van-cell-group>

      <van-cell-group title="开奖结果" v-if="record.draw_red_balls">
        <div class="balls-large">
          <span v-for="b in record.draw_red_balls" :key="b" class="ball red" :class="{ highlight: record.red_balls.includes(b) }">{{ b }}</span>
          <span class="ball blue" :class="{ highlight: record.blue_ball === record.draw_blue_ball }">{{ record.draw_blue_ball }}</span>
        </div>
      </van-cell-group>

      <van-cell-group title="中奖信息">
        <van-cell title="中奖状态" :value="record.is_win ? '中奖' : '未中奖'" :value-class="record.is_win ? 'win' : ''" />
        <van-cell v-if="record.is_win" title="奖级" :value="record.prize_level" />
        <van-cell v-if="record.is_win" title="奖金" :value="`¥${record.prize_amount}`" value-class="win" />
      </van-cell-group>

      <van-cell-group title="其他信息">
        <van-cell title="投注金额" :value="`¥${record.bet_amount}`" />
        <van-cell title="创建时间" :value="formatDate(record.created_at)" />
        <van-cell v-if="record.note" title="备注" :value="record.note" />
      </van-cell-group>

      <div class="actions">
        <van-button block type="danger" @click="deleteRecord">删除</van-button>
      </div>
    </div>

    <van-empty v-else description="记录不存在" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import recordsApi from '@/api/records'
import { showConfirmDialog, showToast } from 'vant'

const router = useRouter()
const route = useRoute()
const record = ref(null)

const goBack = () => router.back()

const formatDate = (str) => new Date(str).toLocaleString()

const load = async () => {
  try {
    record.value = await recordsApi.get(route.params.id)
  } catch (e) {
    showToast('加载失败')
  }
}

const deleteRecord = async () => {
  try {
    await showConfirmDialog({ title: '确认删除', message: '确定删除这条记录吗？' })
    await recordsApi.remove(record.value.id)
    showToast('已删除')
    router.back()
  } catch (e) {
    // 取消
  }
}

onMounted(load)
</script>

<style scoped>
.page { padding-bottom: 60px; }
.content { padding: 16px; }
.balls-large { display: flex; gap: 12px; justify-content: center; padding: 16px; }
.ball { width: 40px; height: 40px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 16px; font-weight: 700; color: white; box-shadow: 0 4px 12px rgba(0,0,0,0.15); }
.ball.red { background: linear-gradient(135deg, #ff6b6b, #ee5253); }
.ball.blue { background: linear-gradient(135deg, #3370ff, #2b5eb3); }
.ball.highlight { box-shadow: 0 0 0 4px #ffd700, 0 4px 12px rgba(0,0,0,0.15); }
.win { color: #00b968; }
.actions { padding: 16px; }
</style>
