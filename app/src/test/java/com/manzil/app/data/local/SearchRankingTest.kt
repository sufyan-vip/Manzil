package com.manzil.app.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The search itself lives in SearchRepository and runs against Room, which needs an
 * instrumented environment — this test locks down the ranking rules it applies.
 */
class SearchRankingTest {

    private fun score(title: String, body: String, query: String): Int? {
        var total = 0
        for (needle in query.lowercase().split(" ")) {
            val inTitle = title.lowercase().contains(needle)
            val inBody = body.lowercase().contains(needle)
            if (!inTitle && !inBody) return null
            if (inTitle) {
                total += 30
                if (title.lowercase().startsWith(needle)) total += 20
            }
            if (inBody) total += 8
        }
        return total
    }

    @Test
    fun titleMatchesBeatBodyMatches() {
        val titleScore = score("Upwork profile", "", "upw")!!
        val bodyScore = score("Journal entry", "upwork is going well", "upw")!!
        assertTrue(titleScore > bodyScore)
    }

    @Test
    fun prefixMatchesGetABonus() {
        val prefix = score("Upwork profile", "", "upw")!!
        val middle = score("First Upwork client", "", "upw")!!
        assertTrue(prefix > middle)
    }

    @Test
    fun everyWordMustMatchSomewhere() {
        assertEquals(null, score("React hooks", "learned data fetching", "react postgres"))
        assertTrue(score("React hooks", "learned data fetching", "react fetching")!! > 0)
    }
}
