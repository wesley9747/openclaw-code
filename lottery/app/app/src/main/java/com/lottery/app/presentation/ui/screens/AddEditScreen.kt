package com.lottery.app.presentation.ui.screens

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.lottery.app.domain.model.LotteryRecord
import com.lottery.app.ocr.LotteryOcrService
import com.lottery.app.presentation.ui.components.BlueBall
import com.lottery.app.presentation.ui.components.NumberSelector
import com.lottery.app.presentation.ui.components.RedBall
import com.lottery.app.presentation.ui.components.RedBallRow
import com.lottery.app.presentation.viewmodel.LotteryViewModel
import kotlinx.coroutines.launch
import java.io.File

/**
 * 添加/编辑记录页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    recordId: Long? = null,
    onNavigateBack: () -> Unit,
    viewModel: LotteryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val ocrService = remember { LotteryOcrService() }

    // 表单状态
    var period by remember { mutableStateOf("") }
    var selectedRedBalls by remember { mutableStateOf(setOf<Int>()) }
    var selectedBlueBall by remember { mutableStateOf(0) }
    var remarks by remember { mutableStateOf("") }
    
    // OCR 状态
    var isProcessingOcr by remember { mutableStateOf(false) }
    var ocrResult by remember { mutableStateOf("") }
    
    // 相机拍照
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var hasCameraPermission by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null) {
            scope.launch {
                isProcessingOcr = true
                try {
                    val bitmap = context.contentResolver.openInputStream(photoUri!!)
                        ?.use { BitmapFactory.decodeStream(it) }
                    if (bitmap != null) {
                        val result = ocrService.recognizeLotteryNumbers(bitmap)
                        ocrResult = result.toDisplayString()
                        if (result.isValid && result.redBalls != null && result.blueBall != null) {
                            selectedRedBalls = result.redBalls.toSet()
                            selectedBlueBall = result.blueBall
                        }
                    }
                } catch (e: Exception) {
                    ocrResult = "识别失败: ${e.message}"
                } finally {
                    isProcessingOcr = false
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    fun takePhoto() {
        val photoFile = File(context.cacheDir, "lottery_${System.currentTimeMillis()}.jpg")
        photoUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
        cameraLauncher.launch(photoUri)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (recordId != null) "编辑记录" else "添加记录") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val record = LotteryRecord(
                                id = recordId ?: 0,
                                period = period,
                                purchaseDate = System.currentTimeMillis(),
                                redBalls = selectedRedBalls.toList(),
                                blueBall = selectedBlueBall,
                                remarks = remarks.takeIf { it.isNotBlank() }
                            )
                            viewModel.saveRecord(record)
                            onNavigateBack()
                        },
                        enabled = period.isNotBlank() && selectedRedBalls.size == 6 && selectedBlueBall > 0
                    ) {
                        Text("保存")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 期号输入
            OutlinedTextField(
                value = period,
                onValueChange = { period = it },
                label = { Text("期号") },
                placeholder = { Text("例如: 2024050") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 拍照识别按钮
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📸 拍照识别彩票",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                                takePhoto()
                            },
                            enabled = !isProcessingOcr
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("拍照")
                        }
                    }
                    if (isProcessingOcr) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                    if (ocrResult.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "识别结果: $ocrResult",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (ocrResult.contains("无法识别")) 
                                MaterialTheme.colorScheme.error 
                            else 
                                MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // 已选号码预览
            if (selectedRedBalls.isNotEmpty() || selectedBlueBall > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "已选号码 (红球 ${selectedRedBalls.size}/6, 蓝球 ${if (selectedBlueBall > 0) 1 else 0}/1)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (selectedRedBalls.isNotEmpty()) {
                                RedBallRow(numbers = selectedRedBalls.toList())
                            } else {
                                Text("请选择红球", color = MaterialTheme.colorScheme.outline)
                            }
                        }
                        if (selectedBlueBall > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("+ ", fontWeight = FontWeight.Bold)
                                BlueBall(number = selectedBlueBall, size = 32.dp)
                            }
                        }
                    }
                }
            }

            // 红球选择器
            NumberSelector(
                title = "🎱 红球 (1-33)",
                numbers = selectedRedBalls.toList(),
                selectedNumbers = selectedRedBalls,
                range = 1..33,
                onNumberSelected = { number ->
                    selectedRedBalls = if (number in selectedRedBalls) {
                        selectedRedBalls - number
                    } else {
                        selectedRedBalls + number
                    }
                },
                maxSelection = 6
            )

            // 蓝球选择器
            NumberSelector(
                title = "🔵 蓝球 (1-16)",
                numbers = if (selectedBlueBall > 0) listOf(selectedBlueBall) else emptyList(),
                selectedNumbers = if (selectedBlueBall > 0) setOf(selectedBlueBall) else emptySet(),
                range = 1..16,
                onNumberSelected = { selectedBlueBall = it },
                maxSelection = 1
            )

            // 备注
            OutlinedTextField(
                value = remarks,
                onValueChange = { remarks = it },
                label = { Text("备注") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
        }
    }
}