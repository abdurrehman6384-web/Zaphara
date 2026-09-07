package com.example.data.models

data class DeviceTelemetry(
    val hwid: String,
    val deviceModel: String,
    val androidVersion: String,
    val cpuUsagePercent: Int,
    val ramUsedMb: Long,
    val ramTotalMb: Long,
    val storageAvailableGb: Float,
    val networkLatencyMs: Long,
    val activeAgent: AgentType,
    val activeProvider: LLMProvider
)
