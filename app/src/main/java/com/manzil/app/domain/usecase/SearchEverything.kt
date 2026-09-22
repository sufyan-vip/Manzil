package com.manzil.app.domain.usecase

import com.manzil.app.data.local.dao.SearchDao
import com.manzil.app.data.local.entity.SearchDoc
import javax.inject.Inject

class SearchEverything @Inject constructor(
    private val searchDao: SearchDao
) {
    suspend fun search(query: String): List<SearchDoc> {
        if (query.isBlank()) return emptyList()
        val ftsQuery = query.split(" ").joinToString(" ") { "$it*" }
        return searchDao.search(ftsQuery)
    }
}
