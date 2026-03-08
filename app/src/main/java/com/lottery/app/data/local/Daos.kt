package com.lottery.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 彩票记录 DAO
 */
@Dao
interface LotteryRecordDao {
    @Query("SELECT * FROM lottery_records ORDER BY purchaseDate DESC")
    fun getAllRecords(): Flow<List<LotteryRecordEntity>>

    @Query("SELECT * FROM lottery_records WHERE id = :id")
    fun getRecordById(id: Long): Flow<LotteryRecordEntity?>

    @Query("SELECT * FROM lottery_records WHERE period = :period LIMIT 1")
    fun getRecordByPeriod(period: String): Flow<LotteryRecordEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: LotteryRecordEntity): Long

    @Update
    suspend fun update(record: LotteryRecordEntity)

    @Query("DELETE FROM lottery_records WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM lottery_records")
    suspend fun deleteAll()

    @Query("SELECT * FROM lottery_records WHERE isWin = 1")
    fun getWinningRecords(): Flow<List<LotteryRecordEntity>>

    @Query("SELECT SUM(prizeAmount) FROM lottery_records WHERE isWin = 1")
    fun getTotalWinnings(): Flow<Double?>
}

/**
 * 开奖结果 DAO
 */
@Dao
interface DrawResultDao {
    @Query("SELECT * FROM draw_results ORDER BY drawDate DESC")
    fun getAllDraws(): Flow<List<DrawResultEntity>>

    @Query("SELECT * FROM draw_results ORDER BY drawDate DESC LIMIT :count")
    suspend fun getRecentDraws(count: Int): List<DrawResultEntity>

    @Query("SELECT * FROM draw_results WHERE period = :period LIMIT 1")
    suspend fun getDrawByPeriod(period: String): DrawResultEntity?

    @Query("SELECT * FROM draw_results ORDER BY drawDate DESC LIMIT 1")
    suspend fun getLatestDraw(): DrawResultEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(draw: DrawResultEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(draws: List<DrawResultEntity>)

    @Query("SELECT COUNT(*) FROM draw_results")
    suspend fun getDrawCount(): Int
}