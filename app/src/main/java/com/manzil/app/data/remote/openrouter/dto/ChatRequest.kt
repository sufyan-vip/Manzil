package com.manzil.app.data.remote.openrouter.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<Message>,
    val temperature: Double = 0.4,
    val max_tokens: Int = 600,
    val stream: Boolean = false
) {
    @Serializable
    data class Message(val role: String, val content: String)

    companion object {
        const val DEFAULT_MODEL = "openrouter/auto"
    }
}
