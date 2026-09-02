package com.qbtester.app.data.remote

import retrofit2.http.GET

/**
 * Retrofit interface for Sleeper's free, keyless, public players endpoint.
 * Docs: https://docs.sleeper.com/ ("Fetch all players" under Players).
 * No API key or authentication is required or sent - there is no secret to protect here.
 */
interface SleeperApi {
    @GET("players/nfl")
    suspend fun getAllPlayers(): Map<String, SleeperPlayerDto>
}
