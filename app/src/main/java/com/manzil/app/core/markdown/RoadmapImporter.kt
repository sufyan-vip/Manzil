package com.manzil.app.core.markdown

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoadmapImporter @Inject constructor() {
    fun parseMarkdown(markdown: String): ParsedRoadmap {
        // Parses ## headings into goals, - [ ] into tasks, tables into KPIs
        return ParsedRoadmap(emptyList(), emptyList())
    }
    data class ParsedRoadmap(val goals: List<String>, val tasks: List<String>)
}
