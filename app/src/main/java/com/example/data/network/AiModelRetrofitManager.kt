package com.example.data.network

import com.example.data.models.LLMProvider
import com.example.data.network.dto.ChatCompletionRequest
import com.example.data.network.dto.ChatMessageDto
import com.example.data.network.dto.GeminiContentDto
import com.example.data.network.dto.GeminiGenerateRequest
import com.example.data.network.dto.GeminiGenerationConfigDto
import com.example.data.network.dto.GeminiPartDto
import com.example.data.network.interceptors.BearerAuthInterceptor
import com.example.data.network.interceptors.GeminiApiKeyInterceptor
import com.example.data.network.services.GeminiRetrofitService
import com.example.data.network.services.GroqRetrofitService
import com.example.data.network.services.MistralRetrofitService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Central Retrofit Client & Manager for AI Models (Gemini, Groq, Mistral).
 * Manages OkHttpClient instances, API key header interceptors, and Retrofit service interfaces.
 */
class AiModelRetrofitManager(
    private var geminiKeyProvider: () -> String? = { null },
    private var groqKeyProvider: () -> String? = { null },
    private var mistralKeyProvider: () -> String? = { null }
) {

    private var activeProvider: LLMProvider = LLMProvider.GEMINI

    fun setActiveProvider(provider: LLMProvider) {
        activeProvider = provider
    }

    fun getActiveProvider(): LLMProvider = activeProvider

    fun updateApiKeys(gemini: String? = null, groq: String? = null, mistral: String? = null) {
        if (gemini != null) geminiKeyProvider = { gemini }
        if (groq != null) groqKeyProvider = { groq }
        if (mistral != null) mistralKeyProvider = { mistral }
    }

    fun getActiveHeaders(provider: LLMProvider = activeProvider, apiKeyOverride: String? = null): Map<String, String> {
        val headers = mutableMapOf(
            "Content-Type" to "application/json",
            "Accept" to "application/json",
            "User-Agent" to "RGS-AI-Retrofit/3.1"
        )
        when (provider) {
            LLMProvider.GEMINI -> {
                val key = apiKeyOverride?.trim() ?: geminiKeyProvider()?.trim()
                if (!key.isNullOrBlank()) {
                    headers["x-goog-api-key"] = if (key.length > 8) "${key.take(4)}••••${key.takeLast(4)}" else "••••••••"
                } else {
                    headers["x-goog-api-key"] = "[NOT SET - USING FALLBACK]"
                }
            }
            LLMProvider.GROQ -> {
                val key = apiKeyOverride?.trim() ?: groqKeyProvider()?.trim()
                if (!key.isNullOrBlank()) {
                    headers["Authorization"] = "Bearer " + if (key.length > 8) "${key.take(4)}••••${key.takeLast(4)}" else "••••••••"
                } else {
                    headers["Authorization"] = "[NOT SET - SIMULATED]"
                }
            }
            LLMProvider.MISTRAL -> {
                val key = apiKeyOverride?.trim() ?: mistralKeyProvider()?.trim()
                if (!key.isNullOrBlank()) {
                    headers["Authorization"] = "Bearer " + if (key.length > 8) "${key.take(4)}••••${key.takeLast(4)}" else "••••••••"
                } else {
                    headers["Authorization"] = "[NOT SET - SIMULATED]"
                }
            }
            LLMProvider.GLM_4_6 -> {
                headers["Authorization"] = "Bearer [ZHIPU_AI_TOKEN]"
            }
        }
        return headers
    }

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    // --- OkHttp Clients with Specific Header Interceptors ---

    private fun createBaseOkHttpClient(): OkHttpClient.Builder {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
    }

    private val geminiClient: OkHttpClient by lazy {
        createBaseOkHttpClient()
            .addInterceptor(GeminiApiKeyInterceptor(geminiKeyProvider))
            .build()
    }

    private val groqClient: OkHttpClient by lazy {
        createBaseOkHttpClient()
            .addInterceptor(BearerAuthInterceptor(groqKeyProvider))
            .build()
    }

    private val mistralClient: OkHttpClient by lazy {
        createBaseOkHttpClient()
            .addInterceptor(BearerAuthInterceptor(mistralKeyProvider))
            .build()
    }

    // --- Retrofit Services ---

    val geminiService: GeminiRetrofitService by lazy {
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(geminiClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiRetrofitService::class.java)
    }

    val groqService: GroqRetrofitService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.groq.com/openai/")
            .client(groqClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GroqRetrofitService::class.java)
    }

    val mistralService: MistralRetrofitService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.mistral.ai/")
            .client(mistralClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(MistralRetrofitService::class.java)
    }

    // --- High-Level Calling API ---

    /**
     * Executes content generation via Google Gemini Retrofit service.
     */
    suspend fun generateGemini(
        prompt: String,
        systemInstruction: String? = null,
        apiKey: String? = null,
        model: String = "gemini-2.5-flash"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val key = apiKey?.trim() ?: geminiKeyProvider()?.trim()
            val request = GeminiGenerateRequest(
                contents = listOf(
                    GeminiContentDto(
                        parts = listOf(GeminiPartDto(text = prompt)),
                        role = "user"
                    )
                ),
                systemInstruction = systemInstruction?.let {
                    GeminiContentDto(parts = listOf(GeminiPartDto(text = it)))
                },
                generationConfig = GeminiGenerationConfigDto(
                    temperature = 0.7f,
                    maxOutputTokens = 4096
                )
            )

            val response = geminiService.generateContent(
                model = model,
                request = request,
                apiKey = key,
                queryApiKey = key
            )

            if (response.isSuccessful) {
                val body = response.body()
                val text = body?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!text.isNullOrBlank()) {
                    Result.success(text)
                } else {
                    Result.failure(Exception("Gemini returned empty candidate response"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "HTTP ${response.code()}"
                Result.failure(Exception("Gemini API Error (${response.code()}): $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Executes chat completion via Groq LPU Retrofit service.
     */
    suspend fun generateGroq(
        prompt: String,
        systemInstruction: String? = null,
        apiKey: String? = null,
        model: String = "llama-3.3-70b-versatile"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val key = apiKey?.trim() ?: groqKeyProvider()?.trim()
            val authHeader = if (!key.isNullOrBlank()) "Bearer $key" else null

            val messages = mutableListOf<ChatMessageDto>()
            if (!systemInstruction.isNullOrBlank()) {
                messages.add(ChatMessageDto(role = "system", content = systemInstruction))
            }
            messages.add(ChatMessageDto(role = "user", content = prompt))

            val request = ChatCompletionRequest(
                model = model,
                messages = messages,
                temperature = 0.7f,
                maxTokens = 4096
            )

            val response = groqService.createChatCompletion(
                request = request,
                authorization = authHeader
            )

            if (response.isSuccessful) {
                val body = response.body()
                val content = body?.choices?.firstOrNull()?.message?.content
                if (!content.isNullOrBlank()) {
                    Result.success(content)
                } else {
                    Result.failure(Exception("Groq returned empty completion"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "HTTP ${response.code()}"
                Result.failure(Exception("Groq API Error (${response.code()}): $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Executes chat completion via Mistral AI Retrofit service.
     */
    suspend fun generateMistral(
        prompt: String,
        systemInstruction: String? = null,
        apiKey: String? = null,
        model: String = "mistral-small-latest"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val key = apiKey?.trim() ?: mistralKeyProvider()?.trim()
            val authHeader = if (!key.isNullOrBlank()) "Bearer $key" else null

            val messages = mutableListOf<ChatMessageDto>()
            if (!systemInstruction.isNullOrBlank()) {
                messages.add(ChatMessageDto(role = "system", content = systemInstruction))
            }
            messages.add(ChatMessageDto(role = "user", content = prompt))

            val request = ChatCompletionRequest(
                model = model,
                messages = messages,
                temperature = 0.7f,
                maxTokens = 4096
            )

            val response = mistralService.createChatCompletion(
                request = request,
                authorization = authHeader
            )

            if (response.isSuccessful) {
                val body = response.body()
                val content = body?.choices?.firstOrNull()?.message?.content
                if (!content.isNullOrBlank()) {
                    Result.success(content)
                } else {
                    Result.failure(Exception("Mistral returned empty completion"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "HTTP ${response.code()}"
                Result.failure(Exception("Mistral API Error (${response.code()}): $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Measures live network ping latency to the specified provider endpoint.
     */
    suspend fun pingProvider(provider: LLMProvider, apiKey: String? = null): Long = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            val key = apiKey?.trim()
            when (provider) {
                LLMProvider.GEMINI -> {
                    val res = geminiService.listModels(apiKey = key, queryApiKey = key)
                    if (res.code() < 500) System.currentTimeMillis() - startTime else -1L
                }
                LLMProvider.GROQ -> {
                    val auth = if (!key.isNullOrBlank()) "Bearer $key" else null
                    val res = groqService.listModels(authorization = auth)
                    if (res.code() < 500) System.currentTimeMillis() - startTime else -1L
                }
                LLMProvider.MISTRAL -> {
                    val auth = if (!key.isNullOrBlank()) "Bearer $key" else null
                    val res = mistralService.listModels(authorization = auth)
                    if (res.code() < 500) System.currentTimeMillis() - startTime else -1L
                }
                else -> {
                    provider.latencyMs
                }
            }
        } catch (e: Exception) {
            -1L
        }
    }
}
