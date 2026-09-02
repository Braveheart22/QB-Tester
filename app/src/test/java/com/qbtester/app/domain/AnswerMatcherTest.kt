package com.qbtester.app.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnswerMatcherTest {

    @Test
    fun `exact full name matches`() {
        assertTrue(AnswerMatcher.isCorrect("Dak Prescott", "Dak Prescott"))
    }

    @Test
    fun `is case insensitive`() {
        assertTrue(AnswerMatcher.isCorrect("dak prescott", "Dak Prescott"))
    }

    @Test
    fun `ignores leading and trailing whitespace`() {
        assertTrue(AnswerMatcher.isCorrect("  Dak Prescott  ", "Dak Prescott"))
    }

    @Test
    fun `accepts last name only`() {
        assertTrue(AnswerMatcher.isCorrect("Prescott", "Dak Prescott"))
        assertTrue(AnswerMatcher.isCorrect("mahomes", "Patrick Mahomes"))
    }

    @Test
    fun `rejects a bare first name`() {
        assertFalse(AnswerMatcher.isCorrect("Josh", "Josh Allen"))
        assertFalse(AnswerMatcher.isCorrect("Patrick", "Patrick Mahomes"))
    }

    @Test
    fun `ignores periods around initials`() {
        assertTrue(AnswerMatcher.isCorrect("C.J. Stroud", "CJ Stroud"))
        assertTrue(AnswerMatcher.isCorrect("cj stroud", "C.J. Stroud"))
    }

    @Test
    fun `strips generational suffixes from the correct answer for comparison`() {
        assertTrue(AnswerMatcher.isCorrect("Marvin Harrison", "Marvin Harrison Jr."))
        assertTrue(AnswerMatcher.isCorrect("Marvin Harrison Jr", "Marvin Harrison Jr."))
        assertTrue(AnswerMatcher.isCorrect("Harrison", "Marvin Harrison Jr."))
    }

    @Test
    fun `rejects an unrelated name`() {
        assertFalse(AnswerMatcher.isCorrect("Tom Brady", "Patrick Mahomes"))
    }

    @Test
    fun `accepts a phonetic misspelling of the full name`() {
        // The motivating case: "Jaxson" misheard/misspelled as "Jackson" - same Soundex code.
        assertTrue(AnswerMatcher.isCorrect("Jackson Dart", "Jaxson Dart"))
        assertTrue(AnswerMatcher.isCorrect("jackson dart", "Jaxson Dart"))
    }

    @Test
    fun `accepts a phonetic misspelling of a bare last name`() {
        assertTrue(AnswerMatcher.isCorrect("Prescot", "Dak Prescott"))
        assertTrue(AnswerMatcher.isCorrect("Dart", "Jaxson Dart"))
    }

    @Test
    fun `still rejects a name that is close but not phonetically equivalent`() {
        // Different leading consonant sound - Soundex requires the first letter to match, so
        // this must not slip through even though it looks superficially similar.
        assertFalse(AnswerMatcher.isCorrect("Trescott", "Dak Prescott"))
        assertFalse(AnswerMatcher.isCorrect("Tom Brady", "Patrick Mahomes"))
    }

    @Test
    fun `phonetic matching never allows a bare first name through`() {
        assertFalse(AnswerMatcher.isCorrect("Jaxson", "Jaxson Dart"))
        assertFalse(AnswerMatcher.isCorrect("Jackson", "Jaxson Dart"))
    }

    @Test
    fun `phonetic matching ignores very short tokens to avoid coincidental collisions`() {
        assertFalse(AnswerMatcher.isCorrect("Jo", "Dak Prescott"))
    }

    @Test
    fun `blank guess is never correct`() {
        assertFalse(AnswerMatcher.isCorrect("", "Dak Prescott"))
        assertFalse(AnswerMatcher.isCorrect("   ", "Dak Prescott"))
    }
}
