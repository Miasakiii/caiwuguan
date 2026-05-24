package com.caiwuguan.domain.model

enum class Category(val displayName: String, val icon: String) {
    FOOD("餐饮", "🍜"),
    TRANSPORT("交通", "🚗"),
    SHOPPING("购物", "🛒"),
    ENTERTAINMENT("娱乐", "🎮"),
    HOUSING("住房", "🏠"),
    MEDICAL("医疗", "💊"),
    EDUCATION("教育", "📚"),
    TRANSFER("转账", "💸"),
    RED_PACKET("红包", "🧧"),
    SALARY("工资", "💰"),
    INVESTMENT("投资", "📈"),
    OTHER("其他", "📦")
}
