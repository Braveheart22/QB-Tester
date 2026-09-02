package com.qbtester.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.qbtester.app.model.NflTeam

val NflTeam.primaryColor: Color get() = Color(primaryColorHex)
val NflTeam.secondaryColor: Color get() = Color(secondaryColorHex)

/**
 * Picks white or near-black text depending on [background]'s luminance so team-color banners
 * stay readable even for very light team colors - accessibility takes priority over always
 * using a fixed on-color.
 */
fun contrastingOnColor(background: Color): Color =
    if (background.luminance() > 0.5f) Color(0xFF10151C) else Color.White
