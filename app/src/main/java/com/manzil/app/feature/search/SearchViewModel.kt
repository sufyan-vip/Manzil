package com.manzil.app.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manzil.app.data.prefs.SettingsRepository
import com.manzil.app.domain.model.SearchHit
import com.manzil.app.domain.usecase.SearchEverything
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchState(
    val query: String = "",
    val filterIndex: Int = 0,
    val hits: List<SearchHit> = emptyList(),
    val recent: List<String> = emptyList(),
    val tookMillis: Long = 0,
    val searching: Boolean = false
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchEverything: SearchEverything,
    private val settings: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            settings.settings.collect { appSettings ->
                _state.value = _state.value.copy(recent = appSettings.recentSearches)
            }
        }
    }

    fun onQueryChange(query: String) {
        _state.value = _state.value.copy(query = query)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (query.length >= 2) {
                delay(150)
                _state.value = _state.value.copy(searching = true)
                val filter = FILTERS.getOrNull(_state.value.filterIndex)
                    ?.takeIf { it != "All" }
                    ?.let { filterLabel ->
                        when (filterLabel) {
                            "Goals" -> "GOAL"
                            "Tasks" -> "TASK"
                            "Events" -> "EVENT"
                            "Journal" -> "JOURNAL"
                            else -> null
                        }
                    }
                val result = searchEverything.search(query, filter)
                _state.value = _state.value.copy(
                    hits = result.hits,
                    tookMillis = result.tookMillis,
                    searching = false
                )
                if (result.hits.isNotEmpty()) {
                    settings.pushRecentSearch(query)
                }
            } else {
                _state.value = _state.value.copy(hits = emptyList(), searching = false)
            }
        }
    }

    fun setFilter(index: Int) {
        _state.value = _state.value.copy(filterIndex = index)
        onQueryChange(_state.value.query)
    }

    fun clearQuery() {
        _state.value = _state.value.copy(query = "", hits = emptyList())
    }

    fun clearRecent() = viewModelScope.launch { settings.clearRecentSearches() }

    companion object {
        val FILTERS = listOf("All", "Goals", "Tasks", "Events", "Journal")
    }
}
