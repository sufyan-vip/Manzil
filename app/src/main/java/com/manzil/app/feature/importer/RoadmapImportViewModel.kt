package com.manzil.app.feature.importer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manzil.app.core.common.FreeToolsGuide
import com.manzil.app.core.markdown.RoadmapImporterFull
import com.manzil.app.data.repository.ManzilRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ImportState(
    val markdown: String = "",
    val parsed: Boolean = false,
    val goals: List<RoadmapImporterFull.ParsedGoal> = emptyList(),
    val tasks: List<RoadmapImporterFull.ParsedTask> = emptyList(),
    val kpis: List<RoadmapImporterFull.ParsedKpi> = emptyList(),
    val banner: String? = null
)

@HiltViewModel
class RoadmapImportViewModel @Inject constructor(
    private val importer: RoadmapImporterFull,
    private val repository: ManzilRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ImportState())
    val state: StateFlow<ImportState> = _state.asStateFlow()

    fun updateMarkdown(value: String) {
        _state.value = _state.value.copy(markdown = value, parsed = false)
    }

    fun parsePreview() {
        val result = importer.import(_state.value.markdown)
        _state.value = _state.value.copy(
            parsed = true,
            goals = result.goals,
            tasks = result.tasks,
            kpis = result.kpis
        )
    }

    fun loadSample() {
        _state.value = _state.value.copy(markdown = SAMPLE, parsed = false)
        parsePreview()
    }

    fun loadStarterPlan() {
        _state.value = _state.value.copy(markdown = STARTER_PLAN, parsed = false)
        parsePreview()
    }

    fun commit() = viewModelScope.launch {
        val stats = repository.importRoadmap(_state.value.markdown)
        _state.value = _state.value.copy(
            banner = "Imported ${stats.goals} goals, ${stats.tasks} tasks and ${stats.kpis} KPIs.",
            parsed = false,
            markdown = ""
        )
    }

    fun dismissBanner() {
        _state.value = _state.value.copy(banner = null)
    }

    companion object {
        private val SAMPLE = """
## Phase 0 — Foundation
### Setup
- [x] Create GitHub profile
- [ ] Install VS Code + Git
- [ ] freeCodeCamp responsive web design
## Phase 1 — First money
### Outreach
- [ ] Send 10 Instagram DMs
- [ ] Post in 2 Facebook groups
- [ ] 3 free local websites as case studies
## Phase 2 — Retainers
### Convert
- [ ] Turn 2 clients into retainers
- [ ] Set up Wave invoicing
| Savings target | 5500000 | PKR |
| MRR target | 350000 | PKR |
        """.trimIndent()

        private val STARTER_PLAN = FreeToolsGuide.getZeroToGoalRoadmap()
            .replace("Week 1-2: Setup (Rs 0)", "## Week 1-2 — Setup")
            .replace("Week 3-4: Portfolio (Rs 0)", "## Week 3-4 — Portfolio")
            .replace("Week 5-8: First Money (Rs 0)", "## Week 5-8 — First money")
            .replace("Week 9-12: Retainer (Rs 0)", "## Week 9-12 — Retainer")
            .lines()
            .joinToString("\n") { line ->
                when {
                    line.startsWith("## ") -> line
                    line.startsWith("- ") -> "- [ ] " + line.removePrefix("- ")
                    line.startsWith("ZERO SE START") || line.isBlank() -> line
                    line.contains(":") && line.endsWith(":") -> "### " + line.removeSuffix(":")
                    else -> line
                }
            }
    }
}
