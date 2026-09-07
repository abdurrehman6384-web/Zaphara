package com.example.data.models

enum class AgentType(
    val id: String,
    val title: String,
    val description: String,
    val colorHex: String,
    val primaryRole: String
) {
    RGS_CORE(
        id = "rgs_core",
        title = "RGS Core",
        description = "Autonomous voice-first central operator & multi-agent orchestrator",
        colorHex = "#00F2FE", // RGS Neon Cyan
        primaryRole = "Master Operator"
    ),
    RGS_VISION(
        id = "rgs_vision",
        title = "RGS Vision & OCR",
        description = "ScreenPeeler visual context, AR camera scanner & multimodal reasoning",
        colorHex = "#38BDF8", // Sky Cyan
        primaryRole = "Visual Intelligence"
    ),
    RGS_TERMINAL(
        id = "rgs_terminal",
        title = "RGS-Zero Terminal",
        description = "Executes shell commands, scripts, code synthesis & sandboxed compiler",
        colorHex = "#00FF87", // Terminal Neon Green
        primaryRole = "Shell & Code Scaffolder"
    ),
    RGS_TELEKINESIS(
        id = "rgs_telekinesis",
        title = "RGS Telekinesis",
        description = "Hardware automation, flashlight, battery, RAM, ADB controls & device telemetry",
        colorHex = "#FF9900", // Cyber Amber
        primaryRole = "Device Operator"
    ),
    RGS_CYBERSEC(
        id = "rgs_cybersec",
        title = "RGS CyberSec & HWID",
        description = "Hardware identifier gating, cryptographic signature & Supabase database vault",
        colorHex = "#E040FB", // Magenta/Violet
        primaryRole = "Security & Licensing"
    ),

    // Backward-compatible aliases
    IRIS_CORE(
        id = "iris_core",
        title = "RGS Core",
        description = "Autonomous voice-first central operator & multi-agent orchestrator",
        colorHex = "#00F2FE",
        primaryRole = "Master Operator"
    ),
    IRIS_VISION(
        id = "iris_vision",
        title = "RGS Vision & OCR",
        description = "ScreenPeeler visual context, AR camera scanner & multimodal reasoning",
        colorHex = "#38BDF8",
        primaryRole = "Visual Intelligence"
    ),
    IRIS_TERMINAL(
        id = "iris_terminal",
        title = "RGS-Zero Terminal",
        description = "Executes shell commands, scripts, code synthesis & sandboxed compiler",
        colorHex = "#00FF87",
        primaryRole = "Shell & Code Scaffolder"
    ),
    IRIS_TELEKINESIS(
        id = "iris_telekinesis",
        title = "RGS Telekinesis",
        description = "Hardware automation, flashlight, battery, RAM, ADB controls & device telemetry",
        colorHex = "#FF9900",
        primaryRole = "Device Operator"
    ),
    IRIS_CYBERSEC(
        id = "iris_cybersec",
        title = "RGS CyberSec & HWID",
        description = "Hardware identifier gating, cryptographic signature & Supabase database vault",
        colorHex = "#E040FB",
        primaryRole = "Security & Licensing"
    ),
    AGENT_MANAGER(
        id = "agent_manager",
        title = "RGS Core (Agent Manager)",
        description = "Autonomous voice-first central operator & multi-agent orchestrator",
        colorHex = "#00F2FE",
        primaryRole = "Master Operator"
    ),
    ACTION_MANAGER(
        id = "action_manager",
        title = "RGS Telekinesis",
        description = "Hardware automation, flashlight, battery, RAM, ADB controls & device telemetry",
        colorHex = "#FF9900",
        primaryRole = "Device Operator"
    ),
    AI_CODER(
        id = "ai_coder",
        title = "RGS-Zero Coder & Shell",
        description = "Generates Kotlin/Python/Rust code, inspects bugs & executes shell sandbox",
        colorHex = "#00FF87",
        primaryRole = "Code Architect"
    ),
    API_MANAGER(
        id = "api_manager",
        title = "RGS Model Router",
        description = "Manages GLM-4.6, Gemini 3.1 Live, Groq Llama 3.3 & Mistral Large routing",
        colorHex = "#8B5CF6",
        primaryRole = "Model Router"
    );

    companion object {
        fun fromId(id: String): AgentType {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: RGS_CORE
        }
    }
}
