package com.lottery.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 开奖结果响应
 */
@JsonClass(generateAdapter = true)
data class DrawResponse(
    @Json(name = "code") val code: Int,
    @Json(name = "msg") val msg: String?,
    @Json(name = "data") val data: DrawData?
)

@JsonClass(generateAdapter = true)
data class DrawData(
    @Json(name = "lottery") val lottery: List<LotteryInfo>?,
    @Json(name = "total") val total: Int?
)

@JsonClass(generateAdapter = true)
data class LotteryInfo(
    @Json(name = "issue") val issue: String?,      // 期号
    @Json(name = "date") val date: String?,       // 开奖日期
    @Json(name = "red") val red: String?,         // 红球
    @Json(name = "blue") val blue: String?,       // 蓝球
    @Json(name = "prize") val prize: String?      // 奖池
)

/**
 * 解析转换工具
 */
fun LotteryInfo.toDrawResult(): com.lottery.app.domain.model.DrawResult? {
    try {
        val redBalls = red?.split(",")?.map { it.trim().toInt() } ?: return null
        val blue = blue?.toIntOrNull() ?: return null
        val issueNum = issue ?: return null
        
        // 解析日期
        val timestamp = parseDate(date)
        
        return com.lottery.app.domain.model.DrawResult(
            period = issueNum,
            drawDate = timestamp,
            redBalls = redBalls,
            blueBall = blue
        )
    } catch (e: Exception) {
        return null
    }
}

private fun parseDate(dateStr: String?): Long {
    if (dateStr == null) return System.currentTimeMillis()
    return try {
        // 格式: 2024-01-15
        val parts = dateStr.split("-")
        if (parts.size >= 3) {
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            val day = parts[2].toInt()
            java.util.Calendar.getInstance().apply {
                set(year, month - 1, day, 20, 0, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }.timeInMillis
        } else {
            System.currentTimeMillis()
        }
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
}