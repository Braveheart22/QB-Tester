package com.qbtester.app.data.remote

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * Partial mapping of Sleeper's `GET /v1/players/nfl` player object. Sleeper returns dozens of
 * fields per player (fantasy stats ids, birth info, college, etc.) - we only declare the ones
 * this app actually uses. `kotlinx.serialization` is configured with `ignoreUnknownKeys = true`
 * (see [com.qbtester.app.di.AppContainer]) so the rest of the payload is simply skipped.
 */
@Serializable
data class SleeperPlayerDto(
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("team") val team: String? = null,
    @SerialName("position") val position: String? = null,
    @SerialName("depth_chart_position") val depthChartPosition: String? = null,
    @SerialName("depth_chart_order") val depthChartOrder: Int? = null,
    @SerialName("active") val active: Boolean? = null,
)
