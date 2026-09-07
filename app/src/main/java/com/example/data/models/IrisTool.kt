package com.example.data.models

enum class IrisToolType(
    val id: String,
    val title: String,
    val category: String,
    val iconName: String,
    val description: String,
    val colorHex: String,
    val requiresPro: Boolean
) {
    VOICE_STREAM(
        id = "voice_stream",
        title = "IRIS Voice Core",
        category = "Voice & Audio",
        iconName = "Mic",
        description = "Bidirectional voice interaction, wake-word activation & speech-to-speech",
        colorHex = "#00F2FE",
        requiresPro = false
    ),
    SCREEN_PEELER(
        id = "screen_peeler",
        title = "ScreenPeeler (Vision OCR)",
        category = "Multimodal Vision",
        iconName = "CropFree",
        description = "Instant camera viewfinder & screen OCR coordinate text/code extraction",
        colorHex = "#38BDF8",
        requiresPro = false
    ),
    TERMINAL_SHELL(
        id = "terminal_shell",
        title = "IRIS-Zero Terminal",
        category = "Execution Sandbox",
        iconName = "Terminal",
        description = "Local & cloud shell sandbox executing curl, python, git, bash commands",
        colorHex = "#00FF87",
        requiresPro = true
    ),
    MOBILE_TELEKINESIS(
        id = "mobile_telekinesis",
        title = "Mobile Telekinesis",
        category = "Device Automation",
        iconName = "PhonelinkSetup",
        description = "Control device hardware, battery telemetry, RAM optimizer & flashlight",
        colorHex = "#FF9900",
        requiresPro = false
    ),
    WEB_GROUNDING(
        id = "web_grounding",
        title = "Live Web Intelligence",
        category = "Search & Knowledge",
        iconName = "TravelExplore",
        description = "Deep search engine queries, real-time live grounding & URL scrapers",
        colorHex = "#8B5CF6",
        requiresPro = false
    ),
    CODE_ARCHITECT(
        id = "code_architect",
        title = "Code Architect",
        category = "Developer Tools",
        iconName = "Code",
        description = "Scaffold complete Kotlin, Python, JS projects with syntax verification",
        colorHex = "#00F2FE",
        requiresPro = true
    ),
    WORKFLOW_CHAIN(
        id = "workflow_chain",
        title = "Autonomous Workflows",
        category = "Automation",
        iconName = "AccountTree",
        description = "Execute multi-step autonomous pipelines with progress verification",
        colorHex = "#E040FB",
        requiresPro = true
    )
}

data class IrisSystemToolAction(
    val id: String,
    val toolType: IrisToolType,
    val name: String,
    val parameterPrompt: String,
    val executionCommand: String,
    val sampleOutput: String
)
