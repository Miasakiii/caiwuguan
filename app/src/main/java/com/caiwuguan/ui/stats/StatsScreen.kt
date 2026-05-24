package com.caiwuguan.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.caiwuguan.ui.common.AmountText
import com.caiwuguan.domain.model.BillType
import com.caiwuguan.domain.model.Category
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
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "上个月")
                }
                Text(
                    "${year}年${month}月",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { viewModel.nextMonth() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "下个月")
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
                            Text("支出", style = MaterialTheme.typography.labelMedium)
                            AmountText(totalExpense, BillType.EXPENSE)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("收入", style = MaterialTheme.typography.labelMedium)
                            AmountText(totalIncome, BillType.INCOME)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                        Text("结余: ", style = MaterialTheme.typography.bodyMedium)
                        AmountText(totalIncome - totalExpense, if (totalIncome >= totalExpense) BillType.INCOME else BillType.EXPENSE)
                    }
                }
            }
        }

        // 分类饼图
        if (categoryStats.isNotEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("分类分布", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))

                        // 饼图
                        Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                            val strokeWidth = size.minDimension * 0.35f
                            val radius = size.minDimension * 0.35f
                            val total = categoryStats.sumOf { it.total }.toFloat()
                            if (total <= 0f) return@Canvas

                            var startAngle = -90f
                            val center = Offset(size.width / 2f, size.height / 2f)

                            categoryStats.forEach { stat ->
                                val sweepAngle = (stat.total / total) * 360f
                                val color = categoryColors[stat.category] ?: Color.Gray
                                drawArc(
                                    color = color,
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = true,
                                    topLeft = Offset(center.x - radius, center.y - radius),
                                    size = Size(radius * 2, radius * 2)
                                )
                                startAngle += sweepAngle
                            }
                        }
                    }
                }
            }

            // 分类排行
            items(categoryStats) { stat ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                                color = categoryColors[stat.category] ?: Color.Gray,
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
                        Text("暂无支出数据", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // 每日趋势
        if (dailyTrend.isNotEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("每日趋势", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
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
