package com.example.data.network.services

import com.example.data.network.dto.GeminiGenerateRequest
import com.example.data.network.dto.GeminiGenerateResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit Service Interface for Google Gemini (Generative Language API).
 */
interface GeminiRetrofitService {

    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Body request: GeminiGenerateRequest,
        @Header("x-goog-api-key") apiKey: String? = null,
        @Query("key") queryApiKey: String? = null
    ): Response<GeminiGenerateResponse>

    @GET("v1beta/models")
    suspend fun listModels(
        @Header("x-goog-api-key") apiKey: String? = null,
        @Query("key") queryApiKey: String? = null
    ): Response<okhttp3.ResponseBody>
}
