package com.caiwuguan.ui.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.caiwuguan.domain.model.BillType

@Composable
fun AmountText(amount: Long, type: BillType) {
    val isExpense = type == BillType.EXPENSE
    val sign = if (isExpense) "-" else "+"
    val amountYuan = amount / 100.0
    val color = if (isExpense) Color(0xFFE53935) else Color(0xFF43A047)

    Text(
        text = "$sign¥${String.format("%.2f", amountYuan)}",
        color = color,
        fontWeight = FontWeight.Medium,
        style = MaterialTheme.typography.bodyLarge
    )
}
