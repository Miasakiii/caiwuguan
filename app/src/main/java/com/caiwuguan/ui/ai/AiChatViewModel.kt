package com.caiwuguan.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caiwuguan.ai.ChatContextBuilder
import com.caiwuguan.ai.deepseek.ChatMessage
import com.caiwuguan.ai.deepseek.DeepSeekClient
import com.caiwuguan.data.db.dao.ChatDao
import com.caiwuguan.data.db.entity.ChatConversationEntity
import com.caiwuguan.data.db.entity.ChatMessageEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val deepSeekClient: DeepSeekClient,
    private val chatContextBuilder: ChatContextBuilder,
    private val chatDao: ChatDao
) : ViewModel() {

    // 当前对话 ID（null 表示新对话）
    private val _conversationId = MutableStateFlow<Long?>(null)
    val conversationId: StateFlow<Long?> = _conversationId.asStateFlow()

    // 消息列表
    private val _messages = MutableStateFlow<List<UiMessage>>(emptyList())
    val messages: StateFlow<List<UiMessage>> = _messages.asStateFlow()

    // 是否正在流式输出
    private val _isStreaming = MutableStateFlow(false)
    val isStreaming: StateFlow<Boolean> = _isStreaming.asStateFlow()

    // 输入文本
    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    // 对话列表（侧边栏）
    private val _conversations = MutableStateFlow<List<ChatConversationEntity>>(emptyList())
    val conversations: StateFlow<List<ChatConversationEntity>> = _conversations.asStateFlow()

    // 错误消息
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        // 加载对话列表
        viewModelScope.launch {
            chatDao.getAllConversations().collect { _conversations.value = it }
        }
    }

    fun updateInputText(text: String) {
        _inputText.value = text
    }

    fun clearError() {
        _error.value = null
    }

    /**
     * 发送消息
     */
    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isEmpty() || _isStreaming.value) return

        _inputText.value = ""

        viewModelScope.launch {
            try {
                // 如果没有当前对话，创建新对话
                val convId = _conversationId.value ?: run {
                    val title = text.take(20) + if (text.length > 20) "..." else ""
                    val newId = chatDao.insertConversation(
                        ChatConversationEntity(title = title)
                    )
                    _conversationId.value = newId
                    newId
                }

                // 保存用户消息到 Room
                val userMsgEntity = ChatMessageEntity(
                    conversationId = convId,
                    role = "user",
                    content = text
                )
                val userMsgId = chatDao.insertMessage(userMsgEntity)

                // 更新 UI
                val userMsg = UiMessage(id = userMsgId, role = "user", content = text)
                _messages.value = _messages.value + userMsg

                // 添加 AI 消息占位（流式状态）
                val aiPlaceholder = UiMessage(
                    id = -1,
                    role = "assistant",
                    content = "",
                    isStreaming = true
                )
                _messages.value = _messages.value + aiPlaceholder
                _isStreaming.value = true

                // 构建 RAG 上下文
                val systemPrompt = chatContextBuilder.buildSystemPrompt()

                // 构建消息历史
                val historyMessages = _messages.value
                    .filter { !it.isStreaming }
                    .map { ChatMessage(role = it.role, content = it.content) }

                val apiMessages = listOf(
                    ChatMessage(role = "system", content = systemPrompt)
                ) + historyMessages

                // 流式调用
                val contentBuilder = StringBuilder()
                deepSeekClient.chatStream(apiMessages).collect { token ->
                    contentBuilder.append(token)
                    // 更新 AI 消息内容
                    _messages.value = _messages.value.map { msg ->
                        if (msg.isStreaming) msg.copy(content = contentBuilder.toString())
                        else msg
                    }
                }

                // 流结束，保存 AI 回复到 Room
                val aiContent = contentBuilder.toString()
                if (aiContent.isNotEmpty()) {
                    val aiMsgEntity = ChatMessageEntity(
                        conversationId = convId,
                        role = "assistant",
                        content = aiContent
                    )
                    val aiMsgId = chatDao.insertMessage(aiMsgEntity)

                    // 更新 UI（取消流式状态，赋正式 ID）
                    _messages.value = _messages.value.map { msg ->
                        if (msg.isStreaming) msg.copy(id = aiMsgId, isStreaming = false)
                        else msg
                    }

                    // 更新对话时间戳
                    chatDao.updateConversationTimestamp(convId)
                } else {
                    // 移除空的 AI 占位
                    _messages.value = _messages.value.filter { !it.isStreaming }
                }

                _isStreaming.value = false
            } catch (e: Exception) {
                _isStreaming.value = false
                // 移除流式占位
                _messages.value = _messages.value.filter { !it.isStreaming }
                _error.value = e.message ?: "发送失败"
            }
        }
    }

    /**
     * 新建对话
     */
    fun newConversation() {
        _conversationId.value = null
        _messages.value = emptyList()
        _error.value = null
    }

    /**
     * 加载历史对话
     */
    fun loadConversation(conversationId: Long) {
        _conversationId.value = conversationId
        _error.value = null

        viewModelScope.launch {
            val entities = chatDao.getMessagesByConversation(conversationId).first()
            _messages.value = entities.map { entity ->
                UiMessage(
                    id = entity.id,
                    role = entity.role,
                    content = entity.content
                )
            }
        }
    }

    /**
     * 删除对话
     */
    fun deleteConversation(conversationId: Long) {
        viewModelScope.launch {
            chatDao.deleteConversation(conversationId)
            // 如果删除的是当前对话，清空
            if (_conversationId.value == conversationId) {
                newConversation()
            }
        }
    }
}
