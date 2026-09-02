package com.qbtester.app.data.repository

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RefreshPolicyTest {

    private val oneDayMillis = 24L * 60L * 60L * 1000L
    private val policy = RefreshPolicy(staleAfterMillis = oneDayMillis)

    @Test
    fun `no cached timestamp is always stale`() {
        assertTrue(policy.isStale(lastUpdatedEpochMillis = null, nowEpochMillis = 1_000L))
    }

    @Test
    fun `data younger than the interval is not stale`() {
        val now = 10_000_000L
        val updated = now - (oneDayMillis / 2)
        assertFalse(policy.isStale(updated, now))
    }

    @Test
    fun `data older than the interval is stale`() {
        val now = 10_000_000_000L
        val updated = now - oneDayMillis - 1
        assertTrue(policy.isStale(updated, now))
    }

    @Test
    fun `data exactly at the interval boundary is stale`() {
        val now = oneDayMillis
        assertTrue(policy.isStale(lastUpdatedEpochMillis = 0L, nowEpochMillis = now))
    }

    @Test
    fun `custom interval is respected`() {
        val shortPolicy = RefreshPolicy(staleAfterMillis = 60_000L)
        assertTrue(shortPolicy.isStale(lastUpdatedEpochMillis = 0L, nowEpochMillis = 61_000L))
        assertFalse(shortPolicy.isStale(lastUpdatedEpochMillis = 0L, nowEpochMillis = 30_000L))
    }
}
