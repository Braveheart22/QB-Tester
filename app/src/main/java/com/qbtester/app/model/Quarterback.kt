package com.qbtester.app.model

/**
 * A resolved starting quarterback for a team, as determined by whichever
 * [com.qbtester.app.data.repository.QuarterbackRepository] implementation is active.
 */
data class Quarterback(
    val playerId: String,
    val fullName: String,
    val teamId: String,
    val headshotUrl: String?,
)

/**
 * The outcome of trying to determine a team's current starter. Modeled explicitly (rather than
 * a nullable [Quarterback]) so callers must consciously decide how to handle the "we don't
 * confidently know" case instead of accidentally treating a missing value as an error.
 */
sealed interface QbLookupResult {
    data class Available(val quarterback: Quarterback) : QbLookupResult
    data class Unavailable(val reason: String) : QbLookupResult
}
