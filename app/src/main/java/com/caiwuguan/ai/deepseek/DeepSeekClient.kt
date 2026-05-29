package com.caiwuguan.ai.deepseek

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class ChatMessage(
    val role: String,
    val content: String
)

@Serializable
data class ChatRequest(
    val model: String = "deepseek-chat",
    val messages: List<ChatMessage>,
    val temperature: Double = 0.7,
    val max_tokens: Int = 1024,
    val stream: Boolean = false
)

@Serializable
data class ChatChoice(
    val index: Int,
    val message: ChatMessage,
    val finish_reason: String
)

@Serializable
data class ChatResponse(
    val id: String,
    val choices: List<ChatChoice>
)

// 流式响应模型
@Serializable
data class StreamDelta(
    val role: String? = null,
    val content: String? = null
)

@Serializable
data class StreamChoice(
    val index: Int,
    val delta: StreamDelta,
    val finish_reason: String? = null
)

@Serializable
data class ChatStreamChunk(
    val id: String,
    val choices: List<StreamChoice>
)

@Singleton
class DeepSeekClient @Inject constructor(
    private val apiKeyManager: ApiKeyManager
) {

    companion object {
        private const val BASE_URL = "https://api.deepseek.com/v1"
        private const val CHAT_ENDPOINT = "$BASE_URL/chat/completions"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun chat(messages: List<ChatMessage>): Result<String> = withContext(Dispatchers.IO) {
        try {
            val key = apiKeyManager.getApiKey() ?: return@withContext Result.failure(Exception("API Key 未设置，请在设置中配置"))

            val request = ChatRequest(messages = messages)
            val requestBody = json.encodeToString(ChatRequest.serializer(), request)

            val httpRequest = Request.Builder()
                .url(CHAT_ENDPOINT)
                .addHeader("Authorization", "Bearer $key")
                .addHeader("Content-Type", "application/json")
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(httpRequest).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("API 请求失败: ${response.code} $responseBody"))
            }

            if (responseBody == null) {
                return@withContext Result.failure(Exception("响应为空"))
            }

            val chatResponse = json.decodeFromString(ChatResponse.serializer(), responseBody)
            val content = chatResponse.choices.firstOrNull()?.message?.content

            if (content != null) {
                Result.success(content)
            } else {
                Result.failure(Exception("无法解析响应"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 流式对话，逐 token 返回内容
     * 返回 Flow<String>，每个元素是一个 token 片段
     */
    fun chatStream(messages: List<ChatMessage>): Flow<String> = callbackFlow {
        val job = launch(Dispatchers.IO) {
            try {
                val key = apiKeyManager.getApiKey()
                if (key == null) {
                    close(Exception("API Key 未设置，请在设置中配置"))
                    return@launch
                }

                val request = ChatRequest(messages = messages, stream = true)
                val requestBody = json.encodeToString(ChatRequest.serializer(), request)

                val httpRequest = Request.Builder()
                    .url(CHAT_ENDPOINT)
                    .addHeader("Authorization", "Bearer $key")
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody.toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(httpRequest).execute()

                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: ""
                    close(Exception("API 请求失败: ${response.code} $errorBody"))
                    return@launch
                }

                val source = response.body?.source()
                if (source == null) {
                    close(Exception("响应为空"))
                    return@launch
                }

                try {
                    while (!source.exhausted()) {
                        val line = source.readUtf8Line() ?: break

                        if (line.startsWith("data: ")) {
                            val data = line.removePrefix("data: ").trim()

                            if (data == "[DONE]") {
                                break
                            }

                            try {
                                val chunk = json.decodeFromString(ChatStreamChunk.serializer(), data)
                                val content = chunk.choices.firstOrNull()?.delta?.content
                                if (content != null) {
                                    trySend(content)
                                }
                            } catch (_: Exception) {
                                // 忽略解析错误的行
                            }
                        }
                    }
                } finally {
                    source.close()
                    response.close()
                }

                close()
            } catch (e: Exception) {
                close(e)
            }
        }

        awaitClose { job.cancel() }
    }
}
