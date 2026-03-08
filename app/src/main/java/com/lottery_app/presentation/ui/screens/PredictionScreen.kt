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
import com.lottery.app.domain.model.RecommendedNumber
import com.lottery.app.presentation.ui.components.BlueBall
import com.lottery.app.presentation.ui.components.RedBallRow
import com.lottery.app.presentation.viewmodel.PredictionViewModel

/**
 * 预测推荐页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionScreen(
    viewModel: PredictionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("智能预测") },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 冷热号统计
            uiState.hotColdStats?.let { stats ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "📊 冷热号统计 (基于 ${uiState.historyDrawCount} 期)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("🔥 热门红球", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    text = stats.hotRedBalls.take(5).joinToString(", ") { "${it.first}" },
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Column {
                                Text("❄️ 冷门红球", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    text = stats.coldRedBalls.take(5).joinToString(", ") { "${it.first}" },
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            // 预测按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.predictWithLocal() },
                    enabled = !uiState.isPredicting && uiState.historyDrawCount > 0,
                    modifier = Modifier.weight(1f)
                ) {
                    if (uiState.isPredicting && uiState.currentMethod == "本地算法") {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Calculate, contentDescription = null)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("本地算法")
                }

                Button(
                    onClick = { viewModel.predictWithQwen() },
                    enabled = !uiState.isPredicting && uiState.historyDrawCount > 0,
                    modifier = Modifier.weight(1f)
                ) {
                    if (uiState.isPredicting && uiState.currentMethod == "Qwen3-Max") {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Qwen3-Max")
                }
            }

            // 预测结果
            uiState.predictionResult?.let { result ->
                Spacer(modifier = Modifier.height(16.dp))
                
                // 置信度
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "�置信度: ${(result.confidence * 100).toInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "基于 ${uiState.historyDrawCount} 期数据分析",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 推荐号码列表
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(result.recommendedNumbers) { number ->
                        RecommendedNumberCard(number)
                    }

                    // 分析说明
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "📝 分析说明",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = result.analysis,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun RecommendedNumberCard(number: RecommendedNumber) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RedBallRow(numbers = number.redBalls, size = 32.dp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("+", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    BlueBall(number = number.blueBall, size = 32.dp)
                }
            }
            number.reason?.let { reason ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = reason,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}