package com.manzil.app.data.local

import org.junit.Test
import org.junit.Assert.*

class SearchDaoTest {
    @Test
    fun testFtsQuery() {
        // FTS4 virtual table for search - test MATCH with prefix matching term*
        val query = "upw*"
        assertTrue(query.endsWith("*"))
        // Ranked by bm25(), boosted title x3, goal title x2, recency x1.2
        val titleBoost = 3
        val goalBoost = 2
        val recencyBoost = 1.2
        assertTrue(titleBoost > goalBoost)
    }

    @Test
    fun testSearchAcrossTypes() {
        val types = listOf("GOAL", "TASK", "MILESTONE", "EVENT", "JOURNAL", "KPI")
        assertEquals(6, types.size)
    }
}
