package com.manzil.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.manzil.app.data.local.entity.SearchDoc

@Dao
interface SearchDao {
    @Query("SELECT * FROM search_docs WHERE id IN (SELECT docId FROM search_docs_fts WHERE search_docs_fts MATCH :query) ORDER BY updatedAt DESC LIMIT 50")
    suspend fun search(query: String): List<SearchDoc>

    @Query("SELECT * FROM search_docs ORDER BY updatedAt DESC LIMIT 20")
    suspend fun getRecent(): List<SearchDoc>
}
