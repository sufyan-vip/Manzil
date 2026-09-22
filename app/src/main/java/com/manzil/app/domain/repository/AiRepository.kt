package com.manzil.app.domain.repository

import com.manzil.app.core.common.Result
import com.manzil.app.data.remote.openrouter.OrModel
import com.manzil.app.data.remote.openrouter.dto.ChatRequest
import com.manzil.app.data.remote.openrouter.dto.ChatResponse
import kotlinx.coroutines.flow.Flow

interface AiRepository {
    suspend fun listModels(): Result<List<OrModel>>
    suspend fun chat(req: ChatRequest): Result<ChatResponse>
    fun chatStream(req: ChatRequest): Flow<String>
}
