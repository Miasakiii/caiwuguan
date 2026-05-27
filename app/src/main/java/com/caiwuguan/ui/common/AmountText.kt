package com.caiwuguan.ui.common

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.caiwuguan.domain.model.BillType

@Composable
fun AmountText(amount: Long, type: BillType) {
    val isExpense = type == BillType.EXPENSE
    val sign = if (isExpense) "-" else "+"
    val targetAmount = amount / 100f
    val color = if (isExpense) Color(0xFFE53935) else Color(0xFF43A047)

    var animatedAmount by remember { mutableFloatStateOf(0f) }
    val animatedValue by animateFloatAsState(
        targetValue = animatedAmount,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "amount_animation"
    )

    LaunchedEffect(amount) {
        animatedAmount = targetAmount
    }

    Text(
        text = "$sign¥${String.format("%.2f", animatedValue)}",
        color = color,
        fontWeight = FontWeight.Medium,
        style = MaterialTheme.typography.bodyLarge
    )
}
