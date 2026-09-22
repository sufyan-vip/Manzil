package com.manzil.app.data.remote.openrouter

import com.manzil.app.core.common.Constants
import com.manzil.app.core.common.Result
import com.manzil.app.core.crypto.SecretVault
import com.manzil.app.core.logging.AppLog
import com.manzil.app.data.remote.openrouter.dto.ChatRequest
import com.manzil.app.data.remote.openrouter.dto.ChatResponse
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * OpenRouter is the only network call this app ever makes. Everything else is local.
 * All requests are asynchronous (never on the main thread) and every failure is mapped
 * to a message a human can act on.
 */
@Singleton
class OpenRouterApi @Inject constructor(
    private val client: OkHttpClient,
    private val json: Json,
    private val secrets: SecretVault
) {
    /** Overridable so unit tests can point it at MockWebServer. */
    internal var baseUrl: String = Constants.OPENROUTER_BASE

    suspend fun listModels(): Result<List<OrModel>> {
        val key = secrets.getApiKey()
            ?: return Result.Error("No API key yet — add one in Settings to use AI features.")
        val request = Request.Builder()
            .url("$baseUrl/models")
            .header("Authorization", "Bearer $key")
            .header("HTTP-Referer", "https://github.com/sufyan-vip/Manzil")
            .header("X-Title", "Manzil")
            .get()
            .build()
        return try {
            execute(request).use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) return@use errorFor(response.code)
                val parsed = runCatching { json.decodeFromString(OrModelsResponse.serializer(), body) }
                    .getOrElse { return@use Result.Error("OpenRouter sent something unexpected. Try again.") }
                Result.Success(parsed.data)
            }
        } catch (e: IOException) {
            AppLog.e(TAG, "listModels failed", e)
            Result.Error("No internet connection — your data is safe offline.")
        } catch (e: Exception) {
            AppLog.e(TAG, "listModels failed", e)
            Result.Error("Could not reach OpenRouter. Try again.")
        }
    }

    suspend fun chat(request: ChatRequest): Result<ChatResponse> {
        val key = secrets.getApiKey()
            ?: return Result.Error("AI is off — add your free OpenRouter key in Settings.")
        val payload = json.encodeToString(ChatRequest.serializer(), request.copy(stream = false))
        val httpRequest = Request.Builder()
            .url("$baseUrl/chat/completions")
            .header("Authorization", "Bearer $key")
            .header("Content-Type", "application/json")
            .header("HTTP-Referer", "https://github.com/sufyan-vip/Manzil")
            .header("X-Title", "Manzil")
            .post(payload.toRequestBody(JSON_MEDIA))
            .build()
        return try {
            execute(httpRequest).use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) return@use errorFor(response.code)
                val parsed = runCatching { json.decodeFromString(ChatResponse.serializer(), body) }
                    .getOrElse { return@use Result.Error("The model replied in an unreadable way. Try again.") }
                val text = parsed.choices.firstOrNull()?.message?.content
                if (text.isNullOrBlank()) {
                    Result.Error("The model returned an empty answer. Try again or pick another model.")
                } else {
                    Result.Success(parsed)
                }
            }
        } catch (e: IOException) {
            AppLog.e(TAG, "chat failed", e)
            Result.Error("No internet connection — local features keep working.")
        } catch (e: Exception) {
            AppLog.e(TAG, "chat failed", e)
            Result.Error("Could not reach OpenRouter. Try again.")
        }
    }

    private fun errorFor(code: Int): Result.Error = when (code) {
        401, 403 -> Result.Error("API key is invalid or revoked. Fix it in Settings.", code)
        402 -> Result.Error("OpenRouter credits finished. Free models still work — pick one.", code)
        404 -> Result.Error("That model is not available. Pick another one in Settings.", code)
        429 -> Result.Error("Too many requests — wait a minute and try again.", code)
        in 500..599 -> Result.Error("OpenRouter is having a moment. Try again shortly.", code)
        else -> Result.Error("OpenRouter error $code. Try again.", code)
    }

    private suspend fun execute(request: Request): Response = suspendCancellableCoroutine { cont ->
        val call = client.newCall(request)
        cont.invokeOnCancellation { call.cancel() }
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (cont.isActive) cont.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (cont.isActive) cont.resume(response) else response.close()
            }
        })
    }

    private companion object {
        const val TAG = "OpenRouterApi"
        val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
    }
}
