package com.qbtester.app.data.repository

import com.qbtester.app.model.QbLookupResult

/**
 * The single seam between quiz/UI logic and "wherever quarterback data actually comes from."
 * Nothing outside the `data` package should know that Sleeper exists - swapping providers later
 * means writing a new implementation of this interface, not touching the ViewModel or Compose UI.
 */
interface QuarterbackRepository {
    /** Best data currently available (cache, filled in from a network fetch if one has ever succeeded). */
    suspend fun getSnapshot(): QbDataSnapshot

    /** Refreshes from the network only if the cache is stale per the configured [RefreshPolicy]. */
    suspend fun refreshIfStale(): RefreshOutcome

    /** Always attempts a network refresh, e.g. for a manual "Refresh QB Data" action. */
    suspend fun forceRefresh(): RefreshOutcome
}

data class QbDataSnapshot(
    val entries: Map<String, QbLookupResult>,
    val updatedAtEpochMillis: Long?,
)

sealed interface RefreshOutcome {
    data object Skipped : RefreshOutcome
    data object Success : RefreshOutcome
    data class Failed(val message: String) : RefreshOutcome
}
