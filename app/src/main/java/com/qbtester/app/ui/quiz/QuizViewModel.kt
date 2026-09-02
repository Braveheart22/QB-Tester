package com.qbtester.app.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qbtester.app.data.repository.QuarterbackRepository
import com.qbtester.app.domain.AnswerMatcher
import com.qbtester.app.model.NflTeam
import com.qbtester.app.model.QbLookupResult
import com.qbtester.app.model.Quarterback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Drives a single 32-team (or fewer, if some teams' starters are currently undetermined) quiz
 * session: randomized team order with no repeats, retry-until-correct-or-give-up scoring, and
 * simple running totals. See [QuizUiState] for the exposed state shape.
 */
class QuizViewModel(
    private val repository: QuarterbackRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private var questions: List<Pair<NflTeam, Quarterback>> = emptyList()
    private var currentIndex = 0

    init {
        startQuiz()
    }

    fun startQuiz() {
        viewModelScope.launch {
            _uiState.value = QuizUiState(phase = QuizPhase.LOADING)

            val snapshot = repository.getSnapshot()
            val available = NflTeam.ALL.mapNotNull { team ->
                val result = snapshot.entries[team.id]
                if (result is QbLookupResult.Available) team to result.quarterback else null
            }
            val skipped = NflTeam.ALL.size - available.size

            if (available.isEmpty()) {
                _uiState.value = QuizUiState(phase = QuizPhase.NO_DATA_AVAILABLE, skippedTeamCount = skipped)
                return@launch
            }

            questions = available.shuffled()
            currentIndex = 0
            _uiState.value = QuizUiState(
                phase = QuizPhase.IN_PROGRESS,
                questionNumber = 1,
                totalQuestions = questions.size,
                currentTeam = questions[0].first,
                skippedTeamCount = skipped,
            )
        }
    }

    fun onInputChanged(text: String) {
        _uiState.update { it.copy(inputText = text, feedback = AnswerFeedback.NONE) }
    }

    fun submitAnswer() {
        val state = _uiState.value
        if (state.phase != QuizPhase.IN_PROGRESS || state.reveal != null) return
        val (_, qb) = questions[currentIndex]

        if (AnswerMatcher.isCorrect(state.inputText, qb.fullName)) {
            _uiState.update {
                it.copy(reveal = RevealState.Correct(qb), correctCount = it.correctCount + 1)
            }
        } else {
            _uiState.update { it.copy(feedback = AnswerFeedback.INCORRECT_TRY_AGAIN, inputText = "") }
        }
    }

    fun giveUp() {
        val state = _uiState.value
        if (state.phase != QuizPhase.IN_PROGRESS || state.reveal != null) return
        val (team, qb) = questions[currentIndex]

        _uiState.update {
            it.copy(
                reveal = RevealState.GaveUp(qb),
                incorrectCount = it.incorrectCount + 1,
                missedTeams = it.missedTeams + MissedTeam(team, qb.fullName),
            )
        }
    }

    fun continueToNext() {
        val state = _uiState.value
        if (state.reveal == null) return

        val nextIndex = currentIndex + 1
        if (nextIndex >= questions.size) {
            _uiState.update { it.copy(phase = QuizPhase.COMPLETE) }
            return
        }

        currentIndex = nextIndex
        _uiState.update {
            it.copy(
                questionNumber = nextIndex + 1,
                currentTeam = questions[nextIndex].first,
                inputText = "",
                feedback = AnswerFeedback.NONE,
                reveal = null,
            )
        }
    }

    fun playAgain() = startQuiz()

    /**
     * Lets the user stop before finishing all 32 teams. Jumps straight to the results screen
     * using whatever correct/incorrect/missed totals have accumulated so far - teams never
     * reached simply aren't counted, same as if the session had only ever had that many teams.
     */
    fun endQuizEarly() {
        if (_uiState.value.phase != QuizPhase.IN_PROGRESS) return
        _uiState.update { it.copy(phase = QuizPhase.COMPLETE) }
    }
}
