package com.caiwuguan.ui.bill

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.caiwuguan.domain.model.Category
import com.caiwuguan.ui.addbill.NlBillConfirmDialog
import com.caiwuguan.ui.common.CategoryColors
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
    val nlInput by viewModel.nlInput.collectAsState()
    val nlParsing by viewModel.nlParsing.collectAsState()
    val nlResult by viewModel.nlResult.collectAsState()
    val nlError by viewModel.nlError.collectAsState()
    val hapticFeedback = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(saved) {
        if (saved) navController.popBackStack()
    }

    // 显示错误信息
    LaunchedEffect(nlError) {
        nlError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearNlError()
        }
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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

            // 自然语言输入
            if (viewModel.hasApiKey) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = nlInput,
                        onValueChange = { viewModel.updateNlInput(it) },
                        label = { Text("智能记账") },
                        placeholder = { Text("例：午饭花了35块") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        enabled = !nlParsing
                    )
                    IconButton(
                        onClick = { viewModel.parseNaturalLanguage() },
                        enabled = nlInput.isNotBlank() && !nlParsing
                    ) {
                        if (nlParsing) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Filled.Send, contentDescription = "解析")
                        }
                    }
                }
            }

            // 收入/支出切换
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (type == com.caiwuguan.domain.model.BillType.EXPENSE) "支出" else "收入",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.width(8.dp))
                Switch(checked = type == com.caiwuguan.domain.model.BillType.INCOME, onCheckedChange = { viewModel.toggleType() })
            }

            // 金额输入 - 使用更大字体
            OutlinedTextField(
                value = amount,
                onValueChange = { viewModel.updateAmount(it) },
                label = { Text("金额") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.headlineMedium,
                suffix = { Text("元", style = MaterialTheme.typography.titleMedium) }
            )

            // 分类选择 - 彩色图标网格
            Text("分类", style = MaterialTheme.typography.titleSmall)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Category.entries.forEach { cat ->
                    val isSelected = category == cat
                    CategoryGridItem(
                        category = cat,
                        isSelected = isSelected,
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.updateCategory(cat)
                        }
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
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.saveBill()
                },
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

    // 自然语言解析结果确认弹窗
    nlResult?.let { result ->
        NlBillConfirmDialog(
            billData = result,
            onConfirm = { viewModel.confirmNlBill() },
            onDismiss = { viewModel.dismissNlResult() }
        )
    }
}

@Composable
private fun CategoryGridItem(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        CategoryColors.getBackground(category)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val borderColor = if (isSelected) {
        CategoryColors.getForeground(category)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(64.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Text(
            text = category.icon,
            fontSize = 24.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = category.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) {
                CategoryColors.getForeground(category)
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
