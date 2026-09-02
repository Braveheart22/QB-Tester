package com.qbtester.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.qbtester.app.model.QbLookupResult
import com.qbtester.app.model.Quarterback
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

val Context.qbDataStore by preferencesDataStore(name = "qb_cache")

data class CachedQbSnapshot(
    val updatedAtEpochMillis: Long,
    val entries: Map<String, QbLookupResult>,
)

/**
 * Persists the last successfully retrieved quarterback data locally (as a single small JSON
 * blob in DataStore Preferences) so the quiz keeps working offline using the last-known-good
 * data, and so the app is not required to make a network call every time it starts.
 */
class QbCacheDataSource(
    private val context: Context,
    private val json: Json,
) : QbLocalCache {
    private val cacheKey = stringPreferencesKey("qb_cache_blob")

    override suspend fun read(): CachedQbSnapshot? {
        val raw = context.qbDataStore.data.first()[cacheKey] ?: return null
        val blob = runCatching { json.decodeFromString<QbCacheBlobDto>(raw) }.getOrNull() ?: return null
        val entries = blob.entries.associate { it.teamId to it.toDomain() }
        return CachedQbSnapshot(blob.updatedAtEpochMillis, entries)
    }

    override suspend fun write(entries: Map<String, QbLookupResult>, updatedAtEpochMillis: Long) {
        val blob = QbCacheBlobDto(
            updatedAtEpochMillis = updatedAtEpochMillis,
            entries = entries.map { (teamId, result) -> CachedQbEntryDto.from(teamId, result) },
        )
        val raw = json.encodeToString(QbCacheBlobDto.serializer(), blob)
        context.qbDataStore.edit { prefs -> prefs[cacheKey] = raw }
    }
}

@Serializable
private data class QbCacheBlobDto(
    val updatedAtEpochMillis: Long,
    val entries: List<CachedQbEntryDto>,
)

@Serializable
private data class CachedQbEntryDto(
    val teamId: String,
    val available: Boolean,
    val playerId: String? = null,
    val fullName: String? = null,
    val headshotUrl: String? = null,
    val unavailableReason: String? = null,
) {
    fun toDomain(): QbLookupResult = if (available && playerId != null && fullName != null) {
        QbLookupResult.Available(Quarterback(playerId, fullName, teamId, headshotUrl))
    } else {
        QbLookupResult.Unavailable(unavailableReason ?: "Unavailable")
    }

    companion object {
        fun from(teamId: String, result: QbLookupResult): CachedQbEntryDto = when (result) {
            is QbLookupResult.Available -> CachedQbEntryDto(
                teamId = teamId,
                available = true,
                playerId = result.quarterback.playerId,
                fullName = result.quarterback.fullName,
                headshotUrl = result.quarterback.headshotUrl,
            )
            is QbLookupResult.Unavailable -> CachedQbEntryDto(
                teamId = teamId,
                available = false,
                unavailableReason = result.reason,
            )
        }
    }
}
