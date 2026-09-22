package com.manzil.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_docs")
data class SearchDoc(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rowid")
    val rowid: Long = 0,
    val id: String,
    val entityType: String,
    val entityId: String,
    val title: String,
    val body: String,
    val goalTitle: String? = null,
    val dateIso: String? = null,
    val updatedAt: Long
)
