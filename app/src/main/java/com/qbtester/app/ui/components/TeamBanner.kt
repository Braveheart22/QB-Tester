package com.qbtester.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qbtester.app.model.NflTeam
import com.qbtester.app.ui.theme.TeamHeaderFontFamily
import com.qbtester.app.ui.theme.contrastingOnColor
import com.qbtester.app.ui.theme.primaryColor
import com.qbtester.app.ui.theme.secondaryColor

/**
 * Visibly associates a question with the team being tested using that team's colors, while
 * keeping the rest of the screen on standard Material surfaces for readability - see the
 * accessibility note in [com.qbtester.app.ui.theme.contrastingOnColor].
 */
@Composable
fun TeamBanner(team: NflTeam, modifier: Modifier = Modifier) {
    val onColor = contrastingOnColor(team.primaryColor)
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(team.primaryColor)
                .padding(vertical = 28.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = team.fullName.uppercase(),
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = TeamHeaderFontFamily,
                // Bebas Neue only ships one (already-bold-looking) weight; forcing Bold on top
                // of it would trigger synthetic/faux bolding and distort the letterforms.
                fontWeight = FontWeight.Normal,
                fontSize = 36.sp,
                letterSpacing = 1.sp,
                color = onColor,
                textAlign = TextAlign.Center,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(team.secondaryColor)
        )
    }
}
