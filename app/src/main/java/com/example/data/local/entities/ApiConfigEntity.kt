package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "api_configs")
data class ApiConfigEntity(
    @PrimaryKey val providerId: String, // glm, gemini, groq, mistral
    val providerName: String,
    val apiKey: String,
    val isEnabled: Boolean,
    val priorityOrder: Int,
    val customProxyUrl: String = "",
    val lastPingMs: Long = 0
)
