package com.lottery.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * 彩票数据库
 */
@Database(
    entities = [LotteryRecordEntity::class, DrawResultEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(LotteryConverters::class)
abstract class LotteryDatabase : RoomDatabase() {
    abstract fun lotteryRecordDao(): LotteryRecordDao
    abstract fun drawResultDao(): DrawResultDao

    companion object {
        const val DATABASE_NAME = "lottery_database"
    }
}