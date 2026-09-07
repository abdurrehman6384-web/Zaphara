package com.example.data.network.services

import com.example.data.network.dto.ChatCompletionRequest
import com.example.data.network.dto.ChatCompletionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Retrofit Service Interface for Groq LPU Inference API (OpenAI Compatible).
 * Base URL: https://api.groq.com/openai/
 */
interface GroqRetrofitService {

    @POST("v1/chat/completions")
    suspend fun createChatCompletion(
        @Body request: ChatCompletionRequest,
        @Header("Authorization") authorization: String? = null
    ): Response<ChatCompletionResponse>

    @GET("v1/models")
    suspend fun listModels(
        @Header("Authorization") authorization: String? = null
    ): Response<okhttp3.ResponseBody>
}
