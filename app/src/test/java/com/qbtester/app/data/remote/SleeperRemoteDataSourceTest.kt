package com.qbtester.app.data.remote

import com.qbtester.app.model.NflTeam
import com.qbtester.app.model.QbLookupResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SleeperRemoteDataSourceTest {

    private val cdn = "https://sleepercdn.com/"

    private fun qb(
        team: String?,
        position: String? = "QB",
        depthChartPosition: String? = "QB",
        depthChartOrder: Int? = 1,
        active: Boolean? = true,
        firstName: String? = "Pat",
        lastName: String? = "Player",
        fullName: String? = "Pat Player",
    ) = SleeperPlayerDto(
        firstName = firstName,
        lastName = lastName,
        fullName = fullName,
        team = team,
        position = position,
        depthChartPosition = depthChartPosition,
        depthChartOrder = depthChartOrder,
        active = active,
    )

    @Test
    fun `resolves the single confirmed QB1 as the starter`() {
        val players = mapOf("100" to qb(team = "KC", fullName = "Patrick Mahomes"))

        val result = SleeperRemoteDataSource.resolveStarters(players, cdn)

        val kc = result.getValue("KC")
        assertTrue(kc is QbLookupResult.Available)
        kc as QbLookupResult.Available
        assertEquals("Patrick Mahomes", kc.quarterback.fullName)
        assertEquals("100", kc.quarterback.playerId)
        assertEquals("https://sleepercdn.com/content/nfl/players/100.jpg", kc.quarterback.headshotUrl)
    }

    @Test
    fun `every catalog team is present in the result even with no matching players`() {
        val result = SleeperRemoteDataSource.resolveStarters(emptyMap(), cdn)
        assertEquals(NflTeam.ALL.map { it.id }.toSet(), result.keys)
        assertTrue(result.values.all { it is QbLookupResult.Unavailable })
    }

    @Test
    fun `two players tied at depth chart order one are unavailable rather than guessed`() {
        val players = mapOf(
            "1" to qb(team = "NYJ", fullName = "Player One"),
            "2" to qb(team = "NYJ", fullName = "Player Two"),
        )

        val result = SleeperRemoteDataSource.resolveStarters(players, cdn)

        assertTrue(result.getValue("NYJ") is QbLookupResult.Unavailable)
    }

    @Test
    fun `inactive player at depth chart order one is not treated as the starter`() {
        val players = mapOf("1" to qb(team = "MIA", active = false))

        val result = SleeperRemoteDataSource.resolveStarters(players, cdn)

        assertTrue(result.getValue("MIA") is QbLookupResult.Unavailable)
    }

    @Test
    fun `non-QB position is ignored even if flagged depth chart position QB`() {
        val players = mapOf("1" to qb(team = "BUF", position = "RB"))

        val result = SleeperRemoteDataSource.resolveStarters(players, cdn)

        assertTrue(result.getValue("BUF") is QbLookupResult.Unavailable)
    }

    @Test
    fun `backup at depth chart order two is not the starter`() {
        val players = mapOf("1" to qb(team = "DAL", depthChartOrder = 2))

        val result = SleeperRemoteDataSource.resolveStarters(players, cdn)

        assertTrue(result.getValue("DAL") is QbLookupResult.Unavailable)
    }

    @Test
    fun `falls back to first plus last name when full_name is missing`() {
        val players = mapOf(
            "5" to qb(team = "SEA", fullName = null, firstName = "Sam", lastName = "Darnold"),
        )

        val result = SleeperRemoteDataSource.resolveStarters(players, cdn)

        val sea = result.getValue("SEA") as QbLookupResult.Available
        assertEquals("Sam Darnold", sea.quarterback.fullName)
    }

    @Test
    fun `player with no team is not attributed to any team`() {
        val players = mapOf("1" to qb(team = null))

        val result = SleeperRemoteDataSource.resolveStarters(players, cdn)

        assertTrue(result.values.all { it is QbLookupResult.Unavailable })
    }
}
