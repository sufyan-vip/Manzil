package com.manzil.app.data.remote.openrouter

import com.manzil.app.core.common.Result
import com.manzil.app.core.crypto.SecretVault
import com.manzil.app.data.remote.openrouter.dto.ChatRequest
import com.manzil.app.data.remote.openrouter.dto.ChatResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenRouterApi @Inject constructor(
    private val client: OkHttpClient,
    private val json: Json,
    private val secrets: SecretVault
) {
    private val base = "https://openrouter.ai/api/v1"

    suspend fun listModels(): Result<List<OrModel>> {
        val key = secrets.getApiKey() ?: return Result.Error("No API key")
        val request = Request.Builder()
            .url("$base/models")
            .header("Authorization", "Bearer $key")
            .header("HTTP-Referer", "https://github.com/manzil/app")
            .header("X-Title", "Manzil")
            .get()
            .build()
        return try {
            val resp = client.newCall(request).execute()
            if (resp.isSuccessful) {
                Result.Success(emptyList())
            } else {
                when (resp.code) {
                    401, 403 -> Result.Error("API key invalid or revoked. Check it in Settings.", resp.code)
                    402 -> Result.Error("Out of OpenRouter credits. Top up at openrouter.ai/credits.", 402)
                    429 -> Result.Error("Rate limited. Wait a moment.", 429)
                    else -> Result.Error("Can't reach OpenRouter. Your data is safe — try again.", resp.code)
                }
            }
        } catch (e: Exception) {
            Result.Error("Can't reach OpenRouter. Your data is safe — try again.")
        }
    }

    suspend fun chat(req: ChatRequest): Result<ChatResponse> {
        val key = secrets.getApiKey() ?: return Result.Error("AI unavailable — add your API key in Settings")
        val body = json.encodeToString(ChatRequest.serializer(), req).toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("$base/chat/completions")
            .header("Authorization", "Bearer $key")
            .header("Content-Type", "application/json")
            .header("HTTP-Referer", "https://github.com/manzil/app")
            .header("X-Title", "Manzil")
            .post(body)
            .build()
        return try {
            val resp = client.newCall(request).execute()
            val str = resp.body?.string() ?: ""
            if (resp.isSuccessful) {
                val parsed = json.decodeFromString(ChatResponse.serializer(), str)
                Result.Success(parsed)
            } else {
                Result.Error("AI error: ${resp.code}", resp.code)
            }
        } catch (e: Exception) {
            Result.Error("Can't reach OpenRouter")
        }
    }

    fun chatStream(req: ChatRequest): Flow<String> = flow {
        // SSE streaming implementation placeholder
        emit("Streaming not yet implemented — using non-stream fallback")
    }
}
