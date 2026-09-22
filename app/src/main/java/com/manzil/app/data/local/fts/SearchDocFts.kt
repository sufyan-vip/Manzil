package com.manzil.app.data.local.fts

import androidx.room.Entity
import androidx.room.Fts4

@Fts4(contentEntity = com.manzil.app.data.local.entity.SearchDoc::class)
@Entity(tableName = "search_docs_fts")
data class SearchDocFts(val title: String, val body: String, val goalTitle: String)
