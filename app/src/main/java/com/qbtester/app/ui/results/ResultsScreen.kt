package com.qbtester.app.ui.results

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.qbtester.app.ui.quiz.QuizUiState

@Composable
fun ResultsScreen(
    state: QuizUiState,
    onPlayAgain: () -> Unit,
    onHome: () -> Unit,
) {
    var showMissed by remember { mutableStateOf(false) }

    Scaffold { padding ->
        ResultsContent(
            state = state,
            padding = padding,
            showMissed = showMissed,
            onToggleMissed = { showMissed = !showMissed },
            onPlayAgain = onPlayAgain,
            onHome = onHome,
        )
    }
}

@Composable
private fun ResultsContent(
    state: QuizUiState,
    padding: PaddingValues,
    showMissed: Boolean,
    onToggleMissed: () -> Unit,
    onPlayAgain: () -> Unit,
    onHome: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "${state.correctCount} / ${state.resolvedCount} Correct — ${state.percentCorrect}%",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 32.dp, bottom = 32.dp),
        )

        Button(onClick = onPlayAgain, modifier = Modifier.fillMaxWidth(0.7f)) {
            Text("PLAY AGAIN")
        }

        OutlinedButton(
            onClick = onToggleMissed,
            enabled = state.missedTeams.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(0.7f).padding(top = 12.dp),
        ) {
            Text(if (showMissed) "Hide Missed Teams" else "Review Missed Teams")
        }

        OutlinedButton(onClick = onHome, modifier = Modifier.fillMaxWidth(0.7f).padding(top = 12.dp)) {
            Text("Home")
        }

        if (showMissed) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp))
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(state.missedTeams) { missed ->
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Text(text = missed.team.fullName, fontWeight = FontWeight.Bold)
                        Text(text = missed.correctAnswer, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
