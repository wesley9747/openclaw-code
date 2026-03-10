package com.lottery.app.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lottery.app.presentation.ui.theme.LotteryColors

/**
 * 彩票红球组件
 */
@Composable
fun RedBall(
    number: Int,
    size: Dp = 36.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(LotteryColors.RedBall),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = String.format("%02d", number),
            color = Color.White,
            fontSize = (size.value * 0.4).sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 彩票蓝球组件
 */
@Composable
fun BlueBall(
    number: Int,
    size: Dp = 36.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(LotteryColors.BlueBall),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = String.format("%02d", number),
            color = Color.White,
            fontSize = (size.value * 0.4).sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 红球列表组件
 */
@Composable
fun RedBallRow(
    numbers: List<Int>,
    size: Dp = 36.dp,
    spacing: Dp = 4.dp,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
        numbers.sorted().forEach { number ->
            RedBall(number = number, size = size)
        }
    }
}

/**
 * 彩票号码展示（红球 + 蓝球）
 */
@Composable
fun LotteryNumbers(
    redBalls: List<Int>,
    blueBall: Int,
    redBallSize: Dp = 36.dp,
    blueBallSize: Dp = 36.dp,
    spacing: Dp = 8.dp,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
        RedBallRow(numbers = redBalls, size = redBallSize)
        Text("+", fontWeight = FontWeight.Bold)
        BlueBall(number = blueBall, size = blueBallSize)
    }
}

/**
 * 匹配状态指示器
 */
@Composable
fun MatchIndicator(
    isMatched: Boolean,
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = if (isMatched) LotteryColors.WinBackground else Color.Gray.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = if (isMatched) LotteryColors.WinHighlight else Color.Gray,
            fontWeight = if (isMatched) FontWeight.Bold else FontWeight.Normal,
            fontSize = 12.sp
        )
    }
}

/**
 * 号码选择器（用于编辑页面）
 */
@Composable
fun NumberSelector(
    title: String,
    numbers: List<Int>,
    selectedNumbers: Set<Int>,
    range: IntRange,
    onNumberSelected: (Int) -> Unit,
    maxSelection: Int = 6,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "$title (已选 ${selectedNumbers.size}/$maxSelection)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 号码网格
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            val rows = range.toList().chunked(8)
            rows.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    row.forEach { number ->
                        val isSelected = number in selectedNumbers
                        SelectableBall(
                            number = number,
                            isSelected = isSelected,
                            onClick = {
                                if (isSelected) {
                                    onNumberSelected(number)
                                } else if (selectedNumbers.size < maxSelection) {
                                    onNumberSelected(number)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectableBall(
    number: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(36.dp),
        shape = CircleShape,
        color = if (isSelected) LotteryColors.RedBall else LotteryColors.RedBallBackground
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = String.format("%02d", number),
                color = if (isSelected) Color.White else LotteryColors.RedBall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}