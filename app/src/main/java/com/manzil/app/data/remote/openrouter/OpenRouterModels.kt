package com.manzil.app.data.remote.openrouter

data class OrModel(val id: String, val name: String, val contextLength: Int, val pricing: Pricing? = null)
data class Pricing(val prompt: String, val completion: String)
data class ChatMessage(val role: String, val content: String)
