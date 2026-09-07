package com.example.data.models

enum class LLMProvider(
    val id: String,
    val shortName: String,
    val displayName: String,
    val defaultModel: String,
    val iconResName: String,
    val endpointUrl: String,
    val latencyMs: Long,
    val contextWindow: String,
    val tagline: String,
    val isFreeTierFriendly: Boolean
) {
    GLM_4_6(
        id = "glm",
        shortName = "GLM-4.6",
        displayName = "GLM-4.6 (Zhipu / Z.ai)",
        defaultModel = "glm-4-flash",
        iconResName = "ic_glm",
        endpointUrl = "https://open.bigmodel.cn/api/paas/v4/chat/completions",
        latencyMs = 42L,
        contextWindow = "128k Tokens",
        tagline = "High-precision bilingual reasoning & agent orchestration",
        isFreeTierFriendly = true
    ),
    GEMINI(
        id = "gemini",
        shortName = "Gemini",
        displayName = "Gemini AI (Google)",
        defaultModel = "gemini-3.5-flash",
        iconResName = "ic_gemini",
        endpointUrl = "https://generativelanguage.googleapis.com/v1beta",
        latencyMs = 58L,
        contextWindow = "1M Tokens",
        tagline = "Ultra-fast multimodal reasoning & code synthesis",
        isFreeTierFriendly = true
    ),
    GROQ(
        id = "groq",
        shortName = "Groq",
        displayName = "Groq LPU (Llama 3.3)",
        defaultModel = "llama-3.3-70b-versatile",
        iconResName = "ic_groq",
        endpointUrl = "https://api.groq.com/openai/v1/chat/completions",
        latencyMs = 26L,
        contextWindow = "128k Tokens",
        tagline = "Lightning-fast LPUs with 500+ tokens/sec throughput",
        isFreeTierFriendly = true
    ),
    MISTRAL(
        id = "mistral",
        shortName = "Mistral",
        displayName = "Mistral AI (Codestral)",
        defaultModel = "mistral-small-latest",
        iconResName = "ic_mistral",
        endpointUrl = "https://api.mistral.ai/v1/chat/completions",
        latencyMs = 72L,
        contextWindow = "64k Tokens",
        tagline = "Open-weights efficiency, structured coding & European privacy",
        isFreeTierFriendly = true
    );

    companion object {
        fun fromId(id: String): LLMProvider {
            return entries.find { it.id.equals(id, ignoreCase = true) || it.shortName.equals(id, ignoreCase = true) } ?: GLM_4_6
        }
    }
}
