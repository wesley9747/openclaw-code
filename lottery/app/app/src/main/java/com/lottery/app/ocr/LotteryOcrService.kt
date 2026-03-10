package com.lottery.app.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 彩票 OCR 识别服务
 * 使用 ML Kit 进行文字识别
 */
@Singleton
class LotteryOcrService @Inject constructor() {

    private val recognizer: TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * 从图片识别彩票号码
     * @param bitmap 图片 bitmap
     * @return 识别结果
     */
    suspend fun recognizeLotteryNumbers(bitmap: Bitmap): OcrResult {
        val image = InputImage.fromBitmap(bitmap, 0)
        
        return suspendCancellableCoroutine { continuation ->
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val result = parseOcrText(visionText.text)
                    continuation.resume(result)
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }
    }

    /**
     * 解析 OCR 识别出的文本
     * 识别双色球格式: 红球 + 蓝球
     * 常见格式:
     * - 01 05 12 19 23 29 + 14
     * - 01,05,12,19,23,29,14
     * - 01 05 12 19 23 29 14
     */
    private fun parseOcrText(text: String): OcrResult {
        val lines = text.replace("\n", " ").split(" ")
            .map { it.trim() }
            .filter { it.isNotBlank() }
        
        var redBalls: List<Int>? = null
        var blueBall: Int? = null
        var confidence = 0f
        
        // 尝试多种解析方式
        
        // 方式1: 查找 6+1 模式 (6个红球 + 1个蓝球，用+分隔)
        val plusPattern = text.replace("\n", " ").let { content ->
            val match = Regex("([0-9]+[,\\s]*){6}\\s*\\+\\s*([0-9]+)").find(content)
            if (match != null) {
                val parts = match.value.replace("+", " ").split(Regex("[,\\s]+"))
                    .filter { it.isNotBlank() }
                if (parts.size == 7) {
                    val reds = parts.take(6).mapNotNull { it.toIntOrNull()?.coerceIn(1, 33) }
                    val blue = parts.lastOrNull()?.toIntOrNull()?.coerceIn(1, 16)
                    if (reds.size == 6 && blue != null) {
                        redBalls = reds
                        blueBall = blue
                        confidence = 0.9f
                    }
                }
            }
        }
        
        // 方式2: 查找连续数字模式
        if (redBalls == null) {
            val allNumbers = Regex("[0-9]+").findAll(text)
                .map { it.value.toIntOrNull() }
                .filterNotNull()
                .toList()
            
            // 找6个1-33的数 + 1个1-16的数
            if (allNumbers.size >= 7) {
                val reds = allNumbers.filter { it in 1..33 }.take(6).distinct()
                val blue = allNumbers.filter { it in 1..16 }.lastOrNull()
                
                if (reds.size == 6 && blue != null) {
                    redBalls = reds
                    blueBall = blue
                    confidence = 0.7f
                }
            }
        }
        
        // 方式3: 分行解析
        if (redBalls == null) {
            val numbersPerLine = lines.mapNotNull { line ->
                Regex("[0-9]+").findAll(line)
                    .map { it.value.toIntOrNull() }
                    .filterNotNull()
                    .toList()
            }
            
            // 假设红球在一行，蓝球在另一行
            val flatNumbers = numbersPerLine.flatten()
            val reds = flatNumbers.filter { it in 1..33 }.take(6).distinct()
            val blue = flatNumbers.filter { it in 1..16 }.lastOrNull()
            
            if (reds.size == 6 && blue != null) {
                redBalls = reds
                blueBall = blue
                confidence = 0.6f
            }
        }
        
        // 验证号码合法性
        val valid = redBalls != null && blueBall != null &&
                    redBalls!!.size == 6 &&
                    redBalls!!.all { it in 1..33 } &&
                    blueBall!! in 1..16 &&
                    redBalls!!.distinct().size == 6
        
        return OcrResult(
            redBalls = redBalls,
            blueBall = blueBall,
            rawText = text,
            confidence = confidence,
            isValid = valid
        )
    }

    /**
     * 验证号码
     */
    fun validateNumbers(redBalls: List<Int>, blueBall: Int): Boolean {
        return redBalls.size == 6 &&
               redBalls.all { it in 1..33 } &&
               redBalls.distinct().size == 6 &&
               blueBall in 1..16
    }
    
    /**
     * 释放资源
     */
    fun close() {
        recognizer.close()
    }
}

/**
 * OCR 识别结果
 */
data class OcrResult(
    val redBalls: List<Int>?,
    val blueBall: Int?,
    val rawText: String,
    val confidence: Float,
    val isValid: Boolean
) {
    fun toDisplayString(): String {
        if (!isValid || redBalls == null || blueBall == null) {
            return "无法识别"
        }
        val redStr = redBalls.sorted().joinToString(", ") { String.format("%02d", it) }
        return "$redStr + ${String.format("%02d", blueBall)}"
    }
}