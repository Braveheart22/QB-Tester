package com.qbtester.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qbtester.app.data.repository.QuarterbackRepository
import com.qbtester.app.data.repository.RefreshOutcome
import com.qbtester.app.model.QbLookupResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: QuarterbackRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val outcome = repository.refreshIfStale()
            applySnapshot(outcome)
        }
    }

    fun manualRefresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, statusMessage = null) }
            val outcome = repository.forceRefresh()
            applySnapshot(outcome)
        }
    }

    private suspend fun applySnapshot(outcome: RefreshOutcome) {
        val snapshot = repository.getSnapshot()
        val availableCount = snapshot.entries.values.count { it is QbLookupResult.Available }
        val hadCache = snapshot.updatedAtEpochMillis != null

        val message = when {
            outcome !is RefreshOutcome.Failed -> null
            hadCache -> "Couldn't refresh - showing the last data we had"
            else -> "Couldn't load QB data - check your connection and try Refresh"
        }

        _uiState.update {
            it.copy(
                isLoading = false,
                isRefreshing = false,
                lastUpdatedEpochMillis = snapshot.updatedAtEpochMillis,
                canStartQuiz = availableCount > 0,
                statusMessage = message,
            )
        }
    }
}
