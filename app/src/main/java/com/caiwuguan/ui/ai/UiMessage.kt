package com.caiwuguan.ui.ai

/**
 * 聊天消息的 UI 模型
 */
data class UiMessage(
    val id: Long = 0,
    val role: String,       // "user" / "assistant"
    val content: String,
    val isStreaming: Boolean = false
)
