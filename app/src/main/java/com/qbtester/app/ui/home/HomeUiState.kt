package com.qbtester.app.ui.home

data class HomeUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val lastUpdatedEpochMillis: Long? = null,
    val canStartQuiz: Boolean = false,
    val statusMessage: String? = null,
)
