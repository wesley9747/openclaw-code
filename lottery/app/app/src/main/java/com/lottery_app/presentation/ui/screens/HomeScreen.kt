package com.lottery.app.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lottery.app.domain.model.LotteryRecord
import com.lottery.app.presentation.ui.components.*
import com.lottery.app.presentation.viewmodel.LotteryViewModel

/**
 * 首页 - 概览
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAdd: () -> Unit,
    viewModel: LotteryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val stats = remember(uiState.records) {
        mapOf(
            "total" to uiState.records.size,
            "wins" to uiState.winCount,
            "winnings" to uiState.totalWinnings
        )
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("双色球彩票") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加记录")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 统计卡片
            item {
                StatisticsCard(stats = stats)
            }

            // 最近记录
            item {
                Text(
                    text = "最近记录",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (uiState.records.isEmpty()) {
                item {
                    EmptyState(onAddClick = onNavigateToAdd)
                }
            } else {
                items(uiState.records.take(5)) { record ->
                    RecordCard(
                        record = record,
                        onCheckWin = { viewModel.checkWinStatus(record) },
                        onDelete = { viewModel.deleteRecord(record.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatisticsCard(stats: Map<String, Any>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                icon = Icons.Default.Receipt,
                value = "${stats["total"]}",
                label = "购买记录"
            )
            StatItem(
                icon = Icons.Default.EmojiEvents,
                value = "${stats["wins"]}",
                label = "中奖次数"
            )
            StatItem(
                icon = Icons.Default.AttachMoney,
                value = "¥${String.format("%.0f", stats["winnings"] as Double)}",
                label = "总奖金"
            )
        }
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun RecordCard(
    record: LotteryRecord,
    onCheckWin: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (record.isWin) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "第 ${record.period} 期",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (record.isWin) {
                    AssistChip(
                        onClick = { },
                        label = { Text(record.prizeLevel ?: "") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 购买号码
            Text(text = "购买号码:", style = MaterialTheme.typography.bodySmall)
            if (record.redBalls.isNotEmpty()) {
                LotteryNumbers(
                    redBalls = record.redBalls,
                    blueBall = record.blueBall,
                    redBallSize = 28.dp,
                    blueBallSize = 28.dp
                )
            } else {
                Text(text = "未设置", color = MaterialTheme.colorScheme.error)
            }

            // 开奖号码
            if (record.drawDate != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "开奖号码:", style = MaterialTheme.typography.bodySmall)
                if (record.drawRedBalls != null) {
                    LotteryNumbers(
                        redBalls = record.drawRedBalls!!,
                        blueBall = record.drawBlueBall!!,
                        redBallSize = 28.dp,
                        blueBallSize = 28.dp
                    )
                }

                // 匹配结果
                if (record.matchedRedCount > 0 || record.matchedBlue) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MatchIndicator(
                            isMatched = record.matchedRedCount > 0,
                            text = "红球 ${record.matchedRedCount}/6"
                        )
                        MatchIndicator(
                            isMatched = record.matchedBlue,
                            text = if (record.matchedBlue) "蓝球 ✓" else "蓝球 ✗"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (record.drawDate == null) {
                    TextButton(onClick = onCheckWin) {
                        Text("检查中奖")
                    }
                }
                TextButton(onClick = { showDeleteDialog = true }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除这条记录吗？") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun EmptyState(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Receipt,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "暂无记录", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "点击下方按钮添加购买记录",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onAddClick) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("添加记录")
        }
    }
}