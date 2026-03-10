package com.lottery.app.data.repository

import com.lottery.app.domain.model.LotteryRecord
import com.lottery.app.domain.model.SyncStatus
import com.lottery.app.domain.repository.FeishuRepository
import com.lottery.app.feishu.FishFlipper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 飞书同步仓库实现
 */
@Singleton
class FeishuRepositoryImpl @Inject constructor(
    private val fishFlipper: FishFlipper
) : FeishuRepository {

    override suspend fun syncToFeishu(records: List<LotteryRecord>): Result<Int> = withContext(Dispatchers.IO) {
        try {
            if (!checkConnection()) {
                return@withContext Result.failure(Exception("飞书连接失败"))
            }

            var syncedCount = 0
            records.forEach { record ->
                try {
                    fishFlipper.createLotteryRecord(record)
                    syncedCount++
                } catch (e: Exception) {
                    // 记录失败但继续
                }
            }
            Result.success(syncedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncFromFeishu(): Result<List<LotteryRecord>> = withContext(Dispatchers.IO) {
        try {
            if (!checkConnection()) {
                return@withContext Result.failure(Exception("飞书连接失败"))
            }

            val records = fishFlipper.getAllLotteryRecords()
            Result.success(records.map { it.copy(syncStatus = SyncStatus.SYNCED) })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkConnection(): Boolean {
        return fishFlipper.checkConnection()
    }
}