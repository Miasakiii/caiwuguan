package com.caiwuguan.ui.addbill

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.caiwuguan.domain.model.BillType
import com.caiwuguan.domain.model.Category

data class NlBillData(
    val amount: Long,
    val type: BillType,
    val category: Category,
    val merchant: String,
    val description: String
)

@Composable
fun NlBillConfirmDialog(
    billData: NlBillData,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("确认账单") },
        text = {
            Column {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (billData.type == BillType.EXPENSE) "-¥${String.format("%.2f", billData.amount / 100.0)}"
                            else "+¥${String.format("%.2f", billData.amount / 100.0)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (billData.type == BillType.EXPENSE) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                InfoRow("类型", if (billData.type == BillType.EXPENSE) "支出" else "收入")
                InfoRow("分类", billData.category.displayName)
                if (billData.merchant.isNotBlank()) {
                    InfoRow("商户", billData.merchant)
                }
                if (billData.description.isNotBlank()) {
                    InfoRow("备注", billData.description)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("确认记账")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
