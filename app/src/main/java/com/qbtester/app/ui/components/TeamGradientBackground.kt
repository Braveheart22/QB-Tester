package com.qbtester.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.qbtester.app.model.NflTeam
import com.qbtester.app.ui.theme.primaryColor
import com.qbtester.app.ui.theme.secondaryColor

private const val GRADIENT_TINT_FRACTION = 0.22f
private const val LOGO_ALPHA = 0.07f

// ESPN's logo PNGs have significant transparent padding baked into the 500x500 canvas, so the
// box needs to be considerably wider than the screen for the actual artwork (not just its
// bounding box) to visibly bleed off both edges.
private val LOGO_SIZE = 600.dp

/**
 * Full-bleed decorative background for the quiz question/reveal area: a soft gradient tinted
 * with the team's colors, with the team's logo huge, centered, and faint behind the content -
 * intentionally sized wider than most phone screens so it bleeds off both edges. Purely
 * decorative: if the logo fails to load, it just silently doesn't appear, leaving the gradient.
 *
 * The gradient is blended with the surface color (not full team-color saturation) specifically
 * so the normal dark-on-light text in the foreground content stays readable regardless of how
 * dark or light a given team's colors are - see the accessibility note on
 * [com.qbtester.app.ui.theme.contrastingOnColor] for the same concern elsewhere in the app.
 */
@Composable
fun TeamGradientBackground(team: NflTeam, modifier: Modifier = Modifier) {
    val surface = MaterialTheme.colorScheme.background
    val tintedPrimary = lerp(surface, team.primaryColor, GRADIENT_TINT_FRACTION)
    val tintedSecondary = lerp(surface, team.secondaryColor, GRADIENT_TINT_FRACTION)

    Box(modifier = modifier.clipToBounds()) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Brush.linearGradient(listOf(tintedPrimary, tintedSecondary)))
        )
        AsyncImage(
            model = team.logoUrl,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .size(LOGO_SIZE)
                .alpha(LOGO_ALPHA),
            contentScale = ContentScale.Fit,
        )
    }
}
