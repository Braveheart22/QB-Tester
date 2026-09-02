package com.qbtester.app.ui.quiz

import com.qbtester.app.model.NflTeam
import com.qbtester.app.model.Quarterback

enum class QuizPhase { LOADING, NO_DATA_AVAILABLE, IN_PROGRESS, COMPLETE }

enum class AnswerFeedback { NONE, INCORRECT_TRY_AGAIN }

data class MissedTeam(val team: NflTeam, val correctAnswer: String)

sealed interface RevealState {
    val quarterback: Quarterback

    data class Correct(override val quarterback: Quarterback) : RevealState
    data class GaveUp(override val quarterback: Quarterback) : RevealState
}

data class QuizUiState(
    val phase: QuizPhase = QuizPhase.LOADING,
    val questionNumber: Int = 0,
    val totalQuestions: Int = 0,
    val currentTeam: NflTeam? = null,
    val inputText: String = "",
    val feedback: AnswerFeedback = AnswerFeedback.NONE,
    val reveal: RevealState? = null,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val missedTeams: List<MissedTeam> = emptyList(),
    val skippedTeamCount: Int = 0,
) {
    val resolvedCount: Int get() = correctCount + incorrectCount
    val percentCorrect: Int
        get() = if (resolvedCount == 0) 0 else (correctCount * 100) / resolvedCount
}
