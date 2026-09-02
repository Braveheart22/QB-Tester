package com.qbtester.app.ui.quiz

import com.qbtester.app.data.repository.QbDataSnapshot
import com.qbtester.app.data.repository.QuarterbackRepository
import com.qbtester.app.data.repository.RefreshOutcome
import com.qbtester.app.model.NflTeam
import com.qbtester.app.model.QbLookupResult
import com.qbtester.app.model.Quarterback

class FakeQuarterbackRepository(
    entries: Map<String, QbLookupResult> = defaultAllAvailable(),
) : QuarterbackRepository {
    var snapshot = QbDataSnapshot(entries, updatedAtEpochMillis = 1_000L)

    override suspend fun getSnapshot(): QbDataSnapshot = snapshot
    override suspend fun refreshIfStale(): RefreshOutcome = RefreshOutcome.Skipped
    override suspend fun forceRefresh(): RefreshOutcome = RefreshOutcome.Success

    companion object {
        fun defaultAllAvailable(): Map<String, QbLookupResult> = NflTeam.ALL.associate { team ->
            team.id to QbLookupResult.Available(
                Quarterback(
                    playerId = "qb-${team.id}",
                    fullName = "Starter ${team.id}",
                    teamId = team.id,
                    headshotUrl = null,
                )
            )
        }
    }
}
