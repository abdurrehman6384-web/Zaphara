package com.example.data.network.interceptors

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Header interceptor for Groq, Mistral, and OpenAI-compatible endpoints.
 * Injects `Authorization: Bearer <apiKey>` into outgoing HTTP requests.
 */
class BearerAuthInterceptor(
    private val apiKeyProvider: () -> String?
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val key = apiKeyProvider()?.trim()

        val requestBuilder = originalRequest.newBuilder()
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")

        if (!key.isNullOrBlank() && originalRequest.header("Authorization") == null) {
            requestBuilder.header("Authorization", "Bearer $key")
        }

        return chain.proceed(requestBuilder.build())
    }
}

/**
 * Interceptor for Google Gemini REST API.
 * Supports injecting `x-goog-api-key` header or `key` query parameter.
 */
class GeminiApiKeyInterceptor(
    private val apiKeyProvider: () -> String?
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val key = apiKeyProvider()?.trim()

        val requestBuilder = originalRequest.newBuilder()
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")

        if (!key.isNullOrBlank()) {
            if (originalRequest.header("x-goog-api-key") == null) {
                requestBuilder.header("x-goog-api-key", key)
            }
        }

        return chain.proceed(requestBuilder.build())
    }
}

/**
 * Multi-model dynamic interceptor that inspects the destination host or custom headers
 * and applies the appropriate authentication headers for Gemini, Groq, or Mistral.
 */
class MultiModelApiKeyInterceptor(
    private val geminiKeyProvider: () -> String?,
    private val groqKeyProvider: () -> String?,
    private val mistralKeyProvider: () -> String?
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val host = originalRequest.url.host.lowercase()
        val requestBuilder = originalRequest.newBuilder()
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")

        when {
            host.contains("generativelanguage.googleapis.com") -> {
                val key = geminiKeyProvider()?.trim()
                if (!key.isNullOrBlank() && originalRequest.header("x-goog-api-key") == null) {
                    requestBuilder.header("x-goog-api-key", key)
                }
            }
            host.contains("groq.com") -> {
                val key = groqKeyProvider()?.trim()
                if (!key.isNullOrBlank() && originalRequest.header("Authorization") == null) {
                    requestBuilder.header("Authorization", "Bearer $key")
                }
            }
            host.contains("mistral.ai") -> {
                val key = mistralKeyProvider()?.trim()
                if (!key.isNullOrBlank() && originalRequest.header("Authorization") == null) {
                    requestBuilder.header("Authorization", "Bearer $key")
                }
            }
        }

        return chain.proceed(requestBuilder.build())
    }
}
