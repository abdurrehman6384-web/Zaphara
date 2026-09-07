package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val sender: String, // "USER" or "AGENT"
    val agentType: String, // AGENT_MANAGER, ACTION_MANAGER, AI_CODER, API_MANAGER
    val content: String,
    val codeSnippet: String? = null,
    val codeLanguage: String? = null,
    val modelUsed: String = "GLM-4.6 / Gemini",
    val timestamp: Long = System.currentTimeMillis()
)
