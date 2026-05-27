package com.caiwuguan.ui.export

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    navController: NavHostController,
    viewModel: ExportViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val exportState by viewModel.exportState.collectAsState()
    val year by viewModel.selectedYear.collectAsState()
    val month by viewModel.selectedMonth.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("导出账单") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 月份选择
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "选择导出月份",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            val totalMonths = year * 12 + month - 2
                            viewModel.selectMonth(totalMonths / 12, totalMonths % 12 + 1)
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "上个月")
                        }
                        Text(
                            "${year}年${month}月",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = {
                            val totalMonths = year * 12 + month
                            viewModel.selectMonth(totalMonths / 12, totalMonths % 12 + 1)
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "下个月")
                        }
                    }
                }
            }

            // 导出按钮
            Button(
                onClick = { viewModel.exportToCsv(context) },
                modifier = Modifier.fillMaxWidth(),
                enabled = exportState !is ExportState.Loading
            ) {
                Text("导出为 CSV 文件")
            }

            // 导出状态
            when (val state = exportState) {
                is ExportState.Loading -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("正在导出...")
                        }
                    }
                }

                is ExportState.Success -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "导出成功",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = {
                                val shareIntent = Intent.createChooser(
                                    com.caiwuguan.data.exporter.CsvExporter().createShareIntent(state.uri),
                                    "分享账单"
                                )
                                context.startActivity(shareIntent)
                            }) {
                                Text("分享文件")
                            }
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { viewModel.resetState() }) {
                                Text("继续导出")
                            }
                        }
                    }
                }

                is ExportState.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "导出失败",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                state.message,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { viewModel.resetState() }) {
                                Text("重试")
                            }
                        }
                    }
                }

                is ExportState.Idle -> {
                    // 不显示任何内容
                }
            }
        }
    }
}
