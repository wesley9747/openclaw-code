# 彩票管理安卓应用 - UI 设计规范

## 1. 设计系统概述

基于 **Material Design 3** (Material You) 的自定义主题，适配安卓 8.0+。

### 1.1 主题色板

```kotlin
// Color.kt
val Primary = Color(0xFF1976D2)       // Material Blue 700
val PrimaryVariant = Color(0xFF2196F3) // Blue 500
val Secondary = Color(0xFF00897B)     // Teal 600
val SecondaryVariant = Color(0xFF4DB6AC) // Teal 300
val Tertiary = Color(0xFFE53935)      // Red 600 (彩票红)

val Background = Color(0xFFFAFAFA)    // 浅灰背景
val Surface = Color(0xFFFFFFFF)       // 纯白卡片
val SurfaceVariant = Color(0xFFE0E0E0) // 灰白分隔

val OnPrimary = Color.White
val OnSecondary = Color.White
val OnBackground = Color(0xFF212121)
val OnSurface = Color(0xFF212121)
val OnSurfaceVariant = Color(0xFF757575)

val RedBall = Color(0xFFD32F2F)       // 红球颜色
val BlueBall = Color(0xFF1976D2)      // 蓝球颜色
val Success = Color(0xFF388E3C)       // 中奖绿
val Warning = Color(0xFFF57C00)       // 警告橙
val Error = Color(0xFFD32F2F)         // 错误红
```

---

## 2. 组件设计

### 2.1 彩票球组件 (LotteryBall)

```kotlin
@Composable
fun LotteryBall(
    number: Int,
    type: BallType = BallType.RED,
    size: Dp = 36.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(if (type == BallType.RED) RedBall else BlueBall),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString().padStart(2, '0'),
            color = Color.White,
            fontSize = (size.value * 0.4).sp,
            fontWeight = FontWeight.Bold
        )
    }
}

enum class BallType { RED, BLUE }
```

**使用**：
```kotlin
Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
    repeat(6) { LotteryBall(redNumbers[it], BallType.RED) }
    Spacer(modifier = width(4.dp))
    LotteryBall(blueNumber, BallType.BLUE)
}
```

---

### 2.2 记录卡片 (RecordCard)

```kotlin
@Composable
fun RecordCard(
    record: LotteryRecord,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 头部：期号 + 日期
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "第${record.period}期",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = formatDate(record.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 号码展示
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                LotteryBallRow(record.redBalls, record.blueBall)
            }

            // 开奖结果（如果有）
            if (record.drawRedBalls != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 中奖结果
                    if (record.isWin) {
                        Surface(
                            color = Success.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Success,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${record.prizeLevel} ¥${record.prizeAmount}",
                                    color = Success,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "未中奖",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
```

---

### 2.3 号码选择器 (NumberSelector)

```kotlin
@Composable
fun NumberSelector(
    title: String,
    numbers: List<Int>,
    selectedNumbers: Set<Int>,
    onNumberClick: (Int) -> Unit,
    maxSelection: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(
            text = "$title (已选 ${selectedNumbers.size}/$maxSelection)",
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(1..33) { num ->
                NumberButton(
                    number = num,
                    selected = selectedNumbers.contains(num),
                    onClick = { onNumberClick(num) }
                )
            }
        }
    }
}

@Composable
fun NumberButton(
    number: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(
                if (selected) Primary else Surface,
                RoundedCornerShape(24.dp)
            )
            .border(
                width = 1.dp,
                color = if (selected) Primary else DividerColor,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString().padStart(2, '0'),
            color = if (selected) OnPrimary else OnSurface,
            fontWeight = FontWeight.Medium
        )
    }
}
```

---

### 2.4 统计卡片 (StatsCard)

```kotlin
@Composable
fun StatsCard(
    title: String,
    value: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = Primary
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }
        }
    }
}
```

---

## 3. 页面详细设计

### 3.1 首页 (HomeScreen)

**布局结构**：
```
Column(
    modifier = Modifier
        .fillMaxSize()
        .background(Background)
        .padding(16.dp)
) {
    // 顶部标题栏
    TopAppBar(
        title = { Text("彩票智查") },
        actions = {
            IconButton(onClick = { /* 设置 */ }) {
                Icon(Icons.Default.Settings, contentDescription = "设置")
            }
        }
    )

    Spacer(modifier = Modifier.height(16.dp))

    // 年度统计卡片（3列）
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatsCard(
            title = "年度花费",
            value = "¥1,240",
            modifier = Modifier.weight(1f)
        )
        StatsCard(
            title = "中奖金额",
            value = "¥340",
            modifier = Modifier.weight(1f)
        )
        StatsCard(
            title = "ROI",
            value = "-72.6%",
            subtitle = "谨慎购彩",
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(20.dp))

    // 快捷操作按钮
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ActionButton(
            icon = Icons.Default.CameraAlt,
            text = "拍照识别",
            onClick = { navController.navigate("camera") },
            modifier = Modifier.weight(1f)
        )
        ActionButton(
            icon = Icons.Default.AutoAwesome,
            text = "智能预测",
            onClick = { navController.navigate("prediction") },
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(20.dp))

    // 最近记录
    Text(
        text = "最近记录",
        style = MaterialTheme.typography.titleLarge
    )

    Spacer(modifier = Modifier.height(8.dp))

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(recentRecords) { record ->
            RecordCard(record = record) {
                navController.navigate("detail/${record.id}")
            }
        }
    }

    if (recentRecords.isEmpty()) {
        EmptyState(
            icon = Icons.Default.ReceiptLong,
            title = "暂无记录",
            description = "点击上方拍照识别开始记录"
        )
    }
}
```

---

### 3.2 拍照识别页 (CameraScreen)

```
Scaffold(
    topBar = {
        TopAppBar(
            title = { Text("拍照识别") },
            navigationIcon = {
                IconButton(onClick = { finish() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                }
            }
        )
    }
) { paddingValues ->
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        // 相机预览
        AndroidView(
            factory = { context ->
                PreviewView(context).apply {
                    // CameraX 配置
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 取景框覆盖层
        OverlayView() // 绘制边框和引导线

        // 底部操作按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FloatingActionButton(
                onClick = { takePhoto() },
                backgroundColor = Primary
            ) {
                Icon(Icons.Default.Camera, contentDescription = "拍照")
            }

            Button(
                onClick = { /* 相册 */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Surface.copy(alpha = 0.9f)
                )
            ) {
                Text("相册")
            }
        }

        // 识别结果预览（识别完成后显示）
        if (showResult) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("识别结果", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    // 显示期号和号码
                    Row {
                        LotteryBallRow(redBalls, blueBall)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(onClick = { retry() }) {
                            Text("重新识别")
                        }
                        Button(
                            onClick = { confirm() },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Text("确认保存")
                        }
                    }
                }
            }
        }

        // 加载指示器
        if (isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("识别中...", color = Color.White)
            }
        }
    }
}
```

---

### 3.3 记录详情页 (RecordDetailScreen)

```
Scaffold(
    topBar = {
        TopAppBar(
            title = { Text("第${record.period}期") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                }
            },
            actions = {
                IconButton(onClick = { editMode = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "编辑")
                }
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "删除")
                }
            }
        )
    }
) { paddingValues ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 投注号码
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("投注号码", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LotteryBallRow(record.redBalls, record.blueBall)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 开奖结果（如果有）
        if (record.drawRedBalls != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, DividerColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("开奖结果", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LotteryBallRow(record.drawRedBalls, record.drawBlueBall)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formatDate(record.drawDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 中奖信息
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (record.isWin) {
                        Success.copy(alpha = 0.1f)
                    } else {
                        Surface
                    }
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (record.isWin) Icons.Default.CheckCircle else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (record.isWin) Success else Error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (record.isWin) {
                                "恭喜中奖 ${record.prizeLevel}！"
                            } else {
                                "未中奖"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            color = if (record.isWin) Success else OnSurface
                        )
                    }

                    if (record.isWin) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "奖金：¥${record.prizeAmount}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Success
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 备注信息
        if (record.remarks.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("备注", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = record.remarks,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
```

---

## 4. 主题配置 (Theme.kt)

```kotlin
@Composable
fun LotteryTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = lightColorScheme(
        primary = Primary,
        onPrimary = OnPrimary,
        secondary = Secondary,
        onSecondary = OnSecondary,
        background = Background,
        onBackground = OnBackground,
        surface = Surface,
        onSurface = OnSurface,
        surfaceVariant = SurfaceVariant,
        onSurfaceVariant = OnSurfaceVariant,
        error = Error,
        onError = OnPrimary
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(
            headlineLarge = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                lineHeight = 40.sp
            ),
            titleLarge = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                lineHeight = 28.sp
            ),
            bodyMedium = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
            // ... 其他文字样式
        ),
        content = content
    )
}
```

---

## 5. 布局规范

### 5.1 间距
- **屏幕边距**：16dp
- **卡片间距**：12dp (vertical), 16dp (horizontal)
- **元素内边距**：8dp / 12dp / 16dp
- **按钮高度**：48dp (最小点击区域)

### 5.2 字体
- **超大标题**：32sp (headlineLarge)
- **大标题**：22sp (titleLarge)
- **正文**：14-16sp (bodyMedium)
- **辅助文字**：12sp (labelSmall)

### 5.3 动画
- 页面切换：fade through (300ms)
- 按钮点击：scale(0.95) + ripple
- 卡片出现：slide in from bottom + fade (200-400ms)

---

## 6. 资源文件

### colors.xml (备用，用于非Compose部分)
```xml
<color name="primary">#1976D2</color>
<color name="red_ball">#D32F2F</color>
<color name="blue_ball">#1976D2</color>
<color name="surface">#FFFFFF</color>
<color name="background">#FAFAFA</color>
```

### dimens.xml
```xml
<dimen name="padding_small">8dp</dimen>
<dimen name="padding_medium">16dp</dimen>
<dimen name="padding_large">24dp</dimen>
<dimen name="ball_size">36dp</dimen>
<dimen name="button_height">48dp</dimen>
```

---

## 7. 图标资源

使用 Material Icons：
- Icons.Filled.CameraAlt (拍照)
- Icons.Filled.AutoAwesome (预测)
- Icons.Filled.ReceiptLong (记录)
- Icons.Filled.Assessment (统计)
- Icons.Filled.Settings (设置)
- Icons.Filled.CheckCircle (成功)
- Icons.Filled.Delete (删除)
- Icons.Filled.Edit (编辑)

---

## 8. 特殊效果

### 8.1 彩票球阴影
```kotlin
shadow = 8.dp, // 卡片式投影
shape = CircleShape
```

### 8.2 波纹效果
所有按钮自动有 Ripple，无需额外添加

### 8.3 中奖高亮
- 成功状态的 Card 使用 `Success.copy(alpha = 0.1f)` 背景色
- 勾选图标和文字使用 Success 色

---

## 9. 完整设计稿

高保真设计图建议使用 **Figma** 或 **Compose Layout Inspector** 预览。

推荐的 Figma 配色和组件库：
- Material Design 3 Kit
- 自定义颜色变量（见第1节）
- 字体：Roboto / Noto Sans SC

---

**设计版本**: v1.0-ui  
**可用作**: Jetpack Compose 开发直接参考  
**下一步**: 架构设计 → 编码实现
