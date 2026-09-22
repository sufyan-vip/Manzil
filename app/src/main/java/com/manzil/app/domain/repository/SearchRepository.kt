package com.manzil.app.domain.repository

import com.manzil.app.data.local.entity.SearchDoc

interface SearchRepository {
    suspend fun search(query: String): List<SearchDoc>
    suspend fun getRecent(): List<SearchDoc>
}
