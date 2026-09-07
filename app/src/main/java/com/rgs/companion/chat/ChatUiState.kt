package com.rgs.companion.chat

import com.rgs.companion.companion.Emotion
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val role: String, // "user", "assistant", "error"
    val proactive: Boolean = false,
    val animateTypewriter: Boolean = false,
)

data class ChatUiState(
    val emotion: Emotion = Emotion.NEUTRAL,
    val mouthLevel: Float = 0f,
    val thinking: Boolean = false,
    val listening: Boolean = false,
    val partialTranscript: String = "",
    val audioRmsLevel: Float = 0f,
    val speechError: String? = null,
    val messages: List<ChatMessage> = emptyList(),
    val greeting: String = "Hello! I am Zaiphra. How can I assist you today?",
)
