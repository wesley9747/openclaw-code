package com.lottery.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.lottery.app.domain.model.LotteryRecord
import com.lottery.app.domain.model.SyncStatus

/**
 * 彩票记录实体
 */
@Entity(tableName = "lottery_records")
@TypeConverters(LotteryConverters::class)
data class LotteryRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val period: String,
    val purchaseDate: Long,
    val redBalls: List<Int>,
    val blueBall: Int,
    val drawDate: Long? = null,
    val drawRedBalls: List<Int>? = null,
    val drawBlueBall: Int? = null,
    val matchedRedCount: Int = 0,
    val matchedBlue: Boolean = false,
    val prizeLevel: String? = null,
    val prizeAmount: Double? = null,
    val isWin: Boolean = false,
    val remarks: String? = null,
    val syncStatus: String = SyncStatus.PENDING.name
) {
    fun toDomainModel(): LotteryRecord = LotteryRecord(
        id = id,
        period = period,
        purchaseDate = purchaseDate,
        redBalls = redBalls,
        blueBall = blueBall,
        drawDate = drawDate,
        drawRedBalls = drawRedBalls,
        drawBlueBall = drawBlueBall,
        matchedRedCount = matchedRedCount,
        matchedBlue = matchedBlue,
        prizeLevel = prizeLevel,
        prizeAmount = prizeAmount,
        isWin = isWin,
        remarks = remarks,
        syncStatus = SyncStatus.valueOf(syncStatus)
    )

    companion object {
        fun fromDomainModel(record: LotteryRecord): LotteryRecordEntity = LotteryRecordEntity(
            id = record.id,
            period = record.period,
            purchaseDate = record.purchaseDate,
            redBalls = record.redBalls,
            blueBall = record.blueBall,
            drawDate = record.drawDate,
            drawRedBalls = record.drawRedBalls,
            drawBlueBall = record.drawBlueBall,
            matchedRedCount = record.matchedRedCount,
            matchedBlue = record.matchedBlue,
            prizeLevel = record.prizeLevel,
            prizeAmount = record.prizeAmount,
            isWin = record.isWin,
            remarks = record.remarks,
            syncStatus = record.syncStatus.name
        )
    }
}

/**
 * 开奖结果实体
 */
@Entity(tableName = "draw_results")
data class DrawResultEntity(
    @PrimaryKey
    val period: String,
    val drawDate: Long,
    val redBalls: String,  // 存储格式: "01,05,12,19,23,29"
    val blueBall: Int,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 类型转换器
 */
class LotteryConverters {
    @TypeConverter
    fun fromIntList(value: List<Int>): String = value.joinToString(",")

    @TypeConverter
    fun toIntList(value: String): List<Int> {
        if (value.isBlank()) return emptyList()
        return value.split(",").map { it.trim().toInt() }
    }
}