package com.lottery.app.domain.repository

import com.lottery.app.domain.model.DrawResult
import com.lottery.app.domain.model.HotColdStatistics
import com.lottery.app.domain.model.LotteryRecord
import com.lottery.app.domain.model.PredictionResult
import kotlinx.coroutines.flow.Flow

/**
 * 彩票记录仓库接口
 */
interface LotteryRepository {
    fun getAllRecords(): Flow<List<LotteryRecord>>
    fun getRecordById(id: Long): Flow<LotteryRecord?>
    fun getRecordByPeriod(period: String): Flow<LotteryRecord?>
    suspend fun insertRecord(record: LotteryRecord): Long
    suspend fun updateRecord(record: LotteryRecord)
    suspend fun deleteRecord(id: Long)
    suspend fun deleteAllRecords()
}

/**
 * 开奖数据仓库接口
 */
interface DrawRepository {
    suspend fun getLatestDraw(): DrawResult?
    suspend fun getDrawByPeriod(period: String): DrawResult?
    suspend fun getRecentDraws(count: Int): List<DrawResult>
    suspend fun insertDraw(draw: DrawResult)
    suspend fun insertAllDraws(draws: List<DrawResult>)
    suspend fun getDrawCount(): Int
}

/**
 * 预测服务接口
 */
interface PredictionRepository {
    suspend fun predictWithLocalAlgorithm(historyDraws: List<DrawResult>): PredictionResult
    fun getHotColdStatistics(draws: List<DrawResult>): HotColdStatistics
}