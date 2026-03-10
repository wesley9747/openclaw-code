package com.lottery.app.domain.usecase

import com.lottery.app.domain.model.DrawResult
import com.lottery.app.domain.model.HotColdStatistics
import com.lottery.app.domain.model.LotteryRecord
import com.lottery.app.domain.model.PredictionResult
import com.lottery.app.domain.model.PrizeLevels
import com.lottery.app.domain.repository.DrawRepository
import com.lottery.app.domain.repository.LotteryRepository
import com.lottery.app.domain.repository.PredictionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 获取所有彩票记录
 */
class GetAllRecordsUseCase @Inject constructor(
    private val repository: LotteryRepository
) {
    operator fun invoke(): Flow<List<LotteryRecord>> = repository.getAllRecords()
}

/**
 * 根据ID获取彩票记录
 */
class GetRecordByIdUseCase @Inject constructor(
    private val repository: LotteryRepository
) {
    operator fun invoke(id: Long): Flow<LotteryRecord?> = repository.getRecordById(id)
}

/**
 * 保存彩票记录
 */
class SaveRecordUseCase @Inject constructor(
    private val repository: LotteryRepository
) {
    suspend operator fun invoke(record: LotteryRecord): Long {
        return if (record.id == 0L) {
            repository.insertRecord(record)
        } else {
            repository.updateRecord(record)
            record.id
        }
    }
}

/**
 * 删除彩票记录
 */
class DeleteRecordUseCase @Inject constructor(
    private val repository: LotteryRepository
) {
    suspend operator fun invoke(id: Long) = repository.deleteRecord(id)
}

/**
 * 检查中奖并更新记录
 */
class CheckWinStatusUseCase @Inject constructor(
    private val lotteryRepository: LotteryRepository,
    private val drawRepository: DrawRepository
) {
    suspend operator fun invoke(record: LotteryRecord): LotteryRecord {
        val drawResult = drawRepository.getDrawByPeriod(record.period)
            ?: return record

        val matchedRed = record.redBalls.count { it in drawResult.redBalls }
        val matchedBlue = record.blueBall == drawResult.blueBall

        val prizeLevel = PrizeLevels.calculatePrizeLevel(matchedRed, matchedBlue)
        val prizeAmount = if (prizeLevel != PrizeLevels.NONE) {
            PrizeLevels.calculatePrizeAmount(prizeLevel)
        } else null

        return record.copy(
            drawDate = drawResult.drawDate,
            drawRedBalls = drawResult.redBalls,
            drawBlueBall = drawResult.blueBall,
            matchedRedCount = matchedRed,
            matchedBlue = matchedBlue,
            prizeLevel = prizeLevel,
            prizeAmount = prizeAmount,
            isWin = prizeLevel != PrizeLevels.NONE
        ).also {
            lotteryRepository.updateRecord(it)
        }
    }
}

/**
 * 获取历史开奖数据
 */
class GetRecentDrawsUseCase @Inject constructor(
    private val drawRepository: DrawRepository
) {
    suspend operator fun invoke(count: Int = 50): List<DrawResult> {
        return drawRepository.getRecentDraws(count)
    }
}

/**
 * 获取冷热号统计
 */
class GetHotColdStatisticsUseCase @Inject constructor(
    private val predictionRepository: PredictionRepository
) {
    operator fun invoke(draws: List<DrawResult>): HotColdStatistics {
        return predictionRepository.getHotColdStatistics(draws)
    }
}

/**
 * 生成预测推荐（本地算法）
 */
class PredictWithLocalUseCase @Inject constructor(
    private val predictionRepository: PredictionRepository,
    private val drawRepository: DrawRepository
) {
    suspend operator fun invoke(): PredictionResult {
        val historyDraws = drawRepository.getRecentDraws(50)
        return predictionRepository.predictWithLocalAlgorithm(historyDraws)
    }
}

/**
 * 同步开奖数据
 */
class SyncDrawsUseCase @Inject constructor(
    private val drawRepository: DrawRepository
) {
    suspend operator fun invoke(): Result<Int> {
        return drawRepository.syncDrawsFromNetwork()
    }
}