package com.example.data.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ==============================================================
// Chat Completion DTOs (Compatible with Groq, Mistral, and OpenAI)
// ==============================================================

@JsonClass(generateAdapter = true)
data class ChatCompletionRequest(
    @Json(name = "model") val model: String,
    @Json(name = "messages") val messages: List<ChatMessageDto>,
    @Json(name = "temperature") val temperature: Float? = 0.7f,
    @Json(name = "max_tokens") val maxTokens: Int? = 4096,
    @Json(name = "top_p") val topP: Float? = null,
    @Json(name = "stream") val stream: Boolean? = false
)

@JsonClass(generateAdapter = true)
data class ChatMessageDto(
    @Json(name = "role") val role: String, // "system", "user", "assistant"
    @Json(name = "content") val content: String
)

@JsonClass(generateAdapter = true)
data class ChatCompletionResponse(
    @Json(name = "id") val id: String? = null,
    @Json(name = "model") val model: String? = null,
    @Json(name = "choices") val choices: List<ChatChoiceDto>? = null,
    @Json(name = "usage") val usage: ChatUsageDto? = null,
    @Json(name = "error") val error: ChatErrorDto? = null
)

@JsonClass(generateAdapter = true)
data class ChatChoiceDto(
    @Json(name = "index") val index: Int? = null,
    @Json(name = "message") val message: ChatMessageDto? = null,
    @Json(name = "finish_reason") val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class ChatUsageDto(
    @Json(name = "prompt_tokens") val promptTokens: Int? = null,
    @Json(name = "completion_tokens") val completionTokens: Int? = null,
    @Json(name = "total_tokens") val totalTokens: Int? = null
)

@JsonClass(generateAdapter = true)
data class ChatErrorDto(
    @Json(name = "message") val message: String? = null,
    @Json(name = "type") val type: String? = null,
    @Json(name = "code") val code: String? = null
)
