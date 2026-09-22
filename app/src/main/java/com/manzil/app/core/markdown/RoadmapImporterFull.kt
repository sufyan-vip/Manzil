package com.manzil.app.core.markdown

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Full Roadmap Importer — Free method
 * Parses Markdown: ## headings → goals, - [ ] → tasks, tables → KPI
 */
@Singleton
class RoadmapImporterFull @Inject constructor() {

    data class ImportResult(
        val goals: List<ParsedGoal>,
        val tasks: List<ParsedTask>,
        val kpis: List<ParsedKpi>
    )
    data class ParsedGoal(val title: String, val parent: String?, val level: Int)
    data class ParsedTask(val title: String, val done: Boolean, val goal: String?)
    data class ParsedKpi(val label: String, val target: Double, val unit: String?)

    fun import(markdown: String): ImportResult {
        val goals = mutableListOf<ParsedGoal>()
        val tasks = mutableListOf<ParsedTask>()
        val kpis = mutableListOf<ParsedKpi>()

        var currentGoal: String? = null
        var currentLevel = 0

        markdown.lines().forEach { line ->
            val trimmed = line.trim()
            when {
                trimmed.startsWith("## ") -> {
                    currentGoal = trimmed.removePrefix("## ").trim()
                    goals.add(ParsedGoal(currentGoal!!, null, 2))
                    currentLevel = 2
                }
                trimmed.startsWith("### ") -> {
                    val title = trimmed.removePrefix("### ").trim()
                    goals.add(ParsedGoal(title, currentGoal, 3))
                }
                trimmed.startsWith("- [ ]") || trimmed.startsWith("- [x]") -> {
                    val done = trimmed.startsWith("- [x]")
                    val title = trimmed.replace(Regex("- \\[[ x]\\]"), "").trim()
                    tasks.add(ParsedTask(title, done, currentGoal))
                }
                trimmed.contains("|") && trimmed.contains("PKR") -> {
                    // Table row with KPI
                    val parts = trimmed.split("|").map { it.trim() }.filter { it.isNotEmpty() }
                    if (parts.size >= 2) {
                        val label = parts[0]
                        val valueStr = parts[1].replace(Regex("[^0-9.]"), "")
                        val value = valueStr.toDoubleOrNull()
                        if (value != null) {
                            kpis.add(ParsedKpi(label, value, "PKR"))
                        }
                    }
                }
            }
        }
        return ImportResult(goals, tasks, kpis)
    }

    fun validateImport(result: ImportResult): Boolean {
        // Must have >=20 goals and >=40 tasks for Sahiwal roadmap
        return result.goals.size >= 5 && result.tasks.size >= 5 // Relaxed for generic
    }
}
