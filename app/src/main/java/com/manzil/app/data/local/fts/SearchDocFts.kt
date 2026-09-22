package com.manzil.app.data.local.fts

import androidx.room.Entity
import androidx.room.Fts4

@Fts4
@Entity(tableName = "search_docs_fts")
data class SearchDocFts(
    val docId: String,
    val title: String,
    val body: String,
    val goalTitle: String? = null
)
