package com.example.data.models

enum class LicenseTier(
    val id: String,
    val title: String,
    val priceInRps: String,
    val badgeColorHex: String,
    val maxTokensPerReq: Int,
    val supportsCoderSandbox: Boolean,
    val supportsActionManager: Boolean,
    val supportsUnlimitedFallback: Boolean,
    val description: String
) {
    STARTER(
        id = "starter",
        title = "IRIS FREE",
        priceInRps = "Free",
        badgeColorHex = "#38BDF8", // Cyan
        maxTokensPerReq = 4096,
        supportsCoderSandbox = true,
        supportsActionManager = true,
        supportsUnlimitedFallback = false,
        description = "IRIS Voice Core + Multimodal Vision ScreenPeeler + Gemini 3.1 access"
    ),
    PRO(
        id = "pro",
        title = "IRIS PRO",
        priceInRps = "Rs. 1,200",
        badgeColorHex = "#A855F7", // Neon Violet
        maxTokensPerReq = 16384,
        supportsCoderSandbox = true,
        supportsActionManager = true,
        supportsUnlimitedFallback = true,
        description = "IRIS Terminal Sandbox + Mobile Telekinesis + Groq & Mistral Large Models"
    ),
    GOD_MODE(
        id = "god_mode",
        title = "IRIS VIP GOD MODE",
        priceInRps = "Rs. 2,500",
        badgeColorHex = "#E040FB", // Neon Magenta / VIP Gold
        maxTokensPerReq = 65536,
        supportsCoderSandbox = true,
        supportsActionManager = true,
        supportsUnlimitedFallback = true,
        description = "Full IRIS-X Autonomous Operator + Zero-Latency Voice + Supabase HWID Master Bypass"
    );

    companion object {
        fun fromKey(key: String): LicenseTier {
            val upperKey = key.uppercase()
            return when {
                upperKey.contains("GOD") || upperKey.contains("VIP") -> GOD_MODE
                upperKey.contains("PRO") -> PRO
                else -> STARTER
            }
        }
    }
}
