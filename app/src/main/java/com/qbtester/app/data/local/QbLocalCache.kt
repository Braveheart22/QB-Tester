package com.qbtester.app.data.local

import com.qbtester.app.model.QbLookupResult

/** Seam so [com.qbtester.app.data.repository.QuarterbackRepositoryImpl] can be tested with a fake. */
interface QbLocalCache {
    suspend fun read(): CachedQbSnapshot?
    suspend fun write(entries: Map<String, QbLookupResult>, updatedAtEpochMillis: Long)
}
