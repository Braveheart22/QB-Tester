package com.qbtester.app.ui.quiz

import com.qbtester.app.MainDispatcherRule
import com.qbtester.app.model.NflTeam
import com.qbtester.app.model.QbLookupResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class QuizViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `a full session covers all 32 teams exactly once`() {
        val viewModel = QuizViewModel(FakeQuarterbackRepository())
        val state = viewModel.uiState.value

        assertEquals(QuizPhase.IN_PROGRESS, state.phase)
        assertEquals(32, state.totalQuestions)

        val seenTeamIds = mutableSetOf<String>()
        repeat(32) {
            val current = viewModel.uiState.value
            assertFalse("team ${current.currentTeam?.id} was asked twice", current.currentTeam!!.id in seenTeamIds)
            seenTeamIds += current.currentTeam!!.id
            viewModel.giveUp()
            viewModel.continueToNext()
        }

        assertEquals(32, seenTeamIds.size)
        assertEquals(QuizPhase.COMPLETE, viewModel.uiState.value.phase)
    }

    @Test
    fun `teams with no confidently known starter are excluded from the quiz`() {
        val entries = FakeQuarterbackRepository.defaultAllAvailable().toMutableMap()
        entries["KC"] = QbLookupResult.Unavailable("depth chart unsettled")
        val viewModel = QuizViewModel(FakeQuarterbackRepository(entries))

        val state = viewModel.uiState.value

        assertEquals(31, state.totalQuestions)
        assertEquals(1, state.skippedTeamCount)
    }

    @Test
    fun `no data available surfaces a distinct phase instead of a zero-question quiz`() {
        val allUnavailable = NflTeam.ALL.associate { it.id to QbLookupResult.Unavailable("no data") }
        val viewModel = QuizViewModel(FakeQuarterbackRepository(allUnavailable))

        assertEquals(QuizPhase.NO_DATA_AVAILABLE, viewModel.uiState.value.phase)
    }

    @Test
    fun `a wrong answer allows another attempt without advancing or scoring`() {
        val viewModel = QuizViewModel(FakeQuarterbackRepository())
        val team = viewModel.uiState.value.currentTeam!!

        viewModel.onInputChanged("Definitely Not The QB")
        viewModel.submitAnswer()

        val state = viewModel.uiState.value
        assertEquals(AnswerFeedback.INCORRECT_TRY_AGAIN, state.feedback)
        assertEquals(0, state.correctCount)
        assertEquals(0, state.incorrectCount)
        assertEquals(1, state.questionNumber)
        assertEquals(team.id, state.currentTeam?.id)
        assertTrue(state.reveal == null)
    }

    @Test
    fun `a correct answer reveals the quarterback and increments the correct count`() {
        val viewModel = QuizViewModel(FakeQuarterbackRepository())
        val team = viewModel.uiState.value.currentTeam!!
        val correctName = "Starter ${team.id}"

        viewModel.onInputChanged(correctName)
        viewModel.submitAnswer()

        val state = viewModel.uiState.value
        assertEquals(1, state.correctCount)
        assertTrue(state.reveal is RevealState.Correct)
        assertEquals(correctName, state.reveal?.quarterback?.fullName)
    }

    @Test
    fun `give up reveals the answer, counts as incorrect, and records the missed team`() {
        val viewModel = QuizViewModel(FakeQuarterbackRepository())
        val team = viewModel.uiState.value.currentTeam!!

        viewModel.giveUp()

        val state = viewModel.uiState.value
        assertTrue(state.reveal is RevealState.GaveUp)
        assertEquals(1, state.incorrectCount)
        assertEquals(0, state.correctCount)
        assertEquals(1, state.missedTeams.size)
        assertEquals(team.id, state.missedTeams.first().team.id)
    }

    @Test
    fun `continuing after the last question completes the quiz with correct final totals`() {
        val threeTeamEntries = FakeQuarterbackRepository.defaultAllAvailable()
            .entries.take(3).associate { it.key to it.value }
        val viewModel = QuizViewModel(FakeQuarterbackRepository(threeTeamEntries))

        // Q1: correct
        val q1 = viewModel.uiState.value.currentTeam!!.id
        viewModel.onInputChanged("Starter $q1")
        viewModel.submitAnswer()
        viewModel.continueToNext()

        // Q2: give up
        viewModel.giveUp()
        viewModel.continueToNext()

        // Q3: give up
        viewModel.giveUp()
        viewModel.continueToNext()

        val finalState = viewModel.uiState.value
        assertEquals(QuizPhase.COMPLETE, finalState.phase)
        assertEquals(1, finalState.correctCount)
        assertEquals(2, finalState.incorrectCount)
        assertEquals(3, finalState.resolvedCount)
        assertEquals(33, finalState.percentCorrect)
    }

    @Test
    fun `play again reshuffles into a fresh 32-question session`() {
        val viewModel = QuizViewModel(FakeQuarterbackRepository())
        viewModel.giveUp()
        viewModel.continueToNext()
        assertEquals(2, viewModel.uiState.value.questionNumber)

        viewModel.playAgain()

        val state = viewModel.uiState.value
        assertEquals(QuizPhase.IN_PROGRESS, state.phase)
        assertEquals(1, state.questionNumber)
        assertEquals(0, state.correctCount)
        assertEquals(0, state.incorrectCount)
        assertTrue(state.missedTeams.isEmpty())
    }

    @Test
    fun `ending the quiz early completes it using only the progress made so far`() {
        val viewModel = QuizViewModel(FakeQuarterbackRepository())

        // Q1: correct
        val q1 = viewModel.uiState.value.currentTeam!!.id
        viewModel.onInputChanged("Starter $q1")
        viewModel.submitAnswer()
        viewModel.continueToNext()

        // Q2: give up, then end early without continuing
        viewModel.giveUp()
        viewModel.endQuizEarly()

        val state = viewModel.uiState.value
        assertEquals(QuizPhase.COMPLETE, state.phase)
        assertEquals(1, state.correctCount)
        assertEquals(1, state.incorrectCount)
        assertEquals(2, state.resolvedCount)
    }

    @Test
    fun `ending the quiz early before answering anything is a harmless zero result`() {
        val viewModel = QuizViewModel(FakeQuarterbackRepository())

        viewModel.endQuizEarly()

        val state = viewModel.uiState.value
        assertEquals(QuizPhase.COMPLETE, state.phase)
        assertEquals(0, state.resolvedCount)
        assertEquals(0, state.percentCorrect)
    }

    @Test
    fun `ending the quiz early does nothing once it has already completed`() {
        val allUnavailable = FakeQuarterbackRepository.defaultAllAvailable()
            .entries.take(1).associate { it.key to it.value }
        val viewModel = QuizViewModel(FakeQuarterbackRepository(allUnavailable))
        viewModel.giveUp()
        viewModel.continueToNext()
        assertEquals(QuizPhase.COMPLETE, viewModel.uiState.value.phase)

        viewModel.endQuizEarly()

        assertEquals(QuizPhase.COMPLETE, viewModel.uiState.value.phase)
        assertEquals(1, viewModel.uiState.value.incorrectCount)
    }
}
