package com.qbtester.app.domain

/**
 * Decides whether a user's typed guess should be accepted as the given quarterback's name.
 *
 * Deliberately conservative: we normalize case/whitespace/punctuation/suffixes and accept a
 * bare last-name answer, and we forgive "sounds right but spelled wrong" guesses (e.g. "Jackson
 * Dart" for "Jaxson Dart") via [Soundex] phonetic matching on the last name. We do NOT do
 * generic edit-distance fuzzy matching (that could accept an unrelated but similarly-spelled
 * name) and we do NOT accept a bare first name - even phonetically - since many quarterbacks
 * share first names (e.g. "Josh" Allen vs. "Josh" ... well, Justin Herbert, but you get the
 * idea) and a wrong guess must never be silently marked correct.
 */
object AnswerMatcher {

    private val suffixTokens = setOf("jr", "sr", "ii", "iii", "iv", "v")

    /** Below this length, phonetic codes collide too easily to trust (e.g. "jo" vs "joe"). */
    private const val MIN_PHONETIC_TOKEN_LENGTH = 3

    fun isCorrect(guess: String, correctFullName: String): Boolean {
        val normalizedGuess = normalize(guess)
        if (normalizedGuess.isEmpty()) return false

        val normalizedFull = normalize(correctFullName)
        if (normalizedGuess == normalizedFull) return true

        val correctTokens = normalizedFull.split(" ").filter { it.isNotBlank() }
        val lastName = correctTokens.lastOrNull().orEmpty()
        if (lastName.isNotEmpty() && normalizedGuess == lastName) return true

        val guessTokens = normalizedGuess.split(" ").filter { it.isNotBlank() }

        // Bare last name, misspelled but phonetically right: "Dart" typo'd, still just one word.
        if (guessTokens.size == 1 && soundsLikeToken(guessTokens[0], lastName)) return true

        // Full name, misspelled but phonetically right, word-for-word: "Jackson Dart" for
        // "Jaxson Dart". Requires matching word counts so this never degrades into a bare first
        // name matching by coincidence.
        if (guessTokens.size > 1 && guessTokens.size == correctTokens.size &&
            guessTokens.indices.all { i -> soundsLikeToken(guessTokens[i], correctTokens[i]) }
        ) {
            return true
        }

        return false
    }

    private fun soundsLikeToken(a: String, b: String): Boolean {
        if (a.length < MIN_PHONETIC_TOKEN_LENGTH || b.length < MIN_PHONETIC_TOKEN_LENGTH) return false
        return Soundex.soundsLike(a, b)
    }

    /**
     * Normalizes a name for comparison: trims, lowercases, strips periods/commas/apostrophes/
     * hyphens, collapses internal whitespace, and drops trailing generational suffixes
     * (Jr, Sr, II, III, IV, V) so "C.J. Stroud" and "Cj Stroud" and "Odell Beckham Jr" style
     * inputs all compare consistently.
     */
    fun normalize(raw: String): String {
        val cleaned = raw
            .lowercase()
            .replace(Regex("[.,'`-]"), "")
            .trim()
            .replace(Regex("\\s+"), " ")

        if (cleaned.isEmpty()) return cleaned

        val tokens = cleaned.split(" ").toMutableList()
        while (tokens.isNotEmpty() && tokens.last() in suffixTokens) {
            tokens.removeAt(tokens.lastIndex)
        }
        return tokens.joinToString(" ")
    }
}
