package com.qbtester.app.ui.quiz

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.qbtester.app.ui.components.QbHeadshotImage
import com.qbtester.app.ui.components.TeamBanner
import com.qbtester.app.ui.components.TeamGradientBackground

@Composable
fun QuizScreen(
    viewModel: QuizViewModel,
    onComplete: () -> Unit,
    onExit: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    if (state.phase == QuizPhase.COMPLETE) {
        onComplete()
        return
    }

    Scaffold { padding ->
        when (state.phase) {
            QuizPhase.LOADING -> LoadingContent(padding)
            QuizPhase.NO_DATA_AVAILABLE -> NoDataContent(padding, onExit)
            QuizPhase.IN_PROGRESS -> QuizInProgressContent(
                state = state,
                padding = padding,
                onInputChanged = viewModel::onInputChanged,
                onSubmit = viewModel::submitAnswer,
                onGiveUp = viewModel::giveUp,
                onContinue = viewModel::continueToNext,
            )
            QuizPhase.COMPLETE -> Unit
        }
    }
}

@Composable
private fun LoadingContent(padding: PaddingValues) {
    Column(
        modifier = Modifier.fillMaxSize().padding(padding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun NoDataContent(padding: PaddingValues, onExit: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "No quarterback data is available right now.",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        TextButton(onClick = onExit, modifier = Modifier.padding(top = 16.dp)) {
            Text("Back to Home")
        }
    }
}

@Composable
private fun QuizInProgressContent(
    state: QuizUiState,
    padding: PaddingValues,
    onInputChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onGiveUp: () -> Unit,
    onContinue: () -> Unit,
) {
    val team = state.currentTeam ?: return

    Column(modifier = Modifier.fillMaxSize().padding(padding)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Question ${state.questionNumber} of ${state.totalQuestions}",
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = "Score: ${state.correctCount} / ${state.resolvedCount}",
                style = MaterialTheme.typography.labelLarge,
            )
        }

        TeamBanner(team = team)

        Box(modifier = Modifier.fillMaxSize()) {
            TeamGradientBackground(team = team, modifier = Modifier.fillMaxSize())

            AnimatedContent(
                targetState = state.reveal,
                transitionSpec = {
                    (fadeIn() + scaleIn(initialScale = 0.92f)) togetherWith fadeOut()
                },
                label = "quiz-reveal-transition",
            ) { reveal ->
                if (reveal == null) {
                    QuestionInput(
                        inputText = state.inputText,
                        feedback = state.feedback,
                        onInputChanged = onInputChanged,
                        onSubmit = onSubmit,
                        onGiveUp = onGiveUp,
                    )
                } else {
                    RevealContent(reveal = reveal, onContinue = onContinue)
                }
            }
        }
    }
}

@Composable
private fun QuestionInput(
    inputText: String,
    feedback: AnswerFeedback,
    onInputChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onGiveUp: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Who is the starting quarterback?",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 20.dp, top = 12.dp),
        )

        OutlinedTextField(
            value = inputText,
            onValueChange = onInputChanged,
            label = { Text("Enter quarterback...") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth(),
        )

        if (feedback == AnswerFeedback.INCORRECT_TRY_AGAIN) {
            Text(
                text = "Not quite - try again!",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        Button(
            onClick = onSubmit,
            enabled = inputText.isNotBlank(),
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        ) {
            Text("SUBMIT")
        }

        TextButton(onClick = onGiveUp, modifier = Modifier.padding(top = 24.dp)) {
            Text("I Give Up", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun RevealContent(reveal: RevealState, onContinue: () -> Unit) {
    val isCorrect = reveal is RevealState.Correct
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = if (isCorrect) "Correct!" else "The starting QB was:",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )

        QbHeadshotImage(
            imageUrl = reveal.quarterback.headshotUrl,
            contentDescription = reveal.quarterback.fullName,
            modifier = Modifier.padding(vertical = 20.dp),
            size = 160.dp,
        )

        Text(
            text = reveal.quarterback.fullName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth(0.7f).padding(top = 28.dp)) {
            Text("CONTINUE")
        }
    }
}
