package com.caiwuguan.ui.bill

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.caiwuguan.domain.model.Bill
import com.caiwuguan.ui.common.AmountText
import com.caiwuguan.ui.common.BillCard
import com.caiwuguan.ui.common.EmptyState
import com.caiwuguan.ui.navigation.NavRoutes
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillListScreen(
    navController: NavHostController,
    bottomPadding: Dp = 0.dp,
    viewModel: BillListViewModel = hiltViewModel()
) {
    val bills by viewModel.bills.collectAsState()
    val year by viewModel.currentYear.collectAsState()
    val month by viewModel.currentMonth.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(bottom = bottomPadding),
        verticalArrangement = Arrangement.spacedBy(4.dp)
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
                    text = "${year}年${month}月",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { viewModel.nextMonth() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "下个月")
                }
            }
        }

        if (bills.isEmpty()) {
            item {
                EmptyState(message = "该月没有账单记录")
            }
        } else {
            // 按日期分组
            val groupedBills = bills.groupBy { bill ->
                val sdf = SimpleDateFormat("yyyy年MM月dd日 EE", Locale.CHINESE)
                sdf.format(Date(bill.timestamp))
            }

            groupedBills.forEach { (date, dateBills) ->
                item {
                    Text(
                        text = date,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(dateBills, key = { it.id }) { bill ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.EndToStart) {
                                viewModel.deleteBill(bill)
                                true
                            } else false
                        }
                    )
                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Red)
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text("删除", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    ) {
                        BillCard(bill = bill, onClick = {
                            navController.navigate(NavRoutes.editBill(bill.id))
                        })
                    }
                }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}
