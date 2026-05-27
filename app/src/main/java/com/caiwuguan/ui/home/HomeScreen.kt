package com.caiwuguan.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.caiwuguan.R
import com.caiwuguan.domain.model.Bill
import com.caiwuguan.domain.model.BillType
import com.caiwuguan.ui.common.AmountText
import com.caiwuguan.ui.common.BillCard
import com.caiwuguan.ui.common.EmptyState
import com.caiwuguan.ui.navigation.NavRoutes
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    navController: NavHostController,
    bottomPadding: Dp = 0.dp,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val todayExpense by viewModel.todayExpense.collectAsState()
    val todayIncome by viewModel.todayIncome.collectAsState()
    val monthExpense by viewModel.monthExpense.collectAsState()
    val monthIncome by viewModel.monthIncome.collectAsState()
    val recentBills by viewModel.recentBills.collectAsState()

    val dateFormat = SimpleDateFormat("yyyy年MM月dd日 EE", Locale.CHINESE)
    val monthBalance = monthIncome - monthExpense

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = bottomPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(Modifier.height(8.dp)) }

        // 今日日期
        item {
            Text(
                text = dateFormat.format(Date()),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Hero 卡片 - 今日收支（渐变背景）
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF667eea),
                                    Color(0xFF764ba2)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            stringResource(R.string.today),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    stringResource(R.string.expense),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "-¥${String.format("%.2f", todayExpense / 100.0)}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    stringResource(R.string.income),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "+¥${String.format("%.2f", todayIncome / 100.0)}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF81C784)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 本月汇总 - 并排迷你卡
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 本月支出迷你卡
                MiniStatCard(
                    modifier = Modifier.weight(1f),
                    label = "本月支出",
                    amount = monthExpense,
                    type = BillType.EXPENSE,
                    color = Color(0xFFE53935)
                )
                // 本月收入迷你卡
                MiniStatCard(
                    modifier = Modifier.weight(1f),
                    label = "本月收入",
                    amount = monthIncome,
                    type = BillType.INCOME,
                    color = Color(0xFF43A047)
                )
            }
        }

        // 本月结余卡片
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (monthBalance >= 0) {
                        Color(0xFFE8F5E9)
                    } else {
                        Color(0xFFFFEBEE)
                    }
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
                        "本月结余",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "${if (monthBalance >= 0) "+" else ""}¥${String.format("%.2f", monthBalance / 100.0)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (monthBalance >= 0) Color(0xFF43A047) else Color(0xFFE53935)
                    )
                }
            }
        }

        // 最近账单标题
        item {
            Text(
                stringResource(R.string.recent_bills),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // 最近账单列表
        if (recentBills.isEmpty()) {
            item {
                EmptyState(message = stringResource(R.string.empty_bills))
            }
        } else {
            items(recentBills) { bill ->
                BillCard(
                    bill = bill,
                    onClick = {
                        navController.navigate(NavRoutes.editBill(bill.id))
                    }
                )
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun MiniStatCard(
    modifier: Modifier = Modifier,
    label: String,
    amount: Long,
    type: BillType,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "${if (type == BillType.EXPENSE) "-" else "+"}¥${String.format("%.2f", amount / 100.0)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
