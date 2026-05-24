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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.caiwuguan.domain.model.BillType
import com.caiwuguan.domain.model.Category

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BillEditScreen(
    navController: NavHostController,
    billId: Long,
    viewModel: BillEditViewModel = hiltViewModel()
) {
    LaunchedEffect(billId) { viewModel.loadBill(billId) }

    val amount by viewModel.amount.collectAsState()
    val type by viewModel.type.collectAsState()
    val category by viewModel.category.collectAsState()
    val merchant by viewModel.merchant.collectAsState()
    val note by viewModel.note.collectAsState()
    val saved by viewModel.saved.collectAsState()

    LaunchedEffect(saved) { if (saved) navController.popBackStack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("编辑账单") },
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

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("类型: ", style = MaterialTheme.typography.titleSmall)
                Switch(
                    checked = type == BillType.INCOME,
                    onCheckedChange = { viewModel.toggleType() }
                )
                Text(if (type == BillType.EXPENSE) "支出" else "收入")
            }

            OutlinedTextField(
                value = amount,
                onValueChange = { viewModel.updateAmount(it) },
                label = { Text("金额 (元)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

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

            OutlinedTextField(
                value = merchant,
                onValueChange = { viewModel.updateMerchant(it) },
                label = { Text("商户") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = note,
                onValueChange = { viewModel.updateNote(it) },
                label = { Text("备注") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { viewModel.saveBill() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存")
            }

            Button(
                onClick = { viewModel.deleteBill() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("删除", color = Color.White)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
