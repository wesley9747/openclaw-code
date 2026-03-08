package com.lottery.app.domain.model

/**
 * 双色球彩票记录
 */
data class LotteryRecord(
    val id: Long = 0,
    val period: String,           // 期号
    val purchaseDate: Long,       // 购买日期（时间戳）
    val redBalls: List<Int>,      // 红球号码 (6个, 1-33)
    val blueBall: Int,            // 蓝球号码 (1-16)
    val drawDate: Long? = null,   // 开奖日期
    val drawRedBalls: List<Int>? = null,   // 开奖红球
    val drawBlueBall: Int? = null,          // 开奖蓝球
    val matchedRedCount: Int = 0,   // 匹配红球数
    val matchedBlue: Boolean = false, // 是否匹配蓝球
    val prizeLevel: String? = null,  // 中奖等级
    val prizeAmount: Double? = null, // 中奖金额
    val isWin: Boolean = false,      // 是否中奖
    val remarks: String? = null,     // 备注
    val syncStatus: SyncStatus = SyncStatus.PENDING  // 同步状态
)

enum class SyncStatus {
    PENDING,
    SYNCED,
    FAILED
}

/**
 * 中奖等级定义
 */
object PrizeLevels {
    const val LEVEL_1 = "一等奖"     // 6+1
    const val LEVEL_2 = "二等奖"     // 6+0
    const val LEVEL_3 = "三等奖"     // 5+1
    const val LEVEL_4 = "四等奖"     // 5+0 或 4+1
    const val LEVEL_5 = "五等奖"     // 4+0 或 3+1
    const val LEVEL_6 = "六等奖"     // 2+1 或 1+1 或 0+1
    const val NONE = "未中奖"

    fun calculatePrizeLevel(matchedRed: Int, matchedBlue: Boolean): String {
        return when {
            matchedRed == 6 && matchedBlue -> LEVEL_1
            matchedRed == 6 -> LEVEL_2
            matchedRed == 5 && matchedBlue -> LEVEL_3
            matchedRed == 5 || (matchedRed == 4 && matchedBlue) -> LEVEL_4
            matchedRed == 4 || (matchedRed == 3 && matchedBlue) -> LEVEL_5
            matchedRed == 2 && matchedBlue || matchedRed == 1 && matchedBlue || matchedBlue -> LEVEL_6
            else -> NONE
        }
    }

    fun calculatePrizeAmount(level: String): Double {
        return when (level) {
            LEVEL_1 -> 10000000.0   // 1000万
            LEVEL_2 -> 500000.0     // 50万
            LEVEL_3 -> 3000.0
            LEVEL_4 -> 200.0
            LEVEL_5 -> 10.0
            LEVEL_6 -> 5.0
            else -> 0.0
        }
    }
}

/**
 * 预测推荐结果
 */
data class PredictionResult(
    val recommendedNumbers: List<RecommendedNumber>,
    val analysis: String,          // 分析说明
    val confidence: Float,         // 置信度 0-1
    val generatedAt: Long = System.currentTimeMillis()
)

data class RecommendedNumber(
    val redBalls: List<Int>,
    val blueBall: Int,
    val reason: String? = null
)

/**
 * 开奖数据
 */
data class DrawResult(
    val period: String,
    val drawDate: Long,
    val redBalls: List<Int>,
    val blueBall: Int
)

/**
 * 冷热号统计
 */
data class HotColdStatistics(
    val hotRedBalls: List<Pair<Int, Int>>,  // 号码, 出现次数
    val coldRedBalls: List<Pair<Int, Int>>,
    val hotBlueBalls: List<Pair<Int, Int>>,
    val coldBlueBalls: List<Pair<Int, Int>>
)