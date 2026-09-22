package com.manzil.app.data.backup

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupContractTest {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false; encodeDefaults = true }

    @Test
    fun backupRoundTrips() {
        val backup = BackupManager.Backup(
            goals = listOf(
                BackupManager.GoalRecord(
                    id = "g1",
                    title = "Software house",
                    startDate = "2026-01-01",
                    targetDate = "2029-01-01"
                )
            ),
            tasks = listOf(
                BackupManager.TaskRecord(
                    id = "t1",
                    title = "Send 10 DMs",
                    goalId = "g1",
                    dueDate = "2026-09-22",
                    priority = 1
                )
            ),
            kpis = listOf(BackupManager.KpiRecord(key = "mrr_pkr", value = 0.0, date = "2026-09-22"))
        )

        val encoded = json.encodeToString(BackupManager.Backup.serializer(), backup)
        val decoded = json.decodeFromString(BackupManager.Backup.serializer(), encoded)

        assertEquals(1, decoded.goals.size)
        assertEquals("Send 10 DMs", decoded.tasks.first().title)
        assertEquals(BackupManager.SCHEMA_VERSION, decoded.schemaVersion)
    }

    @Test
    fun backupNeverContainsSecrets() {
        val encoded = json.encodeToString(
            BackupManager.Backup.serializer(),
            BackupManager.Backup()
        )
        assertFalse(encoded.contains("sk-or-v1"))
        assertFalse(encoded.contains("api_key"))
    }

    @Test
    fun newerSchemaStillImports() {
        val future = """
            {"schemaVersion":9,"exportTime":1,"unknownField":"ignore me",
             "goals":[{"id":"g1","title":"Future goal","startDate":"2026-01-01"}]}
        """.trimIndent()
        val decoded = json.decodeFromString(BackupManager.Backup.serializer(), future)
        assertEquals(9, decoded.schemaVersion)
        assertEquals(1, decoded.goals.size)
    }

    @Test
    fun garbageIsRejectedByTheParser() {
        val failed = runCatching {
            json.decodeFromString(BackupManager.Backup.serializer(), "not json at all")
        }.isFailure
        assertTrue(failed)
    }
}
