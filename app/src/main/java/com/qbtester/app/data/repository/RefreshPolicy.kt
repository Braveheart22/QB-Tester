package com.qbtester.app.data.repository

/**
 * Decides whether cached quarterback data is stale enough to warrant a network refresh.
 * Kept as a tiny, pure, testable unit so the refresh cadence (currently once a day) can be
 * changed in one place without touching repository or ViewModel logic.
 */
class RefreshPolicy(
    private val staleAfterMillis: Long = DEFAULT_STALE_AFTER_MILLIS,
) {
    fun isStale(lastUpdatedEpochMillis: Long?, nowEpochMillis: Long): Boolean {
        if (lastUpdatedEpochMillis == null) return true
        return nowEpochMillis - lastUpdatedEpochMillis >= staleAfterMillis
    }

    companion object {
        const val DEFAULT_STALE_AFTER_MILLIS: Long = 24L * 60L * 60L * 1000L // 1 day
    }
}
