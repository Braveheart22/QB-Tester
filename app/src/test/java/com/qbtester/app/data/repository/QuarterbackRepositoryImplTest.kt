package com.qbtester.app.data.repository

import com.qbtester.app.data.local.CachedQbSnapshot
import com.qbtester.app.data.local.QbLocalCache
import com.qbtester.app.data.remote.QbRemoteDataSource
import com.qbtester.app.model.QbLookupResult
import com.qbtester.app.model.Quarterback
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeRemote(
    var result: Map<String, QbLookupResult> = emptyMap(),
    var shouldThrow: Boolean = false,
) : QbRemoteDataSource {
    var fetchCount = 0
    override suspend fun fetchStartingQuarterbacks(): Map<String, QbLookupResult> {
        fetchCount++
        if (shouldThrow) throw java.io.IOException("network down")
        return result
    }
}

private class FakeCache(initial: CachedQbSnapshot? = null) : QbLocalCache {
    var stored: CachedQbSnapshot? = initial
    var writeCount = 0
    override suspend fun read(): CachedQbSnapshot? = stored
    override suspend fun write(entries: Map<String, QbLookupResult>, updatedAtEpochMillis: Long) {
        writeCount++
        stored = CachedQbSnapshot(updatedAtEpochMillis, entries)
    }
}

private fun availableQb(team: String) = QbLookupResult.Available(
    Quarterback(playerId = "1", fullName = "Test Player", teamId = team, headshotUrl = null)
)

class QuarterbackRepositoryImplTest {

    @Test
    fun `getSnapshot falls back to cache without hitting the network`() = runTest {
        val cache = FakeCache(initial = CachedQbSnapshot(1000L, mapOf("KC" to availableQb("KC"))))
        val remote = FakeRemote()
        val repo = QuarterbackRepositoryImpl(remote, cache, RefreshPolicy())

        val snapshot = repo.getSnapshot()

        assertEquals(1000L, snapshot.updatedAtEpochMillis)
        assertEquals(0, remote.fetchCount)
    }

    @Test
    fun `refreshIfStale does nothing when cache is fresh`() = runTest {
        val now = 1_000_000L
        val cache = FakeCache(initial = CachedQbSnapshot(now - 1000, emptyMap()))
        val remote = FakeRemote()
        val repo = QuarterbackRepositoryImpl(remote, cache, RefreshPolicy(staleAfterMillis = 60_000L)) { now }

        val outcome = repo.refreshIfStale()

        assertTrue(outcome is RefreshOutcome.Skipped)
        assertEquals(0, remote.fetchCount)
    }

    @Test
    fun `refreshIfStale fetches and writes cache when stale`() = runTest {
        val now = 1_000_000L
        val cache = FakeCache(initial = CachedQbSnapshot(0L, emptyMap()))
        val remote = FakeRemote(result = mapOf("KC" to availableQb("KC")))
        val repo = QuarterbackRepositoryImpl(remote, cache, RefreshPolicy(staleAfterMillis = 60_000L)) { now }

        val outcome = repo.refreshIfStale()

        assertTrue(outcome is RefreshOutcome.Success)
        assertEquals(1, remote.fetchCount)
        assertEquals(1, cache.writeCount)
        assertEquals(now, cache.stored?.updatedAtEpochMillis)
    }

    @Test
    fun `a failed refresh preserves the existing cache instead of wiping it`() = runTest {
        val cache = FakeCache(initial = CachedQbSnapshot(0L, mapOf("KC" to availableQb("KC"))))
        val remote = FakeRemote(shouldThrow = true)
        val repo = QuarterbackRepositoryImpl(remote, cache, RefreshPolicy(staleAfterMillis = 0L))

        val outcome = repo.forceRefresh()
        val snapshot = repo.getSnapshot()

        assertTrue(outcome is RefreshOutcome.Failed)
        assertEquals(0L, snapshot.updatedAtEpochMillis)
        assertTrue(snapshot.entries["KC"] is QbLookupResult.Available)
    }

    @Test
    fun `no cache and a failed fetch yields an empty snapshot instead of crashing`() = runTest {
        val cache = FakeCache(initial = null)
        val remote = FakeRemote(shouldThrow = true)
        val repo = QuarterbackRepositoryImpl(remote, cache, RefreshPolicy())

        val outcome = repo.forceRefresh()
        val snapshot = repo.getSnapshot()

        assertTrue(outcome is RefreshOutcome.Failed)
        assertNull(snapshot.updatedAtEpochMillis)
        assertTrue(snapshot.entries.isEmpty())
    }

    @Test
    fun `forceRefresh always fetches even when cache is fresh`() = runTest {
        val now = 1_000_000L
        val cache = FakeCache(initial = CachedQbSnapshot(now, emptyMap()))
        val remote = FakeRemote(result = mapOf("KC" to availableQb("KC")))
        val repo = QuarterbackRepositoryImpl(remote, cache, RefreshPolicy()) { now }

        repo.forceRefresh()

        assertEquals(1, remote.fetchCount)
    }
}
