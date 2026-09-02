package com.qbtester.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onStartQuiz: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold { padding ->
        HomeContent(
            state = state,
            padding = padding,
            onStartQuiz = onStartQuiz,
            onRefresh = viewModel::manualRefresh,
        )
    }
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    padding: PaddingValues,
    onStartQuiz: () -> Unit,
    onRefresh: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "QB TESTER",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "How well do you know the NFL's starting quarterbacks?",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp, bottom = 32.dp),
        )

        if (state.isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = onStartQuiz,
                enabled = state.canStartQuiz,
                modifier = Modifier.fillMaxWidth(0.7f),
            ) {
                Text("START QUIZ")
            }

            if (!state.canStartQuiz) {
                Text(
                    text = "No quarterback data available yet. Connect to the internet and refresh.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }

            state.statusMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            Text(
                text = lastUpdatedLabel(state.lastUpdatedEpochMillis),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 28.dp),
            )

            if (state.isRefreshing) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                TextButton(onClick = onRefresh) {
                    Text("Refresh QB Data")
                }
            }
        }
    }
}

private fun lastUpdatedLabel(epochMillis: Long?): String {
    if (epochMillis == null) return "QB data not downloaded yet"
    val date = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault())
    val formatted = date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
    return "QB data updated: $formatted"
}
