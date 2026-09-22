package com.manzil.app.data.remote.openrouter.dto

import kotlinx.serialization.Serializable

@Serializable
data class ModelError(val error: ErrorDetail) {
    @Serializable
    data class ErrorDetail(val message: String, val code: Int? = null)
}
