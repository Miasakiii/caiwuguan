package com.caiwuguan.ui.dataimport

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    navController: NavHostController,
    viewModel: ImportViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val importState by viewModel.importState.collectAsState()
    var selectedSource by remember { mutableStateOf<ImportSource?>(null) }

    // 文件选择器
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedSource?.let { source ->
                viewModel.importFile(context, it, source)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("导入账单") },
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
            // 导入说明
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "导入说明",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "1. 微信：我 → 钱包 → 账单 → 右上角「...」→ 账单下载",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "2. 支付宝：我的 → 账单 → 右上角「...」→ 开具交易流水证明",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "3. 选择对应的 CSV 文件进行导入",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // 导入按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        selectedSource = ImportSource.WECHAT
                        filePickerLauncher.launch("text/*")
                    },
                    modifier = Modifier.weight(1f),
                    enabled = importState !is ImportState.Loading
                ) {
                    Text("导入微信账单")
                }

                Button(
                    onClick = {
                        selectedSource = ImportSource.ALIPAY
                        filePickerLauncher.launch("text/*")
                    },
                    modifier = Modifier.weight(1f),
                    enabled = importState !is ImportState.Loading
                ) {
                    Text("导入支付宝账单")
                }
            }

            // 导入状态
            when (val state = importState) {
                is ImportState.Loading -> {
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
                            Text("正在导入...")
                        }
                    }
                }

                is ImportState.Success -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "导入完成",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text("成功导入：${state.importedCount} 笔")
                            Text("重复跳过：${state.duplicateCount} 笔")
                            Text("导入失败：${state.failedCount} 笔")
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { viewModel.resetState() }) {
                                Text("继续导入")
                            }
                        }
                    }
                }

                is ImportState.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "导入失败",
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

                is ImportState.Idle -> {
                    // 不显示任何内容
                }
            }
        }
    }
}
