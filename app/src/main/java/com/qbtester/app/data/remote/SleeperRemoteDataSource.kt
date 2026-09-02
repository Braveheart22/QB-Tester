package com.qbtester.app.data.remote

import com.qbtester.app.model.NflTeam
import com.qbtester.app.model.QbLookupResult
import com.qbtester.app.model.Quarterback

/**
 * The ONE place in the app that knows anything about Sleeper's specific JSON shape or how to
 * turn "roster + depth chart" data into a "current starter" answer. Nothing outside this class
 * (and its test) should ever reference [SleeperPlayerDto] - the rest of the app only sees
 * [QbLookupResult]. Swapping data providers later means writing a new class with this same
 * `fetchStartingQuarterbacks(): Map<String, QbLookupResult>` shape and wiring it into
 * [com.qbtester.app.di.AppContainer] - no repository, ViewModel, or UI code should need to change.
 *
 * Starter determination strategy: Sleeper's player records include `depth_chart_position` and
 * `depth_chart_order`, maintained by Sleeper for fantasy football purposes. A player is
 * considered the confirmed starter for a team when they are the ONLY active QB listed at
 * `depth_chart_order == 1` for that team. If zero or more than one player qualifies (missing
 * depth chart data, or a genuinely unsettled QB competition), the team is reported as
 * [QbLookupResult.Unavailable] rather than guessing - see the class-level requirement in the
 * product spec: never teach the user an incorrect answer.
 */
class SleeperRemoteDataSource(
    private val api: SleeperApi,
    private val cdnBaseUrl: String,
) : QbRemoteDataSource {
    override suspend fun fetchStartingQuarterbacks(): Map<String, QbLookupResult> {
        val players = api.getAllPlayers()
        return resolveStarters(players, cdnBaseUrl)
    }

    companion object {
        fun resolveStarters(
            players: Map<String, SleeperPlayerDto>,
            cdnBaseUrl: String,
        ): Map<String, QbLookupResult> {
            val candidatesByTeam: Map<String, List<Pair<String, SleeperPlayerDto>>> = players
                .filter { (_, dto) ->
                    dto.position == "QB" &&
                        dto.depthChartPosition == "QB" &&
                        dto.depthChartOrder == 1 &&
                        dto.active == true &&
                        !dto.team.isNullOrBlank()
                }
                .toList()
                .groupBy { (_, dto) -> dto.team!! }

            return NflTeam.byId.keys.associateWith { teamId ->
                resolveTeam(teamId, candidatesByTeam[teamId].orEmpty(), cdnBaseUrl)
            }
        }

        private fun resolveTeam(
            teamId: String,
            candidates: List<Pair<String, SleeperPlayerDto>>,
            cdnBaseUrl: String,
        ): QbLookupResult = when (candidates.size) {
            0 -> QbLookupResult.Unavailable("No confirmed QB1 found on the depth chart")
            1 -> {
                val (playerId, dto) = candidates.single()
                val name = resolveName(dto)
                if (name == null) {
                    QbLookupResult.Unavailable("Starter found but name data was incomplete")
                } else {
                    QbLookupResult.Available(
                        Quarterback(
                            playerId = playerId,
                            fullName = name,
                            teamId = teamId,
                            headshotUrl = cdnBaseUrl.trimEnd('/') + "/content/nfl/players/$playerId.jpg",
                        )
                    )
                }
            }
            else -> QbLookupResult.Unavailable("Multiple players listed as QB1 - depth chart unsettled")
        }

        private fun resolveName(dto: SleeperPlayerDto): String? {
            val full = dto.fullName?.trim()
            if (!full.isNullOrBlank()) return full
            val combined = "${dto.firstName.orEmpty()} ${dto.lastName.orEmpty()}".trim()
            return combined.ifBlank { null }
        }
    }
}
