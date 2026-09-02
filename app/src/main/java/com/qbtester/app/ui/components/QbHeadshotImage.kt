package com.qbtester.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.qbtester.app.R

/**
 * Headshot for a correctly-answered (or given-up) quarterback. Loads asynchronously via Coil
 * (which also handles memory/disk caching) and falls back to a generic silhouette if the URL is
 * missing or the image fails to load, per the "replace gracefully" requirement - we never show a
 * broken-image icon.
 */
@Composable
fun QbHeadshotImage(
    imageUrl: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
) {
    AsyncImage(
        model = imageUrl,
        contentDescription = contentDescription,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentScale = ContentScale.Crop,
        placeholder = painterResource(R.drawable.ic_player_silhouette),
        error = painterResource(R.drawable.ic_player_silhouette),
        fallback = painterResource(R.drawable.ic_player_silhouette),
    )
}
