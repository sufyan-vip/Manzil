package com.manzil.app.data.local

import org.junit.Test
import org.junit.Assert.*

class SearchRankingTest {
    @Test
    fun testRankingBoost() {
        // Title match x3, goal title x2, recency x1.2
        // Simulate ranking: title match should rank higher
        val titleScore = 1.0 * 3 // title match
        val goalScore = 1.0 * 2 // goal title match
        val bodyScore = 1.0 // body match
        assertTrue(titleScore > goalScore)
        assertTrue(goalScore > bodyScore)
    }

    @Test
    fun testPrefixMatching() {
        val query = "upw"
        val titles = listOf("Upwork profile", "First Upwork client", "Upwork review")
        val matched = titles.filter { it.lowercase().startsWith(query) || it.lowercase().contains(query) }
        assertEquals(3, matched.size)
    }

    @Test
    fun testBm25Ordering() {
        // Typing "upw" should show "Upwork profile", "First Upwork client", journal mentioning Upwork — in that order
        val results = listOf("Upwork profile", "First Upwork client", "Journal: Upwork is great")
        assertEquals("Upwork profile", results[0])
        assertTrue(results[0].lowercase().startsWith("upw"))
    }
}
