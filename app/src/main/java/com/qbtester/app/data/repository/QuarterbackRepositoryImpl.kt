package com.qbtester.app.data.repository

import com.qbtester.app.data.local.CachedQbSnapshot
import com.qbtester.app.data.local.QbLocalCache
import com.qbtester.app.data.remote.QbRemoteDataSource
import kotlinx.coroutines.CancellationException

class QuarterbackRepositoryImpl(
    private val remote: QbRemoteDataSource,
    private val cache: QbLocalCache,
    private val refreshPolicy: RefreshPolicy,
    private val nowProvider: () -> Long = { System.currentTimeMillis() },
) : QuarterbackRepository {

    private var inMemory: CachedQbSnapshot? = null
    private var hasLoadedFromDisk = false

    private suspend fun loaded(): CachedQbSnapshot? {
        if (!hasLoadedFromDisk) {
            inMemory = cache.read()
            hasLoadedFromDisk = true
        }
        return inMemory
    }

    override suspend fun getSnapshot(): QbDataSnapshot {
        val snapshot = loaded()
        return QbDataSnapshot(snapshot?.entries.orEmpty(), snapshot?.updatedAtEpochMillis)
    }

    override suspend fun refreshIfStale(): RefreshOutcome {
        val snapshot = loaded()
        return if (refreshPolicy.isStale(snapshot?.updatedAtEpochMillis, nowProvider())) {
            forceRefresh()
        } else {
            RefreshOutcome.Skipped
        }
    }

    override suspend fun forceRefresh(): RefreshOutcome = try {
        val fresh = remote.fetchStartingQuarterbacks()
        val now = nowProvider()
        cache.write(fresh, now)
        inMemory = CachedQbSnapshot(now, fresh)
        hasLoadedFromDisk = true
        RefreshOutcome.Success
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        RefreshOutcome.Failed(e.message ?: e::class.simpleName ?: "Unknown error refreshing QB data")
    }
}
