package com.manzil.app.feature.settings

import org.junit.Test
import org.junit.Assert.*

class BackupRoundTripTest {
    @Test
    fun testBackupCreate() {
        val manager = BackupManager(db = mockDb())
        val json = manager.createBackupJson()
        assertNotNull(json)
        assertTrue(json.contains("schemaVersion"))
        assertFalse(json.contains("sk-or-v1"))
    }
    @Test
    fun testBackupRestore() {
        val manager = BackupManager(db = mockDb())
        val result = manager.restoreFromJson("""{"schemaVersion":1,"exportTime":123}""")
        assertTrue(result)
    }
    @Test
    fun testForwardCompatible() {
        val manager = BackupManager(db = mockDb())
        val result = manager.restoreFromJson("""{"schemaVersion":2,"newField":"ignore"}""")
        assertTrue(result)
    }
    @Test
    fun testInvalidJson() {
        val manager = BackupManager(db = mockDb())
        assertFalse(manager.restoreFromJson("invalid"))
    }
    private fun mockDb(): com.manzil.app.data.local.ManzilDatabase {
        return org.mockito.Mockito.mock(com.manzil.app.data.local.ManzilDatabase::class.java)
    }
}
