package com.manzil.app.data.remote.openrouter.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChatResponse(
    val id: String,
    val choices: List<Choice>
) {
    @Serializable
    data class Choice(val message: Message)
    @Serializable
    data class Message(val role: String, val content: String)
}
