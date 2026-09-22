package com.manzil.app.core.markdown

import org.junit.Test
import org.junit.Assert.*

class RoadmapImporterTest {

    private val importer = RoadmapImporterFull()

    @Test
    fun test40LineFixture() {
        val fixture = """
## Phase 0 - Foundation
### Learn HTML
- [ ] Build 5 static sites
- [ ] Git & GitHub
### Learn CSS
- [ ] Flexbox/Grid
- [ ] 3 responsive sites
## Phase 1 - JavaScript
### JS Basics
- [ ] ES6+ features
- [ ] DOM manipulation
- [ ] Fetch API
### React
- [ ] React hooks
- [ ] Next.js
- [ ] Tailwind
## Phase 2 - Backend
### Node.js
- [ ] Express
- [ ] REST APIs
- [ ] PostgreSQL
### WordPress
- [ ] Elementor
- [ ] WooCommerce
## Phase 3 - Freelancing
### Upwork Profile
- [ ] 100% complete profile
- [ ] 3 portfolio items
- [ ] 10 proposals/day
### Local Market
- [ ] 3 free websites
- [ ] WhatsApp Business
## Phase 4 - Scale
### Retainer Model
- [ ] Convert clients to retainer
- [ ] 2-3 retainer clients
### Team
- [ ] Hire 1 junior
- [ ] Task delegation
## KPIs
| Metric | Target | Unit |
| Savings | 55,00,000 | PKR |
| MRR | 3,50,000 | PKR |
| Clients | 3 | count |
        """.trimIndent()

        val result = importer.import(fixture)
        // Should parse headings into goals, checkboxes into tasks, tables into KPIs
        assertTrue("Goals should be >=5", result.goals.size >= 5)
        assertTrue("Tasks should be >=10", result.tasks.size >= 10)
        assertTrue("KPIs should be >=1", result.kpis.size >= 1)
        assertTrue(importer.validateImport(result))
    }

    @Test
    fun testSahiwalRoadmapShape() {
        // Simulate Sahiwal roadmap shape: headings + tables + checklists → ≥20 goals and ≥40 tasks
        // For test, we create a larger fixture
        val largeFixture = buildString {
            repeat(20) { i ->
                appendLine("## Goal $i")
                appendLine("### Subgoal $i.1")
                appendLine("- [ ] Task $i.1")
                appendLine("- [ ] Task $i.2")
                appendLine("- [x] Task $i.3 done")
            }
            appendLine("| Savings | 55,00,000 | PKR |")
            appendLine("| MRR | 3,50,000 | PKR |")
        }
        val result = importer.import(largeFixture)
        assertTrue(result.goals.size >= 20)
        assertTrue(result.tasks.size >= 40)
    }
}
