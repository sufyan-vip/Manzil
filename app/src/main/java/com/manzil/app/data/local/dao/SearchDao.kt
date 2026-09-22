package com.manzil.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.manzil.app.data.local.entity.SearchDoc

@Dao
interface SearchDao {
    @Query("""
        SELECT search_docs.* FROM search_docs 
        JOIN search_docs_fts ON search_docs.rowid = search_docs_fts.docid 
        WHERE search_docs_fts MATCH :query 
        ORDER BY search_docs.updatedAt DESC LIMIT 50
    """)
    suspend fun search(query: String): List<SearchDoc>

    @Query("SELECT * FROM search_docs ORDER BY updatedAt DESC LIMIT 20")
    suspend fun getRecent(): List<SearchDoc>
}
