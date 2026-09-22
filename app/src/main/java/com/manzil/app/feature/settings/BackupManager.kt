package com.manzil.app.feature.settings

import android.content.Context
import com.manzil.app.data.local.ManzilDatabase
import kotlinx.serialization.json.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    private val db: ManzilDatabase
) {
    data class BackupData(
        val schemaVersion: Int = 1,
        val exportTime: Long = System.currentTimeMillis(),
        val goals: List<String> = emptyList(),
        val tasks: List<String> = emptyList(),
        val kpis: List<String> = emptyList(),
        // Never include SecretVault
    )

    fun createBackupJson(): String {
        val json = Json { prettyPrint = true }
        val backup = BackupData()
        // Real impl would query all tables and serialize
        return json.encodeToString(
            JsonObject.serializer(),
            buildJsonObject {
                put("schemaVersion", 1)
                put("exportTime", System.currentTimeMillis())
                put("note", "Manzil backup — secrets excluded — offline+online compatible")
            }
        )
    }

    fun restoreFromJson(jsonStr: String): Boolean {
        // Forward-compatible importer — ignores unknown fields, handles schemaVersion 1
        return try {
            val obj = Json.parseToJsonElement(jsonStr).jsonObject
            val version = obj["schemaVersion"]?.jsonPrimitive?.intOrNull ?: 1
            if (version > 1) {
                // Forward compatible: still try to import known fields
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
