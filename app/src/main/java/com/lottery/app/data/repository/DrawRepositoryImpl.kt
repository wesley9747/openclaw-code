package com.lottery.app.data.repository

import com.lottery.app.data.local.DrawResultDao
import com.lottery.app.data.local.DrawResultEntity
import com.lottery.app.domain.model.DrawResult
import com.lottery.app.domain.repository.DrawRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 开奖数据仓库实现（纯本地）
 */
@Singleton
class DrawRepositoryImpl @Inject constructor(
    private val drawResultDao: DrawResultDao
) : DrawRepository {

    override suspend fun getLatestDraw(): DrawResult? {
        return withContext(Dispatchers.IO) {
            drawResultDao.getLatestDraw()?.toDomainModel()
        }
    }

    override suspend fun getDrawByPeriod(period: String): DrawResult? {
        return withContext(Dispatchers.IO) {
            drawResultDao.getDrawByPeriod(period)?.toDomainModel()
        }
    }

    override suspend fun getRecentDraws(count: Int): List<DrawResult> {
        return withContext(Dispatchers.IO) {
            drawResultDao.getRecentDraws(count).mapNotNull { it.toDomainModel() }
        }
    }

    override suspend fun insertDraw(draw: DrawResult) {
        withContext(Dispatchers.IO) {
            drawResultDao.insert(draw.toEntity())
        }
    }

    override suspend fun insertAllDraws(draws: List<DrawResult>) {
        withContext(Dispatchers.IO) {
            val entities = draws.map { it.toEntity() }
            drawResultDao.insertAll(entities)
        }
    }

    override suspend fun getDrawCount(): Int {
        return withContext(Dispatchers.IO) {
            drawResultDao.getDrawCount()
        }
    }

    private fun DrawResultEntity.toDomainModel(): DrawResult {
        return DrawResult(
            period = period,
            drawDate = drawDate,
            redBalls = redBalls.split(",").map { it.trim().toInt() },
            blueBall = blueBall
        )
    }

    private fun DrawResult.toEntity(): DrawResultEntity {
        return DrawResultEntity(
            period = period,
            drawDate = drawDate,
            redBalls = redBalls.joinToString(","),
            blueBall = blueBall
        )
    }
}