package com.lottery.app.data.remote

import com.lottery.app.data.remote.dto.DrawResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 开奖数据 API 接口
 */
interface DrawApiService {
    /**
     * 获取双色球开奖结果
     * @param issueCount 开奖期数
     */
    @GET("ssq/getSsqData")
    suspend fun getSsqDraws(
        @Query("issueCount") issueCount: Int = 50
    ): DrawResponse
}