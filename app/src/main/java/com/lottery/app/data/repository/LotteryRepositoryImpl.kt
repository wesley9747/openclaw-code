package com.lottery.app.data.repository

import com.lottery.app.data.local.*
import com.lottery.app.domain.model.*
import com.lottery.app.domain.repository.LotteryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 彩票记录仓库实现
 */
@Singleton
class LotteryRepositoryImpl @Inject constructor(
    private val lotteryRecordDao: LotteryRecordDao
) : LotteryRepository {

    override fun getAllRecords(): Flow<List<LotteryRecord>> {
        return lotteryRecordDao.getAllRecords().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getRecordById(id: Long): Flow<LotteryRecord?> {
        return lotteryRecordDao.getRecordById(id).map { it?.toDomainModel() }
    }

    override fun getRecordByPeriod(period: String): Flow<LotteryRecord?> {
        return lotteryRecordDao.getRecordByPeriod(period).map { it?.toDomainModel() }
    }

    override suspend fun insertRecord(record: LotteryRecord): Long {
        return lotteryRecordDao.insert(LotteryRecordEntity.fromDomainModel(record))
    }

    override suspend fun updateRecord(record: LotteryRecord) {
        lotteryRecordDao.update(LotteryRecordEntity.fromDomainModel(record))
    }

    override suspend fun deleteRecord(id: Long) {
        lotteryRecordDao.delete(id)
    }

    override suspend fun deleteAllRecords() {
        lotteryRecordDao.deleteAll()
    }
}