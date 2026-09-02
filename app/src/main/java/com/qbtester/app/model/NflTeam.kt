package com.qbtester.app.model

/**
 * Static, local catalog of team branding facts (name/city/abbreviation/colors).
 * These are stable for a season and are not worth fetching from a network source -
 * only the starting quarterback (see [com.qbtester.app.data.repository.QuarterbackRepository])
 * changes often enough to need a live data source.
 *
 * [id] matches the team abbreviation used by the Sleeper API's player `team` field, so it can be
 * used directly as the join key between branding data and quarterback data.
 */
data class NflTeam(
    val id: String,
    val city: String,
    val name: String,
    val abbreviation: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
) {
    val fullName: String get() = "$city $name"

    /**
     * Team logo, loaded at runtime (never bundled into the APK) from ESPN's long-standing public
     * team-logo CDN, keyed by the same abbreviation used everywhere else in this class. This is
     * decorative only - if it fails to load there's no meaningful fallback, so callers should just
     * let it fail silently (see [com.qbtester.app.ui.components.TeamGradientBackground]).
     */
    val logoUrl: String get() = "https://a.espncdn.com/i/teamlogos/nfl/500/${id.lowercase()}.png"

    companion object {
        val ALL: List<NflTeam> = listOf(
            NflTeam("ARI", "Arizona", "Cardinals", "ARI", 0xFF97233F, 0xFF000000),
            NflTeam("ATL", "Atlanta", "Falcons", "ATL", 0xFFA71930, 0xFF000000),
            NflTeam("BAL", "Baltimore", "Ravens", "BAL", 0xFF241773, 0xFF000000),
            NflTeam("BUF", "Buffalo", "Bills", "BUF", 0xFF00338D, 0xFFC60C30),
            NflTeam("CAR", "Carolina", "Panthers", "CAR", 0xFF0085CA, 0xFF101820),
            NflTeam("CHI", "Chicago", "Bears", "CHI", 0xFF0B162A, 0xFFC83803),
            NflTeam("CIN", "Cincinnati", "Bengals", "CIN", 0xFFFB4F14, 0xFF000000),
            NflTeam("CLE", "Cleveland", "Browns", "CLE", 0xFF311D00, 0xFFFF3C00),
            NflTeam("DAL", "Dallas", "Cowboys", "DAL", 0xFF003594, 0xFF041E42),
            NflTeam("DEN", "Denver", "Broncos", "DEN", 0xFFFB4F14, 0xFF002244),
            NflTeam("DET", "Detroit", "Lions", "DET", 0xFF0076B6, 0xFFB0B7BC),
            NflTeam("GB", "Green Bay", "Packers", "GB", 0xFF203731, 0xFFFFB612),
            NflTeam("HOU", "Houston", "Texans", "HOU", 0xFF03202F, 0xFFA71930),
            NflTeam("IND", "Indianapolis", "Colts", "IND", 0xFF002C5F, 0xFFA2AAAD),
            NflTeam("JAX", "Jacksonville", "Jaguars", "JAX", 0xFF101820, 0xFFD7A22A),
            NflTeam("KC", "Kansas City", "Chiefs", "KC", 0xFFE31837, 0xFFFFB81C),
            NflTeam("LV", "Las Vegas", "Raiders", "LV", 0xFF000000, 0xFFA5ACAF),
            NflTeam("LAC", "Los Angeles", "Chargers", "LAC", 0xFF0080C6, 0xFFFFC20E),
            NflTeam("LAR", "Los Angeles", "Rams", "LAR", 0xFF003594, 0xFFFFA300),
            NflTeam("MIA", "Miami", "Dolphins", "MIA", 0xFF008E97, 0xFFFC4C02),
            NflTeam("MIN", "Minnesota", "Vikings", "MIN", 0xFF4F2683, 0xFFFFC62F),
            NflTeam("NE", "New England", "Patriots", "NE", 0xFF002244, 0xFFC60C30),
            NflTeam("NO", "New Orleans", "Saints", "NO", 0xFFD3BC8D, 0xFF101820),
            NflTeam("NYG", "New York", "Giants", "NYG", 0xFF0B2265, 0xFFA71930),
            NflTeam("NYJ", "New York", "Jets", "NYJ", 0xFF125740, 0xFF000000),
            NflTeam("PHI", "Philadelphia", "Eagles", "PHI", 0xFF004C54, 0xFFA5ACAF),
            NflTeam("PIT", "Pittsburgh", "Steelers", "PIT", 0xFFFFB612, 0xFF101820),
            NflTeam("SF", "San Francisco", "49ers", "SF", 0xFFAA0000, 0xFFB3995D),
            NflTeam("SEA", "Seattle", "Seahawks", "SEA", 0xFF002244, 0xFF69BE28),
            NflTeam("TB", "Tampa Bay", "Buccaneers", "TB", 0xFFD50A0A, 0xFFFF7900),
            NflTeam("TEN", "Tennessee", "Titans", "TEN", 0xFF0C2340, 0xFF4B92DB),
            NflTeam("WAS", "Washington", "Commanders", "WAS", 0xFF5A1414, 0xFFFFB612),
        )

        val byId: Map<String, NflTeam> = ALL.associateBy { it.id }
    }
}
