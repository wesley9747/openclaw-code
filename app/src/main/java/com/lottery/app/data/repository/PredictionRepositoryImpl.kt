package com.lottery.app.data.repository

import com.lottery.app.domain.model.*
import com.lottery.app.domain.repository.PredictionRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * 预测服务实现 - 本地算法
 */
@Singleton
class PredictionRepositoryImpl @Inject constructor() : PredictionRepository {

    override suspend fun predictWithAI(historyDraws: List<DrawResult>): Result<PredictionResult> {
        // 通过浏览器调用千问网页版 - 在 Presentation 层实现
        return Result.failure(NotImplementedError("Use PredictionUseCase with Browser"))
    }

    override suspend fun predictWithLocalAlgorithm(historyDraws: List<DrawResult>): PredictionResult {
        if (historyDraws.isEmpty()) {
            return generateRandomPrediction()
        }

        val stats = getHotColdStatistics(historyDraws)
        
        // 使用多种策略生成推荐
        val predictions = mutableListOf<RecommendedNumber>()
        
        // 策略1: 热门号码 + 冷门蓝球
        predictions.add(generateFromHotCold(stats))
        
        // 策略2: 奇偶平衡
        predictions.add(generateFromParity(historyDraws))
        
        // 策略3: 邻号分析
        predictions.add(generateFromNeighbors(historyDraws))
        
        // 策略4: 余数分布
        predictions.add(generateFromRemainder(historyDraws))
        
        // 策略5: 随机组合
        predictions.add(generateFromRandom(stats))
        
        return PredictionResult(
            recommendedNumbers = predictions,
            analysis = buildAnalysisText(stats, historyDraws),
            confidence = 0.6f + Random.nextFloat() * 0.2f
        )
    }

    override fun getHotColdStatistics(draws: List<DrawResult>): HotColdStatistics {
        if (draws.isEmpty()) {
            return HotColdStatistics(
                hotRedBalls = emptyList(),
                coldRedBalls = emptyList(),
                hotBlueBalls = emptyList(),
                coldBlueBalls = emptyList()
            )
        }

        // 计算红球出现次数（越近期的权重越高）
        val redCountMap = mutableMapOf<Int, Int>()
        val blueCountMap = mutableMapOf<Int, Int>()
        
        draws.forEachIndexed { index, draw ->
            val weight = draws.size - index // 越近期的权重越高
            draw.redBalls.forEach { ball ->
                redCountMap[ball] = (redCountMap[ball] ?: 0) + weight
            }
            blueCountMap[draw.blueBall] = (blueCountMap[draw.blueBall] ?: 0) + weight
        }

        val hotRed = redCountMap.entries.sortedByDescending { it.value }.take(10).map { it.key to it.value }
        val coldRed = redCountMap.entries.sortedBy { it.value }.take(10).map { it.key to it.value }
        val hotBlue = blueCountMap.entries.sortedByDescending { it.value }.take(5).map { it.key to it.value }
        val coldBlue = blueCountMap.entries.sortedBy { it.value }.take(5).map { it.key to it.value }

        return HotColdStatistics(
            hotRedBalls = hotRed,
            coldRedBalls = coldRed,
            hotBlueBalls = hotBlue,
            coldBlueBalls = coldBlue
        )
    }

    private fun generateFromHotCold(stats: HotColdStatistics): RecommendedNumber {
        val hotReds = stats.hotRedBalls.take(15).map { it.first }
        val coldBlues = stats.coldBlueBalls.take(3).map { it.first }
        
        val selectedReds = mutableListOf<Int>()
        val available = hotReds.shuffled().toMutableList()
        
        while (selectedReds.size < 6 && available.isNotEmpty()) {
            selectedReds.add(available.removeAt(0))
        }
        
        // 如果不够6个，随机补充
        while (selectedReds.size < 6) {
            val num = Random.nextInt(1, 34)
            if (num !in selectedReds) {
                selectedReds.add(num)
            }
        }
        
        val blueBall = if (coldBlues.isNotEmpty()) {
            coldBlues.random()
        } else {
            Random.nextInt(1, 17)
        }
        
        return RecommendedNumber(
            redBalls = selectedReds.sorted(),
            blueBall = blueBall,
            reason = "热门号码 + 冷门蓝球策略"
        )
    }

    private fun generateFromParity(historyDraws: List<DrawResult>): RecommendedNumber {
        // 分析历史奇偶比例
        val recentDraws = historyDraws.take(10)
        
        var oddCount = 0
        recentDraws.forEach { draw ->
            oddCount += draw.redBalls.count { it % 2 == 1 }
        }
        val avgOdd = oddCount.toFloat() / (recentDraws.size * 6)
        
        // 选择合适的奇偶比例
        val targetOdd = if (avgOdd > 0.5) 3 else 4
        
        val reds = mutableListOf<Int>()
        var oddAdded = 0
        
        // 先尝试添加符合目标的奇数
        while (reds.size < 6) {
            val num = Random.nextInt(1, 34)
            if (num !in reds) {
                if ((num % 2 == 1 && oddAdded < targetOdd) || oddAdded >= targetOdd) {
                    reds.add(num)
                    if (num % 2 == 1) oddAdded++
                }
            }
        }
        
        return RecommendedNumber(
            redBalls = reds.sorted(),
            blueBall = Random.nextInt(1, 17),
            reason = "奇偶平衡策略"
        )
    }

    private fun generateFromNeighbors(historyDraws: List<DrawResult>): RecommendedNumber {
        if (historyDraws.size < 3) {
            return generateRandomPrediction().recommendedNumbers.first()
        }
        
        val recent = historyDraws.take(3).flatMap { it.redBalls }
        val neighbors = recent.flatMap { listOf(it - 1, it + 1) }.filter { it in 1..33 }.distinct()
        
        val reds = mutableListOf<Int>()
        val available = neighbors.shuffled().toMutableList()
        
        while (reds.size < 6 && available.isNotEmpty()) {
            reds.add(available.removeAt(0))
        }
        
        while (reds.size < 6) {
            val num = Random.nextInt(1, 34)
            if (num !in reds) reds.add(num)
        }
        
        return RecommendedNumber(
            redBalls = reds.sorted(),
            blueBall = Random.nextInt(1, 17),
            reason = "邻号分析策略"
        )
    }

    private fun generateFromRemainder(historyDraws: List<DrawResult>): RecommendedNumber {
        // 余数分析 - 均匀分布
        val reds = mutableSetOf<Int>()
        
        for (mod in 0..5) {
            val candidates = (1..33).filter { it % 6 == mod }
            if (candidates.isNotEmpty()) {
                val selected = candidates.random()
                if (selected !in reds) reds.add(selected)
            }
        }
        
        while (reds.size < 6) {
            val num = Random.nextInt(1, 34)
            reds.add(num)
        }
        
        return RecommendedNumber(
            redBalls = reds.toList().sorted(),
            blueBall = Random.nextInt(1, 17),
            reason = "余数均匀分布策略"
        )
    }

    private fun generateFromRandom(stats: HotColdStatistics): RecommendedNumber {
        val allReds = (1..33).toMutableList()
        allReds.shuffle()
        
        val reds = allReds.take(6).sorted()
        val blueBall = Random.nextInt(1, 17)
        
        return RecommendedNumber(
            redBalls = reds,
            blueBall = blueBall,
            reason = "随机组合策略"
        )
    }

    private fun generateRandomPrediction(): PredictionResult {
        val predictions = (1..5).map { generateFromRandom(HotColdStatistics(emptyList(), emptyList(), emptyList(), emptyList())) }
        
        return PredictionResult(
            recommendedNumbers = predictions,
            analysis = "基于随机算法的推荐",
            confidence = 0.5f
        )
    }

    private fun buildAnalysisText(stats: HotColdStatistics, history: List<DrawResult>): String {
        val hotRedStr = stats.hotRedBalls.take(5).joinToString(", ") { "${it.first}(${it.second})" }
        val coldRedStr = stats.coldRedBalls.take(5).joinToString(", ") { "${it.first}(${it.second})" }
        
        return """
            📊 数据分析（基于最近 ${history.size} 期开奖）
            
            🔥 热门红球：$hotRedStr
            ❄️ 冷门红球：$coldRedStr
            
            预测策略说明：
            1. 热门号码+冷门蓝球：选择近期出现频率高的号码
            2. 奇偶平衡：参考历史奇偶比例分布
            3. 邻号分析：基于上期号码的相邻号
            4. 余数分布：确保号码在不同余数区间均匀分布
            5. 随机组合：纯随机生成作为对照
            
            ⚠️ 温馨提示：彩票预测仅供参考，请理性购彩
        """.trimIndent()
    }
}