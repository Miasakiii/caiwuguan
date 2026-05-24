package com.caiwuguan.ui.bill

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.caiwuguan.domain.model.Category
import com.caiwuguan.ui.common.CategoryIcon
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddBillScreen(
    navController: NavHostController,
    viewModel: AddBillViewModel = hiltViewModel()
) {
    val amount by viewModel.amount.collectAsState()
    val type by viewModel.type.collectAsState()
    val category by viewModel.category.collectAsState()
    val merchant by viewModel.merchant.collectAsState()
    val note by viewModel.note.collectAsState()
    val timestamp by viewModel.timestamp.collectAsState()
    val saved by viewModel.saved.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(saved) {
        if (saved) navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("记账") },
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // 收入/支出切换
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (type == com.caiwuguan.domain.model.BillType.EXPENSE) "支出" else "收入",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.width(8.dp))
                Switch(checked = type == com.caiwuguan.domain.model.BillType.INCOME, onCheckedChange = { viewModel.toggleType() })
            }

            // 金额输入
            OutlinedTextField(
                value = amount,
                onValueChange = { viewModel.updateAmount(it) },
                label = { Text("金额 (元)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                suffix = { Text("元") }
            )

            // 分类选择
            Text("分类", style = MaterialTheme.typography.titleSmall)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Category.entries.forEach { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { viewModel.updateCategory(cat) },
                        label = { Text(cat.displayName) },
                        leadingIcon = { Text(cat.icon) }
                    )
                }
            }

            // 商户名
            OutlinedTextField(
                value = merchant,
                onValueChange = { viewModel.updateMerchant(it) },
                label = { Text("商户") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // 日期
            Button(onClick = { showDatePicker = true }) {
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                Text("日期: ${sdf.format(Date(timestamp))}")
            }

            // 备注
            OutlinedTextField(
                value = note,
                onValueChange = { viewModel.updateNote(it) },
                label = { Text("备注") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            // 保存
            Button(
                onClick = { viewModel.saveBill() },
                modifier = Modifier.fillMaxWidth(),
                enabled = amount.isNotBlank()
            ) {
                Text("保存")
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = timestamp)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.updateTimestamp(it) }
                    showDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
