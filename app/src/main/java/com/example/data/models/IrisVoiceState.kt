package com.example.data.models

enum class RgsVoiceState(
    val statusText: String,
    val colorHex: String,
    val pulseFrequencyMs: Long
) {
    IDLE("RGS READY · STANDBY", "#00F2FE", 2000L),
    LISTENING("LISTENING TO VOICE STREAM...", "#00FF87", 800L),
    THINKING("PROCESSING MULTIMODAL INTENT...", "#A855F7", 400L),
    SPEAKING("STREAMING BIDIRECTIONAL AUDIO...", "#38BDF8", 600L),
    EXECUTING("EXECUTING AUTONOMOUS TOOL...", "#FF9900", 300L)
}

typealias IrisVoiceState = RgsVoiceState

data class TerminalLog(
    val id: String = java.util.UUID.randomUUID().toString(),
    val command: String,
    val output: String,
    val exitCode: Int = 0,
    val timestamp: String = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date()),
    val isError: Boolean = exitCode != 0,
    val executionTimeMs: Long = (120..480).random().toLong()
)
