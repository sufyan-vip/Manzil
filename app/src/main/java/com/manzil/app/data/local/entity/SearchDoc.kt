package com.manzil.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_docs")
data class SearchDoc(
    @PrimaryKey val id: String,
    val entityType: String,
    val entityId: String,
    val title: String, val body: String,
    val goalTitle: String? = null,
    val dateIso: String? = null,
    val updatedAt: Long
)
