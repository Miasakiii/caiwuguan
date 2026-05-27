package com.caiwuguan.ui.common

import androidx.compose.ui.graphics.Color
import com.caiwuguan.domain.model.Category

object CategoryColors {

    // 分类背景色 - 每个分类独立色彩
    private val categoryBackgroundMap = mapOf(
        Category.FOOD to Color(0xFFFFF3E0),          // 橙色系
        Category.TRANSPORT to Color(0xFFE3F2FD),      // 蓝色系
        Category.SHOPPING to Color(0xFFFCE4EC),       // 粉色系
        Category.ENTERTAINMENT to Color(0xFFF3E5F5),  // 紫色系
        Category.HOUSING to Color(0xFFE8F5E9),        // 绿色系
        Category.MEDICAL to Color(0xFFFFEBEE),        // 红色系
        Category.EDUCATION to Color(0xFFE0F7FA),      // 青色系
        Category.TRANSFER to Color(0xFFEEEEEE),       // 灰色系
        Category.RED_PACKET to Color(0xFFFFF8E1),     // 黄色系
        Category.SALARY to Color(0xFFE8F5E9),         // 浅绿色
        Category.INVESTMENT to Color(0xFFE8EAF6),     // 靛蓝色
        Category.OTHER to Color(0xFFF5F5F5)           // 浅灰色
    )

    // 分类图标/文字颜色
    private val categoryForegroundMap = mapOf(
        Category.FOOD to Color(0xFFE65100),          // 深橙色
        Category.TRANSPORT to Color(0xFF1565C0),      // 深蓝色
        Category.SHOPPING to Color(0xFFAD1457),       // 深粉色
        Category.ENTERTAINMENT to Color(0xFF7B1FA2),  // 深紫色
        Category.HOUSING to Color(0xFF2E7D32),        // 深绿色
        Category.MEDICAL to Color(0xFFC62828),        // 深红色
        Category.EDUCATION to Color(0xFF00838F),      // 深青色
        Category.TRANSFER to Color(0xFF616161),       // 深灰色
        Category.RED_PACKET to Color(0xFFF9A825),     // 深黄色
        Category.SALARY to Color(0xFF388E3C),         // 深绿色
        Category.INVESTMENT to Color(0xFF3F51B5),     // 深靛蓝色
        Category.OTHER to Color(0xFF757575)           // 深灰色
    )

    fun getBackground(category: Category): Color {
        return categoryBackgroundMap[category] ?: Color(0xFFF5F5F5)
    }

    fun getForeground(category: Category): Color {
        return categoryForegroundMap[category] ?: Color(0xFF757575)
    }
}
