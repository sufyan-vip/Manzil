package com.manzil.app.core.markdown

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoadmapImporterTest {

    private val importer = RoadmapImporterFull()

    @Test
    fun headingsBecomeGoalsAndChecklistsBecomeTasks() {
        val fixture = """
## Phase 0 - Foundation
### Setup
- [x] Create GitHub profile
- [ ] Install VS Code
## Phase 1 - First money
### Outreach
- [ ] Send 10 Instagram DMs
- [ ] Post in 2 Facebook groups
| Savings target | 5500000 | PKR |
| MRR target | 350000 | PKR |
        """.trimIndent()

        val result = importer.import(fixture)

        assertEquals(4, result.goals.size)
        assertEquals(4, result.tasks.size)
        assertTrue(result.tasks.first().done)
        assertEquals(2, result.kpis.size)
        assertEquals(5500000.0, result.kpis.first().target, 0.01)
        assertTrue(importer.validateImport(result))
    }

    @Test
    fun bigRoadmapShapeIsSupported() {
        val large = buildString {
            repeat(20) { index ->
                appendLine("## Goal $index")
                appendLine("### Sub-goal $index")
                appendLine("- [ ] Task $index A")
                appendLine("- [ ] Task $index B")
            }
            appendLine("| Income target | 50000 | PKR |")
        }
        val result = importer.import(large)
        assertTrue(result.goals.size >= 20)
        assertTrue(result.tasks.size >= 40)
        assertEquals(1, result.kpis.size)
    }

    @Test
    fun subHeadingsKeepTheirParent() {
        val result = importer.import(
            """
## Root goal
### Child goal
- [ ] A task
            """.trimIndent()
        )
        assertEquals("Root goal", result.goals[0].title)
        assertEquals("Root goal", result.goals[1].parent)
    }

    @Test
    fun emptyInputIsHarmless() {
        val result = importer.import("")
        assertTrue(result.goals.isEmpty())
        assertTrue(result.tasks.isEmpty())
        assertTrue(result.kpis.isEmpty())
    }
}
