package com.mentalgym.app.data.remote.groq

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class GroqChatRequest(
    val model: String,
    val messages: List<GroqChatMessage>,
    val temperature: Double = 0.35,
    @SerialName("response_format")
    val responseFormat: GroqJsonObjectFormat? = GroqJsonObjectFormat()
)

@Serializable
data class GroqJsonObjectFormat(val type: String = "json_object")

@Serializable
data class GroqChatMessage(
    val role: String,
    val content: String
)

@Serializable
data class GroqChatResponse(
    val choices: List<GroqChoiceDto> = emptyList()
)

@Serializable
data class GroqChoiceDto(
    val message: GroqAssistantMessage? = null
)

@Serializable
data class GroqAssistantMessage(
    val content: String? = null
)

@Singleton
class GroqChatClient @Inject constructor(
    private val client: HttpClient
) {

    suspend fun completeChat(
        apiKey: String,
        model: String,
        systemPrompt: String,
        userPrompt: String
    ): String {
        val response = client.post(CHAT_URL) {
            header(HttpHeaders.Authorization, "Bearer $apiKey")
            contentType(ContentType.Application.Json)
            setBody(
                GroqChatRequest(
                    model = model,
                    messages = listOf(
                        GroqChatMessage("system", systemPrompt),
                        GroqChatMessage("user", userPrompt)
                    )
                )
            )
        }
        if (!response.status.isSuccess()) {
            error("Groq request failed: HTTP ${response.status.value}")
        }
        val body = response.body<GroqChatResponse>()
        return body.choices.firstOrNull()?.message?.content
            ?: error("Groq returned no message content")
    }

    companion object {
        private const val CHAT_URL = "https://api.groq.com/openai/v1/chat/completions"
    }
}
