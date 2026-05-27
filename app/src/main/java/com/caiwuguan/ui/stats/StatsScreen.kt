package com.caiwuguan.ui.stats

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.caiwuguan.R
import com.caiwuguan.ui.common.AmountText
import com.caiwuguan.domain.model.BillType
import com.caiwuguan.domain.model.Category
import com.caiwuguan.ui.common.CategoryColors
import kotlin.math.min
import kotlin.math.roundToInt

private val categoryColors = mapOf(
    Category.FOOD to Color(0xFFFF5722),
    Category.TRANSPORT to Color(0xFF2196F3),
    Category.SHOPPING to Color(0xFFFF9800),
    Category.ENTERTAINMENT to Color(0xFF9C27B0),
    Category.HOUSING to Color(0xFF4CAF50),
    Category.MEDICAL to Color(0xFFF44336),
    Category.EDUCATION to Color(0xFF03A9F4),
    Category.TRANSFER to Color(0xFF607D8B),
    Category.RED_PACKET to Color(0xFFE91E63),
    Category.SALARY to Color(0xFF8BC34A),
    Category.INVESTMENT to Color(0xFF795548),
    Category.OTHER to Color(0xFF9E9E9E)
)

@Composable
fun StatsScreen(
    navController: NavHostController,
    bottomPadding: Dp = 0.dp,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val year by viewModel.currentYear.collectAsState()
    val month by viewModel.currentMonth.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val categoryStats by viewModel.categoryStats.collectAsState()
    val dailyTrend by viewModel.dailyTrend.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(bottom = bottomPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 月份切换
        item {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.previousMonth() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.previous_month))
                }
                Text(
                    "${year}年${month}月",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { viewModel.nextMonth() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = stringResource(R.string.next_month))
                }
            }
        }

        // 月度总览
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(R.string.expense), style = MaterialTheme.typography.labelMedium)
                            AmountText(totalExpense, BillType.EXPENSE)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(R.string.income), style = MaterialTheme.typography.labelMedium)
                            AmountText(totalIncome, BillType.INCOME)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                        Text("${stringResource(R.string.balance)}: ", style = MaterialTheme.typography.bodyMedium)
                        AmountText(totalIncome - totalExpense, if (totalIncome >= totalExpense) BillType.INCOME else BillType.EXPENSE)
                    }
                }
            }
        }

        // 分类环形图
        if (categoryStats.isNotEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.category_distribution), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))

                        // 环形图 - 带入场动画
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            val totalAmount = categoryStats.sumOf { it.total }

                            // 动画进度
                            var animationPlayed by remember { mutableStateOf(false) }
                            val animatedProgress by animateFloatAsState(
                                targetValue = if (animationPlayed) 1f else 0f,
                                animationSpec = androidx.compose.animation.core.tween(
                                    durationMillis = 1000,
                                    easing = androidx.compose.animation.core.FastOutSlowInEasing
                                ),
                                label = "chart_animation"
                            )

                            LaunchedEffect(categoryStats) {
                                animationPlayed = true
                            }

                            Canvas(modifier = Modifier.size(180.dp)) {
                                val strokeWidth = 40f
                                val radius = (size.minDimension - strokeWidth) / 2
                                val total = totalAmount.toFloat()
                                if (total <= 0f) return@Canvas

                                var startAngle = -90f
                                val center = Offset(size.width / 2f, size.height / 2f)

                                categoryStats.forEachIndexed { index, stat ->
                                    val sweepAngle = (stat.total.toFloat() / total) * 360f * animatedProgress
                                    val color = CategoryColors.getForeground(stat.category)
                                    drawArc(
                                        color = color,
                                        startAngle = startAngle,
                                        sweepAngle = sweepAngle,
                                        useCenter = false,
                                        topLeft = Offset(center.x - radius, center.y - radius),
                                        size = Size(radius * 2, radius * 2),
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                                    )
                                    startAngle += sweepAngle
                                }
                            }

                            // 中心显示总额
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "总支出",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                AmountText(totalAmount, BillType.EXPENSE)
                            }
                        }
                    }
                }
            }

            // 分类排行
            items(categoryStats) { stat ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CategoryColors.getBackground(stat.category))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stat.category.icon, fontSize = MaterialTheme.typography.titleLarge.fontSize)
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stat.category.displayName, style = MaterialTheme.typography.bodyMedium)
                            LinearProgressIndicator(
                                progress = { stat.percentage },
                                modifier = Modifier.fillMaxWidth().height(4.dp).padding(top = 2.dp),
                                color = CategoryColors.getForeground(stat.category),
                                trackColor = Color.LightGray
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            AmountText(stat.total, BillType.EXPENSE)
                            Text(
                                "${(stat.percentage * 100).roundToInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.no_expense_data), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // 每日趋势
        if (dailyTrend.isNotEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.daily_trend), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                            drawDailyTrend(dailyTrend)
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

private fun DrawScope.drawDailyTrend(dailyTrend: Map<Int, Long>) {
    if (dailyTrend.isEmpty()) return
    val maxAmount = dailyTrend.values.max()
    if (maxAmount <= 0) return

    val sortedDays = dailyTrend.entries.sortedBy { it.key }
    val barWidth = size.width / sortedDays.size.toFloat() * 0.7f
    val gap = size.width / sortedDays.size.toFloat() * 0.3f

    sortedDays.forEachIndexed { index, (_, amount) ->
        val barHeight = (amount.toFloat() / maxAmount) * size.height * 0.85f
        val x = index * (barWidth + gap) + gap / 2
        drawRect(
            color = Color(0xFF4CAF50),
            topLeft = Offset(x, size.height - barHeight),
            size = Size(barWidth, barHeight)
        )
    }
}
