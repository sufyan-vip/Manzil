package com.manzil.app.core.diff

data class DiffEntry(val field: String, val from: String, val to: String)

object JsonDiff {
    fun diff(oldJson: String, newJson: String): List<DiffEntry> {
        // Simplified JSON diff - real would use kotlinx.serialization
        return emptyList()
    }
}
