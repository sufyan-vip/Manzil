package com.manzil.app.data.remote.openrouter

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrModel(
    val id: String = "",
    val name: String = "",
    @SerialName("context_length") val contextLength: Int = 0,
    val pricing: Pricing? = null
) {
    val isFree: Boolean
        get() = pricing?.let { p ->
            (p.prompt.toDoubleOrNull() ?: 0.0) == 0.0 && (p.completion.toDoubleOrNull() ?: 0.0) == 0.0
        } ?: id.contains(":free")

    val contextLabel: String
        get() = when {
            contextLength >= 1000 -> "${contextLength / 1000}k context"
            contextLength > 0 -> "$contextLength context"
            else -> "context unknown"
        }
}

@Serializable
data class Pricing(
    val prompt: String = "0",
    val completion: String = "0"
)

@Serializable
data class OrModelsResponse(val data: List<OrModel> = emptyList())
